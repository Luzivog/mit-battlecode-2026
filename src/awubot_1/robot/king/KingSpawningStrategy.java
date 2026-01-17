package awubot_1.robot.king;

import battlecode.common.*;
import awubot_1.robot.Strategy;
import static awubot_1.robot.king.RatKing.*;
import static awubot_1.Game.*;
import static awubot_1.util.Util.*;

import awubot_1.RobotTracker;

public class KingSpawningStrategy extends Strategy {
    @Override
    public boolean runTick() throws GameActionException {
        var thresh = Math.min(rc.getGlobalCheese() / 10 + 10, 40);
        if (RobotTracker.numCats > 0 && RobotTracker.numAllies == 0) {
            thresh = 50;
        }
        if(rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
            MapLocation best = null;
            Direction bestDir = null;
            MapLocation centre = new MapLocation(mapWidth / 2, mapHeight / 2);
            for(var dir : directions) {
                MapLocation spawnLoc = myLoc.add(dir).add(dir);
                if(rc.canBuildRat(spawnLoc)) {
                    if(best == null || spawnLoc.distanceSquaredTo(centre) < best.distanceSquaredTo(centre)) {
                        best = spawnLoc;
                        bestDir = dir;
                    }
                }
            }
            if(best != null) {
                turn(bestDir);
                rc.buildRat(best);
            }
        }
        return true;
    }
}
