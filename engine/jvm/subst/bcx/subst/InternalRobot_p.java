package bcx.subst;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.UnitType;
import battlecode.world.GameWorld;

/** Fast path of battlecode.world.InternalRobot.canSenseLocation: the vision test is a function of the offset from the
 *  robot (exact, see GameWorld_p's cone fast path), the vision radius, cone angle, bottom-left flag and facing; each
 *  offset's answer within the vision box is the test's own on first use, kept in the world's masks. The original
 *  stays as canSenseLocation$orig; FastPath.CHECK runs both. */
public final class InternalRobot_p {
    @Orig protected GameWorld gameWorld;
    @Orig private UnitType type;
    @Orig private MapLocation location;
    @Orig private Direction dir;

    public boolean canSenseLocation(MapLocation loc) {
        boolean f = bcxSense(loc);
        if (FastPath.CHECK && f != canSenseLocation$orig(loc))
            FastPath.fail("InternalRobot.canSenseLocation: " + location + " " + dir + " " + type + " " + loc);
        return f;
    }

    // the mask of this robot's last vision key (facing, radius, angle, bottom-left) and its box radius
    private byte[] bcxSenseMask;
    private Direction bcxSenseDir;
    private int bcxSenseR2, bcxSenseAngle, bcxSenseR;
    private boolean bcxSenseBl;

    private boolean bcxSense(MapLocation loc) {
        int r2 = getVisionRadiusSquared();
        int angle = getVisionConeAngle();
        boolean bl = type.usesBottomLeftLocationForDistance();
        MapLocation c = location;
        Direction d = dir;
        if (c == null || loc == null || d == null || gameWorld == null || r2 < 0 || r2 > 1 << 12) return canSenseLocation$orig(loc);
        byte[] mask = bcxSenseMask;
        if (mask == null || d != bcxSenseDir || r2 != bcxSenseR2 || angle != bcxSenseAngle || bl != bcxSenseBl) {
            GameWorld_p g = (GameWorld_p) (Object) gameWorld;
            bcxSenseMask = mask = g.bcxConeMask(d, (double) angle, r2, bl);
            bcxSenseR = g.bcxMaskR;
            bcxSenseDir = d;
            bcxSenseR2 = r2;
            bcxSenseAngle = angle;
            bcxSenseBl = bl;
        }
        int r = bcxSenseR;
        long dx = (long) loc.x - c.x, dy = (long) loc.y - c.y;
        if (dx < -r || dx > r || dy < -r || dy > r) return c.isWithinDistanceSquared(loc, r2, d, (double) angle, bl);
        int k = ((int) dx + r) * (2 * r + 1) + (int) dy + r;
        byte m = mask[k];
        if (m == 0) {
            m = c.isWithinDistanceSquared(loc, r2, d, (double) angle, bl) ? (byte) 2 : (byte) 1;
            mask[k] = m;
        }
        return m == 2;
    }

    @Orig
    public boolean canSenseLocation$orig(MapLocation loc) { return false; }

    @Orig
    public int getVisionRadiusSquared() { return 0; }

    @Orig
    public int getVisionConeAngle() { return 0; }
}
