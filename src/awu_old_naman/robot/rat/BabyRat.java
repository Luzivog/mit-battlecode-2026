package awu_old_naman.robot.rat;

import battlecode.common.*;
import awu_old_naman.CellTracker;
import awu_old_naman.comms.GlobalArray;
import awu_old_naman.robot.Strategy;
import awu_old_naman.robot.rat.CatLureStrategy2;
import awu_old_naman.robot.rat.MinerStrategy;
import awu_old_naman.util.Rng;
import awu_old_naman.util.Util;
import static awu_old_naman.Game.*;
import static awu_old_naman.util.Util.*;

public class BabyRat {
    private static Direction lastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;

    public static void init() throws GameActionException {
        // GlobalArray.readSpawnStrategy();
        primaryStrategy = new MinerStrategy();
    }

    public static void run() throws GameActionException {
        // indicate("cheesemines: " + GlobalArray.cheeseMines.set.length());
        shouldGlance = true;

        tryPickUpCheese();
        tryTransferCheese();

        CellTracker.storeVisibleCells();
        CellTracker.checkSymmetry();

        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        primaryStrategy.runTick();

        executeGlance();
    }

    private static void executeGlance() throws GameActionException {
        if (!rc.canTurn() || !shouldGlance) return;

        indicate("glancing");
        Direction myLeftDir = myDir.rotateLeft().rotateLeft();
        Direction myRightDir = myDir.rotateRight().rotateRight();

        if (lastGlance == myLeftDir) {
            turn(myRightDir);
            lastGlance = myRightDir;
        } else if (lastGlance == myRightDir || Rng.mcg() >> 30 != 0) {
            turn(myLeftDir);
            lastGlance = myLeftDir;
        } else {
            turn(myRightDir);
            lastGlance = myRightDir;
        }
    }

    public static void tryPickUpCheese() throws GameActionException {
        var dirs = relatedDirections(myDir);
        for(var dir : dirs) {
            var loc = myLoc.add(dir);
            if(rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
            }
        }
    }

    // IF TLE: spam this less / optimise
    private static void tryTransferCheese() throws GameActionException {
        int myCheese = rc.getRawCheese();
        if (myCheese <= 0) return;
        if (chebyshevDistance(myLoc, GlobalArray.nearestRatKing) > 2) return;
        // try all possible directions
        var dir = myDir.rotateLeft();
        for(int i = 2; i >= 0; i--) {
            var kingLoc = myLoc.add(dir);
            if (rc.canTransferCheese(kingLoc, myCheese)) {
                rc.transferCheese(kingLoc, myCheese);
            }
            dir = dir.rotateRight();
        }
    }

    public static void rawMove(Direction dir) throws GameActionException {
        if(dir == myDir) {
            rawMove();
            return;
        }
        _rawMove(dir);
        // IF TLE: optimise this
        CellTracker.storeVisibleCells();
        tryPickUpCheese();
        tryTransferCheese();
    }

    public static void turn(Direction dir) throws GameActionException {
        _turn(dir);
        tryPickUpCheese();
        tryTransferCheese();
    }

    public static void rawMove() throws GameActionException {
        _rawMove(myDir);
        CellTracker.storeVisibleCellsPostMoveForward();
        tryPickUpCheese();
        tryTransferCheese();
    }
}
