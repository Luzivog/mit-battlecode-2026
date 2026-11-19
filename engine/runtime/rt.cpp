#include "rt.h"

#include <unistd.h>

#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <string>

#include "gc.h"
#include "gc/gc_inline.h"
#include "sched.h"

Obj* g_exc;
void* g_S;
Monitor g_mon;
bool g_mon_slow;

#define PRIM(c, n) Klass K_prim_##c = {n, nullptr, nullptr, nullptr, 0, 0, nullptr, 0, KF_PRIMITIVE, #c[0], nullptr, nullptr, nullptr, 0, -1, nullptr, 0, nullptr, nullptr, nullptr};
PRIM(Z, "boolean") PRIM(B, "byte") PRIM(C, "char") PRIM(S, "short") PRIM(I, "int") PRIM(J, "long") PRIM(F, "float")
PRIM(D, "double") PRIM(V, "void")

void rt_fatal(const char* fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    fprintf(stderr, "bcx: fatal: ");
    vfprintf(stderr, fmt, ap);
    fprintf(stderr, "\n");
    va_end(ap);
    fflush(stderr);
    _exit(70);
}

static size_t elem_size(char e) {
    switch (e) {
        case 'Z': case 'B': return 1;
        case 'C': case 'S': return 2;
        case 'I': case 'F': return 4;
        case 'J': case 'D': return 8;
        default: return sizeof(Obj*);
    }
}

// Small pointerful objects come from free lists of our own, refilled a block at a time by GC_generic_malloc_many
// (the collector's own fast path, gc_inline.h): a pop instead of a call. The lists' heads are in .bss and each
// object links the next through its first word, so the collector sees them all as live (pointer-free objects would
// hide their links from it, so those stay with GC_MALLOC_ATOMIC). The size classes are GC_malloc's: with interior
// pointers on, one byte more (GC_generic_malloc_many takes the whole granules).
static void* g_fl[GC_TINY_FREELISTS];
static inline void* fast_alloc(size_t bytes) {
    size_t g = (bytes + 1 + GC_GRANULE_BYTES - 1) / GC_GRANULE_BYTES;
    if (UNLIKELY(g >= GC_TINY_FREELISTS)) return GC_MALLOC(bytes);
    void** fl = &g_fl[g];
    void* p = *fl;
    if (UNLIKELY(!p)) {
        GC_generic_malloc_many(g * GC_GRANULE_BYTES, GC_I_NORMAL, fl);  // whole granules, the extra byte included
        p = *fl;
        if (!p) return nullptr;
    }
    *fl = *(void**)p;
    *(void**)p = nullptr;  // the link; the rest is clear
    return p;
}

Obj* rt_new(Klass* k) {
    Obj* o = (Obj*)fast_alloc(k->size < (int)sizeof(Obj) ? sizeof(Obj) : k->size);
    if (!o) rt_fatal("out of memory allocating %s", k->name);
    o->klass = k;
    return o;
}

Obj* rt_newarray(Klass* ak, jint n) {
    size_t es = elem_size(ak->elem);
    size_t bytes = sizeof(ArrHdr) + es * (size_t)n;
    Obj* a;
    // an array of a system enum holds only its constants (or null), which the enum's statics keep alive: the
    // collector need not scan it (the cats' BFS tables are such arrays, tiles squared of them)
    Klass* c = ak->component;
    if (ak->elem == 'L' && !((c->flags & KF_ENUM) && c->module == 0)) a = (Obj*)fast_alloc(bytes);
    else {
        a = (Obj*)GC_MALLOC_ATOMIC(bytes);
        if (a) memset(a, 0, bytes);
    }
    if (!a) rt_fatal("out of memory allocating %s[%d]", ak->name, n);
    a->klass = ak;
    ALEN(a) = n;
    return a;
}

