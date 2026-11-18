package opp_cerr;

import battlecode.common.*;

import java.util.Random;

public class BabyRat extends Robot {

    /* =========================
    STATES
    ========================= */

    // We strictly define states for the method of movement,
    // as Action logic in your code is sequential/overlapping.
    private enum MoveState {
        MICRO, // Combat movement
        RANDOM, // Panic or no target wandering
        BUGSIM, // Efficient pathing
        BUGNAV // Fallback pathing
    }

    /* =========================
    GLOBALS
    ========================= */

    private static MapLocation myLoc;
    static boolean moved;
    static boolean finished_moving;
    static int lastPlacedTrap = -50;
    static MapLocation moveTarget; // the target for what we will move towards
    static MapLocation macroTarget; // the macro exploration target
    // spawnLoc is inherited from Robot - closest rat king location

    static MapInfo[] allMapInfos;
    //    static RobotInfo[] allNearbyRobots;
    MapLocation lastTrapLoc;
    static int carryTurns = 0;
    static int stuckTurns = 0;
    CERR_Random rand;
    static Random rng = new Random();

    private static final boolean USE_BUGSIM = true;
    public static final int CHEESE_BEFORE_GOING_HOME = 100;
    int lastSawCat = -50;
    int lastMicroed = -50;

    // Tracked cat location from squeaks (used in updateMoveTarget)
    static MapLocation nearbyCatLoc = null;

    static boolean goingForCheese = false;

    public BabyRat(RobotController rc) throws GameActionException {
        super(rc);
        lastTrapLoc = spawnLoc;
        rand = new CERR_Random();
        BabyRatMicro.init(rc);
    }

    /* =========================
    GLOBAL SETUP
    ========================= */

    private void getRatKingLocation() {
        if (Comms.allyRatKingLocs.getKeys().length == 0) {
            return;
        }
        spawnLoc = Comms.allyRatKingLocs.getKeys()[0];

        for (MapLocation loc : Comms.allyRatKingLocs.getKeys()) {
            if (myLoc.distanceSquaredTo(spawnLoc) >= myLoc.distanceSquaredTo(loc)) {
                spawnLoc = loc;
            }
        }
    }

    private void setGlobals() throws Exception {
        myLoc = rc.getLocation();
        // allMapInfos = Navigation.nearbyMapInfos;
        allMapInfos = rc.senseNearbyMapInfos();
        //        allNearbyRobots = rc.senseNearbyRobots();

        if (neutralRobots.length > 0) {
            lastSawCat = rc.getRoundNum();
            if (Utils.catCanDetect(neutralRobots[0], myLoc)) Comms.squeakCat(neutralRobots[0]);
            MacroData.updateCatWarning(neutralRobots[0].location);
        }

        getRatKingLocation();
    }

    /*=========
    UTILS
     */
    public static boolean mogEnemy(RobotInfo ri) {
        if (ri.getHealth() < rc.getHealth() - 50) {
            return true;
        }
        if (ri.getRawCheeseAmount() > 12 && rc.getRawCheese() == 0) {
            return true;
        }
        return false;
    }

    /* =========================
    ACTION LOGIC
    ========================= */

    private void doAction() throws GameActionException {
        // 1. Calculate Combat Data
        int nearbyFriends = 0;
        MapLocation bestAttackLoc = rc.getLocation();
        int bestAttackLocScore = -100000;
        int bestThrowScore = -100;
        Direction bestThrowDirection = rc.getDirection();
        MapLocation bestbabyRat = null;

        // Scan directions for Throws and Attacks
        for (Direction d : Direction.allDirections()) {
            if (!d.equals(rc.getDirection())
                    && !d.equals(rc.getDirection().rotateLeft())
                    && !d.equals(rc.getDirection().rotateRight())) {
                continue;
            }

            // Check Throw
            MapLocation curLoc = rc.getLocation().add(d);
            RobotInfo closeRobot = null;
            if (rc.canSenseRobotAtLocation(curLoc)) {
                closeRobot = rc.senseRobotAtLocation(curLoc);
            }
            if (rc.getCarrying() != null
                    && rc.getActionCooldownTurns() < 10
                    && (rc.canTurn() || d == rc.getDirection())) {
                if (closeRobot != null
                        && closeRobot.getTeam() != rc.getTeam()
                        && rc.canMove(d.opposite())) {
                    if (d != rc.getDirection()) {
                        rc.turn(d);
                        rc.move(d.opposite());
                        rc.throwRat();
                    }
                } else {
                    int curThrowScore = checkThrow(d);
                    if (bestThrowScore < curThrowScore) {
                        bestThrowScore = curThrowScore;
                        bestThrowDirection = d;
                    }
                }
            }
            if (closeRobot != null) {
                if (closeRobot.getTeam() == rc.getTeam()) {
                    nearbyFriends++;
                } else {
                    if (closeRobot.getType() == UnitType.BABY_RAT
                            && rc.canCarryRat(closeRobot.getLocation())) {
                        bestbabyRat = closeRobot.getLocation();
                    }
                    int curscore = calculateAttackScore(curLoc, closeRobot);
                    if (bestAttackLocScore < curscore) {
                        bestAttackLocScore = curscore;
                        bestAttackLoc = curLoc;
                    }
                }
            }
        }

        // 2. Execute Actions Sequence
        executeAscend();
        executeCarry(bestbabyRat);
        executeThrow(bestThrowScore, bestThrowDirection);
        executeAttack(bestAttackLocScore, bestAttackLoc);
        executeTransfer();
        if (finished_moving) {
            executeTrap();
            executeCatTrap();
        }

        // 3. Pickup Cheese
        if (finished_moving || rc.getCarrying() == null) {
            pickupCheese();
        }

        // 4. Misc Actions
        executeSqueak(nearbyFriends);
        executeDirtRemoval();
    }

