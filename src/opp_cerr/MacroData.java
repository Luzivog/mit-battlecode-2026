package opp_cerr;

import battlecode.common.*;

import opp_cerr.fast.FastLocSet;
import opp_cerr.fast.FastQueue;

/*
MacroData class to use macro level data from Comms, and interface with things we see
*/

public class MacroData {

    private static RobotController rc;

    static FastLocSet cheeseMineLocations;
    static FastQueue<MapLocation> cheeseMineQueue;

    static MapLocation furthestSafeMine;

    static int catWarningTurn;
    static MapLocation catWarningLoc;

    static int nearbyCombatTurn;
    static MapLocation nearbyCombatLoc;

    static MapLocation ratKingFormationLoc = null;
    static final int MIN_DIST_TO_ALLY_KING = 400;
    static final int CHEESE_FOR_KING = 250;

    public static void init(RobotController r) {
        rc = r;
        cheeseMineLocations = new FastLocSet();
        cheeseMineQueue = new FastQueue<MapLocation>(20);

        catWarningTurn = -100;
        catWarningLoc = null;
        nearbyCombatTurn = -100;
        nearbyCombatLoc = null;
    }

    public static void updateCatWarning(MapLocation loc) {
        catWarningTurn = rc.getRoundNum();
        catWarningLoc = loc;
    }

    public static void updateNearbyCombat(MapLocation loc) {
        nearbyCombatTurn = rc.getRoundNum();
        nearbyCombatLoc = loc;
    }

    public static boolean hasCheeseMineUpdate() {
        return !cheeseMineQueue.isEmpty();
    }

    public static void queueCheeseMine(MapLocation cheeseMine) {
        if (cheeseMineLocations.contains(cheeseMine)) {
            return;
        }
        addCheeseMineLocationToSet(cheeseMine);
        cheeseMineQueue.add(cheeseMine);
    }

    // TODO: if ratking sees a new cheese mine it needs to write it

    public static void queueSqueakCheeseMine(MapLocation cheeseMine) {
        assert (rc.getType() == UnitType.BABY_RAT);

        if (cheeseMineLocations.contains(cheeseMine)) {
            return;
        }

        addCheeseMineLocationToSet(cheeseMine);
    }

    public static void addCheeseMineLocationToSet(MapLocation m) {
        // Add m and all 4 adjacent locations to cheeseMineLocations
        cheeseMineLocations.add(m);
        cheeseMineLocations.add(m.add(Direction.NORTH));
        cheeseMineLocations.add(m.add(Direction.SOUTH));
        cheeseMineLocations.add(m.add(Direction.EAST));
        cheeseMineLocations.add(m.add(Direction.WEST));
    }

    public static int getNumberCheeseMines() {
        for (int i = 0; i < Comms.cheeseMineLocs.length; i++) {
            if (Comms.cheeseMineLocs[i] == null) return i;
        }
        return 10;
    }

    /*
    This function writes a cheeseMine to the global array.
    Called from Comms on processSqueakCheeseMine()
    */
    public static void writeCheeseMineToGlobal(MapLocation cheeseMine) {
        assert (rc.getType() == UnitType.RAT_KING);

        int numMines = getNumberCheeseMines();
        if (numMines >= Comms.numCheeseMines) return;

        if (Comms.lastCheeseMineRefreshRound != rc.getRoundNum()) {
            System.out.println(
                    "[MACRODATA ALERT] This is not good. "
                            + "Last refresh round: "
                            + Comms.lastCheeseMineRefreshRound
                            + ", current round: "
                            + rc.getRoundNum());
            return;
        }
        if (!cheeseMineLocations.contains(cheeseMine)) {
            // write the location to Comms
            Comms.writeCheeseMine(cheeseMine, numMines);
            addCheeseMineLocationToSet(cheeseMine);
            numMines++;
        }

        if (numMines >= Comms.numCheeseMines || !Navigation.symLocked) return;

        MapLocation symLoc = cheeseMine;
        if (Navigation.isSymR()) {
            symLoc = Navigation.getSymR(cheeseMine);
        } else if (Navigation.isSymV()) {
            symLoc = Navigation.getSymR(cheeseMine);
        } else if (Navigation.isSymH()) {
            symLoc = Navigation.getSymR(cheeseMine);
        }

        if (!cheeseMineLocations.contains(symLoc)) {
            System.out.println("location " + cheeseMine + " symmetry " + symLoc);
            Comms.writeCheeseMine(symLoc, numMines);
            addCheeseMineLocationToSet(symLoc);
        }
    }

