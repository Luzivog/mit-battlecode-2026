package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import static awubot.robot.king.RatKing.*;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.RobotTracker;
import awubot.comms.GlobalArray;

public class KingSpawningStrategy extends Strategy {
    private static int numSpawned = 0;

    @Override
    public boolean runTick() throws GameActionException {
        indicate("spawned " + numSpawned);

        int numKings = GlobalArray.countRatKings();
        int extraPerKing = (rc.getGlobalCheese() / 1000) * (numKings - 1);
        int maxRatCost = 10 + 40 * numKings + extraPerKing;
        
        if (rc.getCurrentRatCost() >= maxRatCost) {
            indicate("at rat limit (" + (numKings * 16) + " rats for " + numKings + " kings)");
            return true;
        }

        var thresh = Math.min(rc.getGlobalCheese() / 20 + 10, maxRatCost);

        if (RobotTracker.numCats > 0 && RobotTracker.numAllies == 0) {
            thresh = 50;
        }

        // defensive spawning if enemies outnumber allies
        MapLocation target = null;
        var cnt = 0;
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo robot = RobotTracker.sortedEnemies[i];
            if (robot == null || robot.getType() != UnitType.BABY_RAT) continue;
            cnt++;
            MapLocation loc = robot.getLocation();
            if (target == null || loc.distanceSquaredTo(myLoc) < target.distanceSquaredTo(myLoc)) {
                target = loc;
            }
        }
        for (int i = 0; i < RobotTracker.numAllies; i++) {
            RobotInfo robot = RobotTracker.sortedAllies[i];
            if (robot == null || robot.getType() != UnitType.BABY_RAT) continue;
            cnt--;
        }
        if(cnt > 0) thresh += 20;
        if(target == null) target = new MapLocation(mapWidth / 2, mapHeight / 2);

        if(rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
            trySpawn(target);
        }
        return true;
    }

    // TODO: consider all 16 spawning locations instead of just 8
    void trySpawn(MapLocation target) throws GameActionException {
        MapLocation best = null;
        for(var dir : directions) {
            MapLocation spawnLoc = myLoc.add(dir).add(dir);
            if(rc.canBuildRat(spawnLoc)) {
                if(best == null || spawnLoc.distanceSquaredTo(target) < best.distanceSquaredTo(target)) {
                    best = spawnLoc;
                }
            }
        }
        if(best != null) {
            turn(best.directionTo(target));
            rc.buildRat(best);
            numSpawned++;
        }
    }
}
