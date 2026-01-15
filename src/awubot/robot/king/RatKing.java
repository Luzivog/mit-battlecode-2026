package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;

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

    public static void rawMove(Direction dir) throws GameActionException {
        _rawMove(dir);
    }

    public static void turn(Direction dir) throws GameActionException {
        _turn(dir);
    }
}
