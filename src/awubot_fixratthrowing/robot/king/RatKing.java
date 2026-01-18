package awubot_fixratthrowing.robot.king;

import battlecode.common.*;
import awubot_fixratthrowing.robot.Strategy;
import awubot_fixratthrowing.CellTracker;
import awubot_fixratthrowing.RobotTracker;
import awubot_fixratthrowing.comms.GlobalArray;
import awubot_fixratthrowing.comms.Squeaker;

import static awubot_fixratthrowing.Game.*;
import static awubot_fixratthrowing.util.Util.*;

public class RatKing {
    public static Strategy primaryStrategy;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
    }

    public static void run() throws GameActionException {
        indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");

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
