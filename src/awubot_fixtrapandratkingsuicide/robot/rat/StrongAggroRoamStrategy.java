package awubot_fixtrapandratkingsuicide.robot.rat;

import static awubot_fixtrapandratkingsuicide.Game.rc;

import java.util.List;

import awubot_fixtrapandratkingsuicide.Pathfinder;
import awubot_fixtrapandratkingsuicide.robot.Strategy;
import awubot_fixtrapandratkingsuicide.util.Util;
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
