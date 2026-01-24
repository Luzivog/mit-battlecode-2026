package awubot_quals.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot_quals.CellTracker;
import awubot_quals.Pathfinder;
import awubot_quals.comms.GlobalArray;
import awubot_quals.robot.Strategy;
import static awubot_quals.Game.*;
import static awubot_quals.util.Util.*;

public class CatLuringStrategy extends Strategy {
    private static final int LURE_TTL = 40;
    private static final int CAT_LOST_THRESHOLD = 8;
    private static final int SQUEAK_INTERVAL = 2;
    private static final int MISSION_COMPLETE_DIST_SQ = 49;
    private static final int MIN_TURN_FOR_AUTO = 20;

    private MapLocation catLoc = null;
    private MapLocation enemyKingLoc = null;
    private int lureTTL = 0;
    private boolean isActive = false;
    private int lastCatSeenRound = 0;
    private int squeakCooldown = 0;
    private int missionsCompleted = 0;

    public void activateLure(MapLocation aCatLoc, MapLocation aEnemyKingLoc) {
        catLoc = aCatLoc;
        enemyKingLoc = aEnemyKingLoc;
        lureTTL = LURE_TTL;
        isActive = true;
        lastCatSeenRound = time;
        System.out.println("[LURE] R" + rc.getID() + " t=" + time + " ACTIVATED cat=" + aCatLoc + " target=" + aEnemyKingLoc);
    }

    public boolean tryAutoActivate() throws GameActionException {
        if (isActive) return false;
        if (time < MIN_TURN_FOR_AUTO) return false;

        MapLocation myCatLoc = null;
        for (RobotInfo myRobot : rc.senseNearbyRobots()) {
            if (myRobot.getType() == UnitType.CAT) {
                myCatLoc = myRobot.location;
                break;
            }
        }
        if (myCatLoc == null) return false;

        MapLocation myEnemyKing = getEnemyKingLocation();
        if (myEnemyKing == null) return false;

        activateLure(myCatLoc, myEnemyKing);
        return true;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if (!isActive) return false;

        lureTTL--;
        indicate("LURING[" + lureTTL + "] -> " + enemyKingLoc);

        if (enemyKingLoc != null) {
            rc.setIndicatorLine(myLoc, enemyKingLoc, 255, 0, 255);
        }
        if (catLoc != null) {
            rc.setIndicatorLine(myLoc, catLoc, 255, 0, 0);
        }

        boolean myCatNearby = updateCatTracking();

        System.out.println("[LURE] R" + rc.getID() + " t=" + time +
            " active=" + isActive + " ttl=" + lureTTL +
            " cat=" + catLoc + " catNear=" + myCatNearby +
            " target=" + enemyKingLoc + " missions=" + missionsCompleted);

        if (shouldTerminateLure(myCatNearby)) {
            terminateLure(myCatNearby);
            return true;
        }

        executeOffensiveMovement();
        executeSqueaking();
        return true;
    }

    private boolean updateCatTracking() {
        for (RobotInfo myRobot : rc.senseNearbyRobots()) {
            if (myRobot.getType() == UnitType.CAT) {
                catLoc = myRobot.location;
                lastCatSeenRound = time;
                return true;
            }
        }
        return false;
    }

    private boolean shouldTerminateLure(boolean aCatNearby) {
        if (lureTTL <= 0) {
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " TTL expired");
            return true;
        }

        if (enemyKingLoc != null &&
            myLoc.distanceSquaredTo(enemyKingLoc) <= MISSION_COMPLETE_DIST_SQ) {
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " MISSION COMPLETE - reached enemy!");
            return true;
        }

        if (!aCatNearby && (time - lastCatSeenRound) > CAT_LOST_THRESHOLD) {
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " Cat lost");
            return true;
        }

        if (rc.getHealth() <= 30) {
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " Aborting - low HP");
            return true;
        }

        return false;
    }

    private void terminateLure(boolean aWasSuccessful) {
        if (aWasSuccessful && enemyKingLoc != null &&
            myLoc.distanceSquaredTo(enemyKingLoc) <= MISSION_COMPLETE_DIST_SQ) {
            missionsCompleted++;
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " SUCCESS! Total missions: " + missionsCompleted);
        }

        isActive = false;
        catLoc = null;
        enemyKingLoc = null;
    }

    private void executeOffensiveMovement() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (enemyKingLoc == null) return;

        Direction myToEnemy = myLoc.directionTo(enemyKingLoc);
        Direction[] myTryDirs = {
            myToEnemy,
            myToEnemy.rotateLeft(),
            myToEnemy.rotateRight(),
            myToEnemy.rotateLeft().rotateLeft(),
            myToEnemy.rotateRight().rotateRight()
        };

        for (Direction myDir : myTryDirs) {
            if (rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
                return;
            }
        }

        Pathfinder.makeMove(enemyKingLoc);
    }

    private void executeSqueaking() {
        squeakCooldown--;
        if (squeakCooldown > 0) return;

        rc.squeak(0);
        squeakCooldown = SQUEAK_INTERVAL;
    }

    private MapLocation getEnemyKingLocation() {
        int myX = GlobalArray.nearestRatKing.x;
        int myY = GlobalArray.nearestRatKing.y;

        switch (CellTracker.getSymmetry()) {
            case Rotational:
                return new MapLocation(mapWidth - 1 - myX, mapHeight - 1 - myY);
            case Horizontal:
                return new MapLocation(mapWidth - 1 - myX, myY);
            case Vertical:
                return new MapLocation(myX, mapHeight - 1 - myY);
            default:
                return new MapLocation(mapWidth - 1 - myX, mapHeight - 1 - myY);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public int getMissionsCompleted() {
        return missionsCompleted;
    }
}
