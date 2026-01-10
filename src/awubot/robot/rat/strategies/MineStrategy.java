package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.robot.rat.Rat;
import static awubot.Game.*;

public class MineStrategy extends Strategy {
    @Override
    public boolean isComplete() throws GameActionException {
        Rat.refreshMineLocation();
        return Rat.mineLocation == null;
    }

    @Override
    public void runTick() throws GameActionException {
        Rat.updateMineTarget();
        rc.setIndicatorLine(rc.getLocation(), Rat.mineTarget, 255, 0, 0);
        Pathfinder.makeMove(Rat.mineTarget);
    }
}
