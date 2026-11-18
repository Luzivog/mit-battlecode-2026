package opp_powerpuff.Managers;

import battlecode.common.*;
import battlecode.common.TrapType;
import opp_powerpuff.Rat;
import opp_powerpuff.Utils.RNG;

public class MapData {
    public static MapInfo[][] mapInfos;

    // VERTICAL BIT || HORIZONTAL BIT || ROTATIONAL BIT
    public static int symmetry = 7;

    public static void init() {
        mapInfos = new MapInfo[Rat.W][Rat.H];
    }

    public static void update(MapInfo[] sensedInfos) throws GameActionException {
        int globalSymm = Global.readSymmetry();
        if (symmetry != globalSymm) {
            symmetry &= globalSymm;
        }
        for (MapInfo m: sensedInfos) {
            MapLocation loc = m.getMapLocation();
            mapInfos[loc.x][loc.y] = m;
        }
    }

    public static void updateSymmmetry(MapInfo[] infos) throws GameActionException {
        if (symmetry == 1 || symmetry == 2 || symmetry == 4) {
            if (symmetry == 1) {
                for (MapInfo mapInfo: infos) {
                    if (mapInfo.isWall()) {
                        int x = mapInfo.getMapLocation().x;
                        int y = mapInfo.getMapLocation().y;    
                        mapInfos[Rat.W - 1 - x][y] = new MapInfo(
                            new MapLocation(Rat.W - 1 - x, y), 
                            false, null, true, false, 0, TrapType.NONE, false
                        );
                    }
                }
            } else if (symmetry == 2) {
                for (MapInfo mapInfo: infos) {
                    if (mapInfo.isWall()) {
                        int x = mapInfo.getMapLocation().x;
                        int y = mapInfo.getMapLocation().y;    
                        mapInfos[Rat.W - 1 - x][y] = new MapInfo(
                            new MapLocation(x, Rat.H - 1 - y), 
                            false, null, true, false, 0, TrapType.NONE, false
                        );
                    }
                }
            } else if (symmetry == 4) {
                for (MapInfo mapInfo: infos) {
                    if (mapInfo.isWall()) {
                        int x = mapInfo.getMapLocation().x;
                        int y = mapInfo.getMapLocation().y;    
                        mapInfos[Rat.W - 1 - x][Rat.H - 1 - y] = new MapInfo(
                            new MapLocation(x, Rat.H - 1 - y), 
                            false, null, true, false, 0, TrapType.NONE, false
                        );
                    }
                }
            }
        } else {
            for (MapInfo mapInfo: infos) {
                if (
                    Rat.W % 2 == 1 && Rat.H % 2 == 1 && 
                    mapInfo.getMapLocation().x == Rat.W / 2 &&
                    mapInfo.getMapLocation().y == Rat.H / 2
                ) continue;
                if ((symmetry & (1 << 0)) != 0) {
                    int res = checkVerticalSymmetry(mapInfo);
                    if (res == 1) symmetry &= ~(1 << 0);
                }
                if ((symmetry & (1 << 1)) != 0) {
                    int res = checkHorizontalSymmetry(mapInfo);
                    if (res == 1) symmetry &= ~(1 << 1);
                }

                if ((symmetry & (1 << 2)) != 0) {
                    int res = checkRotationalSymmetry(mapInfo);
                    if (res == 1) symmetry &= ~(1 << 2);
                }
            }
        }

    }

    public static MapInfo getMapInfo(MapLocation loc) {
        return mapInfos[loc.x][loc.y];
    }

    public static int reflectionCount() {
        return Integer.bitCount(symmetry);
    }

    public static MapLocation[] possibleReflections(MapLocation loc) throws GameActionException {
        MapLocation[] res = new MapLocation[3];
        if ((symmetry & (1 << 0)) != 0)
            res[0] = singleReflection(1, loc);
        else
            res[0] = null;
        if ((symmetry & (1 << 1)) != 0)
            res[1] = singleReflection(2, loc);
        else
            res[1] = null;
        if ((symmetry & (1 << 2)) != 0)
            res[2] = singleReflection(4, loc);
        else
            res[2] = null;
        return res;
    }

