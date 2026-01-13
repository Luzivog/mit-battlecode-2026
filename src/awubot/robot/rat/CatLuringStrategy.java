package awubot.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot.CellTracker;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class CatLuringStrategy extends Strategy {
    private static final int LURE_TTL = 40;
    private static final int CAT_LOST_THRESHOLD = 8;
    private static final int SQUEAK_INTERVAL = 2;
    private static final int MISSION_COMPLETE_DIST_SQ = 49;
    private static final int MIN_TURN_FOR_AUTO = 20;

    private MapLocation theCatLoc = null;
    private MapLocation theEnemyKingLoc = null;
    private int theLureTTL = 0;
    private boolean theIsActive = false;
    private int theLastCatSeenRound = 0;
    private int theSqueakCooldown = 0;
    private int theMissionsCompleted = 0;

    @Override
    public boolean isComplete() throws GameActionException {
        return !theIsActive;
    }

    public void activateLure(MapLocation aCatLoc, MapLocation aEnemyKingLoc) {
        theCatLoc = aCatLoc;
        theEnemyKingLoc = aEnemyKingLoc;
        theLureTTL = LURE_TTL;
        theIsActive = true;
        theLastCatSeenRound = time;
        System.out.println("[LURE] R" + rc.getID() + " t=" + time + " ACTIVATED cat=" + aCatLoc + " target=" + aEnemyKingLoc);
    }

    public boolean tryAutoActivate() throws GameActionException {
        if (theIsActive) return false;
        if (time < MIN_TURN_FOR_AUTO) return false;

        MapLocation myCatLoc = null;
        for (RobotInfo myRobot : rc.senseNearbyRobots()) {
            if (myRobot.getType() == UnitType.CAT) {
                myCatLoc = myRobot.getLocation();
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
    public void runTick() throws GameActionException {
        if (!theIsActive) return;

        theLureTTL--;
        indicate("LURING[" + theLureTTL + "] -> " + theEnemyKingLoc);

        if (theEnemyKingLoc != null) {
            rc.setIndicatorLine(myLoc, theEnemyKingLoc, 255, 0, 255);
        }
        if (theCatLoc != null) {
            rc.setIndicatorLine(myLoc, theCatLoc, 255, 0, 0);
        }

        boolean myCatNearby = updateCatTracking();

        System.out.println("[LURE] R" + rc.getID() + " t=" + time +
            " active=" + theIsActive + " ttl=" + theLureTTL +
            " cat=" + theCatLoc + " catNear=" + myCatNearby +
            " target=" + theEnemyKingLoc + " missions=" + theMissionsCompleted);

        if (shouldTerminateLure(myCatNearby)) {
            terminateLure(myCatNearby);
            return;
        }

        executeOffensiveMovement();
        executeSqueaking();
    }

    private boolean updateCatTracking() {
        for (RobotInfo myRobot : rc.senseNearbyRobots()) {
            if (myRobot.getType() == UnitType.CAT) {
                theCatLoc = myRobot.getLocation();
                theLastCatSeenRound = time;
                return true;
            }
        }
        return false;
    }

    private boolean shouldTerminateLure(boolean aCatNearby) {
        if (theLureTTL <= 0) {
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " TTL expired");
            return true;
        }

        if (theEnemyKingLoc != null &&
            myLoc.distanceSquaredTo(theEnemyKingLoc) <= MISSION_COMPLETE_DIST_SQ) {
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " MISSION COMPLETE - reached enemy!");
            return true;
        }

        if (!aCatNearby && (time - theLastCatSeenRound) > CAT_LOST_THRESHOLD) {
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
        if (aWasSuccessful && theEnemyKingLoc != null &&
            myLoc.distanceSquaredTo(theEnemyKingLoc) <= MISSION_COMPLETE_DIST_SQ) {
            theMissionsCompleted++;
            System.out.println("[LURE] R" + rc.getID() + " t=" + time + " SUCCESS! Total missions: " + theMissionsCompleted);
        }

        theIsActive = false;
        theCatLoc = null;
        theEnemyKingLoc = null;
    }

    private void executeOffensiveMovement() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (theEnemyKingLoc == null) return;

        Direction myToEnemy = myLoc.directionTo(theEnemyKingLoc);
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

        Pathfinder.makeMove(theEnemyKingLoc);
    }

    private void executeSqueaking() {
        theSqueakCooldown--;
        if (theSqueakCooldown > 0) return;

        rc.squeak(0);
        theSqueakCooldown = SQUEAK_INTERVAL;
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
        return theIsActive;
    }

    public int getMissionsCompleted() {
        return theMissionsCompleted;
    }
}
