package awubot_sus.robot.king;

import battlecode.common.*;
import awubot_sus.Game;
import awubot_sus.Pathfinder;
import awubot_sus.comms.GlobalArray;
import awubot_sus.robot.Strategy;
import static awubot_sus.Game.*;
import static awubot_sus.util.Util.*;

import awubot_sus.CellTracker;

// TODO: clean up

public class KingMiningStrategy extends Strategy {

    private MapLocation mineTarget = null;
    private MapLocation centerTarget;
    private boolean foundCheeseMine = false;

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
        indicate("stuckTurns: " + stuckTurns);
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

        tryCollectCheese();

        indicate("mine target: " + mineTarget);
        return true;
    }

    // made up a better function for collecting cheese
    public boolean tryCollectCheese() throws GameActionException {
        if (!foundCheeseMine) return false;
        indicate("trying to collect " + mineTarget);
        MapLocation nearestCheese = null;
        for (int x = mineTarget.x - 4; x <= mineTarget.x + 5; x++) {
            for (int y = mineTarget.y - 4; y <= mineTarget.y + 5; y++) {
                MapLocation checkLoc = new MapLocation(x, y);
                if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).getCheeseAmount() > 0) {
                    if (nearestCheese == null || myLoc.distanceSquaredTo(checkLoc) < myLoc.distanceSquaredTo(nearestCheese)) {
                        nearestCheese = checkLoc;
                    }
                }
            }
        }
        if (nearestCheese != null) {
            if (!rc.canPickUpCheese(nearestCheese)) {
                Direction dir = Pathfinder.getMove(nearestCheese);
                indicate("moving toward cheese at " + nearestCheese + " dir " + dir);
                digDirtInDirection(dir);
                if (rc.canMove(dir)) {
                    RatKing.rawMove(dir);
                }
                return true;
            } else {
                rc.pickUpCheese(nearestCheese);
            }
        }
        MapLocation resetLoc = mineTarget;
        for (int x = mineTarget.x; x <= mineTarget.x + 1; x++) {
            for (int y = mineTarget.y; y <= mineTarget.y + 1; y++) {
                MapLocation checkLoc = new MapLocation(x, y);
                if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).hasCheeseMine()) {
                    resetLoc = checkLoc;
                }
            }
        }
        if (!myLoc.equals(resetLoc)) {
            Direction dir = Pathfinder.getMove(resetLoc);
            digDirtInDirection(dir);
            if (rc.canMove(dir)) {
                RatKing.rawMove(dir);
            }
            return true;
        }
        return false;
    }

    public boolean scanForCheeseMines() throws GameActionException {
        MapLocation myBestMine = null;
        int myBestDist = Integer.MAX_VALUE;

        for (MapInfo myInfo : rc.senseNearbyMapInfos()) {
            if (myInfo.hasCheeseMine()) {
                MapLocation myMine = myInfo.getMapLocation();
                int encoded = encode2x2(myMine);
                int canonical = getCanonicalEncoded(encoded);
                GlobalArray.globalCheeseMines.add(canonical);
                Game.knownCheeseMines.add(encoded);
                int symEncoded = getSymmetricEncoded(encoded);
                if (symEncoded >= 0) {
                    Game.knownCheeseMines.add(symEncoded);
                }

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

    private static int stuckTurns = 0;
    private static boolean pending5x5 = false;
    private void moveIfBugTargetFar(MapLocation target) throws GameActionException {
        var dir = Pathfinder.getMove(target);
        if(Pathfinder.getBugPos().distanceSquaredTo(myLoc) > 2) {
            if(dir != Direction.CENTER) {
                stuckTurns = 0;
                digDirtInDirection(dir);
                if(rc.canMove(dir)) {
                    RatKing.rawMove(dir);
                    if(pending5x5 && CellTracker.isPassable5x5(myLoc)) {
                        RatKing.is5x5 = true;
                    }
                }
            } else if(!RatKing.spawnedThisTurn) {
                stuckTurns++;
                if(stuckTurns == 17) {
                    GlobalArray.expandKing();
                    pending5x5 = true;
                }
            }
        }
    }

    private void digDirtInDirection(Direction dir) throws GameActionException {
        if(!rc.isActionReady()) return;
        indicate("dig " + dir);
        int dx = dir.dx;
        int dy = dir.dy;

        if (dx != 0) {
            for (int y = -1; y <= 1; y++) {
                var loc = new MapLocation(myLocX + dx * 2, myLocY + dy + y);
                if(rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    return;
                }
            }
        }

        if (dy != 0) {
            for (int x = -1; x <= 1; x++) {
                var loc = new MapLocation(myLocX + dx + x, myLocY + dy * 2);
                if(rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    return;
                }
            }
        }
    }

    private void moveTowardCenter() throws GameActionException {
        moveIfBugTargetFar(centerTarget);
    }

    private void moveTowardMine() throws GameActionException {
        if (!rc.isMovementReady() || mineTarget == null) return;
        if (myLoc.distanceSquaredTo(mineTarget) < 5) {
            // find nearest cheese
            int closestDist = Integer.MAX_VALUE;
            MapLocation closestLoc = null;
            for (MapInfo info : rc.senseNearbyMapInfos(mineTarget, 13)) {
                if (info.getCheeseAmount() > 0) {
                    int dist = myLoc.distanceSquaredTo(info.getMapLocation());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestLoc = info.getMapLocation();
                    }
                }
            }
            if (closestLoc == null) return;
            moveIfBugTargetFar(closestLoc);
            return;
        }
        moveIfBugTargetFar(mineTarget);
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
