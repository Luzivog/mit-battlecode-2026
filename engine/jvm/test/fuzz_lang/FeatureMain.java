package fuzz_lang;

import bcxtest.Out;

/**
 * Conformance: every fuzz_lang feature on many inputs, results printed (engine/test/conformance.py FeatureMain).
 * Here the features run as system code, not as a bot: identity hashes are the JVM's (a bot's come from the
 * instrumenter's ObjectMethods), System is java.lang.System (a bot's is the injected one) and unseeded randomness is
 * the JVM's (a bot's is seeded by its robot ID). The features that read them are left to the agreement corpus.
 */
public class FeatureMain {
    static final java.util.Set<Integer> BOT_ONLY = java.util.Set.of(13, 22, 23, 35, 49);

    public static void main(String[] a) {
        for (int f = 0; f < T.NF; f++) {
            StringBuilder b = new StringBuilder("f" + f + ":");
            if (BOT_ONLY.contains(f)) {
                Out.print(b.append(" bot only").toString());
                continue;
            }
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
