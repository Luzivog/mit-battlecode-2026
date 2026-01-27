package awubot_micro.robot.rat;

import static awubot_micro.Game.rc;

import awubot_micro.Pathfinder;
import awubot_micro.robot.Strategy;
import awubot_micro.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class ExploreStrategy extends Strategy {
    private MapLocation target;

    @Override
    public boolean runTick() throws GameActionException {
        while (target == null || rc.canSenseLocation(target)) {
            target = Util.genPassiveTarget();
        }
        BabyRat.autoMove(Pathfinder.getMove(target));
        rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 255);
        return true;
    }
}
