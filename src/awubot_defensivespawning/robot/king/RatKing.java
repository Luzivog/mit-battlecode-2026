package awubot_defensivespawning.robot.king;

import battlecode.common.*;
import awubot_defensivespawning.robot.Strategy;
import awubot_defensivespawning.CellTracker;
import awubot_defensivespawning.RobotTracker;
import awubot_defensivespawning.comms.GlobalArray;
import awubot_defensivespawning.comms.Squeaker;

import static awubot_defensivespawning.Game.*;
import static awubot_defensivespawning.util.Util.*;

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
