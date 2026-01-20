package awubot_microtrapless.robot.king;

import battlecode.common.*;
import awubot_microtrapless.robot.Strategy;
import awubot_microtrapless.CellTracker;
import awubot_microtrapless.RobotTracker;
import awubot_microtrapless.comms.GlobalArray;
import awubot_microtrapless.comms.Squeaker;

import static awubot_microtrapless.Game.*;
import static awubot_microtrapless.util.Util.*;

public class RatKing {
    public static Strategy primaryStrategy;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
    }

    public static void run() throws GameActionException {
        indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");

        CellTracker.storeVisibleCellsKing();

        tryPickUpCheese();

        primaryStrategy.runTick();
    }

    public static void tryPickUpCheese() throws GameActionException {
        for(var dir : allDirections) {
            var loc = myLoc.add(dir);
            if(rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
            }
        }
    }

    public static void rawMove(Direction dir) throws GameActionException {
        _rawMove(dir);
        tryPickUpCheese();
    }

    public static void turn(Direction dir) throws GameActionException {
        _turn(dir);
    }
}
