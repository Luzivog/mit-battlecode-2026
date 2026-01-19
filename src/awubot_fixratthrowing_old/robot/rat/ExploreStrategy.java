package awubot_fixratthrowing_old.robot.rat;

import static awubot_fixratthrowing_old.Game.rc;

import awubot_fixratthrowing_old.Pathfinder;
import awubot_fixratthrowing_old.robot.Strategy;
import awubot_fixratthrowing_old.util.Util;
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
