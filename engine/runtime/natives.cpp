// C++ implementations of the methods engine/jvm/natives.txt names. Compiled with the generated types.h, so it
// can read java.lang.String and java.lang.Class (the substitute) by their translated layouts.
#include <sys/stat.h>
#include <time.h>
#include <unistd.h>
#include <zlib.h>

#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>
#include <unordered_map>

#include "gc.h"
#include "sched.h"
#include "types.h"

extern const int g_slot_hashCode, g_slot_toString;
extern void* const g_identity_hash_fns[3];
extern void* const g_object_toString_fn;
long rt_field_offset(Klass* k, const char* name);
Klass* rt_mirror_klass(Obj* m);

// ---- small helpers named in natives.txt
jboolean rt_false() { return 0; }
jboolean rt_true() { return 1; }
void rt_noop() {}
void rt_noop1(Obj*) {}
void rt_noop_self(Obj*) {}
jlong rt_zero_long() { return 0; }
Obj* rt_null_string(Obj*) { return nullptr; }
void rt_monitor_str(Obj*) {}
jlong rt_time_left() { return 1200000000000LL; }
jlong rt_time_elapsed() { return 0; }

static std::string utf8(Obj* s) {
    if (!s) return "null";
    std::string out;
    jint n = rt_string_length(s);
    for (jint i = 0; i < n; i++) {
        jchar c = rt_string_char(s, i);
        if (c < 0x80) out += (char)c;
        else if (c < 0x800) { out += (char)(0xC0 | (c >> 6)); out += (char)(0x80 | (c & 63)); }
        else { out += (char)(0xE0 | (c >> 12)); out += (char)(0x80 | ((c >> 6) & 63)); out += (char)(0x80 | (c & 63)); }
    }
    return out;
}

void rt_test_print(Obj* s) {
    std::string u = utf8(s);
    printf("%s\n", u.c_str());
}

// ---- identity hash codes of system code (Object.hashCode, System.identityHashCode): stable, sequential
static IdHashTable g_ids;
static jint sys_code(IdHashTable*) {
    static juint x = 0x9e3779b9;
    x ^= x << 13; x ^= x >> 17; x ^= x << 5;
    return (jint)(x & 0x7fffffff);
}
jint rt_Object_hashCode(Obj* self) {
    if (!self) return 0;
    return idhash_get(&g_ids, self, sys_code);
}

// ---- java.lang.Object
Obj* rt_Object_getClass(Obj* self) { return rt_mirror(self->klass); }
Obj* rt_Object_clone(Obj* self) {
    if (self->klass->flags & KF_ARRAY) return rt_array_clone(self);
    bool cloneable = false;
    for (int i = 0; i < self->klass->n_ifaces; i++)
        if (!strcmp(self->klass->ifaces[i]->name, "java/lang/Cloneable")) cloneable = true;
    if (!cloneable) { rt_throw_new_internal(rt_cstring(self->klass->name)); return nullptr; }
    Obj* o = rt_new(self->klass);
    memcpy((char*)o + sizeof(Obj), (char*)self + sizeof(Obj), self->klass->size - sizeof(Obj));
    return o;
}
void rt_Object_notify(Obj*) {}
void rt_Object_wait0(Obj*, jlong) { rt_fatal("Object.wait called"); }

// ---- java.lang.System
static size_t esize(char e) {
    switch (e) { case 'Z': case 'B': return 1; case 'C': case 'S': return 2; case 'I': case 'F': return 4; case 'J': case 'D': return 8; default: return sizeof(Obj*); }
}

static void aioobe(const char* fmt, jint a, Obj* arr) {
    char b[160];
    snprintf(b, sizeof b, fmt, a, ALEN(arr));
    rt_throw_new_aioobe(rt_cstring(b));
}

