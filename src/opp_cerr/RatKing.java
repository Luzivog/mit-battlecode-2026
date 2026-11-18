package opp_cerr;

import battlecode.common.*;

public class RatKing extends Robot {

    private static MapLocation myLoc;
    static boolean moved;
    static MapLocation moveTarget;
    private static final CERR_Random rng = new CERR_Random();
    private int lastDanger = -1;
    static MapInfo[] allMapInfos;
    static MapInfo[] nearbyMapInfos;

    static int[] spawndx = {-2, -2, -1, 0, 1, 2, 2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2};
    static int[] spawndy = {1, 2, 2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2, -2, -1, 0, 1};
    static int[] dir2Idx = {1, 3, 5, 7, 9, 11, 13, 15, 1}; // ordinal to spawnidx, N -> NW

    private static final int MIN_DISINTEG_DIST = 49;
    int prevKings = 1000;
    int spawnDelayUntil = -1;
    int recentlySpawned = 10;
    static boolean panic = false;

    public enum Status {
        CORNER,
        COMBAT,
        GOTO_CHEESE,
        EVADE_CAT
    }

    Status currentStatus = Status.CORNER;

    public RatKing(RobotController rc) throws GameActionException {
        super(rc);
        RatKingMicro.init(rc);
        recentlySpawned = 4;
        prevKings = 1000;
    }

    /* ================= GLOBALS + STATUS ================= */

    private void determineStatus() {
        if (enemyRobots.length > 0 || neutralRobots.length > 0) {
            currentStatus = Status.COMBAT;
            return;
        }

        // cat in teh area!
        if (RobotPlayer.round - MacroData.catWarningTurn < 8) {
            currentStatus = Status.EVADE_CAT;
            return;
        }

        MapLocation nearestCheeseMine = MacroData.getNearestCheeseMine();
        boolean safe = lastDanger == -1 || rc.getRoundNum() - lastDanger >= 6;
        if (safe && nearestCheeseMine != null) {
            currentStatus = Status.GOTO_CHEESE;
            return;
        }

        currentStatus = Status.CORNER;
    }

    private void setGlobals() throws GameActionException {
        myLoc = rc.getLocation();
        moved = false;
        allMapInfos = rc.senseNearbyMapInfos();
        nearbyMapInfos = rc.senseNearbyMapInfos(8);
        determineStatus();
    }

    /* ================= MOVE LOGIC ================= */

    private void moveCombatStatus() throws GameActionException {
        lastDanger = rc.getRoundNum();
        RatKingMicro.processTurn(enemyRobots, neutralRobots, allyRobots);
    }

    private void moveCornerStatus() throws GameActionException {
        return;
    }

    private void moveGotoCheeseStatus() throws GameActionException {
        MapLocation nearestCheeseMine = MacroData.getNearestCheeseMine();
        if (nearestCheeseMine != null) {
            moveTarget = nearestCheeseMine;
        }

        if (rc.canSenseLocation(nearestCheeseMine) && rc.isMovementReady()) {
            // see if there is any nearby cheese we can move onto
            Integer closestDist = Integer.MAX_VALUE;
            for (MapInfo mi : allMapInfos) {
                int d = rc.getLocation().distanceSquaredTo(mi.getMapLocation());
                if (mi.getCheeseAmount() > 0 && d < closestDist) {
                    moveTarget = mi.getMapLocation();
                    closestDist = d;
                }
            }
        }

        moveTowardTarget();
    }

    private void moveEvadeCatStatus() throws GameActionException {
        if (MacroData.catWarningLoc == null) return;

        MapLocation myLoc = rc.getLocation();
        Direction bestDir = Direction.CENTER;
        int bestDist = myLoc.distanceSquaredTo(MacroData.catWarningLoc);
        int tempDist;

        for (Direction dir : Utils.DIRECTIONS) {
            if (!rc.canMove(dir)) continue;
            MapLocation next = myLoc.add(dir);
            tempDist = next.distanceSquaredTo(MacroData.catWarningLoc);
            if (tempDist > bestDist) {
                bestDist = tempDist;
                bestDir = dir;
            }
        }

        if (rc.canMove(bestDir)) rc.move(bestDir);
    }

    private void moveTowardTarget() throws GameActionException {
        if (!rc.isMovementReady()) return;
        BugSimKing.move(moveTarget);
    }

    private void move() throws GameActionException {
        moved = true;
        if (!rc.isMovementReady()) return;

        switch (currentStatus) {
            case COMBAT -> moveCombatStatus();
            case GOTO_CHEESE -> moveGotoCheeseStatus();
            case CORNER -> moveCornerStatus();
            case EVADE_CAT -> moveEvadeCatStatus();
        }
    }

