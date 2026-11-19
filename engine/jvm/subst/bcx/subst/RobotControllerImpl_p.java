package bcx.subst;

import battlecode.common.MapLocation;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

/** Fast path of battlecode.world.RobotControllerImpl.canSenseLocation, which calls assertCanSenseLocation and turns its
 *  GameActionException into false: the same two tests (on the map, then the robot's vision), without building the
 *  exception. A null location still throws through the original. The original stays as canSenseLocation$orig;
 *  FastPath.CHECK runs both. */
public final class RobotControllerImpl_p {
    @Orig private GameWorld gameWorld;
    @Orig private InternalRobot robot;

    public boolean canSenseLocation(MapLocation loc) {
        if (loc == null) return canSenseLocation$orig(loc);
        boolean f = gameWorld.getGameMap().onTheMap(loc) && robot.canSenseLocation(loc);
        if (FastPath.CHECK && f != canSenseLocation$orig(loc)) FastPath.fail("RobotControllerImpl.canSenseLocation: " + loc);
        return f;
    }

    @Orig
    public boolean canSenseLocation$orig(MapLocation loc) { return false; }
}