void rt_System_arraycopy(Obj* src, jint sp, Obj* dst, jint dp, jint n) {
    // the common case: arrays of one class, in bounds (no store checks: an element of one is one of the other)
    if (LIKELY(src && dst && src->klass == dst->klass && (src->klass->flags & KF_ARRAY) && (sp | dp | n) >= 0 &&
               (jlong)sp + n <= ALEN(src) && (jlong)dp + n <= ALEN(dst))) {
        size_t es = esize(src->klass->elem);
        memmove(ADATA(char, dst) + (size_t)dp * es, ADATA(char, src) + (size_t)sp * es, (size_t)n * es);
        return;
    }
    if (!src || !dst) { rt_throw_npe(); return; }
    Klass *sk = src->klass, *dk = dst->klass;
    if (!(sk->flags & KF_ARRAY) || !(dk->flags & KF_ARRAY)) { rt_throw_new_ase(rt_cstring("arraycopy: source or destination is not an array")); return; }
    bool sref = sk->elem == 'L', dref = dk->elem == 'L';
    if (sref != dref || (!sref && sk->elem != dk->elem)) { rt_throw_new_ase(rt_cstring("arraycopy: type mismatch")); return; }
    if (n < 0) { char b[96]; snprintf(b, sizeof b, "arraycopy: length %d is negative", n); rt_throw_new_aioobe(rt_cstring(b)); return; }
    if (sp < 0) { aioobe("arraycopy: source index %d out of bounds for length %d", sp, src); return; }
    if (dp < 0) { aioobe("arraycopy: destination index %d out of bounds for length %d", dp, dst); return; }
    if ((jlong)sp + n > ALEN(src)) { aioobe("arraycopy: last source index %d out of bounds for length %d", sp + n, src); return; }
    if ((jlong)dp + n > ALEN(dst)) { aioobe("arraycopy: last destination index %d out of bounds for length %d", dp + n, dst); return; }
    size_t es = esize(sk->elem);
    if (sref && !rt_subtype(sk->component, dk->component)) {
        for (jint i = 0; i < n; i++) {
            Obj* v = ADATA(Obj*, src)[sp + i];
            if (v && !rt_instanceof(v, dk->component)) { rt_throw_ase(v); return; }
            ADATA(Obj*, dst)[dp + i] = v;
        }
        return;
    }
    memmove(ADATA(char, dst) + (size_t)dp * es, ADATA(char, src) + (size_t)sp * es, (size_t)n * es);
}

jlong rt_System_currentTimeMillis() { timespec t; clock_gettime(CLOCK_REALTIME, &t); return (jlong)t.tv_sec * 1000 + t.tv_nsec / 1000000; }
jlong rt_System_nanoTime() { timespec t; clock_gettime(CLOCK_MONOTONIC, &t); return (jlong)t.tv_sec * 1000000000LL + t.tv_nsec; }
void rt_System_exit(jint status) { rt_fatal("System.exit(%d)", status); }

bool g_print_stdout = false;
void rt_PrintStream_write0(jint fd, Obj* s) {
    if (fd == 1 && !g_print_stdout) return;
    std::string u = utf8(s);
    fwrite(u.data(), 1, u.size(), fd == 1 ? stdout : stderr);
}

