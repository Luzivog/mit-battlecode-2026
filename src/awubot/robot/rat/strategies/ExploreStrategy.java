package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.robot.rat.Rat;
import static awubot.Game.*;

public class ExploreStrategy extends Strategy {
    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        Rat.updateExploreTarget();
        rc.setIndicatorLine(rc.getLocation(), Rat.exploreTarget, 255, 0, 0);
        Pathfinder.makeMove(Rat.exploreTarget);
    }
}
