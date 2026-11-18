package fuzz_lang;

import bcxtest.Out;

/** Conformance: every fuzz_lang feature on many inputs, results printed (engine/test/conformance.py FeatureMain). */
public class FeatureMain {
    public static void main(String[] a) {
        for (int f = 0; f < T.NF; f++) {
            StringBuilder b = new StringBuilder("f" + f + ":");
            for (int s = 0; s < 40; s++) {
                int v = s * 7919 + f * 104729;
                try {
                    b.append(' ').append(RobotPlayer.feature(f, v));
                } catch (Throwable t) {
                    b.append(" !").append(t.getClass().getName()).append(t.getMessage() == null ? "" : "(" + t.getMessage() + ")");
                }
            }
            Out.print(b.toString());
        }
    }
}
