package challenger.robot.king;

import battlecode.common.*;
import challenger.robot.Strategy;
import challenger.util.MineEstimate;
import challenger.util.Rng;
import static challenger.Game.*;
import static challenger.util.Util.*;

import challenger.RobotTracker;
import challenger.Tune;
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
        int defaultThresh = Tune.SPAWN_CAP;
        if (mapWidth * mapHeight <= 900 && time < 400) {
            defaultThresh = Tune.SPAWN_CAP_SMALL;
        }
        // Measured: the lower cap gains on mine-poor maps up to 1600 tiles; beyond, it gains ~0 there and the late
        // estimate leaks it onto rich maps.
        if (mapWidth * mapHeight <= 1600 && !isRichMap()) {
            defaultThresh -= Tune.SPAWN_POOR_CUT;
        }
        int thresh = Math.min(rc.getGlobalCheese() / Tune.SPAWN_CHEESE_DIV + 10, defaultThresh);
        if (rc.getGlobalCheese() >= Tune.SPAWN_VRICH - time * 3 / 4) {
            thresh = 1000;
        } else if (rc.getGlobalCheese() >= Tune.SPAWN_RICH - time / 2 && time >= 100) {
            thresh = threshForNumRats(32);
        }

        // A book switch (Tune.PM_SIEGE, E2b's siege arms): a newborn spawns on the king's ring toward the nearest enemy
        // and fights at once, so with an enemy in the king's vision (1), or more enemies than our rats there (2), the
        // king spawns whatever the army size.
        if (Tune.PM_SIEGE != 0 && RobotTracker.numEnemies > (Tune.PM_SIEGE == 1 ? 0 : RobotTracker.numAllies)) {
            thresh = 1000;
        }

        if (RobotTracker.numCats > 0 && RobotTracker.numAllies == 0) {
            thresh += 10;
        }

        // the nearest visible enemy
        MapLocation target = RobotTracker.numEnemies > 0 ? RobotTracker.sortedEnemies[0].getLocation() : null;
        if (target == null) {
            // even out spawning
            if (GlobalArray.ratCounter % GlobalArray.countRatKings() == GlobalArray.getIndexAmongAliveRatKings()) {
                target = new MapLocation(mapWidth / 2, mapHeight / 2);
            }
            
        } else {
            if (time - lastPanicSpawn > 4 && rc.getHealth() < lastHP) {
                thresh += 10;
                if (rc.getHealth() <= 100) {
                    thresh += 10;
                }
                lastPanicSpawn = time;
            }
        }

        if (target != null && rc.getCurrentRatCost() <= thresh && (rc.getGlobalCheese() >= Tune.SPAWN_FLOOR || rc.getCurrentRatCost() == 10)) {
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
