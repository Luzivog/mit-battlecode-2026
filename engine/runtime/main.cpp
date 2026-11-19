// The native engine's command line:
//   bcx play SPECS.jsonl OUTDIR [-j N] [--dump] [--no-replay] [--no-indicators] [--root REPO]
// Each game is played in a forked child (a fresh "JVM": every static starts over), writing OUTDIR/<id or index>/
// result.json, hashes.txt, replay.bc26 (and dump.txt), as engine/play_java.py does. Result lines are printed in
// spec order. Spec and result fields: engine/AGENTS.md.
#include <sys/resource.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <unistd.h>

#include <time.h>

#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <map>
#include <string>
#include <vector>

#include "gc.h"
#include "rt.h"
#include "sched.h"

// the static binary's writable segments (.data, .bss and the like): the collector's only static roots
#include <link.h>
static int add_writable_roots(dl_phdr_info* info, size_t, void*) {
    for (int i = 0; i < info->dlpi_phnum; i++) {
        const ElfW(Phdr)& ph = info->dlpi_phdr[i];
        if (ph.p_type == PT_LOAD && (ph.p_flags & PF_W)) {
            char* lo = (char*)(info->dlpi_addr + ph.p_vaddr);
            GC_add_roots(lo, lo + ph.p_memsz);
        }
    }
    return 0;
}
void rt_link_arrays();
void rt_init_strings();
void rt_init_core();
extern void* const g_entry_fns[];
extern void (*const g_entry_inits[])();
extern std::string g_bundled_maps;
extern bool g_print_stdout;
void rt_io_set_outputs(FILE* hashes, FILE* dump);
extern "C" const char bcx_engine_version[];
extern "C" const char* const bcx_bot_hashes[];

// FNV-1a 64 over a package's class files (relative path, NUL, bytes, NUL), sorted by path: build.py's classes_hash
#include <dirent.h>
#include <algorithm>
static void list_classes(const std::string& root, const std::string& rel, std::vector<std::string>& out) {
    DIR* d = opendir((root + "/" + rel).c_str());
    if (!d) return;
    while (dirent* e = readdir(d)) {
        std::string n = e->d_name;
        if (n == "." || n == "..") continue;
        std::string r = rel.empty() ? n : rel + "/" + n;
        struct stat st;
        if (stat((root + "/" + r).c_str(), &st) != 0) continue;
        if (S_ISDIR(st.st_mode)) list_classes(root, r, out);
        else if (n.size() > 6 && n.compare(n.size() - 6, 6, ".class") == 0) out.push_back(r);
    }
    closedir(d);
}
static std::string classes_hash(const std::string& classes, const std::string& pkg) {
    std::string root = classes + "/" + pkg;
    std::vector<std::string> files;
    list_classes(root, "", files);
    std::sort(files.begin(), files.end());
    uint64_t h = 0xcbf29ce484222325ull;
    auto mix = [&](const char* p, size_t n) { for (size_t i = 0; i < n; i++) { h ^= (unsigned char)p[i]; h *= 0x100000001b3ull; } };
    for (auto& f : files) {
        mix(f.data(), f.size());
        mix("", 1);
        FILE* fp = fopen((root + "/" + f).c_str(), "rb");
        char buf[1 << 16];
        size_t k;
        while (fp && (k = fread(buf, 1, sizeof buf, fp)) > 0) mix(buf, k);
        if (fp) fclose(fp);
        mix("", 1);
    }
    char out[17];
    snprintf(out, sizeof out, "%016llx", (unsigned long long)h);
    return out;
}
static std::string check_bot(const std::string& pkg, const std::string& classes) {
    for (const char* const* b = bcx_bot_hashes; *b; b += 2) {
        if (pkg != b[0]) continue;
        std::string h = classes_hash(classes, pkg);
        if (h != b[1]) return "the build's " + pkg + " differs from " + classes + "/" + pkg + " (rebuild: engine/build.py)";
        return "";
    }
    return "this build has no bot " + pkg + " (rebuild: engine/build.py)";
}