    private void tryPanic() throws GameActionException {
        int numberKings = Comms.allyRatKingLocs.size;
        if (numberKings > 1) {
            Comms.setPanicFlag(false);
        }

        if (rc.getHealth() < 200) {
            panic = true;
            Comms.setPanicFlag(true);
        }
        if (rc.getRoundNum() > 1980 && rc.getHealth() > 200) {
            panic = true;
            Comms.setPanicFlag(true);
        }
    }

    /* ================= ACTION LOGIC ================= */
    private void checkBlockedIn() throws GameActionException {
        int open = 0;
        MapLocation bestDirt = null;
        for (MapInfo mi : nearbyMapInfos) {
            MapLocation l = mi.getMapLocation();
            if (l.distanceSquaredTo(myLoc) > 2 && mi.isPassable()) {
                open++;
            }
            if (rc.canRemoveDirt(l)) {
                if (myLoc.directionTo(l).rotateRight() == rc.getDirection()) {
                    bestDirt = l;
                } else if (bestDirt == null
                        && (myLoc.directionTo(l).rotateRight() == rc.getDirection()
                                || myLoc.directionTo(l).rotateRight() == rc.getDirection())) {
                    bestDirt = l;
                }
            }
        }
        if (open == 0 && bestDirt != null) {
            rc.removeDirt(bestDirt);
        }
    }

    private void actionCombatStatus() throws GameActionException {
        Direction dir = Utils.DIRECTIONS[rng.nextInt(8)];
        MapLocation spawnLoc = rc.getLocation().add(dir).add(dir);

        MapLocation closestEnemy = null;
        boolean isCat = false;

        for (RobotInfo i : enemyRobots) {
            if (closestEnemy == null
                    || myLoc.distanceSquaredTo(i.getLocation())
                            < myLoc.distanceSquaredTo(closestEnemy)) {
                closestEnemy = i.getLocation();
            }
        }

        for (RobotInfo i : neutralRobots) {
            if (closestEnemy == null
                    || myLoc.distanceSquaredTo(i.getLocation())
                            < myLoc.distanceSquaredTo(closestEnemy)) {
                closestEnemy = i.getLocation();
                isCat = true;
            }
        }
        if (!isCat || panic) {
            MapLocation closestToEnemy = null;
            boolean shouldSave = false;
            //                    rc.getGlobalCheese() < 150 && enemyRobots.length <
            // allyRobots.length
            //                            || enemyRobots.length + 2 < allyRobots.length;

            if (!shouldSave) {
                for (MapInfo i : nearbyMapInfos) {
                    MapLocation cur = i.getMapLocation();
                    if (!rc.canBuildRat(cur)) continue;

                    if (closestToEnemy == null
                            || cur.distanceSquaredTo(closestEnemy)
                                    < closestToEnemy.distanceSquaredTo(closestEnemy)) {
                        closestToEnemy = cur;
                    }
                }
            }

            if (rc.canTurn()) rc.turn(myLoc.directionTo(closestEnemy));
            if (closestToEnemy != null
                    && closestToEnemy.distanceSquaredTo(closestEnemy)
                            <= UnitType.BABY_RAT.visionConeRadiusSquared)
                rc.buildRat(closestToEnemy);

        } else {

            rc.setIndicatorString("wow its a cat");
            Direction away = myLoc.directionTo(closestEnemy).opposite();
            if (rc.canTurn()) rc.turn(away);
        }

        if (rc.canAttack(closestEnemy)) {
            rc.attack(closestEnemy);
        }
    }

    private void actionNonCombatStatus() throws GameActionException {
        if (!rc.isActionReady()) return;

        int isSafe = enemyRobots.length > 0 ? 0 : 1;
        if (shouldBuildRat(isSafe)) {
            MapLocation goal =
                    new MapLocation(rng.nextInt(rc.getMapWidth()), rng.nextInt(rc.getMapHeight()));
            while (rc.getLocation().distanceSquaredTo(goal) <= 2) {
                goal =
                        new MapLocation(
                                rng.nextInt(rc.getMapWidth()), rng.nextInt(rc.getMapHeight()));
            }
            for (MapInfo i : nearbyMapInfos) {
                MapLocation cur = i.getMapLocation();
                if (rc.canBuildRat(cur)) {
                    if (spawnLoc == null
                            || goal.distanceSquaredTo(spawnLoc) > goal.distanceSquaredTo(cur)) {
                        spawnLoc = cur;
                    }
                }
            }

            if (rc.canBuildRat(spawnLoc)) {
                if (rc.canTurn()) rc.turn(myLoc.directionTo(spawnLoc));
                rc.buildRat(spawnLoc);
            }
        }

        if (rc.isActionReady()) {
            for (MapInfo i : allMapInfos) {
                if (i.getCheeseAmount() > 0 && rc.canPickUpCheese(i.getMapLocation())) {
                    rc.pickUpCheese(i.getMapLocation());
                }
            }
        }

        // dirt removal
        if (rc.isActionReady() && Clock.getBytecodesLeft() > 1000) {
            MapLocation center = rc.getLocation();
            for (MapLocation offset : Utils.KING_SURROUNDING_SQUARES) {
                if (rc.canRemoveDirt(center.translate(offset.x, offset.y))) {
                    rc.removeDirt(center.translate(offset.x, offset.y));
                    break;
                }
            }
        }
    }

