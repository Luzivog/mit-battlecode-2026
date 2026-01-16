package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import awubot.CellTracker;
import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKing {
    public static Strategy primaryStrategy;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
    }

    public static void run() throws GameActionException {
        indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");

        RobotTracker.writeHistoricalKingLocs();
        GlobalArray.preTick();
        Squeaker.preTick();
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
