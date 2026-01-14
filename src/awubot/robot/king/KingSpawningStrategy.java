package awubot.robot.king;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.util.Rng;
import awubot.util.Util;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class KingSpawningStrategy extends Strategy {
    private final ThreatContext theContext;

    public KingSpawningStrategy(ThreatContext aContext) {
        theContext = aContext;
    }

    public static void spawnRat(MapLocation loc, int strat) throws GameActionException {
        rc.buildRat(loc);
        GlobalArray.writeSpawnStrategy(rc.senseRobotAtLocation(loc).ID, strat);
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getCurrentRatCost() <= 50 && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
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
                spawnRat(best, decideRole());
            }
        }
        return true;
    }

    private int decideRole() throws GameActionException {
        double myRusherRatio = calculateRusherRatio();
        boolean myIsRusher = (Rng.mcg() & 0x7fffffff) < (int)(myRusherRatio * 0x7fffffff);
        // TODO
        return 0;
    }

    private double calculateRusherRatio() {
        if (time < 50) return 0.15;
        if (time < 150) return 0.35;
        if (time < 300) return 0.45;
        return 0.50;
    }

    private MapLocation getNearestMine() {
        if (GlobalArray.cheeseMines.set.length() == 0) return null;

        MapLocation myBest = null;
        int myBestDist = Integer.MAX_VALUE;

        int myMineCount = GlobalArray.cheeseMines.set.length();
        for (int i = 0; i < myMineCount; i++) {
            MapLocation myMine = Util.decode2x2(GlobalArray.cheeseMines.at(i));
            int myDist = myLoc.distanceSquaredTo(myMine);
            if (myDist < myBestDist) {
                myBestDist = myDist;
                myBest = myMine;
            }
        }
        return myBest;
    }
}