// ---- java.lang.Class (the substitute bcx.subst.JClass)
#define KL(m) rt_mirror_klass(m)
static std::string java_name(Klass* k) {
    std::string s = k->name;
    if (k->flags & KF_PRIMITIVE) return s;
    for (char& c : s) if (c == '/') c = '.';
    return s;
}
Obj* rt_Class_getName(Obj* self) { return rt_cstring(java_name(KL(self)).c_str()); }
jboolean rt_Class_isInstance(Obj* self, Obj* o) { return o && !(KL(self)->flags & KF_PRIMITIVE) && rt_instanceof(o, KL(self)); }
jboolean rt_Class_isAssignableFrom(Obj* self, Obj* other) {
    if (!other) { rt_throw_npe(); return 0; }
    Klass *t = KL(self), *s = KL(other);
    if ((t->flags | s->flags) & KF_PRIMITIVE) return t == s;
    return rt_subtype(s, t);
}
jboolean rt_Class_isInterface(Obj* self) { return (KL(self)->flags & KF_INTERFACE) != 0; }
jboolean rt_Class_isArray(Obj* self) { return (KL(self)->flags & KF_ARRAY) != 0; }
jboolean rt_Class_isPrimitive(Obj* self) { return (KL(self)->flags & KF_PRIMITIVE) != 0; }
Obj* rt_Class_getSuperclass(Obj* self) {
    Klass* k = KL(self);
    if (k->flags & (KF_INTERFACE | KF_PRIMITIVE)) return nullptr;
    return k->super ? rt_mirror(k->super) : nullptr;
}
Obj* rt_Class_getComponentType(Obj* self) {
    Klass* k = KL(self);
    return (k->flags & KF_ARRAY) ? rt_mirror(k->component) : nullptr;
}
jint rt_Class_getModifiers(Obj* self) { return (KL(self)->flags >> 8) & 0xffff; }
jboolean rt_Class_desiredAssertionStatus(Obj* self) { return KL(self)->module != 0; }
jboolean rt_Class_isEnum0(Obj* self) {
    Klass* k = KL(self);
    return (k->flags & KF_ENUM) && k->super && !strcmp(k->super->name, "java/lang/Enum");
}
static KCache* g_enum_cache_sys;
Obj* rt_Class_getEnumConstantsShared(Obj* self) {
    Klass* k = KL(self);
    if (!rt_Class_isEnum0(self) || !k->enum_values) return nullptr;
    Obj** slot = k->module ? sched_enum_cache(k) : kcache_slot(&g_enum_cache_sys, k, true);
    if (*slot) return *slot;
    if (k->init) {  // values() is static: initialize the class first, as Method.invoke does
        k->init();
        if (g_exc) { g_exc = nullptr; return nullptr; }
    }
    Obj* v = ((Obj * (*)()) k->enum_values)();
    if (g_exc) { g_exc = nullptr; return nullptr; }
    *slot = v;
    return v;
}
Obj* rt_Class_getPrimitiveClass(Obj* name) {
    std::string n = utf8(name);
    Klass* ks[] = {&K_prim_Z, &K_prim_B, &K_prim_C, &K_prim_S, &K_prim_I, &K_prim_J, &K_prim_F, &K_prim_D, &K_prim_V};
    for (Klass* k : ks) if (n == k->name) return rt_mirror(k);
    return nullptr;
}
Obj* rt_Class_arrayType(Obj* self) { return rt_mirror(rt_array_of(KL(self))); }
Obj* rt_Class_getGenericSignature0(Obj* self) { return KL(self)->gsig ? rt_cstring(KL(self)->gsig) : nullptr; }
Obj* rt_Class_lookup0(Obj* self, Obj* name) {
    std::string n = utf8(name);
    int mod = KL(self)->module;
    for (int pass = 0; pass < 2; pass++)
        for (int i = 0; i < g_n_klasses; i++) {
            Klass* k = g_all_klasses[i];
            if (k->module == (pass ? 0 : mod) && n == k->name) return rt_mirror(k);
        }
    return nullptr;
}
Obj* rt_Class_getInterfaces0(Obj* self) {
    extern Klass K_c0_java_lang_Class;
    return rt_newarray(rt_array_of(&K_c0_java_lang_Class), 0);
}
Obj* rt_Array_newArray(Obj* comp, jint n) {
    if (!comp) { rt_throw_npe(); return nullptr; }
    if (n < 0) { rt_throw_negsize(n); return nullptr; }
    Klass* k = KL(comp);
    if (k == &K_prim_V) { rt_throw_new_internal(rt_cstring("void array")); return nullptr; }
    return rt_newarray(rt_array_of(k), n);
}

// ---- numbers
jint rt_Float_floatToRawIntBits(jfloat f) { return std::bit_cast<jint>(f); }
jfloat rt_Float_intBitsToFloat(jint i) { return std::bit_cast<jfloat>(i); }
jlong rt_Double_doubleToRawLongBits(jdouble d) { return std::bit_cast<jlong>(d); }
jdouble rt_Double_longBitsToDouble(jlong l) { return std::bit_cast<jdouble>(l); }
jdouble rt_sqrt(jdouble d) { return __builtin_sqrt(d); }