    // --- Action Helpers ---

    private int calculateAttackScore(MapLocation loc, RobotInfo i) throws GameActionException {
        if (rc.canAttack(loc) && i.getHealth() > 0) {
            int curscore = 0;
            if (i.getTeam() == rc.getTeam().opponent()) {
                if (i.getHealth() <= allyRobots.length * 10 + 12) curscore += 1000;
                if (i.getType().isRatKingType()) curscore += 200;
                else if (i.getType().isBabyRatType()) curscore += 300 + 100 - i.getHealth();
                curscore += i.getRawCheeseAmount() * 20;
            }
            return curscore;
        }
        return -1;
    }

    private void executeAscend() throws GameActionException {
        if (!rc.canBecomeRatKing()) {
            return;
        }
        if (rc.getRoundNum() > 1990) {
            rc.becomeRatKing();
            return;
        }
        if (MacroData.ratKingFormationLoc != null
                && rc.getLocation().distanceSquaredTo(MacroData.ratKingFormationLoc) < 20
                && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            return;
        }

        if (Comms.isPanic()) {
            if (!rc.canBecomeRatKing()) {
                rc.setIndicatorDot(myLoc, 255, 0, 0);
            }
        }
        if (Comms.isPanic() && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            return;
        }
    }

    private void executeCatTrap() throws GameActionException {
        if (rc.getGlobalCheese() < 300) return;
        if (neutralRobots.length == 0 || enemyRobots.length > 0) return;

        RobotInfo ri = neutralRobots[0];
        MapLocation catLoc = ri.getLocation();
        if (!Utils.catCanDetect(ri, myLoc)) return;
        int bestDist = myLoc.distanceSquaredTo(catLoc);
        int d = myLoc.distanceSquaredTo(catLoc.translate(0, 1));
        if (d < bestDist) bestDist = d;
        d = myLoc.distanceSquaredTo(catLoc.translate(1, 0));
        if (d < bestDist) bestDist = d;
        d = myLoc.distanceSquaredTo(catLoc.translate(1, 1));
        if (d < bestDist) bestDist = d;

        if (bestDist > 10) return;
        Direction dir = myLoc.directionTo(ri.getLocation());
        MapLocation loc = myLoc.add(dir);
        System.out.println("TRYING TO PLACE A CAT TRAP AT " + loc);
        if (rc.canPlaceCatTrap(loc)) {
            rc.placeCatTrap(loc);
            return;
        }
    }

    private void executeThrow(int bestThrowScore, Direction bestThrowDirection)
            throws GameActionException {
        if (rc.canThrowRat() && bestThrowScore > 0) {
            //            rc.setIndicatorLine(
            //                    myLoc, myLoc.add(bestThrowDirection).add(bestThrowDirection), 0,
            // 255, 0);
            if (bestThrowDirection != rc.getDirection()) {
                rc.turn(bestThrowDirection);
            }
            rc.throwRat();
            carryTurns = 0;
        }
    }

    private void executeCarry(MapLocation bestbabyRat) throws GameActionException {
        if (rc.getCarrying() == null && bestbabyRat != null) {
            if (rc.canCarryRat(bestbabyRat)) {
                carryTurns = 1;
                rc.carryRat(bestbabyRat);
            }
        }
    }

