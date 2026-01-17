package awubot_fixtrapandratkingsuicide.robot.king;

import battlecode.common.*;
import awubot_fixtrapandratkingsuicide.robot.Strategy;
import awubot_fixtrapandratkingsuicide.CellTracker;
import awubot_fixtrapandratkingsuicide.RobotTracker;
import awubot_fixtrapandratkingsuicide.comms.GlobalArray;
import awubot_fixtrapandratkingsuicide.comms.Squeaker;

import static awubot_fixtrapandratkingsuicide.Game.*;
import static awubot_fixtrapandratkingsuicide.util.Util.*;

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
