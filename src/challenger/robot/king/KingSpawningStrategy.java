package challenger.robot.king;

import battlecode.common.*;
import challenger.robot.Strategy;
import challenger.util.MineEstimate;
import challenger.util.Rng;
import static challenger.Game.*;
import static challenger.util.Util.*;

import challenger.RobotTracker;
import challenger.comms.GlobalArray;

public class KingSpawningStrategy extends Strategy {
    private static int numSpawned = 0;
    private static int lastPanicSpawn = -6767;

    public static int threshForNumRats(int x) {
        return ((x - 1) / 4) * 10 + 10;
    }

    // The map holds at least RICH_MINES mines (util/MineEstimate). Measured: a cap 4 rats lower wins on maps with <= 4
    // mines and loses on maps with >= 10 (docs/plan.md E2; T1§2.5: the army the income sustains grows with the mines).
    // Latched: on a turn short of bytecode the estimate falls back to the known count.
    private static final int RICH_MINES = 5;
    private static boolean rich = false;

    static boolean isRichMap() {
        if (!rich) rich = MineEstimate.atLeast(RICH_MINES) >= RICH_MINES;
        return rich;
    }

    @Override
    public boolean runTick() throws GameActionException {
        // indicate("spawned " + numSpawned);
        int defaultThresh = 40;
        if (mapWidth * mapHeight <= 900 && time < 400) {
            defaultThresh = 30;
        }
        // Measured: the lower cap gains on mine-poor maps up to 1600 tiles; beyond, it gains ~0 there and the late
        // estimate leaks it onto rich maps.
        if (mapWidth * mapHeight <= 1600 && !isRichMap()) {
            defaultThresh -= 10;
        }
        int thresh = Math.min(rc.getGlobalCheese() / 10 + 10, defaultThresh);
        if (rc.getGlobalCheese() >= 3000 - time * 3 / 4) {
            thresh = 1000;
        } else if (rc.getGlobalCheese() >= 2000 - time / 2 && time >= 100) {
            thresh = threshForNumRats(32);
        }

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
        if (target == null) {
            // even out spawning
            if (GlobalArray.ratCounter % GlobalArray.countRatKings() == GlobalArray.getIndexAmongAliveRatKings()) {
                target = new MapLocation(mapWidth / 2, mapHeight / 2);
            }
            
        } else {
            if (time - lastPanicSpawn > 4 && rc.getHealth() < lastHP) {
                // indicate("targeting enemy cluster at " + target + " with " + targetNeighbours + " neighbours");
                thresh += 10;
                if (rc.getHealth() <= 100) {
                    thresh += 10;
                }
                lastPanicSpawn = time;
            }
        }

        if (target != null && rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
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
                    }
                }
            }
        }
        if (best != null) {
            Direction facing = best.directionTo(target);
            if (facing != Direction.CENTER && rc.canTurn()) RatKing.turn(facing);
            rc.buildRat(best);
            RobotTracker.addAlly(best);
            GlobalArray.registerSpawnedRat(best, Rng.mcg() & ((1 << 13) - 1));
            numSpawned++;
            RatKing.spawnedThisTurn = true;
        }
    }
}
