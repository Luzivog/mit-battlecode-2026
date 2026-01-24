package awubot_quals.robot.rat;

import static awubot_quals.Game.rc;

import java.util.LinkedList;
import java.util.List;

import awubot_quals.Pathfinder;
import awubot_quals.robot.Strategy;
import awubot_quals.util.Util;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class AggroRoamStrategy extends Strategy {
    private MapLocation target;
    private List<MapLocation> targets;

    public AggroRoamStrategy() throws GameActionException {
        //assert (Game.origin != null) : "origin is null";
        targets = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            targets.add(Util.genAggroTarget());
        }
        targets.add(Util.getEnemySpawnList().getFirst()); // fk it we go into their spawn
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
