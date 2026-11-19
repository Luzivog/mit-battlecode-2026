/* HotSpot x86-64 Math.pow/exp/log/log10/sin/cos/tan, bit for bit.
 *
 * HotSpot runs these as stubs (Intel LIBM, UseLibmIntrinsic) in the interpreter, C1 and C2 alike. Source:
 * openjdk/jdk21u tag jdk-21.0.13.0.3+1, src/hotspot/cpu/x86/stubGenerator_x86_64_{pow,exp,log,sin,cos,tan,constants}.cpp.
 * Each stub instruction is one C statement below, in the stub's order, on emulated registers, flags and stack.
 * Generated from those sources by ~/battlecode26-data/engine/tools/hsmath_gen/gen_hsmath.py; test: test/hsmath_test.py.
 * Integer, logic and shuffle instructions are SSE intrinsics. Floating-point arithmetic and conversions are inline asm:
 * the C compiler may constant-fold or commute an intrinsic, and then a NaN's sign or payload can differ.
 *
 * Needs x86-64 with SSE4.1 and the default MXCSR (round to nearest, no FTZ/DAZ: never link with -ffast-math).
 * rcpps (in pow and log) is approximate and model-specific: the results match the JVM on the same CPU.
 * Build: clang -O2 -ffp-contract=off -fno-fast-math -msse4.1. HSMATH_CHECK builds a self-checking variant.
 */
#include <stdint.h>
#include <string.h>
#include <smmintrin.h>
#include "hsmath.h"

#define A(p) ((uint64_t)(uintptr_t)(p))

#ifdef HSMATH_CHECK
#include <stdio.h>
#include <stdlib.h>
struct hs_tab { const void *p; size_t n; };
#define TAB(t) { t, sizeof t }
static uint64_t hs_seed, hs_stk_lo, hs_stk_hi;
static uint64_t hs_g(uint64_t i) {
  uint64_t z = hs_seed + 0x9e3779b97f4a7c15ull * (i + 1);
  if (hs_seed == 0) return 0;
  z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9ull;
  z = (z ^ (z >> 27)) * 0x94d049bb133111ebull;
  return z ^ (z >> 31);
}
void bcx_hs_check_seed(uint64_t s) { hs_seed = s; }
static void hs_check(uint64_t a, size_t n);
#define CHECK(a, n) hs_check((a), (n))
#define GINIT(i) hs_g(i)
#define XINIT(i) _mm_set_epi64x((long long)hs_g(100 + (i)), (long long)hs_g(200 + (i)))
#define SCRATCH(r) ((r) = hs_g(300))
#define STACK_INIT \
  uint8_t stk_[256] __attribute__((aligned(16))); \
  for (int i_ = 0; i_ < 256; i_ += 8) { uint64_t g_ = hs_g(400 + i_); memcpy(stk_ + i_, &g_, 8); } \
  hs_stk_lo = A(stk_); hs_stk_hi = A(stk_) + 256; rsp = A(stk_) + 256 - 8
#else
#define CHECK(a, n) ((void)0)
#define GINIT(i) 0
#define XINIT(i) _mm_setzero_si128()
#define SCRATCH(r) ((void)0)
#define STACK_INIT \
  uint8_t stk_[256] __attribute__((aligned(16))); \
  rsp = A(stk_) + 256 - 8
#endif

#define REGS_INIT \
  uint64_t rax = GINIT(0), rbx = GINIT(1), rcx = GINIT(2), rdx = GINIT(3), rsi = GINIT(4), rdi = GINIT(5); \
  uint64_t rbp = GINIT(6), rsp, r8 = GINIT(8), r9 = GINIT(9), r10 = GINIT(10), r11 = GINIT(11); \
  int zf = 0, sf = 0, cf = 0, of = 0; \
  (void)rax; (void)rbx; (void)rcx; (void)rdx; (void)rsi; (void)rdi; (void)r8; (void)r9; (void)r10; (void)r11; \
  (void)zf; (void)sf; (void)cf; (void)of

/* memory */
static inline uint64_t LD16(uint64_t a) { uint16_t v; CHECK(a, 2); memcpy(&v, (const void *)(uintptr_t)a, 2); return v; }
static inline uint64_t LD32(uint64_t a) { uint32_t v; CHECK(a, 4); memcpy(&v, (const void *)(uintptr_t)a, 4); return v; }
static inline uint64_t LD64(uint64_t a) { uint64_t v; CHECK(a, 8); memcpy(&v, (const void *)(uintptr_t)a, 8); return v; }
static inline void ST32(uint64_t a, uint32_t v) { CHECK(a, 4); memcpy((void *)(uintptr_t)a, &v, 4); }
static inline void ST64(uint64_t a, uint64_t v) { CHECK(a, 8); memcpy((void *)(uintptr_t)a, &v, 8); }
static inline __m128i LDQ(uint64_t a) { CHECK(a, 8); return _mm_loadl_epi64((const __m128i *)(uintptr_t)a); }
static inline __m128i LD128(uint64_t a) { CHECK(a, 16); return _mm_loadu_si128((const __m128i *)(uintptr_t)a); }
static inline void STQ(uint64_t a, __m128i x) { CHECK(a, 8); _mm_storel_epi64((__m128i *)(uintptr_t)a, x); }
#define PUSH(r) do { rsp -= 8; ST64(rsp, (r)); } while (0)
#define POP(r) do { (r) = LD64(rsp); rsp += 8; } while (0)
#define RET(x) return _mm_cvtsd_f64(_mm_castsi128_pd(x))

/* integer instructions and the flags they set (zf, sf, cf, of) */
#define ARITH(W, T, d, s, SUBP, STORE) do { \
    T a_ = (T)(d), b_ = (T)(s), r_ = SUBP ? (T)(a_ - b_) : (T)(a_ + b_); \
    cf = SUBP ? a_ < b_ : r_ < a_; \
    of = SUBP ? (int)(((a_ ^ b_) & (a_ ^ r_)) >> (W - 1)) : (int)(((a_ ^ r_) & (b_ ^ r_)) >> (W - 1)); \
    zf = r_ == 0; sf = (int)(r_ >> (W - 1)); \
    if (STORE) (d) = r_; } while (0)
#define LOGIC(W, T, d, s, OPR, STORE) do { \
    T r_ = (T)((T)(d) OPR (T)(s)); cf = 0; of = 0; zf = r_ == 0; sf = (int)(r_ >> (W - 1)); \
    if (STORE) (d) = r_; } while (0)
#define ADD32(d, s) ARITH(32, uint32_t, d, s, 0, 1)
#define SUB32(d, s) ARITH(32, uint32_t, d, s, 1, 1)
#define CMP32(d, s) ARITH(32, uint32_t, d, s, 1, 0)
#define AND32(d, s) LOGIC(32, uint32_t, d, s, &, 1)
#define OR32(d, s) LOGIC(32, uint32_t, d, s, |, 1)
#define XOR32(d, s) LOGIC(32, uint32_t, d, s, ^, 1)
#define TEST32(d, s) LOGIC(32, uint32_t, d, s, &, 0)
#define ADD64(d, s) ARITH(64, uint64_t, d, s, 0, 1)
#define SUB64(d, s) ARITH(64, uint64_t, d, s, 1, 1)
#define CMP64(d, s) ARITH(64, uint64_t, d, s, 1, 0)
#define AND64(d, s) LOGIC(64, uint64_t, d, s, &, 1)
#define OR64(d, s) LOGIC(64, uint64_t, d, s, |, 1)
#define XOR64(d, s) LOGIC(64, uint64_t, d, s, ^, 1)
#define TEST64(d, s) LOGIC(64, uint64_t, d, s, &, 0)
#define NEGL(d) do { uint32_t a_ = (uint32_t)(d), r_ = 0u - a_; \
    cf = a_ != 0; of = a_ == 0x80000000u; zf = r_ == 0; sf = (int)(r_ >> 31); (d) = r_; } while (0)
#define NOTL(d) ((d) = (uint32_t)~(uint32_t)(d))
/* shifts: count masked; a zero count leaves the flags alone (a 32-bit one still zero-extends) */
#define SHIFT(W, T, ST, d, n, KIND) do { \
    unsigned c_ = (unsigned)(n) & (W - 1); T a_ = (T)(d), r_; \
    if (c_) { \
      if (KIND == 0) { r_ = (T)(a_ << c_); cf = (int)((a_ >> (W - c_)) & 1); of = (int)((r_ >> (W - 1)) ^ (T)cf); } \
      else if (KIND == 1) { r_ = a_ >> c_; cf = (int)((a_ >> (c_ - 1)) & 1); of = (int)(a_ >> (W - 1)); } \
      else { r_ = (T)((ST)a_ >> c_); cf = (int)(((ST)a_ >> (c_ - 1)) & 1); of = 0; } \
      zf = r_ == 0; sf = (int)(r_ >> (W - 1)); (d) = r_; \
    } else (d) = a_; } while (0)
#define SHL32(d, n) SHIFT(32, uint32_t, int32_t, d, n, 0)
#define SHR32(d, n) SHIFT(32, uint32_t, int32_t, d, n, 1)
#define SAR32(d, n) SHIFT(32, uint32_t, int32_t, d, n, 2)
#define SHL64(d, n) SHIFT(64, uint64_t, int64_t, d, n, 0)
#define SHR64(d, n) SHIFT(64, uint64_t, int64_t, d, n, 1)
#define SAR64(d, n) SHIFT(64, uint64_t, int64_t, d, n, 2)
#define IMUL64(d, s) do { int64_t p_; cf = of = __builtin_mul_overflow((int64_t)(d), (int64_t)(s), &p_); \
    (d) = (uint64_t)(d) * (uint64_t)(s); } while (0)
#define SBB64(d, s) do { uint64_t a_ = (d), b_ = (s), c_ = (uint64_t)cf, r_ = a_ - b_ - c_; \
    cf = a_ < b_ || a_ - b_ < c_; of = (int)(((a_ ^ b_) & (a_ ^ r_)) >> 63); \
    zf = r_ == 0; sf = (int)(r_ >> 63); (d) = r_; } while (0)
#define BSRL(d, s) do { uint32_t s_ = (uint32_t)(s); zf = s_ == 0; if (s_) (d) = (uint64_t)(31 - __builtin_clz(s_)); } while (0)
#define BSRQ(d, s) do { uint64_t s_ = (s); zf = s_ == 0; if (s_) (d) = (uint64_t)(63 - __builtin_clzll(s_)); } while (0)

/* floating point: inline asm, so the compiler cannot change an operation or its operand order */
#define FOP(NAME, INSN) static inline __m128i NAME(__m128i a, __m128i b) { __asm__(INSN " %1, %0" : "+x"(a) : "x"(b)); return a; }
#define FOPW(NAME, INSN) static inline __m128i NAME(__m128i a, __m128i b) { (void)a; __asm__(INSN " %1, %0" : "=x"(a) : "x"(b)); return a; }
FOP(ADDSD, "addsd") FOP(SUBSD, "subsd") FOP(MULSD, "mulsd") FOP(DIVSD, "divsd") FOP(SQRTSD, "sqrtsd")
FOP(MULSS, "mulss") FOP(ADDPD, "addpd") FOP(SUBPD, "subpd") FOP(MULPD, "mulpd")
FOPW(RCPPS, "rcpps") FOPW(CVTTPD2DQ, "cvttpd2dq") FOPW(CVTDQ2PD, "cvtdq2pd")
static inline __m128i CVTSI2SDL(__m128i a, uint64_t r) { uint32_t v = (uint32_t)r; __asm__("cvtsi2sdl %1, %0" : "+x"(a) : "r"(v)); return a; }
static inline __m128i CVTSI2SDQ(__m128i a, uint64_t r) { __asm__("cvtsi2sdq %1, %0" : "+x"(a) : "r"(r)); return a; }
static inline uint64_t CVTTSD2SIL(__m128i a) { uint32_t r; __asm__("cvttsd2si %1, %0" : "=r"(r) : "x"(a)); return r; }
static inline uint64_t CVTTSD2SIQ(__m128i a) { uint64_t r; __asm__("cvttsd2si %1, %0" : "=r"(r) : "x"(a)); return r; }

#ifdef HSMATH_CHECK
#define XIN(v, i) _mm_set_epi64x((long long)hs_g(500 + (i)), (long long)hs_bits(v))
static uint64_t hs_bits(double v) { uint64_t b; memcpy(&b, &v, 8); return b; }
#else
#define XIN(v, i) _mm_castpd_si128(_mm_set_sd(v))
#endif

/* tables, as in the stub sources */
static const uint32_t T_exp_ALLONES[4] __attribute__((aligned(16))) = {
  0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
};

static const uint32_t T_exp_INF[2] __attribute__((aligned(16))) = {
  0x00000000, 0x7ff00000,
};

static const uint32_t T_exp_Tbl_addr[256] __attribute__((aligned(16))) = {
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x0e03754d, 0x3cad7bbf,
  0x3e778060, 0x00002c9a, 0x3567f613, 0x3c8cd252, 0xd3158574, 0x000059b0,
  0x61e6c861, 0x3c60f74e, 0x18759bc8, 0x00008745, 0x5d837b6c, 0x3c979aa6,
  0x6cf9890f, 0x0000b558, 0x702f9cd1, 0x3c3ebe3d, 0x32d3d1a2, 0x0000e3ec,
  0x1e63bcd8, 0x3ca3516e, 0xd0125b50, 0x00011301, 0x26f0387b, 0x3ca4c554,
  0xaea92ddf, 0x0001429a, 0x62523fb6, 0x3ca95153, 0x3c7d517a, 0x000172b8,
  0x3f1353bf, 0x3c8b898c, 0xeb6fcb75, 0x0001a35b, 0x3e3a2f5f, 0x3c9aecf7,
  0x3168b9aa, 0x0001d487, 0x44a6c38d, 0x3c8a6f41, 0x88628cd6, 0x0002063b,
  0xe3a8a894, 0x3c968efd, 0x6e756238, 0x0002387a, 0x981fe7f2, 0x3c80472b,
  0x65e27cdd, 0x00026b45, 0x6d09ab31, 0x3c82f7e1, 0xf51fdee1, 0x00029e9d,
  0x720c0ab3, 0x3c8b3782, 0xa6e4030b, 0x0002d285, 0x4db0abb6, 0x3c834d75,
  0x0a31b715, 0x000306fe, 0x5dd3f84a, 0x3c8fdd39, 0xb26416ff, 0x00033c08,
  0xcc187d29, 0x3ca12f8c, 0x373aa9ca, 0x000371a7, 0x738b5e8b, 0x3ca7d229,
  0x34e59ff6, 0x0003a7db, 0xa72a4c6d, 0x3c859f48, 0x4c123422, 0x0003dea6,
  0x259d9205, 0x3ca8b846, 0x21f72e29, 0x0004160a, 0x60c2ac12, 0x3c4363ed,
  0x6061892d, 0x00044e08, 0xdaa10379, 0x3c6ecce1, 0xb5c13cd0, 0x000486a2,
  0xbb7aafb0, 0x3c7690ce, 0xd5362a27, 0x0004bfda, 0x9b282a09, 0x3ca083cc,
  0x769d2ca6, 0x0004f9b2, 0xc1aae707, 0x3ca509b0, 0x569d4f81, 0x0005342b,
  0x18fdd78e, 0x3c933505, 0x36b527da, 0x00056f47, 0xe21c5409, 0x3c9063e1,
  0xdd485429, 0x0005ab07, 0x2b64c035, 0x3c9432e6, 0x15ad2148, 0x0005e76f,
  0x99f08c0a, 0x3ca01284, 0xb03a5584, 0x0006247e, 0x0073dc06, 0x3c99f087,
  0x82552224, 0x00066238, 0x0da05571, 0x3c998d4d, 0x667f3bcc, 0x0006a09e,
  0x86ce4786, 0x3ca52bb9, 0x3c651a2e, 0x0006dfb2, 0x206f0dab, 0x3ca32092,
  0xe8ec5f73, 0x00071f75, 0x8e17a7a6, 0x3ca06122, 0x564267c8, 0x00075feb,
  0x461e9f86, 0x3ca244ac, 0x73eb0186, 0x0007a114, 0xabd66c55, 0x3c65ebe1,
  0x36cf4e62, 0x0007e2f3, 0xbbff67d0, 0x3c96fe9f, 0x994cce12, 0x00082589,
  0x14c801df, 0x3c951f14, 0x9b4492ec, 0x000868d9, 0xc1f0eab4, 0x3c8db72f,
  0x422aa0db, 0x0008ace5, 0x59f35f44, 0x3c7bf683, 0x99157736, 0x0008f1ae,
  0x9c06283c, 0x3ca360ba, 0xb0cdc5e4, 0x00093737, 0x20f962aa, 0x3c95e8d1,
  0x9fde4e4f, 0x00097d82, 0x2b91ce27, 0x3c71affc, 0x82a3f090, 0x0009c491,
  0x589a2ebd, 0x3c9b6d34, 0x7b5de564, 0x000a0c66, 0x9ab89880, 0x3c95277c,
  0xb23e255c, 0x000a5503, 0x6e735ab3, 0x3c846984, 0x5579fdbf, 0x000a9e6b,
  0x92cb3387, 0x3c8c1a77, 0x995ad3ad, 0x000ae89f, 0xdc2d1d96, 0x3ca22466,
  0xb84f15fa, 0x000b33a2, 0xb19505ae, 0x3ca1112e, 0xf2fb5e46, 0x000b7f76,
  0x0a5fddcd, 0x3c74ffd7, 0x904bc1d2, 0x000bcc1e, 0x30af0cb3, 0x3c736eae,
  0xdd85529c, 0x000c199b, 0xd10959ac, 0x3c84e08f, 0x2e57d14b, 0x000c67f1,
  0x6c921968, 0x3c676b2c, 0xdcef9069, 0x000cb720, 0x36df99b3, 0x3c937009,
  0x4a07897b, 0x000d072d, 0xa63d07a7, 0x3c74a385, 0xdcfba487, 0x000d5818,
  0xd5c192ac, 0x3c8e5a50, 0x03db3285, 0x000da9e6, 0x1c4a9792, 0x3c98bb73,
  0x337b9b5e, 0x000dfc97, 0x603a88d3, 0x3c74b604, 0xe78b3ff6, 0x000e502e,
  0x92094926, 0x3c916f27, 0xa2a490d9, 0x000ea4af, 0x41aa2008, 0x3c8ec3bc,
  0xee615a27, 0x000efa1b, 0x31d185ee, 0x3c8a64a9, 0x5b6e4540, 0x000f5076,
  0x4d91cd9d, 0x3c77893b, 0x819e90d8, 0x000fa7c1,
};

static const uint32_t T_exp_XMAX[2] __attribute__((aligned(16))) = {
  0xffffffff, 0x7fefffff,
};

static const uint32_t T_exp_XMIN[2] __attribute__((aligned(16))) = {
  0x00000000, 0x00100000,
};

static const uint32_t T_exp_bias[4] __attribute__((aligned(16))) = {
  0x0000ffc0, 0x00000000, 0x0000ffc0, 0x00000000,
};

static const uint32_t T_exp_cv[24] __attribute__((aligned(16))) = {
  0x652b82fe, 0x40571547, 0x652b82fe, 0x40571547, 0xfefa0000, 0x3f862e42,
  0xfefa0000, 0x3f862e42, 0xbc9e3b3a, 0x3d1cf79a, 0xbc9e3b3a, 0x3d1cf79a,
  0xfffffffe, 0x3fdfffff, 0xfffffffe, 0x3fdfffff, 0xe3289860, 0x3f56c15c,
  0x555b9e25, 0x3fa55555, 0xc090cf0f, 0x3f811115, 0x55548ba1, 0x3fc55555,
};

static const uint32_t T_exp_ebias[4] __attribute__((aligned(16))) = {
  0x00000000, 0x3ff00000, 0x00000000, 0x3ff00000,
};

static const uint32_t T_exp_mmask[4] __attribute__((aligned(16))) = {
  0xffffffc0, 0x00000000, 0xffffffc0, 0x00000000,
};

static const uint32_t T_k_Ctable[512] __attribute__((aligned(16))) = {
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x00000000, 0x3ff00000, 0x176d6d31, 0xbf73b92e, 0xbc29b42c, 0x3fb917a6,
  0xe0000000, 0xbc3e2718, 0x00000000, 0x3ff00000, 0x011469fb, 0xbf93ad06,
  0x3c69a60b, 0x3fc8f8b8, 0xc0000000, 0xbc626d19, 0x00000000, 0x3ff00000,
  0x939d225a, 0xbfa60bea, 0x2ed59f06, 0x3fd29406, 0xa0000000, 0xbc75d28d,
  0x00000000, 0x3ff00000, 0x866b95cf, 0xbfb37ca1, 0xa6aea963, 0x3fd87de2,
  0xe0000000, 0xbc672ced, 0x00000000, 0x3ff00000, 0x73fa1279, 0xbfbe3a68,
  0x3806f63b, 0x3fde2b5d, 0x20000000, 0x3c5e0d89, 0x00000000, 0x3ff00000,
  0x5bc57974, 0xbfc59267, 0x39ae68c8, 0x3fe1c73b, 0x20000000, 0x3c8b25dd,
  0x00000000, 0x3ff00000, 0x53aba2fd, 0xbfcd0dfe, 0x25091dd6, 0x3fe44cf3,
  0x20000000, 0x3c68076a, 0x00000000, 0x3ff00000, 0x99fcef32, 0x3fca8279,
  0x667f3bcd, 0x3fe6a09e, 0x20000000, 0xbc8bdd34, 0x00000000, 0x3fe00000,
  0x94247758, 0x3fc133cc, 0x6b151741, 0x3fe8bc80, 0x20000000, 0xbc82c5e1,
  0x00000000, 0x3fe00000, 0x9ae68c87, 0x3fac73b3, 0x290ea1a3, 0x3fea9b66,
  0xe0000000, 0x3c39f630, 0x00000000, 0x3fe00000, 0x7f909c4e, 0xbf9d4a2c,
  0xf180bdb1, 0x3fec38b2, 0x80000000, 0xbc76e0b1, 0x00000000, 0x3fe00000,
  0x65455a75, 0xbfbe0875, 0xcf328d46, 0x3fed906b, 0x20000000, 0x3c7457e6,
  0x00000000, 0x3fe00000, 0x76acf82d, 0x3fa4a031, 0x56c62dda, 0x3fee9f41,
  0xe0000000, 0x3c8760b1, 0x00000000, 0x3fd00000, 0x0e5967d5, 0xbfac1d1f,
  0xcff75cb0, 0x3fef6297, 0x20000000, 0x3c756217, 0x00000000, 0x3fd00000,
  0x0f592f50, 0xbf9ba165, 0xa3d12526, 0x3fefd88d, 0x40000000, 0xbc887df6,
  0x00000000, 0x3fc00000, 0x00000000, 0x00000000, 0x00000000, 0x3ff00000,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x0f592f50, 0x3f9ba165,
  0xa3d12526, 0x3fefd88d, 0x40000000, 0xbc887df6, 0x00000000, 0xbfc00000,
  0x0e5967d5, 0x3fac1d1f, 0xcff75cb0, 0x3fef6297, 0x20000000, 0x3c756217,
  0x00000000, 0xbfd00000, 0x76acf82d, 0xbfa4a031, 0x56c62dda, 0x3fee9f41,
  0xe0000000, 0x3c8760b1, 0x00000000, 0xbfd00000, 0x65455a75, 0x3fbe0875,
  0xcf328d46, 0x3fed906b, 0x20000000, 0x3c7457e6, 0x00000000, 0xbfe00000,
  0x7f909c4e, 0x3f9d4a2c, 0xf180bdb1, 0x3fec38b2, 0x80000000, 0xbc76e0b1,
  0x00000000, 0xbfe00000, 0x9ae68c87, 0xbfac73b3, 0x290ea1a3, 0x3fea9b66,
  0xe0000000, 0x3c39f630, 0x00000000, 0xbfe00000, 0x94247758, 0xbfc133cc,
  0x6b151741, 0x3fe8bc80, 0x20000000, 0xbc82c5e1, 0x00000000, 0xbfe00000,
  0x99fcef32, 0xbfca8279, 0x667f3bcd, 0x3fe6a09e, 0x20000000, 0xbc8bdd34,
  0x00000000, 0xbfe00000, 0x53aba2fd, 0x3fcd0dfe, 0x25091dd6, 0x3fe44cf3,
  0x20000000, 0x3c68076a, 0x00000000, 0xbff00000, 0x5bc57974, 0x3fc59267,
  0x39ae68c8, 0x3fe1c73b, 0x20000000, 0x3c8b25dd, 0x00000000, 0xbff00000,
  0x73fa1279, 0x3fbe3a68, 0x3806f63b, 0x3fde2b5d, 0x20000000, 0x3c5e0d89,
  0x00000000, 0xbff00000, 0x866b95cf, 0x3fb37ca1, 0xa6aea963, 0x3fd87de2,
  0xe0000000, 0xbc672ced, 0x00000000, 0xbff00000, 0x939d225a, 0x3fa60bea,
  0x2ed59f06, 0x3fd29406, 0xa0000000, 0xbc75d28d, 0x00000000, 0xbff00000,
  0x011469fb, 0x3f93ad06, 0x3c69a60b, 0x3fc8f8b8, 0xc0000000, 0xbc626d19,
  0x00000000, 0xbff00000, 0x176d6d31, 0x3f73b92e, 0xbc29b42c, 0x3fb917a6,
  0xe0000000, 0xbc3e2718, 0x00000000, 0xbff00000, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xbff00000,
  0x176d6d31, 0x3f73b92e, 0xbc29b42c, 0xbfb917a6, 0xe0000000, 0x3c3e2718,
  0x00000000, 0xbff00000, 0x011469fb, 0x3f93ad06, 0x3c69a60b, 0xbfc8f8b8,
  0xc0000000, 0x3c626d19, 0x00000000, 0xbff00000, 0x939d225a, 0x3fa60bea,
  0x2ed59f06, 0xbfd29406, 0xa0000000, 0x3c75d28d, 0x00000000, 0xbff00000,
  0x866b95cf, 0x3fb37ca1, 0xa6aea963, 0xbfd87de2, 0xe0000000, 0x3c672ced,
  0x00000000, 0xbff00000, 0x73fa1279, 0x3fbe3a68, 0x3806f63b, 0xbfde2b5d,
  0x20000000, 0xbc5e0d89, 0x00000000, 0xbff00000, 0x5bc57974, 0x3fc59267,
  0x39ae68c8, 0xbfe1c73b, 0x20000000, 0xbc8b25dd, 0x00000000, 0xbff00000,
  0x53aba2fd, 0x3fcd0dfe, 0x25091dd6, 0xbfe44cf3, 0x20000000, 0xbc68076a,
  0x00000000, 0xbff00000, 0x99fcef32, 0xbfca8279, 0x667f3bcd, 0xbfe6a09e,
  0x20000000, 0x3c8bdd34, 0x00000000, 0xbfe00000, 0x94247758, 0xbfc133cc,
  0x6b151741, 0xbfe8bc80, 0x20000000, 0x3c82c5e1, 0x00000000, 0xbfe00000,
  0x9ae68c87, 0xbfac73b3, 0x290ea1a3, 0xbfea9b66, 0xe0000000, 0xbc39f630,
  0x00000000, 0xbfe00000, 0x7f909c4e, 0x3f9d4a2c, 0xf180bdb1, 0xbfec38b2,
  0x80000000, 0x3c76e0b1, 0x00000000, 0xbfe00000, 0x65455a75, 0x3fbe0875,
  0xcf328d46, 0xbfed906b, 0x20000000, 0xbc7457e6, 0x00000000, 0xbfe00000,
  0x76acf82d, 0xbfa4a031, 0x56c62dda, 0xbfee9f41, 0xe0000000, 0xbc8760b1,
  0x00000000, 0xbfd00000, 0x0e5967d5, 0x3fac1d1f, 0xcff75cb0, 0xbfef6297,
  0x20000000, 0xbc756217, 0x00000000, 0xbfd00000, 0x0f592f50, 0x3f9ba165,
  0xa3d12526, 0xbfefd88d, 0x40000000, 0x3c887df6, 0x00000000, 0xbfc00000,
  0x00000000, 0x00000000, 0x00000000, 0xbff00000, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x0f592f50, 0xbf9ba165, 0xa3d12526, 0xbfefd88d,
  0x40000000, 0x3c887df6, 0x00000000, 0x3fc00000, 0x0e5967d5, 0xbfac1d1f,
  0xcff75cb0, 0xbfef6297, 0x20000000, 0xbc756217, 0x00000000, 0x3fd00000,
  0x76acf82d, 0x3fa4a031, 0x56c62dda, 0xbfee9f41, 0xe0000000, 0xbc8760b1,
  0x00000000, 0x3fd00000, 0x65455a75, 0xbfbe0875, 0xcf328d46, 0xbfed906b,
  0x20000000, 0xbc7457e6, 0x00000000, 0x3fe00000, 0x7f909c4e, 0xbf9d4a2c,
  0xf180bdb1, 0xbfec38b2, 0x80000000, 0x3c76e0b1, 0x00000000, 0x3fe00000,
  0x9ae68c87, 0x3fac73b3, 0x290ea1a3, 0xbfea9b66, 0xe0000000, 0xbc39f630,
  0x00000000, 0x3fe00000, 0x94247758, 0x3fc133cc, 0x6b151741, 0xbfe8bc80,
  0x20000000, 0x3c82c5e1, 0x00000000, 0x3fe00000, 0x99fcef32, 0x3fca8279,
  0x667f3bcd, 0xbfe6a09e, 0x20000000, 0x3c8bdd34, 0x00000000, 0x3fe00000,
  0x53aba2fd, 0xbfcd0dfe, 0x25091dd6, 0xbfe44cf3, 0x20000000, 0xbc68076a,
  0x00000000, 0x3ff00000, 0x5bc57974, 0xbfc59267, 0x39ae68c8, 0xbfe1c73b,
  0x20000000, 0xbc8b25dd, 0x00000000, 0x3ff00000, 0x73fa1279, 0xbfbe3a68,
  0x3806f63b, 0xbfde2b5d, 0x20000000, 0xbc5e0d89, 0x00000000, 0x3ff00000,
  0x866b95cf, 0xbfb37ca1, 0xa6aea963, 0xbfd87de2, 0xe0000000, 0x3c672ced,
  0x00000000, 0x3ff00000, 0x939d225a, 0xbfa60bea, 0x2ed59f06, 0xbfd29406,
  0xa0000000, 0x3c75d28d, 0x00000000, 0x3ff00000, 0x011469fb, 0xbf93ad06,
  0x3c69a60b, 0xbfc8f8b8, 0xc0000000, 0x3c626d19, 0x00000000, 0x3ff00000,
  0x176d6d31, 0xbf73b92e, 0xbc29b42c, 0xbfb917a6, 0xe0000000, 0x3c3e2718,
  0x00000000, 0x3ff00000,
};

static const uint32_t T_k_NEG_ZERO[2] __attribute__((aligned(16))) = {
  0x00000000, 0x80000000,
};

static const uint32_t T_k_ONE[2] __attribute__((aligned(16))) = {
  0x00000000, 0x3ff00000,
};

static const uint32_t T_k_ONEHALF[4] __attribute__((aligned(16))) = {
  0x00000000, 0x3fe00000, 0x00000000, 0x3fe00000,
};

static const uint32_t T_k_PI32INV[2] __attribute__((aligned(16))) = {
  0x6dc9c883, 0x40245f30,
};

static const uint32_t T_k_PI_4[4] __attribute__((aligned(16))) = {
  0x40000000, 0x3fe921fb, 0x18469899, 0x3e64442d,
};

static const uint32_t T_k_PI_INV_TABLE[41] __attribute__((aligned(16))) = {
  0x00000000, 0x00000000, 0xa2f9836e, 0x4e441529, 0xfc2757d1, 0xf534ddc0,
  0xdb629599, 0x3c439041, 0xfe5163ab, 0xdebbc561, 0xb7246e3a, 0x424dd2e0,
  0x06492eea, 0x09d1921c, 0xfe1deb1c, 0xb129a73e, 0xe88235f5, 0x2ebb4484,
  0xe99c7026, 0xb45f7e41, 0x3991d639, 0x835339f4, 0x9c845f8b, 0xbdf9283b,
  0x1ff897ff, 0xde05980f, 0xef2f118b, 0x5a0a6d1f, 0x6d367ecf, 0x27cb09b7,
  0x4f463f66, 0x9e5fea2d, 0x7527bac7, 0xebe5f17b, 0x3d0739f7, 0x8a5292ea,
  0x6bfb5fb1, 0x1f8d5d08, 0x56033046, 0xfc7b6bab, 0xf0cfbc21,
};

static const uint32_t T_k_P_1[2] __attribute__((aligned(16))) = {
  0x54400000, 0x3fb921fb,
};

static const uint32_t T_k_P_2[4] __attribute__((aligned(16))) = {
  0x1a600000, 0x3d90b461, 0x1a600000, 0x3d90b461,
};

static const uint32_t T_k_P_3[2] __attribute__((aligned(16))) = {
  0x2e037073, 0x3b63198a,
};

static const uint32_t T_k_SC_1[4] __attribute__((aligned(16))) = {
  0x55555555, 0xbfc55555, 0x00000000, 0xbfe00000,
};

static const uint32_t T_k_SC_2[4] __attribute__((aligned(16))) = {
  0x11111111, 0x3f811111, 0x55555555, 0x3fa55555,
};

static const uint32_t T_k_SC_3[4] __attribute__((aligned(16))) = {
  0x1a01a01a, 0xbf2a01a0, 0x16c16c17, 0xbf56c16c,
};

static const uint32_t T_k_SC_4[4] __attribute__((aligned(16))) = {
  0xa556c734, 0x3ec71de3, 0x1a01a01a, 0x3efa01a0,
};

static const uint32_t T_k_SHIFTER[4] __attribute__((aligned(16))) = {
  0x00000000, 0x43380000, 0x00000000, 0x43380000,
};

static const uint32_t T_k_SIGN_MASK[2] __attribute__((aligned(16))) = {
  0x00000000, 0x80000000,
};

static const uint32_t T_k_TWO_POW_55[2] __attribute__((aligned(16))) = {
  0x00000000, 0x43600000,
};

static const uint32_t T_k_TWO_POW_M55[2] __attribute__((aligned(16))) = {
  0x00000000, 0x3c800000,
};

static const uint32_t T_k_ZERO[2] __attribute__((aligned(16))) = {
  0x00000000, 0x00000000,
};

static const uint32_t T_log_HIGHSIGMASK_log10[4] __attribute__((aligned(16))) = {
  0xf8000000, 0xffffffff, 0x00000000, 0xffffe000,
};

static const uint32_t T_log_LOG10_E[4] __attribute__((aligned(16))) = {
  0x00000000, 0x3fdbc000, 0xbf2e4108, 0x3f5a7a6c,
};

