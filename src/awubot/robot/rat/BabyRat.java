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
        GlobalArray.readSpawnStrategy();
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
