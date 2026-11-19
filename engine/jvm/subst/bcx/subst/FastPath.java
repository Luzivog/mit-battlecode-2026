package bcx.subst;

/** Fast paths' differential check (engine/AGENTS.md, "The checks"): with BCX_CHECK_FASTPATH=1 set, each fast path
 *  also runs the translated original and calls fail on any difference, which ends the game process. */
public final class FastPath {
    public static final boolean CHECK = check0();

    private static native boolean check0();

    public static native void fail(String what);

    /** A java.util.Random's 48-bit state, its seed left as it is; clears haveNextNextGaussian as setSeed does. */
    public static native long randomState(java.util.Random r);

    /** GameWorld_p's BFS from tile t: table[t][t] = center, then breadth first over nbr (8 per tile, -1: none),
     *  recording back[k] at table[ni][t] for each tile first reached by step k. Unchecked: every row of table is
     *  present and as long as the tile count, nbr's tiles are below it, queue holds it. */
    public static native void bfs(int[] nbr, battlecode.common.Direction[][] table, battlecode.common.Direction[] back,
                                  int[] queue, int t, battlecode.common.Direction center);

    /** Unsafe.get{Short,Int,Long}Unaligned(o, offset, bigEndian), read directly (Unsafe_p). */
    public static native short unalignedShort(Object o, long offset, boolean bigEndian);

    public static native int unalignedInt(Object o, long offset, boolean bigEndian);

    public static native long unalignedLong(Object o, long offset, boolean bigEndian);
}
