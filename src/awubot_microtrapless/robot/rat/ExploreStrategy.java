package awubot_microtrapless.robot.rat;

import static awubot_microtrapless.Game.rc;

import awubot_microtrapless.Pathfinder;
import awubot_microtrapless.robot.Strategy;
import awubot_microtrapless.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class ExploreStrategy extends Strategy {
    private MapLocation target;

    @Override
    public boolean runTick() throws GameActionException {
        while (target == null || rc.canSenseLocation(target)) {
            target = Util.genPassiveTarget();
        }
        Pathfinder.makeMove(target);
        rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 255);
        return true;
    }
}
