package awubot.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class BetterCombatTracker {
    private static final int GRID_RADIUS = 2;
    private static final int GRID_SIZE = 5;
    private static final int DIR_COUNT = 8;
    private static final int MOVE_COUNT = 9;

    private static final boolean[][] isThreat = new boolean[GRID_SIZE][GRID_SIZE];
    private static final long[] moveThreatMask = new long[MOVE_COUNT];
    private static final long[] lowMask = new long[MOVE_COUNT];
    private static final long[] highMask = new long[MOVE_COUNT];
    private static final long[] hiddenMask = new long[DIR_COUNT];
    private static final long[] dirByteMaskFull = new long[DIR_COUNT];
    private static final long[] dirByteMaskOne = new long[DIR_COUNT];

    static {
        for (int i = 0; i < DIR_COUNT; i++) {
            dirByteMaskFull[i] = 0xFFL << (i * 8);
            dirByteMaskOne[i] = 0x01L << (i * 8);
        }
        for (Direction facing : directions) {
            boolean[] visible = new boolean[DIR_COUNT];
            for (Direction dir : relatedDirections(facing)) {
                visible[dir.ordinal()] = true;
            }
            long mask = 0L;
            for (Direction dir : directions) {
                if (!visible[dir.ordinal()]) {
                    mask |= dirByteMaskFull[dir.ordinal()];
                }
            }
            hiddenMask[facing.ordinal()] = mask;
        }
    }

    public static void debug() {
        StringBuilder sb = new StringBuilder();
        for (int y = GRID_RADIUS; y >= -GRID_RADIUS; y--) {
            for (int x = -GRID_RADIUS; x <= GRID_RADIUS; x++) {
                sb.append(isThreat[toIndex(x)][toIndex(y)] ? "X " : ". ");
            }
            sb.append('\n');
        }
        indicate(sb.toString());
    }

    public static void tick() throws GameActionException {
        loadThreatFromMap();
        for (MapInfo info : rc.senseNearbyMapInfos(8)) {
            if (info.getTrap() == TrapType.NONE) continue;
            MapLocation loc = info.getMapLocation();
            int dx = loc.x - myLocX;
            int dy = loc.y - myLocY;
            if (inGrid(dx, dy)) {
                registerTrap(dx, dy);
            }
        }
        recomputeThreatMasks();
        resetEnemyCounts();
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (info.getType() != UnitType.BABY_RAT) continue;
            registerEnemy(info);
        }
    }

    public static void loadThreatFromMap() throws GameActionException {
        for (int dx = -GRID_RADIUS; dx <= GRID_RADIUS; dx++) {
            for (int dy = -GRID_RADIUS; dy <= GRID_RADIUS; dy++) {
                MapLocation loc = new MapLocation(myLocX + dx, myLocY + dy);
                isThreat[toIndex(dx)][toIndex(dy)] = isCellAThreat(loc);
            }
        }
    }

    public static void registerTrap(int dx, int dy) {
        for (int ox = -1; ox <= 1; ox++) {
            for (int oy = -1; oy <= 1; oy++) {
                int nx = dx + ox;
                int ny = dy + oy;
                if (inGrid(nx, ny)) {
                    isThreat[toIndex(nx)][toIndex(ny)] = false;
                }
            }
        }
    }

    public static void recomputeThreatMasks() {
        for (Direction moveDir : allDirections) {
            int moveIdx = moveDir.ordinal();
            MapLocation moveLoc = myLoc.add(moveDir);
            long mask = 0L;
            for (Direction neighborDir : directions) {
                MapLocation neighbor = moveLoc.add(neighborDir);
                int dx = neighbor.x - myLocX;
                int dy = neighbor.y - myLocY;
                if (inGrid(dx, dy) && isThreat[toIndex(dx)][toIndex(dy)]) {
                    mask |= dirByteMaskFull[neighborDir.ordinal()];
                }
            }
            moveThreatMask[moveIdx] = mask;
        }
    }

    public static void resetEnemyCounts() {
        for (int i = 0; i < MOVE_COUNT; i++) {
            lowMask[i] = 0L;
            highMask[i] = 0L;
        }
    }

    public static void registerEnemy(RobotInfo info) {
        int dx = info.getLocation().x - myLocX;
        int dy = info.getLocation().y - myLocY;
        if (!inGrid(dx, dy)) return;
        boolean highHealth = info.getHealth() > rc.getHealth();
        registerEnemy(dx, dy, info.getDirection(), highHealth);
    }

    public static void registerEnemyLow(int dx, int dy, Direction facing) {
        registerEnemy(dx, dy, facing, false);
    }

    public static void registerEnemyHigh(int dx, int dy, Direction facing) {
        registerEnemy(dx, dy, facing, true);
    }

    private static void registerEnemy(int dx, int dy, Direction facing, boolean highHealth) {
        if (!inGrid(dx, dy)) return;
        MapLocation enemyLoc = new MapLocation(myLocX + dx, myLocY + dy);
        for (Direction moveDir : allDirections) {
            int moveIdx = moveDir.ordinal();
            MapLocation moveLoc = myLoc.add(moveDir);
            if (!rc.onTheMap(moveLoc)) continue;
            if (!canSense(enemyLoc, moveLoc, facing)) continue;
            long addMask = enemyAddMaskForMove(enemyLoc, moveLoc);
            if (addMask == 0L) continue;
            if (highHealth) {
                highMask[moveIdx] += addMask;
            } else {
                lowMask[moveIdx] += addMask;
            }
        }
    }

    public static boolean hasThreat(Direction moveDir, Direction facing) {
        int moveIdx = moveDir.ordinal();
        int faceIdx = facing.ordinal();
        if (faceIdx >= DIR_COUNT) return false;
        long combined = highMask[moveIdx] + (lowMask[moveIdx] & hiddenMask[faceIdx]);
        return (combined & moveThreatMask[moveIdx]) != 0L;
    }

    public static boolean hasThreat(Direction moveDir, Direction facing, RobotInfo target) {
        if (target == null) return hasThreat(moveDir, facing);
        if (target.getTeam() == rc.getTeam()) return hasThreat(moveDir, facing);
        if (target.getType() != UnitType.BABY_RAT) return hasThreat(moveDir, facing);
        int dx = target.getLocation().x - myLocX;
        int dy = target.getLocation().y - myLocY;
        if (!inGrid(dx, dy)) return hasThreat(moveDir, facing);
        int moveIdx = moveDir.ordinal();
        int faceIdx = facing.ordinal();
        if (faceIdx >= DIR_COUNT) return hasThreat(moveDir, facing);
        long low = lowMask[moveIdx];
        long high = highMask[moveIdx];
        boolean highHealth = target.getHealth() > rc.getHealth();
        MapLocation enemyLoc = target.getLocation();
        MapLocation moveLoc = myLoc.add(moveDir);
        if (rc.onTheMap(moveLoc) && canSense(enemyLoc, moveLoc, target.getDirection())) {
            long sub = enemyAddMaskForMove(enemyLoc, moveLoc);
            if (highHealth) {
                high -= sub;
            } else {
                low -= sub;
            }
        }
        long combined = high + (low & hiddenMask[faceIdx]);
        return (combined & moveThreatMask[moveIdx]) != 0L;
    }

    private static long enemyAddMaskForMove(MapLocation enemyLoc, MapLocation moveLoc) {
        long mask = 0L;
        for (Direction neighborDir : directions) {
            MapLocation neighbor = moveLoc.add(neighborDir);
            if (enemyCanReach(enemyLoc, neighbor)) {
                mask |= dirByteMaskOne[neighborDir.ordinal()];
            }
        }
        return mask;
    }

    private static boolean enemyCanReach(MapLocation enemyLoc, MapLocation target) {
        int dx = target.x - enemyLoc.x;
        int dy = target.y - enemyLoc.y;
        int adx = dx < 0 ? -dx : dx;
        int ady = dy < 0 ? -dy : dy;
        return (adx <= 1 && ady <= 1);
    }

    private static boolean inGrid(int dx, int dy) {
        return dx >= -GRID_RADIUS && dx <= GRID_RADIUS && dy >= -GRID_RADIUS && dy <= GRID_RADIUS;
    }

    private static int toIndex(int d) {
        return d + GRID_RADIUS;
    }
}