// ---- a flat JSON object parser (strings, integers, null, booleans): enough for specs
struct Val {
    enum { STR, INT, NUL, BOOL, RAW } kind;  // RAW: any other JSON value (an object, an array, a fraction)
    std::string s;
    long long i = 0;
    std::string raw;  // the value's JSON text
};
typedef std::map<std::string, Val> Obj_;

static bool parse_string(const char*& p, std::string& out) {
    if (*p != '"') return false;
    p++;
    while (*p && *p != '"') {
        if (*p == '\\') {
            p++;
            switch (*p) {
                case 'n': out += '\n'; break;
                case 't': out += '\t'; break;
                case 'u': {
                    unsigned v = strtoul(std::string(p + 1, 4).c_str(), nullptr, 16);
                    if (v < 0x80) out += (char)v;
                    else if (v < 0x800) { out += (char)(0xC0 | (v >> 6)); out += (char)(0x80 | (v & 63)); }
                    else { out += (char)(0xE0 | (v >> 12)); out += (char)(0x80 | ((v >> 6) & 63)); out += (char)(0x80 | (v & 63)); }
                    p += 4;
                    break;
                }
                default: out += *p;
            }
            p++;
        } else out += *p++;
    }
    if (*p != '"') return false;
    p++;
    return true;
}

static void ws(const char*& p) { while (*p == ' ' || *p == '\t' || *p == '\r' || *p == '\n') p++; }

// skips one JSON value of any kind
static bool skip_value(const char*& p) {
    if (*p == '"') { std::string t; return parse_string(p, t); }
    if (*p == '{' || *p == '[') {
        int depth = 0;
        while (*p) {
            if (*p == '"') { std::string t; if (!parse_string(p, t)) return false; continue; }
            if (*p == '{' || *p == '[') depth++;
            else if ((*p == '}' || *p == ']') && --depth == 0) { p++; return true; }
            p++;
        }
        return false;
    }
    const char* s = p;
    while (*p && strchr("+-0123456789.eE", *p)) p++;
    return p != s;
}

static bool parse_object(const char* p, Obj_& o, std::string& err) {
    ws(p);
    if (*p++ != '{') { err = "spec is not a JSON object"; return false; }
    ws(p);
    if (*p == '}') return true;
    while (true) {
        ws(p);
        std::string k;
        if (!parse_string(p, k)) { err = "bad key"; return false; }
        ws(p);
        if (*p++ != ':') { err = "expected ':'"; return false; }
        ws(p);
        Val v;
        const char* vs = p;
        if (*p == '"') { v.kind = Val::STR; if (!parse_string(p, v.s)) { err = "bad string"; return false; } }
        else if (!strncmp(p, "null", 4)) { v.kind = Val::NUL; p += 4; }
        else if (!strncmp(p, "true", 4)) { v.kind = Val::BOOL; v.i = 1; p += 4; }
        else if (!strncmp(p, "false", 5)) { v.kind = Val::BOOL; v.i = 0; p += 5; }
        else {
            char* e;
            v.kind = Val::INT;
            v.i = strtoll(p, &e, 10);
            if (e == p || *e == '.' || *e == 'e' || *e == 'E') {
                v.kind = Val::RAW;
                if (!skip_value(p)) { err = "bad value for " + k; return false; }
            } else p = e;
        }
        v.raw = std::string(vs, p - vs);
        o[k] = v;
        ws(p);
        if (*p == ',') { p++; continue; }
        if (*p == '}') return true;
        err = "expected ',' or '}'";
        return false;
    }
}

static std::string json_str(const std::string& s) {
    std::string o = "\"";
    for (char c : s) {
        if (c == '"' || c == '\\') { o += '\\'; o += c; }
        else if (c == '\n') o += "\\n";
        else if ((unsigned char)c < 0x20) { char b[8]; snprintf(b, sizeof b, "\\u%04x", c); o += b; }
        else o += c;
    }
    return o + "\"";
}

static Obj* jstr(const std::string& s) { return rt_cstring(s.c_str()); }

