package awubot_fixtrapandratkingsuicide.robot.rat;

import static awubot_fixtrapandratkingsuicide.Game.rc;

import awubot_fixtrapandratkingsuicide.Pathfinder;
import awubot_fixtrapandratkingsuicide.robot.Strategy;
import awubot_fixtrapandratkingsuicide.util.Util;
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