    /* ================= TURN ================= */

    public void turn() throws GameActionException {
        setGlobals();
        //        int prevBytecodees = Clock.getBytecodesLeft();

        move();
        setGlobals();

        //        int newBytecodes = Clock.getBytecodesLeft();
        myLoc = rc.getLocation();
        //        prevBytecodees = Clock.getBytecodesLeft();
        if (rc.isActionReady()) {
            checkBlockedIn();
            if (currentStatus == Status.COMBAT) actionCombatStatus();
            actionNonCombatStatus();
        }
        //        newBytecodes = Clock.getBytecodesLeft();

        if (shouldDisintegrate()) {
            rc.disintegrate();
            return;
        }

        Comms.updateThisKingMetadata();

        if (Comms.allyRatKingLocs.size() == 5) Comms.updateKingFormationLoc(null);
        if (MacroData.ratKingFormationLoc != null
                && MacroData.ratKingFormationLoc.distanceSquaredTo(myLoc)
                        <= MacroData.MIN_DIST_TO_ALLY_KING) Comms.updateKingFormationLoc(null);
        tryPanic();
        rc.setIndicatorString(
                rc.getGlobalCheese()
                        + " "
                        + MacroData.ratKingFormationLoc
                        + " "
                        + Comms.allyRatKingLocs.size
                        + " "
                        + RobotPlayer.round);
    }

    private boolean shouldBuildRat(int isSafe) {
        // we will build if score > currentRatCost

        int currentCheese = rc.getGlobalCheese();
        int currentRatCost = rc.getCurrentRatCost();

        int MAX_RATS;
        int MAP_SIZE = rc.getMapWidth() * rc.getMapHeight();
        if (MAP_SIZE <= 400) {
            MAX_RATS = 30;
        } else if (MAP_SIZE <= 900) {
            MAX_RATS = 40;
        } else if (MAP_SIZE <= 2000) {
            MAX_RATS = 50;
        } else {
            MAX_RATS = 60;
        }

        if (currentCheese <= 2000) {
            int cheeseMines = MacroData.getNumberCheeseMines();
            if (cheeseMines <= 1) {
                MAX_RATS = Math.min(MAX_RATS, 8);
            } else if (cheeseMines <= 2) {
                MAX_RATS = Math.min(MAX_RATS, 12);
            } else if (cheeseMines <= 3) {
                MAX_RATS = Math.min(MAX_RATS, 16);
            } else if (cheeseMines <= 4) {
                MAX_RATS = Math.min(MAX_RATS, 20);
            }
        }

        if (((rc.getCurrentRatCost() - 1) / 10) * 4 >= MAX_RATS) {
            return false; // ok chill too many rats
        }
        //        if(rc.getRoundNum()>100){
        //            rc.resign();
        //        }

        if (currentRatCost < 40) {
            recentlySpawned--;
            return true;
        } else if (currentCheese > 2500 - rc.getRoundNum() + rc.getCurrentRatCost()
                || recentlySpawned >= 0) {
            recentlySpawned--;
            return true;
        } else if (rc.getRoundNum() > 1500
                && currentCheese > 2500 - rc.getRoundNum() + rc.getCurrentRatCost()) {
            return true;
        }
        return false;

        //        // At 300 cheese, we get sqrt(300-100)*2 = +40
        //        score += (int) Math.sqrt((Math.max(200, currentCheese) - 200) * 2);
        //
        //        score += MacroData.getAverageCheeseIncome();
        //
        //        // prioritize building rats on new kings
        //        if (MacroData.KING_CREATION_ROUND - rc.getRoundNum() <= 50) {
        //            score += 10;
        //        }
        //        score = Math.min(score, 100);
        //
        //        assert score >= 10;
        //
        //        return score >= currentRatCost;
    }

    private boolean shouldDisintegrate() {
        if (rc.getRoundNum() > 1990) return false;
        if (rc.getHealth() > 100 && RobotPlayer.round > 1150) return false;
        for (RobotInfo r : allyRobots) {
            if (r.getType() != UnitType.RAT_KING) continue;
            if (r.getLocation().distanceSquaredTo(rc.getLocation()) > MIN_DISINTEG_DIST) continue;
            if ((neutralRobots.length > 0 || MacroData.catWarningTurn < RobotPlayer.round + 5)
                    && r.getHealth() + 150 > rc.getHealth()) return true;
            if (r.getHealth() > rc.getHealth()) return true;
            if (r.getHealth() == rc.getHealth() && r.getID() < rc.getID()) return true;
        }
        return false;
    }
}
