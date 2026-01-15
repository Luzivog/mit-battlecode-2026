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
import awubot.util.Profiler;
import awubot.CellTracker;

public class CombatTracker {
    private static final int GRID_RADIUS = 2;







    // isThreat flags for the 5x5 around the player.
    private static boolean t_m2_2;
    private static boolean t_m1_2;
    private static boolean t_0_2;
    private static boolean t_1_2;
    private static boolean t_2_2;
    private static boolean t_m2_1;
    private static boolean t_m1_1;
    private static boolean t_0_1;
    private static boolean t_1_1;
    private static boolean t_2_1;
    private static boolean t_m2_0;
    private static boolean t_m1_0;
    private static boolean t_0_0;
    private static boolean t_1_0;
    private static boolean t_2_0;
    private static boolean t_m2_m1;
    private static boolean t_m1_m1;
    private static boolean t_0_m1;
    private static boolean t_1_m1;
    private static boolean t_2_m1;
    private static boolean t_m2_m2;
    private static boolean t_m1_m2;
    private static boolean t_0_m2;
    private static boolean t_1_m2;
    private static boolean t_2_m2;

    // Byte-packed enemy counters (low/high health) per move direction.
    private static long lowN;
    private static long lowNE;
    private static long lowE;
    private static long lowSE;
    private static long lowS;
    private static long lowSW;
    private static long lowW;
    private static long lowNW;
    private static long lowC;
    private static long highN;
    private static long highNE;
    private static long highE;
    private static long highSE;
    private static long highS;
    private static long highSW;
    private static long highW;
    private static long highNW;
    private static long highC;

    // Byte-packed threat masks per move direction (0xFF per reachable neighbor).
    private static long moveThreatN;
    private static long moveThreatNE;
    private static long moveThreatE;
    private static long moveThreatSE;
    private static long moveThreatS;
    private static long moveThreatSW;
    private static long moveThreatW;
    private static long moveThreatNW;
    private static long moveThreatC;

    // Hidden-neighbor masks per facing (0xFF for hidden, 0x00 for visible).
private static final long hiddenN = 0x00ffffffffff0000L;
private static final long hiddenNE = 0xffffffffff000000L;
private static final long hiddenE = 0xffffffff000000ffL;
private static final long hiddenSE = 0xffffff000000ffffL;
private static final long hiddenS = 0xffff000000ffffffL;
private static final long hiddenSW = 0xff000000ffffffffL;
private static final long hiddenW = 0x000000ffffffffffL;
private static final long hiddenNW = 0x0000ffffffffff00L;

    public static void debug() {
        StringBuilder sb = new StringBuilder();
        for (int y = GRID_RADIUS; y >= -GRID_RADIUS; y--) {
            for (int x = -GRID_RADIUS; x <= GRID_RADIUS; x++) {
                sb.append(isThreatAt(x, y) ? "X " : ". ");
            }
            sb.append('\n');
        }
        indicate(sb.toString());
    }

