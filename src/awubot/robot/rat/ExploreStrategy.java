package awubot_test.robot.rat;

import static awubot_test.Game.rc;

import awubot_test.Pathfinder;
import awubot_test.robot.Strategy;
import awubot_test.util.Util;
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