static Obj* multi(Klass* ak, int dims, const jint* counts) {
    Obj* a = rt_newarray(ak, counts[0]);
    if (dims > 1) {
        for (jint i = 0; i < counts[0]; i++) {
            Obj* sub = multi(ak->component, dims - 1, counts + 1);
            if (g_exc) return nullptr;
            ADATA(Obj*, a)[i] = sub;
        }
    }
    return a;
}

Obj* rt_multianewarray(Klass* ak, int dims, ...) {
    jint counts[256];
    va_list ap;
    va_start(ap, dims);
    for (int i = 0; i < dims; i++) counts[i] = va_arg(ap, jint);
    va_end(ap);
    for (int i = 0; i < dims; i++)
        if (counts[i] < 0) { rt_throw_negsize(counts[i]); return nullptr; }
    return multi(ak, dims, counts);
}

Obj* rt_array_clone(Obj* a) {
    Klass* ak = a->klass;
    jint n = ALEN(a);
    Obj* b = rt_newarray(ak, n);
    memcpy(ADATA(char, b), ADATA(char, a), elem_size(ak->elem) * (size_t)n);
    return b;
}

static Klass* object_klass() {
    static Klass* o;
    if (!o)
        for (int i = 0; i < g_n_klasses; i++)
            if (!strcmp(g_all_klasses[i]->name, "java/lang/Object")) o = g_all_klasses[i];
    return o;
}

Klass* rt_array_of(Klass* comp) {
    if (comp->array_klass) return comp->array_klass;
    Klass* object = object_klass();
    Klass* ak = (Klass*)GC_MALLOC_UNCOLLECTABLE(sizeof(Klass));
    std::string nm;
    if (comp->flags & KF_ARRAY) nm = std::string("[") + comp->name;
    else if (comp->flags & KF_PRIMITIVE) nm = std::string("[") + comp->elem;
    else nm = std::string("[L") + comp->name + ";";
    ak->name = strdup(nm.c_str());
    ak->super = object;
    ak->display = object->display;
    ak->depth = 0;
    ak->flags = KF_ARRAY;
    ak->elem = (comp->flags & KF_PRIMITIVE) ? comp->elem : 'L';
    ak->component = comp;
    ak->module = comp->module;
    ak->init_index = -1;
    ak->vtable_len = object->vtable_len;
    ak->vtable = object->vtable;
    comp->array_klass = ak;
    return ak;
}

// ---- type checks
static bool is_named(Klass* t, const char* n) { return strcmp(t->name, n) == 0; }

bool rt_subtype(Klass* s, Klass* t) {
    if (s == t) return true;
    if (s->flags & KF_ARRAY) {
        if (t->flags & KF_ARRAY) {
            Klass *sc = s->component, *tc = t->component;
            if ((sc->flags & KF_PRIMITIVE) || (tc->flags & KF_PRIMITIVE)) return sc == tc;
            return rt_subtype(sc, tc);
        }
        if (t->flags & KF_INTERFACE) return is_named(t, "java/lang/Cloneable") || is_named(t, "java/io/Serializable");
        return t->super == nullptr;  // java.lang.Object
    }
    if (t->flags & KF_ARRAY) return false;
    if (t->flags & KF_INTERFACE) {
        for (int i = 0; i < s->n_ifaces; i++)
            if (s->ifaces[i] == t) return true;
        return false;
    }
    return s->depth >= t->depth && s->display[t->depth] == t;
}

bool rt_store_ok(Obj* arr, Obj* v) {
    Klass* c = arr->klass->component;
    return rt_instanceof(v, c);
}

// a receiver that does not implement the interface: every entry throws IncompatibleClassChangeError (the call's
// arguments are ignored and its result is never read, since an exception is pending)
static Klass* g_icce_k;
static Klass* g_icce_i;
static void icce_stub() {
    std::string m = "Class " + std::string(g_icce_k->name) + " does not implement the requested interface " + g_icce_i->name;
    for (char& c : m) if (c == '/') c = '.';
    rt_throw_new_icce(rt_cstring(m.c_str()));
}
static void* g_icce_table[1024];

