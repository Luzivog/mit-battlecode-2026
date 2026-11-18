package opp_cerr;

import battlecode.common.*;

import opp_cerr.fast.FastIntSet;
import opp_cerr.fast.FastLocSet;

public class BugNav {
    static RobotController rc;
    static MapLocation target;
    static final int INF = 1000000;
    static final int MAX_MAP_SIZE = GameConstants.MAP_MAX_HEIGHT;
    static boolean shouldGuessRotation = true; // if I should guess which rotation is the best
    static boolean rotateRight = true; // if I should rotate right or left
    static MapLocation lastObstacleFound = null; // latest obstacle I've found in my way
    static int minDistToEnemy =
            INF; // minimum distance I've been to the enemy while going around an obstacle
    static MapLocation prevTarget = null; // previous target
    static FastIntSet visited = new FastIntSet();
    static FastLocSet dontMove;
    static CERR_Random random = new CERR_Random();
    static MapLocation lastLoc;

    public static void init(RobotController r) {
        rc = r;
    }

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
        Direction.CENTER
    };

    public static void move(MapLocation loc) throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(loc) < 1) return;
        if (!rc.isMovementReady()) return;

        target = loc;
        rc.setIndicatorDot(target, 0, 255, 255);
        nav();
    }

    static boolean canMove(Direction dir) throws GameActionException {
        if (!rc.canMove(dir)) return false;
        return true;
    }

    static int distance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }

    static void guessRotation(Direction dir, MapLocation myLoc) throws GameActionException {
        // guess rotation only the first time we find a new "obstacle"
        shouldGuessRotation = false;

        // Debug.println("Guessing rot dir", id);
        // Rotate left and right and find the first dir that you can move in
        Direction dirL = dir;
        for (int j = 8; j-- > 0; ) {
            if (canMove(dirL)) break;
            dirL = dirL.rotateLeft();
        }

        Direction dirR = dir;
        for (int j = 8; j-- > 0; ) {
            if (canMove(dirR)) break;
            dirR = dirR.rotateRight();
        }

        // Check which results in a location closer to the target
        MapLocation locL = myLoc.add(dirL);
        MapLocation locR = myLoc.add(dirR);

        rc.setIndicatorDot(locR, 255, 0, 255);
        rc.setIndicatorDot(locL, 255, 0, 255);

        int lDist = distance(target, locL);
        int rDist = distance(target, locR);

        if (lDist < rDist) {
            rotateRight = false;
        } else if (rDist < lDist) {
            rotateRight = true;
        } else {
            if (rc.getLocation().x > rc.getMapWidth() / 2) rotateRight = true;
            else rotateRight = false;
        }
    }

    private static void tryRemoveDirt() throws GameActionException {
        if (!rc.getLocation().equals(lastLoc)) {
            lastLoc = rc.getLocation();
            return;
        }
        if (!rc.isActionReady()) return;
        MapLocation loc;
        loc = rc.getLocation().add(rc.getDirection());
        if (rc.canRemoveDirt(loc)) {
            rc.removeDirt(loc);
            return;
        }
        loc = rc.getLocation().add(rc.getDirection().rotateLeft());
        if (rc.canRemoveDirt(loc)) {
            rc.removeDirt(loc);
            return;
        }
        loc = rc.getLocation().add(rc.getDirection().rotateRight());
        if (rc.canRemoveDirt(loc)) {
            rc.removeDirt(loc);
        }
    }

    static boolean nav() throws GameActionException {
        tryRemoveDirt();

        try {
            // different target? ==> previous data does not help!
            if (prevTarget == null || target.distanceSquaredTo(prevTarget) > 0) {
                resetPathfinding();
            }
            prevTarget = target;

            // If I'm at a minimum distance to the target, I'm free!
            MapLocation myLoc = rc.getLocation();
            int d = distance(myLoc, target);
            if (d < minDistToEnemy) {
                resetPathfinding();
                minDistToEnemy = d;
            }

            int code = getCode();

            if (visited.contains(code)) {
                // Debug.println("Contains code", id);
                resetPathfinding();
            }
            visited.add(code);

            // If there's an obstacle I try to go around it [until I'm free] instead of
            // going to the target directly
            Direction dir = myLoc.directionTo(target);
            // TODO: what happens if lastObstacleFound is cleared by someone else before our turn?
            if (lastObstacleFound != null) {
                // Debug.println("Last obstacle found: " + lastObstacleFound, id);
                dir = myLoc.directionTo(lastObstacleFound);
            } else if (canMove(dir)) {
                // Debug.println("can move: " + dir, id);
                resetPathfinding();
            }

            // I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try
            // to go out of the map I change the orientation
            // Note that we have to try at most 16 times since we can switch orientation in
            // the middle of the loop. (It can be done more efficiently)
            for (int i = 8; i-- > 0; ) {
                MapLocation newLoc = myLoc.add(dir);
                // rc.setIndicatorDot(newLoc, 0, 255, 0);
                if (canMove(dir)) {
                    // TODO: save 5 bytecode here
                    rc.move(dir);
                    return true;
                }

                // TODO: supplement this with additional information from Navigation.java

                if (!rc.isMovementReady()) {
                    // somehow ran out of bytecode
                    System.out.println("This should not happen.");
                    break;
                }

                // At this point, it is guaranteed that we cannot move here.
                // 1. There is a wall
                // 2. There is a robot
                // 3. It is off the map

                if (!rc.onTheMap(newLoc)) {
                    rotateRight = !rotateRight;
                } else {
                    // This is an obstacle
                    lastObstacleFound = newLoc;
                    rc.setIndicatorDot(lastObstacleFound, 255, 0, 0);

                    // shouldGuessRotation = false; // temporarility disable this
                    if (shouldGuessRotation) {
                        guessRotation(dir, myLoc);
                    }
                }

                if (rotateRight) dir = dir.rotateRight();
                else dir = dir.rotateLeft();
            }

            if (canMove(dir)) {
                if (rc.canTurn()) rc.turn(dir);
                rc.move(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    // clear some of the previous data
    static void resetPathfinding() {
        // Debug.println("Resetting pathfinding", id);
        lastObstacleFound = null;
        minDistToEnemy = INF;
        visited.clear();
        shouldGuessRotation = true;
    }

    static int getCode() {
        int x = rc.getLocation().x;
        int y = rc.getLocation().y;
        Direction obstacleDir = rc.getLocation().directionTo(target);
        if (lastObstacleFound != null)
            obstacleDir = rc.getLocation().directionTo(lastObstacleFound);
        int bit = rotateRight ? 1 : 0;
        return (((((x << 6) | y) << 4) | obstacleDir.ordinal()) << 1) | bit;
    }
}
