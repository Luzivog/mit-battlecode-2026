package awubot_quals.robot.rat;

import battlecode.common.*;
import awubot_quals.CellTracker;
import awubot_quals.RobotTracker;
import awubot_quals.comms.GlobalArray;
import awubot_quals.comms.Squeaker;
import awubot_quals.robot.Strategy;
import awubot_quals.robot.rat.CatLureStrategy2;
import awubot_quals.robot.rat.MinerStrategy;
import awubot_quals.robot.rat.combat.CombatStrategy;
import awubot_quals.util.Rng;
import awubot_quals.util.Util;
import static awubot_quals.Game.*;
import static awubot_quals.util.Util.*;
import awubot_quals.util.Profiler;

public class BabyRat {
    private static Direction lastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;
    public static CombatStrategy combatStrategy;

    public static void init() throws GameActionException {
        // GlobalArray.readSpawnStrategy();
        primaryStrategy = new MinerStrategy();
        combatStrategy = new CombatStrategy();
    }

    public static void run() throws GameActionException {
        isLastMove = false;
        // indicate("cheesemines: " + GlobalArray.cheeseMines.set.length());
        shouldGlance = true;

        CellTracker.storeVisibleCells();

        tryPickUpCheese();
        tryTransferCheese();

        if (rc.isBeingThrown() || rc.isBeingCarried()) {
            return;
        }

        indicate("before combat strat: " + Clock.getBytecodeNum());
        if (combatStrategy.runTick()) {
            return;
        }

        // all nonessential systems go here, only run if we are not in combat
        RobotTracker.reset();
        CellTracker.checkSymmetry();
        RobotTracker.updateHistoricalCatLocs();

        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        primaryStrategy.runTick();
        combatStrategy.miniTick();

        isLastMove = true;
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

    // IF TLE: unroll
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

    public static boolean isLastMove;
    public static void rawMove(Direction dir) throws GameActionException {
        if(dir == myDir) {
            rawMove();
            return;
        }
        _rawMove(dir);
        tryPickUpCheese();
        tryTransferCheese();
        // IF TLE: optimise this
        if (!isLastMove) {
            CellTracker.storeVisibleCells();
            RobotTracker.updateRobots();
        }
    }

    public static void turn(Direction dir) throws GameActionException {
        if (dir == myDir) return;
        _turn(dir);
        tryPickUpCheese();
        tryTransferCheese();
        if (!isLastMove) {
            CellTracker.storeVisibleCells();
            RobotTracker.updateRobots();
        }
    }

    public static void rawMove() throws GameActionException {
        _rawMove(myDir);
        tryPickUpCheese();
        tryTransferCheese();
        if (!isLastMove) {
            CellTracker.storeVisibleCellsPostMoveForward();
            RobotTracker.updateRobots();
        }
    }

    public static void attack(RobotInfo info) throws GameActionException {
        rc.attack(myLoc.add(myLoc.directionTo(info.location)));
    }

    public static void attack(RobotInfo info, int damage) throws GameActionException {
        rc.attack(myLoc.add(myLoc.directionTo(info.location)), CombatStrategy.getCheeseNeeded(damage - 10));
    }
}
