// The native layer under translated Java: objects, arrays, type checks, exceptions, arithmetic with Java's
// semantics, and the per-robot bytecode monitor (battlecode.instrumenter.inject.RobotMonitor, line for line).
#pragma once
#include <bit>
#include <cmath>
#include <cstddef>
#include <cstdint>
#include <cstring>

typedef int32_t jint;
typedef uint32_t juint;
typedef int64_t jlong;
typedef uint64_t julong;
typedef float jfloat;
typedef double jdouble;
typedef int8_t jbyte;
typedef uint16_t jchar;
typedef int16_t jshort;
typedef uint8_t jboolean;

#define UNLIKELY(x) __builtin_expect(!!(x), 0)
#define LIKELY(x) __builtin_expect(!!(x), 1)

struct Klass;
struct Obj {
    Klass* klass;
};
struct ArrHdr : Obj {
    jint length;
    jint pad;
};
// generated code: exception check, null check, class initialization check
#define CK(X) if (UNLIKELY(g_exc != nullptr)) goto X
#define NN(v, X) if (UNLIKELY((v) == nullptr)) { rt_throw_npe(); goto X; }
#define INIT(st, fn, X) if (UNLIKELY((st) != 2)) { fn(); CK(X); }
#define ALEN(a) (((ArrHdr*)(a))->length)
#define ADATA(T, a) ((T*)((char*)(a) + sizeof(ArrHdr)))

enum { KF_INTERFACE = 1, KF_ARRAY = 2, KF_ABSTRACT = 4, KF_ENUM = 8, KF_PRIMITIVE = 16 };

struct Klass {
    const char* name;  // internal name: java/lang/String, [I, [Ljava/lang/String;, int
    Klass* super;
    Klass** ifaces;    // every superinterface, transitively
    void*** itables;   // parallel to ifaces (classes only)
    int32_t n_ifaces;
    int32_t depth;     // superclass chain length (Object: 0)
    Klass** display;   // display[0] = Object ... display[depth] = this
    int32_t size;      // instance size
    int32_t flags;
    char elem;         // arrays: element descriptor char ('L' for references)
    Klass* component;  // arrays: component type
    Klass* array_klass;
    Obj* mirror;       // java.lang.Class
    int32_t module;    // 0: system, else the team module
    int32_t init_index;
    void* enum_values; // enums: static values()
    int32_t vtable_len;
    void** vtable;
    const char* gsig;  // the Signature attribute, or null
    void (*init)();    // the class's initialization (JVMS 5.5), or null
};

// identity -> code tables (ObjectMethods per robot, Object.hashCode for system code); keys stay alive
struct IdHashTable {
    Obj** keys;
    jint* vals;
    jint cap, n, last;
};
jint idhash_get(IdHashTable* h, Obj* o, jint (*fresh)(IdHashTable*));
// a GC-visible list Klass -> Obj* (enum constants caches)
struct KCache {
    Klass* k;
    Obj* v;
    KCache* next;
};
Obj** kcache_slot(KCache** head, Klass* k, bool uncollectable);
extern Klass* const g_all_klasses[];
extern const int g_n_klasses;

struct ModuleInfo {
    const char* name;
    size_t statics_size;
    size_t init_offset;
    void (*init_robot_player)();
    void (*run)(Obj* rc);
};
extern const ModuleInfo g_modules[];

extern Obj* g_exc;   // the pending exception
extern void* g_S;    // the running robot's statics
extern Obj* g_str[];
extern Klass K_prim_Z, K_prim_B, K_prim_C, K_prim_S, K_prim_I, K_prim_J, K_prim_F, K_prim_D, K_prim_V;

[[noreturn]] void rt_fatal(const char* fmt, ...);

// allocation
Obj* rt_new(Klass* k);
Obj* rt_newarray(Klass* ak, jint n);
Obj* rt_multianewarray(Klass* ak, int dims, ...);
Obj* rt_array_clone(Obj* a);
Klass* rt_array_of(Klass* comp);
Obj* rt_mirror_slow(Klass* k);
inline Obj* rt_mirror(Klass* k) { return k->mirror ? k->mirror : rt_mirror_slow(k); }
Klass* rt_mirror_klass(Obj* m);

// strings (generated glue knows java.lang.String's layout)
Obj* rt_make_string(const jchar* s, jint n);
Obj* rt_cstring(const char* s);
jint rt_string_length(Obj* s);
jchar rt_string_char(Obj* s, jint i);
void rt_string_utf8(Obj* s, char* buf, size_t cap);

// types
bool rt_subtype(Klass* s, Klass* t);
inline bool rt_instanceof(Obj* o, Klass* t) {
    Klass* k = o->klass;
    if (k == t) return true;
    if (!(t->flags & (KF_INTERFACE | KF_ARRAY)) && !(k->flags & KF_ARRAY))
        return k->depth >= t->depth && k->display[t->depth] == t;
    return rt_subtype(k, t);
}
bool rt_store_ok(Obj* arr, Obj* v);
inline bool rt_is_bool_array(Obj* a) { return a->klass->elem == 'Z'; }
void** rt_itable(Klass* k, Klass* iface);

