package awubot_mystery.robot.king;

import battlecode.common.*;
import awubot_mystery.robot.Strategy;
import static awubot_mystery.robot.king.RatKing.*;
import static awubot_mystery.Game.*;
import static awubot_mystery.util.Util.*;

import awubot_mystery.RobotTracker;
import awubot_mystery.comms.GlobalArray;

public class KingSpawningStrategy extends Strategy {
    private static int numSpawned = 0;

    @Override
    public boolean runTick() throws GameActionException {
        indicate("spawned " + numSpawned);

        int numKings = Math.max(1, GlobalArray.countRatKings());
        int extraPerKing = (rc.getGlobalCheese() / 1000) * (numKings - 1);
        int maxRatCost = 40 * numKings + extraPerKing;
        int thresh = Math.min(rc.getGlobalCheese() / 20 + 10, maxRatCost);

        if (RobotTracker.numCats > 0 && RobotTracker.numAllies == 0) {
            thresh += 10;
        }

        MapLocation target = null;
        int targetNeighbours = 0;
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo robot = RobotTracker.sortedEnemies[i];
            MapLocation loc = robot.getLocation();
            int cnt = 0;
            for (RobotInfo info : rc.senseNearbyRobots(loc, 4, team == Team.A ? Team.B : Team.A)) {
                if (info.getTeam() == team) cnt++;
            }
            if (cnt <= 1 && (target == null || cnt > targetNeighbours)) {
                target = loc;
                targetNeighbours = cnt;
            }
        }
        if (target == null) target = new MapLocation(mapWidth / 2, mapHeight / 2);
        else {
            indicate("targeting enemy cluster at " + target + " with " + targetNeighbours + " neighbours");
            thresh += 10;
        }

        if (rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
            trySpawn(target);
        }
        return true;
    }


    void trySpawn(MapLocation target) throws GameActionException {
        MapLocation best = null;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) == 2) {
                    MapLocation loc = myLoc.translate(dx, dy);
                    if (rc.canBuildRat(loc)) {
                        if (best == null || best.distanceSquaredTo(target) > loc.distanceSquaredTo(target))
                        best = loc;
                        break;
                    }
                }
            }
        }
        if (best != null) {
            rc.turn(best.directionTo(target));
            rc.buildRat(best);
            numSpawned++;
        }
    }
}