/* What Math.pow/exp/log/log10/sin/cos/tan return on HotSpot x86-64 (OpenJDK 21), bit for bit, NaNs included.
 * That is HotSpot's Intel LIBM stubs, not StrictMath. Same result in the interpreter, C1 and C2. See hsmath.c. */
#ifndef BCX_HSMATH_H
#define BCX_HSMATH_H

#ifdef __cplusplus
extern "C" {
#endif

double bcx_hs_pow(double x, double y);
double bcx_hs_exp(double x);
double bcx_hs_log(double x);
double bcx_hs_log10(double x);
double bcx_hs_sin(double x);
double bcx_hs_cos(double x);
double bcx_hs_tan(double x);

#ifdef __cplusplus
}
#endif

#endif
