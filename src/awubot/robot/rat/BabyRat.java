package awubot.robot.rat;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.rat.strategies.CatLureStrategy2;
import awubot.robot.rat.strategies.HybridStrategy;
import awubot.util.Rng;
import awubot.util.Util;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class BabyRat {
    private static Direction theLastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;
    public static BabyRatRole role = BabyRatRole.MINER;
    private static final CatLureStrategy2 theCatLureStrategy = new CatLureStrategy2();

    public static void init() throws GameActionException {
        CellTracker.storeVisibleCells();
        readRole();
        primaryStrategy = new HybridStrategy();
        System.out.println("[INIT] R" + rc.getID() + " role=" + role);
    }

    public static void setLureTarget(MapLocation aCatLoc, MapLocation aEnemyKingLoc) {
        theCatLureStrategy.setLureTarget(aCatLoc, aEnemyKingLoc);
    }

    public static void run() throws GameActionException {
        indicate("cheesemines: " + GlobalArray.cheeseMines.set.length());
        shouldGlance = true;

        tryTransferCheese();

        if (!theCatLureStrategy.isComplete()) {
            theCatLureStrategy.runTick();
            return;
        }

        CellTracker.checkSymmetry();
        CellTracker.storeVisibleCells();

        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        primaryStrategy.runTick();

        executeGlance();
    }

    private static void readRole() throws GameActionException {
        for (RobotInfo myRobot : rc.senseNearbyRobots()) {
            if (myRobot.getType() == UnitType.RAT_KING && myRobot.getTeam() == rc.getTeam()) {
                int myKingSlot = findKingSlot(myRobot.getLocation());
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
            MapLocation myKingLoc = GlobalArray.getRatKing(i);
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

    private static void tryTransferCheese() throws GameActionException {
        int myCheese = rc.getRawCheese();
        if (myCheese <= 0) return;
        if (Util.chebyshevDistance(myLoc, GlobalArray.nearestRatKing) > 2) return;
        if (rc.canTransferCheese(GlobalArray.nearestRatKing, myCheese)) {
            rc.transferCheese(GlobalArray.nearestRatKing, myCheese);
        }
    }

    private static void executeGlance() throws GameActionException {
        if (!rc.canTurn() || !shouldGlance) return;

        indicate("glancing");
        Direction myLeftDir = myDir.rotateLeft().rotateLeft();
        Direction myRightDir = myDir.rotateRight().rotateRight();

        if (theLastGlance == myLeftDir) {
            turn(myRightDir);
            theLastGlance = myRightDir;
        } else if (theLastGlance == myRightDir || Rng.mcg() >> 30 != 0) {
            turn(myLeftDir);
            theLastGlance = myLeftDir;
        } else {
            turn(myRightDir);
            theLastGlance = myRightDir;
        }
    }
}
