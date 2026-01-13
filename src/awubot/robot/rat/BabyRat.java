package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.CellTracker;
import awubot.robot.Strategy;
import awubot.robot.rat.strategies.ExperimentStrategy;
import awubot.robot.rat.strategies.HybridStrategy;
import awubot.robot.rat.strategies.MicroTestStrategy;
import awubot.robot.rat.strategies.MinerStrategy;
import awubot.util.Util;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Rng;

public class BabyRat {
    private static Direction lastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;
    public static BabyRatRole role = BabyRatRole.MINER;

    // Cat luring protocol
    private static MapLocation theLureCatLoc = null;
    private static MapLocation theEnemyKingLoc = null;
    private static int theLureTTL = 0;
    private static boolean theIsLuring = false;

    public static void init() throws GameActionException {
        CellTracker.storeVisibleCells();
        readRole();
        primaryStrategy = new HybridStrategy();
        System.out.println("[INIT] R" + rc.getID() + " role=" + role);
    }

    private static void readRole() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                int myKingSlot = findKingSlot(robot.getLocation());
                if (myKingSlot >= 0) {
                    int myRoleValue = GlobalArray.readSpawnRole(myKingSlot);
                    role = BabyRatRole.fromValue(myRoleValue);
                }
                return;
            }
        }

        int myBestSlot = findNearestKingSlot();
        if (myBestSlot >= 0) {
            int myRoleValue = GlobalArray.readSpawnRole(myBestSlot);
            role = BabyRatRole.fromValue(myRoleValue);
        }
    }

    private static int findNearestKingSlot() throws GameActionException {
        int myBestSlot = -1;
        int myBestDist = Integer.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            MapLocation myKingLoc = GlobalArray.getRat(i);
            if (myKingLoc != null) {
                int myDist = myLoc.distanceSquaredTo(myKingLoc);
                if (myDist <= 18 && myDist < myBestDist) {
                    myBestDist = myDist;
                    myBestSlot = i;
                }
            }
        }
        return myBestSlot;
    }

    private static int findKingSlot(MapLocation aKingLoc) throws GameActionException {
        for (int i = 0; i < 5; i++) {
            MapLocation myStoredLoc = GlobalArray.getRatKing(i);
            if (myStoredLoc != null && myStoredLoc.distanceSquaredTo(aKingLoc) <= 8) {
                return i;
            }
        }
        return -1;
    }

    public static void setLureTarget(MapLocation aCatLoc, MapLocation aEnemyKingLoc) {
        theLureCatLoc = aCatLoc;
        theEnemyKingLoc = aEnemyKingLoc;
        theLureTTL = 30;  // Lure for 30 turns
        theIsLuring = true;
    }

    public static void run() throws GameActionException {
        indicate("cheesemines: " + GlobalArray.cheeseMines.set.length());
        shouldGlance = true;

        tryTransferCheese();

        // Cat luring protocol - takes priority
        if (theIsLuring && theLureTTL > 0) {
            theLureTTL--;
            if (tryLureManeuver()) {
                return;  // Skip normal behavior while luring
            }
        }

        CellTracker.checkSymmetry();

        // IF TLE: remove or optimise this somehow
        // it's only here to update dirt data since it might have changed since our last turn
        CellTracker.storeVisibleCells();

        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        primaryStrategy.runTick();

        if (rc.canTurn() && shouldGlance) {
            indicate("glancing");
            var leftDir = myDir.rotateLeft().rotateLeft();
            var rightDir = myDir.rotateRight().rotateRight();
            if (lastGlance == leftDir) {
                turn(rightDir);
                lastGlance = rightDir;
            } else if (lastGlance == rightDir || Rng.mcg() >> 30 != 0) {
                turn(leftDir);
                lastGlance = leftDir;
            } else {
                turn(rightDir);
                lastGlance = rightDir;
            }
        }
    }

    private static boolean tryLureManeuver() throws GameActionException {
        indicate("LURING CAT TO ENEMY!");

        // 1. Move TOWARD ENEMY KING (leading the cat there!)
        if (rc.isMovementReady() && theEnemyKingLoc != null) {
            Direction toEnemy = myLoc.directionTo(theEnemyKingLoc);
            Direction[] tryDirs = {toEnemy, toEnemy.rotateLeft(), toEnemy.rotateRight(),
                                   toEnemy.rotateLeft().rotateLeft(),
                                   toEnemy.rotateRight().rotateRight()};
            for (Direction dir : tryDirs) {
                if (rc.canMove(dir)) {
                    Pathfinder.makeMove(dir);
                    break;
                }
            }
        }

        // 2. SQUEAK to attract cat! Cat will chase us toward enemy!
        rc.squeak(0);  // Empty squeak - cat only cares about sound location

        // 3. Check if we should stop luring
        boolean shouldStopLuring = false;

        // Stop if we reached enemy territory (close to enemy king loc)
        if (theEnemyKingLoc != null && myLoc.distanceSquaredTo(theEnemyKingLoc) <= 36) {
            indicate("REACHED ENEMY TERRITORY - mission complete!");
            shouldStopLuring = true;
        }

        // Stop if TTL expired
        if (theLureTTL <= 0) {
            shouldStopLuring = true;
        }

        // Check if cat is still following
        RobotInfo[] robots = rc.senseNearbyRobots();
        boolean catNearby = false;
        for (RobotInfo r : robots) {
            if (r.getType() == UnitType.CAT) {
                catNearby = true;
                break;
            }
        }

        // If cat lost interest early, stop luring
        if (!catNearby && theLureTTL < 20) {
            shouldStopLuring = true;
        }

        if (shouldStopLuring) {
            theIsLuring = false;
            theLureCatLoc = null;
            theEnemyKingLoc = null;
            return false;  // Resume normal behavior
        }

        return true;  // Still luring
    }

    private static void tryTransferCheese() throws GameActionException {
        int cheese = rc.getRawCheese();
        if (cheese <= 0) return;
        if (Util.chebyshevDistance(myLoc, GlobalArray.nearestRatKing) > 2) return;
        if (rc.canTransferCheese(GlobalArray.nearestRatKing, cheese)) {
            rc.transferCheese(GlobalArray.nearestRatKing, cheese);
        }
    }

}