    public static MapLocation nearestReflectionIncludingSelf(MapLocation origin, MapLocation reflectionPoint) throws GameActionException {
        MapLocation[] allReflections = possibleReflections(reflectionPoint);
        MapLocation nearestReflection = reflectionPoint;
        for (int i = 0; i < 3; i++) {
            if (allReflections[i] != null) {
                if (nearestReflection == null) nearestReflection = allReflections[i];
                else if (
                    origin.distanceSquaredTo(allReflections[i]) < origin.distanceSquaredTo(nearestReflection)
                ) {
                    nearestReflection = allReflections[i];
                }
            }
        }
        return nearestReflection;
    }

    public static MapLocation nearReflectionToAllyKingIncludingSelf(MapLocation loc) throws GameActionException {
        MapLocation[] allReflections = possibleReflections(loc);
        MapLocation nearestReflection = loc;
        for (int i = 0; i < 3; i++) {
            if (allReflections[i] != null) {
                if (
                    Rat.distTo(loc) < Rat.distTo(nearestReflection)
                ) {
                    nearestReflection = allReflections[i];
                }
            }
        } 
        return nearestReflection;
    }

    public static MapLocation priorityReflection(MapLocation loc) throws GameActionException {
        MapLocation[] allReflections = MapData.possibleReflections(loc);
        int chance = RNG.rng.nextInt(100);
        if (chance < 50 && allReflections[2] != null)
            return allReflections[2];
        if (Rat.W < Rat.H && allReflections[1] != null)
            return allReflections[1];
        if (Rat.W > Rat.H && allReflections[0] != null)
            return allReflections[0];
        if (chance < 75 && allReflections[1] != null)
            return allReflections[1];
        if (allReflections[0] != null)
            return allReflections[0];
        return allReflections[2];
    }

    public static MapLocation existingReflection(MapLocation loc) throws GameActionException {
        MapLocation[] allReflections = possibleReflections(loc);
        int idx = RNG.rng.nextInt(3);
        for (int i = 0; i < 3; i++) {
            if (allReflections[(idx + i) % 3] != null)
                return allReflections[(idx + i) % 3];
        }
        return null;
    }

    public static MapLocation existingReflectionIncludingSelf(MapLocation loc) throws GameActionException {
        int amtExisting = reflectionCount();
        int idx = RNG.rng.nextInt(amtExisting + 1);
        if (idx == amtExisting) return loc;
        MapLocation[] allReflections = possibleReflections(loc);
        for (int i = 0; i < 3; i++) {
            if (allReflections[(i + idx) % 3] != null)
                return allReflections[(i + idx) % 3];
        }
        return null;
    }

    public static MapLocation singleReflection(int symmetry, MapLocation loc) throws GameActionException {
        int x = loc.x; int y = loc.y;
        if (symmetry == 1)
            return new MapLocation(Rat.W - 1 - x, y);
        if (symmetry == 2)
            return new MapLocation(x, Rat.H - 1 - y);
        if (symmetry == 4)
            return new MapLocation(Rat.W - 1 -x, Rat.H - 1 - y);
        return null;
    }

    public static int checkVerticalSymmetry(MapInfo mapInfo) {
        MapLocation loc = mapInfo.getMapLocation();
        int x = loc.x; int y = loc.y;
        MapInfo vertInfo = mapInfos[Rat.W - 1 - x][y];
        return gradeInfo(mapInfo, vertInfo);
    }

    public static int checkHorizontalSymmetry(MapInfo mapInfo) {
        MapLocation loc = mapInfo.getMapLocation();
        int x = loc.x; int y = loc.y;
        MapInfo horInfo = mapInfos[x][Rat.H - 1 - y];
        return gradeInfo(mapInfo, horInfo);
    }

    public static int checkRotationalSymmetry(MapInfo mapInfo) {
        MapLocation loc = mapInfo.getMapLocation();
        int x = loc.x; int y = loc.y;
        MapInfo rotInfo = mapInfos[Rat.W - 1 - x][Rat.H - 1 - y];
        return gradeInfo(mapInfo, rotInfo);
    }

    /*
    INCONCLUSIVE -1
    YES 0
    NO 1
    */
    private static int gradeInfo(MapInfo original, MapInfo reflected) {
        if (reflected == null) return -1;
        if(original.isWall() == reflected.isWall() && original.hasCheeseMine() == reflected.hasCheeseMine()) return 0;
        return 1;
    }
}
