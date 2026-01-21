package awubot_pp2.robot.king;

import battlecode.common.*;
import awubot_pp2.robot.Strategy;
import awubot_pp2.CellTracker;
import awubot_pp2.RobotTracker;
import awubot_pp2.comms.GlobalArray;
import awubot_pp2.comms.Squeaker;

import static awubot_pp2.Game.*;
import static awubot_pp2.util.Util.*;

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