static const uint32_t T_log_L_tbl[516] __attribute__((aligned(16))) = {
  0xfefa3800, 0x3fe62e42, 0x93c76730, 0x3d2ef357, 0xaa241800, 0x3fe5ee82,
  0x0cda46be, 0x3d220238, 0x5c364800, 0x3fe5af40, 0xac10c9fb, 0x3d2dfa63,
  0x26bb8c00, 0x3fe5707a, 0xff3303dd, 0x3d09980b, 0x26867800, 0x3fe5322e,
  0x5d257531, 0x3d05ccc4, 0x835a5000, 0x3fe4f45a, 0x6d93b8fb, 0xbd2e6c51,
  0x6f970c00, 0x3fe4b6fd, 0xed4c541c, 0x3cef7115, 0x27e8a400, 0x3fe47a15,
  0xf94d60aa, 0xbd22cb6a, 0xf2f92400, 0x3fe43d9f, 0x481051f7, 0xbcfd984f,
  0x2125cc00, 0x3fe4019c, 0x30f0c74c, 0xbd26ce79, 0x0c36c000, 0x3fe3c608,
  0x7cfe13c2, 0xbd02b736, 0x17197800, 0x3fe38ae2, 0xbb5569a4, 0xbd218b7a,
  0xad9d8c00, 0x3fe35028, 0x9527e6ac, 0x3d10b83f, 0x44340800, 0x3fe315da,
  0xc5a0ed9c, 0xbd274e93, 0x57b0e000, 0x3fe2dbf5, 0x07b9dc11, 0xbd17a6e5,
  0x6d0ec000, 0x3fe2a278, 0xe797882d, 0x3d206d2b, 0x1134dc00, 0x3fe26962,
  0x05226250, 0xbd0b61f1, 0xd8bebc00, 0x3fe230b0, 0x6e48667b, 0x3d12fc06,
  0x5fc61800, 0x3fe1f863, 0xc9fe81d3, 0xbd2a7242, 0x49ae6000, 0x3fe1c078,
  0xed70e667, 0x3cccacde, 0x40f23c00, 0x3fe188ee, 0xf8ab4650, 0x3d14cc4e,
  0xf6f29800, 0x3fe151c3, 0xa293ae49, 0xbd2edd97, 0x23c75c00, 0x3fe11af8,
  0xbb9ddcb2, 0xbd258647, 0x8611cc00, 0x3fe0e489, 0x07801742, 0x3d1c2998,
  0xe2d05400, 0x3fe0ae76, 0x887e7e27, 0x3d1f486b, 0x0533c400, 0x3fe078bf,
  0x41edf5fd, 0x3d268122, 0xbe760400, 0x3fe04360, 0xe79539e0, 0xbd04c45f,
  0xe5b20800, 0x3fe00e5a, 0xb1727b1c, 0xbd053ba3, 0xaf7a4800, 0x3fdfb358,
  0x3c164935, 0x3d0085fa, 0xee031800, 0x3fdf4aa7, 0x6f014a8b, 0x3d12cde5,
  0x56b41000, 0x3fdee2a1, 0x5a470251, 0x3d2f27f4, 0xc3ddb000, 0x3fde7b42,
  0x5372bd08, 0xbd246550, 0x1a272800, 0x3fde148a, 0x07322938, 0xbd1326b2,
  0x484c9800, 0x3fddae75, 0x60dc616a, 0xbd1ea42d, 0x46def800, 0x3fdd4902,
  0xe9a767a8, 0x3d235baf, 0x18064800, 0x3fdce42f, 0x3ec7a6b0, 0xbd0797c3,
  0xc7455800, 0x3fdc7ff9, 0xc15249ae, 0xbd29b6dd, 0x693fa000, 0x3fdc1c60,
  0x7fe8e180, 0x3d2cec80, 0x1b80e000, 0x3fdbb961, 0xf40a666d, 0x3d27d85b,
  0x04462800, 0x3fdb56fa, 0x2d841995, 0x3d109525, 0x5248d000, 0x3fdaf529,
  0x52774458, 0xbd217cc5, 0x3c8ad800, 0x3fda93ed, 0xbea77a5d, 0x3d1e36f2,
  0x0224f800, 0x3fda3344, 0x7f9d79f5, 0x3d23c645, 0xea15f000, 0x3fd9d32b,
  0x10d0c0b0, 0xbd26279e, 0x43135800, 0x3fd973a3, 0xa502d9f0, 0xbd152313,
  0x635bf800, 0x3fd914a8, 0x2ee6307d, 0xbd1766b5, 0xa88b3000, 0x3fd8b639,
  0xe5e70470, 0xbd205ae1, 0x776dc800, 0x3fd85855, 0x3333778a, 0x3d2fd56f,
  0x3bd81800, 0x3fd7fafa, 0xc812566a, 0xbd272090, 0x687cf800, 0x3fd79e26,
  0x2efd1778, 0x3d29ec7d, 0x76c67800, 0x3fd741d8, 0x49dc60b3, 0x3d2d8b09,
  0xe6af1800, 0x3fd6e60e, 0x7c222d87, 0x3d172165, 0x3e9c6800, 0x3fd68ac8,
  0x2756eba0, 0x3d20a0d3, 0x0b3ab000, 0x3fd63003, 0xe731ae00, 0xbd2db623,
  0xdf596000, 0x3fd5d5bd, 0x08a465dc, 0xbd0a0b2a, 0x53c8d000, 0x3fd57bf7,
  0xee5d40ef, 0x3d1faded, 0x0738a000, 0x3fd522ae, 0x8164c759, 0x3d2ebe70,
  0x9e173000, 0x3fd4c9e0, 0x1b0ad8a4, 0xbd2e2089, 0xc271c800, 0x3fd4718d,
  0x0967d675, 0xbd2f27ce, 0x23d5e800, 0x3fd419b4, 0xec90e09d, 0x3d08e436,
  0x77333000, 0x3fd3c252, 0xb606bd5c, 0x3d183b54, 0x76be1000, 0x3fd36b67,
  0xb0f177c8, 0x3d116ecd, 0xe1d36000, 0x3fd314f1, 0xd3213cb8, 0xbd28e27a,
  0x7cdc9000, 0x3fd2bef0, 0x4a5004f4, 0x3d2a9cfa, 0x1134d800, 0x3fd26962,
  0xdf5bb3b6, 0x3d2c93c1, 0x6d0eb800, 0x3fd21445, 0xba46baea, 0x3d0a87de,
  0x635a6800, 0x3fd1bf99, 0x5147bdb7, 0x3d2ca6ed, 0xcbacf800, 0x3fd16b5c,
  0xf7a51681, 0x3d2b9acd, 0x8227e800, 0x3fd1178e, 0x63a5f01c, 0xbd2c210e,
  0x67616000, 0x3fd0c42d, 0x163ceae9, 0x3d27188b, 0x604d5800, 0x3fd07138,
  0x16ed4e91, 0x3cf89cdb, 0x5626c800, 0x3fd01eae, 0x1485e94a, 0xbd16f08c,
  0x6cb3b000, 0x3fcf991c, 0xca0cdf30, 0x3d1bcbec, 0xe4dd0000, 0x3fcef5ad,
  0x65bb8e11, 0xbcca2115, 0xffe71000, 0x3fce530e, 0x6041f430, 0x3cc21227,
  0xb0d49000, 0x3fcdb13d, 0xf715b035, 0xbd2aff2a, 0xf2656000, 0x3fcd1037,
  0x75b6f6e4, 0xbd084a7e, 0xc6f01000, 0x3fcc6ffb, 0xc5962bd2, 0xbcf1ec72,
  0x383be000, 0x3fcbd087, 0x595412b6, 0xbd2d4bc4, 0x575bd000, 0x3fcb31d8,
  0x4eace1aa, 0xbd0c358d, 0x3c8ae000, 0x3fca93ed, 0x50562169, 0xbd287243,
  0x07089000, 0x3fc9f6c4, 0x6865817a, 0x3d29904d, 0xdcf70000, 0x3fc95a5a,
  0x58a0ff6f, 0x3d07f228, 0xeb390000, 0x3fc8beaf, 0xaae92cd1, 0xbd073d54,
  0x6551a000, 0x3fc823c1, 0x9a631e83, 0x3d1e0ddb, 0x85445000, 0x3fc7898d,
  0x70914305, 0xbd1c6610, 0x8b757000, 0x3fc6f012, 0xe59c21e1, 0xbd25118d,
  0xbe8c1000, 0x3fc6574e, 0x2c3c2e78, 0x3d19cf8b, 0x6b544000, 0x3fc5bf40,
  0xeb68981c, 0xbd127023, 0xe4a1b000, 0x3fc527e5, 0xe5697dc7, 0x3d2633e8,
  0x8333b000, 0x3fc4913d, 0x54fdb678, 0x3d258379, 0xa5993000, 0x3fc3fb45,
  0x7e6a354d, 0xbd2cd1d8, 0xb0159000, 0x3fc365fc, 0x234b7289, 0x3cc62fa8,
  0x0c868000, 0x3fc2d161, 0xcb81b4a1, 0x3d039d6c, 0x2a49c000, 0x3fc23d71,
  0x8fd3df5c, 0x3d100d23, 0x7e23f000, 0x3fc1aa2b, 0x44389934, 0x3d2ca78e,
  0x8227e000, 0x3fc1178e, 0xce2d07f2, 0x3d21ef78, 0xb59e4000, 0x3fc08598,
  0x7009902c, 0xbd27e5dd, 0x39dbe000, 0x3fbfe891, 0x4fa10afd, 0xbd2534d6,
  0x830a2000, 0x3fbec739, 0xafe645e0, 0xbd2dc068, 0x63844000, 0x3fbda727,
  0x1fa71733, 0x3d1a8940, 0x01bc4000, 0x3fbc8858, 0xc65aacd3, 0x3d2646d1,
  0x8dad6000, 0x3fbb6ac8, 0x2bf768e5, 0xbd139080, 0x40b1c000, 0x3fba4e76,
  0xb94407c8, 0xbd0e42b6, 0x5d594000, 0x3fb9335e, 0x3abd47da, 0x3d23115c,
  0x2f40e000, 0x3fb8197e, 0xf96ffdf7, 0x3d0f80dc, 0x0aeac000, 0x3fb700d3,
  0xa99ded32, 0x3cec1e8d, 0x4d97a000, 0x3fb5e95a, 0x3c5d1d1e, 0xbd2c6906,
  0x5d208000, 0x3fb4d311, 0x82f4e1ef, 0xbcf53a25, 0xa7d1e000, 0x3fb3bdf5,
  0xa5db4ed7, 0x3d2cc85e, 0xa4472000, 0x3fb2aa04, 0xae9c697d, 0xbd20b6e8,
  0xd1466000, 0x3fb1973b, 0x560d9e9b, 0xbd25325d, 0xb59e4000, 0x3fb08598,
  0x7009902c, 0xbd17e5dd, 0xc006c000, 0x3faeea31, 0x4fc93b7b, 0xbd0e113e,
  0xcdddc000, 0x3faccb73, 0x47d82807, 0xbd1a68f2, 0xd0fb0000, 0x3faaaef2,
  0x353bb42e, 0x3d20fc1a, 0x149fc000, 0x3fa894aa, 0xd05a267d, 0xbd197995,
  0xf2d4c000, 0x3fa67c94, 0xec19afa2, 0xbd029efb, 0xd42e0000, 0x3fa466ae,
  0x75bdfd28, 0xbd2c1673, 0x2f8d0000, 0x3fa252f3, 0xe021b67b, 0x3d283e9a,
  0x89e74000, 0x3fa0415d, 0x5cf1d753, 0x3d0111c0, 0xec148000, 0x3f9c63d2,
  0x3f9eb2f3, 0x3d2578c6, 0x28c90000, 0x3f984925, 0x325a0c34, 0xbd2aa0ba,
  0x25980000, 0x3f9432a9, 0x928637fe, 0x3d098139, 0x58938000, 0x3f902056,
  0x06e2f7d2, 0xbd23dc5b, 0xa3890000, 0x3f882448, 0xda74f640, 0xbd275577,
  0x75890000, 0x3f801015, 0x999d2be8, 0xbd10c76b, 0x59580000, 0x3f700805,
  0xcb31c67b, 0x3d2166af, 0x00000000, 0x00000000, 0x00000000, 0x80000000,
};

static const uint32_t T_log_L_tbl_log10[516] __attribute__((aligned(16))) = {
  0x509f7800, 0x3fd34413, 0x1f12b358, 0x3d1fef31, 0x80333400, 0x3fd32418,
  0xc671d9d0, 0xbcf542bf, 0x51195000, 0x3fd30442, 0x78a4b0c3, 0x3d18216a,
  0x6fc79400, 0x3fd2e490, 0x80fa389d, 0xbc902869, 0x89d04000, 0x3fd2c502,
  0x75c2f564, 0x3d040754, 0x4ddd1c00, 0x3fd2a598, 0xd219b2c3, 0xbcfa1d84,
  0x6baa7c00, 0x3fd28651, 0xfd9abec1, 0x3d1be6d3, 0x94028800, 0x3fd2672d,
  0xe289a455, 0xbd1ede5e, 0x78b86400, 0x3fd2482c, 0x6734d179, 0x3d1fe79b,
  0xcca3c800, 0x3fd2294d, 0x981a40b8, 0xbced34ea, 0x439c5000, 0x3fd20a91,
  0xcc392737, 0xbd1a9cc3, 0x92752c00, 0x3fd1ebf6, 0x03c9afe7, 0x3d1e98f8,
  0x6ef8dc00, 0x3fd1cd7d, 0x71dae7f4, 0x3d08a86c, 0x8fe4dc00, 0x3fd1af25,
  0xee9185a1, 0xbcff3412, 0xace59400, 0x3fd190ee, 0xc2cab353, 0x3cf17ed9,
  0x7e925000, 0x3fd172d8, 0x6952c1b2, 0x3cf1521c, 0xbe694400, 0x3fd154e2,
  0xcacb79ca, 0xbd0bdc78, 0x26cbac00, 0x3fd1370d, 0xf71f4de1, 0xbd01f8be,
  0x72fa0800, 0x3fd11957, 0x55bf910b, 0x3c946e2b, 0x5f106000, 0x3fd0fbc1,
  0x39e639c1, 0x3d14a84b, 0xa802a800, 0x3fd0de4a, 0xd3f31d5d, 0xbd178385,
  0x0b992000, 0x3fd0c0f3, 0x3843106f, 0xbd1f602f, 0x486ce800, 0x3fd0a3ba,
  0x8819497c, 0x3cef987a, 0x1de49400, 0x3fd086a0, 0x1caa0467, 0x3d0faec7,
  0x4c30cc00, 0x3fd069a4, 0xa4424372, 0xbd1618fc, 0x94490000, 0x3fd04cc6,
  0x946517d2, 0xbd18384b, 0xb7e84000, 0x3fd03006, 0xe0109c37, 0xbd19a6ac,
  0x798a0c00, 0x3fd01364, 0x5121e864, 0xbd164cf7, 0x38ce8000, 0x3fcfedbf,
  0x46214d1a, 0xbcbbc402, 0xc8e62000, 0x3fcfb4ef, 0xdab93203, 0x3d1e0176,
  0x2cb02800, 0x3fcf7c5a, 0x2a2ea8e4, 0xbcfec86a, 0xeeeaa000, 0x3fcf43fd,
  0xc18e49a4, 0x3cf110a8, 0x9bb6e800, 0x3fcf0bda, 0x923cc9c0, 0xbd15ce99,
  0xc093f000, 0x3fced3ef, 0x4d4b51e9, 0x3d1a04c7, 0xec58f800, 0x3fce9c3c,
  0x163cad59, 0x3cac8260, 0x9a907000, 0x3fce2d7d, 0x3fa93646, 0x3ce4a1c0,
  0x37311000, 0x3fcdbf99, 0x32abd1fd, 0x3d07ea9d, 0x6744b800, 0x3fcd528c,
  0x4dcbdfd4, 0xbd1b08e2, 0xe36de800, 0x3fcce653, 0x0b7b7f7f, 0xbd1b8f03,
  0x77506800, 0x3fcc7aec, 0xa821c9fb, 0x3d13c163, 0x00ff8800, 0x3fcc1053,
  0x536bca76, 0xbd074ee5, 0x70719800, 0x3fcba684, 0xd7da9b6b, 0xbd1fbf16,
  0xc6f8d800, 0x3fcb3d7d, 0xe2220bb3, 0x3d1a295d, 0x16c15800, 0x3fcad53c,
  0xe724911e, 0xbcf55822, 0x82533800, 0x3fca6dbc, 0x6d982371, 0x3cac567c,
  0x3c19e800, 0x3fca06fc, 0x84d17d80, 0x3d1da204, 0x85ef8000, 0x3fc9a0f8,
  0x54466a6a, 0xbd002204, 0xb0ac2000, 0x3fc93bae, 0xd601fd65, 0x3d18840c,
  0x1bb9b000, 0x3fc8d71c, 0x7bf58766, 0xbd14f897, 0x34aae800, 0x3fc8733e,
  0x3af6ac24, 0xbd0f5c45, 0x76d68000, 0x3fc81012, 0x4303e1a1, 0xbd1f9a80,
  0x6af57800, 0x3fc7ad96, 0x43fbcb46, 0x3cf4c33e, 0xa6c51000, 0x3fc74bc7,
  0x70f0eac5, 0xbd192e3b, 0xccab9800, 0x3fc6eaa3, 0xc0093dfe, 0xbd0faf15,
  0x8b60b800, 0x3fc68a28, 0xde78d5fd, 0xbc9ea4ee, 0x9d987000, 0x3fc62a53,
  0x962bea6e, 0xbd194084, 0xc9b0e800, 0x3fc5cb22, 0x888dd999, 0x3d1fe201,
  0xe1634800, 0x3fc56c93, 0x16ada7ad, 0x3d1b1188, 0xc176c000, 0x3fc50ea4,
  0x4159b5b5, 0xbcf09c08, 0x51766000, 0x3fc4b153, 0x84393d23, 0xbcf6a89c,
  0x83695000, 0x3fc4549d, 0x9f0b8bbb, 0x3d1c4b8c, 0x538d5800, 0x3fc3f881,
  0xf49df747, 0x3cf89b99, 0xc8138000, 0x3fc39cfc, 0xd503b834, 0xbd13b99f,
  0xf0df0800, 0x3fc3420d, 0xf011b386, 0xbd05d8be, 0xe7466800, 0x3fc2e7b2,
  0xf39c7bc2, 0xbd1bb94e, 0xcdd62800, 0x3fc28de9, 0x05e6d69b, 0xbd10ed05,
  0xd015d800, 0x3fc234b0, 0xe29b6c9d, 0xbd1ff967, 0x224ea800, 0x3fc1dc06,
  0x727711fc, 0xbcffb30d, 0x01540000, 0x3fc183e8, 0x39786c5a, 0x3cc23f57,
  0xb24d9800, 0x3fc12c54, 0xc905a342, 0x3d003a1d, 0x82835800, 0x3fc0d54a,
  0x9b9920c0, 0x3d03b25a, 0xc72ac000, 0x3fc07ec7, 0x46f26a24, 0x3cf0fa41,
  0xdd35d800, 0x3fc028ca, 0x41d9d6dc, 0x3d034a65, 0x52474000, 0x3fbfa6a4,
  0x44f66449, 0x3d19cad3, 0x2da3d000, 0x3fbefcb8, 0x67832999, 0x3d18400f,
  0x32a10000, 0x3fbe53ce, 0x9c0e3b1a, 0xbcff62fd, 0x556b7000, 0x3fbdabe3,
  0x02976913, 0xbcf8243b, 0x97e88000, 0x3fbd04f4, 0xec793797, 0x3d1c0578,
  0x09647000, 0x3fbc5eff, 0x05fc0565, 0xbd1d799e, 0xc6426000, 0x3fbbb9ff,
  0x4625f5ed, 0x3d1f5723, 0xf7afd000, 0x3fbb15f3, 0xdd5aae61, 0xbd1a7e1e,
  0xd358b000, 0x3fba72d8, 0x3314e4d3, 0x3d17bc91, 0x9b1f5000, 0x3fb9d0ab,
  0x9a4d514b, 0x3cf18c9b, 0x9cd4e000, 0x3fb92f69, 0x7e4496ab, 0x3cf1f96d,
  0x31f4f000, 0x3fb88f10, 0xf56479e7, 0x3d165818, 0xbf628000, 0x3fb7ef9c,
  0x26bf486d, 0xbd1113a6, 0xb526b000, 0x3fb7510c, 0x1a1c3384, 0x3ca9898d,
  0x8e31e000, 0x3fb6b35d, 0xb3875361, 0xbd0661ac, 0xd01de000, 0x3fb6168c,
  0x2a7cacfa, 0xbd1bdf10, 0x0af23000, 0x3fb57a98, 0xff868816, 0x3cf046d0,
  0xd8ea0000, 0x3fb4df7c, 0x1515fbe7, 0xbd1fd529, 0xde3b2000, 0x3fb44538,
  0x6e59a132, 0x3d1faeee, 0xc8df9000, 0x3fb3abc9, 0xf1322361, 0xbd198807,
  0x505f1000, 0x3fb3132d, 0x0888e6ab, 0x3d1e5380, 0x359bd000, 0x3fb27b61,
  0xdfbcbb22, 0xbcfe2724, 0x429ee000, 0x3fb1e463, 0x6eb4c58c, 0xbcfe4dd6,
  0x4a673000, 0x3fb14e31, 0x4ce1ac9b, 0x3d1ba691, 0x28b96000, 0x3fb0b8c9,
  0x8c7813b8, 0xbd0b3872, 0xc1f08000, 0x3fb02428, 0xc2bc8c2c, 0x3cb5ea6b,
  0x05a1a000, 0x3faf209c, 0x72e8f18e, 0xbce8df84, 0xc0b5e000, 0x3fadfa6d,
  0x9fdef436, 0x3d087364, 0xaf416000, 0x3facd5c2, 0x1068c3a9, 0x3d0827e7,
  0xdb356000, 0x3fabb296, 0x120a34d3, 0x3d101a9f, 0x5dfea000, 0x3faa90e6,
  0xdaded264, 0xbd14c392, 0x6034c000, 0x3fa970ad, 0x1c9d06a9, 0xbd1b705e,
  0x194c6000, 0x3fa851e8, 0x83996ad9, 0xbd0117bc, 0xcf4ac000, 0x3fa73492,
  0xb1a94a62, 0xbca5ea42, 0xd67b4000, 0x3fa618a9, 0x75aed8ca, 0xbd07119b,
  0x9126c000, 0x3fa4fe29, 0x5291d533, 0x3d12658f, 0x6f4d4000, 0x3fa3e50e,
  0xcd2c5cd9, 0x3d1d5c70, 0xee608000, 0x3fa2cd54, 0xd1008489, 0x3d1a4802,
  0x9900e000, 0x3fa1b6f9, 0x54fb5598, 0xbd16593f, 0x06bb6000, 0x3fa0a1f9,
  0x64ef57b4, 0xbd17636b, 0xb7940000, 0x3f9f1c9f, 0xee6a4737, 0x3cb5d479,
  0x91aa0000, 0x3f9cf7f5, 0x3a16373c, 0x3d087114, 0x156b8000, 0x3f9ad5ed,
  0x836c554a, 0x3c6900b0, 0xd4764000, 0x3f98b67f, 0xed12f17b, 0xbcffc974,
  0x77dec000, 0x3f9699a7, 0x232ce7ea, 0x3d1e35bb, 0xbfbf4000, 0x3f947f5d,
  0xd84ffa6e, 0x3d0e0a49, 0x82c7c000, 0x3f92679c, 0x8d170e90, 0xbd14d9f2,
  0xadd20000, 0x3f90525d, 0x86d9f88e, 0x3cdeb986, 0x86f10000, 0x3f8c7f36,
  0xb9e0a517, 0x3ce29faa, 0xb75c8000, 0x3f885e9e, 0x542568cb, 0xbd1f7bdb,
  0x46b30000, 0x3f8442e8, 0xb954e7d9, 0x3d1e5287, 0xb7e60000, 0x3f802c07,
  0x22da0b17, 0xbd19fb27, 0x6c8b0000, 0x3f7833e3, 0x821271ef, 0xbd190f96,
  0x29910000, 0x3f701936, 0xbc3491a5, 0xbd1bcf45, 0x354a0000, 0x3f600fe3,
  0xc0ff520a, 0xbd19d71c, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
};

static const uint32_t T_log_coeff[12] __attribute__((aligned(16))) = {
  0x92492492, 0x3fc24924, 0x00000000, 0xbfd00000, 0x3d6fb175, 0xbfc5555e,
  0x55555555, 0x3fd55555, 0x9999999a, 0x3fc99999, 0x00000000, 0xbfe00000,
};

static const uint32_t T_log_coeff_log10[12] __attribute__((aligned(16))) = {
  0xc1a5f12e, 0x40358874, 0x64d4ef0d, 0xc0089309, 0x385593b1, 0xc025c917,
  0xdc963467, 0x3ffc6a02, 0x7f9d3aa1, 0x4016ab9f, 0xdc77b115, 0xbff27af2,
};

static const uint32_t T_log_log2[4] __attribute__((aligned(16))) = {
  0xfefa3800, 0x3fa62e42, 0x93c76730, 0x3ceef357,
};

static const uint32_t T_log_log2_log10[4] __attribute__((aligned(16))) = {
  0x509f7800, 0x3f934413, 0x1f12b358, 0x3cdfef31,
};

static const uint32_t T_pow_DOUBLE0[2] __attribute__((aligned(16))) = {
  0x00000000, 0x00000000,
};

static const uint32_t T_pow_DOUBLE0DOT5[2] __attribute__((aligned(16))) = {
  0x00000000, 0x3fe00000,
};

static const uint32_t T_pow_DOUBLE2[2] __attribute__((aligned(16))) = {
  0x00000000, 0x40000000,
};

static const uint32_t T_pow_HALFMASK[4] __attribute__((aligned(16))) = {
  0xf8000000, 0xffffffff, 0xf8000000, 0xffffffff,
};

static const uint32_t T_pow_HIGHMASK_LOG_X[4] __attribute__((aligned(16))) = {
  0xf8000000, 0xffffffff, 0x00000000, 0xfffff800,
};

static const uint32_t T_pow_HIGHMASK_Y[4] __attribute__((aligned(16))) = {
  0x00000000, 0xfffffff8, 0x00000000, 0xffffffff,
};

static const uint32_t T_pow_HIGHSIGMASK[4] __attribute__((aligned(16))) = {
  0x00000000, 0xfffff800, 0x00000000, 0xfffff800,
};

static const uint32_t T_pow_LOG2_E[4] __attribute__((aligned(16))) = {
  0x00000000, 0x3ff72000, 0x161bb241, 0xbf5dabe1,
};