    private boolean checkUseCheese(int cheeseAmount, RobotInfo enemyBot) {
        if (cheeseAmount > 0 && enemyBot.getTeam() == Team.NEUTRAL) {
            return false;
        }
        if (rc.getHealth() <= 50 && rc.getRawCheese() < cheeseAmount) {
            return false;
        }
        if (cheeseAmount > 2) {
            int roundsLeftAlive = 1 + rc.getHealth() / (10 * enemyRobots.length + 1);
            int damage = 10 + (int) (Math.ceil(Math.sqrt(cheeseAmount)) + 0.01);
            boolean lowHealthHighCheese =
                    allyRobots.length + 1 < enemyRobots.length
                            || rc.getHealth() <= enemyRobots.length * 5;
            if (lowHealthHighCheese
                    && rc.getRawCheese() / roundsLeftAlive >= cheeseAmount
                    && enemyBot.getHealth() >= damage) {
                return true;
            } else {
                return false;
            }
        } else if (cheeseAmount == 2) {
            if (rc.getCarrying() == null && enemyBot.getHealth() - 11 == rc.getHealth()) {
                return true;
            } else if (enemyBot.getHealth() - 12 == rc.getHealth()) {
                return true;
            } else if (enemyBot.getHealth() == 12) {
                return true;
            }
            return false;
        } else if (cheeseAmount == 1) {
            if (rc.getCarrying() == null && enemyBot.getHealth() - 10 == rc.getHealth()) {
                return true;
            } else if (enemyBot.getHealth() - 11 == rc.getHealth()) {
                return true;
            } else if (enemyBot.getHealth() == 11) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    private void executeAttack(int bestAttackLocScore, MapLocation bestAttackLoc)
            throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }
        if (rc.isCooperation()) {
            return;
        }
        if (bestAttackLocScore >= 0) {
            rc.setIndicatorString(
                    "trying to attack"
                            + bestAttackLoc.toString()
                            + "with score"
                            + bestAttackLocScore);

            RobotInfo attacked = rc.senseRobotAtLocation(bestAttackLoc);
            if (attacked == null) {
                return;
            }
            if (checkUseCheese(65, attacked) && rc.canAttack(bestAttackLoc, 65)) {
                rc.attack(bestAttackLoc, 65);
            } else if (checkUseCheese(50, attacked) && rc.canAttack(bestAttackLoc, 50)) {
                rc.attack(bestAttackLoc, 50);
            } else if (checkUseCheese(37, attacked) && rc.canAttack(bestAttackLoc, 37)) {
                rc.attack(bestAttackLoc, 37);
            } else if (checkUseCheese(26, attacked) && rc.canAttack(bestAttackLoc, 26)) {
                rc.attack(bestAttackLoc, 26);
            } else if (checkUseCheese(17, attacked) && rc.canAttack(bestAttackLoc, 17)) {
                rc.attack(bestAttackLoc, 17);
            } else if (checkUseCheese(10, attacked) && rc.canAttack(bestAttackLoc, 10)) {
                rc.attack(bestAttackLoc, 10);
            } else if (checkUseCheese(5, attacked) && rc.canAttack(bestAttackLoc, 5)) {
                rc.attack(bestAttackLoc, 5);
            } else if (checkUseCheese(2, attacked) && rc.canAttack(bestAttackLoc, 2)) {
                rc.attack(bestAttackLoc, 2);
            } else if (checkUseCheese(1, attacked) && rc.canAttack(bestAttackLoc, 1)) {
                rc.attack(bestAttackLoc, 1);
            } else if (rc.canAttack(bestAttackLoc)) {
                rc.attack(bestAttackLoc);
            } else {
                return;
            }

            //            Comms.squeakCombat(bestAttackLoc);
        }
    }

    private void executeTransfer() throws GameActionException {
        if (rc.getRawCheese() == 0) return;

        MapLocation transferLoc;

        if (allyKing != null) {
            for (Direction d : Direction.allDirections()) {
                transferLoc = allyKing.getLocation().add(d);
                // check all occupied locs aruond the rat king
                if (rc.canTransferCheese(transferLoc, rc.getRawCheese())) {
                    rc.transferCheese(transferLoc, rc.getRawCheese());
                    return;
                }
            }
        }
    }

    private boolean noNearbyTraps(MapLocation i) throws GameActionException {
        for (Direction d : Direction.allDirections()) {
            MapLocation cur = i.add(d);
            if (rc.canSenseLocation(cur) && rc.senseMapInfo(cur).getTrap() == TrapType.RAT_TRAP) {
                return false;
            }
        }
        return true;
    }