    public static void tick() throws GameActionException {
        // Profiler.push();
        loadThreatFromMap();
        // Profiler.pop("loading threats");

        // Profiler.push();
        for (MapInfo info : rc.senseNearbyMapInfos(8)) {
            if (info.getTrap() == TrapType.NONE) continue;
            MapLocation loc = info.getMapLocation();
            int dx = loc.x - myLocX;
            int dy = loc.y - myLocY;
            if (dx < -2 || dx > 2 || dy < -2 || dy > 2) continue;
            registerTrap(dx, dy);
        }
        // Profiler.pop("registering traps");
        
        // Profiler.push();
        recomputeThreatMasks();
        // Profiler.pop("recomputing threat masks");

        resetEnemyCounts();
        // Profiler.push();
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (info.getType() != UnitType.BABY_RAT) continue;
            registerEnemy(info);
        }
        // Profiler.pop("registering enemies");
    }

    public static void resetEnemyCounts() {
        lowN = 0L;
        lowNE = 0L;
        lowE = 0L;
        lowSE = 0L;
        lowS = 0L;
        lowSW = 0L;
        lowW = 0L;
        lowNW = 0L;
        lowC = 0L;
        highN = 0L;
        highNE = 0L;
        highE = 0L;
        highSE = 0L;
        highS = 0L;
        highSW = 0L;
        highW = 0L;
        highNW = 0L;
        highC = 0L;
    }

    public static void loadThreatFromMap() throws GameActionException {
        int x = myLoc.x;
        int y = myLoc.y;
        {
            MapLocation loc = new MapLocation(x  + -2 , y  + 2 );
            if (CellTracker.isSeen(loc)) {
                t_m2_2 = CellTracker.isPassable(loc);
            } else {
                t_m2_2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -1 , y  + 2 );
            if (CellTracker.isSeen(loc)) {
                t_m1_2 = CellTracker.isPassable(loc);
            } else {
                t_m1_2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x , y  + 2 );
            if (CellTracker.isSeen(loc)) {
                t_0_2 = CellTracker.isPassable(loc);
            } else {
                t_0_2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 1 , y  + 2 );
            if (CellTracker.isSeen(loc)) {
                t_1_2 = CellTracker.isPassable(loc);
            } else {
                t_1_2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 2 , y  + 2 );
            if (CellTracker.isSeen(loc)) {
                t_2_2 = CellTracker.isPassable(loc);
            } else {
                t_2_2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -2 , y  + 1 );
            if (CellTracker.isSeen(loc)) {
                t_m2_1 = CellTracker.isPassable(loc);
            } else {
                t_m2_1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -1 , y  + 1 );
            if (CellTracker.isSeen(loc)) {
                t_m1_1 = CellTracker.isPassable(loc);
            } else {
                t_m1_1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x , y  + 1 );
            if (CellTracker.isSeen(loc)) {
                t_0_1 = CellTracker.isPassable(loc);
            } else {
                t_0_1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 1 , y  + 1 );
            if (CellTracker.isSeen(loc)) {
                t_1_1 = CellTracker.isPassable(loc);
            } else {
                t_1_1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 2 , y  + 1 );
            if (CellTracker.isSeen(loc)) {
                t_2_1 = CellTracker.isPassable(loc);
            } else {
                t_2_1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -2 , y );
            if (CellTracker.isSeen(loc)) {
                t_m2_0 = CellTracker.isPassable(loc);
            } else {
                t_m2_0 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -1 , y );
            if (CellTracker.isSeen(loc)) {
                t_m1_0 = CellTracker.isPassable(loc);
            } else {
                t_m1_0 = false;
            }
        }        t_0_0 = true;        {
            MapLocation loc = new MapLocation(x  + 1 , y );
            if (CellTracker.isSeen(loc)) {
                t_1_0 = CellTracker.isPassable(loc);
            } else {
                t_1_0 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 2 , y );
            if (CellTracker.isSeen(loc)) {
                t_2_0 = CellTracker.isPassable(loc);
            } else {
                t_2_0 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -2 , y  + -1 );
            if (CellTracker.isSeen(loc)) {
                t_m2_m1 = CellTracker.isPassable(loc);
            } else {
                t_m2_m1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -1 , y  + -1 );
            if (CellTracker.isSeen(loc)) {
                t_m1_m1 = CellTracker.isPassable(loc);
            } else {
                t_m1_m1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x , y  + -1 );
            if (CellTracker.isSeen(loc)) {
                t_0_m1 = CellTracker.isPassable(loc);
            } else {
                t_0_m1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 1 , y  + -1 );
            if (CellTracker.isSeen(loc)) {
                t_1_m1 = CellTracker.isPassable(loc);
            } else {
                t_1_m1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 2 , y  + -1 );
            if (CellTracker.isSeen(loc)) {
                t_2_m1 = CellTracker.isPassable(loc);
            } else {
                t_2_m1 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -2 , y  + -2 );
            if (CellTracker.isSeen(loc)) {
                t_m2_m2 = CellTracker.isPassable(loc);
            } else {
                t_m2_m2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + -1 , y  + -2 );
            if (CellTracker.isSeen(loc)) {
                t_m1_m2 = CellTracker.isPassable(loc);
            } else {
                t_m1_m2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x , y  + -2 );
            if (CellTracker.isSeen(loc)) {
                t_0_m2 = CellTracker.isPassable(loc);
            } else {
                t_0_m2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 1 , y  + -2 );
            if (CellTracker.isSeen(loc)) {
                t_1_m2 = CellTracker.isPassable(loc);
            } else {
                t_1_m2 = false;
            }
        }        {
            MapLocation loc = new MapLocation(x  + 2 , y  + -2 );
            if (CellTracker.isSeen(loc)) {
                t_2_m2 = CellTracker.isPassable(loc);
            } else {
                t_2_m2 = false;
            }
        }        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getType() != UnitType.BABY_RAT) continue;
            int dx = info.getLocation().x - x;
            int dy = info.getLocation().y - y;
            switch (dx) {
                case -2 -> {
                    switch (dy) {
                        case -2 -> {                            t_m2_m2 = info.getTeam() != team;                        }
                        case -1 -> {                            t_m2_m1 = info.getTeam() != team;                        }
                        case 0 -> {                            t_m2_0 = info.getTeam() != team;                        }
                        case 1 -> {                            t_m2_1 = info.getTeam() != team;                        }
                        case 2 -> {                            t_m2_2 = info.getTeam() != team;                        }
                        default -> { }
                    }
                }
                case -1 -> {
                    switch (dy) {
                        case -2 -> {                            t_m1_m2 = info.getTeam() != team;                        }
                        case -1 -> {                            t_m1_m1 = info.getTeam() != team;                        }
                        case 0 -> {                            t_m1_0 = info.getTeam() != team;                        }
                        case 1 -> {                            t_m1_1 = info.getTeam() != team;                        }
                        case 2 -> {                            t_m1_2 = info.getTeam() != team;                        }
                        default -> { }
                    }
                }
                case 0 -> {
                    switch (dy) {
                        case -2 -> {                            t_0_m2 = info.getTeam() != team;                        }
                        case -1 -> {                            t_0_m1 = info.getTeam() != team;                        }
                        case 0 -> {                        }
                        case 1 -> {                            t_0_1 = info.getTeam() != team;                        }
                        case 2 -> {                            t_0_2 = info.getTeam() != team;                        }
                        default -> { }
                    }
                }
                case 1 -> {
                    switch (dy) {
                        case -2 -> {                            t_1_m2 = info.getTeam() != team;                        }
                        case -1 -> {                            t_1_m1 = info.getTeam() != team;                        }
                        case 0 -> {                            t_1_0 = info.getTeam() != team;                        }
                        case 1 -> {                            t_1_1 = info.getTeam() != team;                        }
                        case 2 -> {                            t_1_2 = info.getTeam() != team;                        }
                        default -> { }
                    }
                }
                case 2 -> {
                    switch (dy) {
                        case -2 -> {                            t_2_m2 = info.getTeam() != team;                        }
                        case -1 -> {                            t_2_m1 = info.getTeam() != team;                        }
                        case 0 -> {                            t_2_0 = info.getTeam() != team;                        }
                        case 1 -> {                            t_2_1 = info.getTeam() != team;                        }
                        case 2 -> {                            t_2_2 = info.getTeam() != team;                        }
                        default -> { }
                    }
                }
                default -> { }
            }
        }
    }

    public static void registerTrap(int dx, int dy) {
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> {
                        t_m2_m2 = false;
                        t_m2_m1 = false;
                        t_m1_m2 = false;
                        t_m1_m1 = false;
                    }
                    case -1 -> {
                        t_m2_m2 = false;
                        t_m2_m1 = false;
                        t_m2_0 = false;
                        t_m1_m2 = false;
                        t_m1_m1 = false;
                        t_m1_0 = false;
                    }
                    case 0 -> {
                        t_m2_m1 = false;
                        t_m2_0 = false;
                        t_m2_1 = false;
                        t_m1_m1 = false;
                        t_m1_0 = false;
                        t_m1_1 = false;
                    }
                    case 1 -> {
                        t_m2_0 = false;
                        t_m2_1 = false;
                        t_m2_2 = false;
                        t_m1_0 = false;
                        t_m1_1 = false;
                        t_m1_2 = false;
                    }
                    case 2 -> {
                        t_m2_1 = false;
                        t_m2_2 = false;
                        t_m1_1 = false;
                        t_m1_2 = false;
                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -2 -> {
                        t_m2_m2 = false;
                        t_m2_m1 = false;
                        t_m1_m2 = false;
                        t_m1_m1 = false;
                        t_0_m2 = false;
                        t_0_m1 = false;
                    }
                    case -1 -> {
                        t_m2_m2 = false;
                        t_m2_m1 = false;
                        t_m2_0 = false;
                        t_m1_m2 = false;
                        t_m1_m1 = false;
                        t_m1_0 = false;
                        t_0_m2 = false;
                        t_0_m1 = false;
                        t_0_0 = false;
                    }
                    case 0 -> {
                        t_m2_m1 = false;
                        t_m2_0 = false;
                        t_m2_1 = false;
                        t_m1_m1 = false;
                        t_m1_0 = false;
                        t_m1_1 = false;
                        t_0_m1 = false;
                        t_0_0 = false;
                        t_0_1 = false;
                    }
                    case 1 -> {
                        t_m2_0 = false;
                        t_m2_1 = false;
                        t_m2_2 = false;
                        t_m1_0 = false;
                        t_m1_1 = false;
                        t_m1_2 = false;
                        t_0_0 = false;
                        t_0_1 = false;
                        t_0_2 = false;
                    }
                    case 2 -> {
                        t_m2_1 = false;
                        t_m2_2 = false;
                        t_m1_1 = false;
                        t_m1_2 = false;
                        t_0_1 = false;
                        t_0_2 = false;
                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -2 -> {
                        t_m1_m2 = false;
                        t_m1_m1 = false;
                        t_0_m2 = false;
                        t_0_m1 = false;
                        t_1_m2 = false;
                        t_1_m1 = false;
                    }
                    case -1 -> {
                        t_m1_m2 = false;
                        t_m1_m1 = false;
                        t_m1_0 = false;
                        t_0_m2 = false;
                        t_0_m1 = false;
                        t_0_0 = false;
                        t_1_m2 = false;
                        t_1_m1 = false;
                        t_1_0 = false;
                    }
                    case 0 -> {
                        t_m1_m1 = false;
                        t_m1_0 = false;
                        t_m1_1 = false;
                        t_0_m1 = false;
                        t_0_0 = false;
                        t_0_1 = false;
                        t_1_m1 = false;
                        t_1_0 = false;
                        t_1_1 = false;
                    }
                    case 1 -> {
                        t_m1_0 = false;
                        t_m1_1 = false;
                        t_m1_2 = false;
                        t_0_0 = false;
                        t_0_1 = false;
                        t_0_2 = false;
                        t_1_0 = false;
                        t_1_1 = false;
                        t_1_2 = false;
                    }
                    case 2 -> {
                        t_m1_1 = false;
                        t_m1_2 = false;
                        t_0_1 = false;
                        t_0_2 = false;
                        t_1_1 = false;
                        t_1_2 = false;
                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -2 -> {
                        t_0_m2 = false;
                        t_0_m1 = false;
                        t_1_m2 = false;
                        t_1_m1 = false;
                        t_2_m2 = false;
                        t_2_m1 = false;
                    }
                    case -1 -> {
                        t_0_m2 = false;
                        t_0_m1 = false;
                        t_0_0 = false;
                        t_1_m2 = false;
                        t_1_m1 = false;
                        t_1_0 = false;
                        t_2_m2 = false;
                        t_2_m1 = false;
                        t_2_0 = false;
                    }
                    case 0 -> {
                        t_0_m1 = false;
                        t_0_0 = false;
                        t_0_1 = false;
                        t_1_m1 = false;
                        t_1_0 = false;
                        t_1_1 = false;
                        t_2_m1 = false;
                        t_2_0 = false;
                        t_2_1 = false;
                    }
                    case 1 -> {
                        t_0_0 = false;
                        t_0_1 = false;
                        t_0_2 = false;
                        t_1_0 = false;
                        t_1_1 = false;
                        t_1_2 = false;
                        t_2_0 = false;
                        t_2_1 = false;
                        t_2_2 = false;
                    }
                    case 2 -> {
                        t_0_1 = false;
                        t_0_2 = false;
                        t_1_1 = false;
                        t_1_2 = false;
                        t_2_1 = false;
                        t_2_2 = false;
                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -2 -> {
                        t_1_m2 = false;
                        t_1_m1 = false;
                        t_2_m2 = false;
                        t_2_m1 = false;
                    }
                    case -1 -> {
                        t_1_m2 = false;
                        t_1_m1 = false;
                        t_1_0 = false;
                        t_2_m2 = false;
                        t_2_m1 = false;
                        t_2_0 = false;
                    }
                    case 0 -> {
                        t_1_m1 = false;
                        t_1_0 = false;
                        t_1_1 = false;
                        t_2_m1 = false;
                        t_2_0 = false;
                        t_2_1 = false;
                    }
                    case 1 -> {
                        t_1_0 = false;
                        t_1_1 = false;
                        t_1_2 = false;
                        t_2_0 = false;
                        t_2_1 = false;
                        t_2_2 = false;
                    }
                    case 2 -> {
                        t_1_1 = false;
                        t_1_2 = false;
                        t_2_1 = false;
                        t_2_2 = false;
                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    public static void recomputeThreatMasks() {
moveThreatN = (t_0_2 ? 0x00000000000000ffL : 0L) | (t_1_2 ? 0x000000000000ff00L : 0L) | (t_1_1 ? 0x0000000000ff0000L : 0L) | (t_1_0 ? 0x00000000ff000000L : 0L) | (t_0_0 ? 0x000000ff00000000L : 0L) | (t_m1_0 ? 0x0000ff0000000000L : 0L) | (t_m1_1 ? 0x00ff000000000000L : 0L) | (t_m1_2 ? 0xff00000000000000L : 0L);
moveThreatNE = (t_1_2 ? 0x00000000000000ffL : 0L) | (t_2_2 ? 0x000000000000ff00L : 0L) | (t_2_1 ? 0x0000000000ff0000L : 0L) | (t_2_0 ? 0x00000000ff000000L : 0L) | (t_1_0 ? 0x000000ff00000000L : 0L) | (t_0_0 ? 0x0000ff0000000000L : 0L) | (t_0_1 ? 0x00ff000000000000L : 0L) | (t_0_2 ? 0xff00000000000000L : 0L);
moveThreatE = (t_1_1 ? 0x00000000000000ffL : 0L) | (t_2_1 ? 0x000000000000ff00L : 0L) | (t_2_0 ? 0x0000000000ff0000L : 0L) | (t_2_m1 ? 0x00000000ff000000L : 0L) | (t_1_m1 ? 0x000000ff00000000L : 0L) | (t_0_m1 ? 0x0000ff0000000000L : 0L) | (t_0_0 ? 0x00ff000000000000L : 0L) | (t_0_1 ? 0xff00000000000000L : 0L);
moveThreatSE = (t_1_0 ? 0x00000000000000ffL : 0L) | (t_2_0 ? 0x000000000000ff00L : 0L) | (t_2_m1 ? 0x0000000000ff0000L : 0L) | (t_2_m2 ? 0x00000000ff000000L : 0L) | (t_1_m2 ? 0x000000ff00000000L : 0L) | (t_0_m2 ? 0x0000ff0000000000L : 0L) | (t_0_m1 ? 0x00ff000000000000L : 0L) | (t_0_0 ? 0xff00000000000000L : 0L);
moveThreatS = (t_0_0 ? 0x00000000000000ffL : 0L) | (t_1_0 ? 0x000000000000ff00L : 0L) | (t_1_m1 ? 0x0000000000ff0000L : 0L) | (t_1_m2 ? 0x00000000ff000000L : 0L) | (t_0_m2 ? 0x000000ff00000000L : 0L) | (t_m1_m2 ? 0x0000ff0000000000L : 0L) | (t_m1_m1 ? 0x00ff000000000000L : 0L) | (t_m1_0 ? 0xff00000000000000L : 0L);
moveThreatSW = (t_m1_0 ? 0x00000000000000ffL : 0L) | (t_0_0 ? 0x000000000000ff00L : 0L) | (t_0_m1 ? 0x0000000000ff0000L : 0L) | (t_0_m2 ? 0x00000000ff000000L : 0L) | (t_m1_m2 ? 0x000000ff00000000L : 0L) | (t_m2_m2 ? 0x0000ff0000000000L : 0L) | (t_m2_m1 ? 0x00ff000000000000L : 0L) | (t_m2_0 ? 0xff00000000000000L : 0L);
moveThreatW = (t_m1_1 ? 0x00000000000000ffL : 0L) | (t_0_1 ? 0x000000000000ff00L : 0L) | (t_0_0 ? 0x0000000000ff0000L : 0L) | (t_0_m1 ? 0x00000000ff000000L : 0L) | (t_m1_m1 ? 0x000000ff00000000L : 0L) | (t_m2_m1 ? 0x0000ff0000000000L : 0L) | (t_m2_0 ? 0x00ff000000000000L : 0L) | (t_m2_1 ? 0xff00000000000000L : 0L);
moveThreatNW = (t_m1_2 ? 0x00000000000000ffL : 0L) | (t_0_2 ? 0x000000000000ff00L : 0L) | (t_0_1 ? 0x0000000000ff0000L : 0L) | (t_0_0 ? 0x00000000ff000000L : 0L) | (t_m1_0 ? 0x000000ff00000000L : 0L) | (t_m2_0 ? 0x0000ff0000000000L : 0L) | (t_m2_1 ? 0x00ff000000000000L : 0L) | (t_m2_2 ? 0xff00000000000000L : 0L);
moveThreatC = (t_0_1 ? 0x00000000000000ffL : 0L) | (t_1_1 ? 0x000000000000ff00L : 0L) | (t_1_0 ? 0x0000000000ff0000L : 0L) | (t_1_m1 ? 0x00000000ff000000L : 0L) | (t_0_m1 ? 0x000000ff00000000L : 0L) | (t_m1_m1 ? 0x0000ff0000000000L : 0L) | (t_m1_0 ? 0x00ff000000000000L : 0L) | (t_m1_1 ? 0xff00000000000000L : 0L);
    }

    public static void registerEnemy(RobotInfo info) {
        int dx = info.getLocation().x - myLocX;
        int dy = info.getLocation().y - myLocY;
        if (dx < -2 || dx > 2 || dy < -2 || dy > 2) return;
        boolean highHealth = info.getHealth() > rc.getHealth();
        if (highHealth) {
            registerEnemyHigh(dx, dy, info.getDirection());
        } else {
            registerEnemyLow(dx, dy, info.getDirection());
        }
    }

    public static void registerEnemyLow(int dx, int dy, Direction facing) {
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010100000000L;
                                lowW += 0x0000010100000000L;
                                
                                lowC += 0x0000010000000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                lowS += 0x0001010000000000L;
                                lowSW += 0x0001010100000000L;
                                lowW += 0x0000010100000000L;
                                
                                lowC += 0x0000010000000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                
                                lowS += 0x0001010000000000L;
                                lowSW += 0x0001010100000000L;
                                
                                
                                lowC += 0x0000010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0001010100000000L;
                                lowNW += 0x0000010100000000L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                lowW += 0x0001010100000000L;
                                lowNW += 0x0000010100000000L;
                                lowC += 0x0001010000000000L;
                            }
                            case 2 -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                lowW += 0x0001010100000000L;
                                
                                lowC += 0x0001010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0001010100000000L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0001010000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0101010100000001L;
                                lowNW += 0x0001010100000000L;
                                lowC += 0x0101010000000000L;
                            }
                            case 2 -> {
                                lowN += 0x0001010000000000L;
                                
                                
                                
                                lowS += 0x0101000000000000L;
                                lowSW += 0x0101000000000001L;
                                lowW += 0x0101010100000001L;
                                lowNW += 0x0001010100000000L;
                                lowC += 0x0101010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                lowS += 0x0101000000000000L;
                                lowSW += 0x0101000000000001L;
                                lowW += 0x0101010100000001L;
                                
                                lowC += 0x0101010000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000000001L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010100000001L;
                                
                            }
                            case 2 -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                
                                lowW += 0x0101000000000001L;
                                lowNW += 0x0101010100000001L;
                                lowC += 0x0101000000000000L;
                            }
                            case 3 -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                lowSW += 0x0100000000000001L;
                                lowW += 0x0101000000000001L;
                                lowNW += 0x0101010100000001L;
                                lowC += 0x0101000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                lowSW += 0x0100000000000001L;
                                lowW += 0x0101000000000001L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                lowN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case 3 -> {
                                lowN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000001L;
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000001L;
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                
                                lowS += 0x0001010100000000L;
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                lowC += 0x0000010100000000L;
                            }
                            case 1 -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                lowSE += 0x0001010000000000L;
                                lowS += 0x0001010100000000L;
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                lowC += 0x0000010100000000L;
                            }
                            case 2 -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                lowSE += 0x0001010000000000L;
                                lowS += 0x0001010100000000L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000010100000000L;
                                lowNE += 0x0000010000000000L;
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                lowC += 0x0001010100000000L;
                            }
                            case 1 -> {
                                lowN += 0x0000010100000000L;
                                lowNE += 0x0000010000000000L;
                                lowE += 0x0001010000000000L;
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                lowC += 0x0001010100000000L;
                            }
                            case 2 -> {
                                
                                lowNE += 0x0000010000000000L;
                                lowE += 0x0001010000000000L;
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                
                                
                                lowC += 0x0001010100000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0001010100000000L;
                                lowNE += 0x0001010000000000L;
                                lowE += 0x0101010000000000L;
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                lowC += 0x0101010100000001L;
                            }
                            case 2 -> {
                                lowN += 0x0001010100000000L;
                                lowNE += 0x0001010000000000L;
                                lowE += 0x0101010000000000L;
                                lowSE += 0x0101000000000000L;
                                lowS += 0x0101000000000001L;
                                
                                lowW += 0x0101010101010101L;
                                
                                lowC += 0x0101010100000001L;
                            }
                            case 3 -> {
                                
                                
                                lowE += 0x0101010000000000L;
                                lowSE += 0x0101000000000000L;
                                lowS += 0x0101000000000001L;
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                lowC += 0x0101010100000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                lowS += 0x0101000000000001L;
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 1 -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 2 -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                lowE += 0x0101000000000000L;
                                lowSE += 0x0100000000000000L;
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case 3 -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                lowE += 0x0101000000000000L;
                                lowSE += 0x0100000000000000L;
                                lowS += 0x0100000000000001L;
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0100000000000000L;
                                lowS += 0x0100000000000001L;
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                lowN += 0x0101000000000001L;
                                lowNE += 0x0101000000000000L;
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0101000000000001L;
                                lowNE += 0x0101000000000000L;
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                lowC += 0x0100000000000001L;
                            }
                            case 4 -> {
                                lowN += 0x0101000000000001L;
                                
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                lowC += 0x0100000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000010100000000L;
                                lowSE += 0x0001010100000000L;
                                lowS += 0x0001010101010000L;
                                lowSW += 0x0000000101010000L;
                                lowW += 0x0000000101000000L;
                                
                                lowC += 0x0000010101000000L;
                            }
                            case 1 -> {
                                
                                
                                lowE += 0x0000010100000000L;
                                lowSE += 0x0001010100000000L;
                                lowS += 0x0001010101010000L;
                                
                                
                                
                                lowC += 0x0000010101000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                lowSE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000000101010000L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                lowS += 0x0001010101010000L;
                                lowSW += 0x0000000101010000L;
                                lowW += 0x0000000101000000L;
                                
                                lowC += 0x0000010101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000010101000000L;
                                lowNE += 0x0000010100000000L;
                                lowE += 0x0001010100000000L;
                                
                                lowS += 0x0101010101010101L;
                                
                                lowW += 0x0000000101010000L;
                                lowNW += 0x0000000101000000L;
                                lowC += 0x0001010101010000L;
                            }
                            case 1 -> {
                                lowN += 0x0000010101000000L;
                                lowNE += 0x0000010100000000L;
                                lowE += 0x0001010100000000L;
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                lowC += 0x0001010101010000L;
                            }
                            case 2 -> {
                                
                                
                                lowE += 0x0001010100000000L;
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                lowW += 0x0000000101010000L;
                                
                                
                            }
                            case 7 -> {
                                lowN += 0x0000010101000000L;
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                lowW += 0x0000000101010000L;
                                lowNW += 0x0000000101000000L;
                                lowC += 0x0001010101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0001010101010000L;
                                lowNE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            case 1 -> {
                                lowN += 0x0001010101010000L;
                                lowNE += 0x0001010100000000L;
                                lowE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 2 -> {
                                
                                lowNE += 0x0001010100000000L;
                                lowE += 0x0101010100000001L;
                                lowSE += 0x0101000000000001L;
                                
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 3 -> {
                                
                                
                                lowE += 0x0101010100000001L;
                                lowSE += 0x0101000000000001L;
                                lowS += 0x0101000000010101L;
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0101000000000001L;
                                lowS += 0x0101000000010101L;
                                lowSW += 0x0000000000010101L;
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                lowS += 0x0101000000010101L;
                                lowSW += 0x0000000000010101L;
                                lowW += 0x0000000101010101L;
                                
                                lowC += 0x0101010101010101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000000000010101L;
                                lowW += 0x0000000101010101L;
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            case 7 -> {
                                lowN += 0x0001010101010000L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000101010101L;
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                lowE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                lowE += 0x0101000000000001L;
                                lowSE += 0x0100000000000001L;
                                lowS += 0x0100000000000101L;
                                
                                
                                
                                lowC += 0x0101000000010101L;
                            }
                            case 4 -> {
                                lowN += 0x0101010101010101L;
                                
                                lowE += 0x0101000000000001L;
                                lowSE += 0x0100000000000001L;
                                lowS += 0x0100000000000101L;
                                lowSW += 0x0000000000000101L;
                                lowW += 0x0000000000010101L;
                                
                                lowC += 0x0101000000010101L;
                            }
                            case 5 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                lowS += 0x0100000000000101L;
                                lowSW += 0x0000000000000101L;
                                lowW += 0x0000000000010101L;
                                lowNW += 0x0000000101010101L;
                                lowC += 0x0101000000010101L;
                            }
                            case 6 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000010101L;
                                lowNW += 0x0000000101010101L;
                                
                            }
                            case 7 -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                lowNE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                lowN += 0x0101000000010101L;
                                lowNE += 0x0101000000000001L;
                                lowE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                lowC += 0x0100000000000101L;
                            }
                            case 4 -> {
                                lowN += 0x0101000000010101L;
                                lowNE += 0x0101000000000001L;
                                lowE += 0x0100000000000001L;
                                
                                
                                
                                lowW += 0x0000000000000101L;
                                lowNW += 0x0000000000010101L;
                                lowC += 0x0100000000000101L;
                            }
                            case 5 -> {
                                lowN += 0x0101000000010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000000101L;
                                lowNW += 0x0000000000010101L;
                                lowC += 0x0100000000000101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                lowS += 0x0000000101010000L;
                                
                                lowW += 0x0000000001000000L;
                                
                                lowC += 0x0000000101000000L;
                            }
                            case 1 -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                lowS += 0x0000000101010000L;
                                lowSW += 0x0000000001010000L;
                                lowW += 0x0000000001000000L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                lowS += 0x0000000101010000L;
                                lowSW += 0x0000000001010000L;
                                lowW += 0x0000000001000000L;
                                
                                lowC += 0x0000000101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000000101000000L;
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            case 1 -> {
                                
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                lowW += 0x0000000001010000L;
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            case 7 -> {
                                lowN += 0x0000000101000000L;
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                lowW += 0x0000000001010000L;
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000000101010000L;
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                lowS += 0x0000000000010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                lowS += 0x0000000000010101L;
                                lowSW += 0x0000000000010100L;
                                lowW += 0x0000000001010100L;
                                
                                lowC += 0x0000000101010101L;
                            }
                            case 6 -> {
                                lowN += 0x0000000101010000L;
                                
                                lowE += 0x0101010101010101L;
                                
                                lowS += 0x0000000000010101L;
                                lowSW += 0x0000000000010100L;
                                lowW += 0x0000000001010100L;
                                lowNW += 0x0000000001010000L;
                                lowC += 0x0000000101010101L;
                            }
                            case 7 -> {
                                lowN += 0x0000000101010000L;
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                lowW += 0x0000000001010100L;
                                lowNW += 0x0000000001010000L;
                                lowC += 0x0000000101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                lowS += 0x0000000000000101L;
                                lowSW += 0x0000000000000100L;
                                
                                
                                lowC += 0x0000000000010101L;
                            }
                            case 5 -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                lowS += 0x0000000000000101L;
                                lowSW += 0x0000000000000100L;
                                lowW += 0x0000000000010100L;
                                lowNW += 0x0000000001010100L;
                                lowC += 0x0000000000010101L;
                            }
                            case 6 -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                lowSW += 0x0000000000000100L;
                                lowW += 0x0000000000010100L;
                                lowNW += 0x0000000001010100L;
                                lowC += 0x0000000000010101L;
                            }
                            case 7 -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000001010100L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                lowN += 0x0000000000010101L;
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                
                                lowC += 0x0000000000000101L;
                            }
                            case 5 -> {
                                lowN += 0x0000000000010101L;
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                lowNW += 0x0000000000010100L;
                                lowC += 0x0000000000000101L;
                            }
                            case 6 -> {
                                lowN += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                lowNW += 0x0000000000010100L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                lowE += 0x0000000101000000L;
                                lowSE += 0x0000000101010000L;
                                
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                lowSE += 0x0000000101010000L;
                                lowS += 0x0000000001010000L;
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            case 7 -> {
                                
                                
                                lowE += 0x0000000101000000L;
                                lowSE += 0x0000000101010000L;
                                lowS += 0x0000000001010000L;
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                lowN += 0x0000000001000000L;
                                lowNE += 0x0000000101000000L;
                                lowE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                
                            }
                            case 6 -> {
                                lowN += 0x0000000001000000L;
                                
                                lowE += 0x0000000101010000L;
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                lowC += 0x0000000001010000L;
                            }
                            case 7 -> {
                                lowN += 0x0000000001000000L;
                                lowNE += 0x0000000101000000L;
                                lowE += 0x0000000101010000L;
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                lowC += 0x0000000001010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                lowNE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                lowSE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                lowE += 0x0000000101010101L;
                                lowSE += 0x0000000000010101L;
                                lowS += 0x0000000000010100L;
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            case 6 -> {
                                lowN += 0x0000000001010000L;
                                lowNE += 0x0000000101010000L;
                                lowE += 0x0000000101010101L;
                                lowSE += 0x0000000000010101L;
                                lowS += 0x0000000000010100L;
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            case 7 -> {
                                lowN += 0x0000000001010000L;
                                lowNE += 0x0000000101010000L;
                                lowE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                lowE += 0x0000000000010101L;
                                lowSE += 0x0000000000000101L;
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                lowE += 0x0000000000010101L;
                                lowSE += 0x0000000000000101L;
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                lowC += 0x0000000000010100L;
                            }
                            case 6 -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                lowE += 0x0000000000010101L;
                                
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                lowC += 0x0000000000010100L;
                            }
                            case 7 -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                lowNE += 0x0000000000010101L;
                                lowE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case 5 -> {
                                lowN += 0x0000000000010100L;
                                lowNE += 0x0000000000010101L;
                                lowE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case 6 -> {
                                lowN += 0x0000000000010100L;
                                lowNE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    public static void registerEnemyHigh(int dx, int dy, Direction facing) {
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010100000000L;
                                highW += 0x0000010100000000L;
                                
                                highC += 0x0000010000000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                highS += 0x0001010000000000L;
                                highSW += 0x0001010100000000L;
                                highW += 0x0000010100000000L;
                                
                                highC += 0x0000010000000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                
                                highS += 0x0001010000000000L;
                                highSW += 0x0001010100000000L;
                                
                                
                                highC += 0x0000010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0001010100000000L;
                                highNW += 0x0000010100000000L;
                                
                            }
                            case 1 -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                highW += 0x0001010100000000L;
                                highNW += 0x0000010100000000L;
                                highC += 0x0001010000000000L;
                            }
                            case 2 -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                highW += 0x0001010100000000L;
                                
                                highC += 0x0001010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0001010100000000L;
                                
                            }
                            case 1 -> {
                                highN += 0x0001010000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0101010100000001L;
                                highNW += 0x0001010100000000L;
                                highC += 0x0101010000000000L;
                            }
                            case 2 -> {
                                highN += 0x0001010000000000L;
                                
                                
                                
                                highS += 0x0101000000000000L;
                                highSW += 0x0101000000000001L;
                                highW += 0x0101010100000001L;
                                highNW += 0x0001010100000000L;
                                highC += 0x0101010000000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                
                                highS += 0x0101000000000000L;
                                highSW += 0x0101000000000001L;
                                highW += 0x0101010100000001L;
                                
                                highC += 0x0101010000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000000001L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010100000001L;
                                
                            }
                            case 2 -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                highS += 0x0100000000000000L;
                                
                                highW += 0x0101000000000001L;
                                highNW += 0x0101010100000001L;
                                highC += 0x0101000000000000L;
                            }
                            case 3 -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                highS += 0x0100000000000000L;
                                highSW += 0x0100000000000001L;
                                highW += 0x0101000000000001L;
                                highNW += 0x0101010100000001L;
                                highC += 0x0101000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                highS += 0x0100000000000000L;
                                highSW += 0x0100000000000001L;
                                highW += 0x0101000000000001L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                highN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case 3 -> {
                                highN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0100000000000001L;
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000001L;
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000010000000000L;
                                
                                highS += 0x0001010100000000L;
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                highC += 0x0000010100000000L;
                            }
                            case 1 -> {
                                
                                
                                highE += 0x0000010000000000L;
                                highSE += 0x0001010000000000L;
                                highS += 0x0001010100000000L;
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                highC += 0x0000010100000000L;
                            }
                            case 2 -> {
                                
                                
                                highE += 0x0000010000000000L;
                                highSE += 0x0001010000000000L;
                                highS += 0x0001010100000000L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000010100000000L;
                                highNE += 0x0000010000000000L;
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                highC += 0x0001010100000000L;
                            }
                            case 1 -> {
                                highN += 0x0000010100000000L;
                                highNE += 0x0000010000000000L;
                                highE += 0x0001010000000000L;
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                highC += 0x0001010100000000L;
                            }
                            case 2 -> {
                                
                                highNE += 0x0000010000000000L;
                                highE += 0x0001010000000000L;
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                
                                
                                highC += 0x0001010100000000L;
                            }
                            case 3 -> {
                                
                                
                                
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                
                            }
                            case 1 -> {
                                highN += 0x0001010100000000L;
                                highNE += 0x0001010000000000L;
                                highE += 0x0101010000000000L;
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                highC += 0x0101010100000001L;
                            }
                            case 2 -> {
                                highN += 0x0001010100000000L;
                                highNE += 0x0001010000000000L;
                                highE += 0x0101010000000000L;
                                highSE += 0x0101000000000000L;
                                highS += 0x0101000000000001L;
                                
                                highW += 0x0101010101010101L;
                                
                                highC += 0x0101010100000001L;
                            }
                            case 3 -> {
                                
                                
                                highE += 0x0101010000000000L;
                                highSE += 0x0101000000000000L;
                                highS += 0x0101000000000001L;
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                highC += 0x0101010100000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                
                                highS += 0x0101000000000001L;
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 1 -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 2 -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                highE += 0x0101000000000000L;
                                highSE += 0x0100000000000000L;
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case 3 -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                highE += 0x0101000000000000L;
                                highSE += 0x0100000000000000L;
                                highS += 0x0100000000000001L;
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0100000000000000L;
                                highS += 0x0100000000000001L;
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                highN += 0x0101000000000001L;
                                highNE += 0x0101000000000000L;
                                highE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0101000000000001L;
                                highNE += 0x0101000000000000L;
                                highE += 0x0100000000000000L;
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                highC += 0x0100000000000001L;
                            }
                            case 4 -> {
                                highN += 0x0101000000000001L;
                                
                                highE += 0x0100000000000000L;
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                highC += 0x0100000000000001L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000010100000000L;
                                highSE += 0x0001010100000000L;
                                highS += 0x0001010101010000L;
                                highSW += 0x0000000101010000L;
                                highW += 0x0000000101000000L;
                                
                                highC += 0x0000010101000000L;
                            }
                            case 1 -> {
                                
                                
                                highE += 0x0000010100000000L;
                                highSE += 0x0001010100000000L;
                                highS += 0x0001010101010000L;
                                
                                
                                
                                highC += 0x0000010101000000L;
                            }
                            case 2 -> {
                                
                                
                                
                                highSE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000000101010000L;
                                
                                
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                highS += 0x0001010101010000L;
                                highSW += 0x0000000101010000L;
                                highW += 0x0000000101000000L;
                                
                                highC += 0x0000010101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000010101000000L;
                                highNE += 0x0000010100000000L;
                                highE += 0x0001010100000000L;
                                
                                highS += 0x0101010101010101L;
                                
                                highW += 0x0000000101010000L;
                                highNW += 0x0000000101000000L;
                                highC += 0x0001010101010000L;
                            }
                            case 1 -> {
                                highN += 0x0000010101000000L;
                                highNE += 0x0000010100000000L;
                                highE += 0x0001010100000000L;
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                highC += 0x0001010101010000L;
                            }
                            case 2 -> {
                                
                                
                                highE += 0x0001010100000000L;
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                highW += 0x0000000101010000L;
                                
                                
                            }
                            case 7 -> {
                                highN += 0x0000010101000000L;
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                highW += 0x0000000101010000L;
                                highNW += 0x0000000101000000L;
                                highC += 0x0001010101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0001010101010000L;
                                highNE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            case 1 -> {
                                highN += 0x0001010101010000L;
                                highNE += 0x0001010100000000L;
                                highE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 2 -> {
                                
                                highNE += 0x0001010100000000L;
                                highE += 0x0101010100000001L;
                                highSE += 0x0101000000000001L;
                                
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 3 -> {
                                
                                
                                highE += 0x0101010100000001L;
                                highSE += 0x0101000000000001L;
                                highS += 0x0101000000010101L;
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0101000000000001L;
                                highS += 0x0101000000010101L;
                                highSW += 0x0000000000010101L;
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case 5 -> {
                                
                                
                                
                                
                                highS += 0x0101000000010101L;
                                highSW += 0x0000000000010101L;
                                highW += 0x0000000101010101L;
                                
                                highC += 0x0101010101010101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000000000010101L;
                                highW += 0x0000000101010101L;
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            case 7 -> {
                                highN += 0x0001010101010000L;
                                
                                
                                
                                
                                
                                highW += 0x0000000101010101L;
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                highE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                highE += 0x0101000000000001L;
                                highSE += 0x0100000000000001L;
                                highS += 0x0100000000000101L;
                                
                                
                                
                                highC += 0x0101000000010101L;
                            }
                            case 4 -> {
                                highN += 0x0101010101010101L;
                                
                                highE += 0x0101000000000001L;
                                highSE += 0x0100000000000001L;
                                highS += 0x0100000000000101L;
                                highSW += 0x0000000000000101L;
                                highW += 0x0000000000010101L;
                                
                                highC += 0x0101000000010101L;
                            }
                            case 5 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                highS += 0x0100000000000101L;
                                highSW += 0x0000000000000101L;
                                highW += 0x0000000000010101L;
                                highNW += 0x0000000101010101L;
                                highC += 0x0101000000010101L;
                            }
                            case 6 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000010101L;
                                highNW += 0x0000000101010101L;
                                
                            }
                            case 7 -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                highNE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                highN += 0x0101000000010101L;
                                highNE += 0x0101000000000001L;
                                highE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                highC += 0x0100000000000101L;
                            }
                            case 4 -> {
                                highN += 0x0101000000010101L;
                                highNE += 0x0101000000000001L;
                                highE += 0x0100000000000001L;
                                
                                
                                
                                highW += 0x0000000000000101L;
                                highNW += 0x0000000000010101L;
                                highC += 0x0100000000000101L;
                            }
                            case 5 -> {
                                highN += 0x0101000000010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000000101L;
                                highNW += 0x0000000000010101L;
                                highC += 0x0100000000000101L;
                            }
                            case 6 -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000000010101L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                highS += 0x0000000101010000L;
                                
                                highW += 0x0000000001000000L;
                                
                                highC += 0x0000000101000000L;
                            }
                            case 1 -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                
                                highS += 0x0000000101010000L;
                                highSW += 0x0000000001010000L;
                                highW += 0x0000000001000000L;
                                
                                
                            }
                            case 7 -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                highS += 0x0000000101010000L;
                                highSW += 0x0000000001010000L;
                                highW += 0x0000000001000000L;
                                
                                highC += 0x0000000101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000000101000000L;
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            case 1 -> {
                                
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                highW += 0x0000000001010000L;
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            case 7 -> {
                                highN += 0x0000000101000000L;
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                highW += 0x0000000001010000L;
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000000101010000L;
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                highS += 0x0000000000010101L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                highS += 0x0000000000010101L;
                                highSW += 0x0000000000010100L;
                                highW += 0x0000000001010100L;
                                
                                highC += 0x0000000101010101L;
                            }
                            case 6 -> {
                                highN += 0x0000000101010000L;
                                
                                highE += 0x0101010101010101L;
                                
                                highS += 0x0000000000010101L;
                                highSW += 0x0000000000010100L;
                                highW += 0x0000000001010100L;
                                highNW += 0x0000000001010000L;
                                highC += 0x0000000101010101L;
                            }
                            case 7 -> {
                                highN += 0x0000000101010000L;
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                highW += 0x0000000001010100L;
                                highNW += 0x0000000001010000L;
                                highC += 0x0000000101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                highS += 0x0000000000000101L;
                                highSW += 0x0000000000000100L;
                                
                                
                                highC += 0x0000000000010101L;
                            }
                            case 5 -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                highS += 0x0000000000000101L;
                                highSW += 0x0000000000000100L;
                                highW += 0x0000000000010100L;
                                highNW += 0x0000000001010100L;
                                highC += 0x0000000000010101L;
                            }
                            case 6 -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                highSW += 0x0000000000000100L;
                                highW += 0x0000000000010100L;
                                highNW += 0x0000000001010100L;
                                highC += 0x0000000000010101L;
                            }
                            case 7 -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000001010100L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                highN += 0x0000000000010101L;
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                highW += 0x0000000000000100L;
                                
                                highC += 0x0000000000000101L;
                            }
                            case 5 -> {
                                highN += 0x0000000000010101L;
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                highW += 0x0000000000000100L;
                                highNW += 0x0000000000010100L;
                                highC += 0x0000000000000101L;
                            }
                            case 6 -> {
                                highN += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000000100L;
                                highNW += 0x0000000000010100L;
                                
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                highE += 0x0000000101000000L;
                                highSE += 0x0000000101010000L;
                                
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 6 -> {
                                
                                
                                
                                highSE += 0x0000000101010000L;
                                highS += 0x0000000001010000L;
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            case 7 -> {
                                
                                
                                highE += 0x0000000101000000L;
                                highSE += 0x0000000101010000L;
                                highS += 0x0000000001010000L;
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                highN += 0x0000000001000000L;
                                highNE += 0x0000000101000000L;
                                highE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                
                            }
                            case 6 -> {
                                highN += 0x0000000001000000L;
                                
                                highE += 0x0000000101010000L;
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                highC += 0x0000000001010000L;
                            }
                            case 7 -> {
                                highN += 0x0000000001000000L;
                                highNE += 0x0000000101000000L;
                                highE += 0x0000000101010000L;
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                highC += 0x0000000001010000L;
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                highNE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                
                                highSE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                            }
                            case 5 -> {
                                
                                
                                highE += 0x0000000101010101L;
                                highSE += 0x0000000000010101L;
                                highS += 0x0000000000010100L;
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            case 6 -> {
                                highN += 0x0000000001010000L;
                                highNE += 0x0000000101010000L;
                                highE += 0x0000000101010101L;
                                highSE += 0x0000000000010101L;
                                highS += 0x0000000000010100L;
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            case 7 -> {
                                highN += 0x0000000001010000L;
                                highNE += 0x0000000101010000L;
                                highE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                
                                highE += 0x0000000000010101L;
                                highSE += 0x0000000000000101L;
                                highS += 0x0000000000000100L;
                                
                                
                                
                                
                            }
                            case 5 -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                highE += 0x0000000000010101L;
                                highSE += 0x0000000000000101L;
                                highS += 0x0000000000000100L;
                                
                                
                                
                                highC += 0x0000000000010100L;
                            }
                            case 6 -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                highE += 0x0000000000010101L;
                                
                                highS += 0x0000000000000100L;
                                
                                
                                
                                highC += 0x0000000000010100L;
                            }
                            case 7 -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing.ordinal()) {
                            case 0 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 1 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 2 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 3 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case 4 -> {
                                
                                highNE += 0x0000000000010101L;
                                highE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case 5 -> {
                                highN += 0x0000000000010100L;
                                highNE += 0x0000000000010101L;
                                highE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case 6 -> {
                                highN += 0x0000000000010100L;
                                highNE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case 7 -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    public static boolean hasThreat(Direction moveDir, Direction facing) {
        long low = getLowMask(moveDir);
        long high = getHighMask(moveDir);
        long hidden = getHiddenMask(facing);
        long threat = getMoveThreatMask(moveDir);
        long combined = high + (low & hidden);
        return (combined & threat) != 0L;
    }

    public static boolean hasThreat(Direction moveDir, Direction facing, RobotInfo target) {
        if (target == null) return hasThreat(moveDir, facing);
        if (target.getTeam() == rc.getTeam()) return hasThreat(moveDir, facing);
        if (target.getType() != UnitType.BABY_RAT) return hasThreat(moveDir, facing);
        int enemyDx = target.getLocation().x - myLocX;
        int enemyDy = target.getLocation().y - myLocY;
        if (enemyDx < -2 || enemyDx > 2 || enemyDy < -2 || enemyDy > 2) return hasThreat(moveDir, facing);
        long low = getLowMask(moveDir);
        long high = getHighMask(moveDir);
        long hidden = getHiddenMask(facing);
        long threat = getMoveThreatMask(moveDir);
        long sub = enemyMaskForMove(enemyDx, enemyDy, target.getDirection(), moveDir);
        if (target.getHealth() > rc.getHealth()) {
            high -= sub;
        } else {
            low -= sub;
        }
        long combined = high + (low & hidden);
        return (combined & threat) != 0L;
    }

    private static long getLowMask(Direction moveDir) {
        return switch (moveDir.ordinal()) {
            case 0 -> lowN;
            case 1 -> lowNE;
            case 2 -> lowE;
            case 3 -> lowSE;
            case 4 -> lowS;
            case 5 -> lowSW;
            case 6 -> lowW;
            case 7 -> lowNW;
            case 8 -> lowC;
            default -> 0L;
        };
    }

    private static long getHighMask(Direction moveDir) {
        return switch (moveDir.ordinal()) {
            case 0 -> highN;
            case 1 -> highNE;
            case 2 -> highE;
            case 3 -> highSE;
            case 4 -> highS;
            case 5 -> highSW;
            case 6 -> highW;
            case 7 -> highNW;
            case 8 -> highC;
            default -> 0L;
        };
    }

    private static long getMoveThreatMask(Direction moveDir) {
        return switch (moveDir.ordinal()) {
            case 0 -> moveThreatN;
            case 1 -> moveThreatNE;
            case 2 -> moveThreatE;
            case 3 -> moveThreatSE;
            case 4 -> moveThreatS;
            case 5 -> moveThreatSW;
            case 6 -> moveThreatW;
            case 7 -> moveThreatNW;
            case 8 -> moveThreatC;
            default -> 0L;
        };
    }

    private static long getHiddenMask(Direction facing) {
        return switch (facing.ordinal()) {
            case 0 -> hiddenN;
            case 1 -> hiddenNE;
            case 2 -> hiddenE;
            case 3 -> hiddenSE;
            case 4 -> hiddenS;
            case 5 -> hiddenSW;
            case 6 -> hiddenW;
            case 7 -> hiddenNW;
            default -> 0L;
        };
    }

    private static long enemyMaskForMove(int dx, int dy, Direction facing, Direction moveDir) {
        int faceIdx = facing.ordinal();
        int moveIdx = moveDir.ordinal();
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0001010100000000L; }
                                    case 6 -> { return 0x0000010100000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010000000000L; }
                                    case 5 -> { return 0x0001010100000000L; }
                                    case 6 -> { return 0x0000010100000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010000000000L; }
                                    case 5 -> { return 0x0001010100000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0001010100000000L; }
                                    case 7 -> { return 0x0000010100000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010000000000L; }
                                    case 5 -> { return 0x0101010100000001L; }
                                    case 6 -> { return 0x0001010100000000L; }
                                    case 7 -> { return 0x0000010100000000L; }
                                    case 8 -> { return 0x0001010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010000000000L; }
                                    case 5 -> { return 0x0101010100000001L; }
                                    case 6 -> { return 0x0001010100000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0001010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010000000000L; }
                                    case 5 -> { return 0x0101010100000001L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0001010100000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010100000001L; }
                                    case 7 -> { return 0x0001010100000000L; }
                                    case 8 -> { return 0x0101010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000000000L; }
                                    case 5 -> { return 0x0101000000000001L; }
                                    case 6 -> { return 0x0101010100000001L; }
                                    case 7 -> { return 0x0001010100000000L; }
                                    case 8 -> { return 0x0101010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000000000L; }
                                    case 5 -> { return 0x0101000000000001L; }
                                    case 6 -> { return 0x0101010100000001L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101000000000001L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010100000001L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101000000000001L; }
                                    case 7 -> { return 0x0101010100000001L; }
                                    case 8 -> { return 0x0101000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000000L; }
                                    case 5 -> { return 0x0100000000000001L; }
                                    case 6 -> { return 0x0101000000000001L; }
                                    case 7 -> { return 0x0101010100000001L; }
                                    case 8 -> { return 0x0101000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000000L; }
                                    case 5 -> { return 0x0100000000000001L; }
                                    case 6 -> { return 0x0101000000000001L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101000000000001L; }
                                    case 8 -> { return 0x0100000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000001L; }
                                    case 7 -> { return 0x0101000000000001L; }
                                    case 8 -> { return 0x0100000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000001L; }
                                    case 7 -> { return 0x0101000000000001L; }
                                    case 8 -> { return 0x0100000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010100000000L; }
                                    case 5 -> { return 0x0001010101010000L; }
                                    case 6 -> { return 0x0000010101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010000000000L; }
                                    case 3 -> { return 0x0001010000000000L; }
                                    case 4 -> { return 0x0001010100000000L; }
                                    case 5 -> { return 0x0001010101010000L; }
                                    case 6 -> { return 0x0000010101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010000000000L; }
                                    case 3 -> { return 0x0001010000000000L; }
                                    case 4 -> { return 0x0001010100000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0001010101010000L; }
                                    case 6 -> { return 0x0000010101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010100000000L; }
                                    case 1 -> { return 0x0000010000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0001010101010000L; }
                                    case 7 -> { return 0x0000010101000000L; }
                                    case 8 -> { return 0x0001010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010100000000L; }
                                    case 1 -> { return 0x0000010000000000L; }
                                    case 2 -> { return 0x0001010000000000L; }
                                    case 3 -> { return 0x0101010000000000L; }
                                    case 4 -> { return 0x0101010100000001L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0001010101010000L; }
                                    case 7 -> { return 0x0000010101000000L; }
                                    case 8 -> { return 0x0001010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000010000000000L; }
                                    case 2 -> { return 0x0001010000000000L; }
                                    case 3 -> { return 0x0101010000000000L; }
                                    case 4 -> { return 0x0101010100000001L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0001010100000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010000000000L; }
                                    case 4 -> { return 0x0101010100000001L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101010101010101L; }
                                    case 6 -> { return 0x0001010101010000L; }
                                    case 7 -> { return 0x0000010101000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010100000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0001010101010000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010100000000L; }
                                    case 1 -> { return 0x0001010000000000L; }
                                    case 2 -> { return 0x0101010000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0001010101010000L; }
                                    case 8 -> { return 0x0101010100000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010100000000L; }
                                    case 1 -> { return 0x0001010000000000L; }
                                    case 2 -> { return 0x0101010000000000L; }
                                    case 3 -> { return 0x0101000000000000L; }
                                    case 4 -> { return 0x0101000000000001L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010100000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010000000000L; }
                                    case 3 -> { return 0x0101000000000000L; }
                                    case 4 -> { return 0x0101000000000001L; }
                                    case 5 -> { return 0x0101000000010101L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010100000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000000001L; }
                                    case 5 -> { return 0x0101000000010101L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0101000000010101L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0101010101010101L; }
                                    case 7 -> { return 0x0001010101010000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010100000001L; }
                                    case 1 -> { return 0x0101010000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010100000001L; }
                                    case 1 -> { return 0x0101010000000000L; }
                                    case 2 -> { return 0x0101000000000000L; }
                                    case 3 -> { return 0x0100000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0101000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010100000001L; }
                                    case 1 -> { return 0x0101010000000000L; }
                                    case 2 -> { return 0x0101000000000000L; }
                                    case 3 -> { return 0x0100000000000000L; }
                                    case 4 -> { return 0x0100000000000001L; }
                                    case 5 -> { return 0x0100000000000101L; }
                                    case 6 -> { return 0x0101000000010101L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0101000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0100000000000000L; }
                                    case 4 -> { return 0x0100000000000001L; }
                                    case 5 -> { return 0x0100000000000101L; }
                                    case 6 -> { return 0x0101000000010101L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0101000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0100000000000101L; }
                                    case 6 -> { return 0x0101000000010101L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0101010101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000001L; }
                                    case 1 -> { return 0x0101000000000000L; }
                                    case 2 -> { return 0x0100000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000001L; }
                                    case 1 -> { return 0x0101000000000000L; }
                                    case 2 -> { return 0x0100000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000101L; }
                                    case 7 -> { return 0x0101000000010101L; }
                                    case 8 -> { return 0x0100000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000000001L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0100000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000101L; }
                                    case 7 -> { return 0x0101000000010101L; }
                                    case 8 -> { return 0x0100000000000001L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0100000000000101L; }
                                    case 7 -> { return 0x0101000000010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010100000000L; }
                                    case 3 -> { return 0x0001010100000000L; }
                                    case 4 -> { return 0x0001010101010000L; }
                                    case 5 -> { return 0x0000000101010000L; }
                                    case 6 -> { return 0x0000000101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010100000000L; }
                                    case 3 -> { return 0x0001010100000000L; }
                                    case 4 -> { return 0x0001010101010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0001010100000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000101010000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0001010101010000L; }
                                    case 5 -> { return 0x0000000101010000L; }
                                    case 6 -> { return 0x0000000101000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000010101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010101000000L; }
                                    case 1 -> { return 0x0000010100000000L; }
                                    case 2 -> { return 0x0001010100000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000101010000L; }
                                    case 7 -> { return 0x0000000101000000L; }
                                    case 8 -> { return 0x0001010101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010101000000L; }
                                    case 1 -> { return 0x0000010100000000L; }
                                    case 2 -> { return 0x0001010100000000L; }
                                    case 3 -> { return 0x0101010100000001L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0001010101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0001010100000000L; }
                                    case 3 -> { return 0x0101010100000001L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010100000001L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000101010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000101010101L; }
                                    case 6 -> { return 0x0000000101010000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000010101000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101010101010101L; }
                                    case 5 -> { return 0x0000000101010101L; }
                                    case 6 -> { return 0x0000000101010000L; }
                                    case 7 -> { return 0x0000000101000000L; }
                                    case 8 -> { return 0x0001010101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010101010000L; }
                                    case 1 -> { return 0x0001010100000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000101010000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010101010000L; }
                                    case 1 -> { return 0x0001010100000000L; }
                                    case 2 -> { return 0x0101010100000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0001010100000000L; }
                                    case 2 -> { return 0x0101010100000001L; }
                                    case 3 -> { return 0x0101000000000001L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010100000001L; }
                                    case 3 -> { return 0x0101000000000001L; }
                                    case 4 -> { return 0x0101000000010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101000000000001L; }
                                    case 4 -> { return 0x0101000000010101L; }
                                    case 5 -> { return 0x0000000000010101L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0101000000010101L; }
                                    case 5 -> { return 0x0000000000010101L; }
                                    case 6 -> { return 0x0000000101010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000010101L; }
                                    case 6 -> { return 0x0000000101010101L; }
                                    case 7 -> { return 0x0000000101010000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0001010101010000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000101010101L; }
                                    case 7 -> { return 0x0000000101010000L; }
                                    case 8 -> { return 0x0101010101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0101010100000001L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0101010100000001L; }
                                    case 2 -> { return 0x0101000000000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0101010100000001L; }
                                    case 2 -> { return 0x0101000000000001L; }
                                    case 3 -> { return 0x0100000000000001L; }
                                    case 4 -> { return 0x0100000000000101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101000000000001L; }
                                    case 3 -> { return 0x0100000000000001L; }
                                    case 4 -> { return 0x0100000000000101L; }
                                    case 5 -> { return 0x0000000000000101L; }
                                    case 6 -> { return 0x0000000000010101L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0101000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0100000000000101L; }
                                    case 5 -> { return 0x0000000000000101L; }
                                    case 6 -> { return 0x0000000000010101L; }
                                    case 7 -> { return 0x0000000101010101L; }
                                    case 8 -> { return 0x0101000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000010101L; }
                                    case 7 -> { return 0x0000000101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101010101010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000101010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101000000000001L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000010101L; }
                                    case 1 -> { return 0x0101000000000001L; }
                                    case 2 -> { return 0x0100000000000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0100000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000010101L; }
                                    case 1 -> { return 0x0101000000000001L; }
                                    case 2 -> { return 0x0100000000000001L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000101L; }
                                    case 7 -> { return 0x0000000000010101L; }
                                    case 8 -> { return 0x0100000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0101000000010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000101L; }
                                    case 7 -> { return 0x0000000000010101L; }
                                    case 8 -> { return 0x0100000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000010101L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010101000000L; }
                                    case 3 -> { return 0x0001010101010000L; }
                                    case 4 -> { return 0x0000000101010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000001000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010101000000L; }
                                    case 3 -> { return 0x0001010101010000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000101010000L; }
                                    case 5 -> { return 0x0000000001010000L; }
                                    case 6 -> { return 0x0000000001000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000010101000000L; }
                                    case 3 -> { return 0x0001010101010000L; }
                                    case 4 -> { return 0x0000000101010000L; }
                                    case 5 -> { return 0x0000000001010000L; }
                                    case 6 -> { return 0x0000000001000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000101000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101000000L; }
                                    case 1 -> { return 0x0000010101000000L; }
                                    case 2 -> { return 0x0001010101010000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000001000000L; }
                                    case 8 -> { return 0x0000000101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000010101000000L; }
                                    case 2 -> { return 0x0001010101010000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000101010101L; }
                                    case 5 -> { return 0x0000000001010100L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000101010101L; }
                                    case 5 -> { return 0x0000000001010100L; }
                                    case 6 -> { return 0x0000000001010000L; }
                                    case 7 -> { return 0x0000000001000000L; }
                                    case 8 -> { return 0x0000000101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101000000L; }
                                    case 1 -> { return 0x0000010101000000L; }
                                    case 2 -> { return 0x0001010101010000L; }
                                    case 3 -> { return 0x0101010101010101L; }
                                    case 4 -> { return 0x0000000101010101L; }
                                    case 5 -> { return 0x0000000001010100L; }
                                    case 6 -> { return 0x0000000001010000L; }
                                    case 7 -> { return 0x0000000001000000L; }
                                    case 8 -> { return 0x0000000101010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010000L; }
                                    case 1 -> { return 0x0001010101010000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0001010101010000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0101000000010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0101000000010101L; }
                                    case 4 -> { return 0x0000000000010101L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0101000000010101L; }
                                    case 4 -> { return 0x0000000000010101L; }
                                    case 5 -> { return 0x0000000000010100L; }
                                    case 6 -> { return 0x0000000001010100L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000010101L; }
                                    case 5 -> { return 0x0000000000010100L; }
                                    case 6 -> { return 0x0000000001010100L; }
                                    case 7 -> { return 0x0000000001010000L; }
                                    case 8 -> { return 0x0000000101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010000L; }
                                    case 1 -> { return 0x0001010101010000L; }
                                    case 2 -> { return 0x0101010101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000001010100L; }
                                    case 7 -> { return 0x0000000001010000L; }
                                    case 8 -> { return 0x0000000101010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0101000000010101L; }
                                    case 3 -> { return 0x0100000000000101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0101000000010101L; }
                                    case 3 -> { return 0x0100000000000101L; }
                                    case 4 -> { return 0x0000000000000101L; }
                                    case 5 -> { return 0x0000000000000100L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010101L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0101000000010101L; }
                                    case 3 -> { return 0x0100000000000101L; }
                                    case 4 -> { return 0x0000000000000101L; }
                                    case 5 -> { return 0x0000000000000100L; }
                                    case 6 -> { return 0x0000000000010100L; }
                                    case 7 -> { return 0x0000000001010100L; }
                                    case 8 -> { return 0x0000000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010101L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000100L; }
                                    case 6 -> { return 0x0000000000010100L; }
                                    case 7 -> { return 0x0000000001010100L; }
                                    case 8 -> { return 0x0000000000010101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000101010101L; }
                                    case 1 -> { return 0x0101010101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000001010100L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0101000000010101L; }
                                    case 2 -> { return 0x0100000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010101L; }
                                    case 1 -> { return 0x0101000000010101L; }
                                    case 2 -> { return 0x0100000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000100L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010101L; }
                                    case 1 -> { return 0x0101000000010101L; }
                                    case 2 -> { return 0x0100000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000100L; }
                                    case 7 -> { return 0x0000000000010100L; }
                                    case 8 -> { return 0x0000000000000101L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010101L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000100L; }
                                    case 7 -> { return 0x0000000000010100L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101000000L; }
                                    case 3 -> { return 0x0000000101010000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000101010000L; }
                                    case 4 -> { return 0x0000000001010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101000000L; }
                                    case 3 -> { return 0x0000000101010000L; }
                                    case 4 -> { return 0x0000000001010000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001000000L; }
                                    case 1 -> { return 0x0000000101000000L; }
                                    case 2 -> { return 0x0000000101010000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000101010101L; }
                                    case 4 -> { return 0x0000000001010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101010000L; }
                                    case 3 -> { return 0x0000000101010101L; }
                                    case 4 -> { return 0x0000000001010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001000000L; }
                                    case 1 -> { return 0x0000000101000000L; }
                                    case 2 -> { return 0x0000000101010000L; }
                                    case 3 -> { return 0x0000000101010101L; }
                                    case 4 -> { return 0x0000000001010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000101010000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000010101L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000101010101L; }
                                    case 3 -> { return 0x0000000000010101L; }
                                    case 4 -> { return 0x0000000000010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010000L; }
                                    case 1 -> { return 0x0000000101010000L; }
                                    case 2 -> { return 0x0000000101010101L; }
                                    case 3 -> { return 0x0000000000010101L; }
                                    case 4 -> { return 0x0000000000010100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010000L; }
                                    case 1 -> { return 0x0000000101010000L; }
                                    case 2 -> { return 0x0000000101010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000001010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000010101L; }
                                    case 3 -> { return 0x0000000000000101L; }
                                    case 4 -> { return 0x0000000000000100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010100L; }
                                    case 1 -> { return 0x0000000101010101L; }
                                    case 2 -> { return 0x0000000000010101L; }
                                    case 3 -> { return 0x0000000000000101L; }
                                    case 4 -> { return 0x0000000000000100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010100L; }
                                    case 1 -> { return 0x0000000101010101L; }
                                    case 2 -> { return 0x0000000000010101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000100L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000010100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000001010100L; }
                                    case 1 -> { return 0x0000000101010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (faceIdx) {
                            case 0 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 1 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 2 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 3 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 4 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000010101L; }
                                    case 2 -> { return 0x0000000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 5 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010100L; }
                                    case 1 -> { return 0x0000000000010101L; }
                                    case 2 -> { return 0x0000000000000101L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 6 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000010100L; }
                                    case 1 -> { return 0x0000000000010101L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000100L; }
                                    default -> { return 0L; }
                                }
                            }
                            case 7 -> {
                                switch (moveIdx) {
                                    case 0 -> { return 0x0000000000000000L; }
                                    case 1 -> { return 0x0000000000000000L; }
                                    case 2 -> { return 0x0000000000000000L; }
                                    case 3 -> { return 0x0000000000000000L; }
                                    case 4 -> { return 0x0000000000000000L; }
                                    case 5 -> { return 0x0000000000000000L; }
                                    case 6 -> { return 0x0000000000000000L; }
                                    case 7 -> { return 0x0000000000000000L; }
                                    case 8 -> { return 0x0000000000000000L; }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    default -> { return 0L; }
                }
            }
            default -> { return 0L; }
        }
    }

    private static boolean isThreatAt(int dx, int dy) {
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> { return t_m2_m2; }
                    case -1 -> { return t_m2_m1; }
                    case 0 -> { return t_m2_0; }
                    case 1 -> { return t_m2_1; }
                    case 2 -> { return t_m2_2; }
                    default -> { return false; }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -2 -> { return t_m1_m2; }
                    case -1 -> { return t_m1_m1; }
                    case 0 -> { return t_m1_0; }
                    case 1 -> { return t_m1_1; }
                    case 2 -> { return t_m1_2; }
                    default -> { return false; }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -2 -> { return t_0_m2; }
                    case -1 -> { return t_0_m1; }
                    case 0 -> { return t_0_0; }
                    case 1 -> { return t_0_1; }
                    case 2 -> { return t_0_2; }
                    default -> { return false; }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -2 -> { return t_1_m2; }
                    case -1 -> { return t_1_m1; }
                    case 0 -> { return t_1_0; }
                    case 1 -> { return t_1_1; }
                    case 2 -> { return t_1_2; }
                    default -> { return false; }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -2 -> { return t_2_m2; }
                    case -1 -> { return t_2_m1; }
                    case 0 -> { return t_2_0; }
                    case 1 -> { return t_2_1; }
                    case 2 -> { return t_2_2; }
                    default -> { return false; }
                }
            }
            default -> { return false; }
        }
    }

    // vision is fully precomputed into enemy masks, no runtime cone checks needed
}