// exceptions the JVM throws (generated glue builds them with their real constructors)
void rt_throw_new_npe(Obj* msg);
void rt_throw_new_arith(Obj* msg);
void rt_throw_new_aioobe(Obj* msg);
void rt_throw_new_cce(Obj* msg);
void rt_throw_new_negsize(Obj* msg);
void rt_throw_new_ase(Obj* msg);
void rt_throw_new_ncdfe(Obj* msg);
void rt_throw_new_ame(Obj* msg);
void rt_throw_new_internal(Obj* msg);
void rt_throw_new_nsme(Obj* msg);
void rt_throw_new_nsfe(Obj* msg);
void rt_throw_new_iae(Obj* msg);
void rt_throw_new_icce(Obj* msg);
void rt_throw_new_nsfx(Obj* msg);
void rt_throw_instr(int type, Obj* msg);
void rt_init_failed();
inline void rt_throw_npe() { rt_throw_new_npe(nullptr); }
void rt_throw_arith();
void rt_throw_aioobe(jint i, jint len);
void rt_throw_cce(Obj* o, Klass* k);
void rt_throw_negsize(jint n);
void rt_throw_ase(Obj* v);
void rt_throw_ncdfe(Klass* k);
void rt_abstract_method();
[[noreturn]] void rt_no_receiver_fail(int s);
template <class T>
T rt_no_receiver(int s) { rt_no_receiver_fail(s); }

// Java arithmetic
inline jint rt_idiv(jint a, jint b) { return (a == INT32_MIN && b == -1) ? a : a / b; }
inline jint rt_irem(jint a, jint b) { return b == -1 ? 0 : a % b; }
inline jlong rt_ldiv(jlong a, jlong b) { return (a == INT64_MIN && b == -1) ? a : a / b; }
inline jlong rt_lrem(jlong a, jlong b) { return b == -1 ? 0 : a % b; }
inline jfloat rt_frem(jfloat a, jfloat b) { return fmodf(a, b); }
inline jdouble rt_drem(jdouble a, jdouble b) { return fmod(a, b); }
inline jint rt_f2i(jfloat f) {
    if (f != f) return 0;
    if (f >= 2147483648.0f) return INT32_MAX;
    if (f <= -2147483648.0f) return INT32_MIN;
    return (jint)f;
}
inline jlong rt_f2l(jfloat f) {
    if (f != f) return 0;
    if (f >= 9223372036854775808.0f) return INT64_MAX;
    if (f <= -9223372036854775808.0f) return INT64_MIN;
    return (jlong)f;
}
inline jint rt_d2i(jdouble d) {
    if (d != d) return 0;
    if (d >= 2147483648.0) return INT32_MAX;
    if (d <= -2147483648.0) return INT32_MIN;
    return (jint)d;
}
inline jlong rt_d2l(jdouble d) {
    if (d != d) return 0;
    if (d >= 9223372036854775808.0) return INT64_MAX;
    if (d <= -9223372036854775808.0) return INT64_MIN;
    return (jlong)d;
}
inline jint rt_lcmp(jlong a, jlong b) { return (a > b) - (a < b); }
inline jint rt_fcmpl(jfloat a, jfloat b) { return a > b ? 1 : a == b ? 0 : -1; }
inline jint rt_fcmpg(jfloat a, jfloat b) { return a < b ? -1 : a == b ? 0 : 1; }
inline jint rt_dcmpl(jdouble a, jdouble b) { return a > b ? 1 : a == b ? 0 : -1; }
inline jint rt_dcmpg(jdouble a, jdouble b) { return a < b ? -1 : a == b ? 0 : 1; }
inline jfloat rt_fbits(juint b) { return std::bit_cast<jfloat>(b); }
inline jdouble rt_dbits(julong b) { return std::bit_cast<jdouble>(b); }

// ---- the bytecode monitor of the running robot (RobotMonitor's statics)
struct Monitor {
    jint left;        // bytecodesLeft
    jint limit;       // bytecodeLimit
    jint to_remove;   // bytecodesToRemove
    jint debug_level;
    jint seed;        // randomSeed
    bool should_die;
};
extern Monitor g_mon;
extern bool g_mon_slow;  // should_die || debug_level != 0 || to_remove != 0
void rt_bc_slow(jint n);
inline void rt_bc_inc(jint n) {
    jint l = g_mon.left - n;
    if (UNLIKELY((l <= 0) | g_mon_slow)) rt_bc_slow(n);
    else g_mon.left = l;
}
void rt_bc_defer(jint n);          // incrementBytecodesWithoutInterrupt
inline jint rt_bc_sanitize(jint i) { return i > 1 ? i : 1; }
jint rt_bc_multi_cost(Obj* dims);  // calculateMultiArrayCost
void rt_pause();                   // RobotMonitor.pause (Clock.yield)
void rt_debug_inc();
void rt_debug_dec();
inline jint rt_bc_num() { return g_mon.limit - g_mon.left; }
inline jint rt_bc_left() { return g_mon.left; }
inline jlong rt_bc_seed() { return (jlong)g_mon.seed; }
void rt_monitor_noop();