// SHA-256 of a file in hex, "" if unreadable (the replay digest: tools/play reports its first 16 digits)
static std::string sha256_file(const std::string& path) {
    static const uint32_t K[64] = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98,
        0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786,
        0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8,
        0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819,
        0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a,
        0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7,
        0xc67178f2};
    FILE* f = fopen(path.c_str(), "rb");
    if (!f) return "";
    std::string m;
    char b[1 << 16];
    for (size_t n; (n = fread(b, 1, sizeof b, f)) > 0;) m.append(b, n);
    fclose(f);
    uint64_t bits = (uint64_t)m.size() * 8;
    m += (char)0x80;
    while (m.size() % 64 != 56) m += (char)0;
    for (int i = 7; i >= 0; i--) m += (char)(bits >> (i * 8));
    uint32_t h[8] = {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};
    auto rotr = [](uint32_t x, int n) { return (x >> n) | (x << (32 - n)); };
    for (size_t off = 0; off < m.size(); off += 64) {
        uint32_t w[64];
        const unsigned char* q = (const unsigned char*)m.data() + off;
        for (int i = 0; i < 16; i++) w[i] = (uint32_t)q[4 * i] << 24 | (uint32_t)q[4 * i + 1] << 16 | (uint32_t)q[4 * i + 2] << 8 | q[4 * i + 3];
        for (int i = 16; i < 64; i++)
            w[i] = w[i - 16] + (rotr(w[i - 15], 7) ^ rotr(w[i - 15], 18) ^ (w[i - 15] >> 3)) + w[i - 7] +
                   (rotr(w[i - 2], 17) ^ rotr(w[i - 2], 19) ^ (w[i - 2] >> 10));
        uint32_t a = h[0], bb = h[1], c = h[2], d = h[3], e = h[4], ff = h[5], g = h[6], hh = h[7];
        for (int i = 0; i < 64; i++) {
            uint32_t t1 = hh + (rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25)) + ((e & ff) ^ (~e & g)) + K[i] + w[i];
            uint32_t t2 = (rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22)) + ((a & bb) ^ (a & c) ^ (bb & c));
            hh = g; g = ff; ff = e; e = d + t1; d = c; c = bb; bb = a; a = t1 + t2;
        }
        h[0] += a; h[1] += bb; h[2] += c; h[3] += d; h[4] += e; h[5] += ff; h[6] += g; h[7] += hh;
    }
    char hex[65];
    for (int i = 0; i < 8; i++) snprintf(hex + 8 * i, 9, "%08x", h[i]);
    return hex;
}

static std::string dir_name(const Obj_& spec, size_t index) {
    auto it = spec.find("id");
    if (it == spec.end() || it->second.kind == Val::NUL) return std::to_string(index);
    std::string s = it->second.kind == Val::STR ? it->second.s : std::to_string(it->second.i);
    for (char& c : s)
        if (!(isalnum((unsigned char)c) || strchr("._+=~-", c))) c = '_';
    return s;
}

struct Opts {
    std::string root, outdir;
    bool dump = false, replay = true, indicators = true;
    int jobs = 1;
};

static const char* FIELDS[] = {"a", "b", "map", "seed", "engine", "classes", "classes_a", "classes_b", "id", "cat_rng_skip",
                               "replay", "indicators", nullptr};