void** rt_itable(Klass* k, Klass* iface) {
    for (int i = 0; i < k->n_ifaces; i++)
        if (k->ifaces[i] == iface) return k->itables[i];
    g_icce_k = k;
    g_icce_i = iface;
    if (!g_icce_table[0]) for (auto& e : g_icce_table) e = (void*)&icce_stub;
    return g_icce_table;
}

// ---- exceptions with messages built here
Obj* rt_cstring(const char* s) {
    size_t n = strlen(s);
    jchar buf[512];
    if (n > 512) n = 512;
    for (size_t i = 0; i < n; i++) buf[i] = (uint8_t)s[i];
    return rt_make_string(buf, (jint)n);
}

void rt_string_utf8(Obj* s, char* buf, size_t cap) {
    if (!s) { snprintf(buf, cap, "null"); return; }
    jint n = rt_string_length(s);
    size_t j = 0;
    for (jint i = 0; i < n && j + 4 < cap; i++) {
        jchar c = rt_string_char(s, i);
        if (c < 0x80) buf[j++] = (char)c;
        else if (c < 0x800) { buf[j++] = (char)(0xC0 | (c >> 6)); buf[j++] = (char)(0x80 | (c & 63)); }
        else { buf[j++] = (char)(0xE0 | (c >> 12)); buf[j++] = (char)(0x80 | ((c >> 6) & 63)); buf[j++] = (char)(0x80 | (c & 63)); }
    }
    buf[j] = 0;
}

static std::string dotted(const char* n) {
    std::string s(n);
    for (char& c : s) if (c == '/') c = '.';
    return s;
}

void rt_throw_arith() { rt_throw_new_arith(rt_cstring("/ by zero")); }
void rt_throw_aioobe(jint i, jint len) {
    char b[96];
    snprintf(b, sizeof b, "Index %d out of bounds for length %d", i, len);
    rt_throw_new_aioobe(rt_cstring(b));
}
void rt_throw_cce(Obj* o, Klass* k) {
    std::string m = "class " + dotted(o->klass->name) + " cannot be cast to class " + dotted(k->name);
    rt_throw_new_cce(rt_cstring(m.c_str()));
}
void rt_throw_negsize(jint n) {
    char b[32];
    snprintf(b, sizeof b, "%d", n);
    rt_throw_new_negsize(rt_cstring(b));
}
void rt_throw_ase(Obj* v) { rt_throw_new_ase(rt_cstring(dotted(v->klass->name).c_str())); }
void rt_throw_ncdfe(Klass* k) {
    std::string m = "Could not initialize class " + dotted(k->name);
    rt_throw_new_ncdfe(rt_cstring(m.c_str()));
}
void rt_abstract_method() { rt_throw_new_ame(nullptr); }
void rt_no_receiver_fail(int s) {
    char b[512];
    rt_string_utf8(g_str[s], b, sizeof b);
    rt_fatal("virtual call with no possible receiver: %s", b);
}

// ---- RobotMonitor (engine/src/main/battlecode/instrumenter/inject/RobotMonitor.java)
// BCX_TRACE=FILE logs every incrementBytecodes (robot, n, left, toRemove, debugLevel), as a debugging copy of
// RobotMonitor can on the official engine, to find the first differing count
FILE* g_bc_trace;
static long g_bc_trace_n;
static void mon_flags() { g_mon_slow = g_mon.should_die || g_mon.debug_level != 0 || g_mon.to_remove != 0 || g_bc_trace; }

