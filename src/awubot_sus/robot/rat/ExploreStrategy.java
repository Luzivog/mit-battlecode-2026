package awubot_sus.robot.rat;

import static awubot_sus.Game.rc;

import awubot_sus.Pathfinder;
import awubot_sus.robot.Strategy;
import awubot_sus.util.Util;
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
