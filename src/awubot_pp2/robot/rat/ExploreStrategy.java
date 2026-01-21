package awubot_pp2.robot.rat;

import static awubot_pp2.Game.rc;

import awubot_pp2.Pathfinder;
import awubot_pp2.robot.Strategy;
import awubot_pp2.util.Util;
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
