package awubot_us.robot.king;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import awubot_us.Game;
import awubot_us.Pathfinder;
import awubot_us.comms.GlobalArray;
import awubot_us.robot.Strategy;
import static awubot_us.Game.*;
import static awubot_us.util.Util.*;

// TODO: clean up

public class KingMiningStrategy extends Strategy {
    private static final int STUCK_THRESHOLD = 3;

    private MapLocation mineTarget = null;
    private MapLocation centerTarget;
    private boolean foundCheeseMine = false;
    private int stuckCounter = 0;

    private final int tabooMinX;
    private final int tabooMaxX;
    private final int tabooMinY;
    private final int tabooMaxY;

    public KingMiningStrategy() {
        if (mapWidth < 40 && mapHeight < 40) {
            tabooMinX = mapWidth / 3;
            tabooMaxX = 2 * mapWidth / 3;
            tabooMinY = mapHeight / 3;
            tabooMaxY = 2 * mapHeight / 3;
        } else if (mapWidth < 50 && mapHeight < 50) {
            tabooMinX = 3 * mapWidth / 7;
            tabooMaxX = 4 * mapWidth / 7;
            tabooMinY = 3 * mapHeight / 7;
            tabooMaxY = 4 * mapHeight / 7;
        } else {
            tabooMinX = 4 * mapWidth / 9;
            tabooMaxX = 5 * mapWidth / 9;
            tabooMinY = 4 * mapHeight / 9;
            tabooMaxY = 5 * mapHeight / 9;
        }

        centerTarget = selectSafeCenterTarget();
    }

    private boolean isInTabooZone(MapLocation aLoc) {
        return aLoc.x >= tabooMinX && aLoc.x <= tabooMaxX
            && aLoc.y >= tabooMinY && aLoc.y <= tabooMaxY;
    }

    private MapLocation selectSafeCenterTarget() {
        MapLocation myCenter = new MapLocation(mapWidth / 2, mapHeight / 2);
        if (!isInTabooZone(myCenter)) {
            return myCenter;
        }
        MapLocation myClosest = null;
        int myBestDist = Integer.MAX_VALUE;
        MapLocation[] myCandidates = {
            new MapLocation(tabooMinX - 1, mapHeight / 2),
            new MapLocation(tabooMaxX + 1, mapHeight / 2),
            new MapLocation(mapWidth / 2, tabooMinY - 1),
            new MapLocation(mapWidth / 2, tabooMaxY + 1)
        };
        for (MapLocation myCandidate : myCandidates) {
            if (myCandidate.x < 0 || myCandidate.x >= mapWidth) continue;
            if (myCandidate.y < 0 || myCandidate.y >= mapHeight) continue;
            int myDist = myLoc.distanceSquaredTo(myCandidate);
            if (myDist < myBestDist) {
                myBestDist = myDist;
                myClosest = myCandidate;
            }
        }
        return myClosest != null ? myClosest : myCenter;
    }

    public MapLocation getMineTarget() {
        return mineTarget;
    }

    public MapLocation getCenterTarget() {
        return centerTarget;
    }

    @Override
    public boolean runTick() throws GameActionException {
        scanForCheeseMines();

        if (!foundCheeseMine) {
            selectFromKnownMines();
        }

        if (!foundCheeseMine) {
            moveTowardCenter();
        } else {
            GlobalArray.writeKingMineAssignment(kingId, mineTarget);
            moveTowardMine();
        }

        indicate("mine target: " + mineTarget);
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
                if (isInTabooZone(myMine)) continue;

                int myDist = myLoc.distanceSquaredTo(myMine);
                if (myDist < myBestDist) {
                    myBestDist = myDist;
                    myBestMine = myMine;
                }
            }
        }

        if (myBestMine != null) {
            mineTarget = myBestMine;
            foundCheeseMine = true;
            GlobalArray.writeKingMineAssignment(kingId, mineTarget);
            return true;
        }
        return false;
    }

    private void moveTowardCenter() throws GameActionException {
        Pathfinder.makeMove(centerTarget);
    }

    private void moveTowardMine() throws GameActionException {
        if (!rc.isMovementReady() || mineTarget == null) return;
        if (myLoc.distanceSquaredTo(mineTarget) <= 4) return;
        Pathfinder.makeMove(mineTarget);
    }

    private void selectFromKnownMines() throws GameActionException {
        if (mineTarget != null) return;

        MapLocation myBestMine = null;
        int myBestDist = Integer.MAX_VALUE;

        int myMineCount = Game.knownCheeseMines.set.length();
        for (int i = 0; i < myMineCount && i < 10; i++) {
            MapLocation myMine = decode2x2(Game.knownCheeseMines.at(i));
            if (myMine == null) continue;
            if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;
            if (isInTabooZone(myMine)) continue;

            int myDist = myLoc.distanceSquaredTo(myMine);
            if (myDist < myBestDist) {
                myBestDist = myDist;
                myBestMine = myMine;
            }
        }

        if (myBestMine != null) {
            mineTarget = myBestMine;
            foundCheeseMine = true;
            GlobalArray.writeKingMineAssignment(kingId, mineTarget);
        }
    }
}