// Plays one spec in this (child) process; writes the game's files and returns its result line.
static std::string play_one(const Obj_& spec, const std::string& gdir, const Opts& o) {
    std::string out = "{";
    auto field = [&](const char* k) -> const Val* { auto it = spec.find(k); return it == spec.end() ? nullptr : &it->second; };
    auto add = [&](const std::string& k, const std::string& v) { if (out.size() > 1) out += ", "; out += json_str(k) + ": " + v; };
    for (const char* k : {"id", "a", "b", "map"}) {
        const Val* v = field(k);
        if (!v) continue;
        add(k, v->kind == Val::STR ? json_str(v->s) : v->kind == Val::NUL ? "null" : std::to_string(v->i));
    }
    std::string engine = field("engine") && field("engine")->kind == Val::STR ? field("engine")->s : "1.2.6";
    add("engine", json_str(engine));
    const Val* skipv = field("cat_rng_skip");
    if (skipv) add("cat_rng_skip", skipv->kind == Val::INT ? std::to_string(skipv->i) : "null");
    std::string err;
    for (auto& kv : spec) {   // other fields: copied
        bool known = false;
        for (const char** f = FIELDS; *f; f++) if (kv.first == *f) known = true;
        if (!known) add(kv.first, kv.second.raw);
    }
    const Val* replayv = field("replay");
    const Val* indv = field("indicators");
    for (const char* k : {"replay", "indicators"})
        if (field(k) && field(k)->kind != Val::BOOL) err = std::string("spec field ") + k + " must be a boolean";
    bool indicators = indv ? indv->i != 0 : o.indicators;
    for (const char* k : {"a", "b", "map"})
        if (!field(k) || field(k)->kind != Val::STR) err = std::string("spec field ") + k + " is required (a string)";
    if (skipv && (skipv->kind != Val::INT || skipv->i < 0)) err = "spec field cat_rng_skip must be a non-negative integer";
    if (engine != bcx_engine_version) err = "this build plays engine " + std::string(bcx_engine_version) + ", the spec asks " + engine;
    if (!err.empty()) {
        add("backend", "\"native\"");
        add("error", json_str(err));
        return out;  // closed by the parent with cpu_s
    }
    bool has_seed = field("seed") && field("seed")->kind == Val::INT;
    int seed = has_seed ? (int)field("seed")->i : 0;
    std::string classes = field("classes") ? field("classes")->s : "build/classes";
    auto res = [&](const std::string& p) { return p.size() && p[0] == '/' ? p : o.root + "/" + p; };
    std::string ca = res(field("classes_a") ? field("classes_a")->s : classes);
    std::string cb = res(field("classes_b") ? field("classes_b")->s : classes);
    std::string replay = o.replay ? gdir + "/replay.bc26" : "";
    for (auto [pkg, cl] : {std::pair(field("a")->s, ca), std::pair(field("b")->s, cb)}) {
        std::string e = check_bot(pkg, cl);
        if (!e.empty()) {
            if (has_seed) add("seed", std::to_string(seed));
            add("error", json_str(e));
            add("backend", "\"native\"");
            return out;
        }
    }

    FILE* hashes = fopen((gdir + "/hashes.txt").c_str(), "w");
    FILE* dump = o.dump ? fopen((gdir + "/dump.txt").c_str(), "w") : nullptr;
    rt_io_set_outputs(hashes, dump);
    typedef Obj* (*PlayFn)(Obj*, Obj*, Obj*, Obj*, jboolean, jint, Obj*, jboolean, Obj*, Obj*, jlong, jboolean);
    if (g_entry_inits[0]) g_entry_inits[0]();
    Obj* r = nullptr;
    if (!g_exc)
        r = ((PlayFn)g_entry_fns[0])(jstr(field("a")->s), jstr(field("b")->s), jstr(field("map")->s), jstr(o.root + "/maps"),
                                      has_seed, seed, jstr(replay), o.dump, jstr(ca), jstr(cb), skipv ? (jlong)skipv->i : 0, indicators);
    // a lost write (a full disk) must end the game in an error, not leave a short file that reads as a difference
    bool hashes_bad = !hashes || ferror(hashes) || fclose(hashes) != 0;
    bool dump_bad = dump && (ferror(dump) || fclose(dump) != 0);
    std::string line;
    if (hashes_bad || dump_bad) line = "error cannot write " + gdir + (hashes_bad ? "/hashes.txt" : "/dump.txt");
    else if (g_exc || !r) line = std::string("error uncaught ") + (g_exc ? g_exc->klass->name : "null");
    else {
        char b[4096];
        rt_string_utf8(r, b, sizeof b);
        line = b;
    }
    if (line.rfind("ok ", 0) == 0) {
        char winner[8], wtype[64], hash[32];
        int s, round;
        long long draws = 0;
        sscanf(line.c_str() + 3, "%d %7s %63s %d %31s %lld", &s, winner, wtype, &round, hash, &draws);
        size_t tab = line.find('\t');
        add("seed", std::to_string(s));
        add("winner", json_str(winner));
        add("winner_pkg", json_str(winner[0] == 'A' ? field("a")->s : field("b")->s));
        add("win_type", json_str(wtype));
        add("round", std::to_string(round));
        add("hash", json_str(hash));
        add("cat_rng_draws", std::to_string(draws));
        if (tab != std::string::npos) out += ", " + line.substr(tab + 1);
        if (!replay.empty()) {
            std::string d = sha256_file(replay);
            if (!d.empty()) add("digest", json_str(d.substr(0, 16)));
            if (replayv && !replayv->i) unlink(replay.c_str());
        }
    } else {
        if (has_seed) add("seed", std::to_string(seed));
        add("error", json_str(line.rfind("error ", 0) == 0 ? line.substr(6) : line));
    }
    add("backend", "\"native\"");
    return out;  // closed by the parent with cpu_s
}

