package awubot_founddumbestbug.robot.rat;

import static awubot_founddumbestbug.Game.rc;

import awubot_founddumbestbug.Pathfinder;
import awubot_founddumbestbug.robot.Strategy;
import awubot_founddumbestbug.util.Util;
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