    private void executeTrap() throws GameActionException {
        if (enemyRobots.length == 0) return;
        if (rc.isCooperation()) {
            return;
        }

        if (rc.getRawCheese() < 20 && rc.getGlobalCheese() < 300) return;
        if (BabyRatMicro.closestEnemy == null) return;

        if (rc.canSenseRobotAtLocation(BabyRatMicro.closestEnemy)
                && myLoc.distanceSquaredTo(BabyRatMicro.closestEnemy) <= 8
                && myLoc.distanceSquaredTo(BabyRatMicro.closestEnemy) > 2) {
            //            if (rc.getCurrentRatCost() <= 20) {
            //                return;
            //            }

            RobotInfo ri = rc.senseRobotAtLocation(BabyRatMicro.closestEnemy);
            if (mogEnemy(ri)) {
                return;
            }
            if (!Utils.ratCanDetect(ri, myLoc)) {
                return;
            }
            if (!Utils.unitCanReach(rc, ri, myLoc)) {
                return;
            }
            Direction d = myLoc.directionTo(ri.getLocation());
            MapLocation loc = myLoc.add(d);
            if (rc.canPlaceRatTrap(loc) && noNearbyTraps(loc)) { // traps to prevent "flanking"
                lastTrapLoc = loc;
                rc.placeRatTrap(loc);
                lastPlacedTrap = rc.getRoundNum();
                return;
            }
        }

        //        if (enemyRobots.length < allyRobots.length + 2) return;
        //
        //
        //        if (rc.getCurrentRatCost() <= 60) return;
        //        Direction d = rc.getDirection();
        //
        //        MapLocation loc = myLoc.add(d);
        //        loc = myLoc.add(d.rotateLeft());
        //        if (rc.canPlaceRatTrap(loc) && noNearbyTraps(loc)) {// traps to prevent "flanking"
        //            lastTrapLoc = loc;
        //            rc.placeRatTrap(loc);
        //            return;
        //        }
        //
        //        loc = myLoc.add(d.rotateR ight());
        //        if (rc.canPlaceRatTrap(loc) && noNearbyTraps(loc)) {
        //            lastTrapLoc = loc;
        //            rc.placeRatTrap(loc);
        //            return;
        //        }
        //        loc = myLoc.add(d);
        //        if (rc.canPlaceRatTrap(loc) && noNearbyTraps(loc)) {
        //            lastTrapLoc = loc;
        //            rc.placeRatTrap(loc);
        //            return;
        //        }

    }

    private void pickupCheese() throws GameActionException {
        if (rc.getRawCheese() >= CHEESE_BEFORE_GOING_HOME) {
            return;
        }
        MapLocation maxCheeseLoc = null;
        int maxCheeseAmount = 0;

        for (MapInfo i : allMapInfos) {
            if (i.getCheeseAmount() == 0) {
                continue;
            }
            boolean shouldExplore =
                    allyRobots.length
                            == 0; // if you dont see any freinds nearby, then explore rather than
            // greedy.
            if (i.getCheeseAmount() >= maxCheeseAmount
                    && (shouldExplore || rc.canPickUpCheese(i.getMapLocation()))) {
                if (i.getCheeseAmount() > maxCheeseAmount) {
                    maxCheeseAmount = i.getCheeseAmount();
                    maxCheeseLoc = i.getMapLocation();

                } else {
                    boolean equalButCloser =
                            i.getCheeseAmount() == maxCheeseAmount
                                    && i.getMapLocation().distanceSquaredTo(myLoc)
                                            < maxCheeseLoc.distanceSquaredTo(myLoc);
                    if (equalButCloser) {
                        maxCheeseAmount = i.getCheeseAmount();
                        maxCheeseLoc = i.getMapLocation();
                    }
                }
            }
        }

        if (maxCheeseLoc != null && rc.canPickUpCheese(maxCheeseLoc)) {
            rc.pickUpCheese(maxCheeseLoc);
        }
    }

    private void executeSqueak(int nearbyFriends) throws GameActionException {
        if (myLoc.distanceSquaredTo(spawnLoc) > 30) {
            if (enemyRobots.length > 0 && neutralRobots.length == 0) {
                rc.squeak(1); // 1 means go there
            } else if (neutralRobots.length > 0 && MacroData.ratKingFormationLoc != null) {
                rc.squeak(2); // 2 means stay away
            }
        }
    }

    private void executeDirtRemoval() throws GameActionException {
        if (moveTarget == null) return;

        Direction dirto = rc.getLocation().directionTo(moveTarget);
        if (rc.getDirection() == dirto) {
            MapLocation l1 = myLoc.add(dirto);
            MapLocation l2 = myLoc.add(dirto.rotateLeft());
            MapLocation l3 = myLoc.add(dirto.rotateRight());

            if (!rc.canSenseLocation(l1)) l1 = myLoc;
            if (!rc.canSenseLocation(l2)) l2 = myLoc;
            if (!rc.canSenseLocation(l3)) l3 = myLoc;

            MapInfo loc1 = rc.senseMapInfo(l1);
            MapInfo loc2 = rc.senseMapInfo(l2);
            MapInfo loc3 = rc.senseMapInfo(l3);

            if ((loc1.isWall() || loc1.isDirt())
                    && (loc2.isWall() || loc2.isDirt())
                    && (loc3.isWall() || loc3.isDirt())) {
                if (rc.canRemoveDirt(myLoc.add(dirto))) {
                    rc.removeDirt(myLoc.add(dirto));
                }
            }
        }
        if (MacroData.ratKingFormationLoc != null
                && myLoc.distanceSquaredTo(MacroData.ratKingFormationLoc) < 4) {
            for (MapInfo i : rc.senseNearbyMapInfos(2)) {
                if (i.getMapLocation().distanceSquaredTo(MacroData.ratKingFormationLoc) < 4
                        && rc.canRemoveDirt(i.getMapLocation())) {
                    rc.removeDirt(i.getMapLocation());
                }
            }
        }
    }

