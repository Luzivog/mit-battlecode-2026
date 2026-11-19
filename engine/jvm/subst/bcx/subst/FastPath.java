package bcx.subst;

/** Fast paths' differential check (engine/AGENTS.md, "The checks"): with BCX_CHECK_FASTPATH=1 set, each fast path
 *  also runs the translated original and calls fail on any difference, which ends the game process. */
public final class FastPath {
    public static final boolean CHECK = check0();

    private static native boolean check0();

    public static native void fail(String what);

    /** A java.util.Random's 48-bit state, its seed left as it is; clears haveNextNextGaussian as setSeed does. */
    public static native long randomState(java.util.Random r);
}
