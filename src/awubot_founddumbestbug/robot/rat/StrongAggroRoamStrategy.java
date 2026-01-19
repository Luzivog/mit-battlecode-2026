package awubot_founddumbestbug.robot.rat;

import static awubot_founddumbestbug.Game.rc;

import java.util.List;

import awubot_founddumbestbug.Pathfinder;
import awubot_founddumbestbug.robot.Strategy;
import awubot_founddumbestbug.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class StrongAggroRoamStrategy extends Strategy {
    private MapLocation target;
    private List<MapLocation> targets;

    public StrongAggroRoamStrategy() throws GameActionException {
        targets = Util.getEnemySpawnList();
        target = targets.getFirst();
    }

    @Override
    public boolean runTick() throws GameActionException {
        while (rc.canSenseLocation(target)) {
            targets.removeFirst();
            if (targets.isEmpty()) {
                targets.add(Util.genAggroTarget());
            }
            target = targets.getFirst();
        }
        Pathfinder.makeMove(target);
        rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 255);
        return true;
    }
}