void rt_bc_slow(jint n) {
    if (g_bc_trace && ++g_bc_trace_n <= 20000000)
    {
        fprintf(g_bc_trace, "%d %d %d %d %d\n", g_mon.seed, n, g_mon.left, g_mon.to_remove, g_mon.debug_level);
        static long stop = getenv("BCX_TRACE_STOP") ? atol(getenv("BCX_TRACE_STOP")) : -1;
        if (g_bc_trace_n == stop) __builtin_debugtrap();
    }
    if (g_mon.should_die) sched_robot_die();
    if (g_mon.debug_level == 0) {
        int64_t t = (int64_t)g_mon.left - n;
        if (t < INT32_MIN) g_mon.left = INT32_MIN;
        else {
            int64_t u = t - g_mon.to_remove;
            g_mon.left = u < INT32_MIN ? INT32_MIN : (jint)u;
        }
        while (g_mon.left <= 0) rt_pause();
    }
    g_mon.to_remove = 0;
    mon_flags();
}

void rt_bc_defer(jint n) {
    if (n > 0) {
        int64_t t = (int64_t)g_mon.to_remove + n;
        g_mon.to_remove = t > INT32_MAX ? INT32_MAX : (jint)t;
    }
    mon_flags();
}

jint rt_bc_multi_cost(Obj* dims) {
    jint cost = 1;
    for (jint i = ALEN(dims) - 1; i >= 0; i--) {
        jint d = ADATA(jint, dims)[i];
        cost = (jint)((juint)cost * (juint)(d > 1 ? d : 1));
    }
    return cost;
}

void rt_pause() {
    sched_pause();
    // reactivate()
    if (g_mon.should_die) sched_robot_die();
    if (g_mon.left < 0) g_mon.left += g_mon.limit;
    else g_mon.left = g_mon.limit;
}

void rt_debug_inc() { g_mon.debug_level++; mon_flags(); }
void rt_debug_dec() {
    g_mon.debug_level--;
    if (g_mon.debug_level < 0) { g_mon.should_die = true; }
    mon_flags();
}
void rt_monitor_noop() {}

// ---- identity tables
static juint slot_of(Obj* k, jint cap) { return (juint)(((uintptr_t)k >> 4) * 2654435761u) & (juint)(cap - 1); }

jint idhash_get(IdHashTable* h, Obj* o, jint (*fresh)(IdHashTable*)) {
    if (h->keys) {
        for (juint i = slot_of(o, h->cap); h->vals[i] != INT32_MIN; i = (i + 1) & (juint)(h->cap - 1))
            if (h->keys[i] == o) return h->vals[i];
    }
    if (!h->keys || (h->n + 1) * 2 > h->cap) {
        jint ncap = h->keys ? h->cap * 2 : 64;
        Obj** nk = (Obj**)GC_MALLOC(sizeof(Obj*) * ncap);
        jint* nv = (jint*)GC_MALLOC_ATOMIC(sizeof(jint) * ncap);
        for (jint i = 0; i < ncap; i++) nv[i] = INT32_MIN;
        for (jint j = 0; h->keys && j < h->cap; j++) {
            if (h->vals[j] == INT32_MIN) continue;
            juint i = slot_of(h->keys[j], ncap);
            while (nv[i] != INT32_MIN) i = (i + 1) & (juint)(ncap - 1);
            nk[i] = h->keys[j];
            nv[i] = h->vals[j];
        }
        h->keys = nk;
        h->vals = nv;
        h->cap = ncap;
    }
    juint i = slot_of(o, h->cap);
    while (h->vals[i] != INT32_MIN) i = (i + 1) & (juint)(h->cap - 1);
    jint code = fresh(h);
    h->keys[i] = o;
    h->vals[i] = code;
    h->n++;
    return code;
}

Obj** kcache_slot(KCache** head, Klass* k, bool uncollectable) {
    for (KCache* c = *head; c; c = c->next)
        if (c->k == k) return &c->v;
    KCache* c = (KCache*)(uncollectable ? GC_MALLOC_UNCOLLECTABLE(sizeof(KCache)) : GC_MALLOC(sizeof(KCache)));
    c->k = k;
    c->v = nullptr;
    c->next = *head;
    *head = c;
    return &c->v;
}