static const uint32_t T_pow_L_tbl_pow[2052] __attribute__((aligned(16))) = {
  0x00000000, 0x3ff00000, 0x00000000, 0x00000000, 0x20000000, 0x3feff00a,
  0x96621f95, 0x3e5b1856, 0xe0000000, 0x3fefe019, 0xe5916f9e, 0xbe325278,
  0x00000000, 0x3fefd02f, 0x859a1062, 0x3e595fb7, 0xc0000000, 0x3fefc049,
  0xb245f18f, 0xbe529c38, 0xe0000000, 0x3fefb069, 0xad2880a7, 0xbe501230,
  0x60000000, 0x3fefa08f, 0xc8e72420, 0x3e597bd1, 0x80000000, 0x3fef90ba,
  0xc30c4500, 0xbe5d6c75, 0xe0000000, 0x3fef80ea, 0x02c63f43, 0x3e2e1318,
  0xc0000000, 0x3fef7120, 0xb3d4cccc, 0xbe44c52a, 0x00000000, 0x3fef615c,
  0xdbd91397, 0xbe4e7d6c, 0xa0000000, 0x3fef519c, 0x65c5cd68, 0xbe522dc8,
  0xa0000000, 0x3fef41e2, 0x46d1306c, 0xbe5a840e, 0xe0000000, 0x3fef322d,
  0xd2980e94, 0x3e5071af, 0xa0000000, 0x3fef227e, 0x773abade, 0xbe5891e5,
  0xa0000000, 0x3fef12d4, 0xdc6bf46b, 0xbe5cccbe, 0xe0000000, 0x3fef032f,
  0xbc7247fa, 0xbe2bab83, 0x80000000, 0x3feef390, 0xbcaa1e46, 0xbe53bb3b,
  0x60000000, 0x3feee3f6, 0x5f6c682d, 0xbe54c619, 0x80000000, 0x3feed461,
  0x5141e368, 0xbe4b6d86, 0xe0000000, 0x3feec4d1, 0xec678f76, 0xbe369af6,
  0x80000000, 0x3feeb547, 0x41301f55, 0xbe2d4312, 0x60000000, 0x3feea5c2,
  0x676da6bd, 0xbe4d8dd0, 0x60000000, 0x3fee9642, 0x57a891c4, 0x3e51f991,
  0xa0000000, 0x3fee86c7, 0xe4eb491e, 0x3e579bf9, 0x20000000, 0x3fee7752,
  0xfddc4a2c, 0xbe3356e6, 0xc0000000, 0x3fee67e1, 0xd75b5bf1, 0xbe449531,
  0x80000000, 0x3fee5876, 0xbd423b8e, 0x3df54fe4, 0x60000000, 0x3fee4910,
  0x330e51b9, 0x3e54289c, 0x80000000, 0x3fee39af, 0x8651a95f, 0xbe55aad6,
  0xa0000000, 0x3fee2a53, 0x5e98c708, 0xbe2fc4a9, 0xe0000000, 0x3fee1afc,
  0x0989328d, 0x3e23958c, 0x40000000, 0x3fee0bab, 0xee642abd, 0xbe425dd8,
  0xa0000000, 0x3fedfc5e, 0xc394d236, 0x3e526362, 0x20000000, 0x3feded17,
  0xe104aa8e, 0x3e4ce247, 0xc0000000, 0x3fedddd4, 0x265a9be4, 0xbe5bb77a,
  0x40000000, 0x3fedce97, 0x0ecac52f, 0x3e4a7cb1, 0xe0000000, 0x3fedbf5e,
  0x124cb3b8, 0x3e257024, 0x80000000, 0x3fedb02b, 0xe6d4febe, 0xbe2033ee,
  0x20000000, 0x3feda0fd, 0x39cca00e, 0xbe3ddabc, 0xc0000000, 0x3fed91d3,
  0xef8a552a, 0xbe543390, 0x40000000, 0x3fed82af, 0xb8e85204, 0x3e513850,
  0xe0000000, 0x3fed738f, 0x3d59fe08, 0xbe5db728, 0x40000000, 0x3fed6475,
  0x3aa7ead1, 0x3e58804b, 0xc0000000, 0x3fed555f, 0xf8a35ba9, 0xbe5298b0,
  0x00000000, 0x3fed464f, 0x9a88dd15, 0x3e5a8cdb, 0x40000000, 0x3fed3743,
  0xb0b0a190, 0x3e598635, 0x80000000, 0x3fed283c, 0xe2113295, 0xbe5c1119,
  0x80000000, 0x3fed193a, 0xafbf1728, 0xbe492e9c, 0x60000000, 0x3fed0a3d,
  0xe4a4ccf3, 0x3e19b90e, 0x20000000, 0x3fecfb45, 0xba3cbeb8, 0x3e406b50,
  0xc0000000, 0x3fecec51, 0x110f7ddd, 0x3e0d6806, 0x40000000, 0x3fecdd63,
  0x7dd7d508, 0xbe5a8943, 0x80000000, 0x3fecce79, 0x9b60f271, 0xbe50676a,
  0x80000000, 0x3fecbf94, 0x0b9ad660, 0x3e59174f, 0x60000000, 0x3fecb0b4,
  0x00823d9c, 0x3e5bbf72, 0x20000000, 0x3feca1d9, 0x38a6ec89, 0xbe4d38f9,
  0x80000000, 0x3fec9302, 0x3a0b7d8e, 0x3e53dbfd, 0xc0000000, 0x3fec8430,
  0xc6826b34, 0xbe27c5c9, 0xc0000000, 0x3fec7563, 0x0c706381, 0xbe593653,
  0x60000000, 0x3fec669b, 0x7df34ec7, 0x3e461ab5, 0xe0000000, 0x3fec57d7,
  0x40e5e7e8, 0xbe5c3dae, 0x00000000, 0x3fec4919, 0x5602770f, 0xbe55219d,
  0xc0000000, 0x3fec3a5e, 0xec7911eb, 0x3e5a5d25, 0x60000000, 0x3fec2ba9,
  0xb39ea225, 0xbe53c00b, 0x80000000, 0x3fec1cf8, 0x967a212e, 0x3e5a8ddf,
  0x60000000, 0x3fec0e4c, 0x580798bd, 0x3e5f53ab, 0x00000000, 0x3febffa5,
  0xb8282df6, 0xbe46b874, 0x20000000, 0x3febf102, 0xe33a6729, 0x3e54963f,
  0x00000000, 0x3febe264, 0x3b53e88a, 0xbe3adce1, 0x60000000, 0x3febd3ca,
  0xc2585084, 0x3e5cde9f, 0x80000000, 0x3febc535, 0xa335c5ee, 0xbe39fd9c,
  0x20000000, 0x3febb6a5, 0x7325b04d, 0x3e42ba15, 0x60000000, 0x3feba819,
  0x1564540f, 0x3e3a9f35, 0x40000000, 0x3feb9992, 0x83fff592, 0xbe5465ce,
  0xa0000000, 0x3feb8b0f, 0xb9da63d3, 0xbe4b1a0a, 0x80000000, 0x3feb7c91,
  0x6d6f1ea4, 0x3e557657, 0x00000000, 0x3feb6e18, 0x5e80a1bf, 0x3e4ddbb6,
  0x00000000, 0x3feb5fa3, 0x1c9eacb5, 0x3e592877, 0xa0000000, 0x3feb5132,
  0x6d40beb3, 0xbe51858c, 0xa0000000, 0x3feb42c6, 0xd740c67b, 0x3e427ad2,
  0x40000000, 0x3feb345f, 0xa3e0ccee, 0xbe5c2fc4, 0x40000000, 0x3feb25fc,
  0x8e752b50, 0xbe3da3c2, 0xc0000000, 0x3feb179d, 0xa892e7de, 0x3e1fb481,
  0xc0000000, 0x3feb0943, 0x21ed71e9, 0xbe365206, 0x20000000, 0x3feafaee,
  0x0e1380a3, 0x3e5c5b7b, 0x20000000, 0x3feaec9d, 0x3c3d640e, 0xbe5dbbd0,
  0x60000000, 0x3feade50, 0x8f97a715, 0x3e3a8ec5, 0x20000000, 0x3fead008,
  0x23ab2839, 0x3e2fe98a, 0x40000000, 0x3feac1c4, 0xf4bbd50f, 0x3e54d8f6,
  0xe0000000, 0x3feab384, 0x14757c4d, 0xbe48774c, 0xc0000000, 0x3feaa549,
  0x7c7b0eea, 0x3e5b51bb, 0x20000000, 0x3fea9713, 0xf56f7013, 0x3e386200,
  0xe0000000, 0x3fea88e0, 0xbe428ebe, 0xbe514af5, 0xe0000000, 0x3fea7ab2,
  0x8d0e4496, 0x3e4f9165, 0x60000000, 0x3fea6c89, 0xdbacc5d5, 0xbe5c063b,
  0x20000000, 0x3fea5e64, 0x3f19d970, 0xbe5a0c8c, 0x20000000, 0x3fea5043,
  0x09ea3e6b, 0x3e5065dc, 0x80000000, 0x3fea4226, 0x78df246c, 0x3e5e05f6,
  0x40000000, 0x3fea340e, 0x4057d4a0, 0x3e431b2b, 0x40000000, 0x3fea25fa,
  0x82867bb5, 0x3e4b76be, 0xa0000000, 0x3fea17ea, 0x9436f40a, 0xbe5aad39,
  0x20000000, 0x3fea09df, 0x4b5253b3, 0x3e46380b, 0x00000000, 0x3fe9fbd8,
  0x8fc52466, 0xbe386f9b, 0x20000000, 0x3fe9edd5, 0x22d3f344, 0xbe538347,
  0x60000000, 0x3fe9dfd6, 0x1ac33522, 0x3e5dbc53, 0x00000000, 0x3fe9d1dc,
  0xeabdff1d, 0x3e40fc0c, 0xe0000000, 0x3fe9c3e5, 0xafd30e73, 0xbe585e63,
  0xe0000000, 0x3fe9b5f3, 0xa52f226a, 0xbe43e8f9, 0x20000000, 0x3fe9a806,
  0xecb8698d, 0xbe515b36, 0x80000000, 0x3fe99a1c, 0xf2b4e89d, 0x3e48b62b,
  0x20000000, 0x3fe98c37, 0x7c9a88fb, 0x3e44414c, 0x00000000, 0x3fe97e56,
  0xda015741, 0xbe5d13ba, 0xe0000000, 0x3fe97078, 0x5fdace06, 0x3e51b947,
  0x00000000, 0x3fe962a0, 0x956ca094, 0x3e518785, 0x40000000, 0x3fe954cb,
  0x01164c1d, 0x3e5d5b57, 0xc0000000, 0x3fe946fa, 0xe63b3767, 0xbe4f84e7,
  0x40000000, 0x3fe9392e, 0xe57cc2a9, 0x3e34eda3, 0xe0000000, 0x3fe92b65,
  0x8c75b544, 0x3e5766a0, 0xc0000000, 0x3fe91da1, 0x37d1d087, 0xbe5e2ab1,
  0x80000000, 0x3fe90fe1, 0xa953dc20, 0x3e5fa1f3, 0x80000000, 0x3fe90225,
  0xdbd3f369, 0x3e47d6db, 0xa0000000, 0x3fe8f46d, 0x1c9be989, 0xbe5e2b0a,
  0xa0000000, 0x3fe8e6b9, 0x3c93d76a, 0x3e5c8618, 0xe0000000, 0x3fe8d909,
  0x2182fc9a, 0xbe41aa9e, 0x20000000, 0x3fe8cb5e, 0xe6b3539d, 0xbe530d19,
  0x60000000, 0x3fe8bdb6, 0x49e58cc3, 0xbe3bb374, 0xa0000000, 0x3fe8b012,
  0xa7cfeb8f, 0x3e56c412, 0x00000000, 0x3fe8a273, 0x8d52bc19, 0x3e1429b8,
  0x60000000, 0x3fe894d7, 0x4dc32c6c, 0xbe48604c, 0xc0000000, 0x3fe8873f,
  0x0c868e56, 0xbe564ee5, 0x00000000, 0x3fe879ac, 0x56aee828, 0x3e5e2fd8,
  0x60000000, 0x3fe86c1c, 0x7ceab8ec, 0x3e493365, 0xc0000000, 0x3fe85e90,
  0x78d4dadc, 0xbe4f7f25, 0x00000000, 0x3fe85109, 0x0ccd8280, 0x3e31e7a2,
  0x40000000, 0x3fe84385, 0x34ba4e15, 0x3e328077, 0x80000000, 0x3fe83605,
  0xa670975a, 0xbe53eee5, 0xa0000000, 0x3fe82889, 0xf61b77b2, 0xbe43a20a,
  0xa0000000, 0x3fe81b11, 0x13e6643b, 0x3e5e5fe5, 0xc0000000, 0x3fe80d9d,
  0x82cc94e8, 0xbe5ff1f9, 0xa0000000, 0x3fe8002d, 0x8a0c9c5d, 0xbe42b0e7,
  0x60000000, 0x3fe7f2c1, 0x22a16f01, 0x3e5d9ea0, 0x20000000, 0x3fe7e559,
  0xc38cd451, 0x3e506963, 0xc0000000, 0x3fe7d7f4, 0x9902bc71, 0x3e4503d7,
  0x40000000, 0x3fe7ca94, 0xdef2a3c0, 0x3e3d98ed, 0xa0000000, 0x3fe7bd37,
  0xed49abb0, 0x3e24c1ff, 0xe0000000, 0x3fe7afde, 0xe3b0be70, 0xbe40c467,
  0x00000000, 0x3fe7a28a, 0xaf9f193c, 0xbe5dff6c, 0xe0000000, 0x3fe79538,
  0xb74cf6b6, 0xbe258ed0, 0xa0000000, 0x3fe787eb, 0x1d9127c7, 0x3e345fb0,
  0x40000000, 0x3fe77aa2, 0x1028c21d, 0xbe4619bd, 0xa0000000, 0x3fe76d5c,
  0x7cb0b5e4, 0x3e40f1a2, 0xe0000000, 0x3fe7601a, 0x2b1bc4ad, 0xbe32e8bb,
  0xe0000000, 0x3fe752dc, 0x6839f64e, 0x3e41f57b, 0xc0000000, 0x3fe745a2,
  0xc4121f7e, 0xbe52c40a, 0x60000000, 0x3fe7386c, 0xd6852d72, 0xbe5c4e6b,
  0xc0000000, 0x3fe72b39, 0x91d690f7, 0xbe57f88f, 0xe0000000, 0x3fe71e0a,
  0x627a2159, 0xbe4425d5, 0xc0000000, 0x3fe710df, 0x50a54033, 0x3e422b7e,
  0x60000000, 0x3fe703b8, 0x3b0b5f91, 0x3e5d3857, 0xe0000000, 0x3fe6f694,
  0x84d628a2, 0xbe51f090, 0x00000000, 0x3fe6e975, 0x306d8894, 0xbe414d83,
  0xe0000000, 0x3fe6dc58, 0x30bf24aa, 0xbe4650ca, 0x80000000, 0x3fe6cf40,
  0xd4628d69, 0xbe5db007, 0xc0000000, 0x3fe6c22b, 0xa2aae57b, 0xbe31d279,
  0xc0000000, 0x3fe6b51a, 0x860edf7e, 0xbe2d4c4a, 0x80000000, 0x3fe6a80d,
  0xf3559341, 0xbe5f7e98, 0xe0000000, 0x3fe69b03, 0xa885899e, 0xbe5c2011,
  0xe0000000, 0x3fe68dfd, 0x2bdc6d37, 0x3e224a82, 0xa0000000, 0x3fe680fb,
  0xc12ad1b9, 0xbe40cf56, 0x00000000, 0x3fe673fd, 0x1bcdf659, 0xbdf52f2d,
  0x00000000, 0x3fe66702, 0x5df10408, 0x3e5663e0, 0xc0000000, 0x3fe65a0a,
  0xa4070568, 0xbe40b12f, 0x00000000, 0x3fe64d17, 0x71c54c47, 0x3e5f5e8b,
  0x00000000, 0x3fe64027, 0xbd4b7e83, 0x3e42ead6, 0xa0000000, 0x3fe6333a,
  0x61598bd2, 0xbe4c48d4, 0xc0000000, 0x3fe62651, 0x6f538d61, 0x3e548401,
  0xa0000000, 0x3fe6196c, 0x14344120, 0xbe529af6, 0x00000000, 0x3fe60c8b,
  0x5982c587, 0xbe3e1e4f, 0x00000000, 0x3fe5ffad, 0xfe51d4ea, 0xbe4c897a,
  0x80000000, 0x3fe5f2d2, 0xfd46ebe1, 0x3e552e00, 0xa0000000, 0x3fe5e5fb,
  0xa4695699, 0x3e5ed471, 0x60000000, 0x3fe5d928, 0x80d118ae, 0x3e456b61,
  0xa0000000, 0x3fe5cc58, 0x304c330b, 0x3e54dc29, 0x80000000, 0x3fe5bf8c,
  0x0af2dedf, 0xbe3aa9bd, 0xe0000000, 0x3fe5b2c3, 0x15fc9258, 0xbe479a37,
  0xc0000000, 0x3fe5a5fe, 0x9292c7ea, 0x3e188650, 0x20000000, 0x3fe5993d,
  0x33b4d380, 0x3e5d6d93, 0x20000000, 0x3fe58c7f, 0x02fd16c7, 0x3e2fe961,
  0xa0000000, 0x3fe57fc4, 0x4a05edb6, 0xbe4d55b4, 0xa0000000, 0x3fe5730d,
  0x3d443abb, 0xbe5e6954, 0x00000000, 0x3fe5665a, 0x024acfea, 0x3e50e61b,
  0x00000000, 0x3fe559aa, 0xcc9edd09, 0xbe325403, 0x60000000, 0x3fe54cfd,
  0x1fe26950, 0x3e5d500e, 0x60000000, 0x3fe54054, 0x6c5ae164, 0xbe4a79b4,
  0xc0000000, 0x3fe533ae, 0x154b0287, 0xbe401571, 0xa0000000, 0x3fe5270c,
  0x0673f401, 0xbe56e56b, 0xe0000000, 0x3fe51a6d, 0x751b639c, 0x3e235269,
  0xa0000000, 0x3fe50dd2, 0x7c7b2bed, 0x3ddec887, 0xc0000000, 0x3fe5013a,
  0xafab4e17, 0x3e5e7575, 0x60000000, 0x3fe4f4a6, 0x2e308668, 0x3e59aed6,
  0x80000000, 0x3fe4e815, 0xf33e2a76, 0xbe51f184, 0xe0000000, 0x3fe4db87,
  0x839f3e3e, 0x3e57db01, 0xc0000000, 0x3fe4cefd, 0xa9eda7bb, 0x3e535e0f,
  0x00000000, 0x3fe4c277, 0x2a8f66a5, 0x3e5ce451, 0xc0000000, 0x3fe4b5f3,
  0x05192456, 0xbe4e8518, 0xc0000000, 0x3fe4a973, 0x4aa7cd1d, 0x3e46784a,
  0x40000000, 0x3fe49cf7, 0x8e23025e, 0xbe5749f2, 0x00000000, 0x3fe4907e,
  0x18d30215, 0x3e360f39, 0x20000000, 0x3fe48408, 0x63dcf2f3, 0x3e5e00fe,
  0xc0000000, 0x3fe47795, 0x46182d09, 0xbe5173d9, 0xa0000000, 0x3fe46b26,
  0x8f0e62aa, 0xbe48f281, 0xe0000000, 0x3fe45eba, 0x5775c40c, 0xbe56aad4,
  0x60000000, 0x3fe45252, 0x0fe25f69, 0x3e48bd71, 0x40000000, 0x3fe445ed,
  0xe9989ec5, 0x3e590d97, 0x80000000, 0x3fe4398b, 0xb3d9ffe3, 0x3e479dbc,
  0x20000000, 0x3fe42d2d, 0x388e4d2e, 0xbe5eed80, 0xe0000000, 0x3fe420d1,
  0x6f797c18, 0x3e554b4c, 0x20000000, 0x3fe4147a, 0x31048bb4, 0xbe5b1112,
  0x80000000, 0x3fe40825, 0x2efba4f9, 0x3e48ebc7, 0x40000000, 0x3fe3fbd4,
  0x50201119, 0x3e40b701, 0x40000000, 0x3fe3ef86, 0x0a4db32c, 0x3e551de8,
  0xa0000000, 0x3fe3e33b, 0x0c9c148b, 0xbe50c1f6, 0x20000000, 0x3fe3d6f4,
  0xc9129447, 0x3e533fa0, 0x00000000, 0x3fe3cab0, 0xaae5b5a0, 0xbe22b68e,
  0x20000000, 0x3fe3be6f, 0x02305e8a, 0xbe54fc08, 0x60000000, 0x3fe3b231,
  0x7f908258, 0x3e57dc05, 0x00000000, 0x3fe3a5f7, 0x1a09af78, 0x3e08038b,
  0xe0000000, 0x3fe399bf, 0x490643c1, 0xbe5dbe42, 0xe0000000, 0x3fe38d8b,
  0x5e8ad724, 0xbe3c2b72, 0x20000000, 0x3fe3815b, 0xc67196b6, 0x3e1713cf,
  0xa0000000, 0x3fe3752d, 0x6182e429, 0xbe3ec14c, 0x40000000, 0x3fe36903,
  0xab6eb1ae, 0x3e5a2cc5, 0x40000000, 0x3fe35cdc, 0xfe5dc064, 0xbe5c5878,
  0x40000000, 0x3fe350b8, 0x0ba6b9e4, 0x3e51619b, 0x80000000, 0x3fe34497,
  0x857761aa, 0x3e5fff53, 0x00000000, 0x3fe3387a, 0xf872d68c, 0x3e484f4d,
  0xa0000000, 0x3fe32c5f, 0x087e97c2, 0x3e52842e, 0x80000000, 0x3fe32048,
  0x73d6d0c0, 0xbe503edf, 0x80000000, 0x3fe31434, 0x0c1456a1, 0xbe5f72ad,
  0xa0000000, 0x3fe30823, 0x83a1a4d5, 0xbe5e65cc, 0xe0000000, 0x3fe2fc15,
  0x855a7390, 0xbe506438, 0x40000000, 0x3fe2f00b, 0xa2898287, 0x3e3d22a2,
  0xe0000000, 0x3fe2e403, 0x8b56f66f, 0xbe5aa5fd, 0x80000000, 0x3fe2d7ff,
  0x52db119a, 0x3e3a2e3d, 0x60000000, 0x3fe2cbfe, 0xe2ddd4c0, 0xbe586469,
  0x40000000, 0x3fe2c000, 0x6b01bf10, 0x3e352b9d, 0x40000000, 0x3fe2b405,
  0xb07a1cdf, 0x3e5c5cda, 0x80000000, 0x3fe2a80d, 0xc7b5f868, 0xbe5668b3,
  0xc0000000, 0x3fe29c18, 0x185edf62, 0xbe563d66, 0x00000000, 0x3fe29027,
  0xf729e1cc, 0x3e59a9a0, 0x80000000, 0x3fe28438, 0x6433c727, 0xbe43cc89,
  0x00000000, 0x3fe2784d, 0x41782631, 0xbe30750c, 0xa0000000, 0x3fe26c64,
  0x914911b7, 0xbe58290e, 0x40000000, 0x3fe2607f, 0x3dcc73e1, 0xbe4269cd,
  0x00000000, 0x3fe2549d, 0x2751bf70, 0xbe5a6998, 0xc0000000, 0x3fe248bd,
  0x4248b9fb, 0xbe4ddb00, 0x80000000, 0x3fe23ce1, 0xf35cf82f, 0x3e561b71,
  0x60000000, 0x3fe23108, 0x8e481a2d, 0x3e518fb9, 0x60000000, 0x3fe22532,
  0x5ab96edc, 0xbe5fafc5, 0x40000000, 0x3fe2195f, 0x80943911, 0xbe07f819,
  0x40000000, 0x3fe20d8f, 0x386f2d6c, 0xbe54ba8b, 0x40000000, 0x3fe201c2,
  0xf29664ac, 0xbe5eb815, 0x20000000, 0x3fe1f5f8, 0x64f03390, 0x3e5e320c,
  0x20000000, 0x3fe1ea31, 0x747ff696, 0x3e5ef0a5, 0x40000000, 0x3fe1de6d,
  0x3e9ceb51, 0xbe5f8d27, 0x20000000, 0x3fe1d2ac, 0x4ae0b55e, 0x3e5faa21,
  0x20000000, 0x3fe1c6ee, 0x28569a5e, 0x3e598a4f, 0x20000000, 0x3fe1bb33,
  0x54b33e07, 0x3e46130a, 0x20000000, 0x3fe1af7b, 0x024f1078, 0xbe4dbf93,
  0x00000000, 0x3fe1a3c6, 0xb0783bfa, 0x3e419248, 0xe0000000, 0x3fe19813,
  0x2f02b836, 0x3e4e02b7, 0xc0000000, 0x3fe18c64, 0x28dec9d4, 0x3e09064f,
  0x80000000, 0x3fe180b8, 0x45cbf406, 0x3e5b1f46, 0x40000000, 0x3fe1750f,
  0x03d9964c, 0x3e5b0a79, 0x00000000, 0x3fe16969, 0x8b5b882b, 0xbe238086,
  0xa0000000, 0x3fe15dc5, 0x73bad6f8, 0xbdf1fca4, 0x20000000, 0x3fe15225,
  0x5385769c, 0x3e5e8d76, 0xa0000000, 0x3fe14687, 0x1676dc6b, 0x3e571d08,
  0x20000000, 0x3fe13aed, 0xa8c41c7f, 0xbe598a25, 0x60000000, 0x3fe12f55,
  0xc4e1aaf0, 0x3e435277, 0xa0000000, 0x3fe123c0, 0x403638e1, 0xbe21aa7c,
  0xc0000000, 0x3fe1182e, 0x557a092b, 0xbdd0116b, 0xc0000000, 0x3fe10c9f,
  0x7d779f66, 0x3e4a61ba, 0xc0000000, 0x3fe10113, 0x2b09c645, 0xbe5d586e,
  0x20000000, 0x3fe0ea04, 0xea2cad46, 0x3e5aa97c, 0x20000000, 0x3fe0d300,
  0x23190e54, 0x3e50f1a7, 0xa0000000, 0x3fe0bc07, 0x1379a5a6, 0xbe51619d,
  0x60000000, 0x3fe0a51a, 0x926a3d4a, 0x3e5cf019, 0xa0000000, 0x3fe08e38,
  0xa8c24358, 0x3e35241e, 0x20000000, 0x3fe07762, 0x24317e7a, 0x3e512cfa,
  0x00000000, 0x3fe06097, 0xfd9cf274, 0xbe55bef3, 0x00000000, 0x3fe049d7,
  0x3689b49d, 0xbe36d26d, 0x40000000, 0x3fe03322, 0xf72ef6c4, 0xbe54cd08,
  0xa0000000, 0x3fe01c78, 0x23702d2d, 0xbe5900bf, 0x00000000, 0x3fe005da,
  0x3f59c14c, 0x3e57d80b, 0x40000000, 0x3fdfde8d, 0xad67766d, 0xbe57fad4,
  0x40000000, 0x3fdfb17c, 0x644f4ae7, 0x3e1ee43b, 0x40000000, 0x3fdf8481,
  0x903234d2, 0x3e501a86, 0x40000000, 0x3fdf579c, 0xafe9e509, 0xbe267c3e,
  0x00000000, 0x3fdf2acd, 0xb7dfda0b, 0xbe48149b, 0x40000000, 0x3fdefe13,
  0x3b94305e, 0x3e5f4ea7, 0x80000000, 0x3fded16f, 0x5d95da61, 0xbe55c198,
  0x00000000, 0x3fdea4e1, 0x406960c9, 0xbdd99a19, 0x00000000, 0x3fde7868,
  0xd22f3539, 0x3e470c78, 0x80000000, 0x3fde4c04, 0x83eec535, 0xbe3e1232,
  0x40000000, 0x3fde1fb6, 0x3dfbffcb, 0xbe4b7d71, 0x40000000, 0x3fddf37d,
  0x7e1be4e0, 0xbe5b8f8f, 0x40000000, 0x3fddc759, 0x46dae887, 0xbe350458,
  0x80000000, 0x3fdd9b4a, 0xed6ecc49, 0xbe5f0045, 0x80000000, 0x3fdd6f50,
  0x2e9e883c, 0x3e2915da, 0x80000000, 0x3fdd436b, 0xf0bccb32, 0x3e4a68c9,
  0x80000000, 0x3fdd179b, 0x9bbfc779, 0xbe54a26a, 0x00000000, 0x3fdcebe0,
  0x7cea33ab, 0x3e43c6b7, 0x40000000, 0x3fdcc039, 0xe740fd06, 0x3e5526c2,
  0x40000000, 0x3fdc94a7, 0x9eadeb1a, 0xbe396d8d, 0xc0000000, 0x3fdc6929,
  0xf0a8f95a, 0xbe5c0ab2, 0x80000000, 0x3fdc3dc0, 0x6ee2693b, 0x3e0992e6,
  0xc0000000, 0x3fdc126b, 0x5ac6b581, 0xbe2834b6, 0x40000000, 0x3fdbe72b,
  0x8cc226ff, 0x3e3596a6, 0x00000000, 0x3fdbbbff, 0xf92a74bb, 0x3e3c5813,
  0x00000000, 0x3fdb90e7, 0x479664c0, 0xbe50d644, 0x00000000, 0x3fdb65e3,
  0x5004975b, 0xbe55258f, 0x00000000, 0x3fdb3af3, 0xe4b23194, 0xbe588407,
  0xc0000000, 0x3fdb1016, 0xe65d4d0a, 0x3e527c26, 0x80000000, 0x3fdae54e,
  0x814fddd6, 0x3e5962a2, 0x40000000, 0x3fdaba9a, 0xe19d0913, 0xbe562f4e,
  0x80000000, 0x3fda8ff9, 0x43cfd006, 0xbe4cfdeb, 0x40000000, 0x3fda656c,
  0x686f0a4e, 0x3e5e47a8, 0xc0000000, 0x3fda3af2, 0x7200d410, 0x3e5e1199,
  0xc0000000, 0x3fda108c, 0xabd2266e, 0x3e5ee4d1, 0x40000000, 0x3fd9e63a,
  0x396f8f2c, 0x3e4dbffb, 0x00000000, 0x3fd9bbfb, 0xe32b25dd, 0x3e5c3a54,
  0x40000000, 0x3fd991cf, 0x431e4035, 0xbe457925, 0x80000000, 0x3fd967b6,
  0x7bed3dd3, 0x3e40c61d, 0x00000000, 0x3fd93db1, 0xd7449365, 0x3e306419,
  0x80000000, 0x3fd913be, 0x1746e791, 0x3e56fcfc, 0x40000000, 0x3fd8e9df,
  0xf3a9028b, 0xbe5041b9, 0xc0000000, 0x3fd8c012, 0x56840c50, 0xbe26e20a,
  0x40000000, 0x3fd89659, 0x19763102, 0xbe51f466, 0x80000000, 0x3fd86cb2,
  0x7032de7c, 0xbe4d298a, 0x80000000, 0x3fd8431e, 0xdeb39fab, 0xbe4361eb,
  0x40000000, 0x3fd8199d, 0x5d01cbe0, 0xbe5425b3, 0x80000000, 0x3fd7f02e,
  0x3ce99aa9, 0x3e146fa8, 0x80000000, 0x3fd7c6d2, 0xd1a262b9, 0xbe5a1a69,
  0xc0000000, 0x3fd79d88, 0x8606c236, 0x3e423a08, 0x80000000, 0x3fd77451,
  0x8fd1e1b7, 0x3e5a6a63, 0xc0000000, 0x3fd74b2c, 0xe491456a, 0x3e42c1ca,
  0x40000000, 0x3fd7221a, 0x4499a6d7, 0x3e36a69a, 0x00000000, 0x3fd6f91a,
  0x5237df94, 0xbe0f8f02, 0x00000000, 0x3fd6d02c, 0xb6482c6e, 0xbe5abcf7,
  0x00000000, 0x3fd6a750, 0x1919fd61, 0xbe57ade2, 0x00000000, 0x3fd67e86,
  0xaa7a994d, 0xbe3f3fbd, 0x00000000, 0x3fd655ce, 0x67db014c, 0x3e33c550,
  0x00000000, 0x3fd62d28, 0xa82856b7, 0xbe1409d1, 0xc0000000, 0x3fd60493,
  0x1e6a300d, 0x3e55d899, 0x80000000, 0x3fd5dc11, 0x1222bd5c, 0xbe35bfc0,
  0xc0000000, 0x3fd5b3a0, 0x6e8dc2d3, 0x3e5d4d79, 0x00000000, 0x3fd58b42,
  0xe0e4ace6, 0xbe517303, 0x80000000, 0x3fd562f4, 0xb306e0a8, 0x3e5edf0f,
  0xc0000000, 0x3fd53ab8, 0x6574bc54, 0x3e5ee859, 0x80000000, 0x3fd5128e,
  0xea902207, 0x3e5f6188, 0xc0000000, 0x3fd4ea75, 0x9f911d79, 0x3e511735,
  0x80000000, 0x3fd4c26e, 0xf9c77397, 0xbe5b1643, 0x40000000, 0x3fd49a78,
  0x15fc9258, 0x3e479a37, 0x80000000, 0x3fd47293, 0xd5a04dd9, 0xbe426e56,
  0xc0000000, 0x3fd44abf, 0xe04042f5, 0x3e56f7c6, 0x40000000, 0x3fd422fd,
  0x1d8bf2c8, 0x3e5d8810, 0x00000000, 0x3fd3fb4c, 0x88a8ddee, 0xbe311454,
  0xc0000000, 0x3fd3d3ab, 0x3e3b5e47, 0xbe5d1b72, 0x40000000, 0x3fd3ac1c,
  0xc2ab5d59, 0x3e31b02b, 0xc0000000, 0x3fd3849d, 0xd4e34b9e, 0x3e51cb2f,
  0x40000000, 0x3fd35d30, 0x177204fb, 0xbe2b8cd7, 0x80000000, 0x3fd335d3,
  0xfcd38c82, 0xbe4356e1, 0x80000000, 0x3fd30e87, 0x64f54acc, 0xbe4e6224,
  0x00000000, 0x3fd2e74c, 0xaa7975d9, 0x3e5dc0fe, 0x80000000, 0x3fd2c021,
  0x516dab3f, 0xbe50ffa3, 0x40000000, 0x3fd29907, 0x2bfb7313, 0x3e5674a2,
  0xc0000000, 0x3fd271fd, 0x0549fc99, 0x3e385d29, 0xc0000000, 0x3fd24b04,
  0x55b63073, 0xbe500c6d, 0x00000000, 0x3fd2241c, 0x3f91953a, 0x3e389977,
  0xc0000000, 0x3fd1fd43, 0xa1543f71, 0xbe3487ab, 0xc0000000, 0x3fd1d67b,
  0x4ec8867c, 0x3df6a2dc, 0x00000000, 0x3fd1afc4, 0x4328e3bb, 0x3e41d9c0,
  0x80000000, 0x3fd1891c, 0x2e1cda84, 0x3e3bdd87, 0x40000000, 0x3fd16285,
  0x4b5331ae, 0xbe53128e, 0x00000000, 0x3fd13bfe, 0xb9aec164, 0xbe52ac98,
  0xc0000000, 0x3fd11586, 0xd91e1316, 0xbe350630, 0x80000000, 0x3fd0ef1f,
  0x7cacc12c, 0x3e3f5219, 0x40000000, 0x3fd0c8c8, 0xbce277b7, 0x3e3d30c0,
  0x00000000, 0x3fd0a281, 0x2a63447d, 0xbe541377, 0x80000000, 0x3fd07c49,
  0xfac483b5, 0xbe5772ec, 0xc0000000, 0x3fd05621, 0x36b8a570, 0xbe4fd4bd,
  0xc0000000, 0x3fd03009, 0xbae505f7, 0xbe450388, 0x80000000, 0x3fd00a01,
  0x3e35aead, 0xbe5430fc, 0x80000000, 0x3fcfc811, 0x707475ac, 0x3e38806e,
  0x80000000, 0x3fcf7c3f, 0xc91817fc, 0xbe40ccea, 0x80000000, 0x3fcf308c,
  0xae05d5e9, 0xbe4919b8, 0x80000000, 0x3fcee4f8, 0xae6cc9e6, 0xbe530b94,
  0x00000000, 0x3fce9983, 0x1efe3e8e, 0x3e57747e, 0x00000000, 0x3fce4e2d,
  0xda78d9bf, 0xbe59a608, 0x00000000, 0x3fce02f5, 0x8abe2c2e, 0x3e4a35ad,
  0x00000000, 0x3fcdb7dc, 0x1495450d, 0xbe0872cc, 0x80000000, 0x3fcd6ce1,
  0x86ee0ba0, 0xbe4f59a0, 0x00000000, 0x3fcd2205, 0xe81ca888, 0x3e5402c3,
  0x00000000, 0x3fccd747, 0x3b4424b9, 0x3e5dfdc3, 0x80000000, 0x3fcc8ca7,
  0xd305b56c, 0x3e202da6, 0x00000000, 0x3fcc4226, 0x399a6910, 0xbe482a1c,
  0x80000000, 0x3fcbf7c2, 0x747f7938, 0xbe587372, 0x80000000, 0x3fcbad7c,
  0x6fc246a0, 0x3e50d83d, 0x00000000, 0x3fcb6355, 0xee9e9be5, 0xbe5c35bd,
  0x80000000, 0x3fcb194a, 0x8416c0bc, 0x3e546d4f, 0x00000000, 0x3fcacf5e,
  0x49f7f08f, 0x3e56da76, 0x00000000, 0x3fca858f, 0x5dc30de2, 0x3e5f390c,
  0x00000000, 0x3fca3bde, 0x950583b6, 0xbe5e4169, 0x80000000, 0x3fc9f249,
  0x33631553, 0x3e52aeb1, 0x00000000, 0x3fc9a8d3, 0xde8795a6, 0xbe59a504,
  0x00000000, 0x3fc95f79, 0x076bf41e, 0x3e5122fe, 0x80000000, 0x3fc9163c,
  0x2914c8e7, 0x3e3dd064, 0x00000000, 0x3fc8cd1d, 0x3a30eca3, 0xbe21b4aa,
  0x80000000, 0x3fc8841a, 0xb2a96650, 0xbe575444, 0x80000000, 0x3fc83b34,
  0x2376c0cb, 0xbe2a74c7, 0x80000000, 0x3fc7f26b, 0xd8a0b653, 0xbe5181b6,
  0x00000000, 0x3fc7a9bf, 0x32257882, 0xbe4a78b4, 0x00000000, 0x3fc7612f,
  0x1eee8bd9, 0xbe1bfe9d, 0x80000000, 0x3fc718bb, 0x0c603cc4, 0x3e36fdc9,
  0x80000000, 0x3fc6d064, 0x3728b8cf, 0xbe1e542e, 0x80000000, 0x3fc68829,
  0xc79a4067, 0x3e5c380f, 0x00000000, 0x3fc6400b, 0xf69eac69, 0x3e550a84,
  0x80000000, 0x3fc5f808, 0xb7a780a4, 0x3e5d9224, 0x80000000, 0x3fc5b022,
  0xad9dfb1e, 0xbe55242f, 0x00000000, 0x3fc56858, 0x659b18be, 0xbe4bfda3,
  0x80000000, 0x3fc520a9, 0x66ee3631, 0xbe57d769, 0x80000000, 0x3fc4d916,
  0x1ec62819, 0x3e2427f7, 0x80000000, 0x3fc4919f, 0xdec25369, 0xbe435431,
  0x00000000, 0x3fc44a44, 0xa8acfc4b, 0xbe3c62e8, 0x00000000, 0x3fc40304,
  0xcf1d3eab, 0xbdfba29f, 0x80000000, 0x3fc3bbdf, 0x79aba3ea, 0xbdf1b7c8,
  0x80000000, 0x3fc374d6, 0xb8d186da, 0xbe5130cf, 0x80000000, 0x3fc32de8,
  0x9d74f152, 0x3e2285b6, 0x00000000, 0x3fc2e716, 0x50ae7ca9, 0xbe503920,
  0x80000000, 0x3fc2a05e, 0x6caed92e, 0xbe533924, 0x00000000, 0x3fc259c2,
  0x9cb5034e, 0xbe510e31, 0x80000000, 0x3fc21340, 0x12c4d378, 0xbe540b43,
  0x80000000, 0x3fc1ccd9, 0xcc418706, 0x3e59887a, 0x00000000, 0x3fc1868e,
  0x921f4106, 0xbe528e67, 0x80000000, 0x3fc1405c, 0x3969441e, 0x3e5d8051,
  0x00000000, 0x3fc0fa46, 0xd941ef5b, 0x3e5f9079, 0x80000000, 0x3fc0b44a,
  0x5a3e81b2, 0xbe567691, 0x00000000, 0x3fc06e69, 0x9d66afe7, 0xbe4d43fb,
  0x00000000, 0x3fc028a2, 0x0a92a162, 0xbe52f394, 0x00000000, 0x3fbfc5ea,
  0x209897e5, 0x3e529e37, 0x00000000, 0x3fbf3ac5, 0x8458bd7b, 0x3e582831,
  0x00000000, 0x3fbeafd5, 0xb8d8b4b8, 0xbe486b4a, 0x00000000, 0x3fbe2518,
  0xe0a3b7b6, 0x3e5bafd2, 0x00000000, 0x3fbd9a90, 0x2bf2710e, 0x3e383b2b,
  0x00000000, 0x3fbd103c, 0x73eb6ab7, 0xbe56d78d, 0x00000000, 0x3fbc861b,
  0x32ceaff5, 0xbe32dc5a, 0x00000000, 0x3fbbfc2e, 0xbee04cb7, 0xbe4a71a4,
  0x00000000, 0x3fbb7274, 0x35ae9577, 0x3e38142f, 0x00000000, 0x3fbae8ee,
  0xcbaddab4, 0xbe5490f0, 0x00000000, 0x3fba5f9a, 0x95ce1114, 0x3e597c71,
  0x00000000, 0x3fb9d67a, 0x6d7c0f78, 0x3e3abc2d, 0x00000000, 0x3fb94d8d,
  0x2841a782, 0xbe566cbc, 0x00000000, 0x3fb8c4d2, 0x6ed429c6, 0xbe3cfff9,
  0x00000000, 0x3fb83c4a, 0xe4a49fbb, 0xbe552964, 0x00000000, 0x3fb7b3f4,
  0x2193d81e, 0xbe42fa72, 0x00000000, 0x3fb72bd0, 0xdd70c122, 0x3e527a8c,
  0x00000000, 0x3fb6a3df, 0x03108a54, 0xbe450393, 0x00000000, 0x3fb61c1f,
  0x30ff7954, 0x3e565840, 0x00000000, 0x3fb59492, 0xdedd460c, 0xbe5422b5,
  0x00000000, 0x3fb50d36, 0x950f9f45, 0xbe5313f6, 0x00000000, 0x3fb4860b,
  0x582cdcb1, 0x3e506d39, 0x00000000, 0x3fb3ff12, 0x7216d3a6, 0x3e4aa719,
  0x00000000, 0x3fb3784a, 0x57a423fd, 0x3e5a9b9f, 0x00000000, 0x3fb2f1b4,
  0x7a138b41, 0xbe50b418, 0x00000000, 0x3fb26b4e, 0x2fbfd7ea, 0x3e23a53e,
  0x00000000, 0x3fb1e519, 0x18913ccb, 0x3e465fc1, 0x00000000, 0x3fb15f15,
  0x7ea24e21, 0x3e042843, 0x00000000, 0x3fb0d941, 0x7c6d9c77, 0x3e59f61e,
  0x00000000, 0x3fb0539e, 0x114efd44, 0x3e4ccab7, 0x00000000, 0x3faf9c56,
  0x1777f657, 0x3e552f65, 0x00000000, 0x3fae91d2, 0xc317b86a, 0xbe5a61e0,
  0x00000000, 0x3fad87ac, 0xb7664efb, 0xbe41f64e, 0x00000000, 0x3fac7de6,
  0x5d3d03a9, 0x3e0807a0, 0x00000000, 0x3fab7480, 0x743c38eb, 0xbe3726e1,
  0x00000000, 0x3faa6b78, 0x06a253f1, 0x3e5ad636, 0x00000000, 0x3fa962d0,
  0xa35f541b, 0x3e5a187a, 0x00000000, 0x3fa85a88, 0x4b86e446, 0xbe508150,
  0x00000000, 0x3fa7529c, 0x2589cacf, 0x3e52938a, 0x00000000, 0x3fa64b10,
  0xaf6b11f2, 0xbe3454cd, 0x00000000, 0x3fa543e2, 0x97506fef, 0xbe5fdec5,
  0x00000000, 0x3fa43d10, 0xe75f7dd9, 0xbe388dd3, 0x00000000, 0x3fa3369c,
  0xa4139632, 0xbdea5177, 0x00000000, 0x3fa23086, 0x352d6f1e, 0xbe565ad6,
  0x00000000, 0x3fa12acc, 0x77449eb7, 0xbe50d5c7, 0x00000000, 0x3fa0256e,
  0x7478da78, 0x3e404724, 0x00000000, 0x3f9e40dc, 0xf59cef7f, 0xbe539d0a,
  0x00000000, 0x3f9c3790, 0x1511d43c, 0x3e53c2c8, 0x00000000, 0x3f9a2f00,
  0x9b8bff3c, 0xbe43b3e1, 0x00000000, 0x3f982724, 0xad1e22a5, 0x3e46f0bd,
  0x00000000, 0x3f962000, 0x130d9356, 0x3e475ba0, 0x00000000, 0x3f941994,
  0x8f86f883, 0xbe513d0b, 0x00000000, 0x3f9213dc, 0x914d0dc8, 0xbe534335,
  0x00000000, 0x3f900ed8, 0x2d73e5e7, 0xbe22ba75, 0x00000000, 0x3f8c1510,
  0xc5b7d70e, 0x3e599c5d, 0x00000000, 0x3f880de0, 0x8a27857e, 0xbe3d28c8,
  0x00000000, 0x3f840810, 0xda767328, 0x3e531b3d, 0x00000000, 0x3f8003b0,
  0x77bacaf3, 0xbe5f04e3, 0x00000000, 0x3f780150, 0xdf4b0720, 0x3e5a8bff,
  0x00000000, 0x3f6ffc40, 0x34c48e71, 0xbe3fcd99, 0x00000000, 0x3f5ff6c0,
  0x1ad218af, 0xbe4c78a7, 0x00000000, 0x00000000, 0x00000000, 0x80000000,
};