    /* =========================
    MOVEMENT LOGIC
    ========================= */

    private void move() throws GameActionException {
        // 1. Micro (combat movement) - causes return
        boolean catMicro = false;
        for (RobotInfo r : neutralRobots) {
            if (Utils.catCanDetect(r, myLoc)) {
                catMicro = true;
                break;
            }
        }
        catMicro |= (BabyRatMicro.shouldAttackCats() && neutralRobots.length > 0);
        if (rc.getRoundNum() - lastMicroed < 2
                && enemyRobots.length == 0
                && neutralRobots.length == 0) {
            Direction tryTurn =
                    BabyRatMicro.closestEnemy == null
                            ? Direction.CENTER
                            : myLoc.directionTo(BabyRatMicro.closestEnemy);
            if (rc.canTurn() && tryTurn != Direction.CENTER) {
                if (rc.getDirection() != tryTurn) rc.turn(tryTurn);
                else {
                    rc.turn(
                            rc.getRoundNum() % 2 == 0
                                    ? tryTurn.rotateLeft()
                                    : tryTurn.rotateRight());
                }
            }
        }
        //        int reachableEnemies = 0;
        //        for (RobotInfo r : enemyRobots) {
        //            if (Navigation.localReachable(r.getLocation())) {
        //                reachableEnemies++;
        //            }
        //        }

        if (enemyRobots.length > 0 || catMicro) {
            moved = BabyRatMicro.processTurn(enemyRobots, neutralRobots, allyRobots, spawnLoc);
            if (enemyRobots.length > 0) {
                lastMicroed = rc.getRoundNum();
            }
            //            if(enemyRobots.length!=0){
            //                return;
            //            }

            //            if (!moved) stuckTurns++;
            //            else stuckTurns = 0;
            if (stuckTurns < 5 || rng.nextBoolean()) return;
        }
        stuckTurns = 0;

        // 2. Update targets
        updateMacroTarget();
        updateMoveTarget();

        if (macroTarget != null) {
            rc.setIndicatorLine(myLoc, macroTarget, 100, 255, 255);
        }

        rc.setIndicatorString(macroTarget + " " + moveTarget);

        if (moveTarget != null) {
            if (goingForCheese) {
                rc.setIndicatorLine(myLoc, moveTarget, 0, 255, 0);
            } else {
                rc.setIndicatorLine(myLoc, moveTarget, 0, 0, 255);
            }
        }

        // 3. Move towards moveTarget
        MoveState state = determineMoveState();
        executeMoveState(state);
    }

    /* =========================
    TARGET UPDATE LOGIC
    ========================= */

    static int ticks = -1;

    /**
     * Checks whether the baby rat should return home to deposit cheese. TODO: User will provide
     * final implementation
     */
    private boolean shouldGoHome() throws GameActionException {
        // if (rc.getRawCheese() == 0) ticks = -1;

        // if (MacroData.hasCheeseMineUpdate()) return true;

        // if (isExplorer()) {
        //     if (rc.getRawCheese() > 0) {
        //         if (ticks == -1) {
        //             ticks = 20;
        //         } else if (ticks > 0) {
        //             ticks--;
        //         } else {
        //             return true;
        //         }
        //     }
        // } else {
        if (MacroData.ratKingFormationLoc != null
                && MacroData.ratKingFormationLoc.distanceSquaredTo(myLoc) < 10) {
            return false;
        }
        if (rc.getCarrying() != null && rc.getRawCheese() < 100) return false;
        if (rc.getRawCheese() > 0) return true;
        // }

        // if (Navigation.needUpdateSymmetry) {
        //     rc.setIndicatorDot(myLoc.add(Direction.NORTH), 0, 0, 255);
        // }
        // RE_ENABLE if we ever need symmetry
        // if (Navigation.needUpdateSymmetry) return true;

        return false;
    }

    /** Updates the macro-level target based on strategic priorities. */
    private void updateMacroTarget() throws GameActionException {
        // 1. Check whether we should go home to spawn location
        if (shouldGoHome()) {
            macroTarget = spawnLoc;
            return;
        }

        // 2. Check build king - if we have a buildKingLoc, go there
        if (MacroData.ratKingFormationLoc != null
                && MacroData.checkBuildKingForBabyRat(
                        neutralRobots.length, enemyRobots.length - allyRobots.length)
                && !isExplorer()) {
            macroTarget = MacroData.ratKingFormationLoc;
            return;
        }

        // 3. Split between specialization: ID % 3 < 2 == cheese, else explore
        if (!isExplorer()) {
            // Cheese specialization
            boolean safety = checkSafety(); // TODO: LOOK INTO THIS
            // pass in safety to check safe cheese mines
            macroTarget = MacroData.getCheeseMineTarget(safety, allMapInfos);

            if (macroTarget != null && macroTarget.distanceSquaredTo(spawnLoc) <= 36) {
                macroTarget = Explore.getExplore3Target();
            }

        } else {
            // Explore specialization
            macroTarget = Explore.getExplore3Target();
        }

        if (macroTarget == null) {
            macroTarget = Explore.getExplore3Target();
        }
    }

