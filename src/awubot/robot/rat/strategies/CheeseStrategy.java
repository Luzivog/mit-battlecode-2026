package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.robot.rat.Rat;
import static awubot.Game.*;

public class CheeseStrategy extends Strategy {
    @Override
    public boolean isComplete() throws GameActionException {
        Rat.refreshCheeseTarget();
        return Rat.cheeseTarget == null;
    }

    @Override
    public void runTick() throws GameActionException {
        Rat.lastSeenCheese = rc.getRoundNum();
        if (rc.getLocation().isAdjacentTo(Rat.cheeseTarget)) {
            Pathfinder.turn(rc.getLocation().directionTo(Rat.cheeseTarget));
            if (rc.isActionReady()) {
                rc.pickUpCheese(Rat.cheeseTarget);
                Rat.cheeseTarget = null;
            }
            return;
        }
        rc.setIndicatorLine(rc.getLocation(), Rat.cheeseTarget, 255, 0, 0);
        Pathfinder.makeMove(Rat.cheeseTarget);
    }
}
