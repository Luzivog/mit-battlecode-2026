package opp_goobie;

import battlecode.common.*;

public class BugNav {
    static int MAX_STACK_SIZE = 100;
    static Direction[] bugStack = new Direction[MAX_STACK_SIZE];
    static int bugStackIndex = 0;
    static MapLocation lastTargetLocation = null;
    static MapLocation lastLocation;
    static MapLocation currentLocation;
    static int stuckTurns = 0;
    static boolean jiggleRight = false;

    static boolean allowedToTurnForFun = true;
    static boolean turnRight = true;

    static void moveTo(MapLocation loc) throws GameActionException {
        if (loc == null)
            return;
        bugNav(loc);

        if (Robot.rc.getType().isBabyRatType() && allowedToTurnForFun && Robot.rc.canTurn()) {
            Direction d = Robot.rc.getDirection();
            if (turnRight)
                Robot.rc.turn(d.rotateRight().rotateRight());
            else
                Robot.rc.turn(d.rotateLeft().rotateLeft());

            turnRight = !turnRight;
        }
    }

    static void bugNav(MapLocation loc) throws GameActionException {
        if (!Robot.rc.isMovementReady()) {
            return;
        }

        MapLocation me = Robot.rc.getLocation();

        if (me.equals(lastLocation)) {
            stuckTurns++;
            if (stuckTurns >= 6) {
                Debug.addIndicatorString("STUCK! Random escape");
                bugStack = new Direction[MAX_STACK_SIZE];
                bugStackIndex = 0;
                stuckTurns = 0;

                Direction[] dirs = Direction.values();
                for (int i = 0; i < 8; i++) {
                    int randomIdx = (int) (Math.random() * 8);
                    Direction randomDir = dirs[randomIdx];
                    if (randomDir != Direction.CENTER && MovementManager.canMove(randomDir)) {
                        MovementManager.move(randomDir);
                        return;
                    }
                }
            }
        } else {
            stuckTurns = 0;
        }

        lastLocation = currentLocation;
        currentLocation = me;
        if (lastTargetLocation == null || lastTargetLocation.distanceSquaredTo(loc) > 8
            || bugStackIndex >= MAX_STACK_SIZE - 10) {
            bugStack = new Direction[MAX_STACK_SIZE];
            bugStackIndex = 0;
            lastTargetLocation = loc;
            lastLocation = me;
        }
        if (lastTargetLocation != null && lastTargetLocation.distanceSquaredTo(loc) <= 8) {
            lastTargetLocation = loc;
        }

        while (bugStackIndex != 0 && (Robot.rc.canMove(bugStack[bugStackIndex - 1])
            || (bugStackIndex > 1 && Robot.rc.canMove(bugStack[bugStackIndex - 2])
            && !me.add(bugStack[bugStackIndex - 2]).equals(lastLocation)))) {
            bugStackIndex--;
        }
        if (reachableFrom(me, loc)) {
            bugStack = new Direction[MAX_STACK_SIZE];
            bugStackIndex = 0;
        }

        // going directly to the target
        if (bugStackIndex == 0) {
            Direction dirToTarget = me.directionTo(loc);
            Direction bestDir = null;
            int bestScore = -9999;
            int score1 = tileScore(me.add(dirToTarget));
            int score2 = tileScore(me.add(dirToTarget.rotateLeft()));
            int score3 = tileScore(me.add(dirToTarget.rotateRight()));

            // jiggle if cardinal
            if (dirToTarget.ordinal() % 2 == 0 && me.distanceSquaredTo(loc) > 50) {
                if (Robot.rc.getRoundNum() % 4 < 2)
                    score2++;
                else
                    score3++;
            }

            if (MovementManager.canMove(dirToTarget) && score1 > bestScore
                && !me.add(dirToTarget).equals(lastLocation)) {
                bestDir = dirToTarget;
                bestScore = score1;
            }
            if (MovementManager.canMove(dirToTarget.rotateLeft()) && score2 > bestScore
                && !me.add(dirToTarget.rotateLeft()).equals(lastLocation)) {
                bestDir = dirToTarget.rotateLeft();
                bestScore = score2;
            }
            if (MovementManager.canMove(dirToTarget.rotateRight()) && score3 > bestScore
                && !me.add(dirToTarget.rotateRight()).equals(lastLocation)) {
                bestDir = dirToTarget.rotateRight();
                bestScore = score3;
            }
            if (bestDir != null && bestScore > -20) {
                MovementManager.move(bestDir);
                return;
            }

            // check if all units are robots
            MapLocation locCheck = me.add(dirToTarget);
            boolean checkFrontRobot = Robot.rc.onTheMap(locCheck) && Robot.rc.canSenseRobotAtLocation(locCheck);
            locCheck = me.add(dirToTarget.rotateLeft());
            boolean checkLeftRobot = Robot.rc.onTheMap(locCheck) && Robot.rc.canSenseRobotAtLocation(locCheck);
            locCheck = me.add(dirToTarget.rotateRight());
            boolean checkRightRobot = Robot.rc.onTheMap(locCheck) && Robot.rc.canSenseRobotAtLocation(locCheck);
            if (checkFrontRobot && checkLeftRobot && checkRightRobot) {
                fuzzyMove(dirToTarget.opposite());
                return;
            }

            bugStack[bugStackIndex] = Robot.right ? dirToTarget.rotateLeft() : dirToTarget.rotateRight();
            bugStackIndex++;
        }
        // add subsequent directions if cant move towards the target
        if (Robot.right) {
            Direction dir = bugStack[bugStackIndex - 1].rotateRight();
            for (int i = 0; i < 8; i++) {
                if (!MovementManager.canMove(dir)) {
                    if (!Robot.rc.onTheMap(me.add(dir))) {
                        bugStack = new Direction[MAX_STACK_SIZE];
                        bugStackIndex = 0;
                        Robot.right = !Robot.right;
                        break;
                    }
                    bugStack[bugStackIndex] = dir;
                    bugStackIndex++;
                } else {
                    // debugString.append("Wall Following ");
                    MovementManager.move(dir);
                    return;
                }
                dir = dir.rotateRight();
            }
        } else {
            Direction dir = bugStack[bugStackIndex - 1].rotateLeft();
            for (int i = 0; i < 8; i++) {
                if (!MovementManager.canMove(dir)) {
                    if (!Robot.rc.onTheMap(me.add(dir))) {
                        bugStack = new Direction[MAX_STACK_SIZE];
                        bugStackIndex = 0;
                        Robot.right = !Robot.right;
                        break;
                    }
                    bugStack[bugStackIndex] = dir;
                    bugStackIndex++;
                } else {
                    // debugString.append("Wall Following ");
                    MovementManager.move(dir);
                    return;
                }
                dir = dir.rotateLeft();
            }
        }
    }

    static int tileScore(MapLocation loc) throws GameActionException {
        if (!Robot.rc.onTheMap(loc) || MapData.isWall(loc))
            return -9999;

        int score = 0;
        if (MapData.isDirt(loc))
            score -= 20;

        return score;
    }

    static boolean reachableFrom(MapLocation loc, MapLocation target) throws GameActionException {
        if (!MapData.isPassable(target))
            return false;

        MapLocation checkLoc = loc;
        int i = 8;
        while (--i >= 0) {
            if (!Robot.rc.onTheMap(checkLoc) || !MapData.isPassable(checkLoc)) {
                return false;
            }
            checkLoc = checkLoc.add(checkLoc.directionTo(target));
        }
        return true;
    }

    static void fuzzyMove(Direction dir) throws GameActionException {
        for (Direction d : fuzzyDirs(dir)) {
            if (Robot.rc.canMove(d)) {
                MovementManager.move(d);
                return;
            }
        }
    }

    static Direction[] fuzzyDirs(Direction dir) throws GameActionException {
        if (!Robot.right) {
            return new Direction[]{
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight()
            };
        } else {
            return new Direction[]{
                dir,
                dir.rotateRight(),
                dir.rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
            };
        }
    }
}
