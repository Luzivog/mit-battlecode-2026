package challenger.robot.king;

import battlecode.common.*;
import challenger.robot.Strategy;
import challenger.util.Rng;
import static challenger.Game.*;
import static challenger.util.Util.*;

import challenger.CellTracker;
import challenger.RobotTracker;
import challenger.comms.GlobalArray;
import challenger.util.string.FastIndexedIntSet;

public class KingSpawningStrategy extends Strategy {
    private static int numSpawned = 0;
    private static int lastPanicSpawn = -6767;

    public static int threshForNumRats(int x) {
        return ((x - 1) / 4) * 10 + 10;
    }

    // The map holds at least RICH_MINES mines: the known ones plus their unknown mirrors. Income is about 1.1 per
    // mine per side (docs/theory/t1-economy.md §2.1), so below it the army the income sustains is under 16 rats
    // (§2.5). Latched: mines are never forgotten.
    private static final int RICH_MINES = 8;
    private static boolean rich = false;
    private static int richKey = -1;

    static boolean isRichMap() {
        if (rich) return true;
        FastIndexedIntSet mines = GlobalArray.globalCheeseMines;
        int n = mines.size();
        boolean sym = CellTracker.isSymmetryKnown();
        int key = sym ? n + 1024 : n;
        if (key == richKey) return false;
        richKey = key;
        int est = n;
        if (sym) {
            // encode2x2 halves coordinates, so on an odd dimension the mirror of an odd coordinate lands one cell high
            int sym3 = CellTracker.getSymmetry().ordinal();
            boolean oddX = sym3 != 3 && (mapWidth & 1) == 1;
            boolean oddY = sym3 != 2 && (mapHeight & 1) == 1;
            for (int i = 0; i < n && est < RICH_MINES; i++) {
                int s = getSymmetricEncoded(mines.at(i));
                if (mines.contains(s)) continue;
                if (oddX && mines.contains(s - 1)) continue;
                if (oddY && mines.contains(s - 32)) continue;
                if (oddX && oddY && mines.contains(s - 33)) continue;
                est++;
            }
        }
        rich = est >= RICH_MINES;
        return rich;
    }

    @Override
    public boolean runTick() throws GameActionException {
        // indicate("spawned " + numSpawned);
        int defaultThresh = 40;
        if (mapWidth * mapHeight <= 900 && time < 400) {
            defaultThresh = 30;
        }
        if (!isRichMap()) {
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