    /** Updates the immediate move target based on tactical context. */
    private void updateMoveTarget() throws GameActionException {
        nearbyCatLoc = null;
        MapLocation battleLoc = null;

        // check if we are panic
        if (Comms.isPanic()) {
            if (allyRobots.length > 0) {

                MapLocation cheeseMineLocation = MacroData.getClosestCheeseLocation();

                if (cheeseMineLocation != null
                        && cheeseMineLocation.distanceSquaredTo(myLoc) >= 25) {
                    moveTarget = cheeseMineLocation;
                    return;
                }

                // find the lowest id ally robot
                int minID = Integer.MAX_VALUE;
                MapLocation allyLoc = null;
                for (RobotInfo ally : allyRobots) {
                    if (ally.getType() == UnitType.BABY_RAT && ally.getID() < minID) {
                        minID = ally.getID();
                        allyLoc = ally.getLocation();
                    }
                }

                // 1. If I am the lowest ID baby rat, be the leader and stay put
                if (rc.getID() < minID) {
                    moveTarget = rc.getLocation(); // stay put and be the leader
                    return;
                }

                moveTarget = allyLoc;

                return;
            }
        }

        // 1. Check squeaks for battle nearby or cat nearby
        for (Message msg : rc.readSqueaks(rc.getRoundNum())) {
            if (msg.getBytes() == 1) {
                // Battle squeak - go towards it
                if (battleLoc == null
                        || msg.getSource().distanceSquaredTo(myLoc)
                                < myLoc.distanceSquaredTo(battleLoc)) {
                    battleLoc = msg.getSource();
                }
            } else if (msg.getBytes() == 2) {
                // Cat squeak - track it
                if (nearbyCatLoc == null
                        || msg.getSource().distanceSquaredTo(myLoc)
                                < myLoc.distanceSquaredTo(nearbyCatLoc)) {
                    nearbyCatLoc = msg.getSource();
                }
            }
        }
        if (nearbyCatLoc != null) {
            if (MacroData.cheeseMineTarget != null
                    && MacroData.cheeseMineTarget.distanceSquaredTo(nearbyCatLoc) < 9) {
                MacroData.cheeseMineTarget = null;
                macroTarget = MacroData.getRandomKnownCheeseMine(spawnLoc);
                return;
            }
        }
        // If there is a battle nearby, go towards it
        if (battleLoc != null) {
            moveTarget = battleLoc;
            return;
        }

        // TODO: If there is cheese nearby, and I cannot pickup the cheese, move towards the cheese
        MapLocation cheeseLoc = goForCheese();
        if (cheeseLoc != null) {
            moveTarget = cheeseLoc;
            return;
        }

        // 2. Otherwise, move towards macroTarget
        // If there is a cat nearby, check if it intersects our path
        if (nearbyCatLoc != null && macroTarget != null) {
            // TODO
            // if (checkCatIntersects(nearbyCatLoc)) {
            //     // Cat is in our path - don't update moveTarget, keep previous or wait
            //     // Could add avoidance logic here
            //     rc.setIndicatorString("Cat blocking path to macro target");
            //     // For now, still go to macroTarget but this flag could be used for avoidance
            // }
        }

        // Set moveTarget to macroTarget
        moveTarget = macroTarget;
    }

    /**
     * Returns the closest cheese location if: 1. No cheese is immediately in collect range (dist^2
     * <= 2) 2. This rat is the closest ally to that cheese Returns null otherwise.
     */
    private MapLocation goForCheese() throws GameActionException {
        // I already have enough cheese
        if (rc.getRawCheese() >= CHEESE_BEFORE_GOING_HOME) {
            return null;
        }

        MapLocation bestCheeseLoc = null;
        int bestCheeseAmount = 0;

        for (MapInfo info : allMapInfos) {
            if (info.getCheeseAmount() == 0) {
                continue;
            }
            if (info.getCheeseAmount() >= bestCheeseAmount) {
                int dist = myLoc.distanceSquaredTo(info.getMapLocation());
                // no null ptr execption bc info.getcheesemount is only equal to best cheese amount
                // if best cheese amount is pos and best cheese loc is not null
                if (info.getCheeseAmount() == bestCheeseAmount
                        && bestCheeseLoc.distanceSquaredTo(myLoc)
                                < dist) { // if equal, but im closer. return
                    continue;
                }

                if (!Navigation.localReachable(info.getMapLocation())) {
                    continue;
                }
                bestCheeseAmount = info.getCheeseAmount();
                bestCheeseLoc = info.getMapLocation();
            }
        }

        // No cheese found nearby
        if (bestCheeseLoc == null) {
            return null;
        }

        rc.setIndicatorDot(bestCheeseLoc, 255, 255, 123);

        // If cheese is in collect range, don't need to move towards it
        if (rc.isActionReady() && myLoc.distanceSquaredTo(bestCheeseLoc) <= 2) {
            return null;
        }

        // DO NOT turn. we need to pick up the cheese
        Attention.focus(null, bestCheeseLoc, Attention.AttentionType.CHEESE);
        goingForCheese = true;

        return bestCheeseLoc;
    }

