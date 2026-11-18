package opp_powerpuff.Helpers;

import battlecode.common.*;

import opp_powerpuff.Rat;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Utils.RNG;

public class BugNav {

    public static final int STUCK_THRESHOLD = 4;
    public static MapLocation lastTargetLocation;
    public static MapLocation stuckLastLocation = null;
    public static int stuckTurns = 0;

    // resset bug
    public static int MAX_STACK_SIZE = 100;
    public static Direction[] bugStack = new Direction[MAX_STACK_SIZE];
    public static int bugStackIndex = 0;
    public static boolean RIGHT = true;
    public static boolean rlook = true;

    // location
    public static MapLocation lastLocation;
    public static MapLocation currentLocation = Rat.rc.getLocation();

    public static void moveTo(MapLocation target) throws GameActionException {
        RobotController rc = Rat.rc;

        if (!rc.isMovementReady()) return;

        if (currentLocation == null)  {
            currentLocation = rc.getLocation();
            lastLocation = rc.getLocation();
        }

        rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        if (isStuck()) {
            resetBug();
            RIGHT = !RIGHT;

            stuckTurns = 0;
            stuckLastLocation = rc.getLocation();
            return;
        }

        updateLocationTracking();
        handleTargetReset(target);
        popStackIfPossible();

        if (canGoDirect(target)) {
            resetBug();
        }

        if (bugStackIndex == 0) {
            if (tryDirectMove(target)) return; // moved successuflly

            // greedy failed -> enter bug mode
            startBug(target);
        }

        // bug mode
        followWall();

    }


    public static void reset() {
        resetBug();
        lastTargetLocation = null;
        stuckLastLocation = null;
        stuckTurns = 0;
    }


    public static void startBug(MapLocation target) throws GameActionException {
        RobotController rc = Rat.rc;
        Direction toTarget = rc.getLocation().directionTo(target);
        bugStack[bugStackIndex++] = RIGHT ? toTarget.rotateLeft() : toTarget.rotateRight();
    }

    public static void resetBug() {
        bugStack = new Direction[MAX_STACK_SIZE];
        bugStackIndex = 0;
    }

    public static void handleTargetReset(MapLocation target) {
        RobotController rc = Rat.rc;
        if (lastTargetLocation  == null
            || lastTargetLocation.distanceSquaredTo(target) > 8
            || bugStackIndex >= MAX_STACK_SIZE - 10) {

            resetBug();
            lastTargetLocation = target;
            lastLocation = rc.getLocation();
        } else {
            lastTargetLocation = target;
        }
    }

    public static void followWall() throws GameActionException {
        RobotController rc = Rat.rc;
        Direction dir = bugStack[bugStackIndex - 1];

        for (int i = 0; i < 8; i++) {

            // rotate around obstacle
            dir = RIGHT ? dir.rotateRight() : dir.rotateLeft();
            MapLocation next = rc.getLocation().add(dir);

            // hit map edge -> flip sid eand reset
            if (!rc.onTheMap(next)) {
                RIGHT = !RIGHT;
                resetBug();
                return;
            }

            MapInfo info = MapData.getMapInfo(next);
            boolean blocked = !rc.canMove(dir) && (info == null || !info.isDirt() || !(rc.canRemoveDirt(next) && Rat.rc.getAllCheese() > 0));

            // blocked -> remember as part of wall
            if (blocked) {
                if (bugStackIndex >= MAX_STACK_SIZE - 1) {
                    resetBug();
                    RIGHT = !RIGHT;
                    return;
                }
                bugStack[bugStackIndex++] = dir; 
            } else {
                wrappedStrafeMove(dir);
                return;
            }
        }
    }

    public static void updateLocationTracking() throws GameActionException {
        RobotController rc = Rat.rc;
        lastLocation = currentLocation;
        currentLocation = rc.getLocation();
    }

    public static boolean ignoreEmptyTiles() {
        RobotController rc = Rat.rc;
        return rc.getType() == UnitType.BABY_RAT || rc.getType() == UnitType.RAT_KING;
    }

    public static boolean canGoDirect(MapLocation target) throws GameActionException {
        RobotController rc = Rat.rc;
        return reachableFrom(rc.getLocation(), target);
    }

