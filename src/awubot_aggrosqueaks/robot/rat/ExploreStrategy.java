package awubot_aggrosqueaks.robot.rat;

import static awubot_aggrosqueaks.Game.rc;

import awubot_aggrosqueaks.robot.Strategy;
import awubot_aggrosqueaks.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class ExploreStrategy extends Strategy {
    private MapLocation target;
    private TraverseStrategy traverseStrategy = new TraverseStrategy(new MapLocation(0, 0), 0);

    @Override
    public boolean runTick() throws GameActionException {
        while (target == null || rc.canSenseLocation(target)) {
            target = Util.genPassiveTarget();
        }
        traverseStrategy.updateTarget(target);
        traverseStrategy.runTick();
        return true;
    }
}
