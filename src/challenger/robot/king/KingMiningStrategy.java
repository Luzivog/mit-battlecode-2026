

package challenger.robot.king;

import battlecode.common.*;
import challenger.Game;
import challenger.Pathfinder;
import challenger.RobotTracker;
import challenger.comms.GlobalArray;
import challenger.robot.Strategy;
import static challenger.robot.king.RatKing.*;
import static challenger.Game.*;
import static challenger.util.Util.*;

import challenger.Book;
import challenger.CatLanes;
import challenger.CellTracker;
import challenger.Tune;

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
        if (Tune.CM_KING1_SITE != 0) firstKing = time <= 1;
    }

    // Tune.CM_KING1_SITE (a book switch, 0 in main): the first king's site is the k-th tile of Book.KING1_SITE (the last
    // when k exceeds the list). From round 1 it is the king's mine target and stays so: the king walks there, collects
    // the cheese within Chebyshev 4 of it and walks back, as at any mine (moveTowardMine, tryCollectCheese). The cat flee
    // and the formation coordinator run before mining every turn as in main; the king returns afterwards.
    private boolean firstKing;

    private boolean runAtSite() throws GameActionException {
        scanForCheeseMines();
        MapLocation[] sites = Book.KING1_SITE;
        mineTarget = sites[Math.min(Tune.CM_KING1_SITE, sites.length) - 1];
        foundCheeseMine = true;
        GlobalArray.writeKingMineAssignment(kingId, mineTarget);
        // Round 1's turn already holds the book's scan and the king's setup: the first path to the site waits a round,
        // or the turn overruns its 20,000 bytecodes (7 of 9 booked map-seats, 1120-084751-ab-9a8f).
        if (time == 1) return true;
        moveTowardMine();
        tryCollectCheese();
        return true;
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

    // No step into a visible cat's forward lane (KingCombatStrategy.laneCross). With a cat in view the king paths
    // at most once a turn: Pathfinder.getMove costs 4-5k there, and two calls overrun the king's turn.
    private boolean pathedNearCat;

    private boolean skipPath(MapLocation target) {
        if (challenger.Tune.CM_LANE_MEMORY != 0 && myLoc.equals(laneBlockedAt) && target.equals(laneBlockedTarget) && CatLanes.version == laneBlockedVersion) {
            return true;
        }
        if (RobotTracker.numCats == 0) return false;
        if (pathedNearCat || KingCombatStrategy.entersCatLane(myLoc.directionTo(target))) return true;
        pathedNearCat = true;
        return false;
    }

    // Tune.CM_LANE_MEMORY (K2 D'): also no mining step that puts more cat footprints under the king (CatLanes): it may
    // walk along or out of a known lane, never into one. A king refused that step stays movement-ready, so it does not
    // path to the same target again until it, the target or the footprints change: the same path would be refused
    // again, at 4-5k a call.
    private static MapLocation laneBlockedAt, laneBlockedTarget;
    private static int laneBlockedVersion;

    private static boolean stepIntoCatLane(Direction dir) {
        return RobotTracker.numCats > 0 && KingCombatStrategy.entersCatLane(dir);
    }

    private static boolean stepIntoKnownLane(Direction dir, MapLocation target) {
        if (stepIntoCatLane(dir)) return true;
        if (CatLanes.kingOverlap(myLocX + dir.dx, myLocY + dir.dy) <= CatLanes.kingOverlap(myLocX, myLocY)) return false;
        laneBlockedAt = myLoc;
        laneBlockedTarget = target;
        laneBlockedVersion = CatLanes.version;
        return true;
    }

    @Override
    public boolean runTick() throws GameActionException {
        // indicate("stuckTurns: " + stuckTurns);
        pathedNearCat = false;
        if (challenger.Tune.CM_LANE_MEMORY != 0) CatLanes.markVisibleCats();
        if (Tune.KING_PATH != 0) routed = false;
        if (Tune.CM_KING1_SITE != 0 && firstKing && Book.KING1_SITE != null) return runAtSite();
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

        // indicate("mine target: " + mineTarget);
        return true;
    }

    // made up a better function for collecting cheese
    public boolean tryCollectCheese() throws GameActionException {
        if (!foundCheeseMine) return false;
        // indicate("trying to collect " + mineTarget);
        MapLocation nearestCheese = null;
        for(var info : rc.senseNearbyMapInfos()) {
            if (info.getCheeseAmount() > 0) {
                MapLocation checkLoc = info.getMapLocation();
                if(challenger.Tune.CM_LANE_MEMORY != 0 ? chebyshevDistance(checkLoc, mineTarget) > 4 || CatLanes.onLane(checkLoc) : chebyshevDistance(checkLoc, mineTarget) > 4) continue;
                if (nearestCheese == null || myLoc.distanceSquaredTo(checkLoc) < myLoc.distanceSquaredTo(nearestCheese)) {
                    nearestCheese = checkLoc;
                }
            }
        }
        if (nearestCheese != null) {
            if (!rc.canPickUpCheese(nearestCheese)) {
                if((Tune.KING_PATH == 0 || !routed) && rc.isMovementReady() && !skipPath(nearestCheese)) {
                    Direction dir = Pathfinder.getMove(nearestCheese);
                    // indicate("moving toward cheese at " + nearestCheese + " dir " + dir);
                    if (!(challenger.Tune.CM_LANE_MEMORY != 0 ? stepIntoKnownLane(dir, nearestCheese) : stepIntoCatLane(dir))) {
                        digDirtInDirection(dir);
                        if (rc.canMove(dir)) {
                            RatKing.rawMove(dir);
                        }
                    }
                }
                return true;
            } else {
                {
                    var loc = myLoc.translate(-1, -1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(-1, 0);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(-1, 1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(0, -1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(0, 0);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(0, 1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(1, -1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(1, 0);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
                {
                    var loc = myLoc.translate(1, 1);
                    if(rc.canPickUpCheese(loc)) {
                        rc.pickUpCheese(loc);
                    }
                }
            }
        }
        MapLocation resetLoc = mineTarget;
        // unrolled version:
        {
            MapLocation checkLoc = mineTarget.translate(0, 1);
            if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).hasCheeseMine()) {
                resetLoc = checkLoc;
            }
        }
        {
            MapLocation checkLoc = mineTarget.translate(1, 0);
            if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).hasCheeseMine()) {
                resetLoc = checkLoc;
            }
        }
        {
            MapLocation checkLoc = mineTarget.translate(1, 1);
            if (rc.canSenseLocation(checkLoc) && rc.senseMapInfo(checkLoc).hasCheeseMine()) {
                resetLoc = checkLoc;
            }
        }
        if (!myLoc.equals(resetLoc)) {
            if((Tune.KING_PATH == 0 || !routed) && rc.isMovementReady() && !skipPath(resetLoc)) {
                Direction dir = Pathfinder.getMove(resetLoc);
                if (!(challenger.Tune.CM_LANE_MEMORY != 0 ? stepIntoKnownLane(dir, resetLoc) : stepIntoCatLane(dir))) {
                    digDirtInDirection(dir);
                    if (rc.canMove(dir)) {
                        RatKing.rawMove(dir);
                    }
                }
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
                GlobalArray.registerGlobalCheeseMine(encoded);

                if (GlobalArray.isMineAssignedToOtherKing(myMine)) continue;
                if (challenger.Tune.CM_LANE_MEMORY != 0 && CatLanes.nearLane(myMine)) continue;
                // if (isInTabooZone(myMine)) continue;

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
    // KingPath moved the king or dug, or plans, this turn: tryCollectCheese does not walk too
    private static boolean routed;

    private void moveIfBugTargetFar(MapLocation target) throws GameActionException {
        if(skipPath(target)) return;
        if(Tune.KING_PATH != 0 && KingPath.handles(target)) {
            routed = true;
            if(!rc.isMovementReady()) {
                KingPath.prepare(target);
                return;
            }
            var planned = KingPath.next(target);
            if(planned == null || planned == Direction.CENTER) return;
            stuckTurns = 0;
            if(stepIntoCatLane(planned)) return;
            digDirtInDirection(planned);
            if(!rc.canMove(planned) && !dirtAhead(planned)) {
                // a robot stands in the way: step round it beside the path
                var around = KingPath.around(planned);
                if(around != null && !stepIntoCatLane(around)) planned = around;
            }
            if(rc.canMove(planned)) {
                RatKing.rawMove(planned);
                if(pending5x5 && CellTracker.isPassable5x5(myLoc)) {
                    RatKing.is5x5 = true;
                }
            }
            return;
        }
        var dir = Pathfinder.getMove(target);
        if(Pathfinder.getBugPos().distanceSquaredTo(myLoc) > 2) {
            if(dir != Direction.CENTER) {
                stuckTurns = 0;
                if(challenger.Tune.CM_LANE_MEMORY != 0 ? stepIntoKnownLane(dir, target) : stepIntoCatLane(dir)) return;
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

    // seen dirt in the 3x3 the king would step onto
    private static boolean dirtAhead(Direction dir) {
        int x = myLocX + dir.dx, y = myLocY + dir.dy;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (CellTracker.isDirt(x + dx, y + dy)) return true;
            }
        }
        return false;
    }

    private void moveTowardCenter() throws GameActionException {
        moveIfBugTargetFar(centerTarget);
    }

    private void moveTowardMine() throws GameActionException {
        if (Tune.KING_PATH != 0 && mineTarget != null && !rc.isMovementReady()) {
            // plan the route to a far mine in the turns the king cannot move (KingPath)
            if (myLoc.distanceSquaredTo(mineTarget) >= 5 && KingPath.handles(mineTarget)) moveIfBugTargetFar(mineTarget);
            return;
        }
        if (!rc.isMovementReady() || mineTarget == null) return;
        if (myLoc.distanceSquaredTo(mineTarget) < 5) {
            // find nearest cheese
            int closestDist = Integer.MAX_VALUE;
            MapLocation closestLoc = null;
            for (MapInfo info : rc.senseNearbyMapInfos(mineTarget, 13)) {
                if (challenger.Tune.CM_LANE_MEMORY != 0 ? info.getCheeseAmount() > 0 && !CatLanes.onLane(info.getMapLocation()) : info.getCheeseAmount() > 0) {
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
            if (GlobalArray.isMineAssignedToOtherKing(myMine)) continue;
            if (challenger.Tune.CM_LANE_MEMORY != 0 && CatLanes.nearLane(myMine)) continue;
            // if (isInTabooZone(myMine)) continue;

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