    public static void popStackIfPossible() throws GameActionException {
        RobotController rc = Rat.rc;
        while (bugStackIndex > 0) {
            Direction top = bugStack[bugStackIndex - 1];
            MapLocation next = rc.getLocation().add(top);

            if (!rc.onTheMap(next)) break;

            MapInfo info = MapData.getMapInfo(next);
            boolean traversable = rc.canMove(top) || (info != null && info.isDirt() && (rc.canRemoveDirt(next) && Rat.rc.getAllCheese() > 0));

            if (traversable && !next.equals(lastLocation)) {
                bugStackIndex--;
            } else {
                break;
            }
        }
    }

    public static boolean tryDirectMove(MapLocation target) throws GameActionException {
        RobotController rc = Rat.rc;
        Direction dir = rc.getLocation().directionTo(target);

        Direction[] options = {
            dir,
            (rlook) ? dir.rotateRight() : dir.rotateLeft(),
            (rlook) ? dir.rotateLeft() : dir.rotateRight()
        };

        int bestScore = -9999;
        Direction bestDir = null;

        for (Direction d : options) {
            MapLocation next = rc.getLocation().add(d);
            if (!rc.onTheMap(next)) continue;
            if (next.equals(lastLocation)) continue;

            MapInfo info = MapData.getMapInfo(next);
            boolean traversable = rc.canMove(d) || (info != null && info.isDirt() && (rc.canRemoveDirt(next) && Rat.rc.getAllCheese() > 0));

            if (!traversable) continue;
            if (next.equals(lastLocation)) continue;

            int score = tileScore(next, target, ignoreEmptyTiles());
            if (score > bestScore) {
                bestScore = score;
                bestDir = d;
            }
        }


        if (bestDir != null) {
            wrappedStrafeMove(bestDir);
            return true;
        }

        return false;

    }

    // Helper functions
    public static boolean reachableFrom(MapLocation start, MapLocation target) throws GameActionException {
        if (!Rat.rc.onTheMap(target)) return false;


        MapInfo targetInfo = MapData.mapInfos[target.x][target.y];
        if (targetInfo == null || targetInfo.isWall()) {
            return false;
        }

        MapLocation curr = start;
        int steps = 0;

        while (!curr.equals(target)) {
            Direction dir = curr.directionTo(target);
            MapLocation next = curr.add(dir);

            // off map check
            if (!Rat.rc.onTheMap(next)) {
                return false;
            }

            MapInfo info = MapData.mapInfos[next.x][next.y];
            if (info == null || info.isWall()) return false;

            curr = next;

            if (++steps > 50) return false;
               
        }

        return true;

    }

    public static void wrappedStrafeMove(Direction d) throws GameActionException {
        Nav.removeDirtPriority(d);
        Direction turnDirection;
        if (rlook) 
            turnDirection = d.rotateRight();
        else
            turnDirection = d.rotateLeft();
        boolean success = Nav.strafeMove(d, turnDirection);
        if (success)
            rlook = !rlook;
    }


    public static boolean isStuck() throws GameActionException {
        RobotController rc = Rat.rc;
        MapLocation curr = rc.getLocation();

        if (stuckLastLocation != null && curr.equals(stuckLastLocation)) {
            stuckTurns++;
        } else {
            stuckTurns = 0;
        }

        stuckLastLocation = curr;
        return stuckTurns >= STUCK_THRESHOLD;
    }

    public static boolean tooCloseToEnemyTower(MapLocation loc) throws GameActionException {
        return false;
    }

    public static int tileScore(MapLocation loc, MapLocation target, boolean ignoreEmpty) throws GameActionException{
        return tileScore(loc, target, ignoreEmpty, false);
    }

    public static int tileScore(MapLocation loc, MapLocation target, boolean ignoreEmpty, boolean checkAllyBehind) throws GameActionException {
        RobotController rc = Rat.rc;
        int score = 0;

        // impossible tiles
        if (!rc.onTheMap(loc)) return -9999;

        MapInfo info = MapData.getMapInfo(loc);
        if (info == null) return -500;
        if (info.isWall()) return -9999;

        score -= loc.distanceSquaredTo(target);

        if (info.isDirt()) {
            if (rc.isActionReady() && rc.getAllCheese() > Constants.KING_CHEESE_THRESHOLD) return -5000;
            else return -9999;
        }

        if (rc.canSenseRobotAtLocation(loc)) {
            if (rc.senseRobotAtLocation(loc) != null) {
                return -9999;
            }
        }

        // if (VisionManager.lastSeenClosestCat != null)
        //     score += Rat.distTo(VisionManager.lastSeenClosestCat.getLocation());

        return score;
    }
    
}
