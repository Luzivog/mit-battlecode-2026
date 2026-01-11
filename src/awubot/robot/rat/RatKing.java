package awubot.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKing extends Rat {
    private int built = 0;

    @Override
    public void init() throws GameActionException {
    }

    @Override
    public void run() throws GameActionException {
        indicate(rc.getGlobalCheese());
        if (built < 5) {
            rc.buildRat(rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST));
        }
        built++;
    }
}