    public static boolean checkBuildKingForBabyRat(int catsNearby, int enemiesNearby) {
        if (MacroData.ratKingFormationLoc == null) {
            return false;
        }
        if (rc.getCurrentRatCost() < 60) {
            return false;
        }

        if (rc.getLocation().distanceSquaredTo(MacroData.ratKingFormationLoc) < 20
                && (catsNearby > 0
                        || enemiesNearby > 1
                        || rc.readSqueaks(-1).length > 0)) { // TODO: SQUEAKS!!!!!!
            return false;
        }
        return true;
    }

    public static boolean checkBuildKingForKingRat(MapLocation cheeseMine, boolean isSafe) {
        if (!isSafe) {
            return false;
        }

        if (Comms.allyRatKingLocs.getKeys().length == 1 && rc.getHealth() <= 250) {
            return true;
        }

        if (rc.getCurrentRatCost() < 60) {
            return false;
        }
        if (cheeseMine.distanceSquaredTo(rc.getLocation()) < MIN_DIST_TO_ALLY_KING) {
            return false;
        }
        if (cheeseMine.equals(getClosestCheeseLocation())) {
            return false;
        }
        if (MacroData.ratKingFormationLoc == null
                || rc.getLocation().distanceSquaredTo(MacroData.ratKingFormationLoc)
                        < rc.getLocation().distanceSquaredTo(cheeseMine)) {
            return true;
        }
        return false;
    }

    public static void updateMineSafety(MapLocation loc, boolean isSafe)
            throws GameActionException {
        if (rc.getType() != UnitType.RAT_KING) {
            return;
        }

        if (Comms.allyRatKingLocs.size > 1 && RobotPlayer.round > 1190) {
            if (ratKingFormationLoc != null) {
                ratKingFormationLoc = null;
                Comms.updateKingFormationLoc(null);
            }
            return;
        }

        //        System.out.println("recieved message for loc" + loc);
        // if current location is no longer safe,
        if (!isSafe && loc.equals(MacroData.ratKingFormationLoc)) {
            // then update to null
            Comms.updateKingFormationLoc(null);
            ratKingFormationLoc = null;
        }
        boolean wantToBuildKing = checkBuildKingForKingRat(loc, isSafe);
        if (wantToBuildKing) {
            System.out.println("updating new rat king fomration loc at" + ratKingFormationLoc);
            ratKingFormationLoc = loc;
            Comms.updateKingFormationLoc(loc);
        }
    }

    // TODO: improve this routing
    static MapLocation cheeseMineTarget = null;

    public static boolean shouldResetCheeseMineTarget(MapInfo[] nearbyMapInfos) {
        MapLocation myLoc = rc.getLocation();

        int nearbyCheese = 0;
        for (MapInfo i : nearbyMapInfos) {
            if (i.getCheeseAmount() > 0) {
                nearbyCheese++;
            }
        }

        if (myLoc.distanceSquaredTo(cheeseMineTarget) <= 4 && nearbyCheese <= 1) {
            return true;
        }

        return false;
    }

    public static MapLocation getCheeseMineTarget(boolean safety, MapInfo[] nearbyMapInfos) {
        if (cheeseMineTarget == null) {
            cheeseMineTarget = getRandomKnownCheeseMine(null);
        }
        if (cheeseMineTarget == null) {
            return null;
        }
        if (shouldResetCheeseMineTarget(nearbyMapInfos)) {
            // handle safe cheese mine logic
            if (furthestSafeMine == null && safety) {
                furthestSafeMine = cheeseMineTarget;
            }
            if (furthestSafeMine != null && furthestSafeMine.equals(cheeseMineTarget) && !safety) {
                furthestSafeMine = null;
            }
            cheeseMineTarget = null;
        }
        return cheeseMineTarget;
    }

