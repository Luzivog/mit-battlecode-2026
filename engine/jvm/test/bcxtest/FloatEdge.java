package bcxtest;

/** Floating-point edge values, raw bits (NaN signs and payloads included). */
public class FloatEdge {
    static void p(String n, double d) { Out.print(n + " " + Long.toHexString(Double.doubleToRawLongBits(d))); }
    static void p(String n, float f) { Out.print(n + " " + Integer.toHexString(Float.floatToRawIntBits(f))); }
    static void p(String n, long v) { Out.print(n + " " + v); }

    public static void main(String[] a) {
        for (int s = 0; s < 2; s++) {
            double nan = Double.NaN, pinf = Double.POSITIVE_INFINITY, ninf = Double.NEGATIVE_INFINITY, nz = -0.0;
            double v = (s & 1) == 0 ? nan : nz;
            p("cmp", Double.compare(0.0, -0.0) * 10 + Double.compare(nan, pinf));
            p("min-0", Math.min(-0.0, 0.0));
            p("max-0", Math.max(-0.0, 0.0));
            p("minnan", Math.min(nan, 1.0));
            p("maxfnan", Math.max(1.0f, Float.NaN));
            p("1/nz", 1.0 / nz);
            p("inf-inf", pinf - pinf);
            p("ninf*0", ninf * 0);
            p("abs nz", Math.abs(nz));
            p("-nz", -nz);
            p("nz+0", nz + 0.0);
            p("minv/2", Double.MIN_VALUE / 2);
            p("max*2", Double.MAX_VALUE * 2);
            p("fminv", Float.MIN_VALUE);
            p("(f)dmin", (float) Double.MIN_VALUE);
            p("(f)1e40", (float) 1e40);
            p("sqrt-1", Math.sqrt(-1));
            p("hash nz", Double.hashCode(nz));
            p("fcmp", Float.compare(Float.NaN, Float.NaN));
            p("bits nan", Double.doubleToLongBits(nan));
            p("fbits", Float.floatToIntBits(Float.intBitsToFloat(0x7fc00001)));
            p("v", v);
            p("nan*v", nan * v);
            p("v-v", v - v);
        }
    }
}