static const uint32_t T_pow_T_exp[1024] __attribute__((aligned(16))) = {
  0x00000000, 0x3ff00000, 0x00000000, 0x3b700000, 0xfa5abcbf, 0x3ff00b1a,
  0xa7609f71, 0xbc84f6b2, 0xa9fb3335, 0x3ff0163d, 0x9ab8cdb7, 0x3c9b6129,
  0x143b0281, 0x3ff02168, 0x0fc54eb6, 0xbc82bf31, 0x3e778061, 0x3ff02c9a,
  0x535b085d, 0xbc719083, 0x2e11bbcc, 0x3ff037d4, 0xeeade11a, 0x3c656811,
  0xe86e7f85, 0x3ff04315, 0x1977c96e, 0xbc90a31c, 0x72f654b1, 0x3ff04e5f,
  0x3aa0d08c, 0x3c84c379, 0xd3158574, 0x3ff059b0, 0xa475b465, 0x3c8d73e2,
  0x0e3c1f89, 0x3ff0650a, 0x5799c397, 0xbc95cb7b, 0x29ddf6de, 0x3ff0706b,
  0xe2b13c27, 0xbc8c91df, 0x2b72a836, 0x3ff07bd4, 0x54458700, 0x3c832334,
  0x18759bc8, 0x3ff08745, 0x4bb284ff, 0x3c6186be, 0xf66607e0, 0x3ff092bd,
  0x800a3fd1, 0xbc968063, 0xcac6f383, 0x3ff09e3e, 0x18316136, 0x3c914878,
  0x9b1f3919, 0x3ff0a9c7, 0x873d1d38, 0x3c85d16c, 0x6cf9890f, 0x3ff0b558,
  0x4adc610b, 0x3c98a62e, 0x45e46c85, 0x3ff0c0f1, 0x06d21cef, 0x3c94f989,
  0x2b7247f7, 0x3ff0cc92, 0x16e24f71, 0x3c901edc, 0x23395dec, 0x3ff0d83b,
  0xe43f316a, 0xbc9bc14d, 0x32d3d1a2, 0x3ff0e3ec, 0x27c57b52, 0x3c403a17,
  0x5fdfa9c5, 0x3ff0efa5, 0xbc54021b, 0xbc949db9, 0xaffed31b, 0x3ff0fb66,
  0xc44ebd7b, 0xbc6b9bed, 0x28d7233e, 0x3ff10730, 0x1692fdd5, 0x3c8d46eb,
  0xd0125b51, 0x3ff11301, 0x39449b3a, 0xbc96c510, 0xab5e2ab6, 0x3ff11edb,
  0xf703fb72, 0xbc9ca454, 0xc06c31cc, 0x3ff12abd, 0xb36ca5c7, 0xbc51b514,
  0x14f204ab, 0x3ff136a8, 0xba48dcf0, 0xbc67108f, 0xaea92de0, 0x3ff1429a,
  0x9af1369e, 0xbc932fbf, 0x934f312e, 0x3ff14e95, 0x39bf44ab, 0xbc8b91e8,
  0xc8a58e51, 0x3ff15a98, 0xb9eeab0a, 0x3c82406a, 0x5471c3c2, 0x3ff166a4,
  0x82ea1a32, 0x3c58f23b, 0x3c7d517b, 0x3ff172b8, 0xb9d78a76, 0xbc819041,
  0x8695bbc0, 0x3ff17ed4, 0xe2ac5a64, 0x3c709e3f, 0x388c8dea, 0x3ff18af9,
  0xd1970f6c, 0xbc911023, 0x58375d2f, 0x3ff19726, 0x85f17e08, 0x3c94aadd,
  0xeb6fcb75, 0x3ff1a35b, 0x7b4968e4, 0x3c8e5b4c, 0xf8138a1c, 0x3ff1af99,
  0xa4b69280, 0x3c97bf85, 0x84045cd4, 0x3ff1bbe0, 0x352ef607, 0xbc995386,
  0x95281c6b, 0x3ff1c82f, 0x8010f8c9, 0x3c900977, 0x3168b9aa, 0x3ff1d487,
  0x00a2643c, 0x3c9e016e, 0x5eb44027, 0x3ff1e0e7, 0x088cb6de, 0xbc96fdd8,
  0x22fcd91d, 0x3ff1ed50, 0x027bb78c, 0xbc91df98, 0x8438ce4d, 0x3ff1f9c1,
  0xa097af5c, 0xbc9bf524, 0x88628cd6, 0x3ff2063b, 0x814a8495, 0x3c8dc775,
  0x3578a819, 0x3ff212be, 0x2cfcaac9, 0x3c93592d, 0x917ddc96, 0x3ff21f49,
  0x9494a5ee, 0x3c82a97e, 0xa27912d1, 0x3ff22bdd, 0x5577d69f, 0x3c8d34fb,
  0x6e756238, 0x3ff2387a, 0xb6c70573, 0x3c99b07e, 0xfb82140a, 0x3ff2451f,
  0x911ca996, 0x3c8acfcc, 0x4fb2a63f, 0x3ff251ce, 0xbef4f4a4, 0x3c8ac155,
  0x711ece75, 0x3ff25e85, 0x4ac31b2c, 0x3c93e1a2, 0x65e27cdd, 0x3ff26b45,
  0x9940e9d9, 0x3c82bd33, 0x341ddf29, 0x3ff2780e, 0x05f9e76c, 0x3c9e067c,
  0xe1f56381, 0x3ff284df, 0x8c3f0d7e, 0xbc9a4c3a, 0x7591bb70, 0x3ff291ba,
  0x28401cbd, 0xbc82cc72, 0xf51fdee1, 0x3ff29e9d, 0xafad1255, 0x3c8612e8,
  0x66d10f13, 0x3ff2ab8a, 0x191690a7, 0xbc995743, 0xd0dad990, 0x3ff2b87f,
  0xd6381aa4, 0xbc410adc, 0x39771b2f, 0x3ff2c57e, 0xa6eb5124, 0xbc950145,
  0xa6e4030b, 0x3ff2d285, 0x54db41d5, 0x3c900247, 0x1f641589, 0x3ff2df96,
  0xfbbce198, 0x3c9d16cf, 0xa93e2f56, 0x3ff2ecaf, 0x45d52383, 0x3c71ca0f,
  0x4abd886b, 0x3ff2f9d2, 0x532bda93, 0xbc653c55, 0x0a31b715, 0x3ff306fe,
  0xd23182e4, 0x3c86f46a, 0xedeeb2fd, 0x3ff31432, 0xf3f3fcd1, 0x3c8959a3,
  0xfc4cd831, 0x3ff32170, 0x8e18047c, 0x3c8a9ce7, 0x3ba8ea32, 0x3ff32eb8,
  0x3cb4f318, 0xbc9c45e8, 0xb26416ff, 0x3ff33c08, 0x843659a6, 0x3c932721,
  0x66e3fa2d, 0x3ff34962, 0x930881a4, 0xbc835a75, 0x5f929ff1, 0x3ff356c5,
  0x5c4e4628, 0xbc8b5cee, 0xa2de883b, 0x3ff36431, 0xa06cb85e, 0xbc8c3144,
  0x373aa9cb, 0x3ff371a7, 0xbf42eae2, 0xbc963aea, 0x231e754a, 0x3ff37f26,
  0x9eceb23c, 0xbc99f5ca, 0x6d05d866, 0x3ff38cae, 0x3c9904bd, 0xbc9e958d,
  0x1b7140ef, 0x3ff39a40, 0xfc8e2934, 0xbc99a9a5, 0x34e59ff7, 0x3ff3a7db,
  0xd661f5e3, 0xbc75e436, 0xbfec6cf4, 0x3ff3b57f, 0xe26fff18, 0x3c954c66,
  0xc313a8e5, 0x3ff3c32d, 0x375d29c3, 0xbc9efff8, 0x44ede173, 0x3ff3d0e5,
  0x8c284c71, 0x3c7fe8d0, 0x4c123422, 0x3ff3dea6, 0x11f09ebc, 0x3c8ada09,
  0xdf1c5175, 0x3ff3ec70, 0x7b8c9bca, 0xbc8af663, 0x04ac801c, 0x3ff3fa45,
  0xf956f9f3, 0xbc97d023, 0xc367a024, 0x3ff40822, 0xb6f4d048, 0x3c8bddf8,
  0x21f72e2a, 0x3ff4160a, 0x1c309278, 0xbc5ef369, 0x2709468a, 0x3ff423fb,
  0xc0b314dd, 0xbc98462d, 0xd950a897, 0x3ff431f5, 0xe35f7999, 0xbc81c7dd,
  0x3f84b9d4, 0x3ff43ffa, 0x9704c003, 0x3c8880be, 0x6061892d, 0x3ff44e08,
  0x04ef80d0, 0x3c489b7a, 0x42a7d232, 0x3ff45c20, 0x82fb1f8e, 0xbc686419,
  0xed1d0057, 0x3ff46a41, 0xd1648a76, 0x3c9c944b, 0x668b3237, 0x3ff4786d,
  0xed445733, 0xbc9c20f0, 0xb5c13cd0, 0x3ff486a2, 0xb69062f0, 0x3c73c1a3,
  0xe192aed2, 0x3ff494e1, 0x5e499ea0, 0xbc83b289, 0xf0d7d3de, 0x3ff4a32a,
  0xf3d1be56, 0x3c99cb62, 0xea6db7d7, 0x3ff4b17d, 0x7f2897f0, 0xbc8125b8,
  0xd5362a27, 0x3ff4bfda, 0xafec42e2, 0x3c7d4397, 0xb817c114, 0x3ff4ce41,
  0x690abd5d, 0x3c905e29, 0x99fddd0d, 0x3ff4dcb2, 0xbc6a7833, 0x3c98ecdb,
  0x81d8abff, 0x3ff4eb2d, 0x2e5d7a52, 0xbc95257d, 0x769d2ca7, 0x3ff4f9b2,
  0xd25957e3, 0xbc94b309, 0x7f4531ee, 0x3ff50841, 0x49b7465f, 0x3c7a249b,
  0xa2cf6642, 0x3ff516da, 0x69bd93ef, 0xbc8f7685, 0xe83f4eef, 0x3ff5257d,
  0x43efef71, 0xbc7c998d, 0x569d4f82, 0x3ff5342b, 0x1db13cad, 0xbc807abe,
  0xf4f6ad27, 0x3ff542e2, 0x192d5f7e, 0x3c87926d, 0xca5d920f, 0x3ff551a4,
  0xefede59b, 0xbc8d689c, 0xdde910d2, 0x3ff56070, 0x168eebf0, 0xbc90fb6e,
  0x36b527da, 0x3ff56f47, 0x011d93ad, 0x3c99bb2c, 0xdbe2c4cf, 0x3ff57e27,
  0x8a57b9c4, 0xbc90b98c, 0xd497c7fd, 0x3ff58d12, 0x5b9a1de8, 0x3c8295e1,
  0x27ff07cc, 0x3ff59c08, 0xe467e60f, 0xbc97e2ce, 0xdd485429, 0x3ff5ab07,
  0x054647ad, 0x3c96324c, 0xfba87a03, 0x3ff5ba11, 0x4c233e1a, 0xbc9b77a1,
  0x8a5946b7, 0x3ff5c926, 0x816986a2, 0x3c3c4b1b, 0x90998b93, 0x3ff5d845,
  0xa8b45643, 0xbc9cd6a7, 0x15ad2148, 0x3ff5e76f, 0x3080e65e, 0x3c9ba6f9,
  0x20dceb71, 0x3ff5f6a3, 0xe3cdcf92, 0xbc89eadd, 0xb976dc09, 0x3ff605e1,
  0x9b56de47, 0xbc93e242, 0xe6cdf6f4, 0x3ff6152a, 0x4ab84c27, 0x3c9e4b3e,
  0xb03a5585, 0x3ff6247e, 0x7e40b497, 0xbc9383c1, 0x1d1929fd, 0x3ff633dd,
  0xbeb964e5, 0x3c984710, 0x34ccc320, 0x3ff64346, 0x759d8933, 0xbc8c483c,
  0xfebc8fb7, 0x3ff652b9, 0xc9a73e09, 0xbc9ae3d5, 0x82552225, 0x3ff66238,
  0x87591c34, 0xbc9bb609, 0xc70833f6, 0x3ff671c1, 0x586c6134, 0xbc8e8732,
  0xd44ca973, 0x3ff68155, 0x44f73e65, 0x3c6038ae, 0xb19e9538, 0x3ff690f4,
  0x9aeb445d, 0x3c8804bd, 0x667f3bcd, 0x3ff6a09e, 0x13b26456, 0xbc9bdd34,
  0xfa75173e, 0x3ff6b052, 0x2c9a9d0e, 0x3c7a38f5, 0x750bdabf, 0x3ff6c012,
  0x67ff0b0d, 0xbc728956, 0xddd47645, 0x3ff6cfdc, 0xb6f17309, 0x3c9c7aa9,
  0x3c651a2f, 0x3ff6dfb2, 0x683c88ab, 0xbc6bbe3a, 0x98593ae5, 0x3ff6ef92,
  0x9e1ac8b2, 0xbc90b974, 0xf9519484, 0x3ff6ff7d, 0x25860ef6, 0xbc883c0f,
  0x66f42e87, 0x3ff70f74, 0xd45aa65f, 0x3c59d644, 0xe8ec5f74, 0x3ff71f75,
  0x86887a99, 0xbc816e47, 0x86ead08a, 0x3ff72f82, 0x2cd62c72, 0xbc920aa0,
  0x48a58174, 0x3ff73f9a, 0x6c65d53c, 0xbc90a8d9, 0x35d7cbfd, 0x3ff74fbd,
  0x618a6e1c, 0x3c9047fd, 0x564267c9, 0x3ff75feb, 0x57316dd3, 0xbc902459,
  0xb1ab6e09, 0x3ff77024, 0x169147f8, 0x3c9b7877, 0x4fde5d3f, 0x3ff78069,
  0x0a02162d, 0x3c9866b8, 0x38ac1cf6, 0x3ff790b9, 0x62aadd3e, 0x3c9349a8,
  0x73eb0187, 0x3ff7a114, 0xee04992f, 0xbc841577, 0x0976cfdb, 0x3ff7b17b,
  0x8468dc88, 0xbc9bebb5, 0x0130c132, 0x3ff7c1ed, 0xd1164dd6, 0x3c9f124c,
  0x62ff86f0, 0x3ff7d26a, 0xfb72b8b4, 0x3c91bddb, 0x36cf4e62, 0x3ff7e2f3,
  0xba15797e, 0x3c705d02, 0x8491c491, 0x3ff7f387, 0xcf9311ae, 0xbc807f11,
  0x543e1a12, 0x3ff80427, 0x626d972b, 0xbc927c86, 0xadd106d9, 0x3ff814d2,
  0x0d151d4d, 0x3c946437, 0x994cce13, 0x3ff82589, 0xd41532d8, 0xbc9d4c1d,
  0x1eb941f7, 0x3ff8364c, 0x31df2bd5, 0x3c999b9a, 0x4623c7ad, 0x3ff8471a,
  0xa341cdfb, 0xbc88d684, 0x179f5b21, 0x3ff857f4, 0xf8b216d0, 0xbc5ba748,
  0x9b4492ed, 0x3ff868d9, 0x9bd4f6ba, 0xbc9fc6f8, 0xd931a436, 0x3ff879ca,
  0xd2db47bd, 0x3c85d2d7, 0xd98a6699, 0x3ff88ac7, 0xf37cb53a, 0x3c9994c2,
  0xa478580f, 0x3ff89bd0, 0x4475202a, 0x3c9d5395, 0x422aa0db, 0x3ff8ace5,
  0x56864b27, 0x3c96e9f1, 0xbad61778, 0x3ff8be05, 0xfc43446e, 0x3c9ecb5e,
  0x16b5448c, 0x3ff8cf32, 0x32e9e3aa, 0xbc70d55e, 0x5e0866d9, 0x3ff8e06a,
  0x6fc9b2e6, 0xbc97114a, 0x99157736, 0x3ff8f1ae, 0xa2e3976c, 0x3c85cc13,
  0xd0282c8a, 0x3ff902fe, 0x85fe3fd2, 0x3c9592ca, 0x0b91ffc6, 0x3ff9145b,
  0x2e582524, 0xbc9dd679, 0x53aa2fe2, 0x3ff925c3, 0xa639db7f, 0xbc83455f,
  0xb0cdc5e5, 0x3ff93737, 0x81b57ebc, 0xbc675fc7, 0x2b5f98e5, 0x3ff948b8,
  0x797d2d99, 0xbc8dc3d6, 0xcbc8520f, 0x3ff95a44, 0x96a5f039, 0xbc764b7c,
  0x9a7670b3, 0x3ff96bdd, 0x7f19c896, 0xbc5ba596, 0x9fde4e50, 0x3ff97d82,
  0x7c1b85d1, 0xbc9d185b, 0xe47a22a2, 0x3ff98f33, 0xa24c78ec, 0x3c7cabda,
  0x70ca07ba, 0x3ff9a0f1, 0x91cee632, 0xbc9173bd, 0x4d53fe0d, 0x3ff9b2bb,
  0x4df6d518, 0xbc9dd84e, 0x82a3f090, 0x3ff9c491, 0xb071f2be, 0x3c7c7c46,
  0x194bb8d5, 0x3ff9d674, 0xa3dd8233, 0xbc9516be, 0x19e32323, 0x3ff9e863,
  0x78e64c6e, 0x3c7824ca, 0x8d07f29e, 0x3ff9fa5e, 0xaaf1face, 0xbc84a9ce,
  0x7b5de565, 0x3ffa0c66, 0x5d1cd533, 0xbc935949, 0xed8eb8bb, 0x3ffa1e7a,
  0xee8be70e, 0x3c9c6618, 0xec4a2d33, 0x3ffa309b, 0x7ddc36ab, 0x3c96305c,
  0x80460ad8, 0x3ffa42c9, 0x589fb120, 0xbc9aa780, 0xb23e255d, 0x3ffa5503,
  0xdb8d41e1, 0xbc9d2f6e, 0x8af46052, 0x3ffa674a, 0x30670366, 0x3c650f56,
  0x1330b358, 0x3ffa799e, 0xcac563c7, 0x3c9bcb7e, 0x53c12e59, 0x3ffa8bfe,
  0xb2ba15a9, 0xbc94f867, 0x5579fdbf, 0x3ffa9e6b, 0x0ef7fd31, 0x3c90fac9,
  0x21356eba, 0x3ffab0e5, 0xdae94545, 0x3c889c31, 0xbfd3f37a, 0x3ffac36b,
  0xcae76cd0, 0xbc8f9234, 0x3a3c2774, 0x3ffad5ff, 0xb6b1b8e5, 0x3c97ef3b,
  0x995ad3ad, 0x3ffae89f, 0x345dcc81, 0x3c97a1cd, 0xe622f2ff, 0x3ffafb4c,
  0x0f315ecd, 0xbc94b2fc, 0x298db666, 0x3ffb0e07, 0x4c80e425, 0xbc9bdef5,
  0x6c9a8952, 0x3ffb20ce, 0x4a0756cc, 0x3c94dd02, 0xb84f15fb, 0x3ffb33a2,
  0x3084d708, 0xbc62805e, 0x15b749b1, 0x3ffb4684, 0xe9df7c90, 0xbc7f763d,
  0x8de5593a, 0x3ffb5972, 0xbbba6de3, 0xbc9c71df, 0x29f1c52a, 0x3ffb6c6e,
  0x52883f6e, 0x3c92a8f3, 0xf2fb5e47, 0x3ffb7f76, 0x7e54ac3b, 0xbc75584f,
  0xf22749e4, 0x3ffb928c, 0x54cb65c6, 0xbc9b7216, 0x30a1064a, 0x3ffba5b0,
  0x0e54292e, 0xbc9efcd3, 0xb79a6f1f, 0x3ffbb8e0, 0xc9696205, 0xbc3f52d1,
  0x904bc1d2, 0x3ffbcc1e, 0x7a2d9e84, 0x3c823dd0, 0xc3f3a207, 0x3ffbdf69,
  0x60ea5b53, 0xbc3c2623, 0x5bd71e09, 0x3ffbf2c2, 0x3f6b9c73, 0xbc9efdca,
  0x6141b33d, 0x3ffc0628, 0xa1fbca34, 0xbc8d8a5a, 0xdd85529c, 0x3ffc199b,
  0x895048dd, 0x3c811065, 0xd9fa652c, 0x3ffc2d1c, 0x17c8a5d7, 0xbc96e516,
  0x5fffd07a, 0x3ffc40ab, 0xe083c60a, 0x3c9b4537, 0x78fafb22, 0x3ffc5447,
  0x2493b5af, 0x3c912f07, 0x2e57d14b, 0x3ffc67f1, 0xff483cad, 0x3c92884d,
  0x8988c933, 0x3ffc7ba8, 0xbe255559, 0xbc8e76bb, 0x9406e7b5, 0x3ffc8f6d,
  0x48805c44, 0x3c71acbc, 0x5751c4db, 0x3ffca340, 0xd10d08f5, 0xbc87f2be,
  0xdcef9069, 0x3ffcb720, 0xd1e949db, 0x3c7503cb, 0x2e6d1675, 0x3ffccb0f,
  0x86009092, 0xbc7d220f, 0x555dc3fa, 0x3ffcdf0b, 0x53829d72, 0xbc8dd83b,
  0x5b5bab74, 0x3ffcf315, 0xb86dff57, 0xbc9a08e9, 0x4a07897c, 0x3ffd072d,
  0x43797a9c, 0xbc9cbc37, 0x2b08c968, 0x3ffd1b53, 0x219a36ee, 0x3c955636,
  0x080d89f2, 0x3ffd2f87, 0x719d8578, 0xbc9d487b, 0xeacaa1d6, 0x3ffd43c8,
  0xbf5a1614, 0x3c93db53, 0xdcfba487, 0x3ffd5818, 0xd75b3707, 0x3c82ed02,
  0xe862e6d3, 0x3ffd6c76, 0x4a8165a0, 0x3c5fe87a, 0x16c98398, 0x3ffd80e3,
  0x8beddfe8, 0xbc911ec1, 0x71ff6075, 0x3ffd955d, 0xbb9af6be, 0x3c9a052d,
  0x03db3285, 0x3ffda9e6, 0x696db532, 0x3c9c2300, 0xd63a8315, 0x3ffdbe7c,
  0x926b8be4, 0xbc9b76f1, 0xf301b460, 0x3ffdd321, 0x78f018c3, 0x3c92da57,
  0x641c0658, 0x3ffde7d5, 0x8e79ba8f, 0xbc9ca552, 0x337b9b5f, 0x3ffdfc97,
  0x4f184b5c, 0xbc91a5cd, 0x6b197d17, 0x3ffe1167, 0xbd5c7f44, 0xbc72b529,
  0x14f5a129, 0x3ffe2646, 0x817a1496, 0xbc97b627, 0x3b16ee12, 0x3ffe3b33,
  0x31fdc68b, 0xbc99f4a4, 0xe78b3ff6, 0x3ffe502e, 0x80a9cc8f, 0x3c839e89,
  0x24676d76, 0x3ffe6539, 0x7522b735, 0xbc863ff8, 0xfbc74c83, 0x3ffe7a51,
  0xca0c8de2, 0x3c92d522, 0x77cdb740, 0x3ffe8f79, 0x80b054b1, 0xbc910894,
  0xa2a490da, 0x3ffea4af, 0x179c2893, 0xbc9e9c23, 0x867cca6e, 0x3ffeb9f4,
  0x2293e4f2, 0x3c94832f, 0x2d8e67f1, 0x3ffecf48, 0xb411ad8c, 0xbc9c93f3,
  0xa2188510, 0x3ffee4aa, 0xa487568d, 0x3c91c68d, 0xee615a27, 0x3ffefa1b,
  0x86a4b6b0, 0x3c9dc7f4, 0x1cb6412a, 0x3fff0f9c, 0x65181d45, 0xbc932200,
  0x376bba97, 0x3fff252b, 0xbf0d8e43, 0x3c93a1a5, 0x48dd7274, 0x3fff3ac9,
  0x3ed837de, 0xbc795a5a, 0x5b6e4540, 0x3fff5076, 0x2dd8a18b, 0x3c99d3e1,
  0x798844f8, 0x3fff6632, 0x3539343e, 0x3c9fa37b, 0xad9cbe14, 0x3fff7bfd,
  0xd006350a, 0xbc9dbb12, 0x02243c89, 0x3fff91d8, 0xa779f689, 0xbc612ea8,
  0x819e90d8, 0x3fffa7c1, 0xf3a5931e, 0x3c874853, 0x3692d514, 0x3fffbdba,
  0x15098eb6, 0xbc796773, 0x2b8f71f1, 0x3fffd3c2, 0x966579e7, 0x3c62eb74,
  0x6b2a23d9, 0x3fffe9d9, 0x7442fde3, 0x3c74a603,
};

static const uint32_t T_pow_coeff_h[4] __attribute__((aligned(16))) = {
  0x00000000, 0xbfd61a00, 0x00000000, 0xbf5dabe1,
};

static const uint32_t T_pow_coeff_pow[24] __attribute__((aligned(16))) = {
  0x6dc96112, 0xbf836578, 0xee241472, 0xbf9b0301, 0x9f95985a, 0xbfb528db,
  0xb3841d2a, 0xbfd619b6, 0x518775e3, 0x3f9004f2, 0xac8349bb, 0x3fa76c9b,
  0x486ececc, 0x3fc4635e, 0x161bb241, 0xbf5dabe1, 0x9f95985a, 0xbfb528db,
  0xf8b5787d, 0x3ef2531e, 0x486ececb, 0x3fc4635e, 0x412055cc, 0xbdd61bb2,
};

static const uint32_t T_pow_e_coeff[12] __attribute__((aligned(16))) = {
  0xe78a6731, 0x3f55d87f, 0xd704a0c0, 0x3fac6b08, 0x6fba4e77, 0x3f83b2ab,
  0xff82c58f, 0x3fcebfbd, 0xfefa39ef, 0x3fe62e42, 0x00000000, 0x00000000,
};

static const uint32_t T_pow_log2_pow[4] __attribute__((aligned(16))) = {
  0xfefa39ef, 0x3fe62e42, 0xfefa39ef, 0xbfe62e42,
};

static const uint32_t T_sin_ALL_ONES[2] __attribute__((aligned(16))) = {
  0xffffffff, 0x3fefffff,
};

static const uint32_t T_tan_Ctable_tan[1408] __attribute__((aligned(16))) = {
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x882c10fa, 0x3f9664f4,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x55e6c23d, 0x3f8226e3, 0x55555555, 0x3fd55555, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x0e157de0, 0x3f6d6d3d, 0x11111111, 0x3fc11111,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x452b75e3, 0x3f57da36,
  0x1ba1ba1c, 0x3faba1ba, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x00000000, 0x3ff00000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x4e435f9b, 0x3f953f83, 0x00000000, 0x00000000,
  0x3c6e8e46, 0x3f9b74ea, 0x00000000, 0x00000000, 0xda5b7511, 0x3f85ad63,
  0xdc230b9b, 0x3fb97558, 0x26cb3788, 0x3f881308, 0x76fc4985, 0x3fd62ac9,
  0x77bb08ba, 0x3f757c85, 0xb6247521, 0x3fb1381e, 0x5922170c, 0x3f754e95,
  0x8746482d, 0x3fc27f83, 0x11055b30, 0x3f64e391, 0x3e666320, 0x3fa3e609,
  0x0de9dae3, 0x3f6301df, 0x1f1dca06, 0x3fafa8ae, 0x8c5b2da2, 0x3fb936bb,
  0x4e88f7a5, 0x3c587d05, 0x00000000, 0x3ff00000, 0xa8935dd9, 0x3f83dde2,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x5a279ea3, 0x3faa3407,
  0x00000000, 0x00000000, 0x432d65fa, 0x3fa70153, 0x00000000, 0x00000000,
  0x891a4602, 0x3f9d03ef, 0xd62ca5f8, 0x3fca77d9, 0xb35f4628, 0x3f97a265,
  0x433258fa, 0x3fd8cf51, 0xb58fd909, 0x3f8f88e3, 0x01771cea, 0x3fc2b154,
  0xf3562f8e, 0x3f888f57, 0xc028a723, 0x3fc7370f, 0x20b7f9f0, 0x3f80f44c,
  0x214368e9, 0x3fb6dfaa, 0x28891863, 0x3f79b4b6, 0x172dbbf0, 0x3fb6cb8e,
  0xe0553158, 0x3fc975f5, 0x593fe814, 0x3c2ef5d3, 0x00000000, 0x3ff00000,
  0x03dec550, 0x3fa44203, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x9314533e, 0x3fbb8ec5, 0x00000000, 0x00000000, 0x09aa36d0, 0x3fb6d3f4,
  0x00000000, 0x00000000, 0xdcb427fd, 0x3fb13950, 0xd87ab0bb, 0x3fd5335e,
  0xce0ae8a5, 0x3fabb382, 0x79143126, 0x3fddba41, 0x5f2b28d4, 0x3fa552f1,
  0x59f21a6d, 0x3fd015ab, 0x22c27d95, 0x3fa0e984, 0xe19fc6aa, 0x3fd0576c,
  0x8f2c2950, 0x3f9a4898, 0xc0b3f22c, 0x3fc59462, 0x1883a4b8, 0x3f94b61c,
  0x3f838640, 0x3fc30eb8, 0x355c63dc, 0x3fd36a08, 0x1dce993d, 0xbc6d704d,
  0x00000000, 0x3ff00000, 0x2b82ab63, 0x3fb78e92, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x56f37042, 0x3fccfc56, 0x00000000, 0x00000000,
  0xaa563951, 0x3fc90125, 0x00000000, 0x00000000, 0x3d0e7c5d, 0x3fc50533,
  0x9bed9b2e, 0x3fdf0ed9, 0x5fe7c47c, 0x3fc1f250, 0x96c125e5, 0x3fe2edd9,
  0x5a02bbd8, 0x3fbe5c71, 0x86362c20, 0x3fda08b7, 0x4b4435ed, 0x3fb9d342,
  0x4b494091, 0x3fd911bd, 0xb56658be, 0x3fb5e4c7, 0x93a2fd76, 0x3fd3c092,
  0xda271794, 0x3fb29910, 0x3303df2b, 0x3fd189be, 0x99fcef32, 0x3fda8279,
  0xb68c1467, 0x3c708b2f, 0x00000000, 0x3ff00000, 0x980c4337, 0x3fc5f619,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xcc03e501, 0x3fdff10f,
  0x00000000, 0x00000000, 0x44a4e845, 0x3fddb63b, 0x00000000, 0x00000000,
  0x3768ad9f, 0x3fdb72a4, 0x3dd01cca, 0x3fe5fdb9, 0xa61d2811, 0x3fd972b2,
  0x5645ad0b, 0x3fe977f9, 0xd013b3ab, 0x3fd78ca3, 0xbf0bf914, 0x3fe4f192,
  0x4d53e730, 0x3fd5d060, 0x3f8b9000, 0x3fe49933, 0xe2b82f08, 0x3fd4322a,
  0x5936a835, 0x3fe27ae1, 0xb1c61c9b, 0x3fd2b3fb, 0xef478605, 0x3fe1659e,
  0x190834ec, 0x3fe11ab7, 0xcdb625ea, 0xbc8e564b, 0x00000000, 0x3ff00000,
  0xb07217e3, 0x3fd248f1, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x2b2c49d0, 0x3ff2de9c, 0x00000000, 0x00000000, 0x2655bc98, 0x3ff33e58,
  0x00000000, 0x00000000, 0xff691fa2, 0x3ff3972e, 0xe93463bd, 0x3feeed87,
  0x070e10a0, 0x3ff3f5b2, 0xf4d790a4, 0x3ff20c10, 0xa04e8ea3, 0x3ff4541a,
  0x386accd3, 0x3ff1369e, 0x222a66dd, 0x3ff4b521, 0x22a9777e, 0x3ff20817,
  0x52a04a6e, 0x3ff5178f, 0xddaa0031, 0x3ff22137, 0x4447d47c, 0x3ff57c01,
  0x1e9c7f1d, 0x3ff29311, 0x2ab7f990, 0x3fe561b8, 0x209c7df1, 0x3c87a8c5,
  0x00000000, 0x3ff00000, 0x4170bcc6, 0x3fdc92d8, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0xc7ab4d5a, 0x40085e24, 0x00000000, 0x00000000,
  0xe93ea75d, 0x400b963d, 0x00000000, 0x00000000, 0x94a7f25a, 0x400f37e2,
  0x4b6261cb, 0x3ff5f984, 0x5a9dd812, 0x4011aab0, 0x74c30018, 0x3ffaf5a5,
  0x7f2ce8e3, 0x4013fe8b, 0xfe8e54fa, 0x3ffd7334, 0x670d618d, 0x4016a10c,
  0x4db97058, 0x4000e012, 0x24df44dd, 0x40199c5f, 0x697d6ece, 0x4003006e,
  0x83298b82, 0x401cfc4d, 0x19d490d6, 0x40058c19, 0x2ae42850, 0x3fea4300,
  0x118e20e6, 0xbc7a6db8, 0x00000000, 0x40000000, 0xe33345b8, 0xbfd4e526,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x65965966, 0x40219659,
  0x00000000, 0x00000000, 0x882c10fa, 0x402664f4, 0x00000000, 0x00000000,
  0x83cd3723, 0x402c8342, 0x00000000, 0x40000000, 0x55e6c23d, 0x403226e3,
  0x55555555, 0x40055555, 0x34451939, 0x40371c96, 0xaaaaaaab, 0x400aaaaa,
  0x0e157de0, 0x403d6d3d, 0x11111111, 0x40111111, 0xa738201f, 0x4042bbce,
  0x05b05b06, 0x4015b05b, 0x452b75e3, 0x4047da36, 0x1ba1ba1c, 0x401ba1ba,
  0x00000000, 0x3ff00000, 0x00000000, 0x00000000, 0x00000000, 0x40000000,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x4f48b8d3, 0xbf33eaf9, 0x00000000, 0x00000000, 0x0cf7586f, 0x3f20b8ea,
  0x00000000, 0x00000000, 0xd0258911, 0xbf0abaf3, 0x23e49fe9, 0xbfab5a8c,
  0x2d53222e, 0x3ef60d15, 0x21169451, 0x3fa172b2, 0xbb254dbc, 0xbee1d3b5,
  0xdbf93b8e, 0xbf84c7db, 0x05b4630b, 0x3ecd3364, 0xee9aada7, 0x3f743924,
  0x794a8297, 0xbeb7b7b9, 0xe015f797, 0xbf5d41f5, 0xe41a4a56, 0x3ea35dfb,
  0xe4c2a251, 0x3f49a2ab, 0x5af9e000, 0xbfce49ce, 0x8c743719, 0x3d1eb860,
  0x00000000, 0x00000000, 0x1b4863cf, 0x3fd78294, 0x00000000, 0x3ff00000,
  0x00000000, 0xfffffff8, 0x535ad890, 0xbf2b9320, 0x00000000, 0x00000000,
  0x018fdf1f, 0x3f16d61d, 0x00000000, 0x00000000, 0x0359f1be, 0xbf0139e4,
  0xa4317c6d, 0xbfa67e17, 0x82672d0f, 0x3eebb405, 0x2f1b621e, 0x3f9f455b,
  0x51ccf238, 0xbed55317, 0xf437b9ac, 0xbf804bee, 0xc791a2b5, 0x3ec0e993,
  0x919a1db2, 0x3f7080c2, 0x336a5b0e, 0xbeaa48a2, 0x0a268358, 0xbf55a443,
  0xdfd978e4, 0x3e94b61f, 0xd7767a58, 0x3f431806, 0x2aea0000, 0xbfc9bbe8,
  0x7723ea61, 0xbd3a2369, 0x00000000, 0x00000000, 0xdf7796ff, 0x3fd6e642,
  0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8, 0xb9ff07ce, 0xbf231c78,
  0x00000000, 0x00000000, 0xa5517182, 0x3f0ff0e0, 0x00000000, 0x00000000,
  0x790b4cbc, 0xbef66191, 0x848a46c6, 0xbfa21ac0, 0xb16435fa, 0x3ee1d3ec,
  0x2a1aa832, 0x3f9c71ea, 0xfdd299ef, 0xbec9dd1a, 0x3f8dbaaf, 0xbf793363,
  0x309fc6ea, 0x3eb415d6, 0xbee60471, 0x3f6b83ba, 0x94a0a697, 0xbe9dae11,
  0x3e5c67b3, 0xbf4fd07b, 0x9a8f3e3e, 0x3e86bd75, 0xa4beb7a4, 0x3f3d1eb1,
  0x29cfc000, 0xbfc549ce, 0xbf159358, 0xbd397b33, 0x00000000, 0x00000000,
  0x871fee6c, 0x3fd666f0, 0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8,
  0x7d98a556, 0xbf1a3958, 0x00000000, 0x00000000, 0x9d88dc01, 0x3f0704c2,
  0x00000000, 0x00000000, 0x73742a2b, 0xbeed054a, 0x58844587, 0xbf9c2a13,
  0x55688a79, 0x3ed7a326, 0xee33f1d6, 0x3f9a48f4, 0xa8dc9888, 0xbebf8939,
  0xaad4b5b8, 0xbf72f746, 0x9102efa1, 0x3ea88f82, 0xdabc29cf, 0x3f678228,
  0x9289afb8, 0xbe90f456, 0x741fb4ed, 0xbf46f3a3, 0xa97f6663, 0x3e79b4bf,
  0xca89ff3f, 0x3f36db70, 0xa8a2a000, 0xbfc0ee13, 0x3da24be1, 0xbd338b9f,
  0x00000000, 0x00000000, 0x11cd6c69, 0x3fd601fd, 0x00000000, 0x3ff00000,
  0x00000000, 0xfffffff8, 0x1a154b97, 0xbf116b01, 0x00000000, 0x00000000,
  0x2d427630, 0x3f0147bf, 0x00000000, 0x00000000, 0xb93820c8, 0xbee264d4,
  0xbb6cbb18, 0xbf94ab8c, 0x888d4d92, 0x3ed0568b, 0x60730f7c, 0x3f98b19b,
  0xe4b1fb11, 0xbeb2f950, 0x22cf9f74, 0xbf6b21cd, 0x4a3ff0a6, 0x3e9f499e,
  0xfd2b83ce, 0x3f64aad7, 0x637b73af, 0xbe83487c, 0xe522591a, 0xbf3fc092,
  0xa158e8bc, 0x3e6e3aae, 0xe5e82ffa, 0x3f329d2f, 0xd636a000, 0xbfb9477f,
  0xc2c2d2bc, 0xbd135ef9, 0x00000000, 0x00000000, 0xf2fdb123, 0x3fd5b566,
  0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8, 0xc41acb64, 0xbf05448d,
  0x00000000, 0x00000000, 0xdbb03d6f, 0x3efb7ad2, 0x00000000, 0x00000000,
  0x9e42962d, 0xbed5aea5, 0x2579f8ef, 0xbf8b2398, 0x288a1ed9, 0x3ec81441,
  0xb0198dc5, 0x3f979a3a, 0x2fdfe253, 0xbea57cd3, 0x5766336f, 0xbf617caa,
  0x600944c3, 0x3e954ed6, 0xa4e0aaf8, 0x3f62c646, 0x6b8fb29c, 0xbe74e3a3,
  0xdc4c0409, 0xbf33f952, 0x9bffe365, 0x3e6301ec, 0xb8869e44, 0x3f2fc566,
  0xe1e04000, 0xbfb0cc62, 0x016b907f, 0xbd119cbc, 0x00000000, 0x00000000,
  0xe6b9d8fa, 0x3fd57fb3, 0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8,
  0x5daf22a6, 0xbef429d7, 0x00000000, 0x00000000, 0x06bca545, 0x3ef7a27d,
  0x00000000, 0x00000000, 0x7211c19a, 0xbec41c3e, 0x956ed53e, 0xbf7ae3f4,
  0xee750e72, 0x3ec3901b, 0x91d443f5, 0x3f96f713, 0x36661e6c, 0xbe936e09,
  0x506f9381, 0xbf5122e8, 0xcb6dd43f, 0x3e9041b9, 0x6698b2ff, 0x3f61b0c7,
  0x576bf12b, 0xbe625a8a, 0xe5a0e9dc, 0xbf23499d, 0x110384dd, 0x3e5b1c2c,
  0x68d43db6, 0x3f2cb899, 0x6ecac000, 0xbfa0c414, 0xcd7dd58c, 0x3d13500f,
  0x00000000, 0x00000000, 0x85a2c8fb, 0x3fd55fe0, 0x00000000, 0x3ff00000,
  0x00000000, 0xfffffff8, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x2bf70ebe, 0x3ef66a8f, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0xd644267f, 0x3ec22805, 0x16c16c17, 0x3f96c16c,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xc4e09162, 0x3e8d6db2,
  0xbc011567, 0x3f61566a, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x1f79955c, 0x3e57da4e, 0x9334ef0b, 0x3f2bbd77, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x55555555, 0x3fd55555,
  0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8, 0x5daf22a6, 0x3ef429d7,
  0x00000000, 0x00000000, 0x06bca545, 0x3ef7a27d, 0x00000000, 0x00000000,
  0x7211c19a, 0x3ec41c3e, 0x956ed53e, 0x3f7ae3f4, 0xee750e72, 0x3ec3901b,
  0x91d443f5, 0x3f96f713, 0x36661e6c, 0x3e936e09, 0x506f9381, 0x3f5122e8,
  0xcb6dd43f, 0x3e9041b9, 0x6698b2ff, 0x3f61b0c7, 0x576bf12b, 0x3e625a8a,
  0xe5a0e9dc, 0x3f23499d, 0x110384dd, 0x3e5b1c2c, 0x68d43db6, 0x3f2cb899,
  0x6ecac000, 0x3fa0c414, 0xcd7dd58c, 0xbd13500f, 0x00000000, 0x00000000,
  0x85a2c8fb, 0x3fd55fe0, 0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8,
  0xc41acb64, 0x3f05448d, 0x00000000, 0x00000000, 0xdbb03d6f, 0x3efb7ad2,
  0x00000000, 0x00000000, 0x9e42962d, 0x3ed5aea5, 0x2579f8ef, 0x3f8b2398,
  0x288a1ed9, 0x3ec81441, 0xb0198dc5, 0x3f979a3a, 0x2fdfe253, 0x3ea57cd3,
  0x5766336f, 0x3f617caa, 0x600944c3, 0x3e954ed6, 0xa4e0aaf8, 0x3f62c646,
  0x6b8fb29c, 0x3e74e3a3, 0xdc4c0409, 0x3f33f952, 0x9bffe365, 0x3e6301ec,
  0xb8869e44, 0x3f2fc566, 0xe1e04000, 0x3fb0cc62, 0x016b907f, 0x3d119cbc,
  0x00000000, 0x00000000, 0xe6b9d8fa, 0x3fd57fb3, 0x00000000, 0x3ff00000,
  0x00000000, 0xfffffff8, 0x1a154b97, 0x3f116b01, 0x00000000, 0x00000000,
  0x2d427630, 0x3f0147bf, 0x00000000, 0x00000000, 0xb93820c8, 0x3ee264d4,
  0xbb6cbb18, 0x3f94ab8c, 0x888d4d92, 0x3ed0568b, 0x60730f7c, 0x3f98b19b,
  0xe4b1fb11, 0x3eb2f950, 0x22cf9f74, 0x3f6b21cd, 0x4a3ff0a6, 0x3e9f499e,
  0xfd2b83ce, 0x3f64aad7, 0x637b73af, 0x3e83487c, 0xe522591a, 0x3f3fc092,
  0xa158e8bc, 0x3e6e3aae, 0xe5e82ffa, 0x3f329d2f, 0xd636a000, 0x3fb9477f,
  0xc2c2d2bc, 0x3d135ef9, 0x00000000, 0x00000000, 0xf2fdb123, 0x3fd5b566,
  0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8, 0x7d98a556, 0x3f1a3958,
  0x00000000, 0x00000000, 0x9d88dc01, 0x3f0704c2, 0x00000000, 0x00000000,
  0x73742a2b, 0x3eed054a, 0x58844587, 0x3f9c2a13, 0x55688a79, 0x3ed7a326,
  0xee33f1d6, 0x3f9a48f4, 0xa8dc9888, 0x3ebf8939, 0xaad4b5b8, 0x3f72f746,
  0x9102efa1, 0x3ea88f82, 0xdabc29cf, 0x3f678228, 0x9289afb8, 0x3e90f456,
  0x741fb4ed, 0x3f46f3a3, 0xa97f6663, 0x3e79b4bf, 0xca89ff3f, 0x3f36db70,
  0xa8a2a000, 0x3fc0ee13, 0x3da24be1, 0x3d338b9f, 0x00000000, 0x00000000,
  0x11cd6c69, 0x3fd601fd, 0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8,
  0xb9ff07ce, 0x3f231c78, 0x00000000, 0x00000000, 0xa5517182, 0x3f0ff0e0,
  0x00000000, 0x00000000, 0x790b4cbc, 0x3ef66191, 0x848a46c6, 0x3fa21ac0,
  0xb16435fa, 0x3ee1d3ec, 0x2a1aa832, 0x3f9c71ea, 0xfdd299ef, 0x3ec9dd1a,
  0x3f8dbaaf, 0x3f793363, 0x309fc6ea, 0x3eb415d6, 0xbee60471, 0x3f6b83ba,
  0x94a0a697, 0x3e9dae11, 0x3e5c67b3, 0x3f4fd07b, 0x9a8f3e3e, 0x3e86bd75,
  0xa4beb7a4, 0x3f3d1eb1, 0x29cfc000, 0x3fc549ce, 0xbf159358, 0x3d397b33,
  0x00000000, 0x00000000, 0x871fee6c, 0x3fd666f0, 0x00000000, 0x3ff00000,
  0x00000000, 0xfffffff8, 0x535ad890, 0x3f2b9320, 0x00000000, 0x00000000,
  0x018fdf1f, 0x3f16d61d, 0x00000000, 0x00000000, 0x0359f1be, 0x3f0139e4,
  0xa4317c6d, 0x3fa67e17, 0x82672d0f, 0x3eebb405, 0x2f1b621e, 0x3f9f455b,
  0x51ccf238, 0x3ed55317, 0xf437b9ac, 0x3f804bee, 0xc791a2b5, 0x3ec0e993,
  0x919a1db2, 0x3f7080c2, 0x336a5b0e, 0x3eaa48a2, 0x0a268358, 0x3f55a443,
  0xdfd978e4, 0x3e94b61f, 0xd7767a58, 0x3f431806, 0x2aea0000, 0x3fc9bbe8,
  0x7723ea61, 0x3d3a2369, 0x00000000, 0x00000000, 0xdf7796ff, 0x3fd6e642,
  0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8, 0x4f48b8d3, 0x3f33eaf9,
  0x00000000, 0x00000000, 0x0cf7586f, 0x3f20b8ea, 0x00000000, 0x00000000,
  0xd0258911, 0x3f0abaf3, 0x23e49fe9, 0x3fab5a8c, 0x2d53222e, 0x3ef60d15,
  0x21169451, 0x3fa172b2, 0xbb254dbc, 0x3ee1d3b5, 0xdbf93b8e, 0x3f84c7db,
  0x05b4630b, 0x3ecd3364, 0xee9aada7, 0x3f743924, 0x794a8297, 0x3eb7b7b9,
  0xe015f797, 0x3f5d41f5, 0xe41a4a56, 0x3ea35dfb, 0xe4c2a251, 0x3f49a2ab,
  0x5af9e000, 0x3fce49ce, 0x8c743719, 0xbd1eb860, 0x00000000, 0x00000000,
  0x1b4863cf, 0x3fd78294, 0x00000000, 0x3ff00000, 0x00000000, 0xfffffff8,
  0x65965966, 0xc0219659, 0x00000000, 0x00000000, 0x882c10fa, 0x402664f4,
  0x00000000, 0x00000000, 0x83cd3723, 0xc02c8342, 0x00000000, 0xc0000000,
  0x55e6c23d, 0x403226e3, 0x55555555, 0x40055555, 0x34451939, 0xc0371c96,
  0xaaaaaaab, 0xc00aaaaa, 0x0e157de0, 0x403d6d3d, 0x11111111, 0x40111111,
  0xa738201f, 0xc042bbce, 0x05b05b06, 0xc015b05b, 0x452b75e3, 0x4047da36,
  0x1ba1ba1c, 0x401ba1ba, 0x00000000, 0xbff00000, 0x00000000, 0x00000000,
  0x00000000, 0x40000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0xc7ab4d5a, 0xc0085e24, 0x00000000, 0x00000000,
  0xe93ea75d, 0x400b963d, 0x00000000, 0x00000000, 0x94a7f25a, 0xc00f37e2,
  0x4b6261cb, 0xbff5f984, 0x5a9dd812, 0x4011aab0, 0x74c30018, 0x3ffaf5a5,
  0x7f2ce8e3, 0xc013fe8b, 0xfe8e54fa, 0xbffd7334, 0x670d618d, 0x4016a10c,
  0x4db97058, 0x4000e012, 0x24df44dd, 0xc0199c5f, 0x697d6ece, 0xc003006e,
  0x83298b82, 0x401cfc4d, 0x19d490d6, 0x40058c19, 0x2ae42850, 0xbfea4300,
  0x118e20e6, 0x3c7a6db8, 0x00000000, 0x40000000, 0xe33345b8, 0xbfd4e526,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x2b2c49d0, 0xbff2de9c,
  0x00000000, 0x00000000, 0x2655bc98, 0x3ff33e58, 0x00000000, 0x00000000,
  0xff691fa2, 0xbff3972e, 0xe93463bd, 0xbfeeed87, 0x070e10a0, 0x3ff3f5b2,
  0xf4d790a4, 0x3ff20c10, 0xa04e8ea3, 0xbff4541a, 0x386accd3, 0xbff1369e,
  0x222a66dd, 0x3ff4b521, 0x22a9777e, 0x3ff20817, 0x52a04a6e, 0xbff5178f,
  0xddaa0031, 0xbff22137, 0x4447d47c, 0x3ff57c01, 0x1e9c7f1d, 0x3ff29311,
  0x2ab7f990, 0xbfe561b8, 0x209c7df1, 0xbc87a8c5, 0x00000000, 0x3ff00000,
  0x4170bcc6, 0x3fdc92d8, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0xcc03e501, 0xbfdff10f, 0x00000000, 0x00000000, 0x44a4e845, 0x3fddb63b,
  0x00000000, 0x00000000, 0x3768ad9f, 0xbfdb72a4, 0x3dd01cca, 0xbfe5fdb9,
  0xa61d2811, 0x3fd972b2, 0x5645ad0b, 0x3fe977f9, 0xd013b3ab, 0xbfd78ca3,
  0xbf0bf914, 0xbfe4f192, 0x4d53e730, 0x3fd5d060, 0x3f8b9000, 0x3fe49933,
  0xe2b82f08, 0xbfd4322a, 0x5936a835, 0xbfe27ae1, 0xb1c61c9b, 0x3fd2b3fb,
  0xef478605, 0x3fe1659e, 0x190834ec, 0xbfe11ab7, 0xcdb625ea, 0x3c8e564b,
  0x00000000, 0x3ff00000, 0xb07217e3, 0x3fd248f1, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x56f37042, 0xbfccfc56, 0x00000000, 0x00000000,
  0xaa563951, 0x3fc90125, 0x00000000, 0x00000000, 0x3d0e7c5d, 0xbfc50533,
  0x9bed9b2e, 0xbfdf0ed9, 0x5fe7c47c, 0x3fc1f250, 0x96c125e5, 0x3fe2edd9,
  0x5a02bbd8, 0xbfbe5c71, 0x86362c20, 0xbfda08b7, 0x4b4435ed, 0x3fb9d342,
  0x4b494091, 0x3fd911bd, 0xb56658be, 0xbfb5e4c7, 0x93a2fd76, 0xbfd3c092,
  0xda271794, 0x3fb29910, 0x3303df2b, 0x3fd189be, 0x99fcef32, 0xbfda8279,
  0xb68c1467, 0xbc708b2f, 0x00000000, 0x3ff00000, 0x980c4337, 0x3fc5f619,
  0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x9314533e, 0xbfbb8ec5,
  0x00000000, 0x00000000, 0x09aa36d0, 0x3fb6d3f4, 0x00000000, 0x00000000,
  0xdcb427fd, 0xbfb13950, 0xd87ab0bb, 0xbfd5335e, 0xce0ae8a5, 0x3fabb382,
  0x79143126, 0x3fddba41, 0x5f2b28d4, 0xbfa552f1, 0x59f21a6d, 0xbfd015ab,
  0x22c27d95, 0x3fa0e984, 0xe19fc6aa, 0x3fd0576c, 0x8f2c2950, 0xbf9a4898,
  0xc0b3f22c, 0xbfc59462, 0x1883a4b8, 0x3f94b61c, 0x3f838640, 0x3fc30eb8,
  0x355c63dc, 0xbfd36a08, 0x1dce993d, 0x3c6d704d, 0x00000000, 0x3ff00000,
  0x2b82ab63, 0x3fb78e92, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
  0x5a279ea3, 0xbfaa3407, 0x00000000, 0x00000000, 0x432d65fa, 0x3fa70153,
  0x00000000, 0x00000000, 0x891a4602, 0xbf9d03ef, 0xd62ca5f8, 0xbfca77d9,
  0xb35f4628, 0x3f97a265, 0x433258fa, 0x3fd8cf51, 0xb58fd909, 0xbf8f88e3,
  0x01771cea, 0xbfc2b154, 0xf3562f8e, 0x3f888f57, 0xc028a723, 0x3fc7370f,
  0x20b7f9f0, 0xbf80f44c, 0x214368e9, 0xbfb6dfaa, 0x28891863, 0x3f79b4b6,
  0x172dbbf0, 0x3fb6cb8e, 0xe0553158, 0xbfc975f5, 0x593fe814, 0xbc2ef5d3,
  0x00000000, 0x3ff00000, 0x03dec550, 0x3fa44203, 0x00000000, 0x00000000,
  0x00000000, 0x00000000, 0x4e435f9b, 0xbf953f83, 0x00000000, 0x00000000,
  0x3c6e8e46, 0x3f9b74ea, 0x00000000, 0x00000000, 0xda5b7511, 0xbf85ad63,
  0xdc230b9b, 0xbfb97558, 0x26cb3788, 0x3f881308, 0x76fc4985, 0x3fd62ac9,
  0x77bb08ba, 0xbf757c85, 0xb6247521, 0xbfb1381e, 0x5922170c, 0x3f754e95,
  0x8746482d, 0x3fc27f83, 0x11055b30, 0xbf64e391, 0x3e666320, 0xbfa3e609,
  0x0de9dae3, 0x3f6301df, 0x1f1dca06, 0x3fafa8ae, 0x8c5b2da2, 0xbfb936bb,
  0x4e88f7a5, 0xbc587d05, 0x00000000, 0x3ff00000, 0xa8935dd9, 0x3f83dde2,
  0x00000000, 0x00000000, 0x00000000, 0x00000000,
};

