package awubot_dspawn.robot.rat;

import static awubot_dspawn.Game.rc;

import awubot_dspawn.Pathfinder;
import awubot_dspawn.robot.Strategy;
import awubot_dspawn.util.Util;
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