// ---- strings
static std::unordered_map<std::u16string, Obj*>* g_interned;
Obj* rt_String_intern(Obj* self) {
    if (!g_interned) g_interned = new std::unordered_map<std::u16string, Obj*>();
    std::u16string k;
    jint n = rt_string_length(self);
    for (jint i = 0; i < n; i++) k += (char16_t)rt_string_char(self, i);
    auto it = g_interned->find(k);
    if (it != g_interned->end()) return it->second;
    Obj** keep = (Obj**)GC_MALLOC_UNCOLLECTABLE(sizeof(Obj*));
    *keep = self;
    (*g_interned)[k] = self;
    return self;
}

Obj* rt_Throwable_fillInStackTrace(Obj* self, jint) { return self; }

// ---- jdk.internal.misc.Unsafe
jint rt_Unsafe_arrayBaseOffset0(Obj*, Obj*) { return (jint)sizeof(ArrHdr); }
jint rt_Unsafe_arrayIndexScale0(Obj*, Obj* c) {
    Klass* k = KL(c);
    return (jint)esize(k->elem);
}
jint rt_Unsafe_addressSize0(Obj*) { return 8; }
jint rt_Unsafe_pageSize(Obj*) { return 4096; }
jlong rt_Unsafe_objectFieldOffset1(Obj*, Obj* c, Obj* name) {
    std::string n = utf8(name);
    for (Klass* k = KL(c); k; k = k->super) {
        long off = rt_field_offset(k, n.c_str());
        if (off >= 0) return off;
    }
    rt_throw_new_internal(rt_cstring(n.c_str()));
    return 0;
}
#define AT(T, o, off) (*(T*)((char*)(o) + (off)))
jint rt_Unsafe_getInt(Obj*, Obj* o, jlong off) { return AT(jint, o, off); }
void rt_Unsafe_putInt(Obj*, Obj* o, jlong off, jint v) { AT(jint, o, off) = v; }
jlong rt_Unsafe_getLong(Obj*, Obj* o, jlong off) { return AT(jlong, o, off); }
void rt_Unsafe_putLong(Obj*, Obj* o, jlong off, jlong v) { AT(jlong, o, off) = v; }
Obj* rt_Unsafe_getReference(Obj*, Obj* o, jlong off) { return AT(Obj*, o, off); }
void rt_Unsafe_putReference(Obj*, Obj* o, jlong off, Obj* v) { AT(Obj*, o, off) = v; }
jboolean rt_Unsafe_getBoolean(Obj*, Obj* o, jlong off) { return AT(jboolean, o, off); }
void rt_Unsafe_putBoolean(Obj*, Obj* o, jlong off, jboolean v) { AT(jboolean, o, off) = v; }
jbyte rt_Unsafe_getByte(Obj*, Obj* o, jlong off) { return AT(jbyte, o, off); }
void rt_Unsafe_putByte(Obj*, Obj* o, jlong off, jbyte v) { AT(jbyte, o, off) = v; }
jchar rt_Unsafe_getChar(Obj*, Obj* o, jlong off) { return AT(jchar, o, off); }
void rt_Unsafe_putChar(Obj*, Obj* o, jlong off, jchar v) { AT(jchar, o, off) = v; }
jshort rt_Unsafe_getShort(Obj*, Obj* o, jlong off) { return AT(jshort, o, off); }
// FastPath.unaligned{Short,Int,Long}(o, off, bigEndian), for Unsafe_p: with UnsafeConstants.BIG_ENDIAN false,
// Unsafe's Java reads the value little-endian (aligned or byte by byte) and reverses its bytes for bigEndian
jshort rt_fastpath_unaligned_short(Obj* o, jlong off, jboolean big) {
    uint16_t v;
    memcpy(&v, (char*)o + off, sizeof v);
    return (jshort)(big ? __builtin_bswap16(v) : v);
}
jint rt_fastpath_unaligned_int(Obj* o, jlong off, jboolean big) {
    uint32_t v;
    memcpy(&v, (char*)o + off, sizeof v);
    return (jint)(big ? __builtin_bswap32(v) : v);
}
jlong rt_fastpath_unaligned_long(Obj* o, jlong off, jboolean big) {
    uint64_t v;
    memcpy(&v, (char*)o + off, sizeof v);
    return (jlong)(big ? __builtin_bswap64(v) : v);
}
void rt_Unsafe_putShort(Obj*, Obj* o, jlong off, jshort v) { AT(jshort, o, off) = v; }
jboolean rt_Unsafe_casInt(Obj*, Obj* o, jlong off, jint e, jint v) { if (AT(jint, o, off) != e) return 0; AT(jint, o, off) = v; return 1; }
jboolean rt_Unsafe_casLong(Obj*, Obj* o, jlong off, jlong e, jlong v) { if (AT(jlong, o, off) != e) return 0; AT(jlong, o, off) = v; return 1; }
jboolean rt_Unsafe_casReference(Obj*, Obj* o, jlong off, Obj* e, Obj* v) { if (AT(Obj*, o, off) != e) return 0; AT(Obj*, o, off) = v; return 1; }
jint rt_Unsafe_caxInt(Obj*, Obj* o, jlong off, jint e, jint v) { jint w = AT(jint, o, off); if (w == e) AT(jint, o, off) = v; return w; }
jlong rt_Unsafe_caxLong(Obj*, Obj* o, jlong off, jlong e, jlong v) { jlong w = AT(jlong, o, off); if (w == e) AT(jlong, o, off) = v; return w; }
Obj* rt_Unsafe_caxReference(Obj*, Obj* o, jlong off, Obj* e, Obj* v) { Obj* w = AT(Obj*, o, off); if (w == e) AT(Obj*, o, off) = v; return w; }

