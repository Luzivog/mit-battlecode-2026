package awubot_bugfixes.robot.rat;

import static awubot_bugfixes.Game.rc;

import awubot_bugfixes.Pathfinder;
import awubot_bugfixes.robot.Strategy;
import awubot_bugfixes.util.Util;
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
