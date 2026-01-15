package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class KingSpawningStrategy extends Strategy {
    @Override
    public boolean runTick() throws GameActionException {
        var thresh = Math.min(rc.getGlobalCheese() / 18 + 10, 40);
        if(rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
            MapLocation best = null;
            MapLocation centre = new MapLocation(mapWidth / 2, mapHeight / 2);
            for(var dir : directions) {
                MapLocation spawnLoc = myLoc.add(dir).add(dir);
                if(rc.canBuildRat(spawnLoc)) {
                    if(best == null || spawnLoc.distanceSquaredTo(centre) < best.distanceSquaredTo(centre)) {
                        best = spawnLoc;
                    }
                }
            }
            if(best != null) {
                rc.buildRat(best);
            }
        }
        return true;
    }
}
