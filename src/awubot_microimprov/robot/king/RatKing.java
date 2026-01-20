package awubot_microimprov.robot.king;

import battlecode.common.*;
import awubot_microimprov.robot.Strategy;
import awubot_microimprov.CellTracker;
import awubot_microimprov.RobotTracker;
import awubot_microimprov.comms.GlobalArray;
import awubot_microimprov.comms.Squeaker;

import static awubot_microimprov.Game.*;
import static awubot_microimprov.util.Util.*;

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