// ---- battlecode.instrumenter.inject.ObjectMethods (per robot)
static bool identity_hashed(Obj* o) {
    void* f = o->klass->vtable[g_slot_hashCode];
    return f == g_identity_hash_fns[0] || (f && f == g_identity_hash_fns[1]) || (f && f == g_identity_hash_fns[2]);
}
jint rt_OM_identityHashCode(Obj* o) { return sched_identity_hash(o); }
jint rt_OM_hashCode(Obj* o) {
    if (!o) { rt_throw_npe(); return 0; }
    if (identity_hashed(o)) return sched_identity_hash(o);
    return ((jint(*)(Obj*))o->klass->vtable[g_slot_hashCode])(o);
}
Obj* rt_OM_identityToString(Obj* o) {
    char b[32];
    snprintf(b, sizeof b, "object%d", sched_identity_hash(o));
    return rt_cstring(b);
}
Obj* rt_OM_toString(Obj* o) {
    if (!o) { rt_throw_npe(); return nullptr; }
    if (o->klass->vtable[g_slot_toString] == g_object_toString_fn) return rt_OM_identityToString(o);
    return ((Obj * (*)(Obj*)) o->klass->vtable[g_slot_toString])(o);
}

// ---- the native engine's files
std::string g_bundled_maps;
static FILE* g_hashes;
static FILE* g_dump;
void rt_io_set_outputs(FILE* hashes, FILE* dump) { g_hashes = hashes; g_dump = dump; }
void rt_io_round(jint round, jlong hash, Obj* dump) {
    if (g_hashes) fprintf(g_hashes, "%d %016llx\n", round, (unsigned long long)hash);
    if (g_dump && dump) {
        std::string u = utf8(dump);
        fprintf(g_dump, "== round %d\n%s", round, u.c_str());
        if (!u.empty() && u.back() != '\n') fputc('\n', g_dump);
    }
}
// ---- fast paths' differential check (bcx.subst.FastPath)
jboolean rt_fastpath_check() {
    const char* v = getenv("BCX_CHECK_FASTPATH");
    return v && *v && strcmp(v, "0") != 0;
}
void rt_fastpath_fail(Obj* what) {
    fprintf(stderr, "bcx: fast path differs from the original: %s\n", utf8(what).c_str());
    fflush(stderr);
    abort();
}
void rt_fastpath_bfs(Obj* nbrA, Obj* tableA, Obj* backA, Obj* queueA, jint t, Obj* center) {
    const jint* nbr = ADATA(jint, nbrA);
    Obj** rows = ADATA(Obj*, tableA);
    Obj** back = ADATA(Obj*, backA);
    jint* q = ADATA(jint, queueA);
    ADATA(Obj*, rows[t])[t] = center;
    q[0] = t;
    jint head = 0, tail = 1;
    while (head < tail) {
        const jint* nb = nbr + 8 * q[head++];
        for (int k = 0; k < 8; k++) {
            jint ni = nb[k];
            if (ni < 0) continue;
            Obj** row = ADATA(Obj*, rows[ni]);
            if (row[t]) continue;
            row[t] = back[k];
            q[tail++] = ni;
        }
    }
}
jlong rt_fastpath_random_state(Obj* r) {
    c0_java_util_Random* rr = (c0_java_util_Random*)r;
    rr->f_haveNextNextGaussian = 0;
    return ((c0_java_util_concurrent_atomic_AtomicLong*)rr->f_seed)->f_value;
}
jboolean rt_io_exists(Obj* path) {
    struct stat st;
    return stat(utf8(path).c_str(), &st) == 0;
}
Obj* rt_io_readFile(Obj* path) {
    std::string p = utf8(path);
    FILE* f = fopen(p.c_str(), "rb");
    if (!f) { rt_throw_new_internal(rt_cstring(("cannot read " + p).c_str())); return nullptr; }
    fseek(f, 0, SEEK_END);
    long n = ftell(f);
    fseek(f, 0, SEEK_SET);
    extern Klass KA1_prim_B;
    Obj* a = rt_newarray(&KA1_prim_B, (jint)n);
    if (fread(ADATA(char, a), 1, n, f) != (size_t)n) rt_fatal("short read of %s", p.c_str());
    fclose(f);
    return a;
}
void rt_io_writeFile(Obj* path, Obj* data) {
    std::string p = utf8(path);
    FILE* f = fopen(p.c_str(), "wb");
    if (!f) rt_fatal("cannot write %s", p.c_str());
    fwrite(ADATA(char, data), 1, ALEN(data), f);
    fclose(f);
}
Obj* rt_io_bundledMaps() { return rt_cstring(g_bundled_maps.c_str()); }

