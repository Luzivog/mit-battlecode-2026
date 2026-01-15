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
        loadThreatFromMap();
        for (MapInfo info : rc.senseNearbyMapInfos(8)) {
            if (info.getTrap() == TrapType.NONE) continue;
            MapLocation loc = info.getMapLocation();
            int dx = loc.x - myLocX;
            int dy = loc.y - myLocY;
            if (dx < -2 || dx > 2 || dy < -2 || dy > 2) continue;
            registerTrap(dx, dy);
        }
        recomputeThreatMasks();
        resetEnemyCounts();
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (info.getType() != UnitType.BABY_RAT) continue;
            registerEnemy(info);
        }
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
        t_m2_2 = isCellMaybePassable(new MapLocation(x  + -2 , y  + 2 ));
        t_m1_2 = isCellMaybePassable(new MapLocation(x  + -1 , y  + 2 ));
        t_0_2 = isCellMaybePassable(new MapLocation(x , y  + 2 ));
        t_1_2 = isCellMaybePassable(new MapLocation(x  + 1 , y  + 2 ));
        t_2_2 = isCellMaybePassable(new MapLocation(x  + 2 , y  + 2 ));
        t_m2_1 = isCellMaybePassable(new MapLocation(x  + -2 , y  + 1 ));
        t_m1_1 = isCellMaybePassable(new MapLocation(x  + -1 , y  + 1 ));
        t_0_1 = isCellMaybePassable(new MapLocation(x , y  + 1 ));
        t_1_1 = isCellMaybePassable(new MapLocation(x  + 1 , y  + 1 ));
        t_2_1 = isCellMaybePassable(new MapLocation(x  + 2 , y  + 1 ));
        t_m2_0 = isCellMaybePassable(new MapLocation(x  + -2 , y ));
        t_m1_0 = isCellMaybePassable(new MapLocation(x  + -1 , y ));
        t_0_0 = isCellMaybePassable(new MapLocation(x , y ));
        t_1_0 = isCellMaybePassable(new MapLocation(x  + 1 , y ));
        t_2_0 = isCellMaybePassable(new MapLocation(x  + 2 , y ));
        t_m2_m1 = isCellMaybePassable(new MapLocation(x  + -2 , y  + -1 ));
        t_m1_m1 = isCellMaybePassable(new MapLocation(x  + -1 , y  + -1 ));
        t_0_m1 = isCellMaybePassable(new MapLocation(x , y  + -1 ));
        t_1_m1 = isCellMaybePassable(new MapLocation(x  + 1 , y  + -1 ));
        t_2_m1 = isCellMaybePassable(new MapLocation(x  + 2 , y  + -1 ));
        t_m2_m2 = isCellMaybePassable(new MapLocation(x  + -2 , y  + -2 ));
        t_m1_m2 = isCellMaybePassable(new MapLocation(x  + -1 , y  + -2 ));
        t_0_m2 = isCellMaybePassable(new MapLocation(x , y  + -2 ));
        t_1_m2 = isCellMaybePassable(new MapLocation(x  + 1 , y  + -2 ));
        t_2_m2 = isCellMaybePassable(new MapLocation(x  + 2 , y  + -2 ));
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010100000000L;
                                lowW += 0x0000010100000000L;
                                
                                lowC += 0x0000010000000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                lowS += 0x0001010000000000L;
                                lowSW += 0x0001010100000000L;
                                lowW += 0x0000010100000000L;
                                
                                lowC += 0x0000010000000000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                lowS += 0x0001010000000000L;
                                lowSW += 0x0001010100000000L;
                                
                                
                                lowC += 0x0000010000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0001010100000000L;
                                lowNW += 0x0000010100000000L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                lowW += 0x0001010100000000L;
                                lowNW += 0x0000010100000000L;
                                lowC += 0x0001010000000000L;
                            }
                            case Direction.EAST -> {
                                lowN += 0x0000010000000000L;
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                lowW += 0x0001010100000000L;
                                
                                lowC += 0x0001010000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                lowS += 0x0101010000000000L;
                                lowSW += 0x0101010100000001L;
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0001010100000000L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0001010000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0101010100000001L;
                                lowNW += 0x0001010100000000L;
                                lowC += 0x0101010000000000L;
                            }
                            case Direction.EAST -> {
                                lowN += 0x0001010000000000L;
                                
                                
                                
                                lowS += 0x0101000000000000L;
                                lowSW += 0x0101000000000001L;
                                lowW += 0x0101010100000001L;
                                lowNW += 0x0001010100000000L;
                                lowC += 0x0101010000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                lowS += 0x0101000000000000L;
                                lowSW += 0x0101000000000001L;
                                lowW += 0x0101010100000001L;
                                
                                lowC += 0x0101010000000000L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000000001L;
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010100000001L;
                                
                            }
                            case Direction.EAST -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                
                                lowW += 0x0101000000000001L;
                                lowNW += 0x0101010100000001L;
                                lowC += 0x0101000000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                lowN += 0x0101010000000000L;
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                lowSW += 0x0100000000000001L;
                                lowW += 0x0101000000000001L;
                                lowNW += 0x0101010100000001L;
                                lowC += 0x0101000000000000L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                lowS += 0x0100000000000000L;
                                lowSW += 0x0100000000000001L;
                                lowW += 0x0101000000000001L;
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                lowN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                lowN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000001L;
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000001L;
                                lowNW += 0x0101000000000001L;
                                lowC += 0x0100000000000000L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                
                                lowS += 0x0001010100000000L;
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                lowC += 0x0000010100000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                lowSE += 0x0001010000000000L;
                                lowS += 0x0001010100000000L;
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                lowC += 0x0000010100000000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                lowE += 0x0000010000000000L;
                                lowSE += 0x0001010000000000L;
                                lowS += 0x0001010100000000L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0001010101010000L;
                                lowW += 0x0000010101000000L;
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0000010100000000L;
                                lowNE += 0x0000010000000000L;
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                lowC += 0x0001010100000000L;
                            }
                            case Direction.NORTHEAST -> {
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
                            case Direction.EAST -> {
                                
                                lowNE += 0x0000010000000000L;
                                lowE += 0x0001010000000000L;
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                
                                
                                lowC += 0x0001010100000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                lowSE += 0x0101010000000000L;
                                lowS += 0x0101010100000001L;
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101010101010101L;
                                lowW += 0x0001010101010000L;
                                lowNW += 0x0000010101000000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0001010100000000L;
                                lowNE += 0x0001010000000000L;
                                lowE += 0x0101010000000000L;
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                lowC += 0x0101010100000001L;
                            }
                            case Direction.EAST -> {
                                lowN += 0x0001010100000000L;
                                lowNE += 0x0001010000000000L;
                                lowE += 0x0101010000000000L;
                                lowSE += 0x0101000000000000L;
                                lowS += 0x0101000000000001L;
                                
                                lowW += 0x0101010101010101L;
                                
                                lowC += 0x0101010100000001L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                lowE += 0x0101010000000000L;
                                lowSE += 0x0101000000000000L;
                                lowS += 0x0101000000000001L;
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                lowC += 0x0101010100000001L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                lowS += 0x0101000000000001L;
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0101000000010101L;
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0101010101010101L;
                                lowNW += 0x0001010101010000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case Direction.EAST -> {
                                lowN += 0x0101010100000001L;
                                lowNE += 0x0101010000000000L;
                                lowE += 0x0101000000000000L;
                                lowSE += 0x0100000000000000L;
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case Direction.SOUTHEAST -> {
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
                            case Direction.SOUTH -> {
                                
                                
                                
                                lowSE += 0x0100000000000000L;
                                lowS += 0x0100000000000001L;
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                lowC += 0x0101000000000001L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0100000000000101L;
                                lowW += 0x0101000000010101L;
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0101010101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                lowN += 0x0101000000000001L;
                                lowNE += 0x0101000000000000L;
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                lowN += 0x0101000000000001L;
                                lowNE += 0x0101000000000000L;
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                lowC += 0x0100000000000001L;
                            }
                            case Direction.SOUTH -> {
                                lowN += 0x0101000000000001L;
                                
                                lowE += 0x0100000000000000L;
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                lowC += 0x0100000000000001L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                lowW += 0x0100000000000101L;
                                lowNW += 0x0101000000010101L;
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                lowE += 0x0000010100000000L;
                                lowSE += 0x0001010100000000L;
                                lowS += 0x0001010101010000L;
                                lowSW += 0x0000000101010000L;
                                lowW += 0x0000000101000000L;
                                
                                lowC += 0x0000010101000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                lowE += 0x0000010100000000L;
                                lowSE += 0x0001010100000000L;
                                lowS += 0x0001010101010000L;
                                
                                
                                
                                lowC += 0x0000010101000000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                lowSE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000000101010000L;
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                lowS += 0x0001010101010000L;
                                lowSW += 0x0000000101010000L;
                                lowW += 0x0000000101000000L;
                                
                                lowC += 0x0000010101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0000010101000000L;
                                lowNE += 0x0000010100000000L;
                                lowE += 0x0001010100000000L;
                                
                                lowS += 0x0101010101010101L;
                                
                                lowW += 0x0000000101010000L;
                                lowNW += 0x0000000101000000L;
                                lowC += 0x0001010101010000L;
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0000010101000000L;
                                lowNE += 0x0000010100000000L;
                                lowE += 0x0001010100000000L;
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                lowC += 0x0001010101010000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                lowE += 0x0001010100000000L;
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                lowSE += 0x0101010100000001L;
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                lowS += 0x0101010101010101L;
                                lowSW += 0x0000000101010101L;
                                lowW += 0x0000000101010000L;
                                
                                
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0001010101010000L;
                                lowNE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0001010101010000L;
                                lowNE += 0x0001010100000000L;
                                lowE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case Direction.EAST -> {
                                
                                lowNE += 0x0001010100000000L;
                                lowE += 0x0101010100000001L;
                                lowSE += 0x0101000000000001L;
                                
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                lowE += 0x0101010100000001L;
                                lowSE += 0x0101000000000001L;
                                lowS += 0x0101000000010101L;
                                
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                lowSE += 0x0101000000000001L;
                                lowS += 0x0101000000010101L;
                                lowSW += 0x0000000000010101L;
                                
                                
                                lowC += 0x0101010101010101L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                lowS += 0x0101000000010101L;
                                lowSW += 0x0000000000010101L;
                                lowW += 0x0000000101010101L;
                                
                                lowC += 0x0101010101010101L;
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                lowSW += 0x0000000000010101L;
                                lowW += 0x0000000101010101L;
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            case Direction.NORTHWEST -> {
                                lowN += 0x0001010101010000L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000101010101L;
                                lowNW += 0x0000000101010000L;
                                lowC += 0x0101010101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                lowE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                lowN += 0x0101010101010101L;
                                lowNE += 0x0101010100000001L;
                                lowE += 0x0101000000000001L;
                                lowSE += 0x0100000000000001L;
                                lowS += 0x0100000000000101L;
                                
                                
                                
                                lowC += 0x0101000000010101L;
                            }
                            case Direction.SOUTH -> {
                                lowN += 0x0101010101010101L;
                                
                                lowE += 0x0101000000000001L;
                                lowSE += 0x0100000000000001L;
                                lowS += 0x0100000000000101L;
                                lowSW += 0x0000000000000101L;
                                lowW += 0x0000000000010101L;
                                
                                lowC += 0x0101000000010101L;
                            }
                            case Direction.SOUTHWEST -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                lowS += 0x0100000000000101L;
                                lowSW += 0x0000000000000101L;
                                lowW += 0x0000000000010101L;
                                lowNW += 0x0000000101010101L;
                                lowC += 0x0101000000010101L;
                            }
                            case Direction.WEST -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000010101L;
                                lowNW += 0x0000000101010101L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                lowN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                lowNE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                lowN += 0x0101000000010101L;
                                lowNE += 0x0101000000000001L;
                                lowE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                lowC += 0x0100000000000101L;
                            }
                            case Direction.SOUTH -> {
                                lowN += 0x0101000000010101L;
                                lowNE += 0x0101000000000001L;
                                lowE += 0x0100000000000001L;
                                
                                
                                
                                lowW += 0x0000000000000101L;
                                lowNW += 0x0000000000010101L;
                                lowC += 0x0100000000000101L;
                            }
                            case Direction.SOUTHWEST -> {
                                lowN += 0x0101000000010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000000101L;
                                lowNW += 0x0000000000010101L;
                                lowC += 0x0100000000000101L;
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                lowNW += 0x0000000000010101L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                lowS += 0x0000000101010000L;
                                
                                lowW += 0x0000000001000000L;
                                
                                lowC += 0x0000000101000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                lowE += 0x0000010101000000L;
                                lowSE += 0x0001010101010000L;
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                lowS += 0x0000000101010000L;
                                lowSW += 0x0000000001010000L;
                                lowW += 0x0000000001000000L;
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0000000101000000L;
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                lowNE += 0x0000010101000000L;
                                lowE += 0x0001010101010000L;
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                lowSE += 0x0101010101010101L;
                                lowS += 0x0000000101010101L;
                                lowSW += 0x0000000001010100L;
                                lowW += 0x0000000001010000L;
                                lowNW += 0x0000000001000000L;
                                lowC += 0x0000000101010000L;
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0000000101010000L;
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                lowNE += 0x0001010101010000L;
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                lowS += 0x0000000000010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                lowE += 0x0101010101010101L;
                                lowSE += 0x0101000000010101L;
                                lowS += 0x0000000000010101L;
                                lowSW += 0x0000000000010100L;
                                lowW += 0x0000000001010100L;
                                
                                lowC += 0x0000000101010101L;
                            }
                            case Direction.WEST -> {
                                lowN += 0x0000000101010000L;
                                
                                lowE += 0x0101010101010101L;
                                
                                lowS += 0x0000000000010101L;
                                lowSW += 0x0000000000010100L;
                                lowW += 0x0000000001010100L;
                                lowNW += 0x0000000001010000L;
                                lowC += 0x0000000101010101L;
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                lowNE += 0x0101010101010101L;
                                lowE += 0x0101000000010101L;
                                lowSE += 0x0100000000000101L;
                                lowS += 0x0000000000000101L;
                                lowSW += 0x0000000000000100L;
                                
                                
                                lowC += 0x0000000000010101L;
                            }
                            case Direction.SOUTHWEST -> {
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
                            case Direction.WEST -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                lowSW += 0x0000000000000100L;
                                lowW += 0x0000000000010100L;
                                lowNW += 0x0000000001010100L;
                                lowC += 0x0000000000010101L;
                            }
                            case Direction.NORTHWEST -> {
                                lowN += 0x0000000101010101L;
                                lowNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                lowNW += 0x0000000001010100L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                lowN += 0x0000000000010101L;
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                
                                lowC += 0x0000000000000101L;
                            }
                            case Direction.SOUTHWEST -> {
                                lowN += 0x0000000000010101L;
                                lowNE += 0x0101000000010101L;
                                lowE += 0x0100000000000101L;
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                lowNW += 0x0000000000010100L;
                                lowC += 0x0000000000000101L;
                            }
                            case Direction.WEST -> {
                                lowN += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                lowW += 0x0000000000000100L;
                                lowNW += 0x0000000000010100L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                lowE += 0x0000000101000000L;
                                lowSE += 0x0000000101010000L;
                                
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                lowSE += 0x0000000101010000L;
                                lowS += 0x0000000001010000L;
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                lowE += 0x0000000101000000L;
                                lowSE += 0x0000000101010000L;
                                lowS += 0x0000000001010000L;
                                
                                
                                
                                lowC += 0x0000000001000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                lowN += 0x0000000001000000L;
                                lowNE += 0x0000000101000000L;
                                lowE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                lowN += 0x0000000001000000L;
                                
                                lowE += 0x0000000101010000L;
                                lowSE += 0x0000000101010101L;
                                lowS += 0x0000000001010100L;
                                
                                
                                
                                lowC += 0x0000000001010000L;
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                lowNE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                lowSE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                lowE += 0x0000000101010101L;
                                lowSE += 0x0000000000010101L;
                                lowS += 0x0000000000010100L;
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            case Direction.WEST -> {
                                lowN += 0x0000000001010000L;
                                lowNE += 0x0000000101010000L;
                                lowE += 0x0000000101010101L;
                                lowSE += 0x0000000000010101L;
                                lowS += 0x0000000000010100L;
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            case Direction.NORTHWEST -> {
                                lowN += 0x0000000001010000L;
                                lowNE += 0x0000000101010000L;
                                lowE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000001010100L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                lowE += 0x0000000000010101L;
                                lowSE += 0x0000000000000101L;
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                lowE += 0x0000000000010101L;
                                lowSE += 0x0000000000000101L;
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                lowC += 0x0000000000010100L;
                            }
                            case Direction.WEST -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                lowE += 0x0000000000010101L;
                                
                                lowS += 0x0000000000000100L;
                                
                                
                                
                                lowC += 0x0000000000010100L;
                            }
                            case Direction.NORTHWEST -> {
                                lowN += 0x0000000001010100L;
                                lowNE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                lowNE += 0x0000000000010101L;
                                lowE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case Direction.SOUTHWEST -> {
                                lowN += 0x0000000000010100L;
                                lowNE += 0x0000000000010101L;
                                lowE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case Direction.WEST -> {
                                lowN += 0x0000000000010100L;
                                lowNE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                
                                lowC += 0x0000000000000100L;
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010100000000L;
                                highW += 0x0000010100000000L;
                                
                                highC += 0x0000010000000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                highS += 0x0001010000000000L;
                                highSW += 0x0001010100000000L;
                                highW += 0x0000010100000000L;
                                
                                highC += 0x0000010000000000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                highS += 0x0001010000000000L;
                                highSW += 0x0001010100000000L;
                                
                                
                                highC += 0x0000010000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0001010100000000L;
                                highNW += 0x0000010100000000L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                highW += 0x0001010100000000L;
                                highNW += 0x0000010100000000L;
                                highC += 0x0001010000000000L;
                            }
                            case Direction.EAST -> {
                                highN += 0x0000010000000000L;
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                highW += 0x0001010100000000L;
                                
                                highC += 0x0001010000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                highS += 0x0101010000000000L;
                                highSW += 0x0101010100000001L;
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0001010100000000L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0001010000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0101010100000001L;
                                highNW += 0x0001010100000000L;
                                highC += 0x0101010000000000L;
                            }
                            case Direction.EAST -> {
                                highN += 0x0001010000000000L;
                                
                                
                                
                                highS += 0x0101000000000000L;
                                highSW += 0x0101000000000001L;
                                highW += 0x0101010100000001L;
                                highNW += 0x0001010100000000L;
                                highC += 0x0101010000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                highS += 0x0101000000000000L;
                                highSW += 0x0101000000000001L;
                                highW += 0x0101010100000001L;
                                
                                highC += 0x0101010000000000L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000000001L;
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010100000001L;
                                
                            }
                            case Direction.EAST -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                highS += 0x0100000000000000L;
                                
                                highW += 0x0101000000000001L;
                                highNW += 0x0101010100000001L;
                                highC += 0x0101000000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                highN += 0x0101010000000000L;
                                
                                
                                
                                highS += 0x0100000000000000L;
                                highSW += 0x0100000000000001L;
                                highW += 0x0101000000000001L;
                                highNW += 0x0101010100000001L;
                                highC += 0x0101000000000000L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                highS += 0x0100000000000000L;
                                highSW += 0x0100000000000001L;
                                highW += 0x0101000000000001L;
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                highN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                highN += 0x0101000000000000L;
                                
                                
                                
                                
                                
                                highW += 0x0100000000000001L;
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000001L;
                                highNW += 0x0101000000000001L;
                                highC += 0x0100000000000000L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                highE += 0x0000010000000000L;
                                
                                highS += 0x0001010100000000L;
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                highC += 0x0000010100000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                highE += 0x0000010000000000L;
                                highSE += 0x0001010000000000L;
                                highS += 0x0001010100000000L;
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                highC += 0x0000010100000000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                highE += 0x0000010000000000L;
                                highSE += 0x0001010000000000L;
                                highS += 0x0001010100000000L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0001010101010000L;
                                highW += 0x0000010101000000L;
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0000010100000000L;
                                highNE += 0x0000010000000000L;
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                highC += 0x0001010100000000L;
                            }
                            case Direction.NORTHEAST -> {
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
                            case Direction.EAST -> {
                                
                                highNE += 0x0000010000000000L;
                                highE += 0x0001010000000000L;
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                
                                
                                highC += 0x0001010100000000L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                highSE += 0x0101010000000000L;
                                highS += 0x0101010100000001L;
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101010101010101L;
                                highW += 0x0001010101010000L;
                                highNW += 0x0000010101000000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0001010100000000L;
                                highNE += 0x0001010000000000L;
                                highE += 0x0101010000000000L;
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                highC += 0x0101010100000001L;
                            }
                            case Direction.EAST -> {
                                highN += 0x0001010100000000L;
                                highNE += 0x0001010000000000L;
                                highE += 0x0101010000000000L;
                                highSE += 0x0101000000000000L;
                                highS += 0x0101000000000001L;
                                
                                highW += 0x0101010101010101L;
                                
                                highC += 0x0101010100000001L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                highE += 0x0101010000000000L;
                                highSE += 0x0101000000000000L;
                                highS += 0x0101000000000001L;
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                highC += 0x0101010100000001L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                highS += 0x0101000000000001L;
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0101000000010101L;
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0101010101010101L;
                                highNW += 0x0001010101010000L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case Direction.EAST -> {
                                highN += 0x0101010100000001L;
                                highNE += 0x0101010000000000L;
                                highE += 0x0101000000000000L;
                                highSE += 0x0100000000000000L;
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case Direction.SOUTHEAST -> {
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
                            case Direction.SOUTH -> {
                                
                                
                                
                                highSE += 0x0100000000000000L;
                                highS += 0x0100000000000001L;
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                highC += 0x0101000000000001L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0100000000000101L;
                                highW += 0x0101000000010101L;
                                highNW += 0x0101010101010101L;
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0101010101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                highN += 0x0101000000000001L;
                                highNE += 0x0101000000000000L;
                                highE += 0x0100000000000000L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                highN += 0x0101000000000001L;
                                highNE += 0x0101000000000000L;
                                highE += 0x0100000000000000L;
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                highC += 0x0100000000000001L;
                            }
                            case Direction.SOUTH -> {
                                highN += 0x0101000000000001L;
                                
                                highE += 0x0100000000000000L;
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                highC += 0x0100000000000001L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                highW += 0x0100000000000101L;
                                highNW += 0x0101000000010101L;
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                highE += 0x0000010100000000L;
                                highSE += 0x0001010100000000L;
                                highS += 0x0001010101010000L;
                                highSW += 0x0000000101010000L;
                                highW += 0x0000000101000000L;
                                
                                highC += 0x0000010101000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                highE += 0x0000010100000000L;
                                highSE += 0x0001010100000000L;
                                highS += 0x0001010101010000L;
                                
                                
                                
                                highC += 0x0000010101000000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                highSE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000000101010000L;
                                
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                highS += 0x0001010101010000L;
                                highSW += 0x0000000101010000L;
                                highW += 0x0000000101000000L;
                                
                                highC += 0x0000010101000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0000010101000000L;
                                highNE += 0x0000010100000000L;
                                highE += 0x0001010100000000L;
                                
                                highS += 0x0101010101010101L;
                                
                                highW += 0x0000000101010000L;
                                highNW += 0x0000000101000000L;
                                highC += 0x0001010101010000L;
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0000010101000000L;
                                highNE += 0x0000010100000000L;
                                highE += 0x0001010100000000L;
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                highC += 0x0001010101010000L;
                            }
                            case Direction.EAST -> {
                                
                                
                                highE += 0x0001010100000000L;
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                highSE += 0x0101010100000001L;
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                highS += 0x0101010101010101L;
                                highSW += 0x0000000101010101L;
                                highW += 0x0000000101010000L;
                                
                                
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0001010101010000L;
                                highNE += 0x0001010100000000L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0001010101010000L;
                                highNE += 0x0001010100000000L;
                                highE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case Direction.EAST -> {
                                
                                highNE += 0x0001010100000000L;
                                highE += 0x0101010100000001L;
                                highSE += 0x0101000000000001L;
                                
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                highE += 0x0101010100000001L;
                                highSE += 0x0101000000000001L;
                                highS += 0x0101000000010101L;
                                
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                highSE += 0x0101000000000001L;
                                highS += 0x0101000000010101L;
                                highSW += 0x0000000000010101L;
                                
                                
                                highC += 0x0101010101010101L;
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                highS += 0x0101000000010101L;
                                highSW += 0x0000000000010101L;
                                highW += 0x0000000101010101L;
                                
                                highC += 0x0101010101010101L;
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                highSW += 0x0000000000010101L;
                                highW += 0x0000000101010101L;
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            case Direction.NORTHWEST -> {
                                highN += 0x0001010101010000L;
                                
                                
                                
                                
                                
                                highW += 0x0000000101010101L;
                                highNW += 0x0000000101010000L;
                                highC += 0x0101010101010101L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                highE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                highN += 0x0101010101010101L;
                                highNE += 0x0101010100000001L;
                                highE += 0x0101000000000001L;
                                highSE += 0x0100000000000001L;
                                highS += 0x0100000000000101L;
                                
                                
                                
                                highC += 0x0101000000010101L;
                            }
                            case Direction.SOUTH -> {
                                highN += 0x0101010101010101L;
                                
                                highE += 0x0101000000000001L;
                                highSE += 0x0100000000000001L;
                                highS += 0x0100000000000101L;
                                highSW += 0x0000000000000101L;
                                highW += 0x0000000000010101L;
                                
                                highC += 0x0101000000010101L;
                            }
                            case Direction.SOUTHWEST -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                highS += 0x0100000000000101L;
                                highSW += 0x0000000000000101L;
                                highW += 0x0000000000010101L;
                                highNW += 0x0000000101010101L;
                                highC += 0x0101000000010101L;
                            }
                            case Direction.WEST -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000010101L;
                                highNW += 0x0000000101010101L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                highN += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000101010101L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                highNE += 0x0101000000000001L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                highN += 0x0101000000010101L;
                                highNE += 0x0101000000000001L;
                                highE += 0x0100000000000001L;
                                
                                
                                
                                
                                
                                highC += 0x0100000000000101L;
                            }
                            case Direction.SOUTH -> {
                                highN += 0x0101000000010101L;
                                highNE += 0x0101000000000001L;
                                highE += 0x0100000000000001L;
                                
                                
                                
                                highW += 0x0000000000000101L;
                                highNW += 0x0000000000010101L;
                                highC += 0x0100000000000101L;
                            }
                            case Direction.SOUTHWEST -> {
                                highN += 0x0101000000010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000000101L;
                                highNW += 0x0000000000010101L;
                                highC += 0x0100000000000101L;
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                
                                
                                
                                highNW += 0x0000000000010101L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                highS += 0x0000000101010000L;
                                
                                highW += 0x0000000001000000L;
                                
                                highC += 0x0000000101000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                highE += 0x0000010101000000L;
                                highSE += 0x0001010101010000L;
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                
                                highS += 0x0000000101010000L;
                                highSW += 0x0000000001010000L;
                                highW += 0x0000000001000000L;
                                
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0000000101000000L;
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                highNE += 0x0000010101000000L;
                                highE += 0x0001010101010000L;
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                highSE += 0x0101010101010101L;
                                highS += 0x0000000101010101L;
                                highSW += 0x0000000001010100L;
                                highW += 0x0000000001010000L;
                                highNW += 0x0000000001000000L;
                                highC += 0x0000000101010000L;
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0000000101010000L;
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                highNE += 0x0001010101010000L;
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                highE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                highS += 0x0000000000010101L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                highE += 0x0101010101010101L;
                                highSE += 0x0101000000010101L;
                                highS += 0x0000000000010101L;
                                highSW += 0x0000000000010100L;
                                highW += 0x0000000001010100L;
                                
                                highC += 0x0000000101010101L;
                            }
                            case Direction.WEST -> {
                                highN += 0x0000000101010000L;
                                
                                highE += 0x0101010101010101L;
                                
                                highS += 0x0000000000010101L;
                                highSW += 0x0000000000010100L;
                                highW += 0x0000000001010100L;
                                highNW += 0x0000000001010000L;
                                highC += 0x0000000101010101L;
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                highNE += 0x0101010101010101L;
                                highE += 0x0101000000010101L;
                                highSE += 0x0100000000000101L;
                                highS += 0x0000000000000101L;
                                highSW += 0x0000000000000100L;
                                
                                
                                highC += 0x0000000000010101L;
                            }
                            case Direction.SOUTHWEST -> {
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
                            case Direction.WEST -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                highSW += 0x0000000000000100L;
                                highW += 0x0000000000010100L;
                                highNW += 0x0000000001010100L;
                                highC += 0x0000000000010101L;
                            }
                            case Direction.NORTHWEST -> {
                                highN += 0x0000000101010101L;
                                highNE += 0x0101010101010101L;
                                
                                
                                
                                
                                
                                highNW += 0x0000000001010100L;
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                highN += 0x0000000000010101L;
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                highW += 0x0000000000000100L;
                                
                                highC += 0x0000000000000101L;
                            }
                            case Direction.SOUTHWEST -> {
                                highN += 0x0000000000010101L;
                                highNE += 0x0101000000010101L;
                                highE += 0x0100000000000101L;
                                
                                
                                
                                highW += 0x0000000000000100L;
                                highNW += 0x0000000000010100L;
                                highC += 0x0000000000000101L;
                            }
                            case Direction.WEST -> {
                                highN += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                highW += 0x0000000000000100L;
                                highNW += 0x0000000000010100L;
                                
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                highE += 0x0000000101000000L;
                                highSE += 0x0000000101010000L;
                                
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                
                                
                                
                                highSE += 0x0000000101010000L;
                                highS += 0x0000000001010000L;
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                highE += 0x0000000101000000L;
                                highSE += 0x0000000101010000L;
                                highS += 0x0000000001010000L;
                                
                                
                                
                                highC += 0x0000000001000000L;
                            }
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                highN += 0x0000000001000000L;
                                highNE += 0x0000000101000000L;
                                highE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                
                            }
                            case Direction.WEST -> {
                                highN += 0x0000000001000000L;
                                
                                highE += 0x0000000101010000L;
                                highSE += 0x0000000101010101L;
                                highS += 0x0000000001010100L;
                                
                                
                                
                                highC += 0x0000000001010000L;
                            }
                            case Direction.NORTHWEST -> {
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
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                highNE += 0x0000000101010000L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                
                                highSE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                
                                
                                highE += 0x0000000101010101L;
                                highSE += 0x0000000000010101L;
                                highS += 0x0000000000010100L;
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            case Direction.WEST -> {
                                highN += 0x0000000001010000L;
                                highNE += 0x0000000101010000L;
                                highE += 0x0000000101010101L;
                                highSE += 0x0000000000010101L;
                                highS += 0x0000000000010100L;
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            case Direction.NORTHWEST -> {
                                highN += 0x0000000001010000L;
                                highNE += 0x0000000101010000L;
                                highE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000001010100L;
                            }
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                
                                highE += 0x0000000000010101L;
                                highSE += 0x0000000000000101L;
                                highS += 0x0000000000000100L;
                                
                                
                                
                                
                            }
                            case Direction.SOUTHWEST -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                highE += 0x0000000000010101L;
                                highSE += 0x0000000000000101L;
                                highS += 0x0000000000000100L;
                                
                                
                                
                                highC += 0x0000000000010100L;
                            }
                            case Direction.WEST -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                highE += 0x0000000000010101L;
                                
                                highS += 0x0000000000000100L;
                                
                                
                                
                                highC += 0x0000000000010100L;
                            }
                            case Direction.NORTHWEST -> {
                                highN += 0x0000000001010100L;
                                highNE += 0x0000000101010101L;
                                
                                
                                
                                
                                
                                
                                
                            }
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.NORTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.EAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTHEAST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            }
                            case Direction.SOUTH -> {
                                
                                highNE += 0x0000000000010101L;
                                highE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case Direction.SOUTHWEST -> {
                                highN += 0x0000000000010100L;
                                highNE += 0x0000000000010101L;
                                highE += 0x0000000000000101L;
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case Direction.WEST -> {
                                highN += 0x0000000000010100L;
                                highNE += 0x0000000000010101L;
                                
                                
                                
                                
                                
                                
                                highC += 0x0000000000000100L;
                            }
                            case Direction.NORTHWEST -> {
                                
                                
                                
                                
                                
                                
                                
                                
                                
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
        return switch (moveDir) {
            case Direction.NORTH -> lowN;
            case Direction.NORTHEAST -> lowNE;
            case Direction.EAST -> lowE;
            case Direction.SOUTHEAST -> lowSE;
            case Direction.SOUTH -> lowS;
            case Direction.SOUTHWEST -> lowSW;
            case Direction.WEST -> lowW;
            case Direction.NORTHWEST -> lowNW;
            case Direction.CENTER -> lowC;
            default -> 0L;
        };
    }

    private static long getHighMask(Direction moveDir) {
        return switch (moveDir) {
            case Direction.NORTH -> highN;
            case Direction.NORTHEAST -> highNE;
            case Direction.EAST -> highE;
            case Direction.SOUTHEAST -> highSE;
            case Direction.SOUTH -> highS;
            case Direction.SOUTHWEST -> highSW;
            case Direction.WEST -> highW;
            case Direction.NORTHWEST -> highNW;
            case Direction.CENTER -> highC;
            default -> 0L;
        };
    }

    private static long getMoveThreatMask(Direction moveDir) {
        return switch (moveDir) {
            case Direction.NORTH -> moveThreatN;
            case Direction.NORTHEAST -> moveThreatNE;
            case Direction.EAST -> moveThreatE;
            case Direction.SOUTHEAST -> moveThreatSE;
            case Direction.SOUTH -> moveThreatS;
            case Direction.SOUTHWEST -> moveThreatSW;
            case Direction.WEST -> moveThreatW;
            case Direction.NORTHWEST -> moveThreatNW;
            case Direction.CENTER -> moveThreatC;
            default -> 0L;
        };
    }

    private static long getHiddenMask(Direction facing) {
        return switch (facing) {
            case Direction.NORTH -> hiddenN;
            case Direction.NORTHEAST -> hiddenNE;
            case Direction.EAST -> hiddenE;
            case Direction.SOUTHEAST -> hiddenSE;
            case Direction.SOUTH -> hiddenS;
            case Direction.SOUTHWEST -> hiddenSW;
            case Direction.WEST -> hiddenW;
            case Direction.NORTHWEST -> hiddenNW;
            default -> 0L;
        };
    }

    private static long enemyMaskForMove(int dx, int dy, Direction facing, Direction moveDir) {
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
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
            case -1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010100000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010100000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010100000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010100000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010100000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010100000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010100000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010100000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000001L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
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
            case 0 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010101000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010101000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000010101000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010101010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010100000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010101010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0001010101010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010100000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101010101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010100000001L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0101000000010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000000001L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000001L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0100000000000101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
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
            case 1 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000010101000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0001010101010000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000101010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000010101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101010101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0101000000010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0100000000000101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000101L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
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
            case 2 -> {
                switch (dy) {
                    case -2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case -1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001010000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 0 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001010100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001010100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001010000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101010000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000001010100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 1 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000010100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000100L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000010100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000001010100L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000101010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            default -> { return 0L; }
                        }
                    }
                    case 2 -> {
                        switch (facing) {
                            case Direction.NORTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.EAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHEAST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTH -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.SOUTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000101L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.WEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000010100L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000010101L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000100L;
                                    }
                                    default -> { return 0L; }
                                }
                            }
                            case Direction.NORTHWEST -> {
                                switch (moveDir) {
                                    case Direction.NORTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.EAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHEAST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTH -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.SOUTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.WEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.NORTHWEST -> {
                                        return 0x0000000000000000L;
                                    }
                                    case Direction.CENTER -> {
                                        return 0x0000000000000000L;
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