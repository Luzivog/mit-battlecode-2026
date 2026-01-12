package awubot.robot.king.strategies;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class KingMiningStrategy extends Strategy {
    private static final int STUCK_THRESHOLD = 3;

    private MapLocation theMineTarget = null;
    private MapLocation theCenterTarget;
    private boolean theFoundCheeseMine = false;
    private int theStuckCounter = 0;

    public void init() {
        theCenterTarget = new MapLocation(mapWidth / 2, mapHeight / 2);
    }

    public MapLocation getMineTarget() {
        return theMineTarget;
    }

    public MapLocation getCenterTarget() {
        return theCenterTarget;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        if (!theFoundCheeseMine) {
            if (!scanForCheeseMines()) {
                moveTowardCenter();
            }
        }

        if (theFoundCheeseMine) {
            scanForCheeseMines();
            moveTowardMine();
        }
    }

    public boolean scanForCheeseMines() throws GameActionException {
        for (MapInfo myInfo : rc.senseNearbyMapInfos()) {
            if (myInfo.hasCheeseMine()) {
                theMineTarget = myInfo.getMapLocation();
                GlobalArray.cheeseMines.add(encode2x2(theMineTarget));
                theFoundCheeseMine = true;
                return true;
            }
        }
        return false;
    }

    private void moveTowardCenter() throws GameActionException {
        if (!rc.isMovementReady()) return;

        Direction myDir = myLoc.directionTo(theCenterTarget);

        for (Direction myTryDir : new Direction[]{myDir, myDir.rotateLeft(), myDir.rotateRight()}) {
            if (rc.canMove(myTryDir)) {
                Pathfinder.makeMove(myTryDir);
                theStuckCounter = 0;
                return;
            }
        }
        theStuckCounter++;

        if (theStuckCounter >= STUCK_THRESHOLD) {
            Pathfinder.makeMove(theCenterTarget);
        }
    }

    private void moveTowardMine() throws GameActionException {
        if (!rc.isMovementReady() || theMineTarget == null) return;
        if (myLoc.distanceSquaredTo(theMineTarget) <= 9) return;

        Direction myDir = myLoc.directionTo(theMineTarget);
        for (Direction myTryDir : new Direction[]{myDir, myDir.rotateLeft(), myDir.rotateRight()}) {
            if (rc.canMove(myTryDir)) {
                Pathfinder.makeMove(myTryDir);
                return;
            }
        }
        Pathfinder.makeMove(theMineTarget);
    }
}