static int usage() {
    fprintf(stderr, "usage: bcx play SPECS.jsonl OUTDIR [-j N] [--dump] [--no-replay] [--no-indicators] [--root REPO]\n");
    return 2;
}

int main(int argc, char** argv) {
    if (argc < 4 || strcmp(argv[1], "play")) return usage();
    Opts o;
    const char* specs_path = nullptr;
    {   // the repo root: BCX_ROOT, else the checkout this binary was built in (engine/build/bcx)
        std::string exe(argv[0]);
        char rp0[4096];
        if (realpath(exe.c_str(), rp0)) exe = rp0;
        size_t sl = exe.rfind('/');
        o.root = getenv("BCX_ROOT") ? getenv("BCX_ROOT") : (sl == std::string::npos ? std::string(".") : exe.substr(0, sl) + "/../..");
    }
    for (int i = 2; i < argc; i++) {   // options anywhere
        if (!strcmp(argv[i], "-j") && i + 1 < argc) o.jobs = atoi(argv[++i]);
        else if (!strcmp(argv[i], "--dump")) o.dump = true;
        else if (!strcmp(argv[i], "--no-replay")) o.replay = false;
        else if (!strcmp(argv[i], "--no-indicators")) o.indicators = false;
        else if (!strcmp(argv[i], "--root") && i + 1 < argc) o.root = argv[++i];
        else if (!strcmp(argv[i], "--stdout")) g_print_stdout = true;
        else if (argv[i][0] != '-' && !specs_path) specs_path = argv[i];
        else if (argv[i][0] != '-' && o.outdir.empty()) o.outdir = argv[i];
        else return usage();
    }
    if (!specs_path || o.outdir.empty()) return usage();
    if (const char* bm = getenv("BCX_BUNDLED_MAPS")) g_bundled_maps = bm;
    else {
        std::string exe(argv[0]);
        size_t sl = exe.rfind('/');
        g_bundled_maps = (sl == std::string::npos ? std::string(".") : exe.substr(0, sl)) + "/maps-" + bcx_engine_version;
    }
    char rp[4096];
    if (realpath(o.root.c_str(), rp)) o.root = rp;
    FILE* f = fopen(specs_path, "r");
    if (!f) { fprintf(stderr, "bcx: cannot read %s\n", specs_path); return 2; }
    std::vector<std::string> lines;
    char buf[1 << 16];
    while (fgets(buf, sizeof buf, f)) {
        std::string l(buf);
        while (!l.empty() && (l.back() == '\n' || l.back() == '\r')) l.pop_back();
        if (l.find_first_not_of(" \t") != std::string::npos) lines.push_back(l);
    }
    fclose(f);
    mkdir(o.outdir.c_str(), 0777);

    size_t n = lines.size(), next = 0, done_upto = 0;
    std::vector<std::string> results(n);
    std::vector<bool> finished(n);
    std::map<pid_t, size_t> running;
    int failures = 0;
    auto start = [&](size_t i) {
        Obj_ spec;
        std::string err;
        std::string gdir;
        bool ok = parse_object(lines[i].c_str(), spec, err);
        gdir = o.outdir + "/" + (ok ? dir_name(spec, i) : std::to_string(i));
        mkdir(gdir.c_str(), 0777);
        fflush(stdout);
        fflush(stderr);
        pid_t pid = fork();
        if (pid == 0) {
            timespec t0;
            clock_gettime(CLOCK_MONOTONIC, &t0);
            std::string line;
            if (!ok) line = "{\"backend\": \"native\", \"error\": " + json_str("bad spec: " + err);
            else {
                GC_set_all_interior_pointers(1);
                GC_INIT();
                // in a static binary the collector would take everything from the first page to the end of .bss
                // as roots, code included (90 MB scanned per collection); only writable segments can hold heap
                // pointers
                GC_clear_roots();
                dl_iterate_phdr(add_writable_roots, nullptr);
                // a game's live data is small next to what it allocates: collect rarely
                GC_set_free_space_divisor(1);
                GC_expand_hp((size_t)256 << 20);
                sched_init();
                rt_link_arrays();
                rt_init_strings();
                rt_init_core();
                line = play_one(spec, gdir, o);
            }
            rusage ru;
            getrusage(RUSAGE_SELF, &ru);
            double cpu = ru.ru_utime.tv_sec + ru.ru_utime.tv_usec / 1e6 + ru.ru_stime.tv_sec + ru.ru_stime.tv_usec / 1e6;
            timespec t1;
            clock_gettime(CLOCK_MONOTONIC, &t1);
            long long wall = (t1.tv_sec - t0.tv_sec) * 1000LL + (t1.tv_nsec - t0.tv_nsec) / 1000000;
            char c[96];
            snprintf(c, sizeof c, ", \"cpu_s\": %.2f, \"wall_ms\": %lld}", cpu, wall);
            line += c;
            FILE* rf = fopen((gdir + "/result.json").c_str(), "w");
            fprintf(rf, "%s\n", line.c_str());
            fclose(rf);
            _exit(0);
        }
        running[pid] = i;
    };
    while (done_upto < n) {
        while (next < n && (int)running.size() < o.jobs) start(next++);
        int st;
        pid_t pid = wait(&st);
        if (pid < 0) break;
        size_t i = running[pid];
        running.erase(pid);
        Obj_ spec;
        std::string err;
        parse_object(lines[i].c_str(), spec, err);
        std::string gdir = o.outdir + "/" + (err.empty() ? dir_name(spec, i) : std::to_string(i));
        FILE* rf = fopen((gdir + "/result.json").c_str(), "r");
        std::string line;
        if (rf && fgets(buf, sizeof buf, rf)) { line = buf; while (!line.empty() && line.back() == '\n') line.pop_back(); }
        if (rf) fclose(rf);
        if (line.empty() || !WIFEXITED(st) || WEXITSTATUS(st) != 0) {
            char m[128];
            snprintf(m, sizeof m, "the game process %s %d", WIFSIGNALED(st) ? "died of signal" : "exited with", WIFSIGNALED(st) ? WTERMSIG(st) : WEXITSTATUS(st));
            line = "{" + (spec.count("id") && spec["id"].kind == Val::STR ? "\"id\": " + json_str(spec["id"].s) + ", " : std::string()) +
                   "\"backend\": \"native\", \"error\": " + json_str(m) + "}";
            FILE* wf = fopen((gdir + "/result.json").c_str(), "w");
            if (wf) { fprintf(wf, "%s\n", line.c_str()); fclose(wf); }
        }
        if (line.find("\"error\"") != std::string::npos) failures++;
        results[i] = line;
        finished[i] = true;
        while (done_upto < n && finished[done_upto]) printf("%s\n", results[done_upto++].c_str()), fflush(stdout);
    }
    return failures ? 1 : 0;
}
