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

public class SlowCombatTracker {
    private static final int GRID_RADIUS = 2;
    private static final int GRID_SIZE = 5;
    private static final int DIR_COUNT = 8;
    private static final int MOVE_COUNT = 9;

    private static final boolean[][] isThreat = new boolean[GRID_SIZE][GRID_SIZE];
    private static final boolean[][] moveThreat = new boolean[MOVE_COUNT][DIR_COUNT];
    private static final int[][] lowCount = new int[MOVE_COUNT][DIR_COUNT];
    private static final int[][] highCount = new int[MOVE_COUNT][DIR_COUNT];
    private static final boolean[][] hidden = new boolean[DIR_COUNT][DIR_COUNT];

    static {
        for (Direction facing : directions) {
            boolean[] visible = new boolean[DIR_COUNT];
            for (Direction dir : relatedDirections(facing)) {
                visible[dir.ordinal()] = true;
            }
            for (Direction dir : directions) {
                hidden[facing.ordinal()][dir.ordinal()] = !visible[dir.ordinal()];
            }
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
            for (Direction neighborDir : directions) {
                MapLocation neighbor = moveLoc.add(neighborDir);
                int dx = neighbor.x - myLocX;
                int dy = neighbor.y - myLocY;
                if (inGrid(dx, dy)) {
                    moveThreat[moveIdx][neighborDir.ordinal()] = isThreat[toIndex(dx)][toIndex(dy)];
                } else {
                    moveThreat[moveIdx][neighborDir.ordinal()] = false;
                }
            }
        }
    }

    public static void resetEnemyCounts() {
        for (int i = 0; i < MOVE_COUNT; i++) {
            for (int j = 0; j < DIR_COUNT; j++) {
                lowCount[i][j] = 0;
                highCount[i][j] = 0;
            }
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
            MapLocation moveLoc = myLoc.add(moveDir);
            if (!rc.onTheMap(moveLoc)) continue;
            if (!canSense(enemyLoc, moveLoc, facing)) continue;
            int moveIdx = moveDir.ordinal();
            for (Direction neighborDir : directions) {
                MapLocation neighbor = moveLoc.add(neighborDir);
                if (enemyCanReach(enemyLoc, neighbor)) {
                    int idx = neighborDir.ordinal();
                    if (highHealth) {
                        highCount[moveIdx][idx]++;
                    } else {
                        lowCount[moveIdx][idx]++;
                    }
                }
            }
        }
    }

    public static boolean hasThreat(Direction moveDir, Direction facing) {
        int moveIdx = moveDir.ordinal();
        int faceIdx = facing.ordinal();
        if (faceIdx >= DIR_COUNT) return false;
        for (int i = 0; i < DIR_COUNT; i++) {
            if (!moveThreat[moveIdx][i]) continue;
            if (highCount[moveIdx][i] > 0) return true;
            if (lowCount[moveIdx][i] > 0 && hidden[faceIdx][i]) return true;
        }
        return false;
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
        boolean highHealth = target.getHealth() > rc.getHealth();
        MapLocation enemyLoc = target.getLocation();
        MapLocation moveLoc = myLoc.add(moveDir);
        boolean enemySeesMove = rc.onTheMap(moveLoc) && canSense(enemyLoc, moveLoc, target.getDirection());
        for (Direction neighborDir : directions) {
            int idx = neighborDir.ordinal();
            if (!moveThreat[moveIdx][idx]) continue;
            int high = highCount[moveIdx][idx];
            int low = lowCount[moveIdx][idx];
            if (enemySeesMove && enemyCanReach(enemyLoc, moveLoc.add(neighborDir))) {
                if (highHealth) {
                    high--;
                } else {
                    low--;
                }
            }
            if (high > 0) return true;
            if (low > 0 && hidden[faceIdx][idx]) return true;
        }
        return false;
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
