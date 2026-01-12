package awubot.util;

import battlecode.common.*;
import static awubot.Game.*;

import java.util.*;
import static java.lang.Math.*;

public class Util {
    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static final Direction[] allDirections = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
        Direction.CENTER
    };

    public static final Direction[] orthDirections = {
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST
    };

    public static void println(Object... os) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : os) {
            sb.append(obj.toString());
            sb.append(' ');
        }
        System.out.println(sb.toString());
    }

    public static void preTick() {
        indicatorString = "";
    }

    public static boolean canSense(MapLocation s, MapLocation t, Direction dir) {
        int dx = t.x - s.x;
        int dy = t.y - s.y;
        if (dx * dx + dy * dy > 20) return false;
        int key = dx + 20 * dy;
        switch (dir) {
            case NORTH -> {
                switch (key) {
                    case 0, 19, 20, 21, 38, 39, 40, 41, 42, 57, 58, 59, 60, 61, 62, 63, 78, 79, 80, 81, 82 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case SOUTH -> {
                switch (key) {
                    case 0, -82, -81, -80, -79, -78, -63, -62, -61, -60, -59, -58, -57, -42, -41, -40, -39, -38, -21, -20, -19 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case EAST -> {
                switch (key) {
                    case 0, -57, -38, -37, -36, -19, -18, -17, -16, 1, 2, 3, 4, 21, 22, 23, 24, 42, 43, 44, 63 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case WEST -> {
                switch (key) {
                    case 0, -63, -44, -43, -42, -24, -23, -22, -21, -4, -3, -2, -1, 16, 17, 18, 19, 36, 37, 38, 57 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case NORTHEAST -> {
                switch (key) {
                    case 0, 1, 2, 3, 4, 20, 21, 22, 23, 24, 40, 41, 42, 43, 44, 60, 61, 62, 63, 80, 81, 82 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case NORTHWEST -> {
                switch (key) {
                    case 0, -4, -3, -2, -1, 16, 17, 18, 19, 20, 36, 37, 38, 39, 40, 57, 58, 59, 60, 78, 79, 80 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case SOUTHEAST -> {
                switch (key) {
                    case 0, -80, -79, -78, -60, -59, -58, -57, -40, -39, -38, -37, -36, -20, -19, -18, -17, -16, 1, 2, 3, 4 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case SOUTHWEST -> {
                switch (key) {
                    case 0, -82, -81, -80, -63, -62, -61, -60, -44, -43, -42, -41, -40, -24, -23, -22, -21, -20, -4, -3, -2, -1 -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
            default -> {
                return false;
            }
        }
    }

    public static Direction[] relatedDirections(Direction dir) {
        if (dir == Direction.NORTH) {
            return new Direction[]{dir, Direction.NORTHEAST, Direction.NORTHWEST};
        } else if (dir == Direction.SOUTH) {
            return new Direction[]{dir, Direction.SOUTHEAST, Direction.SOUTHWEST};
        } else if (dir == Direction.EAST) {
            return new Direction[]{dir, Direction.NORTHEAST, Direction.SOUTHEAST};
        } else if (dir == Direction.WEST) {
            return new Direction[]{dir, Direction.NORTHWEST, Direction.SOUTHWEST};
        } else {
            return new Direction[]{dir};
        }
    }

    public static MapLocation decode2x2(int pos) {
        return new MapLocation((pos & 0b11111) << 1, (pos >>> 5) << 1);
    }

    public static int encode2x2(MapLocation loc) {
        return (loc.x >>> 1) + ((loc.y >>> 1) << 5);
    }

    public static int encode2x2(int myLocX, int myLocY) {
        return (myLocX >>> 1) + ((myLocY >>> 1) << 5);
    }

    public static int encode2x2() {
        return (myLocX >>> 1) + ((myLocY >>> 1) << 5);
    }

    public static int writeBits(int bits, int start, int[] data, int[] lens) {
        int off = start;
        assert data.length == lens.length;
        for (int i = 0; i < data.length; i++) {
            bits |= (data[i] << off);
            off += lens[i];
        }
        return bits;
    }

    public static int[] getBits(int bits, int[] segs) {
        bits >>>= segs[0];
        int[] ret = new int[segs.length - 1];
        for (int i = 0; i < segs.length - 1; i++) {
            ret[i] = (bits & ((1 << segs[i + 1]) - 1));
            bits >>= segs[i + 1];
        }
        return ret;
    }

    public static int getBits(int bits, int l, int len) {
        int msk = (1 << len) - 1;
        msk <<= l;
        return (bits & msk) >>> l;
    }

    public static int writeBits(int bits, int data, int st) {
        return bits | (data << st);
    }

    public static MapLocation flipHor(MapLocation loc) {
        return new MapLocation(mapWidth - 1 - loc.x, loc.y);
    }

    public static MapLocation flipVer(MapLocation loc) {
        return new MapLocation(loc.x, mapHeight - 1 - loc.y);
    }

    public static MapLocation rot180(MapLocation loc) {
        return new MapLocation(mapWidth - 1 - loc.x, mapHeight - 1 - loc.y);
    }

    // for debugging purposes
    public static String indicatorString = "";
    public static void indicate(Object o) {
        indicatorString += o + "\n";
        rc.setIndicatorString(indicatorString);
    }

    public static int manhattan(MapLocation a, MapLocation b) {
        return abs(a.x - b.x) + abs(a.y - b.y);
    }

    final static int SIZE = 60;
    public static MapLocation decodeLoc(int code) {
        // assert code < mapWidth * SIZE : code + " is not a valid map code " + rc.getID();
        return new MapLocation(code / SIZE, code % SIZE);
    }

    public static int encodeLoc(MapLocation loc) {
        // assert 0 <= loc.x && loc.x < SIZE && 0 <= loc.y && loc.y < SIZE;
        return loc.x * SIZE + loc.y;
    }

    public static void dead(Object msg) {
        System.out.println("Robot " + rc.getID() + " hit a dead end: " + msg);
        rc.disintegrate();
    }

    // public static Pair<Double, Double> relativeDistsToCentre(MapLocation loc) {
    //     double relX = (double) loc.x / (double) mapWidth;
    //     double relY = (double) loc.y / (double) mapHeight;
    //     return new Pair(abs(relX - 0.5), abs(relY - 0.5));
    // }

    // public static List<MapLocation> guessEnemyLocs(MapLocation src, boolean closestFirst) throws GameActionException {
    //     List<MapLocation> enemyLocs = new LinkedList<>();

    //     if (src == null) {
    //         // give 3 CustomRandom locations
    //         for (int i = 0; i < 3; i++) {
    //             enemyLocs.addLast(genExplorationTarget(trng));
    //         }
    //         return enemyLocs;
    //     }


    //     // also make the path as easy as possible between targets
    //     int dist_hormiddle = Math.abs(src.x - Game.mapWidth / 2);
    //     int dist_vertmiddle = Math.abs(src.y - Game.mapHeight / 2);
    //     if(closestFirst) {
    //         dist_hormiddle *= -1;
    //         dist_vertmiddle *= -1;
    //     }
    //     if (dist_hormiddle > dist_vertmiddle) {
    //         // first is hor
    //         enemyLocs.addLast(flipHor(src));

    //         int d1 = flipHor(src).distanceSquaredTo(rot180(src)) + rot180(src).distanceSquaredTo(flipVer(src));
    //         int d2 = flipHor(src).distanceSquaredTo(flipVer(src)) + flipVer(src).distanceSquaredTo(rot180(src));

    //         if (d1 < d2) {
    //             enemyLocs.addLast(rot180(src));
    //             enemyLocs.addLast(flipVer(src));
    //         } else {
    //             enemyLocs.addLast(flipVer(src));
    //             enemyLocs.addLast(rot180(src));
    //         }

    //     // } else if (dist_hormiddle < dist_vertmiddle) {
    //     } else {
    //         // first is vert ref
    //         enemyLocs.addLast(flipVer(src));

    //         int d1 = flipVer(src).distanceSquaredTo(rot180(src)) + rot180(src).distanceSquaredTo(flipHor(src));
    //         int d2 = flipVer(src).distanceSquaredTo(flipHor(src)) + flipHor(src).distanceSquaredTo(rot180(src));

    //         if (d1 < d2) {
    //             enemyLocs.addLast(rot180(src));
    //             enemyLocs.addLast(flipHor(src));
    //         } else {
    //             enemyLocs.addLast(flipHor(src));
    //             enemyLocs.addLast(rot180(src));
    //         }

    //     // } else {
    //     //     // first is rot 180
    //     //     enemyLocs.addLast(rot180(src));

    //     //     int d1 = rot180(src).distanceSquaredTo(flipHor(src)) + flipHor(src).distanceSquaredTo(flipVer(src));
    //     //     int d2 = rot180(src).distanceSquaredTo(flipVer(src)) + flipVer(src).distanceSquaredTo(flipHor(src));

    //     //     if (d1 < d2) {
    //     //         enemyLocs.addLast(flipHor(src));
    //     //         enemyLocs.addLast(flipVer(src));
    //     //     } else {
    //     //         enemyLocs.addLast(flipVer(src));
    //     //         enemyLocs.addLast(flipHor(src));
    //     //     }
    //     }
    //     return enemyLocs;
    // }
    // public static List<MapLocation> guessEnemyLocs(MapLocation src) throws GameActionException {
    //     return guessEnemyLocs(src, false);
    // }

    // public static int expectedRushDistance(MapLocation src) throws GameActionException {
    //     List<MapLocation> enemyLocs = guessEnemyLocs(src, true);
    //     MapLocation target = enemyLocs.get(0);
    //     return (int)Math.sqrt((double)src.distanceSquaredTo(target));
    // }

    // public static List<MapLocation> guessSpawnLocs() throws GameActionException {
    //     return guessEnemyLocs(origin);
    // }

    public static int chebyshevDistance(MapLocation a, MapLocation b) {
        return max(abs(a.x - b.x), abs(a.y - b.y));
    }

    public static boolean isOccupied(MapLocation loc) throws GameActionException {
        return rc.senseRobotAtLocation(loc) != null;
    }

    // public static boolean isAllyAgent(RobotInfo bot) {
    //     return isFriendly(bot) && bot.getType().isRobotType();
    // }

    // public static MapInfo getClosestNeighbourTo(MapLocation dest, GamePredicate<MapInfo> pred) throws GameActionException {
    //     MapInfo best = null;
    //     for (Direction dir : directions) {
    //         if (dir == Direction.CENTER) continue;
    //         MapLocation newLoc = myLoc.add(dir);
    //         MapInfo newInfo = rc.senseMapInfo(newLoc);
    //         if (pred.test(newInfo)) {
    //             if (best == null || best.getMapLocation().distanceSquaredTo(dest) > newLoc.distanceSquaredTo(dest)) {
    //                 best = newInfo;
    //             }
    //         }
    //     }
    //     return best;
    // }

    // public static boolean canMove(MapLocation loc) throws GameActionException {
    //     Direction dir = getDiff(myLoc, loc);
    //     return rc.canMove(dir);
    // }

    // public static Direction getClosestDirTo(MapLocation dest, GamePredicate<MapInfo> pred) throws GameActionException {
    //     Direction best = null;
    //     for (Direction dir : directions) {
    //         if (dir == Direction.CENTER) continue;
    //         MapLocation newLoc = myLoc.add(dir);
    //         if (rc.onTheMap(newLoc)) {
    //             MapInfo newInfo = rc.senseMapInfo(newLoc);
    //             if (pred.test(newInfo)) {
    //                 if (best == null || myLoc.add(best).distanceSquaredTo(dest) > newLoc.distanceSquaredTo(dest)) {
    //                     best = dir;
    //                 }
    //             }
    //         }
    //     }
    //     return best;
    // }

    public static Direction getDiff(MapLocation src, MapLocation dest) throws GameActionException {
        int dx = dest.x - src.x;
        int dy = dest.y - src.y;
        for (Direction dir : directions) {
            if (dir.getDeltaX() == dx && dir.getDeltaY() == dy) {
                return dir;
            }
        }
        return null;
    }

    // public static boolean isInAttackRange(MapLocation loc) {
    //     return loc.distanceSquaredTo(myLoc) <= rc.getType().actionRadiusSquared;
    // }

    // public static boolean isEnemyAgent(RobotInfo info) {
    //     return info.getType().isRobotType() && !isFriendly(info);
    // }

    // public static void println(Object obj) {
    //     if (time > 100) return;
    //     System.out.println(obj);
    // }

    // public static MapInfo getClosestCellTo(MapLocation dest, GamePredicate<MapInfo> pred) throws GameActionException {
    //     MapInfo best = null;
    //     for (MapInfo newLoc : rc.senseNearbyMapInfos()) {
    //         if (pred.test(newLoc)) {
    //             if (best == null || best.getMapLocation().distanceSquaredTo(dest) > newLoc.getMapLocation().distanceSquaredTo(dest)) {
    //                 best = newLoc;
    //             }
    //         }
    //     }
    //     return best;
    // }

    // public static int countNearbyBots(GamePredicate<RobotInfo> pred) throws GameActionException {
    //     int res = 0;
    //     for (RobotInfo bot : rc.senseNearbyRobots()) {
    //         if (pred.test(bot)) {
    //             res++;
    //         }
    //     }
    //     return res;
    // }

    public static MapLocation add(MapLocation a, MapLocation b) {
        return new MapLocation(a.x + b.x, a.y + b.y);
    }

    public static MapLocation subtract(MapLocation a, MapLocation b) {
        return new MapLocation(a.x - b.x, a.y - b.y);
    }

    public static MapLocation rotr(MapLocation vec) {
        return new MapLocation(vec.y, -vec.x);
    }

    public static MapLocation rotl(MapLocation vec) {
        return new MapLocation(-vec.y, vec.x);
    }

    public static MapLocation scale(MapLocation vec, double amt) {
        return new MapLocation((int) (vec.x * amt), (int) (vec.y * amt));
    }

    // public static boolean isFriendly(RobotInfo info) {
    //     return team.equals(info.getTeam());
    // }

    public static MapLocation project(MapLocation cur, MapLocation moveVec, double maxDist) {
        double dx = moveVec.x;
        double dy = moveVec.y;

        double magnitude = Math.sqrt(dx * dx + dy * dy);
        dx /= magnitude;
        dy /= magnitude;

        double tMin = maxDist;

        if (dx < 0) {
            double t = -cur.x / dx;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        if (dx > 0) {
            double t = (mapWidth - 1 - cur.x) / dx;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        if (dy < 0) {
            double t = -cur.y / dy;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        if (dy > 0) {
            double t = (mapHeight - 1 - cur.y) / dy;
            if (t >= 0) tMin = Math.min(tMin, t);
        }

        int x = (int) Math.round(cur.x + tMin * dx);
        int y = (int) Math.round(cur.y + tMin * dy);

        x = Math.max(0, Math.min(mapWidth - 1, x));
        y = Math.max(0, Math.min(mapHeight - 1, y));

        return new MapLocation(x, y);
    }

    public static MapLocation project(MapLocation cur, MapLocation moveVec) {
        return project(cur, moveVec, Double.MAX_VALUE);
    }

    public static MapLocation getOpposite(MapLocation cur) {
        MapLocation centre = new MapLocation(mapWidth / 2, mapHeight / 2);
        int dx = centre.x - cur.x;
        int dy = centre.y - cur.y;
        return project(cur, new MapLocation(dx, dy));
    }
}