    /* =========================
    MOVEMENT HELPERS
    ========================= */

    private boolean isExplorer() {
        int numCheeseMines = MacroData.getNumberCheeseMines();

        int THRESHOLD;

        if (numCheeseMines == 0) { // TODO: COME UP WITH BETER FORMULA
            THRESHOLD = 100;
        } else if (numCheeseMines <= 1) {
            THRESHOLD = 75;
        } else if (numCheeseMines <= 2) {
            THRESHOLD = 50;
        } else {
            THRESHOLD = 33;
        }
        //        if(rc.getRoundNum()<Navigation.ROUNDS_IN_DIR-2){
        //            return true;
        //        }

        int score = (rc.getID() + rc.getRoundNum() / 2) % 100;

        return score < THRESHOLD;
    }

    private boolean checkSafety() {
        // initial building mines
        if (enemyRobots.length <= allyRobots.length + 1 && rc.getRoundNum() - lastSawCat > 10) {
            return true;
        }
        return false;
    }

    private MoveState determineMoveState() {
        if (USE_BUGSIM && rc.getRoundNum() > Robot.spawnTurn + 5) {
            return MoveState.BUGSIM;
        } else {
            return MoveState.BUGNAV;
        }
    }

    private void executeMoveState(MoveState state) throws GameActionException {
        // Safety check inherited from original code
        if (moveTarget == null) return;

        Direction scoutDir;

        switch (state) {
            case BUGSIM:
                BugSim.move(moveTarget);
                Direction turnDir = rc.getDirection();
                scoutDir = turnDir;
                switch (rc.getRoundNum() % 2) {
                    case 1:
                        scoutDir = turnDir.rotateLeft().rotateLeft();
                        break;
                    case 0:
                        scoutDir = turnDir.rotateRight().rotateRight();
                        break;
                }
                Attention.focus(scoutDir, null, Attention.AttentionType.SCOUT);
                Attention.turn();
                break;

            case BUGNAV:
                Direction navDir = myLoc.directionTo(moveTarget);

                if (navDir != Direction.CENTER) {
                    scoutDir = navDir;
                    switch (rc.getRoundNum() % 2) {
                        case 1:
                            scoutDir = navDir.rotateLeft().rotateLeft();
                            break;
                        case 0:
                            scoutDir = navDir.rotateRight().rotateRight();
                            break;
                    }
                    Attention.focus(null, moveTarget, Attention.AttentionType.MOVE);
                    Attention.focus(scoutDir, null, Attention.AttentionType.SCOUT);
                    Attention.turn();
                }

                BugNav.move(moveTarget);
                break;

            default:
                break;
        }
    }

    /* =========================
    SCORING / CALCULATIONS
    ========================= */

    private int robotScore(RobotInfo i) {
        int curscore = 0;
        if (i.getHealth() > 0) {
            if (i.getHealth() <= 30) {
                curscore += 200;
            }
            if (i.getType() == UnitType.RAT_KING) {
                curscore += 200;
            } else if (i.getType().isBabyRatType()) {
                curscore += 50 + 50 - i.getHealth() / 2;
            } else {
                curscore += 100;
            }
            if (i.getRawCheeseAmount() > 0) {
                curscore += i.getRawCheeseAmount() * 5;
            }
            return curscore;
        }
        return -1;
    }

