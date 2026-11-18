package opp_cerr;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Explore {

    private static RobotController rc;

    static Direction exploreDir = null;
    static MapLocation exploreTarget = null;

    static int exploreDirUntil = 0;

    public static final int ROUNDS_IN_DIR = 40;
    public static final int DIST_TO_TARGET_BEFORE_CHANGING = 20;
    public static final int REROLLS = 3;

    public static void init(RobotController r) {
        rc = r;
        exploreDir = null;
        exploreTarget = null;
        exploreDirUntil = 0;
    }

    private static MapLocation projectToEdge(MapLocation loc, Direction dir) {
        int dx = dir.dx;
        int dy = dir.dy;

        int stepsX = Integer.MAX_VALUE;
        int stepsY = Integer.MAX_VALUE;

        if (dx > 0) stepsX = rc.getMapWidth() - 1 - loc.x;
        else if (dx < 0) stepsX = loc.x;

        if (dy > 0) stepsY = rc.getMapHeight() - 1 - loc.y;
        else if (dy < 0) stepsY = loc.y;

        int steps = Math.min(stepsX, stepsY);
        if (steps < 0) steps = 0;

        return loc.translate(dx * steps, dy * steps);
    }

    private static void pickNewExploreTarget() {
        MapLocation myLoc = rc.getLocation();
        Direction bestDir = null;
        int bestDist = -1;

        for (int i = 0; i < REROLLS; i++) {
            Direction d = Utils.DIRECTIONS[Robot.random.nextInt(8)];
            MapLocation edge = projectToEdge(myLoc, d);
            int dist = myLoc.distanceSquaredTo(edge);

            if (dist > bestDist) {
                bestDist = dist;
                bestDir = d;
            }
        }

        exploreDir = bestDir;
        exploreTarget = projectToEdge(myLoc, exploreDir);
        exploreDirUntil = rc.getRoundNum() + ROUNDS_IN_DIR;
    }

    public static MapLocation getExplore3Target() {
        MapLocation myLoc = rc.getLocation();
        int round = rc.getRoundNum();

        // Need to reset?
        if (exploreTarget == null
                || round >= exploreDirUntil
                || myLoc.distanceSquaredTo(exploreTarget) <= DIST_TO_TARGET_BEFORE_CHANGING) {

            pickNewExploreTarget();
        }

        return exploreTarget;
    }
}
