package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import static awubot.robot.king.RatKing.*;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.RobotTracker;

public class KingSpawningStrategy extends Strategy {
    @Override
    public boolean runTick() throws GameActionException {
        var thresh = Math.min(rc.getGlobalCheese() / 10 + 10, 40);
        if (RobotTracker.numCats > 0 && RobotTracker.numAllies == 0) {
            thresh = 50;
        }

        // defensive spawning if enemies outnumber allies
        MapLocation target = null;
        var cnt = 0;
        for(var robot : rc.senseNearbyRobots()) {
            if(robot.getType() == UnitType.BABY_RAT) {
                if(robot.getTeam() != team) {
                    cnt++;
                    var loc = robot.getLocation();
                    if(target == null || loc.distanceSquaredTo(myLoc) < target.distanceSquaredTo(myLoc)) {
                        target = loc;
                    }
                } else {
                    cnt--;
                }
            }
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
        }
    }
}
