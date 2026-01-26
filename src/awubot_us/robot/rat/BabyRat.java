package awubot_us.robot.rat;

import battlecode.common.*;
import awubot_us.CellTracker;
import awubot_us.Pathfinder;
import awubot_us.RobotTracker;
import awubot_us.comms.GlobalArray;
import awubot_us.comms.Squeaker;
import awubot_us.robot.Strategy;
import awubot_us.robot.rat.CatLureStrategy2;
import awubot_us.robot.rat.MinerStrategy;
import awubot_us.robot.rat.combat.CombatStrategy;
import awubot_us.util.Rng;
import awubot_us.util.Util;
import static awubot_us.Game.*;
import static awubot_us.util.Util.*;
import awubot_us.util.Profiler;

public class BabyRat {
    private static Direction lastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;
    public static CombatStrategy combatStrategy;

    public static void init() throws GameActionException {
        int payload = GlobalArray.loadSpawnInfo();
        println("BabyRat payload: " + payload + " id " + rc.getID() + " index " + myIndex);
        primaryStrategy = new MinerStrategy();
        combatStrategy = new CombatStrategy();
        Pathfinder.init();
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
        for(int i = 0; i < 5; i++) {
            var kingLoc = GlobalArray.getRatKing(i);
            if(kingLoc != null && kingLoc.distanceSquaredTo(myLoc) <= 18) {
                var dir = kingLoc.directionTo(myLoc).rotateLeft();
                for(int j = 0; j < 3; j++) {
                    var loc = kingLoc.add(dir);
                    if(rc.canTransferCheese(loc, myCheese)) {
                        rc.transferCheese(loc, myCheese);
                        return;
                    }
                    dir = dir.rotateRight();
                }
            }
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