static const uint32_t T_tan_MASK_35_tan[4] __attribute__((aligned(16))) = {
  0xfffc0000, 0xffffffff, 0x00000000, 0x00000000,
};

static const uint32_t T_tan_MUL16[4] __attribute__((aligned(16))) = {
  0x00000000, 0x40300000, 0x00000000, 0x3ff00000,
};

static const uint32_t T_tan_PI32INV_tan[4] __attribute__((aligned(16))) = {
  0x6dc9c883, 0x3fe45f30, 0x6dc9c883, 0x40245f30,
};

static const uint32_t T_tan_PI_4_tan[4] __attribute__((aligned(16))) = {
  0x00000000, 0x3fe921fb, 0x4611a626, 0x3e85110b,
};

static const uint32_t T_tan_P_1_tan[4] __attribute__((aligned(16))) = {
  0x54444000, 0x3fb921fb, 0x54440000, 0x3fb921fb,
};

static const uint32_t T_tan_P_2_tan[4] __attribute__((aligned(16))) = {
  0x67674000, 0xbd32e7b9, 0x4c4c0000, 0x3d468c23,
};

static const uint32_t T_tan_P_3_tan[4] __attribute__((aligned(16))) = {
  0x3707344a, 0x3aa8a2e0, 0x03707345, 0x3ae98a2e,
};

static const uint32_t T_tan_QQ_2_tan[2] __attribute__((aligned(16))) = {
  0x676733af, 0x3d32e7b9,
};

static const uint32_t T_tan_Q_11_tan[2] __attribute__((aligned(16))) = {
  0xb8fe4d77, 0x3f82609a,
};

static const uint32_t T_tan_Q_3_tan[2] __attribute__((aligned(16))) = {
  0x55555612, 0x3fd55555,
};

static const uint32_t T_tan_Q_5_tan[2] __attribute__((aligned(16))) = {
  0x11092746, 0x3fc11111,
};

static const uint32_t T_tan_Q_7_tan[2] __attribute__((aligned(16))) = {
  0x52c4c8ab, 0x3faba1ba,
};

static const uint32_t T_tan_Q_9_tan[2] __attribute__((aligned(16))) = {
  0xbf847a43, 0x3f9664a0,
};

static const uint32_t T_tan_sign_mask_tan[4] __attribute__((aligned(16))) = {
  0x00000000, 0x80000000, 0x00000000, 0x80000000,
};

#ifdef HSMATH_CHECK
static const struct hs_tab hs_tabs[] = {
  TAB(T_exp_ALLONES),
  TAB(T_exp_INF),
  TAB(T_exp_Tbl_addr),
  TAB(T_exp_XMAX),
  TAB(T_exp_XMIN),
  TAB(T_exp_bias),
  TAB(T_exp_cv),
  TAB(T_exp_ebias),
  TAB(T_exp_mmask),
  TAB(T_k_Ctable),
  TAB(T_k_NEG_ZERO),
  TAB(T_k_ONE),
  TAB(T_k_ONEHALF),
  TAB(T_k_PI32INV),
  TAB(T_k_PI_4),
  TAB(T_k_PI_INV_TABLE),
  TAB(T_k_P_1),
  TAB(T_k_P_2),
  TAB(T_k_P_3),
  TAB(T_k_SC_1),
  TAB(T_k_SC_2),
  TAB(T_k_SC_3),
  TAB(T_k_SC_4),
  TAB(T_k_SHIFTER),
  TAB(T_k_SIGN_MASK),
  TAB(T_k_TWO_POW_55),
  TAB(T_k_TWO_POW_M55),
  TAB(T_k_ZERO),
  TAB(T_log_HIGHSIGMASK_log10),
  TAB(T_log_LOG10_E),
  TAB(T_log_L_tbl),
  TAB(T_log_L_tbl_log10),
  TAB(T_log_coeff),
  TAB(T_log_coeff_log10),
  TAB(T_log_log2),
  TAB(T_log_log2_log10),
  TAB(T_pow_DOUBLE0),
  TAB(T_pow_DOUBLE0DOT5),
  TAB(T_pow_DOUBLE2),
  TAB(T_pow_HALFMASK),
  TAB(T_pow_HIGHMASK_LOG_X),
  TAB(T_pow_HIGHMASK_Y),
  TAB(T_pow_HIGHSIGMASK),
  TAB(T_pow_LOG2_E),
  TAB(T_pow_L_tbl_pow),
  TAB(T_pow_T_exp),
  TAB(T_pow_coeff_h),
  TAB(T_pow_coeff_pow),
  TAB(T_pow_e_coeff),
  TAB(T_pow_log2_pow),
  TAB(T_sin_ALL_ONES),
  TAB(T_tan_Ctable_tan),
  TAB(T_tan_MASK_35_tan),
  TAB(T_tan_MUL16),
  TAB(T_tan_PI32INV_tan),
  TAB(T_tan_PI_4_tan),
  TAB(T_tan_P_1_tan),
  TAB(T_tan_P_2_tan),
  TAB(T_tan_P_3_tan),
  TAB(T_tan_QQ_2_tan),
  TAB(T_tan_Q_11_tan),
  TAB(T_tan_Q_3_tan),
  TAB(T_tan_Q_5_tan),
  TAB(T_tan_Q_7_tan),
  TAB(T_tan_Q_9_tan),
  TAB(T_tan_sign_mask_tan),
};
static void hs_check(uint64_t a, size_t n) {
  if (a >= hs_stk_lo && a + n <= hs_stk_hi) return;
  for (size_t i = 0; i < sizeof hs_tabs / sizeof hs_tabs[0]; i++)
    if (a >= A(hs_tabs[i].p) && a + n <= A(hs_tabs[i].p) + hs_tabs[i].n) return;
  fprintf(stderr, "hsmath: access outside tables and stack\n");
  abort();
}
#endif

/* the stubs */
static double hs_pow(__m128i xmm0, __m128i xmm1) {
  REGS_INIT;
  __m128i xmm2 = XINIT(2);
  __m128i xmm3 = XINIT(3);
  __m128i xmm4 = XINIT(4);
  __m128i xmm5 = XINIT(5);
  __m128i xmm6 = XINIT(6);
  __m128i xmm7 = XINIT(7);
  STACK_INIT;
  PUSH(rbp); rbp = rsp;
  SUB64(rsp, 0x28ull);
  STQ(rsp + 8, xmm0);
  STQ(rsp + 16, xmm1);
  r8 = (uint64_t)_mm_cvtsi128_si64(xmm1);
  CMP64(r8, LD64(A(T_pow_DOUBLE2))); SCRATCH(r9);
  if (!zf) goto B1_2;
  xmm0 = MULSD(xmm0, xmm0);
  goto B1_5;
B1_2:;
  CMP64(r8, LD64(A(T_pow_DOUBLE0DOT5))); SCRATCH(r9);
  if (!zf) goto L_POW;
  r9 = (uint64_t)_mm_cvtsi128_si64(xmm0);
  CMP64(r9, LD64(A(T_pow_DOUBLE0))); SCRATCH(r10);
  if (sf != of) goto L_POW;
  xmm0 = SQRTSD(xmm0, xmm0);
  goto B1_5;
L_POW:;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  xmm2 = _mm_xor_si128(xmm2, xmm2);
  r9 = 0x3ff0000000000000ull;
  xmm2 = _mm_cvtsi64_si128((long long)r9);
  r8 = 0x3fb90000u;
  xmm7 = _mm_cvtsi64_si128((long long)r8);
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  r10 = 0x77f0000000000000ull;
  xmm1 = _mm_cvtsi64_si128((long long)r10);
  xmm3 = xmm0;
  rdx = 0x7ff0u;
  AND32(rdx, rax);
  SUB32(rdx, 0x3ff0u);
  rcx = (uint32_t)rdx;
  SAR32(rdx, 31);
  ADD32(rcx, rdx);
  XOR32(rcx, rdx);
  xmm0 = _mm_or_si128(xmm0, xmm2);
  xmm6 = LD128(A(T_pow_HIGHSIGMASK)); SCRATCH(r9);
  xmm0 = _mm_srli_epi64(xmm0, 27);
  xmm2 = LDQ(A(T_pow_LOG2_E)); SCRATCH(r9);
  xmm0 = _mm_srli_epi32(xmm0, 2);
  ADD32(rcx, 0x10u);
  BSRL(rcx, rcx);
  xmm0 = RCPPS(xmm0, xmm0);
  xmm3 = _mm_slli_epi64(xmm3, 12);
  r11 = 0x2000u;
  xmm4 = _mm_cvtsi64_si128((long long)r11);
  xmm3 = _mm_srli_epi64(xmm3, 12);
  SUB32(rax, 0x10u);
  CMP32(rax, 0x7fe0u);
  if (!cf) goto L_2TAG_PACKET_0_0_2;
  r8 = 0x0u;
L_2TAG_PACKET_1_0_2:;
  xmm0 = MULSS(xmm0, xmm7);
  rdx = 0xffffffffu;
  SUB32(rcx, 0x4u);
  SHL32(rdx, rcx);
  SHL64(rdx, 32);
  xmm5 = _mm_cvtsi64_si128((long long)rdx);
  xmm3 = _mm_or_si128(xmm3, xmm1);
  SUB32(rax, 0x3fdfu);
  CMP32(rax, 0x1u);
  if (cf || zf) goto L_2TAG_PACKET_2_0_2;
  xmm0 = _mm_add_epi32(xmm0, xmm4);
  xmm5 = _mm_and_si128(xmm5, xmm3);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  xmm0 = _mm_slli_epi64(xmm0, 29);
L_2TAG_PACKET_3_0_2:;
  xmm3 = SUBSD(xmm3, xmm5);
  xmm0 = _mm_and_si128(xmm0, xmm6);
  SUB32(rax, 0x1u);
  SAR32(rax, 4);
  xmm7 = CVTSI2SDL(xmm7, rax);
  xmm5 = MULPD(xmm5, xmm0);
L_2TAG_PACKET_4_0_2:;
  xmm3 = MULSD(xmm3, xmm0);
  xmm1 = LD128(A(T_pow_coeff_pow)); SCRATCH(r9);
  r11 = A(T_pow_L_tbl_pow);
  xmm5 = SUBSD(xmm5, xmm2);
  xmm4 = LD128(A(T_pow_coeff_pow) + 16); SCRATCH(r9);
  rcx = (uint32_t)rax;
  SAR32(rax, 31);
  ADD32(rcx, rax);
  XOR32(rax, rcx);
  ADD32(rax, 0x1u);
  BSRL(rax, rax);
  xmm5 = _mm_unpacklo_epi64(xmm5, xmm3);
  xmm6 = LD128(A(T_pow_coeff_pow) + 32); SCRATCH(r9);
  xmm3 = ADDSD(xmm3, xmm5);
  AND32(rdx, 0xffc000u);
  SHR32(rdx, 10);
  xmm5 = ADDPD(xmm5, LD128(r11 + rdx + (uint64_t)(int64_t)-3648));
  xmm0 = LD128(A(T_pow_coeff_pow) + 48); SCRATCH(r9);
  xmm2 = _mm_shuffle_epi32(xmm3, 68);
  xmm3 = MULSD(xmm3, xmm3);
  xmm1 = MULPD(xmm1, xmm2);
  xmm4 = MULPD(xmm4, xmm2);
  xmm5 = ADDSD(xmm5, xmm7);
  xmm2 = MULSD(xmm2, xmm3);
  xmm6 = ADDPD(xmm6, xmm1);
  xmm3 = MULSD(xmm3, xmm3);
  xmm0 = ADDPD(xmm0, xmm4);
  xmm1 = LDQ(rsp + 16);
  rcx = (rcx & ~0xffffull) | LD16(rsp + 22);
  xmm7 = _mm_shuffle_epi32(xmm5, 238);
  xmm4 = LDQ(A(T_pow_HIGHMASK_Y)); SCRATCH(r9);
  xmm6 = MULPD(xmm6, xmm2);
  xmm3 = _mm_shuffle_epi32(xmm3, 68);
  xmm0 = MULPD(xmm0, xmm2);
  SHL32(rax, 4);
  SUB32(rax, 0x3e00u);
  AND32(rcx, 0x7ff0u);
  ADD32(rax, rcx);
  xmm3 = MULPD(xmm3, xmm6);
  CMP32(rax, 0x270u);
  if (!cf) goto L_2TAG_PACKET_5_0_2;
  xmm6 = _mm_xor_si128(xmm6, xmm6);
  rdx = 0x42b8u;
  xmm6 = _mm_insert_epi16(xmm6, (int)(uint16_t)rdx, 3);
  xmm2 = xmm1;
  xmm4 = _mm_and_si128(xmm4, xmm1);
  xmm1 = SUBSD(xmm1, xmm4);
  xmm4 = MULSD(xmm4, xmm5);
  xmm0 = ADDSD(xmm0, xmm7);
  xmm1 = MULSD(xmm1, xmm5);
  xmm7 = xmm6;
  xmm6 = ADDSD(xmm6, xmm4);
  r11 = A(T_pow_T_exp);
  xmm3 = ADDPD(xmm3, xmm0);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm6);
  xmm6 = SUBSD(xmm6, xmm7);
  xmm0 = _mm_shuffle_epi32(xmm3, 238);
  xmm4 = SUBSD(xmm4, xmm6);
  xmm0 = ADDSD(xmm0, xmm3);
  rcx = (uint32_t)rdx;
  AND32(rdx, 0xffu);
  ADD32(rdx, rdx);
  xmm5 = LD128(r11 + rdx * 8);
  xmm4 = ADDSD(xmm4, xmm1);
  xmm2 = MULSD(xmm2, xmm0);
  xmm7 = LD128(A(T_pow_e_coeff)); SCRATCH(r9);
  xmm3 = LD128(A(T_pow_e_coeff) + 16); SCRATCH(r9);
  SHL32(rcx, 12);
  XOR32(rcx, r8);
  AND32(rcx, 0xfff00000u);
  xmm6 = _mm_cvtsi64_si128((long long)rcx);
  xmm2 = ADDSD(xmm2, xmm4);
  r9 = 0x3fe62e42fefa39efull;
  xmm1 = _mm_cvtsi64_si128((long long)r9);
  xmm0 = _mm_shuffle_epi32(xmm2, 68);
  xmm4 = _mm_shuffle_epi32(xmm2, 68);
  xmm1 = MULSD(xmm1, xmm2);
  xmm6 = _mm_shuffle_epi32(xmm6, 17);
  xmm0 = MULPD(xmm0, xmm0);
  xmm7 = MULPD(xmm7, xmm4);
  xmm5 = _mm_add_epi32(xmm5, xmm6);
  xmm1 = MULSD(xmm1, xmm5);
  xmm6 = _mm_shuffle_epi32(xmm5, 238);
  xmm0 = MULSD(xmm0, xmm0);
  xmm3 = ADDPD(xmm3, xmm7);
  xmm1 = ADDSD(xmm1, xmm6);
  xmm0 = MULPD(xmm0, xmm3);
  xmm3 = _mm_shuffle_epi32(xmm0, 238);
  xmm0 = MULSD(xmm0, xmm5);
  xmm3 = MULSD(xmm3, xmm5);
  xmm0 = ADDSD(xmm0, xmm1);
  xmm0 = ADDSD(xmm0, xmm3);
  xmm0 = ADDSD(xmm0, xmm5);
  goto B1_5;
L_2TAG_PACKET_0_0_2:;
  ADD32(rax, 0x10u);
  rdx = 0x7ff0u;
  AND32(rdx, rax);
  CMP32(rdx, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_6_0_2;
  TEST32(rax, 0x8000u);
  if (!zf) goto L_2TAG_PACKET_7_0_2;
L_2TAG_PACKET_8_0_2:;
  xmm0 = LDQ(rsp + 8);
  xmm3 = LDQ(rsp + 8);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm3);
  xmm3 = _mm_srli_epi64(xmm3, 32);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm3);
  OR32(rdx, rcx);
  CMP32(rdx, 0x0u);
  if (zf) goto L_2TAG_PACKET_9_0_2;
  xmm3 = _mm_xor_si128(xmm3, xmm3);
  rax = 0x47f0u;
  xmm3 = _mm_insert_epi16(xmm3, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm3);
  xmm2 = _mm_xor_si128(xmm2, xmm2);
  rax = 0x3ff0u;
  xmm2 = _mm_insert_epi16(xmm2, (int)(uint16_t)rax, 3);
  xmm3 = xmm0;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  xmm0 = _mm_or_si128(xmm0, xmm2);
  rcx = 0x47f0u;
  xmm0 = _mm_srli_epi64(xmm0, 27);
  xmm2 = LDQ(A(T_pow_LOG2_E)); SCRATCH(r9);
  xmm0 = _mm_srli_epi32(xmm0, 2);
  xmm0 = RCPPS(xmm0, xmm0);
  xmm3 = _mm_slli_epi64(xmm3, 12);
  xmm6 = LD128(A(T_pow_HIGHSIGMASK)); SCRATCH(r9);
  xmm3 = _mm_srli_epi64(xmm3, 12);
  xmm0 = MULSS(xmm0, xmm7);
  rdx = 0xfffffc00u;
  xmm5 = _mm_cvtsi32_si128((int)(uint32_t)rdx);
  xmm3 = _mm_or_si128(xmm3, xmm1);
  xmm0 = _mm_add_epi32(xmm0, xmm4);
  xmm5 = _mm_slli_epi64(xmm5, 32);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  xmm0 = _mm_slli_epi64(xmm0, 29);
  xmm5 = _mm_and_si128(xmm5, xmm3);
  r8 = 0x0u;
  xmm0 = _mm_and_si128(xmm0, xmm6);
  xmm3 = SUBSD(xmm3, xmm5);
  AND32(rax, 0x7ff0u);
  SUB32(rax, 0x47f0u);
  SAR32(rax, 4);
  xmm7 = CVTSI2SDL(xmm7, rax);
  xmm5 = MULPD(xmm5, xmm0);
  goto L_2TAG_PACKET_4_0_2;
L_2TAG_PACKET_10_0_2:;
  xmm0 = LDQ(rsp + 8);
  xmm3 = LDQ(rsp + 8);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm3);
  xmm3 = _mm_srli_epi64(xmm3, 32);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm3);
  OR32(rdx, rcx);
  CMP32(rdx, 0x0u);
  if (zf) goto L_2TAG_PACKET_9_0_2;
  xmm3 = _mm_xor_si128(xmm3, xmm3);
  rax = 0x47f0u;
  xmm3 = _mm_insert_epi16(xmm3, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm3);
  xmm2 = _mm_xor_si128(xmm2, xmm2);
  rax = 0x3ff0u;
  xmm2 = _mm_insert_epi16(xmm2, (int)(uint16_t)rax, 3);
  xmm3 = xmm0;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  xmm0 = _mm_or_si128(xmm0, xmm2);
  rcx = 0x47f0u;
  xmm0 = _mm_srli_epi64(xmm0, 27);
  xmm2 = LDQ(A(T_pow_LOG2_E)); SCRATCH(r9);
  xmm0 = _mm_srli_epi32(xmm0, 2);
  xmm0 = RCPPS(xmm0, xmm0);
  xmm3 = _mm_slli_epi64(xmm3, 12);
  xmm6 = LD128(A(T_pow_HIGHSIGMASK)); SCRATCH(r9);
  xmm3 = _mm_srli_epi64(xmm3, 12);
  xmm0 = MULSS(xmm0, xmm7);
  rdx = 0xfffffc00u;
  xmm5 = _mm_cvtsi32_si128((int)(uint32_t)rdx);
  xmm3 = _mm_or_si128(xmm3, xmm1);
  xmm0 = _mm_add_epi32(xmm0, xmm4);
  xmm5 = _mm_slli_epi64(xmm5, 32);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  xmm0 = _mm_slli_epi64(xmm0, 29);
  xmm5 = _mm_and_si128(xmm5, xmm3);
  r8 = 0x80000000u;
  xmm0 = _mm_and_si128(xmm0, xmm6);
  xmm3 = SUBSD(xmm3, xmm5);
  AND32(rax, 0x7ff0u);
  SUB32(rax, 0x47f0u);
  SAR32(rax, 4);
  xmm7 = CVTSI2SDL(xmm7, rax);
  xmm5 = MULPD(xmm5, xmm0);
  goto L_2TAG_PACKET_4_0_2;
L_2TAG_PACKET_5_0_2:;
  CMP32(rax, 0x0u);
  if (sf != of) goto L_2TAG_PACKET_11_0_2;
  CMP32(rax, 0x2f0u);
  if (!cf) goto L_2TAG_PACKET_12_0_2;
  xmm0 = ADDSD(xmm0, xmm7);
  xmm2 = LDQ(A(T_pow_HALFMASK)); SCRATCH(r9);
  xmm3 = ADDPD(xmm3, xmm0);
  xmm6 = _mm_xor_si128(xmm6, xmm6);
  rax = 0x42b8u;
  xmm6 = _mm_insert_epi16(xmm6, (int)(uint16_t)rax, 3);
  xmm0 = _mm_shuffle_epi32(xmm3, 238);
  xmm0 = ADDSD(xmm0, xmm3);
  xmm3 = xmm5;
  xmm5 = ADDSD(xmm5, xmm0);
  xmm4 = xmm2;
  xmm3 = SUBSD(xmm3, xmm5);
  xmm7 = xmm5;
  xmm5 = _mm_and_si128(xmm5, xmm2);
  xmm2 = xmm1;
  xmm4 = _mm_and_si128(xmm4, xmm1);
  xmm7 = SUBSD(xmm7, xmm5);
  xmm0 = ADDSD(xmm0, xmm3);
  xmm1 = SUBSD(xmm1, xmm4);
  xmm4 = MULSD(xmm4, xmm5);
  xmm0 = ADDSD(xmm0, xmm7);
  xmm2 = MULSD(xmm2, xmm0);
  xmm7 = xmm6;
  xmm1 = MULSD(xmm1, xmm5);
  xmm6 = ADDSD(xmm6, xmm4);
  rax = (uint32_t)_mm_cvtsi128_si32(xmm6);
  xmm6 = SUBSD(xmm6, xmm7);
  r11 = A(T_pow_T_exp);
  xmm2 = ADDSD(xmm2, xmm1);
  xmm7 = LD128(A(T_pow_e_coeff)); SCRATCH(r9);
  xmm3 = LD128(A(T_pow_e_coeff) + 16); SCRATCH(r9);
  xmm4 = SUBSD(xmm4, xmm6);
  rdx = (uint16_t)_mm_extract_epi16(xmm6, 3);
  rcx = (uint32_t)rax;
  AND32(rax, 0xffu);
  ADD32(rax, rax);
  xmm5 = LD128(r11 + rax * 8);
  xmm2 = ADDSD(xmm2, xmm4);
  SAR32(rcx, 8);
  rax = (uint32_t)rcx;
  SAR32(rcx, 1);
  SUB32(rax, rcx);
  SHL32(rcx, 20);
  XOR32(rcx, r8);
  xmm6 = _mm_cvtsi32_si128((int)(uint32_t)rcx);
  xmm1 = LDQ(A(T_pow_e_coeff) + 32); SCRATCH(r9);
  AND32(rdx, 0x7fffu);
  CMP32(rdx, 0x4091u);
  if (!cf && !zf) goto L_2TAG_PACKET_12_0_2;
  xmm0 = _mm_shuffle_epi32(xmm2, 68);
  xmm4 = _mm_shuffle_epi32(xmm2, 68);
  xmm0 = MULPD(xmm0, xmm0);
  xmm7 = MULPD(xmm7, xmm4);
  xmm6 = _mm_shuffle_epi32(xmm6, 17);
  xmm1 = MULSD(xmm1, xmm2);
  xmm0 = MULSD(xmm0, xmm0);
  xmm5 = _mm_add_epi32(xmm5, xmm6);
  xmm3 = ADDPD(xmm3, xmm7);
  xmm1 = MULSD(xmm1, xmm5);
  xmm6 = _mm_shuffle_epi32(xmm5, 238);
  xmm0 = MULPD(xmm0, xmm3);
  xmm1 = ADDSD(xmm1, xmm6);
  xmm3 = _mm_shuffle_epi32(xmm0, 238);
  xmm0 = MULSD(xmm0, xmm5);
  xmm3 = MULSD(xmm3, xmm5);
  SHL32(rax, 4);
  xmm4 = _mm_xor_si128(xmm4, xmm4);
  ADD32(rax, 0x3ff0u);
  xmm4 = _mm_insert_epi16(xmm4, (int)(uint16_t)rax, 3);
  xmm0 = ADDSD(xmm0, xmm1);
  xmm0 = ADDSD(xmm0, xmm3);
  xmm1 = xmm0;
  xmm0 = ADDSD(xmm0, xmm5);
  xmm0 = MULSD(xmm0, xmm4);
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_13_0_2;
  CMP32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_14_0_2;
  goto B1_5;
L_2TAG_PACKET_6_0_2:;
  xmm1 = LDQ(rsp + 16);
  xmm0 = LDQ(rsp + 8);
  xmm2 = xmm0;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm2);
  xmm2 = _mm_srli_epi64(xmm2, 20);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm2);
  OR32(rax, rdx);
  if (zf) goto L_2TAG_PACKET_15_0_2;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm1 = _mm_srli_epi64(xmm1, 32);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  rcx = (uint32_t)rdx;
  ADD32(rdx, rdx);
  OR32(rax, rdx);
  if (zf) goto L_2TAG_PACKET_16_0_2;
  xmm0 = ADDSD(xmm0, xmm0);
  goto B1_5;
L_2TAG_PACKET_16_0_2:;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x3ff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  ST32(rsp, 0x1du);
  goto L_2TAG_PACKET_17_0_2;
L_2TAG_PACKET_18_0_2:;
  xmm0 = LDQ(rsp + 16);
  xmm0 = ADDPD(xmm0, xmm0);
  goto B1_5;
L_2TAG_PACKET_15_0_2:;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm2 = xmm1;
  xmm1 = _mm_srli_epi64(xmm1, 32);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  rcx = (uint32_t)rdx;
  ADD32(rdx, rdx);
  OR32(rax, rdx);
  if (zf) goto L_2TAG_PACKET_19_0_2;
  rax = (uint16_t)_mm_extract_epi16(xmm2, 3);
  AND32(rax, 0x7ff0u);
  CMP32(rax, 0x7ff0u);
  if (!zf) goto L_2TAG_PACKET_20_0_2;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm2);
  xmm2 = _mm_srli_epi64(xmm2, 20);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm2);
  OR32(rax, rdx);
  if (!zf) goto L_2TAG_PACKET_18_0_2;
L_2TAG_PACKET_20_0_2:;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  TEST32(rax, 0x8000u);
  if (!zf) goto L_2TAG_PACKET_21_0_2;
  TEST32(rcx, 0x80000000u);
  if (!zf) goto L_2TAG_PACKET_22_0_2;
  goto B1_5;
L_2TAG_PACKET_23_0_2:;
  xmm1 = LDQ(rsp + 16);
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  TEST32(rax, 0x1u);
  if (!zf) goto L_2TAG_PACKET_24_0_2;
  TEST32(rax, 0x2u);
  if (!zf) goto L_2TAG_PACKET_25_0_2;
  goto L_2TAG_PACKET_24_0_2;
L_2TAG_PACKET_21_0_2:;
  SHR32(rcx, 20);
  AND32(rcx, 0x7ffu);
  CMP32(rcx, 0x433u);
  if (!cf && !zf) goto L_2TAG_PACKET_24_0_2;
  if (zf) goto L_2TAG_PACKET_26_0_2;
  CMP32(rcx, 0x432u);
  if (!cf && !zf) goto L_2TAG_PACKET_23_0_2;
  CMP32(rcx, 0x3ffu);
  if (cf) goto L_2TAG_PACKET_24_0_2;
  xmm1 = LDQ(rsp + 16);
  rax = 0x4338u;
  xmm3 = _mm_xor_si128(xmm3, xmm3);
  xmm3 = _mm_insert_epi16(xmm3, (int)(uint16_t)rax, 3);
  xmm4 = xmm3;
  xmm3 = ADDSD(xmm3, xmm1);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm1 = ADDSD(xmm1, xmm4);
  rax = (uint16_t)_mm_extract_epi16(xmm1, 3);
  AND32(rax, 0x7ff0u);
  if (!zf) goto L_2TAG_PACKET_24_0_2;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm3);
  AND32(rax, 0x1u);
  if (zf) goto L_2TAG_PACKET_24_0_2;
