package awubot.robot.king;

import battlecode.common.GameActionException;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.king.strategies.RatKingStrategy;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKing {
    public static Strategy primaryStrategy;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
    }

    public static void run() throws GameActionException {
        indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");
        primaryStrategy.runTick();
    }
}
