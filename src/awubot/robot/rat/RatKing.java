package awubot.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKing {
    private static int built = 0;

    public static void init() {}

    public static void run() throws GameActionException {
        indicate(rc.getGlobalCheese());
        if (built < 20) {
            var buildLoc = rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST);
            if(rc.canBuildRat(buildLoc)) rc.buildRat(buildLoc);
        }
        built++;
    }
}