L_2TAG_PACKET_25_0_2:;
  xmm1 = LDQ(rsp + 16);
  rax = (uint16_t)_mm_extract_epi16(xmm1, 3);
  AND32(rax, 0x8000u);
  if (!zf) goto L_2TAG_PACKET_27_0_2;
  goto B1_5;
L_2TAG_PACKET_27_0_2:;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x8000u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  goto B1_5;
L_2TAG_PACKET_24_0_2:;
  xmm1 = LDQ(rsp + 16);
  rax = (uint16_t)_mm_extract_epi16(xmm1, 3);
  AND32(rax, 0x8000u);
  if (!zf) goto L_2TAG_PACKET_22_0_2;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x7ff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  goto B1_5;
L_2TAG_PACKET_26_0_2:;
  xmm1 = LDQ(rsp + 16);
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  AND32(rax, 0x1u);
  if (zf) goto L_2TAG_PACKET_24_0_2;
  goto L_2TAG_PACKET_25_0_2;
L_2TAG_PACKET_28_0_2:;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm1 = _mm_srli_epi64(xmm1, 20);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  OR32(rax, rdx);
  if (zf) goto L_2TAG_PACKET_29_0_2;
  xmm0 = LDQ(rsp + 16);
  xmm0 = ADDSD(xmm0, xmm0);
  goto B1_5;
L_2TAG_PACKET_29_0_2:;
  xmm0 = LDQ(rsp + 8);
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  CMP32(rax, 0xbff0u);
  if (!zf) goto L_2TAG_PACKET_30_0_2;
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  xmm0 = _mm_srli_epi64(xmm0, 20);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  OR32(rcx, rdx);
  if (!zf) goto L_2TAG_PACKET_30_0_2;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x7ff8u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  goto B1_5;
L_2TAG_PACKET_30_0_2:;
  xmm1 = LDQ(rsp + 16);
  AND32(rax, 0x7ff0u);
  SUB32(rax, 0x3ff0u);
  rdx = (uint16_t)_mm_extract_epi16(xmm1, 3);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  XOR32(rax, rdx);
  AND32(rax, 0x8000u);
  if (zf) goto L_2TAG_PACKET_31_0_2;
  goto B1_5;
L_2TAG_PACKET_31_0_2:;
  rcx = 0x7ff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rcx, 3);
  goto B1_5;
L_2TAG_PACKET_32_0_2:;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  CMP32(rdx, 0x4320u);
  if (!cf && !zf) goto L_2TAG_PACKET_33_0_2;
  TEST32(rax, 0x1u);
  if (!zf) goto L_2TAG_PACKET_34_0_2;
  TEST32(rax, 0x2u);
  if (zf) goto L_2TAG_PACKET_35_0_2;
  goto L_2TAG_PACKET_36_0_2;
L_2TAG_PACKET_33_0_2:;
  TEST32(rax, 0x1u);
  if (zf) goto L_2TAG_PACKET_35_0_2;
  goto L_2TAG_PACKET_36_0_2;
L_2TAG_PACKET_7_0_2:;
  xmm2 = LDQ(rsp + 8);
  rax = (uint32_t)_mm_cvtsi128_si32(xmm2);
  xmm2 = _mm_srli_epi64(xmm2, 31);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm2);
  OR32(rax, rcx);
  if (zf) goto L_2TAG_PACKET_9_0_2;
  xmm1 = LDQ(rsp + 16);
  rdx = (uint16_t)_mm_extract_epi16(xmm1, 3);
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm2 = xmm1;
  xmm2 = _mm_srli_epi64(xmm2, 32);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm2);
  ADD32(rcx, rcx);
  OR32(rcx, rax);
  if (zf) goto L_2TAG_PACKET_37_0_2;
  AND32(rdx, 0x7ff0u);
  CMP32(rdx, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_28_0_2;
  CMP32(rdx, 0x4330u);
  if (!cf && !zf) goto L_2TAG_PACKET_35_0_2;
  CMP32(rdx, 0x4320u);
  if (!cf) goto L_2TAG_PACKET_32_0_2;
  CMP32(rdx, 0x3ff0u);
  if (cf) goto L_2TAG_PACKET_34_0_2;
  rax = 0x4338u;
  xmm2 = _mm_xor_si128(xmm2, xmm2);
  xmm2 = _mm_insert_epi16(xmm2, (int)(uint16_t)rax, 3);
  xmm4 = xmm2;
  xmm2 = ADDSD(xmm2, xmm1);
  xmm4 = SUBSD(xmm4, xmm2);
  xmm1 = ADDSD(xmm1, xmm4);
  rax = (uint16_t)_mm_extract_epi16(xmm1, 3);
  AND32(rax, 0x7fffu);
  if (!zf) goto L_2TAG_PACKET_34_0_2;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm2);
  AND32(rax, 0x1u);
  if (zf) goto L_2TAG_PACKET_35_0_2;
L_2TAG_PACKET_36_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  rdx = 0x77f0u;
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rdx, 3);
  xmm2 = LDQ(A(T_pow_LOG2_E)); SCRATCH(r9);
  xmm4 = LDQ(rsp + 8);
  rax = (uint16_t)_mm_extract_epi16(xmm4, 3);
  rdx = 0x2000u;
  xmm4 = _mm_cvtsi32_si128((int)(uint32_t)rdx);
  AND32(rax, 0x7fffu);
  SUB32(rax, 0x10u);
  if (sf != of) goto L_2TAG_PACKET_10_0_2;
  rdx = (uint32_t)rax;
  AND32(rdx, 0x7ff0u);
  SUB32(rdx, 0x3ff0u);
  rcx = (uint32_t)rdx;
  SAR32(rdx, 31);
  ADD32(rcx, rdx);
  XOR32(rcx, rdx);
  ADD32(rcx, 0x10u);
  BSRL(rcx, rcx);
  r8 = 0x80000000u;
  goto L_2TAG_PACKET_1_0_2;
L_2TAG_PACKET_34_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  rax = 0x7ff0u;
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rax, 3);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  xmm0 = MULSD(xmm0, xmm1);
  ST32(rsp, 0x1cu);
  goto L_2TAG_PACKET_17_0_2;
L_2TAG_PACKET_35_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  rdx = 0x77f0u;
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rdx, 3);
  xmm2 = LDQ(A(T_pow_LOG2_E)); SCRATCH(r9);
  xmm4 = LDQ(rsp + 8);
  rax = (uint16_t)_mm_extract_epi16(xmm4, 3);
  rdx = 0x2000u;
  xmm4 = _mm_cvtsi32_si128((int)(uint32_t)rdx);
  AND32(rax, 0x7fffu);
  SUB32(rax, 0x10u);
  if (sf != of) goto L_2TAG_PACKET_8_0_2;
  rdx = (uint32_t)rax;
  AND32(rdx, 0x7ff0u);
  SUB32(rdx, 0x3ff0u);
  rcx = (uint32_t)rdx;
  SAR32(rdx, 31);
  ADD32(rcx, rdx);
  XOR32(rcx, rdx);
  ADD32(rcx, 0x10u);
  BSRL(rcx, rcx);
  r8 = 0x0u;
  goto L_2TAG_PACKET_1_0_2;
L_2TAG_PACKET_19_0_2:;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x3ff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  goto B1_5;
L_2TAG_PACKET_22_0_2:;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  goto B1_5;
L_2TAG_PACKET_11_0_2:;
  ADD32(rax, 0x180u);
  CMP32(rax, 0x0u);
  if (sf != of) goto L_2TAG_PACKET_38_0_2;
  xmm5 = MULSD(xmm5, xmm1);
  xmm0 = ADDSD(xmm0, xmm7);
  SHR32(r8, 31);
  xmm3 = ADDPD(xmm3, xmm0);
  xmm0 = _mm_shuffle_epi32(xmm3, 238);
  xmm3 = ADDSD(xmm3, xmm0);
  r11 = A(T_pow_log2_pow);
  xmm4 = LDQ(r11 + r8 * 8);
  xmm1 = MULSD(xmm1, xmm3);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x3ff0u;
  SHL32(r8, 15);
  OR32(rax, r8);
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  xmm5 = ADDSD(xmm5, xmm1);
  xmm5 = MULSD(xmm5, xmm4);
  xmm0 = ADDSD(xmm0, xmm5);
  goto B1_5;
L_2TAG_PACKET_38_0_2:;
L_2TAG_PACKET_37_0_2:;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x3ff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  goto B1_5;
L_2TAG_PACKET_39_0_2:;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x3ff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  ST32(rsp, 0x1au);
  goto L_2TAG_PACKET_17_0_2;
L_2TAG_PACKET_9_0_2:;
  xmm1 = LDQ(rsp + 16);
  xmm2 = xmm1;
  rax = (uint16_t)_mm_extract_epi16(xmm1, 3);
  AND32(rax, 0x7ff0u);
  CMP32(rax, 0x7ff0u);
  if (!zf) goto L_2TAG_PACKET_40_0_2;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm2);
  xmm2 = _mm_srli_epi64(xmm2, 20);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm2);
  OR32(rax, rdx);
  if (!zf) goto L_2TAG_PACKET_18_0_2;
L_2TAG_PACKET_40_0_2:;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm1 = _mm_srli_epi64(xmm1, 32);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  rcx = (uint32_t)rdx;
  ADD32(rdx, rdx);
  OR32(rax, rdx);
  if (zf) goto L_2TAG_PACKET_39_0_2;
  SHR32(rdx, 21);
  CMP32(rdx, 0x433u);
  if (!cf && !zf) goto L_2TAG_PACKET_41_0_2;
  if (zf) goto L_2TAG_PACKET_42_0_2;
  CMP32(rdx, 0x3ffu);
  if (cf) goto L_2TAG_PACKET_41_0_2;
  xmm1 = LDQ(rsp + 16);
  rax = 0x4338u;
  xmm3 = _mm_xor_si128(xmm3, xmm3);
  xmm3 = _mm_insert_epi16(xmm3, (int)(uint16_t)rax, 3);
  xmm4 = xmm3;
  xmm3 = ADDSD(xmm3, xmm1);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm1 = ADDSD(xmm1, xmm4);
  rax = (uint16_t)_mm_extract_epi16(xmm1, 3);
  AND32(rax, 0x7ff0u);
  if (!zf) goto L_2TAG_PACKET_41_0_2;
  rax = (uint32_t)_mm_cvtsi128_si32(xmm3);
  AND32(rax, 0x1u);
  if (zf) goto L_2TAG_PACKET_41_0_2;
L_2TAG_PACKET_43_0_2:;
  xmm0 = LDQ(rsp + 8);
  TEST32(rcx, 0x80000000u);
  if (!zf) goto L_2TAG_PACKET_44_0_2;
  goto B1_5;
L_2TAG_PACKET_42_0_2:;
  xmm1 = LDQ(rsp + 16);
  rax = (uint32_t)_mm_cvtsi128_si32(xmm1);
  TEST32(rax, 0x1u);
  if (!zf) goto L_2TAG_PACKET_43_0_2;
L_2TAG_PACKET_41_0_2:;
  TEST32(rcx, 0x80000000u);
  if (zf) goto L_2TAG_PACKET_22_0_2;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
L_2TAG_PACKET_44_0_2:;
  rax = 0x3ff0u;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rax, 3);
  xmm1 = DIVSD(xmm1, xmm0);
  xmm0 = xmm1;
  ST32(rsp, 0x1bu);
  goto L_2TAG_PACKET_17_0_2;
L_2TAG_PACKET_12_0_2:;
  xmm2 = LDQ(rsp + 8);
  xmm6 = LDQ(rsp + 16);
  rax = (uint16_t)_mm_extract_epi16(xmm2, 3);
  rdx = (uint16_t)_mm_extract_epi16(xmm6, 3);
  rcx = 0x7ff0u;
  AND32(rcx, rdx);
  CMP32(rcx, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_45_0_2;
  AND32(rax, 0x7ff0u);
  SUB32(rax, 0x3ff0u);
  XOR32(rdx, rax);
  TEST32(rdx, 0x8000u);
  if (!zf) goto L_2TAG_PACKET_46_0_2;
L_2TAG_PACKET_47_0_2:;
  rax = 0x7fe0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  SHR32(r8, 16);
  OR32(rax, r8);
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm1);
L_2TAG_PACKET_14_0_2:;
  ST32(rsp, 0x18u);
  goto L_2TAG_PACKET_17_0_2;
L_2TAG_PACKET_46_0_2:;
  rax = 0x10u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm0);
  TEST32(r8, 0x80000000u);
  if (zf) goto L_2TAG_PACKET_48_0_2;
  r9 = 0x8000000000000000ull;
  xmm2 = _mm_cvtsi64_si128((long long)r9);
  xmm0 = _mm_xor_si128(xmm0, xmm2);
L_2TAG_PACKET_48_0_2:;
  ST32(rsp, 0x19u);
  goto L_2TAG_PACKET_17_0_2;
L_2TAG_PACKET_13_0_2:;
  rcx = (uint16_t)_mm_extract_epi16(xmm5, 3);
  rdx = (uint16_t)_mm_extract_epi16(xmm4, 3);
  rax = 0xffffffffu;
  AND32(rcx, 0x7ff0u);
  SUB32(rcx, 0x3ff0u);
  AND32(rdx, 0x7ff0u);
  ADD32(rdx, rcx);
  rcx = 0xffffffe1u;
  SAR32(rdx, 4);
  SUB32(rcx, rdx);
  if (zf || sf != of) goto L_2TAG_PACKET_49_0_2;
  CMP32(rcx, 0x14u);
  if (!cf && !zf) goto L_2TAG_PACKET_50_0_2;
  SHL32(rax, rcx);
L_2TAG_PACKET_49_0_2:;
  xmm0 = _mm_cvtsi32_si128((int)(uint32_t)rax);
  xmm0 = _mm_slli_epi64(xmm0, 32);
  xmm0 = _mm_and_si128(xmm0, xmm5);
  xmm5 = SUBSD(xmm5, xmm0);
  xmm5 = ADDSD(xmm5, xmm1);
  xmm0 = MULSD(xmm0, xmm4);
  xmm5 = MULSD(xmm5, xmm4);
  xmm0 = ADDSD(xmm0, xmm5);
L_2TAG_PACKET_50_0_2:;
  goto L_2TAG_PACKET_48_0_2;
L_2TAG_PACKET_2_0_2:;
  rcx = (rcx & ~0xffffull) | LD16(rsp + 22);
  rdx = 0x80000000u;
  xmm1 = _mm_cvtsi32_si128((int)(uint32_t)rdx);
  xmm7 = _mm_xor_si128(xmm7, xmm7);
  xmm0 = _mm_add_epi32(xmm0, xmm4);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  xmm0 = _mm_slli_epi64(xmm0, 29);
  xmm1 = _mm_add_epi64(xmm1, xmm3);
  xmm5 = _mm_and_si128(xmm5, xmm1);
  AND32(rcx, 0x7ff0u);
  CMP32(rcx, 0x40b0u);
  if (sf != of) goto L_2TAG_PACKET_3_0_2;
  xmm0 = _mm_and_si128(xmm0, xmm6);
  xmm3 = SUBSD(xmm3, xmm5);
  ADD32(rax, 0x3fdfu);
  SHR32(rax, 4);
  SUB32(rax, 0x3feu);
  xmm7 = CVTSI2SDL(xmm7, rax);
  xmm5 = MULPD(xmm5, xmm0);
  r11 = A(T_pow_L_tbl_pow);
  xmm4 = LDQ(A(T_pow_coeff_h)); SCRATCH(r9);
  xmm3 = MULSD(xmm3, xmm0);
  xmm6 = LDQ(A(T_pow_coeff_h)); SCRATCH(r9);
  xmm5 = SUBSD(xmm5, xmm2);
  xmm1 = LDQ(A(T_pow_coeff_h) + 8); SCRATCH(r9);
  xmm2 = _mm_shuffle_epi32(xmm3, 68);
  xmm5 = _mm_unpacklo_epi64(xmm5, xmm3);
  xmm3 = ADDSD(xmm3, xmm5);
  xmm0 = LDQ(A(T_pow_coeff_h) + 8); SCRATCH(r9);
  AND32(rdx, 0xffc000u);
  SHR32(rdx, 10);
  xmm7 = ADDPD(xmm7, LD128(r11 + rdx + (uint64_t)(int64_t)-3648));
  xmm4 = MULSD(xmm4, xmm5);
  xmm0 = MULSD(xmm0, xmm5);
  xmm6 = MULSD(xmm6, xmm2);
  xmm1 = MULSD(xmm1, xmm2);
  xmm2 = xmm5;
  xmm4 = MULSD(xmm4, xmm5);
  xmm5 = ADDSD(xmm5, xmm0);
  xmm0 = xmm7;
  xmm2 = ADDSD(xmm2, xmm3);
  xmm7 = ADDSD(xmm7, xmm5);
  xmm6 = MULSD(xmm6, xmm2);
  xmm0 = SUBSD(xmm0, xmm7);
  xmm2 = xmm7;
  xmm7 = ADDSD(xmm7, xmm4);
  xmm0 = ADDSD(xmm0, xmm5);
  xmm2 = SUBSD(xmm2, xmm7);
  xmm4 = ADDSD(xmm4, xmm2);
  xmm2 = _mm_shuffle_epi32(xmm5, 238);
  xmm5 = xmm7;
  xmm7 = ADDSD(xmm7, xmm2);
  xmm4 = ADDSD(xmm4, xmm0);
  xmm0 = LD128(A(T_pow_coeff_pow)); SCRATCH(r9);
  xmm5 = SUBSD(xmm5, xmm7);
  xmm6 = ADDSD(xmm6, xmm4);
  xmm4 = xmm7;
  xmm5 = ADDSD(xmm5, xmm2);
  xmm7 = ADDSD(xmm7, xmm1);
  xmm2 = LD128(A(T_pow_coeff_pow) + 64); SCRATCH(r9);
  xmm4 = SUBSD(xmm4, xmm7);
  xmm6 = ADDSD(xmm6, xmm5);
  xmm4 = ADDSD(xmm4, xmm1);
  xmm5 = _mm_shuffle_epi32(xmm7, 238);
  xmm1 = xmm7;
  xmm7 = ADDSD(xmm7, xmm5);
  xmm1 = SUBSD(xmm1, xmm7);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm5 = LD128(A(T_pow_coeff_pow) + 80); SCRATCH(r9);
  xmm3 = _mm_shuffle_epi32(xmm3, 68);
  xmm6 = ADDSD(xmm6, xmm4);
  xmm6 = ADDSD(xmm6, xmm1);
  xmm1 = LD128(A(T_pow_coeff_pow) + 32); SCRATCH(r9);
  xmm0 = MULPD(xmm0, xmm3);
  xmm2 = MULPD(xmm2, xmm3);
  xmm4 = _mm_shuffle_epi32(xmm3, 68);
  xmm3 = MULPD(xmm3, xmm3);
  xmm0 = ADDPD(xmm0, xmm1);
  xmm5 = ADDPD(xmm5, xmm2);
  xmm4 = MULSD(xmm4, xmm3);
  xmm2 = LDQ(A(T_pow_HIGHMASK_LOG_X)); SCRATCH(r9);
  xmm3 = MULPD(xmm3, xmm3);
  xmm1 = LDQ(rsp + 16);
  rcx = (rcx & ~0xffffull) | LD16(rsp + 22);
  xmm0 = MULPD(xmm0, xmm4);
  rax = (uint16_t)_mm_extract_epi16(xmm7, 3);
  xmm5 = MULPD(xmm5, xmm4);
  xmm0 = MULPD(xmm0, xmm3);
  xmm4 = LDQ(A(T_pow_HIGHMASK_Y) + 8); SCRATCH(r9);
  xmm2 = _mm_and_si128(xmm2, xmm7);
  xmm5 = ADDSD(xmm5, xmm6);
  xmm7 = SUBSD(xmm7, xmm2);
  xmm5 = ADDPD(xmm5, xmm0);
  AND32(rax, 0x7ff0u);
  SUB32(rax, 0x3ff0u);
  AND32(rcx, 0x7ff0u);
  CMP32(rcx, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_45_0_2;
  ADD32(rcx, rax);
  CMP32(rcx, 0x40c0u);
  if (!cf) goto L_2TAG_PACKET_51_0_2;
  xmm0 = _mm_shuffle_epi32(xmm5, 238);
  xmm4 = _mm_and_si128(xmm4, xmm1);
  xmm3 = xmm1;
  xmm5 = ADDSD(xmm5, xmm0);
  xmm1 = SUBSD(xmm1, xmm4);
  xmm6 = _mm_xor_si128(xmm6, xmm6);
  rdx = 0x42b8u;
  xmm6 = _mm_insert_epi16(xmm6, (int)(uint16_t)rdx, 3);
  xmm7 = ADDSD(xmm7, xmm5);
  xmm4 = MULSD(xmm4, xmm2);
  xmm1 = MULSD(xmm1, xmm2);
  xmm5 = xmm6;
  xmm3 = MULSD(xmm3, xmm7);
  xmm6 = ADDSD(xmm6, xmm4);
  xmm1 = ADDSD(xmm1, xmm3);
  xmm7 = LD128(A(T_pow_e_coeff)); SCRATCH(r9);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm6);
  xmm6 = SUBSD(xmm6, xmm5);
  r11 = A(T_pow_T_exp);
  xmm3 = LD128(A(T_pow_e_coeff) + 16); SCRATCH(r9);
  xmm2 = LDQ(A(T_pow_e_coeff) + 32); SCRATCH(r9);
  xmm4 = SUBSD(xmm4, xmm6);
  rcx = (uint32_t)rdx;
  AND32(rdx, 0xffu);
  ADD32(rdx, rdx);
  xmm5 = LD128(r11 + rdx * 8);
  xmm4 = ADDSD(xmm4, xmm1);
  rdx = (uint16_t)_mm_extract_epi16(xmm6, 3);
  SHR32(rcx, 8);
  rax = (uint32_t)rcx;
  SHR32(rcx, 1);
  SUB32(rax, rcx);
  SHL32(rcx, 20);
  xmm6 = _mm_cvtsi32_si128((int)(uint32_t)rcx);
  xmm0 = _mm_shuffle_epi32(xmm4, 68);
  xmm1 = _mm_shuffle_epi32(xmm4, 68);
  xmm0 = MULPD(xmm0, xmm0);
  xmm7 = MULPD(xmm7, xmm1);
  xmm6 = _mm_shuffle_epi32(xmm6, 17);
  xmm2 = MULSD(xmm2, xmm4);
  AND32(rdx, 0x7fffu);
  CMP32(rdx, 0x4091u);
  if (!cf && !zf) goto L_2TAG_PACKET_12_0_2;
  xmm0 = MULSD(xmm0, xmm0);
  xmm5 = _mm_add_epi32(xmm5, xmm6);
  xmm3 = ADDPD(xmm3, xmm7);
  xmm2 = MULSD(xmm2, xmm5);
  xmm6 = _mm_shuffle_epi32(xmm5, 238);
  xmm0 = MULPD(xmm0, xmm3);
  xmm2 = ADDSD(xmm2, xmm6);
  xmm3 = _mm_shuffle_epi32(xmm0, 238);
  ADD32(rax, 0x3ffu);
  SHL32(rax, 20);
  OR32(rax, r8);
  xmm4 = _mm_cvtsi32_si128((int)(uint32_t)rax);
  xmm0 = MULSD(xmm0, xmm5);
  xmm3 = MULSD(xmm3, xmm5);
  xmm0 = ADDSD(xmm0, xmm2);
  xmm4 = _mm_slli_epi64(xmm4, 32);
  xmm0 = ADDSD(xmm0, xmm3);
  xmm1 = xmm0;
  xmm0 = ADDSD(xmm0, xmm5);
  xmm0 = MULSD(xmm0, xmm4);
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_13_0_2;
  CMP32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_14_0_2;
/* L_2TAG_PACKET_52_0_2 */
  goto B1_5;
L_2TAG_PACKET_45_0_2:;
  xmm0 = LDQ(rsp + 8);
  xmm2 = _mm_xor_si128(xmm2, xmm2);
  rax = 0xbff0u;
  xmm2 = _mm_insert_epi16(xmm2, (int)(uint16_t)rax, 3);
  xmm2 = ADDSD(xmm2, xmm0);
  rax = (uint16_t)_mm_extract_epi16(xmm2, 3);
  CMP32(rax, 0x0u);
  if (!zf) goto L_2TAG_PACKET_53_0_2;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x7ff8u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  goto B1_5;
L_2TAG_PACKET_53_0_2:;
  xmm1 = LDQ(rsp + 16);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm3 = xmm1;
  xmm3 = _mm_srli_epi64(xmm3, 20);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm3);
  OR32(rcx, rdx);
  if (zf) goto L_2TAG_PACKET_54_0_2;
  xmm1 = ADDSD(xmm1, xmm1);
  xmm0 = xmm1;
  goto B1_5;
L_2TAG_PACKET_51_0_2:;
  rax = (uint16_t)_mm_extract_epi16(xmm1, 3);
  rcx = (uint16_t)_mm_extract_epi16(xmm2, 3);
  XOR32(rax, rcx);
  TEST32(rax, 0x8000u);
  if (zf) goto L_2TAG_PACKET_47_0_2;
  goto L_2TAG_PACKET_46_0_2;
L_2TAG_PACKET_54_0_2:;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7ff0u);
  rdx = (uint16_t)_mm_extract_epi16(xmm1, 3);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  SUB32(rax, 0x3ff0u);
  XOR32(rax, rdx);
  TEST32(rax, 0x8000u);
  if (zf) goto L_2TAG_PACKET_55_0_2;
  goto B1_5;
L_2TAG_PACKET_55_0_2:;
  rdx = 0x7ff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rdx, 3);
  goto B1_5;
L_2TAG_PACKET_17_0_2:;
  STQ(rsp + 24, xmm0);
/* B1_3 */
  xmm0 = LDQ(rsp + 24);
/* L_2TAG_PACKET_56_0_2 */
B1_5:;
  ADD64(rsp, 0x28ull);
  rsp = rbp; POP(rbp);
  RET(xmm0);
}

static double hs_exp(__m128i xmm0) {
  REGS_INIT;
  __m128i xmm1 = XINIT(1);
  __m128i xmm2 = XINIT(2);
  __m128i xmm3 = XINIT(3);
  __m128i xmm4 = XINIT(4);
  __m128i xmm5 = XINIT(5);
  __m128i xmm6 = XINIT(6);
  __m128i xmm7 = XINIT(7);
  STACK_INIT;
  PUSH(rbp); rbp = rsp;
  SUB64(rsp, 0x18ull);
  STQ(rsp + 8, xmm0);
  xmm0 = _mm_unpacklo_epi64(xmm0, xmm0);
  xmm1 = LD128(A(T_exp_cv)); SCRATCH(r11);
  xmm2 = LD128(A(T_exp_cv) + 16); SCRATCH(r11);
  xmm3 = LD128(A(T_exp_cv) + 32); SCRATCH(r11);
  xmm6 = LD128(A(T_k_SHIFTER)); SCRATCH(r11);
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7fffu);
  rdx = 0x408fu;
  SUB32(rdx, rax);
  SUB32(rax, 0x3c90u);
  OR32(rdx, rax);
  CMP32(rdx, 0x80000000u);
  if (!cf) goto L_2TAG_PACKET_0_0_2;
  xmm1 = MULPD(xmm1, xmm0);
  xmm1 = ADDPD(xmm1, xmm6);
  xmm7 = xmm1;
  xmm1 = SUBPD(xmm1, xmm6);
  xmm2 = MULPD(xmm2, xmm1);
  xmm4 = LD128(A(T_exp_cv) + 64); SCRATCH(r11);
  xmm3 = MULPD(xmm3, xmm1);
  xmm5 = LD128(A(T_exp_cv) + 80); SCRATCH(r11);
  xmm0 = SUBPD(xmm0, xmm2);
  rax = (uint32_t)_mm_cvtsi128_si32(xmm7);
  rcx = (uint32_t)rax;
  AND32(rcx, 0x3fu);
  SHL32(rcx, 4);
  SAR32(rax, 6);
  rdx = (uint32_t)rax;
  xmm6 = LD128(A(T_exp_mmask)); SCRATCH(r11);
  xmm7 = _mm_and_si128(xmm7, xmm6);
  xmm6 = LD128(A(T_exp_bias)); SCRATCH(r11);
  xmm7 = _mm_add_epi64(xmm7, xmm6);
  xmm7 = _mm_slli_epi64(xmm7, 46);
  xmm0 = SUBPD(xmm0, xmm3);
  r11 = A(T_exp_Tbl_addr);
  xmm2 = LD128(rcx + r11);
  xmm4 = MULPD(xmm4, xmm0);
  xmm6 = xmm0;
  xmm1 = xmm0;
  xmm6 = MULPD(xmm6, xmm6);
  xmm0 = MULPD(xmm0, xmm6);
  xmm5 = ADDPD(xmm5, xmm4);
  xmm0 = MULSD(xmm0, xmm6);
  xmm6 = MULPD(xmm6, LD128(A(T_exp_cv) + 48)); SCRATCH(r11);
  xmm1 = ADDSD(xmm1, xmm2);
  xmm2 = _mm_unpackhi_epi64(xmm2, xmm2);
  xmm0 = MULPD(xmm0, xmm5);
  xmm1 = ADDSD(xmm1, xmm0);
  xmm2 = _mm_or_si128(xmm2, xmm7);
  xmm0 = _mm_unpackhi_epi64(xmm0, xmm0);
  xmm0 = ADDSD(xmm0, xmm1);
  xmm0 = ADDSD(xmm0, xmm6);
  ADD32(rdx, 0x37eu);
  CMP32(rdx, 0x77cu);
  if (!cf && !zf) goto L_2TAG_PACKET_1_0_2;
  xmm0 = MULSD(xmm0, xmm2);
  xmm0 = ADDSD(xmm0, xmm2);
  goto B1_5;
L_2TAG_PACKET_1_0_2:;
  xmm3 = _mm_xor_si128(xmm3, xmm3);
  xmm4 = LD128(A(T_exp_ALLONES)); SCRATCH(r11);
  rdx = 0xfffffc02u;
  SUB32(rdx, rax);
  xmm5 = _mm_cvtsi32_si128((int)(uint32_t)rdx);
  xmm4 = _mm_sll_epi64(xmm4, xmm5);
  rcx = (uint32_t)rax;
  SAR32(rax, 1);
  xmm3 = _mm_insert_epi16(xmm3, (int)(uint16_t)rax, 3);
  xmm6 = LD128(A(T_exp_ebias)); SCRATCH(r11);
  xmm3 = _mm_slli_epi64(xmm3, 4);
  xmm2 = _mm_sub_epi32(xmm2, xmm3);
  xmm0 = MULSD(xmm0, xmm2);
  CMP32(rdx, 0x34u);
  if (!zf && sf == of) goto L_2TAG_PACKET_2_0_2;
  xmm4 = _mm_and_si128(xmm4, xmm2);
  xmm3 = _mm_add_epi32(xmm3, xmm6);
  xmm2 = SUBSD(xmm2, xmm4);
  xmm0 = ADDSD(xmm0, xmm2);
  CMP32(rcx, 0x3ffu);
  if (sf == of) goto L_2TAG_PACKET_3_0_2;
  rcx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rcx, 0x8000u);
  OR32(rdx, rcx);
  CMP32(rdx, 0x0u);
  if (zf) goto L_2TAG_PACKET_4_0_2;
  xmm6 = xmm0;
  xmm0 = ADDSD(xmm0, xmm4);
  xmm0 = MULSD(xmm0, xmm3);
  rcx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rcx, 0x7ff0u);
  CMP32(rcx, 0x0u);
  if (zf) goto L_2TAG_PACKET_5_0_2;
  goto B1_5;
L_2TAG_PACKET_5_0_2:;
  xmm6 = MULSD(xmm6, xmm3);
  xmm4 = MULSD(xmm4, xmm3);
  xmm0 = xmm6;
  xmm6 = _mm_xor_si128(xmm6, xmm4);
  xmm6 = _mm_srai_epi32(xmm6, 31);
  xmm6 = _mm_shuffle_epi32(xmm6, 85);
  xmm0 = _mm_slli_epi64(xmm0, 1);
  xmm0 = _mm_srli_epi64(xmm0, 1);
  xmm0 = _mm_xor_si128(xmm0, xmm6);
  xmm6 = _mm_srli_epi64(xmm6, 63);
  xmm0 = _mm_add_epi64(xmm0, xmm6);
  xmm0 = _mm_add_epi64(xmm0, xmm4);
  ST32(rsp, 0xfu);
  goto L_2TAG_PACKET_6_0_2;
L_2TAG_PACKET_4_0_2:;
  xmm0 = ADDSD(xmm0, xmm4);
  xmm0 = MULSD(xmm0, xmm3);
  goto B1_5;
L_2TAG_PACKET_3_0_2:;
  xmm0 = ADDSD(xmm0, xmm4);
  xmm0 = MULSD(xmm0, xmm3);
  rcx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rcx, 0x7ff0u);
  CMP32(rcx, 0x7ff0u);
  if (!cf) goto L_2TAG_PACKET_7_0_2;
  goto B1_5;
L_2TAG_PACKET_2_0_2:;
  xmm3 = _mm_add_epi32(xmm3, xmm6);
  xmm0 = ADDPD(xmm0, xmm2);
  xmm0 = MULSD(xmm0, xmm3);
  ST32(rsp, 0xfu);
  goto L_2TAG_PACKET_6_0_2;
L_2TAG_PACKET_8_0_2:;
  CMP32(rax, 0x7ff00000u);
  if (!cf) goto L_2TAG_PACKET_9_0_2;
  rax = LD32(rsp + 12);
  CMP32(rax, 0x80000000u);
  if (!cf) goto L_2TAG_PACKET_10_0_2;
  xmm0 = LDQ(A(T_exp_XMAX)); SCRATCH(r11);
  xmm0 = MULSD(xmm0, xmm0);
L_2TAG_PACKET_7_0_2:;
  ST32(rsp, 0xeu);
  goto L_2TAG_PACKET_6_0_2;
L_2TAG_PACKET_10_0_2:;
  xmm0 = LDQ(A(T_exp_XMIN)); SCRATCH(r11);
  xmm0 = MULSD(xmm0, xmm0);
  ST32(rsp, 0xfu);
  goto L_2TAG_PACKET_6_0_2;
L_2TAG_PACKET_9_0_2:;
  rdx = LD32(rsp + 8);
  CMP32(rax, 0x7ff00000u);
  if (!cf && !zf) goto L_2TAG_PACKET_11_0_2;
  CMP32(rdx, 0x0u);
  if (!zf) goto L_2TAG_PACKET_11_0_2;
  rax = LD32(rsp + 12);
  CMP32(rax, 0x7ff00000u);
  if (!zf) goto L_2TAG_PACKET_12_0_2;
  xmm0 = LDQ(A(T_exp_INF)); SCRATCH(r11);
  goto B1_5;
L_2TAG_PACKET_12_0_2:;
  xmm0 = LDQ(A(T_k_ZERO)); SCRATCH(r11);
  goto B1_5;
L_2TAG_PACKET_11_0_2:;
  xmm0 = LDQ(rsp + 8);
  xmm0 = ADDSD(xmm0, xmm0);
  goto B1_5;
L_2TAG_PACKET_0_0_2:;
  rax = LD32(rsp + 12);
  AND32(rax, 0x7fffffffu);
  CMP32(rax, 0x40900000u);
  if (!cf) goto L_2TAG_PACKET_8_0_2;
  STQ(rsp + 8, xmm0);
  xmm0 = ADDSD(xmm0, LDQ(A(T_k_ONE))); SCRATCH(r11);
  goto B1_5;
L_2TAG_PACKET_6_0_2:;
  STQ(rsp + 16, xmm0);
/* B1_3 */
  xmm0 = LDQ(rsp + 16);
B1_5:;
  ADD64(rsp, 0x18ull);
  rsp = rbp; POP(rbp);
  RET(xmm0);
}