// java.util.zip.GZIPOutputStream's output for the whole buffer: header, raw deflate (level 6), CRC32, size
Obj* rt_gzip(Obj* data) {
    jint n = ALEN(data);
    z_stream zs;
    memset(&zs, 0, sizeof zs);
    if (deflateInit2(&zs, Z_DEFAULT_COMPRESSION, Z_DEFLATED, -MAX_WBITS, 8, Z_DEFAULT_STRATEGY) != Z_OK) rt_fatal("deflateInit2");
    uLong bound = deflateBound(&zs, n);
    std::string out(10 + bound + 8, '\0');
    const unsigned char hdr[10] = {0x1f, 0x8b, 8, 0, 0, 0, 0, 0, 0, 0xff};
    memcpy(&out[0], hdr, 10);
    zs.next_in = (Bytef*)ADATA(char, data);
    zs.avail_in = n;
    zs.next_out = (Bytef*)&out[10];
    zs.avail_out = bound;
    if (deflate(&zs, Z_FINISH) != Z_STREAM_END) rt_fatal("deflate");
    size_t len = 10 + zs.total_out;
    deflateEnd(&zs);
    uLong crc = crc32(0, (const Bytef*)ADATA(char, data), n);
    unsigned char tr[8] = {(unsigned char)crc, (unsigned char)(crc >> 8), (unsigned char)(crc >> 16), (unsigned char)(crc >> 24),
                           (unsigned char)n, (unsigned char)(n >> 8), (unsigned char)(n >> 16), (unsigned char)(n >> 24)};
    memcpy(&out[len], tr, 8);
    len += 8;
    extern Klass KA1_prim_B;
    Obj* a = rt_newarray(&KA1_prim_B, (jint)len);
    memcpy(ADATA(char, a), out.data(), len);
    return a;
}