    public static MapLocation getRandomKnownCheeseMine(MapLocation spawnLoc) {
        int numberCheeseMines = getNumberCheeseMines();

        if (numberCheeseMines == 0) return null;

        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);

        MapLocation best = Comms.cheeseMineLocs[RobotPlayer.rng.nextInt(numberCheeseMines)];
        int bestDist = best.distanceSquaredTo(center);

        // sample a few more to bias toward center
        for (int i = 0; i < 1; i++) {
            MapLocation cur = Comms.cheeseMineLocs[RobotPlayer.rng.nextInt(numberCheeseMines)];
            int d = cur.distanceSquaredTo(center);
            if (d < bestDist) {
                best = cur;
                bestDist = d;
            }
        }

        return best;
    }

    public static MapLocation getNearestCheeseMine() {
        int numberCheeseMines = getNumberCheeseMines();
        System.out.println(numberCheeseMines);
        MapLocation myLoc = rc.getLocation();
        if (numberCheeseMines == 0) return null;

        MapLocation best = Comms.cheeseMineLocs[0];
        int bestDist = best.distanceSquaredTo(myLoc);

        for (int i = 1; i < numberCheeseMines; i++) {
            MapLocation cur = Comms.cheeseMineLocs[i];
            int d = cur.distanceSquaredTo(myLoc);
            if (d < bestDist) {
                best = cur;
                bestDist = d;
            }
        }

        return best;
    }

    public static MapLocation getClosestCheeseLocation() {
        MapLocation bestLoc = null;
        if (Comms.allyRatKingLocs.size == 1) {
            MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);

            int bestDist = Integer.MAX_VALUE;

            for (int i = 0; i < Comms.cheeseMineLocs.length; i++) {
                MapLocation loc = Comms.cheeseMineLocs[i];
                if (loc == null) continue;

                int dist = center.distanceSquaredTo(loc);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestLoc = loc;
                }
            }
        } else {
            MapLocation myLoc = rc.getLocation();
            int bestDist = Integer.MAX_VALUE;
            for (int i = 0; i < Comms.cheeseMineLocs.length; i++) {
                MapLocation loc = Comms.cheeseMineLocs[i];
                if (loc == null) continue;
                int dist = myLoc.distanceSquaredTo(loc);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestLoc = loc;
                }
            }
        }

        return bestLoc;
    }

    //    public static MapLocation getClosestCheeseLocation() {
    //        MapLocation myLoc = rc.getLocation();
    //        MapLocation bestLoc = null;
    //        int bestDist = Integer.MAX_VALUE;
    //        for (int i = 0; i < Comms.cheeseMineLocs.length; i++) {
    //            MapLocation loc = Comms.cheeseMineLocs[i];
    //            if (loc == null) continue;
    //            int dist = myLoc.distanceSquaredTo(loc);
    //            if (dist < bestDist) {
    //                bestDist = dist;
    //                bestLoc = loc;
    //            }
    //        }
    //        if (bestLoc == null) return null;
    //        return bestLoc;
    //    }

    // Track Cheese Income Over Last 100 Turns
    // This is an underestimate since other people could be spending cheese
    static FastQueue<Integer> cheeseIncomeHistory = new FastQueue<>(110);
    static int cheeseIncomeSum = 0;
    static int cheeseIncomeCount = 0;
    static int lastTurnCheese = Integer.MAX_VALUE;

    public static void updateCheeseIncome() {
        // called every turn
        // cheese is the amount of cheese gained this turn

        int currentCheese = rc.getGlobalCheese();

        cheeseIncomeHistory.add(Math.max(0, currentCheese - lastTurnCheese));
        cheeseIncomeSum += Math.max(0, currentCheese - lastTurnCheese);
        cheeseIncomeCount++;

        if (cheeseIncomeCount > 100) {
            cheeseIncomeSum -= cheeseIncomeHistory.poll();
            cheeseIncomeCount--;
        }

        lastTurnCheese = currentCheese;
    }

    public static double getAverageCheeseIncome() {
        if (cheeseIncomeCount == 0) return 0;
        return (1.0 * cheeseIncomeSum) / cheeseIncomeCount;
    }
}
