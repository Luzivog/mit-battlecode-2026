package awubot.robot.rat.strategies;

import static awubot.Game.rc;

import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class MigratedExploreStrategy extends Strategy {
    private MapLocation target;

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        while (target == null || rc.canSenseLocation(target)) {
            target = Util.genPassiveTarget();
        }
        Pathfinder.makeMove(target);
        rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 255);
    }
}
