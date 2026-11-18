package opp_gravy;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class BugNavigator extends RobotPlayer {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static Direction tracingDir;
    private static boolean tracing;
    private static boolean side;
    private static int rotateCount;
    private static int traceCount;
    private static Direction lastMoveDir;
    private static int straightCount;

    public static boolean canMove(Direction dir, boolean breakDirt) throws GameActionException {
        if (rc.canMove(dir)) return true;
        if (!breakDirt) return false;
        if (!rc.canSenseLocation(rc.getLocation().add(dir))) return false;
        MapInfo tile = rc.senseMapInfo(rc.getLocation().add(dir));
        if (tile.isDirt()) return true;
        return false;
    }

    public static void moveTo(MapLocation target) throws GameActionException {
        moveTo(target, false, false);
    }

    public static void moveTo(MapLocation target, boolean breakDirt) throws GameActionException {
        moveTo(target, breakDirt, false);
    }

    public static void moveTo(MapLocation target, boolean breakDirt, boolean radar) throws GameActionException {
        if (BabyRat.willNotMove){
            return;
        }
        MapLocation loc = rc.getLocation();
        if (loc.equals(target)){
            return;
        }
        // rc.setIndicatorDot(target, 0, 0, 255);
        if (!rc.isMovementReady())
            return;
        if (currentTarget == null || currentTarget.distanceSquaredTo(target) > 2) {
            reset(target);
        }
        else if (!currentTarget.equals(target)){
            currentTarget = target;
            minDistanceToTarget = distance(loc, target);
        }

        if (distance(loc, target) < minDistanceToTarget) {
            minDistanceToTarget = distance(loc, target);
            tracing = false;
        }

        if (!tracing) {
            Direction dir = dirTo(target);
            if (rc.onTheMap(rc.getLocation().add(dir)) && canMove(dir, breakDirt)) {
                if (radar) moveWithRadar(dir, breakDirt);
                else moveWithTurn(dir, breakDirt);
                traceCount = 0;
                return;
            }
            tracing = true;

            Direction L = dir, R = dir;
            int cntL = 0, cntR = 0;
            while ((!canMove(R, breakDirt) || !rc.onTheMap(loc.add(R))) && cntR <= 7) {
                R = R.rotateRight();
                cntR++;
            }
            while ((!canMove(L, breakDirt) || !rc.onTheMap(loc.add(L))) && cntL <= 7) {
                L = L.rotateLeft();
                cntL++;
            }
            if (cntL > 7 && cntR > 7){
                tracing = false;
                return;
            }

            if (cntL < cntR) {
                dir = L;
                side = true;
            } else if (cntL > cntR) {
                dir = R;
                side = false;
            }
            else if (checkRightBias(target, L, R)){
                dir = R;
                side = false;
            }
            else {
                dir = L;
                side = true;
            }
            tracingDir = dir;
            moveWithTurn(tracingDir, breakDirt);
            rotateCount = 0;
            return;
        }

        if (tracingDir == null){
            tracing = false;
            return;
        }

        // If you trace too much, reset already
        traceCount++;
        if (traceCount >= mapWidth + mapHeight){
            reset(target);
            return;
        }
        if (!side) {
            tracingDir = tracingDir.rotateLeft();
            tracingDir = tracingDir.rotateLeft();
            int turn = -2;
            for (int i = 0; i < 8; i++) {
                if (canMove(tracingDir, breakDirt) && rc.onTheMap(loc.add(tracingDir))) {
                    rotateCount += turn;
                    if (rotateCount <= -8 || rotateCount >= 8) reset(target);
                    moveWithTurn(tracingDir, breakDirt);
                    return;
                }
                turn++;
                tracingDir = tracingDir.rotateRight();
            }
            tracingDir = tracingDir.rotateRight();
            tracingDir = tracingDir.rotateRight();
        } else {
            tracingDir = tracingDir.rotateRight();
            tracingDir = tracingDir.rotateRight();
            int turn = 2;
            for (int i = 0; i < 8; i++) {
                if (canMove(tracingDir, breakDirt) && rc.onTheMap(loc.add(tracingDir))) {
                    rotateCount += turn;
                    if (rotateCount <= -8 || rotateCount >= 8) reset(target);
                    moveWithTurn(tracingDir, breakDirt);
                    return;
                }
                turn--;
                tracingDir = tracingDir.rotateLeft();
            }
            tracingDir = tracingDir.rotateLeft();
            tracingDir = tracingDir.rotateLeft();
        }
    }

    public static void reset(MapLocation target) {
        currentTarget = target;
        minDistanceToTarget = distance(rc.getLocation(), target);
        tracingDir = null;
        tracing = false;
        rotateCount = 0;
        traceCount = 0;
        lastMoveDir = null;
        straightCount = 0;
    }

    private static int sign(int n) {
        if (n > 0)
            return 1;
        if (n < 0)
            return -1;
        return 0;
    }

    private static Direction dirTo(MapLocation target) {
        MapLocation loc = rc.getLocation();
        int dx = target.x - loc.x, dy = target.y - loc.y;
        int[] w = new int[8];
        // unroll loops
        // w[dir] = cos^2 * sign(cos)
        w[0] = dx * 0 + dy * 1; // NORTH
        w[1] = dx * 1 + dy * 1; // NORTHEAST
        w[2] = dx * 1 + dy * 0; // EAST
        w[3] = dx * 1 + dy * -1; // SOUTHEAST
        w[4] = dx * 0 + dy * -1; // SOUTH
        w[5] = dx * -1 + dy * -1; // SOUTHWEST
        w[6] = dx * -1 + dy *0; // WEST
        w[7] = dx * -1 + dy * 1; // NORTHWEST
        
        w[0] *= w[0] * sign(w[0]);
        w[1] *= w[1] * sign(w[1]);
        w[2] *= w[2] * sign(w[2]);
        w[3] *= w[3] * sign(w[3]);
        w[4] *= w[4] * sign(w[4]);
        w[5] *= w[5] * sign(w[5]);
        w[6] *= w[6] * sign(w[6]);
        w[7] *= w[7] * sign(w[7]);
        
        w[1] /= 2;
        w[3] /= 2;
        w[5] /= 2;
        w[7] /= 2;

        int res = 0;
        res = Math.max(res, w[0]);
        res = Math.max(res, w[1]);
        res = Math.max(res, w[2]);
        res = Math.max(res, w[3]);
        res = Math.max(res, w[4]);
        res = Math.max(res, w[5]);
        res = Math.max(res, w[6]);
        res = Math.max(res, w[7]);

        if (res == w[0]) return Direction.NORTH;
        if (res == w[1]) return Direction.NORTHEAST;
        if (res == w[2]) return Direction.EAST;
        if (res == w[3]) return Direction.SOUTHEAST;
        if (res == w[4]) return Direction.SOUTH;
        if (res == w[5]) return Direction.SOUTHWEST;
        if (res == w[6]) return Direction.WEST;
        if (res == w[7]) return Direction.NORTHWEST;
        return Direction.CENTER;
    }

    private static int distance(MapLocation a, MapLocation b) {
        return a.distanceSquaredTo(b);
    }

    private static boolean checkRightBias(MapLocation target, Direction dirL, Direction dirR){
        MapLocation loc = rc.getLocation();
        int dx = target.x - loc.x, dy = target.y - loc.y;
        int[] w = new int[8];
        // unroll loops
        // w[dir] = cos^2 * sign(cos)
        w[0] = dx * 0 + dy * 1; // NORTH
        w[1] = dx * 1 + dy * 1; // NORTHEAST
        w[2] = dx * 1 + dy * 0; // EAST
        w[3] = dx * 1 + dy * -1; // SOUTHEAST
        w[4] = dx * 0 + dy * -1; // SOUTH
        w[5] = dx * -1 + dy * -1; // SOUTHWEST
        w[6] = dx * -1 + dy *0; // WEST
        w[7] = dx * -1 + dy * 1; // NORTHWEST
        
        w[0] *= w[0] * sign(w[0]);
        w[1] *= w[1] * sign(w[1]);
        w[2] *= w[2] * sign(w[2]);
        w[3] *= w[3] * sign(w[3]);
        w[4] *= w[4] * sign(w[4]);
        w[5] *= w[5] * sign(w[5]);
        w[6] *= w[6] * sign(w[6]);
        w[7] *= w[7] * sign(w[7]);
        
        w[1] /= 2;
        w[3] /= 2;
        w[5] /= 2;
        w[7] /= 2;

        int L, R;
        switch(dirL){
            case Direction.NORTH -> {L = 0;}
            case Direction.NORTHEAST -> {L = 1;}
            case Direction.EAST -> {L = 2;}
            case Direction.SOUTHEAST -> {L = 3;}
            case Direction.SOUTH -> {L = 4;}
            case Direction.SOUTHWEST -> {L = 5;}
            case Direction.WEST -> {L = 6;}
            case Direction.NORTHWEST -> {L = 7;}
            default -> {L = 0;}
        }
        switch(dirR){
            case Direction.NORTH -> {R = 0;}
            case Direction.NORTHEAST -> {R = 1;}
            case Direction.EAST -> {R = 2;}
            case Direction.SOUTHEAST -> {R = 3;}
            case Direction.SOUTH -> {R = 4;}
            case Direction.SOUTHWEST -> {R = 5;}
            case Direction.WEST -> {R = 6;}
            case Direction.NORTHWEST -> {R = 7;}
            default -> {R = 0;}
        }
        return w[R] > w[L];
    }

    private static void moveWithTurn(Direction dir, boolean breakDirt) throws GameActionException {
        if (dir == Direction.CENTER) return;
        if (dir == null){
            tracing = false;
            return;
        }
        if (rc.onTheMap(rc.getLocation().add(dir))) {
            if (rc.canTurn()) rc.turn(dir);
            move(dir, breakDirt);
        }
        if (distance(rc.getLocation(), currentTarget) < minDistanceToTarget) {
            minDistanceToTarget = distance(rc.getLocation(), currentTarget);
            tracing = false;
        }
    }

    private static void moveWithRadar(Direction dir, boolean breakDirt) throws GameActionException {
        if (dir == Direction.CENTER || dir == null) return;
        if (!rc.onTheMap(rc.getLocation().add(dir))) return;

        if (dir == lastMoveDir) {
            straightCount++;
        } else {
            lastMoveDir = dir;
            straightCount = 0;
        }

        if ((straightCount & 1) == 0) {
            if (rc.canTurn()) rc.turn(dir);
            move(dir, breakDirt);
            return;
        }

        if ((straightCount & 3) == 1) {
            move(dir, breakDirt);
            if (rc.canTurn()) rc.turn(dir.rotateLeft());
            return;
        }

        if ((straightCount & 3) == 3) {
            move(dir, breakDirt);
            if (rc.canTurn()) rc.turn(dir.rotateRight());
        }
    }
}
