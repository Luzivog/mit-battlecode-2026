package bcx.subst;

/** Fast path of battlecode.common.MapLocation.toString, String.format("[%d, %d]", x, y): the same text built
 *  directly (%d of an int is its decimal form, '-' first when negative). The original stays as toString$orig;
 *  FastPath.CHECK runs both. */
public final class MapLocation_p {
    @Orig public int x;
    @Orig public int y;

    public String toString() {
        String f = new StringBuilder(24).append('[').append(x).append(", ").append(y).append(']').toString();
        if (FastPath.CHECK && !f.equals(toString$orig())) FastPath.fail("MapLocation.toString: " + f);
        return f;
    }

    @Orig
    public String toString$orig() { return null; }
}