static double hs_log(__m128i xmm0) {
  REGS_INIT;
  __m128i xmm1 = XINIT(1);
  __m128i xmm2 = XINIT(2);
  __m128i xmm3 = XINIT(3);
  __m128i xmm4 = XINIT(4);
  __m128i xmm5 = XINIT(5);
  __m128i xmm6 = XINIT(6);
  __m128i xmm7 = XINIT(7);
  STACK_INIT;
  PUSH(rbp); rbp = rsp;
  SUB64(rsp, 0x18ull);
  STQ(rsp, xmm0);
  rax = 0x3ff0000000000000ull;
  xmm2 = _mm_cvtsi64_si128((long long)rax);
  rdx = 0x77f0000000000000ull;
  xmm3 = _mm_cvtsi64_si128((long long)rdx);
  rcx = 0x8000u;
  xmm4 = _mm_cvtsi32_si128((int)(uint32_t)rcx);
  r11 = 0xffffe00000000000ull;
  xmm5 = _mm_cvtsi64_si128((long long)r11);
  xmm1 = xmm0;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  xmm0 = _mm_or_si128(xmm0, xmm2);
  rcx = 0x3fe0u;
  xmm0 = _mm_srli_epi64(xmm0, 27);
  r8 = A(T_log_L_tbl);
  xmm0 = _mm_srli_epi32(xmm0, 2);
  xmm0 = RCPPS(xmm0, xmm0);
  xmm1 = _mm_slli_epi64(xmm1, 12);
  xmm6 = _mm_shuffle_epi32(xmm5, 228);
  xmm1 = _mm_srli_epi64(xmm1, 12);
  SUB32(rax, 0x10u);
  CMP32(rax, 0x7fe0u);
  if (!cf) goto L_2TAG_PACKET_0_0_2;
L_2TAG_PACKET_1_0_2:;
  xmm0 = _mm_add_epi32(xmm0, xmm4);
  xmm1 = _mm_or_si128(xmm1, xmm3);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  xmm0 = _mm_slli_epi64(xmm0, 29);
  xmm5 = _mm_and_si128(xmm5, xmm1);
  xmm0 = _mm_and_si128(xmm0, xmm6);
  xmm1 = SUBSD(xmm1, xmm5);
  xmm5 = MULPD(xmm5, xmm0);
  AND32(rax, 0x7ff0u);
  SUB32(rax, rcx);
  xmm7 = CVTSI2SDL(xmm7, rax);
  xmm1 = MULSD(xmm1, xmm0);
  xmm6 = LDQ(A(T_log_log2)); SCRATCH(r11);
  xmm3 = LD128(A(T_log_coeff)); SCRATCH(r11);
  xmm5 = SUBSD(xmm5, xmm2);
  AND32(rdx, 0xff0000u);
  SHR32(rdx, 12);
  xmm0 = LD128(r8 + rdx);
  xmm4 = LD128(A(T_log_coeff) + 16); SCRATCH(r11);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm2 = LD128(A(T_log_coeff) + 32); SCRATCH(r11);
  xmm6 = MULSD(xmm6, xmm7);
  xmm5 = _mm_unpacklo_epi64(xmm1, xmm1);
  xmm7 = MULSD(xmm7, LDQ(A(T_log_log2) + 8)); SCRATCH(r11);
  xmm3 = MULSD(xmm3, xmm1);
  xmm0 = ADDSD(xmm0, xmm6);
  xmm4 = MULPD(xmm4, xmm5);
  xmm5 = MULPD(xmm5, xmm5);
  xmm6 = _mm_unpacklo_epi64(xmm0, xmm0);
  xmm0 = ADDSD(xmm0, xmm1);
  xmm4 = ADDPD(xmm4, xmm2);
  xmm3 = MULPD(xmm3, xmm5);
  xmm6 = SUBSD(xmm6, xmm0);
  xmm4 = MULSD(xmm4, xmm1);
  xmm2 = _mm_shuffle_epi32(xmm0, 238);
  xmm1 = ADDSD(xmm1, xmm6);
  xmm5 = MULSD(xmm5, xmm5);
  xmm7 = ADDSD(xmm7, xmm2);
  xmm4 = ADDPD(xmm4, xmm3);
  xmm1 = ADDSD(xmm1, xmm7);
  xmm4 = MULPD(xmm4, xmm5);
  xmm1 = ADDSD(xmm1, xmm4);
  xmm5 = _mm_shuffle_epi32(xmm4, 238);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm0 = ADDSD(xmm0, xmm1);
  goto B1_5;
L_2TAG_PACKET_0_0_2:;
  xmm0 = LDQ(rsp);
  xmm1 = LDQ(rsp);
  ADD32(rax, 0x10u);
  CMP32(rax, 0x8000u);
  if (!cf) goto L_2TAG_PACKET_2_0_2;
  CMP32(rax, 0x10u);
  if (cf) goto L_2TAG_PACKET_3_0_2;
L_2TAG_PACKET_4_0_2:;
  xmm0 = ADDSD(xmm0, xmm0);
  goto B1_5;
L_2TAG_PACKET_5_0_2:;
  if (!cf && !zf) goto L_2TAG_PACKET_4_0_2;
  CMP32(rdx, 0x0u);
  if (!cf && !zf) goto L_2TAG_PACKET_4_0_2;
  goto L_2TAG_PACKET_6_0_2;
L_2TAG_PACKET_3_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  xmm1 = ADDSD(xmm1, xmm0);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm1 = _mm_srli_epi64(xmm1, 32);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  OR32(rdx, rcx);
  CMP32(rdx, 0x0u);
  if (zf) goto L_2TAG_PACKET_7_0_2;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  rax = 0x47f0u;
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm1);
  xmm1 = xmm0;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  xmm0 = _mm_or_si128(xmm0, xmm2);
  xmm0 = _mm_srli_epi64(xmm0, 27);
  rcx = 0x47f0u;
  xmm0 = _mm_srli_epi32(xmm0, 2);
  xmm0 = RCPPS(xmm0, xmm0);
  xmm1 = _mm_slli_epi64(xmm1, 12);
  xmm6 = _mm_shuffle_epi32(xmm5, 228);
  xmm1 = _mm_srli_epi64(xmm1, 12);
  goto L_2TAG_PACKET_1_0_2;
L_2TAG_PACKET_2_0_2:;
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm1 = _mm_srli_epi64(xmm1, 32);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  ADD32(rcx, rcx);
  CMP32(rcx, 0xffe00000u);
  if (!cf) goto L_2TAG_PACKET_5_0_2;
  OR32(rdx, rcx);
  CMP32(rdx, 0x0u);
  if (zf) goto L_2TAG_PACKET_7_0_2;
L_2TAG_PACKET_6_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x7ff0u;
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm1);
  ST32(rsp + 16, 0x3u);
  goto L_2TAG_PACKET_8_0_2;
L_2TAG_PACKET_7_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0xbff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  xmm0 = DIVSD(xmm0, xmm1);
  ST32(rsp + 16, 0x2u);
L_2TAG_PACKET_8_0_2:;
  STQ(rsp + 8, xmm0);
/* B1_3 */
  xmm0 = LDQ(rsp + 8);
B1_5:;
  ADD64(rsp, 0x18ull);
  rsp = rbp; POP(rbp);
  RET(xmm0);
}

static double hs_log10(__m128i xmm0) {
  REGS_INIT;
  __m128i xmm1 = XINIT(1);
  __m128i xmm2 = XINIT(2);
  __m128i xmm3 = XINIT(3);
  __m128i xmm4 = XINIT(4);
  __m128i xmm5 = XINIT(5);
  __m128i xmm6 = XINIT(6);
  __m128i xmm7 = XINIT(7);
  STACK_INIT;
  PUSH(rbp); rbp = rsp;
  SUB64(rsp, 0x18ull);
  STQ(rsp, xmm0);
/* B1_2 */
  xmm2 = _mm_xor_si128(xmm2, xmm2);
  rax = 0x3ff0u;
  xmm2 = _mm_insert_epi16(xmm2, (int)(uint16_t)rax, 3);
  rcx = 0x3ede0000u;
  xmm7 = _mm_cvtsi32_si128((int)(uint32_t)rcx);
  xmm3 = _mm_xor_si128(xmm3, xmm3);
  rdx = 0x77f0u;
  xmm3 = _mm_insert_epi16(xmm3, (int)(uint16_t)rdx, 3);
  xmm1 = xmm0;
  rdx = 0x8000u;
  xmm4 = _mm_cvtsi32_si128((int)(uint32_t)rdx);
  xmm5 = LD128(A(T_log_HIGHSIGMASK_log10)); SCRATCH(r8);
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  xmm0 = _mm_or_si128(xmm0, xmm2);
  rcx = 0x3fe0u;
  xmm0 = _mm_srli_epi64(xmm0, 27);
  xmm2 = LD128(A(T_log_LOG10_E)); SCRATCH(r8);
  xmm0 = _mm_srli_epi32(xmm0, 2);
  xmm0 = RCPPS(xmm0, xmm0);
  xmm1 = _mm_slli_epi64(xmm1, 12);
  xmm6 = _mm_shuffle_epi32(xmm5, 78);
  xmm1 = _mm_srli_epi64(xmm1, 12);
  SUB32(rax, 0x10u);
  CMP32(rax, 0x7fe0u);
  if (!cf) goto L_2TAG_PACKET_0_0_2;
L_2TAG_PACKET_1_0_2:;
  xmm0 = MULSS(xmm0, xmm7);
  xmm1 = _mm_or_si128(xmm1, xmm3);
  r11 = A(T_log_L_tbl_log10);
  xmm5 = _mm_and_si128(xmm5, xmm1);
  xmm0 = _mm_add_epi32(xmm0, xmm4);
  xmm1 = SUBSD(xmm1, xmm5);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm0);
  xmm0 = _mm_slli_epi64(xmm0, 29);
  xmm0 = _mm_and_si128(xmm0, xmm6);
  AND32(rax, 0x7ff0u);
  SUB32(rax, rcx);
  xmm7 = CVTSI2SDL(xmm7, rax);
  xmm5 = MULPD(xmm5, xmm0);
  xmm1 = MULSD(xmm1, xmm0);
  xmm6 = LDQ(A(T_log_log2_log10)); SCRATCH(r8);
  xmm3 = LD128(A(T_log_coeff_log10)); SCRATCH(r8);
  xmm5 = SUBSD(xmm5, xmm2);
  AND32(rdx, 0xff0000u);
  SHR32(rdx, 12);
  xmm0 = LD128(r11 + rdx + (uint64_t)(int64_t)-1504);
  xmm4 = LD128(A(T_log_coeff_log10) + 16); SCRATCH(r8);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm2 = LD128(A(T_log_coeff_log10) + 32); SCRATCH(r8);
  xmm6 = MULSD(xmm6, xmm7);
  xmm5 = _mm_shuffle_epi32(xmm1, 68);
  xmm7 = MULSD(xmm7, LDQ(A(T_log_log2_log10) + 8)); SCRATCH(r8);
  xmm3 = MULSD(xmm3, xmm1);
  xmm0 = ADDSD(xmm0, xmm6);
  xmm4 = MULPD(xmm4, xmm5);
  xmm6 = LDQ(A(T_log_LOG10_E) + 8); SCRATCH(r8);
  xmm5 = MULPD(xmm5, xmm5);
  xmm4 = ADDPD(xmm4, xmm2);
  xmm3 = MULPD(xmm3, xmm5);
  xmm2 = _mm_shuffle_epi32(xmm0, 228);
  xmm0 = ADDSD(xmm0, xmm1);
  xmm4 = MULSD(xmm4, xmm1);
  xmm2 = SUBSD(xmm2, xmm0);
  xmm6 = MULSD(xmm6, xmm1);
  xmm1 = ADDSD(xmm1, xmm2);
  xmm2 = _mm_shuffle_epi32(xmm0, 238);
  xmm5 = MULSD(xmm5, xmm5);
  xmm7 = ADDSD(xmm7, xmm2);
  xmm1 = ADDSD(xmm1, xmm6);
  xmm4 = ADDPD(xmm4, xmm3);
  xmm1 = ADDSD(xmm1, xmm7);
  xmm4 = MULPD(xmm4, xmm5);
  xmm1 = ADDSD(xmm1, xmm4);
  xmm5 = _mm_shuffle_epi32(xmm4, 238);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm0 = ADDSD(xmm0, xmm1);
  goto B1_5;
L_2TAG_PACKET_0_0_2:;
  xmm0 = LDQ(rsp);
  xmm1 = LDQ(rsp);
  ADD32(rax, 0x10u);
  CMP32(rax, 0x8000u);
  if (!cf) goto L_2TAG_PACKET_2_0_2;
  CMP32(rax, 0x10u);
  if (cf) goto L_2TAG_PACKET_3_0_2;
L_2TAG_PACKET_4_0_2:;
  xmm0 = ADDSD(xmm0, xmm0);
  goto B1_5;
L_2TAG_PACKET_5_0_2:;
  if (!cf && !zf) goto L_2TAG_PACKET_4_0_2;
  CMP32(rdx, 0x0u);
  if (!cf && !zf) goto L_2TAG_PACKET_4_0_2;
  goto L_2TAG_PACKET_6_0_2;
L_2TAG_PACKET_3_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  xmm1 = ADDSD(xmm1, xmm0);
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm1 = _mm_srli_epi64(xmm1, 32);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  OR32(rdx, rcx);
  CMP32(rdx, 0x0u);
  if (zf) goto L_2TAG_PACKET_7_0_2;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  rax = 0x47f0u;
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm1);
  xmm2 = _mm_xor_si128(xmm2, xmm2);
  rax = 0x3ff0u;
  xmm2 = _mm_insert_epi16(xmm2, (int)(uint16_t)rax, 3);
  xmm1 = xmm0;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  xmm0 = _mm_or_si128(xmm0, xmm2);
  rcx = 0x47f0u;
  xmm0 = _mm_srli_epi64(xmm0, 27);
  xmm2 = LD128(A(T_log_LOG10_E)); SCRATCH(r8);
  xmm0 = _mm_srli_epi32(xmm0, 2);
  xmm0 = RCPPS(xmm0, xmm0);
  xmm1 = _mm_slli_epi64(xmm1, 12);
  xmm6 = _mm_shuffle_epi32(xmm5, 78);
  xmm1 = _mm_srli_epi64(xmm1, 12);
  goto L_2TAG_PACKET_1_0_2;
L_2TAG_PACKET_2_0_2:;
  rdx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  xmm1 = _mm_srli_epi64(xmm1, 32);
  rcx = (uint32_t)_mm_cvtsi128_si32(xmm1);
  ADD32(rcx, rcx);
  CMP32(rcx, 0xffe00000u);
  if (!cf) goto L_2TAG_PACKET_5_0_2;
  OR32(rdx, rcx);
  CMP32(rdx, 0x0u);
  if (zf) goto L_2TAG_PACKET_7_0_2;
L_2TAG_PACKET_6_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0x7ff0u;
  xmm1 = _mm_insert_epi16(xmm1, (int)(uint16_t)rax, 3);
  xmm0 = MULSD(xmm0, xmm1);
  ST32(rsp + 16, 0x9u);
  goto L_2TAG_PACKET_8_0_2;
L_2TAG_PACKET_7_0_2:;
  xmm1 = _mm_xor_si128(xmm1, xmm1);
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  rax = 0xbff0u;
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  xmm0 = DIVSD(xmm0, xmm1);
  ST32(rsp + 16, 0x8u);
L_2TAG_PACKET_8_0_2:;
  STQ(rsp + 8, xmm0);
/* B1_3 */
  xmm0 = LDQ(rsp + 8);
/* L_2TAG_PACKET_9_0_2 */
B1_5:;
  ADD64(rsp, 0x18ull);
  rsp = rbp; POP(rbp);
  RET(xmm0);
}

static double hs_sin(__m128i xmm0) {
  REGS_INIT;
  __m128i xmm1 = XINIT(1);
  __m128i xmm2 = XINIT(2);
  __m128i xmm3 = XINIT(3);
  __m128i xmm4 = XINIT(4);
  __m128i xmm5 = XINIT(5);
  __m128i xmm6 = XINIT(6);
  __m128i xmm7 = XINIT(7);
  STACK_INIT;
  PUSH(rbp); rbp = rsp;
  PUSH(rbx);
  SUB64(rsp, 0x10ull);
  STQ(rsp + 8, xmm0);
  rax = LD32(rsp + 12);
  xmm1 = LDQ(A(T_k_PI32INV)); SCRATCH(r8);
  xmm2 = LDQ(A(T_k_SHIFTER)); SCRATCH(r8);
  AND32(rax, 0x7fff0000u);
  SUB32(rax, 0x30300000u);
  CMP32(rax, 0x10c50000u);
  if (!cf && !zf) goto L_2TAG_PACKET_0_0_1;
  xmm1 = MULSD(xmm1, xmm0);
  xmm5 = LD128(A(T_k_ONEHALF)); SCRATCH(r8);
  xmm4 = LDQ(A(T_k_SIGN_MASK)); SCRATCH(r8);
  xmm4 = _mm_and_si128(xmm4, xmm0);
  xmm5 = _mm_or_si128(xmm5, xmm4);
  xmm1 = ADDPD(xmm1, xmm5);
  rdx = CVTTSD2SIL(xmm1);
  xmm1 = CVTSI2SDL(xmm1, rdx);
  xmm6 = LD128(A(T_k_P_2)); SCRATCH(r8);
  r8 = 0x3fb921fb54400000ull;
  xmm3 = _mm_cvtsi64_si128((long long)r8);
  xmm5 = LD128(A(T_k_SC_4)); SCRATCH(r8);
  xmm4 = _mm_shuffle_epi32(xmm0, 68);
  xmm3 = MULSD(xmm3, xmm1);
  xmm1 = _mm_unpacklo_epi64(xmm1, xmm1);
  AND32(rdx, 0x3fu);
  SHL32(rdx, 5);
  rax = A(T_k_Ctable);
  ADD64(rax, rdx);
  xmm6 = MULPD(xmm6, xmm1);
  xmm1 = MULSD(xmm1, LDQ(A(T_k_P_3))); SCRATCH(r8);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm7 = LDQ(rax + 8);
  xmm0 = SUBSD(xmm0, xmm3);
  xmm3 = _mm_unpacklo_epi64(xmm4, xmm4);
  xmm4 = SUBSD(xmm4, xmm6);
  xmm0 = _mm_shuffle_epi32(xmm0, 68);
  xmm2 = LD128(rax);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = SUBPD(xmm0, xmm6);
  xmm7 = MULSD(xmm7, xmm4);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm3 = SUBSD(xmm3, xmm6);
  xmm6 = LD128(A(T_k_SC_2)); SCRATCH(r8);
  xmm1 = SUBSD(xmm1, xmm3);
  xmm3 = LDQ(rax + 24);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm7 = SUBSD(xmm7, xmm2);
  xmm2 = MULSD(xmm2, xmm4);
  xmm6 = MULPD(xmm6, xmm0);
  xmm3 = MULSD(xmm3, xmm4);
  xmm2 = MULPD(xmm2, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm5 = ADDPD(xmm5, LD128(A(T_k_SC_3))); SCRATCH(r8);
  xmm4 = MULSD(xmm4, LDQ(rax));
  xmm6 = ADDPD(xmm6, LD128(A(T_k_SC_1))); SCRATCH(r8);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = xmm3;
  xmm3 = ADDSD(xmm3, LDQ(rax + 8));
  xmm1 = MULPD(xmm1, xmm7);
  xmm7 = xmm4;
  xmm4 = ADDSD(xmm4, xmm3);
  xmm6 = ADDPD(xmm6, xmm5);
  xmm5 = LDQ(rax + 8);
  xmm5 = SUBSD(xmm5, xmm3);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm1 = ADDSD(xmm1, LDQ(rax + 16));
  xmm6 = MULPD(xmm6, xmm2);
  xmm5 = ADDSD(xmm5, xmm0);
  xmm3 = ADDSD(xmm3, xmm7);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm1 = ADDSD(xmm1, xmm3);
  xmm1 = ADDSD(xmm1, xmm6);
  xmm6 = _mm_unpackhi_epi64(xmm6, xmm6);
  xmm0 = xmm4;
  xmm1 = ADDSD(xmm1, xmm6);
  xmm0 = ADDSD(xmm0, xmm1);
  goto B1_4;
L_2TAG_PACKET_0_0_1:;
  if (!zf && sf == of) goto L_2TAG_PACKET_1_0_1;
  SHR32(rax, 20);
  CMP32(rax, 0xcfdu);
  if (!zf) goto L_2TAG_PACKET_2_0_1;
  xmm0 = MULSD(xmm0, LDQ(A(T_sin_ALL_ONES))); SCRATCH(r8);
  goto B1_4;
L_2TAG_PACKET_2_0_1:;
  xmm3 = LDQ(A(T_k_TWO_POW_55)); SCRATCH(r8);
  xmm3 = MULSD(xmm3, xmm0);
  xmm3 = SUBSD(xmm3, xmm0);
  xmm3 = MULSD(xmm3, LDQ(A(T_k_TWO_POW_M55))); SCRATCH(r8);
  goto B1_4;
L_2TAG_PACKET_1_0_1:;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7ff0u);
  CMP32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_3_0_1;
  rcx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rcx, 0x7ff0u);
  SUB32(rcx, 0x3f60u);
  SHR32(rcx, 7);
  AND32(rcx, 0xfffcu);
  r11 = A(T_k_PI_INV_TABLE);
  ADD64(rcx, r11);
  rax = (uint64_t)_mm_cvtsi128_si64(xmm0);
  r10 = LD32(rcx + 20);
  r8 = LD32(rcx + 24);
  rdx = (uint32_t)rax;
  SHR64(rax, 21);
  OR32(rax, 0x80000000u);
  SHR32(rax, 11);
  r9 = (uint32_t)r10;
  IMUL64(r10, rdx);
  IMUL64(r9, rax);
  IMUL64(r8, rax);
  rsi = LD32(rcx + 16);
  rdi = LD32(rcx + 12);
  r11 = (uint32_t)r10;
  SHR64(r10, 32);
  ADD64(r9, r10);
  ADD64(r11, r8);
  r8 = (uint32_t)r11;
  SHR64(r11, 32);
  ADD64(r9, r11);
  r10 = (uint32_t)rsi;
  IMUL64(rsi, rdx);
  IMUL64(r10, rax);
  r11 = (uint32_t)rdi;
  IMUL64(rdi, rdx);
  rbx = (uint32_t)rsi;
  SHR64(rsi, 32);
  ADD64(r9, rbx);
  rbx = (uint32_t)r9;
  SHR64(r9, 32);
  ADD64(r10, rsi);
  ADD64(r10, r9);
  SHL64(rbx, 32);
  OR64(r8, rbx);
  IMUL64(r11, rax);
  r9 = LD32(rcx + 8);
  rsi = LD32(rcx + 4);
  rbx = (uint32_t)rdi;
  SHR64(rdi, 32);
  ADD64(r10, rbx);
  rbx = (uint32_t)r10;
  SHR64(r10, 32);
  ADD64(r11, rdi);
  ADD64(r11, r10);
  rdi = r9;
  IMUL64(r9, rdx);
  IMUL64(rdi, rax);
  r10 = (uint32_t)r9;
  SHR64(r9, 32);
  ADD64(r11, r10);
  r10 = (uint32_t)r11;
  SHR64(r11, 32);
  ADD64(rdi, r9);
  ADD64(rdi, r11);
  r9 = rsi;
  IMUL64(rsi, rdx);
  IMUL64(r9, rax);
  SHL64(r10, 32);
  OR64(r10, rbx);
  rax = LD32(rcx);
  r11 = (uint32_t)rsi;
  SHR64(rsi, 32);
  ADD64(rdi, r11);
  r11 = (uint32_t)rdi;
  SHR64(rdi, 32);
  ADD64(r9, rsi);
  ADD64(r9, rdi);
  IMUL64(rdx, rax);
  rbx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  rdi = A(T_k_PI_INV_TABLE);
  SUB64(rcx, rdi);
  ADD32(rcx, rcx);
  ADD32(rcx, rcx);
  ADD32(rcx, rcx);
  ADD32(rcx, 0x13u);
  rsi = 0x8000u;
  AND32(rsi, rbx);
  SHR32(rbx, 4);
  AND32(rbx, 0x7ffu);
  SUB32(rbx, 0x3ffu);
  SUB32(rcx, rbx);
  ADD64(r9, rdx);
  rdx = (uint32_t)rcx;
  ADD32(rdx, 0x20u);
  CMP32(rcx, 0x1u);
  if (sf != of) goto L_2TAG_PACKET_4_0_1;
  NEGL(rcx);
  ADD32(rcx, 0x1du);
  SHL32(r9, rcx);
  rdi = (uint32_t)r9;
  AND32(r9, 0x1fffffffu);
  TEST32(r9, 0x10000000u);
  if (!zf) goto L_2TAG_PACKET_5_0_1;
  SHR32(r9, rcx);
  rbx = 0x0u;
  SHL64(r9, 32);
  OR64(r9, r11);
L_2TAG_PACKET_6_0_1:;
L_2TAG_PACKET_7_0_1:;
  CMP64(r9, 0x0ull);
  if (zf) goto L_2TAG_PACKET_8_0_1;
L_2TAG_PACKET_9_0_1:;
  BSRQ(r11, r9);
  rcx = 0x1du;
  SUB32(rcx, r11);
  if (zf || sf != of) goto L_2TAG_PACKET_10_0_1;
  SHL64(r9, rcx);
  rax = r10;
  SHL64(r10, rcx);
  ADD32(rdx, rcx);
  NEGL(rcx);
  ADD32(rcx, 0x40u);
  SHR64(rax, rcx);
  SHR64(r8, rcx);
  OR64(r9, rax);
  OR64(r10, r8);
L_2TAG_PACKET_11_0_1:;
  xmm0 = CVTSI2SDQ(xmm0, r9);
  SHR64(r10, 1);
  xmm3 = CVTSI2SDQ(xmm3, r10);
  xmm4 = _mm_xor_si128(xmm4, xmm4);
  SHL32(rdx, 4);
  NEGL(rdx);
  ADD32(rdx, 0x3ff0u);
  OR32(rdx, rsi);
  XOR32(rdx, rbx);
  xmm4 = _mm_insert_epi16(xmm4, (int)(uint16_t)rdx, 3);
  xmm2 = LDQ(A(T_k_PI_4)); SCRATCH(r8);
  xmm6 = LDQ(A(T_k_PI_4) + 8); SCRATCH(r8);
  xmm5 = _mm_xor_si128(xmm5, xmm5);
  SUB32(rdx, 0x3f0u);
  xmm5 = _mm_insert_epi16(xmm5, (int)(uint16_t)rdx, 3);
  xmm0 = MULSD(xmm0, xmm4);
  SHL32(rsi, 16);
  SAR32(rsi, 31);
  xmm3 = MULSD(xmm3, xmm5);
  xmm1 = xmm0;
  xmm0 = MULSD(xmm0, xmm2);
  SHR32(rdi, 29);
  xmm1 = ADDSD(xmm1, xmm3);
  xmm3 = MULSD(xmm3, xmm2);
  ADD32(rdi, rsi);
  XOR32(rdi, rsi);
  xmm6 = MULSD(xmm6, xmm1);
  rax = (uint32_t)rdi;
  xmm6 = ADDSD(xmm6, xmm3);
  xmm2 = xmm0;
  xmm0 = ADDSD(xmm0, xmm6);
  xmm2 = SUBSD(xmm2, xmm0);
  xmm6 = ADDSD(xmm6, xmm2);
L_2TAG_PACKET_12_0_1:;
  xmm1 = LDQ(A(T_k_PI32INV)); SCRATCH(r8);
  xmm1 = MULSD(xmm1, xmm0);
  xmm5 = LDQ(A(T_k_ONEHALF)); SCRATCH(r8);
  xmm4 = LDQ(A(T_k_SIGN_MASK)); SCRATCH(r8);
  xmm4 = _mm_and_si128(xmm4, xmm0);
  xmm5 = _mm_or_si128(xmm5, xmm4);
  xmm1 = ADDPD(xmm1, xmm5);
  rdx = CVTTSD2SIL(xmm1);
  xmm1 = CVTSI2SDL(xmm1, rdx);
  xmm3 = LDQ(A(T_k_P_1)); SCRATCH(r8);
  xmm2 = LD128(A(T_k_P_2)); SCRATCH(r8);
  xmm3 = MULSD(xmm3, xmm1);
  xmm1 = _mm_unpacklo_epi64(xmm1, xmm1);
  SHL32(rax, 3);
  ADD32(rdx, 0x1c7600u);
  xmm4 = xmm0;
  ADD32(rdx, rax);
  AND32(rdx, 0x3fu);
  xmm5 = LD128(A(T_k_SC_4)); SCRATCH(r8);
  rax = A(T_k_Ctable);
  SHL32(rdx, 5);
  ADD64(rax, rdx);
  xmm2 = MULPD(xmm2, xmm1);
  xmm0 = SUBSD(xmm0, xmm3);
  xmm1 = MULSD(xmm1, LDQ(A(T_k_P_3))); SCRATCH(r8);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm7 = LDQ(rax + 8);
  xmm0 = _mm_unpacklo_epi64(xmm0, xmm0);
  xmm3 = xmm4;
  xmm4 = SUBSD(xmm4, xmm2);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = SUBPD(xmm0, xmm2);
  xmm7 = MULSD(xmm7, xmm4);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm3 = SUBSD(xmm3, xmm2);
  xmm2 = LD128(rax);
  xmm1 = SUBSD(xmm1, xmm3);
  xmm3 = LDQ(rax + 24);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm7 = SUBSD(xmm7, xmm2);
  xmm1 = SUBSD(xmm1, xmm6);
  xmm6 = LD128(A(T_k_SC_2)); SCRATCH(r8);
  xmm2 = MULSD(xmm2, xmm4);
  xmm6 = MULPD(xmm6, xmm0);
  xmm3 = MULSD(xmm3, xmm4);
  xmm2 = MULPD(xmm2, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm5 = ADDPD(xmm5, LD128(A(T_k_SC_3))); SCRATCH(r8);
  xmm4 = MULSD(xmm4, LDQ(rax));
  xmm6 = ADDPD(xmm6, LD128(A(T_k_SC_1))); SCRATCH(r8);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = xmm3;
  xmm3 = ADDSD(xmm3, LDQ(rax + 8));
  xmm1 = MULPD(xmm1, xmm7);
  xmm7 = xmm4;
  xmm4 = ADDSD(xmm4, xmm3);
  xmm6 = ADDPD(xmm6, xmm5);
  xmm5 = LDQ(rax + 8);
  xmm5 = SUBSD(xmm5, xmm3);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm1 = ADDSD(xmm1, LDQ(rax + 16));
  xmm6 = MULPD(xmm6, xmm2);
  xmm5 = ADDSD(xmm5, xmm0);
  xmm3 = ADDSD(xmm3, xmm7);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm1 = ADDSD(xmm1, xmm3);
  xmm1 = ADDSD(xmm1, xmm6);
  xmm6 = _mm_unpackhi_epi64(xmm6, xmm6);
  xmm0 = xmm4;
  xmm1 = ADDSD(xmm1, xmm6);
  xmm0 = ADDSD(xmm0, xmm1);
  goto B1_4;
L_2TAG_PACKET_8_0_1:;
  ADD32(rdx, 0x40u);
  r9 = r10;
  r10 = r8;
  r8 = 0x0u;
  CMP64(r9, 0x0ull);
  if (!zf) goto L_2TAG_PACKET_9_0_1;
  ADD32(rdx, 0x40u);
  r9 = r10;
  r10 = r8;
  CMP64(r9, 0x0ull);
  if (!zf) goto L_2TAG_PACKET_9_0_1;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  xmm6 = _mm_xor_si128(xmm6, xmm6);
  goto L_2TAG_PACKET_12_0_1;
L_2TAG_PACKET_10_0_1:;
  if (zf) goto L_2TAG_PACKET_11_0_1;
  NEGL(rcx);
  SHR64(r10, rcx);
  rax = r9;
  SHR64(r9, rcx);
  SUB32(rdx, rcx);
  NEGL(rcx);
  ADD32(rcx, 0x40u);
  SHL64(rax, rcx);
  OR64(r10, rax);
  goto L_2TAG_PACKET_11_0_1;
L_2TAG_PACKET_4_0_1:;
  NEGL(rcx);
  SHL64(r9, 32);
  OR64(r9, r11);
  SHL64(r9, rcx);
  rdi = r9;
  TEST32(r9, 0x80000000u);
  if (!zf) goto L_2TAG_PACKET_13_0_1;
  SHR32(r9, rcx);
  rbx = 0x0u;
  SHR64(rdi, 3);
  goto L_2TAG_PACKET_7_0_1;
L_2TAG_PACKET_5_0_1:;
  SHR32(r9, rcx);
  rbx = 0x20000000u;
  SHR32(rbx, rcx);
  SHL64(r9, 32);
  OR64(r9, r11);
  SHL64(rbx, 32);
  ADD32(rdi, 0x20000000u);
  rcx = 0x0u;
  r11 = 0x0u;
  SUB64(rcx, r8);
  SBB64(r11, r10);
  SBB64(rbx, r9);
  r8 = rcx;
  r10 = r11;
  r9 = rbx;
  rbx = 0x8000u;
  goto L_2TAG_PACKET_6_0_1;
L_2TAG_PACKET_13_0_1:;
  SHR32(r9, rcx);
  rbx = 0x100000000ull;
  SHR64(rbx, rcx);
  rcx = 0x0u;
  r11 = 0x0u;
  SUB64(rcx, r8);
  SBB64(r11, r10);
  SBB64(rbx, r9);
  r8 = rcx;
  r10 = r11;
  r9 = rbx;
  rbx = 0x8000u;
  SHR64(rdi, 3);
  ADD32(rdi, 0x20000000u);
  goto L_2TAG_PACKET_7_0_1;
L_2TAG_PACKET_3_0_1:;
  xmm0 = LDQ(rsp + 8);
  xmm0 = MULSD(xmm0, LDQ(A(T_k_NEG_ZERO))); SCRATCH(r8);
  STQ(rsp, xmm0);
/* L_2TAG_PACKET_14_0_1 */
B1_4:;
  ADD64(rsp, 0x10ull);
  POP(rbx);
  rsp = rbp; POP(rbp);
  RET(xmm0);
}

static double hs_cos(__m128i xmm0) {
  REGS_INIT;
  __m128i xmm1 = XINIT(1);
  __m128i xmm2 = XINIT(2);
  __m128i xmm3 = XINIT(3);
  __m128i xmm4 = XINIT(4);
  __m128i xmm5 = XINIT(5);
  __m128i xmm6 = XINIT(6);
  __m128i xmm7 = XINIT(7);
  STACK_INIT;
  PUSH(rbp); rbp = rsp;
  PUSH(rbx);
  SUB64(rsp, 0x10ull);
  STQ(rsp + 8, xmm0);
/* B1_2 */
  rax = LD32(rsp + 12);
  xmm1 = LDQ(A(T_k_PI32INV)); SCRATCH(rbx);
  AND32(rax, 0x7fff0000u);
  SUB32(rax, 0x30300000u);
  CMP32(rax, 0x10c50000u);
  if (!cf && !zf) goto L_2TAG_PACKET_0_0_1;
  xmm1 = MULSD(xmm1, xmm0);
  xmm5 = LD128(A(T_k_ONEHALF)); SCRATCH(rbx);
  xmm4 = LDQ(A(T_k_SIGN_MASK)); SCRATCH(rbx);
  xmm4 = _mm_and_si128(xmm4, xmm0);
  xmm5 = _mm_or_si128(xmm5, xmm4);
  xmm1 = ADDPD(xmm1, xmm5);
  rdx = CVTTSD2SIL(xmm1);
  xmm1 = CVTSI2SDL(xmm1, rdx);
  xmm2 = LD128(A(T_k_P_2)); SCRATCH(rbx);
  xmm3 = LDQ(A(T_k_P_1)); SCRATCH(rbx);
  xmm3 = MULSD(xmm3, xmm1);
  xmm1 = _mm_unpacklo_epi64(xmm1, xmm1);
  ADD64(rdx, 0x1c7610ull);
  xmm4 = xmm0;
  AND64(rdx, 0x3full);
  xmm5 = LD128(A(T_k_SC_4)); SCRATCH(rbx);
  rax = A(T_k_Ctable);
  SHL64(rdx, 5);
  ADD64(rax, rdx);
  xmm2 = MULPD(xmm2, xmm1);
  xmm0 = SUBSD(xmm0, xmm3);
  xmm1 = MULSD(xmm1, LDQ(A(T_k_P_3))); SCRATCH(rbx);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm7 = LDQ(rax + 8);
  xmm0 = _mm_unpacklo_epi64(xmm0, xmm0);
  xmm3 = xmm4;
  xmm4 = SUBSD(xmm4, xmm2);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = SUBPD(xmm0, xmm2);
  xmm6 = LD128(A(T_k_SC_2)); SCRATCH(rbx);
  xmm7 = MULSD(xmm7, xmm4);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm3 = SUBSD(xmm3, xmm2);
  xmm2 = LD128(rax);
  xmm1 = SUBSD(xmm1, xmm3);
  xmm3 = LDQ(rax + 24);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm7 = SUBSD(xmm7, xmm2);
  xmm2 = MULSD(xmm2, xmm4);
  xmm6 = MULPD(xmm6, xmm0);
  xmm3 = MULSD(xmm3, xmm4);
  xmm2 = MULPD(xmm2, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm5 = ADDPD(xmm5, LD128(A(T_k_SC_3))); SCRATCH(rbx);
  xmm4 = MULSD(xmm4, LDQ(rax));
  xmm6 = ADDPD(xmm6, LD128(A(T_k_SC_1))); SCRATCH(rbx);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = xmm3;
  xmm3 = ADDSD(xmm3, LDQ(rax + 8));
  xmm1 = MULPD(xmm1, xmm7);
  xmm7 = xmm4;
  xmm4 = ADDSD(xmm4, xmm3);
  xmm6 = ADDPD(xmm6, xmm5);
  xmm5 = LDQ(rax + 8);
  xmm5 = SUBSD(xmm5, xmm3);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm1 = ADDSD(xmm1, LDQ(rax + 16));
  xmm6 = MULPD(xmm6, xmm2);
  xmm0 = ADDSD(xmm0, xmm5);
  xmm3 = ADDSD(xmm3, xmm7);
  xmm0 = ADDSD(xmm0, xmm1);
  xmm0 = ADDSD(xmm0, xmm3);
  xmm0 = ADDSD(xmm0, xmm6);
  xmm6 = _mm_unpackhi_epi64(xmm6, xmm6);
  xmm0 = ADDSD(xmm0, xmm6);
  xmm0 = ADDSD(xmm0, xmm4);
  goto B1_4;
L_2TAG_PACKET_0_0_1:;
  if (!zf && sf == of) goto L_2TAG_PACKET_1_0_1;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7fffu);
  xmm0 = _mm_insert_epi16(xmm0, (int)(uint16_t)rax, 3);
  xmm1 = LDQ(A(T_k_ONE)); SCRATCH(rbx);
  xmm1 = SUBSD(xmm1, xmm0);
  xmm0 = xmm1;
  goto B1_4;
L_2TAG_PACKET_1_0_1:;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7ff0u);
  CMP32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_2_0_1;
  rcx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rcx, 0x7ff0u);
  SUB32(rcx, 0x3f60u);
  SHR32(rcx, 7);
  AND32(rcx, 0xfffcu);
  r11 = A(T_k_PI_INV_TABLE);
  ADD64(rcx, r11);
  rax = (uint64_t)_mm_cvtsi128_si64(xmm0);
  r10 = LD32(rcx + 20);
  r8 = LD32(rcx + 24);
  rdx = (uint32_t)rax;
  SHR64(rax, 21);
  OR32(rax, 0x80000000u);
  SHR32(rax, 11);
  r9 = (uint32_t)r10;
  IMUL64(r10, rdx);
  IMUL64(r9, rax);
  IMUL64(r8, rax);
  rsi = LD32(rcx + 16);
  rdi = LD32(rcx + 12);
  r11 = (uint32_t)r10;
  SHR64(r10, 32);
  ADD64(r9, r10);
  ADD64(r11, r8);
  r8 = (uint32_t)r11;
  SHR64(r11, 32);
  ADD64(r9, r11);
  r10 = (uint32_t)rsi;
  IMUL64(rsi, rdx);
  IMUL64(r10, rax);
  r11 = (uint32_t)rdi;
  IMUL64(rdi, rdx);
  rbx = (uint32_t)rsi;
  SHR64(rsi, 32);
  ADD64(r9, rbx);
  rbx = (uint32_t)r9;
  SHR64(r9, 32);
  ADD64(r10, rsi);
  ADD64(r10, r9);
  SHL64(rbx, 32);
  OR64(r8, rbx);
  IMUL64(r11, rax);
  r9 = LD32(rcx + 8);
  rsi = LD32(rcx + 4);
  rbx = (uint32_t)rdi;
  SHR64(rdi, 32);
  ADD64(r10, rbx);
  rbx = (uint32_t)r10;
  SHR64(r10, 32);
  ADD64(r11, rdi);
  ADD64(r11, r10);
  rdi = r9;
  IMUL64(r9, rdx);
  IMUL64(rdi, rax);
  r10 = (uint32_t)r9;
  SHR64(r9, 32);
  ADD64(r11, r10);
  r10 = (uint32_t)r11;
  SHR64(r11, 32);
  ADD64(rdi, r9);
  ADD64(rdi, r11);
  r9 = rsi;
  IMUL64(rsi, rdx);
  IMUL64(r9, rax);
  SHL64(r10, 32);
  OR64(r10, rbx);
  rax = LD32(rcx);
  r11 = (uint32_t)rsi;
  SHR64(rsi, 32);
  ADD64(rdi, r11);
  r11 = (uint32_t)rdi;
  SHR64(rdi, 32);
  ADD64(r9, rsi);
  ADD64(r9, rdi);
  IMUL64(rdx, rax);
  rbx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  rdi = A(T_k_PI_INV_TABLE);
  SUB64(rcx, rdi);
  ADD32(rcx, rcx);
  ADD32(rcx, rcx);
  ADD32(rcx, rcx);
  ADD32(rcx, 0x13u);
  rsi = 0x8000u;
  AND32(rsi, rbx);
  SHR32(rbx, 4);
  AND32(rbx, 0x7ffu);
  SUB32(rbx, 0x3ffu);
  SUB32(rcx, rbx);
  ADD64(r9, rdx);
  rdx = (uint32_t)rcx;
  ADD32(rdx, 0x20u);
  CMP32(rcx, 0x1u);
  if (sf != of) goto L_2TAG_PACKET_3_0_1;
  NEGL(rcx);
  ADD32(rcx, 0x1du);
  SHL32(r9, rcx);
  rdi = (uint32_t)r9;
  AND32(r9, 0x1fffffffu);
  TEST32(r9, 0x10000000u);
  if (!zf) goto L_2TAG_PACKET_4_0_1;
  SHR32(r9, rcx);
  rbx = 0x0u;
  SHL64(r9, 32);
  OR64(r9, r11);
