package awubot.robot.rat;

import static awubot.Game.rc;

import awubot.robot.Strategy;
import awubot.util.Util;
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