// ---- java.lang.Math's HotSpot intrinsics (hsmath.c, bit-exact ports of the x86-64 stubs)
#include "hsmath.h"
jdouble rt_hs_pow(jdouble x, jdouble y) { return bcx_hs_pow(x, y); }
jdouble rt_hs_exp(jdouble x) { return bcx_hs_exp(x); }
jdouble rt_hs_log(jdouble x) { return bcx_hs_log(x); }
jdouble rt_hs_log10(jdouble x) { return bcx_hs_log10(x); }
jdouble rt_hs_sin(jdouble x) { return bcx_hs_sin(x); }
jdouble rt_hs_cos(jdouble x) { return bcx_hs_cos(x); }
jdouble rt_hs_tan(jdouble x) { return bcx_hs_tan(x); }

// ---- java.lang.ref.Reference: referents are ordinary (strong) fields for this GC
#ifdef HAS_c0_java_lang_ref_Reference
Obj* rt_Reference_get0(Obj* self) { return ((c0_java_lang_ref_Reference*)self)->f_referent; }
jboolean rt_Reference_refersTo0(Obj* self, Obj* o) { return ((c0_java_lang_ref_Reference*)self)->f_referent == o; }
void rt_Reference_clear0(Obj* self) { ((c0_java_lang_ref_Reference*)self)->f_referent = nullptr; }
#endif
void rt_Unsafe_copyMemory0(Obj*, Obj* src, jlong so, Obj* dst, jlong d, jlong n) { memmove((char*)dst + d, (char*)src + so, (size_t)n); }

// ---- the reflection java.util.Random's static initializer uses (getDeclaredField, objectFieldOffset)
#ifdef HAS_c0_java_lang_reflect_Field
Obj* rt_Class_getDeclaredField(Obj* self, Obj* name) {
    if (!name) { rt_throw_npe(); return nullptr; }
    std::string n = utf8(name);
    if (rt_field_offset(KL(self), n.c_str()) < 0) { rt_throw_new_nsfx(name); return nullptr; }
    extern Klass K_c0_java_lang_reflect_Field;
    c0_java_lang_reflect_Field* f = (c0_java_lang_reflect_Field*)rt_new(&K_c0_java_lang_reflect_Field);
    f->f_clazz = self;
    f->f_name = name;
    return f;
}
jlong rt_Unsafe_objectFieldOffset0(Obj*, Obj* field) {
    c0_java_lang_reflect_Field* f = (c0_java_lang_reflect_Field*)field;
    std::string n = utf8(f->f_name);
    return rt_field_offset(KL(f->f_clazz), n.c_str());
}
#endif
void rt_Unsafe_copySwapMemory0(Obj*, Obj* src, jlong so, Obj* dst, jlong d, jlong n, jlong es) {
    char* s = (char*)src + so;
    char* t = (char*)dst + d;
    std::string tmp(s, (size_t)n);
    for (jlong i = 0; i < n; i += es)
        for (jlong k = 0; k < es; k++) t[i + k] = tmp[i + es - 1 - k];
}
// Math.fma: exactly rounded, as the spec, HotSpot's intrinsic and the BigDecimal fallback all are
jdouble rt_fma(jdouble a, jdouble b, jdouble c) { return __builtin_fma(a, b, c); }
jfloat rt_fmaf(jfloat a, jfloat b, jfloat c) { return __builtin_fmaf(a, b, c); }

// "Class: message" of a throwable, for diagnostics (BCX_ROBOT_ERRORS)
void rt_describe_exception(Obj* e, char* buf, size_t cap) {
    c0_java_lang_Throwable* t = (c0_java_lang_Throwable*)e;
    std::string m = java_name(e->klass);
    if (t->f_detailMessage) m += ": " + utf8(t->f_detailMessage);
    snprintf(buf, cap, "%s", m.c_str());
}

Obj* rt_Class_forName(Obj* name, jboolean init, Obj*) {
    if (!name) { rt_throw_npe(); return nullptr; }
    std::string n = utf8(name);
    for (char& c : n) if (c == '.') c = '/';
    for (int i = 0; i < g_n_klasses; i++) {
        Klass* k = g_all_klasses[i];
        if (k->module == 0 && n == k->name) {
            if (init && k->init) { k->init(); if (g_exc) return nullptr; }
            return rt_mirror(k);
        }
    }
    rt_throw_new_internal(rt_cstring(("bcx: Class.forName of a class not in this build: " + n).c_str()));
    return nullptr;
}
