// The native engine's command line:
//   bcx play SPECS.jsonl OUTDIR [-j N] [--dump] [--no-replay] [--root REPO]
// Each game is played in a forked child (a fresh "JVM": every static starts over), writing OUTDIR/<id or index>/
// result.json, hashes.txt, replay.bc26 (and dump.txt), as engine/play_java.py does. Result lines are printed in
// spec order. Spec and result fields: engine/AGENTS.md.
#include <sys/resource.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <unistd.h>

#include <cstdio>
#include <cstdlib>
#include <map>
#include <string>
#include <vector>

#include "gc.h"
#include "rt.h"
#include "sched.h"

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
    enum { STR, INT, NUL, BOOL } kind;
    std::string s;
    long long i = 0;
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
        if (*p == '"') { v.kind = Val::STR; if (!parse_string(p, v.s)) { err = "bad string"; return false; } }
        else if (!strncmp(p, "null", 4)) { v.kind = Val::NUL; p += 4; }
        else if (!strncmp(p, "true", 4)) { v.kind = Val::BOOL; v.i = 1; p += 4; }
        else if (!strncmp(p, "false", 5)) { v.kind = Val::BOOL; v.i = 0; p += 5; }
        else {
            char* e;
            v.kind = Val::INT;
            v.i = strtoll(p, &e, 10);
            if (e == p || *e == '.' || *e == 'e' || *e == 'E') { err = "bad value for " + k; return false; }
            p = e;
        }
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
    bool dump = false, replay = true;
    int jobs = 1;
};

static const char* FIELDS[] = {"a", "b", "map", "seed", "engine", "classes", "classes_a", "classes_b", "id", nullptr};

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
    std::string err;
    for (auto& kv : spec) {
        bool known = false;
        for (const char** f = FIELDS; *f; f++) if (kv.first == *f) known = true;
        if (!known) err = "unknown spec field " + kv.first;
    }
    for (const char* k : {"a", "b", "map"})
        if (!field(k) || field(k)->kind != Val::STR) err = std::string("spec field ") + k + " is required (a string)";
    if (engine != bcx_engine_version) err = "this build plays engine " + std::string(bcx_engine_version) + ", the spec asks " + engine;
    if (!err.empty()) {
        add("backend", "\"native\"");
        add("error", json_str(err));
        return out + "}";
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
    typedef Obj* (*PlayFn)(Obj*, Obj*, Obj*, Obj*, jboolean, jint, Obj*, jboolean, Obj*, Obj*);
    if (g_entry_inits[0]) g_entry_inits[0]();
    Obj* r = nullptr;
    if (!g_exc)
        r = ((PlayFn)g_entry_fns[0])(jstr(field("a")->s), jstr(field("b")->s), jstr(field("map")->s), jstr(o.root + "/maps"),
                                      has_seed, seed, jstr(replay), o.dump, jstr(ca), jstr(cb));
    fclose(hashes);
    if (dump) fclose(dump);
    std::string line;
    if (g_exc || !r) line = std::string("error uncaught ") + (g_exc ? g_exc->klass->name : "null");
    else {
        char b[4096];
        rt_string_utf8(r, b, sizeof b);
        line = b;
    }
    if (line.rfind("ok ", 0) == 0) {
        char winner[8], wtype[64], hash[32];
        int s, round;
        sscanf(line.c_str() + 3, "%d %7s %63s %d %31s", &s, winner, wtype, &round, hash);
        add("seed", std::to_string(s));
        add("winner", json_str(winner));
        add("winner_pkg", json_str(winner[0] == 'A' ? field("a")->s : field("b")->s));
        add("win_type", json_str(wtype));
        add("round", std::to_string(round));
        add("hash", json_str(hash));
    } else {
        if (has_seed) add("seed", std::to_string(seed));
        add("error", json_str(line.rfind("error ", 0) == 0 ? line.substr(6) : line));
    }
    add("backend", "\"native\"");
    return out;  // closed by the parent with cpu_s
}

static int usage() {
    fprintf(stderr, "usage: bcx play SPECS.jsonl OUTDIR [-j N] [--dump] [--no-replay] [--root REPO]\n");
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
            std::string line;
            if (!ok) line = "{\"backend\": \"native\", \"error\": " + json_str("bad spec: " + err);
            else {
                GC_set_all_interior_pointers(1);
                GC_INIT();
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
            char c[64];
            snprintf(c, sizeof c, ", \"cpu_s\": %.2f}", cpu);
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
