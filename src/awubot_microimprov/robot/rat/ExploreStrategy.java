package awubot_microimprov.robot.rat;

import static awubot_microimprov.Game.rc;

import awubot_microimprov.Pathfinder;
import awubot_microimprov.robot.Strategy;
import awubot_microimprov.util.Util;
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