L_2TAG_PACKET_5_0_1:;
L_2TAG_PACKET_6_0_1:;
  CMP64(r9, 0x0ull);
  if (zf) goto L_2TAG_PACKET_7_0_1;
L_2TAG_PACKET_8_0_1:;
  BSRQ(r11, r9);
  rcx = 0x1du;
  SUB32(rcx, r11);
  if (zf || sf != of) goto L_2TAG_PACKET_9_0_1;
  SHL64(r9, rcx);
  rax = r10;
  SHL64(r10, rcx);
  ADD32(rdx, rcx);
  NEGL(rcx);
  ADD32(rcx, 0x40u);
  SHR64(rax, rcx);
  SHR64(r8, rcx);
  OR64(r9, rax);
  OR64(r10, r8);
L_2TAG_PACKET_10_0_1:;
  xmm0 = CVTSI2SDQ(xmm0, r9);
  SHR64(r10, 1);
  xmm3 = CVTSI2SDQ(xmm3, r10);
  xmm4 = _mm_xor_si128(xmm4, xmm4);
  SHL32(rdx, 4);
  NEGL(rdx);
  ADD32(rdx, 0x3ff0u);
  OR32(rdx, rsi);
  XOR32(rdx, rbx);
  xmm4 = _mm_insert_epi16(xmm4, (int)(uint16_t)rdx, 3);
  xmm2 = LDQ(A(T_k_PI_4)); SCRATCH(rbx);
  xmm6 = LDQ(A(T_k_PI_4) + 8); SCRATCH(rbx);
  xmm5 = _mm_xor_si128(xmm5, xmm5);
  SUB32(rdx, 0x3f0u);
  xmm5 = _mm_insert_epi16(xmm5, (int)(uint16_t)rdx, 3);
  xmm0 = MULSD(xmm0, xmm4);
  SHL32(rsi, 16);
  SAR32(rsi, 31);
  xmm3 = MULSD(xmm3, xmm5);
  xmm1 = xmm0;
  xmm0 = MULSD(xmm0, xmm2);
  SHR32(rdi, 29);
  xmm1 = ADDSD(xmm1, xmm3);
  xmm3 = MULSD(xmm3, xmm2);
  ADD32(rdi, rsi);
  XOR32(rdi, rsi);
  xmm6 = MULSD(xmm6, xmm1);
  rax = (uint32_t)rdi;
  xmm6 = ADDSD(xmm6, xmm3);
  xmm2 = xmm0;
  xmm0 = ADDSD(xmm0, xmm6);
  xmm2 = SUBSD(xmm2, xmm0);
  xmm6 = ADDSD(xmm6, xmm2);
L_2TAG_PACKET_11_0_1:;
  xmm1 = LDQ(A(T_k_PI32INV)); SCRATCH(rbx);
  xmm1 = MULSD(xmm1, xmm0);
  xmm5 = LDQ(A(T_k_ONEHALF)); SCRATCH(rbx);
  xmm4 = LDQ(A(T_k_SIGN_MASK)); SCRATCH(rbx);
  xmm4 = _mm_and_si128(xmm4, xmm0);
  xmm5 = _mm_or_si128(xmm5, xmm4);
  xmm1 = ADDPD(xmm1, xmm5);
  rdx = CVTTSD2SIQ(xmm1);
  xmm1 = CVTSI2SDQ(xmm1, rdx);
  xmm3 = LDQ(A(T_k_P_1)); SCRATCH(rbx);
  xmm2 = LD128(A(T_k_P_2)); SCRATCH(rbx);
  xmm3 = MULSD(xmm3, xmm1);
  xmm1 = _mm_unpacklo_epi64(xmm1, xmm1);
  SHL32(rax, 3);
  ADD32(rdx, 0x1c7610u);
  xmm4 = xmm0;
  ADD32(rdx, rax);
  AND32(rdx, 0x3fu);
  xmm5 = LD128(A(T_k_SC_4)); SCRATCH(rbx);
  rax = A(T_k_Ctable);
  SHL32(rdx, 5);
  ADD64(rax, rdx);
  xmm2 = MULPD(xmm2, xmm1);
  xmm0 = SUBSD(xmm0, xmm3);
  xmm1 = MULSD(xmm1, LDQ(A(T_k_P_3))); SCRATCH(rbx);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm7 = LDQ(rax + 8);
  xmm0 = _mm_unpacklo_epi64(xmm0, xmm0);
  xmm3 = xmm4;
  xmm4 = SUBSD(xmm4, xmm2);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = SUBPD(xmm0, xmm2);
  xmm7 = MULSD(xmm7, xmm4);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm3 = SUBSD(xmm3, xmm2);
  xmm2 = LD128(rax);
  xmm1 = SUBSD(xmm1, xmm3);
  xmm3 = LDQ(rax + 24);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm7 = SUBSD(xmm7, xmm2);
  xmm1 = SUBSD(xmm1, xmm6);
  xmm6 = LD128(A(T_k_SC_2)); SCRATCH(rbx);
  xmm2 = MULSD(xmm2, xmm4);
  xmm6 = MULPD(xmm6, xmm0);
  xmm3 = MULSD(xmm3, xmm4);
  xmm2 = MULPD(xmm2, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm5 = ADDPD(xmm5, LD128(A(T_k_SC_3))); SCRATCH(rbx);
  xmm4 = MULSD(xmm4, LDQ(rax));
  xmm6 = ADDPD(xmm6, LD128(A(T_k_SC_1))); SCRATCH(rbx);
  xmm5 = MULPD(xmm5, xmm0);
  xmm0 = xmm3;
  xmm3 = ADDSD(xmm3, LDQ(rax + 8));
  xmm1 = MULPD(xmm1, xmm7);
  xmm7 = xmm4;
  xmm4 = ADDSD(xmm4, xmm3);
  xmm6 = ADDPD(xmm6, xmm5);
  xmm5 = LDQ(rax + 8);
  xmm5 = SUBSD(xmm5, xmm3);
  xmm3 = SUBSD(xmm3, xmm4);
  xmm1 = ADDSD(xmm1, LDQ(rax + 16));
  xmm6 = MULPD(xmm6, xmm2);
  xmm5 = ADDSD(xmm5, xmm0);
  xmm3 = ADDSD(xmm3, xmm7);
  xmm1 = ADDSD(xmm1, xmm5);
  xmm1 = ADDSD(xmm1, xmm3);
  xmm1 = ADDSD(xmm1, xmm6);
  xmm6 = _mm_unpackhi_epi64(xmm6, xmm6);
  xmm0 = xmm4;
  xmm1 = ADDSD(xmm1, xmm6);
  xmm0 = ADDSD(xmm0, xmm1);
  goto B1_4;
L_2TAG_PACKET_7_0_1:;
  ADD32(rdx, 0x40u);
  r9 = r10;
  r10 = r8;
  r8 = 0x0u;
  CMP64(r9, 0x0ull);
  if (!zf) goto L_2TAG_PACKET_8_0_1;
  ADD32(rdx, 0x40u);
  r9 = r10;
  r10 = r8;
  CMP64(r9, 0x0ull);
  if (!zf) goto L_2TAG_PACKET_8_0_1;
  xmm0 = _mm_xor_si128(xmm0, xmm0);
  xmm6 = _mm_xor_si128(xmm6, xmm6);
  goto L_2TAG_PACKET_11_0_1;
L_2TAG_PACKET_9_0_1:;
  if (zf) goto L_2TAG_PACKET_10_0_1;
  NEGL(rcx);
  SHR64(r10, rcx);
  rax = r9;
  SHR64(r9, rcx);
  SUB32(rdx, rcx);
  NEGL(rcx);
  ADD32(rcx, 0x40u);
  SHL64(rax, rcx);
  OR64(r10, rax);
  goto L_2TAG_PACKET_10_0_1;
L_2TAG_PACKET_3_0_1:;
  NEGL(rcx);
  SHL64(r9, 32);
  OR64(r9, r11);
  SHL64(r9, rcx);
  rdi = r9;
  TEST32(r9, 0x80000000u);
  if (!zf) goto L_2TAG_PACKET_12_0_1;
  SHR32(r9, rcx);
  rbx = 0x0u;
  SHR64(rdi, 3);
  goto L_2TAG_PACKET_6_0_1;
L_2TAG_PACKET_4_0_1:;
  SHR32(r9, rcx);
  rbx = 0x20000000u;
  SHR32(rbx, rcx);
  SHL64(r9, 32);
  OR64(r9, r11);
  SHL64(rbx, 32);
  ADD32(rdi, 0x20000000u);
  rcx = 0x0u;
  r11 = 0x0u;
  SUB64(rcx, r8);
  SBB64(r11, r10);
  SBB64(rbx, r9);
  r8 = rcx;
  r10 = r11;
  r9 = rbx;
  rbx = 0x8000u;
  goto L_2TAG_PACKET_5_0_1;
L_2TAG_PACKET_12_0_1:;
  SHR32(r9, rcx);
  rbx = 0x100000000ull;
  SHR64(rbx, rcx);
  rcx = 0x0u;
  r11 = 0x0u;
  SUB64(rcx, r8);
  SBB64(r11, r10);
  SBB64(rbx, r9);
  r8 = rcx;
  r10 = r11;
  r9 = rbx;
  rbx = 0x8000u;
  SHR64(rdi, 3);
  ADD32(rdi, 0x20000000u);
  goto L_2TAG_PACKET_6_0_1;
L_2TAG_PACKET_2_0_1:;
  xmm0 = LDQ(rsp + 8);
  xmm0 = MULSD(xmm0, LDQ(A(T_k_NEG_ZERO))); SCRATCH(rbx);
  STQ(rsp, xmm0);
/* L_2TAG_PACKET_13_0_1 */
B1_4:;
  ADD64(rsp, 0x10ull);
  POP(rbx);
  rsp = rbp; POP(rbp);
  RET(xmm0);
}

static double hs_tan(__m128i xmm0) {
  REGS_INIT;
  __m128i xmm1 = XINIT(1);
  __m128i xmm2 = XINIT(2);
  __m128i xmm3 = XINIT(3);
  __m128i xmm4 = XINIT(4);
  __m128i xmm5 = XINIT(5);
  __m128i xmm6 = XINIT(6);
  __m128i xmm7 = XINIT(7);
  STACK_INIT;
  PUSH(rbp); rbp = rsp;
  PUSH(rbx);
  SUB64(rsp, 0x10ull);
  STQ(rsp + 8, xmm0);
/* B1_2 */
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7fffu);
  SUB32(rax, 0x3fbau);
  CMP32(rax, 0x10eu);
  if (!cf && !zf) goto L_2TAG_PACKET_0_0_1;
  xmm5 = LD128(A(T_k_ONEHALF)); SCRATCH(rbx);
  xmm6 = LD128(A(T_tan_MUL16)); SCRATCH(rbx);
  xmm0 = _mm_unpacklo_epi64(xmm0, xmm0);
  xmm4 = LD128(A(T_tan_sign_mask_tan)); SCRATCH(rbx);
  xmm4 = _mm_and_si128(xmm4, xmm0);
  xmm1 = LD128(A(T_tan_PI32INV_tan)); SCRATCH(rbx);
  xmm1 = MULPD(xmm1, xmm0);
  xmm5 = _mm_or_si128(xmm5, xmm4);
  xmm1 = ADDPD(xmm1, xmm5);
  xmm7 = xmm1;
  xmm7 = _mm_unpackhi_epi64(xmm7, xmm7);
  rdx = CVTTSD2SIL(xmm7);
  xmm1 = CVTTPD2DQ(xmm1, xmm1);
  xmm1 = CVTDQ2PD(xmm1, xmm1);
  xmm1 = MULPD(xmm1, xmm6);
  xmm3 = LD128(A(T_tan_P_1_tan)); SCRATCH(rbx);
  xmm5 = LDQ(A(T_tan_QQ_2_tan)); SCRATCH(rbx);
  ADD64(rdx, 0x72900ull);
  xmm4 = LD128(A(T_tan_P_2_tan)); SCRATCH(rbx);
  xmm3 = MULPD(xmm3, xmm1);
  AND64(rdx, 0x1full);
  xmm5 = MULSD(xmm5, xmm1);
  rcx = rdx;
  xmm4 = MULPD(xmm4, xmm1);
  SHL64(rcx, 1);
  xmm0 = SUBPD(xmm0, xmm3);
  xmm1 = MULPD(xmm1, LD128(A(T_tan_P_3_tan))); SCRATCH(rbx);
  ADD64(rdx, rcx);
  SHL64(rcx, 2);
  ADD64(rdx, rcx);
  xmm5 = ADDSD(xmm5, xmm0);
  xmm2 = xmm0;
  xmm0 = SUBPD(xmm0, xmm4);
  xmm6 = LDQ(A(T_k_ONE)); SCRATCH(rbx);
  SHL64(rdx, 4);
  rax = A(T_tan_Ctable_tan);
  xmm5 = _mm_and_si128(xmm5, LD128(A(T_tan_MASK_35_tan))); SCRATCH(rbx);
  xmm3 = xmm0;
  ADD64(rax, rdx);
  xmm2 = SUBPD(xmm2, xmm0);
  xmm0 = _mm_unpackhi_epi64(xmm0, xmm0);
  xmm6 = DIVSD(xmm6, xmm5);
  xmm2 = SUBPD(xmm2, xmm4);
  xmm7 = LD128(rax + 16);
  xmm3 = SUBSD(xmm3, xmm5);
  xmm7 = MULPD(xmm7, xmm0);
  xmm2 = SUBPD(xmm2, xmm1);
  xmm1 = LD128(rax + 48);
  xmm1 = MULPD(xmm1, xmm0);
  xmm4 = LD128(rax + 96);
  xmm4 = MULPD(xmm4, xmm0);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm3 = xmm0;
  xmm0 = MULPD(xmm0, xmm0);
  xmm7 = ADDPD(xmm7, LD128(rax));
  xmm1 = ADDPD(xmm1, LD128(rax + 32));
  xmm1 = MULPD(xmm1, xmm0);
  xmm4 = ADDPD(xmm4, LD128(rax + 80));
  xmm7 = ADDPD(xmm7, xmm1);
  xmm1 = LD128(rax + 112);
  xmm1 = MULPD(xmm1, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm4 = ADDPD(xmm4, xmm1);
  xmm1 = LD128(rax + 64);
  xmm1 = MULPD(xmm1, xmm0);
  xmm7 = ADDPD(xmm7, xmm1);
  xmm1 = xmm3;
  xmm3 = MULPD(xmm3, xmm0);
  xmm0 = MULSD(xmm0, xmm0);
  xmm1 = MULPD(xmm1, LD128(rax + 144));
  xmm4 = MULPD(xmm4, xmm3);
  xmm3 = xmm1;
  xmm7 = ADDPD(xmm7, xmm4);
  xmm4 = xmm1;
  xmm0 = MULSD(xmm0, xmm7);
  xmm7 = _mm_unpackhi_epi64(xmm7, xmm7);
  xmm0 = ADDSD(xmm0, xmm7);
  xmm1 = _mm_unpackhi_epi64(xmm1, xmm1);
  xmm3 = ADDSD(xmm3, xmm1);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm1 = ADDSD(xmm1, xmm4);
  xmm4 = xmm2;
  xmm7 = LDQ(rax + 144);
  xmm2 = _mm_unpackhi_epi64(xmm2, xmm2);
  xmm7 = ADDSD(xmm7, LDQ(rax + 152));
  xmm7 = MULSD(xmm7, xmm2);
  xmm7 = ADDSD(xmm7, LDQ(rax + 136));
  xmm7 = ADDSD(xmm7, xmm1);
  xmm0 = ADDSD(xmm0, xmm7);
  xmm7 = LDQ(A(T_k_ONE)); SCRATCH(rbx);
  xmm4 = MULSD(xmm4, xmm6);
  xmm2 = LDQ(rax + 168);
  xmm2 = _mm_and_si128(xmm2, xmm6);
  xmm5 = MULSD(xmm5, xmm2);
  xmm6 = MULSD(xmm6, LDQ(rax + 160));
  xmm7 = SUBSD(xmm7, xmm5);
  xmm2 = SUBSD(xmm2, LDQ(rax + 128));
  xmm7 = SUBSD(xmm7, xmm4);
  xmm7 = MULSD(xmm7, xmm6);
  xmm4 = xmm3;
  xmm3 = SUBSD(xmm3, xmm2);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm4 = SUBSD(xmm4, xmm2);
  xmm0 = ADDSD(xmm0, xmm4);
  xmm0 = SUBSD(xmm0, xmm7);
  xmm0 = ADDSD(xmm0, xmm3);
  goto B1_4;
L_2TAG_PACKET_0_0_1:;
  if (!zf && sf == of) goto L_2TAG_PACKET_1_0_1;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  rdx = (uint32_t)rax;
  AND32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_2_0_1;
  AND32(rdx, 0x7fffu);
  CMP32(rdx, 0x3e20u);
  if (cf) goto L_2TAG_PACKET_3_0_1;
  xmm2 = xmm0;
  xmm3 = xmm0;
  xmm1 = LDQ(A(T_tan_Q_11_tan)); SCRATCH(rbx);
  xmm2 = MULSD(xmm2, xmm0);
  xmm3 = MULSD(xmm3, xmm2);
  xmm1 = MULSD(xmm1, xmm2);
  xmm1 = ADDSD(xmm1, LDQ(A(T_tan_Q_9_tan))); SCRATCH(rbx);
  xmm1 = MULSD(xmm1, xmm2);
  xmm1 = ADDSD(xmm1, LDQ(A(T_tan_Q_7_tan))); SCRATCH(rbx);
  xmm1 = MULSD(xmm1, xmm2);
  xmm1 = ADDSD(xmm1, LDQ(A(T_tan_Q_5_tan))); SCRATCH(rbx);
  xmm1 = MULSD(xmm1, xmm2);
  xmm1 = ADDSD(xmm1, LDQ(A(T_tan_Q_3_tan))); SCRATCH(rbx);
  xmm1 = MULSD(xmm1, xmm3);
  xmm0 = ADDSD(xmm0, xmm1);
  goto B1_4;
L_2TAG_PACKET_3_0_1:;
  xmm3 = LDQ(A(T_k_TWO_POW_55)); SCRATCH(rbx);
  xmm3 = MULSD(xmm3, xmm0);
  xmm0 = ADDSD(xmm0, xmm3);
  xmm0 = MULSD(xmm0, LDQ(A(T_k_TWO_POW_M55))); SCRATCH(rbx);
  goto B1_4;
L_2TAG_PACKET_2_0_1:;
  xmm1 = xmm0;
  xmm1 = MULSD(xmm1, xmm1);
  goto B1_4;
L_2TAG_PACKET_1_0_1:;
  rax = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rax, 0x7ff0u);
  CMP32(rax, 0x7ff0u);
  if (zf) goto L_2TAG_PACKET_4_0_1;
  rcx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  AND32(rcx, 0x7ff0u);
  SUB32(rcx, 0x3f60u);
  SHR32(rcx, 7);
  AND32(rcx, 0xfffcu);
  r11 = A(T_k_PI_INV_TABLE);
  ADD64(rcx, r11);
  rax = (uint64_t)_mm_cvtsi128_si64(xmm0);
  r10 = LD32(rcx + 20);
  r8 = LD32(rcx + 24);
  rdx = (uint32_t)rax;
  SHR64(rax, 21);
  OR32(rax, 0x80000000u);
  SHR32(rax, 11);
  r9 = (uint32_t)r10;
  IMUL64(r10, rdx);
  IMUL64(r9, rax);
  IMUL64(r8, rax);
  rsi = LD32(rcx + 16);
  rdi = LD32(rcx + 12);
  r11 = (uint32_t)r10;
  SHR64(r10, 32);
  ADD64(r9, r10);
  ADD64(r11, r8);
  r8 = (uint32_t)r11;
  SHR64(r11, 32);
  ADD64(r9, r11);
  r10 = (uint32_t)rsi;
  IMUL64(rsi, rdx);
  IMUL64(r10, rax);
  r11 = (uint32_t)rdi;
  IMUL64(rdi, rdx);
  rbx = (uint32_t)rsi;
  SHR64(rsi, 32);
  ADD64(r9, rbx);
  rbx = (uint32_t)r9;
  SHR64(r9, 32);
  ADD64(r10, rsi);
  ADD64(r10, r9);
  SHL64(rbx, 32);
  OR64(r8, rbx);
  IMUL64(r11, rax);
  r9 = LD32(rcx + 8);
  rsi = LD32(rcx + 4);
  rbx = (uint32_t)rdi;
  SHR64(rdi, 32);
  ADD64(r10, rbx);
  rbx = (uint32_t)r10;
  SHR64(r10, 32);
  ADD64(r11, rdi);
  ADD64(r11, r10);
  rdi = r9;
  IMUL64(r9, rdx);
  IMUL64(rdi, rax);
  r10 = (uint32_t)r9;
  SHR64(r9, 32);
  ADD64(r11, r10);
  r10 = (uint32_t)r11;
  SHR64(r11, 32);
  ADD64(rdi, r9);
  ADD64(rdi, r11);
  r9 = rsi;
  IMUL64(rsi, rdx);
  IMUL64(r9, rax);
  SHL64(r10, 32);
  OR64(r10, rbx);
  rax = LD32(rcx);
  r11 = (uint32_t)rsi;
  SHR64(rsi, 32);
  ADD64(rdi, r11);
  r11 = (uint32_t)rdi;
  SHR64(rdi, 32);
  ADD64(r9, rsi);
  ADD64(r9, rdi);
  IMUL64(rdx, rax);
  rbx = (uint16_t)_mm_extract_epi16(xmm0, 3);
  rdi = A(T_k_PI_INV_TABLE);
  SUB64(rcx, rdi);
  ADD32(rcx, rcx);
  ADD32(rcx, rcx);
  ADD32(rcx, rcx);
  ADD32(rcx, 0x13u);
  rsi = 0x8000u;
  AND32(rsi, rbx);
  SHR32(rbx, 4);
  AND32(rbx, 0x7ffu);
  SUB32(rbx, 0x3ffu);
  SUB32(rcx, rbx);
  ADD64(r9, rdx);
  rdx = (uint32_t)rcx;
  ADD32(rdx, 0x20u);
  CMP32(rcx, 0x0u);
  if (sf != of) goto L_2TAG_PACKET_5_0_1;
  NEGL(rcx);
  ADD32(rcx, 0x1du);
  SHL32(r9, rcx);
  rdi = (uint32_t)r9;
  AND32(r9, 0x3fffffffu);
  TEST32(r9, 0x20000000u);
  if (!zf) goto L_2TAG_PACKET_6_0_1;
  SHR32(r9, rcx);
  rbx = 0x0u;
  SHL64(r9, 32);
  OR64(r9, r11);
L_2TAG_PACKET_7_0_1:;
L_2TAG_PACKET_8_0_1:;
  CMP64(r9, 0x0ull);
  if (zf) goto L_2TAG_PACKET_9_0_1;
L_2TAG_PACKET_10_0_1:;
  BSRQ(r11, r9);
  rcx = 0x1du;
  SUB32(rcx, r11);
  if (zf || sf != of) goto L_2TAG_PACKET_11_0_1;
  SHL64(r9, rcx);
  rax = r10;
  SHL64(r10, rcx);
  ADD32(rdx, rcx);
  NEGL(rcx);
  ADD32(rcx, 0x40u);
  SHR64(rax, rcx);
  SHR64(r8, rcx);
  OR64(r9, rax);
  OR64(r10, r8);
L_2TAG_PACKET_12_0_1:;
  xmm0 = CVTSI2SDQ(xmm0, r9);
  SHR64(r10, 1);
  xmm3 = CVTSI2SDQ(xmm3, r10);
  xmm4 = _mm_xor_si128(xmm4, xmm4);
  SHL32(rdx, 4);
  NEGL(rdx);
  ADD32(rdx, 0x3ff0u);
  OR32(rdx, rsi);
  XOR32(rdx, rbx);
  xmm4 = _mm_insert_epi16(xmm4, (int)(uint16_t)rdx, 3);
  xmm2 = LDQ(A(T_tan_PI_4_tan)); SCRATCH(rbx);
  xmm7 = LDQ(A(T_tan_PI_4_tan) + 8); SCRATCH(rbx);
  xmm5 = _mm_xor_si128(xmm5, xmm5);
  SUB32(rdx, 0x3f0u);
  xmm5 = _mm_insert_epi16(xmm5, (int)(uint16_t)rdx, 3);
  xmm0 = MULSD(xmm0, xmm4);
  SHL32(rsi, 16);
  SAR32(rsi, 31);
  xmm3 = MULSD(xmm3, xmm5);
  xmm1 = xmm0;
  xmm0 = MULSD(xmm0, xmm2);
  SHR32(rdi, 30);
  xmm1 = ADDSD(xmm1, xmm3);
  xmm3 = MULSD(xmm3, xmm2);
  ADD32(rdi, rsi);
  XOR32(rdi, rsi);
  xmm7 = MULSD(xmm7, xmm1);
  rax = (uint32_t)rdi;
  xmm7 = ADDSD(xmm7, xmm3);
  xmm2 = xmm0;
  xmm0 = ADDSD(xmm0, xmm7);
  xmm2 = SUBSD(xmm2, xmm0);
  xmm7 = ADDSD(xmm7, xmm2);
  xmm1 = LD128(A(T_tan_PI32INV_tan)); SCRATCH(rbx);
  xmm0 = _mm_unpacklo_epi64(xmm0, xmm0);
  xmm4 = LD128(A(T_tan_sign_mask_tan)); SCRATCH(rbx);
  xmm4 = _mm_and_si128(xmm4, xmm0);
  xmm1 = MULPD(xmm1, xmm0);
  xmm7 = _mm_unpacklo_epi64(xmm7, xmm7);
  xmm5 = LD128(A(T_k_ONEHALF)); SCRATCH(rbx);
  xmm6 = LD128(A(T_tan_MUL16)); SCRATCH(rbx);
  xmm5 = _mm_or_si128(xmm5, xmm4);
  xmm1 = ADDPD(xmm1, xmm5);
  xmm5 = xmm1;
  xmm5 = _mm_unpackhi_epi64(xmm5, xmm5);
  rdx = CVTTSD2SIL(xmm5);
  xmm1 = CVTTPD2DQ(xmm1, xmm1);
  xmm1 = CVTDQ2PD(xmm1, xmm1);
  xmm1 = MULPD(xmm1, xmm6);
  xmm3 = LD128(A(T_tan_P_1_tan)); SCRATCH(rbx);
  xmm5 = LDQ(A(T_tan_QQ_2_tan)); SCRATCH(rbx);
  SHL32(rax, 4);
  ADD32(rdx, 0x72900u);
  xmm4 = LD128(A(T_tan_P_2_tan)); SCRATCH(rbx);
  xmm3 = MULPD(xmm3, xmm1);
  ADD32(rdx, rax);
  AND32(rdx, 0x1fu);
  xmm5 = MULSD(xmm5, xmm1);
  rcx = (uint32_t)rdx;
  xmm4 = MULPD(xmm4, xmm1);
  SHL32(rcx, 1);
  xmm0 = SUBPD(xmm0, xmm3);
  xmm1 = MULPD(xmm1, LD128(A(T_tan_P_3_tan))); SCRATCH(rbx);
  ADD32(rdx, rcx);
  SHL32(rcx, 2);
  ADD32(rdx, rcx);
  xmm5 = ADDSD(xmm5, xmm0);
  xmm2 = xmm0;
  xmm0 = SUBPD(xmm0, xmm4);
  xmm6 = LDQ(A(T_k_ONE)); SCRATCH(rbx);
  SHL32(rdx, 4);
  rax = A(T_tan_Ctable_tan);
  xmm5 = _mm_and_si128(xmm5, LD128(A(T_tan_MASK_35_tan))); SCRATCH(rbx);
  xmm3 = xmm0;
  ADD64(rax, rdx);
  xmm2 = SUBPD(xmm2, xmm0);
  xmm0 = _mm_unpackhi_epi64(xmm0, xmm0);
  xmm6 = DIVSD(xmm6, xmm5);
  xmm2 = SUBPD(xmm2, xmm4);
  xmm3 = SUBSD(xmm3, xmm5);
  xmm2 = SUBPD(xmm2, xmm1);
  xmm1 = LD128(rax + 48);
  xmm2 = ADDPD(xmm2, xmm7);
  xmm7 = LD128(rax + 16);
  xmm7 = MULPD(xmm7, xmm0);
  xmm4 = LD128(rax + 96);
  xmm1 = MULPD(xmm1, xmm0);
  xmm4 = MULPD(xmm4, xmm0);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm3 = xmm0;
  xmm0 = MULPD(xmm0, xmm0);
  xmm7 = ADDPD(xmm7, LD128(rax));
  xmm1 = ADDPD(xmm1, LD128(rax + 32));
  xmm1 = MULPD(xmm1, xmm0);
  xmm4 = ADDPD(xmm4, LD128(rax + 80));
  xmm7 = ADDPD(xmm7, xmm1);
  xmm1 = LD128(rax + 112);
  xmm1 = MULPD(xmm1, xmm0);
  xmm0 = MULPD(xmm0, xmm0);
  xmm4 = ADDPD(xmm4, xmm1);
  xmm1 = LD128(rax + 64);
  xmm1 = MULPD(xmm1, xmm0);
  xmm7 = ADDPD(xmm7, xmm1);
  xmm1 = xmm3;
  xmm3 = MULPD(xmm3, xmm0);
  xmm0 = MULSD(xmm0, xmm0);
  xmm1 = MULPD(xmm1, LD128(rax + 144));
  xmm4 = MULPD(xmm4, xmm3);
  xmm3 = xmm1;
  xmm7 = ADDPD(xmm7, xmm4);
  xmm4 = xmm1;
  xmm0 = MULSD(xmm0, xmm7);
  xmm7 = _mm_unpackhi_epi64(xmm7, xmm7);
  xmm0 = ADDSD(xmm0, xmm7);
  xmm1 = _mm_unpackhi_epi64(xmm1, xmm1);
  xmm3 = ADDSD(xmm3, xmm1);
  xmm4 = SUBSD(xmm4, xmm3);
  xmm1 = ADDSD(xmm1, xmm4);
  xmm4 = xmm2;
  xmm7 = LDQ(rax + 144);
  xmm2 = _mm_unpackhi_epi64(xmm2, xmm2);
  xmm7 = ADDSD(xmm7, LDQ(rax + 152));
  xmm7 = MULSD(xmm7, xmm2);
  xmm7 = ADDSD(xmm7, LDQ(rax + 136));
  xmm7 = ADDSD(xmm7, xmm1);
  xmm0 = ADDSD(xmm0, xmm7);
  xmm7 = LDQ(A(T_k_ONE)); SCRATCH(rbx);
  xmm4 = MULSD(xmm4, xmm6);
  xmm2 = LDQ(rax + 168);
  xmm2 = _mm_and_si128(xmm2, xmm6);
  xmm5 = MULSD(xmm5, xmm2);
  xmm6 = MULSD(xmm6, LDQ(rax + 160));
  xmm7 = SUBSD(xmm7, xmm5);
  xmm2 = SUBSD(xmm2, LDQ(rax + 128));
  xmm7 = SUBSD(xmm7, xmm4);
  xmm7 = MULSD(xmm7, xmm6);
  xmm4 = xmm3;
  xmm3 = SUBSD(xmm3, xmm2);
  xmm2 = ADDSD(xmm2, xmm3);
  xmm4 = SUBSD(xmm4, xmm2);
  xmm0 = ADDSD(xmm0, xmm4);
  xmm0 = SUBSD(xmm0, xmm7);
  xmm0 = ADDSD(xmm0, xmm3);
  goto B1_4;
L_2TAG_PACKET_9_0_1:;
  ADD32(rdx, 0x40u);
  r9 = r10;
  r10 = r8;
  r8 = 0x0u;
  CMP64(r9, 0x0ull);
  if (!zf) goto L_2TAG_PACKET_10_0_1;
  ADD32(rdx, 0x40u);
  r9 = r10;
  r10 = r8;
  CMP64(r9, 0x0ull);
  if (!zf) goto L_2TAG_PACKET_10_0_1;
  goto L_2TAG_PACKET_12_0_1;
L_2TAG_PACKET_11_0_1:;
  if (zf) goto L_2TAG_PACKET_12_0_1;
  NEGL(rcx);
  SHR64(r10, rcx);
  rax = r9;
  SHR64(r9, rcx);
  SUB32(rdx, rcx);
  NEGL(rcx);
  ADD32(rcx, 0x40u);
  SHL64(rax, rcx);
  OR64(r10, rax);
  goto L_2TAG_PACKET_12_0_1;
L_2TAG_PACKET_5_0_1:;
  NOTL(rcx);
  SHL64(r9, 32);
  OR64(r9, r11);
  SHL64(r9, rcx);
  rdi = r9;
  TEST32(r9, 0x80000000u);
  if (!zf) goto L_2TAG_PACKET_13_0_1;
  SHR32(r9, rcx);
  rbx = 0x0u;
  SHR64(rdi, 2);
  goto L_2TAG_PACKET_8_0_1;
L_2TAG_PACKET_6_0_1:;
  SHR32(r9, rcx);
  rbx = 0x40000000u;
  SHR32(rbx, rcx);
  SHL64(r9, 32);
  OR64(r9, r11);
  SHL64(rbx, 32);
  ADD32(rdi, 0x40000000u);
  rcx = 0x0u;
  r11 = 0x0u;
  SUB64(rcx, r8);
  SBB64(r11, r10);
  SBB64(rbx, r9);
  r8 = rcx;
  r10 = r11;
  r9 = rbx;
  rbx = 0x8000u;
  goto L_2TAG_PACKET_7_0_1;
L_2TAG_PACKET_13_0_1:;
  SHR32(r9, rcx);
  rbx = 0x100000000ull;
  SHR64(rbx, rcx);
  rcx = 0x0u;
  r11 = 0x0u;
  SUB64(rcx, r8);
  SBB64(r11, r10);
  SBB64(rbx, r9);
  r8 = rcx;
  r10 = r11;
  r9 = rbx;
  rbx = 0x8000u;
  SHR64(rdi, 2);
  ADD32(rdi, 0x40000000u);
  goto L_2TAG_PACKET_8_0_1;
L_2TAG_PACKET_4_0_1:;
  xmm0 = LDQ(rsp + 8);
  xmm0 = MULSD(xmm0, LDQ(A(T_k_NEG_ZERO))); SCRATCH(rbx);
  STQ(rsp, xmm0);
/* L_2TAG_PACKET_14_0_1 */
B1_4:;
  ADD64(rsp, 0x10ull);
  POP(rbx);
  rsp = rbp; POP(rbp);
  RET(xmm0);
}

double bcx_hs_pow(double x, double y) { return hs_pow(XIN(x, 0), XIN(y, 1)); }
double bcx_hs_exp(double x) { return hs_exp(XIN(x, 0)); }
double bcx_hs_log(double x) { return hs_log(XIN(x, 0)); }
double bcx_hs_log10(double x) { return hs_log10(XIN(x, 0)); }
double bcx_hs_sin(double x) { return hs_sin(XIN(x, 0)); }
double bcx_hs_cos(double x) { return hs_cos(XIN(x, 0)); }
double bcx_hs_tan(double x) { return hs_tan(XIN(x, 0)); }
