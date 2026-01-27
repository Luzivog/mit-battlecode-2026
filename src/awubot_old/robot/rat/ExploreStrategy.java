package awubot_old.robot.rat;

import static awubot_old.Game.rc;

import awubot_old.Pathfinder;
import awubot_old.robot.Strategy;
import awubot_old.util.Util;
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