    //    private int checkThrow(Direction d) throws GameActionException {
    //        if (!rc.canThrowRat()) return -100;
    //
    //        MapLocation dist1 = myLoc.add(d);
    //        if (!rc.onTheMap(dist1)) return -100;
    //
    //        MapLocation dist2 = dist1.add(d);
    //        MapLocation dist3 = dist2.add(d);
    //        MapLocation dist4 = dist3.add(d);
    //
    //        if (!rc.senseMapInfo(dist1).isPassable() || rc.senseRobotAtLocation(dist1) != null)
    //            return -100;
    //
    //        int score = -10 + carryTurns;
    //        if (carryTurns > 9) score += 200; // don't want to swap places
    //        if (BabyRatMicro.calculated_carry && enemyRobots.length > 0) score += 10;
    //        //        else if (carryTurns > 8) score += 100;
    //
    //        if (rc.onTheMap(dist2) && rc.canSenseLocation(dist2)) {
    //            boolean dist2Passable = rc.onTheMap(dist2) && rc.senseMapInfo(dist2).isPassable();
    //
    //            if (rc.canSenseRobotAtLocation(dist2)) {
    //                if (rc.senseRobotAtLocation(dist2).getTeam() == rc.getTeam()) score -= 100;
    //                else score += 100 + robotScore(rc.senseRobotAtLocation(dist2));
    //                return score;
    //            } else if (!dist2Passable) { // dist 2
    //                if (enemyRobots.length == 0) {
    //                    score += 5; // can ratnap again in 20 cd
    //                } else {
    //                    score += 4;
    //                }
    //                return score;
    //            }
    //        }
    //        if (rc.onTheMap(dist3) && rc.canSenseLocation(dist3)) {
    //            boolean dist3Passable = rc.onTheMap(dist3) && rc.senseMapInfo(dist3).isPassable();
    //
    //            if (rc.canSenseRobotAtLocation(dist3)) {
    //                if (rc.senseRobotAtLocation(dist3).getTeam() == rc.getTeam()) score -= 50;
    //                else score += 2;
    //                return score;
    //            } else if (!dist3Passable) { // dist 3
    //                if (enemyRobots.length == 0) {
    //                    score += 4; // can ratnap again in 20 cd
    //                } else {
    //                    score += 2;
    //                }
    //                return score;
    //            }
    //        }
    //        if (rc.onTheMap(dist4) && rc.canSenseLocation(dist4)) {
    //            boolean dist4Passable = rc.onTheMap(dist4) && rc.senseMapInfo(dist4).isPassable();
    //
    //            if (rc.canSenseRobotAtLocation(dist4)) { // dist 4
    //                if (rc.senseRobotAtLocation(dist4).getTeam() == rc.getTeam()) score -= 50;
    //                return score;
    //            } else if (!dist4Passable) { // dist 3
    //                score += 2;
    //                return score;
    //            }
    //        }
    //
    //        return score;
    //    }
    private int checkThrow(Direction d) throws GameActionException {
        if (!rc.canThrowRat()) return -100;

        MapLocation dist1 = myLoc.add(d);
        if (!rc.onTheMap(dist1)) return -100;

        MapLocation dist2 = dist1.add(d);
        MapLocation dist3 = dist2.add(d);
        MapLocation dist4 = dist3.add(d);

        if (!rc.senseMapInfo(dist1).isPassable() || rc.senseRobotAtLocation(dist1) != null)
            return -100;

        int score = -10;
        if (carryTurns > 9) score += 200; // don't want to swap places
        else if (carryTurns > 8) score += 100;
        boolean dist2Passable = rc.onTheMap(dist2) && rc.senseMapInfo(dist2).isPassable();
        boolean dist3Passable = rc.onTheMap(dist3) && rc.senseMapInfo(dist3).isPassable();
        if (!rc.onTheMap(dist4)) dist4 = dist1;

        if (!rc.onTheMap(dist2) || !rc.onTheMap(dist3) || !rc.onTheMap(dist4)) return score;

        if (!dist2Passable) { // dist 2
            score += 100; // can ratnap again in 20 cd
        } else if (rc.canSenseRobotAtLocation(dist2)) {
            if (rc.senseRobotAtLocation(dist2).getTeam() == rc.getTeam()) score -= 100;
            else score += 100 + robotScore(rc.senseRobotAtLocation(dist2));
        } else if (!dist3Passable) { // dist 3
            score += 75;
        } else if (rc.canSenseRobotAtLocation(dist3)) {
            if (rc.senseRobotAtLocation(dist3).getTeam() == rc.getTeam()) score -= 50;
            else score += 75 + robotScore(rc.senseRobotAtLocation(dist3));
        } else if (rc.canSenseRobotAtLocation(dist4)) { // dist 4
            if (rc.senseRobotAtLocation(dist4).getTeam() == rc.getTeam()) score -= 50;
            else score += 50;
        }

        return score;
    }

    /* =========================
    MAIN LOOP
    ========================= */

    public void debugReachability() throws GameActionException {
        for (MapInfo info : rc.senseNearbyMapInfos()) {
            if (info.isPassable()) {
                boolean reachable = Navigation.localReachable(info.getMapLocation());
                if (reachable) {
                    rc.setIndicatorDot(info.getMapLocation(), 0, 255, 0);
                } else {
                    rc.setIndicatorDot(info.getMapLocation(), 255, 0, 0);
                }
            }
        }
    }

    public void turn() throws Exception {
        // alow bugsim to gather info unless going for cheese

        goingForCheese = false; // debug
        finished_moving = false;
        if (carryTurns != 0) {
            carryTurns++;
        }

        setGlobals();
        if (rc.isActionReady()) doAction();
        move();
        finished_moving = true;
        updateGlobalsRobot();
        setGlobals();
        myLoc = rc.getLocation();
        if (rc.isActionReady()) doAction();

        // debugReachability();
    }
}
