package awubot_pp.robot.king;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import awubot_pp.Game;
import awubot_pp.Pathfinder;
import awubot_pp.comms.GlobalArray;
import awubot_pp.robot.Strategy;
import static awubot_pp.Game.*;
import static awubot_pp.util.Util.*;

// TODO: clean up

public class KingMiningStrategy extends Strategy {
    private static final int STUCK_THRESHOLD = 3;

    private MapLocation theMineTarget = null;
    private MapLocation theCenterTarget;
    private boolean theFoundCheeseMine = false;
    private int theStuckCounter = 0;

    public KingMiningStrategy() {
        theCenterTarget = new MapLocation(mapWidth / 2, mapHeight / 2);
    }

    public MapLocation getMineTarget() {
        return theMineTarget;
    }

    public MapLocation getCenterTarget() {
        return theCenterTarget;
    }

    @Override
    public boolean runTick() throws GameActionException {
        scanForCheeseMines();

        if (!theFoundCheeseMine) {
            selectFromKnownMines();
        }

        if (!theFoundCheeseMine) {
            moveTowardCenter();
        } else {
            GlobalArray.writeKingMineAssignment(kingId, theMineTarget);
            moveTowardMine();
        }

        indicate("mine target: " + theMineTarget);
        return true;
    }

    public boolean scanForCheeseMines() throws GameActionException {
        MapLocation myBestMine = null;
        int myBestDist = Integer.MAX_VALUE;

        for (MapInfo myInfo : rc.senseNearbyMapInfos()) {
            if (myInfo.hasCheeseMine()) {
                MapLocation myMine = myInfo.getMapLocation();
                GlobalArray.globalCheeseMines.add(encode2x2(myMine));

                if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;

                int myDist = myLoc.distanceSquaredTo(myMine);
                if (myDist < myBestDist) {
                    myBestDist = myDist;
                    myBestMine = myMine;
                }
            }
        }

        if (myBestMine != null) {
            theMineTarget = myBestMine;
            theFoundCheeseMine = true;
            GlobalArray.writeKingMineAssignment(kingId, theMineTarget);
            return true;
        }
        return false;
    }

    private void moveTowardCenter() throws GameActionException {
        Pathfinder.makeMove(theCenterTarget);
    }

    private void moveTowardMine() throws GameActionException {
        if (!rc.isMovementReady() || theMineTarget == null) return;
        if (myLoc.distanceSquaredTo(theMineTarget) <= 4) return;
        Pathfinder.makeMove(theMineTarget);
    }

    private void selectFromKnownMines() throws GameActionException {
        if (theMineTarget != null) return;

        MapLocation myBestMine = null;
        int myBestDist = Integer.MAX_VALUE;

        int myMineCount = Game.knownCheeseMines.set.length();
        for (int i = 0; i < myMineCount && i < 10; i++) {
            MapLocation myMine = decode2x2(Game.knownCheeseMines.at(i));
            if (myMine == null) continue;
            if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;

            int myDist = myLoc.distanceSquaredTo(myMine);
            if (myDist < myBestDist) {
                myBestDist = myDist;
                myBestMine = myMine;
            }
        }

        if (myBestMine != null) {
            theMineTarget = myBestMine;
            theFoundCheeseMine = true;
            GlobalArray.writeKingMineAssignment(kingId, theMineTarget);
        }
    }
}
