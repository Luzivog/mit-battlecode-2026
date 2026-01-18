package awubot.robot.rat;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;
import awubot.robot.Strategy;
import awubot.robot.rat.CatLureStrategy2;
import awubot.robot.rat.MinerStrategy;
import awubot.robot.rat.combat.CombatStrategy;
import awubot.util.Rng;
import awubot.util.Util;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Profiler;

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
        // indicate("cheesemines: " + GlobalArray.cheeseMines.set.length());
        shouldGlance = true;

        tryPickUpCheese();
        tryTransferCheese();

        if (rc.isBeingThrown()) {
            if (rc.canTurn()) {
                rc.turn(myLoc.directionTo(myPreviousLoc));
            }
        }

        // indicate("before combat strat: " + Clock.getBytecodeNum());
        if (combatStrategy.runTick()) {
            return;
        }

        // all nonessential systems go here, only run if we are not in combat
        CellTracker.checkSymmetry();

        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        primaryStrategy.runTick();
        combatStrategy.miniTick();

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
        RobotTracker.updateRobots();
    }

    public static void turn(Direction dir) throws GameActionException {
        _turn(dir);
        tryPickUpCheese();
        tryTransferCheese();
        RobotTracker.updateRobots();
    }

    public static void rawMove() throws GameActionException {
        _rawMove(myDir);
        CellTracker.storeVisibleCellsPostMoveForward();
        tryPickUpCheese();
        tryTransferCheese();
        RobotTracker.updateRobots();
    }

    public static void attack(RobotInfo info) throws GameActionException {
        rc.attack(myLoc.add(myLoc.directionTo(info.location)));
    }

}
