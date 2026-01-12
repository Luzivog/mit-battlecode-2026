package awubot.robot.rat;

import battlecode.common.GameActionException;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.rat.strategies.king.RatKingStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKing {
    public static Strategy primaryStrategy;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
    }

    public static void run() throws GameActionException {
        GlobalArray.writeRatLoc(kingId);
        indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");
        primaryStrategy.runTick();
    }
}
