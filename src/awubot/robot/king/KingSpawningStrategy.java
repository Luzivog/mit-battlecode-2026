package awubot.robot.king;

import battlecode.common.*;
import awubot.robot.Strategy;
import awubot.util.Rng;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.RobotTracker;
import awubot.comms.CheeseTracker;
import awubot.comms.GlobalArray;

public class KingSpawningStrategy extends Strategy {
    private static int numSpawned = 0;
    private static int lastPanicSpawn = -6767;

    public static int threshForNumRats(int x) {
        return ((x - 1) / 4) * 10 + 10;
    }

    @Override
    public boolean runTick() throws GameActionException {
        indicate("spawned " + numSpawned);

        boolean gainedCheese = CheeseTracker.hasGainedCheese();
        boolean inCombat = CheeseTracker.isInCombat();
        boolean smallMap = mapWidth * mapHeight <= 1200;
        double cheeseGainRate = CheeseTracker.getCheeseGainRate();
        indicate("cheese gain rate: " + cheeseGainRate);

        int defaultThresh = 40;
        if (smallMap) defaultThresh = 30;

        int decayThresh = 3000 - Math.max(0, time - 1000);

        int thresh = defaultThresh;
        if (time < 100) {
            ; // default
        } else if (!inCombat) {
            if (gainedCheese) {
                if (rc.getAllCheese() >= 2500) thresh = 1000;
                else thresh = threshForNumRats(rc.getAllCheese() / 67);
            }
            thresh = Math.min(thresh, rc.getAllCheese() / 20);
        } else if (smallMap) {
            if (rc.getGlobalCheese() >= 2000 - Math.max(0, time - 1000)) {
                thresh = 10000;
            } else if (rc.getAllCheese() > 500 + GlobalArray.numberOfRatsAlive * GlobalArray.numberOfRatsAlive) {
                thresh = Math.max(thresh, (int) (CheeseTracker.getCheeseGainRate() * 6.7));
            }
        } else {
            // big map
            if (rc.getGlobalCheese() >= 3000 - Math.max(0, time - 1000)) {
                thresh = 1000;
            } else if (rc.getAllCheese() > 500 + GlobalArray.numberOfRatsAlive * GlobalArray.numberOfRatsAlive) {
                thresh = Math.max(thresh, (int) (CheeseTracker.getCheeseGainRate() * 6.7));
            }
        }

        if (rc.getAllCheese() < 200) thresh = rc.getAllCheese() / 10;

        if (RobotTracker.numCats > 0 && RobotTracker.numAllies == 0) {
            thresh += 10;
        }

        MapLocation target = null;
        int targetNeighbours = 0;
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo robot = RobotTracker.sortedEnemies[i];
            MapLocation loc = robot.getLocation();
            int cnt = 0;
            for (RobotInfo info : rc.senseNearbyRobots(loc, 2, team == Team.A ? Team.B : Team.A)) {
                if (info.getTeam() == team) cnt++;
            }
            if (cnt <= 1 && (target == null || cnt > targetNeighbours)) {
                target = loc;
                targetNeighbours = cnt;
            }
        }
        boolean isEnemy = false;
        if (target == null) {
            // even out spawning
            if (GlobalArray.ratCounter % GlobalArray.countRatKings() == GlobalArray.getIndexAmongAliveRatKings()) {
                target = new MapLocation(mapWidth / 2, mapHeight / 2);
            }
        } else {
            isEnemy = true;
            if (time - lastPanicSpawn > 4 && rc.getHealth() < lastHP) {
                indicate("targeting enemy cluster at " + target + " with " + targetNeighbours + " neighbours");
                thresh += 10;
                if (rc.getHealth() <= 100) {
                    thresh += 10;
                }
                lastPanicSpawn = time;
            }
        }

        if (target != null && rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
            trySpawn(target, isEnemy);
        }
        return true;
    }


    void trySpawn(MapLocation target, boolean isEnemy) throws GameActionException {
        MapLocation best = null;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) == 2) {
                    MapLocation loc = myLoc.translate(dx, dy);
                    if (rc.canBuildRat(loc)) {
                        if (isEnemy && loc.distanceSquaredTo(target) <= 2) continue;
                        if (best == null || best.distanceSquaredTo(target) > loc.distanceSquaredTo(target))
                        best = loc;
                    }
                }
            }
        }
        if (best != null) {
            rc.turn(best.directionTo(target));
            rc.buildRat(best);
            RobotTracker.addAlly(best);
            GlobalArray.registerSpawnedRat(best, Rng.mcg() & ((1 << 13) - 1));
            numSpawned++;
            RatKing.spawnedThisTurn = true;
        }
    }
}
