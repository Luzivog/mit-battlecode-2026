package awubot.robot.rat.strategies;

import static awubot.Game.rc;

import java.util.List;

import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class MigratedStrongAggroRoamStrategy extends Strategy {
    private MapLocation target;
    private List<MapLocation> targets;

    public MigratedStrongAggroRoamStrategy() throws GameActionException {
        targets = Util.getEnemySpawnList();
        target = targets.getFirst();
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        while (rc.canSenseLocation(target)) {
            targets.removeFirst();
            if (targets.isEmpty()) {
                targets.add(Util.genAggroTarget());
            }
            target = targets.getFirst();
        }
        Pathfinder.makeMove(target);
        rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 255);
    }
}
