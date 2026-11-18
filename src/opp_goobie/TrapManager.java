package opp_goobie;

import battlecode.common.*;

public class TrapManager {
    static final int MAX_ENEMY_DISTANCE = 5;
    static final Direction[] directions = {
            Direction.CENTER,
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };

    public static void tryPlaceTrapNearMine() throws GameActionException {
        if (!Robot.rc.isActionReady())
            return;
        if (Robot.rc.getRawCheese() < 10)
            return;

        if (Robot.rc.getGlobalCheese() < 400)
            return;

        MapLocation myLoc = Robot.rc.getLocation();

        MapLocation nearestMine = MapData.getClosestCheeseMine(myLoc);
        if (nearestMine == null)
            return;

        int distToMine = myLoc.distanceSquaredTo(nearestMine);
        if (distToMine > 18)
            return;

        MapLocation[] kings = CommsArray.getRatKings();
        if (MapData.hasRatKing(nearestMine, kings))
            return;

        MapLocation center = Robot.centerLoc;
        int mineDistToCenter = nearestMine.distanceSquaredTo(center);
        int maxDistFromCenter = (Robot.W * Robot.W + Robot.H * Robot.H) / 4; // Half the map diagonal
        if (mineDistToCenter > maxDistFromCenter)
            return;

        tryPlaceTrapAtBestLocation(nearestMine);
    }

    private static boolean tryPlaceTrapAtBestLocation(MapLocation mine) throws GameActionException {
        if (Robot.rc.canPlaceRatTrap(mine)) {
            Robot.rc.placeRatTrap(mine);
            return true;
        }

        if (Robot.rc.canPlaceRatTrap(mine.add(Direction.NORTH))) {
            Robot.rc.placeRatTrap(mine.add(Direction.NORTH));
            return true;
        }
        if (Robot.rc.canPlaceRatTrap(mine.add(Direction.EAST))) {
            Robot.rc.placeRatTrap(mine.add(Direction.EAST));
            return true;
        }
        if (Robot.rc.canPlaceRatTrap(mine.add(Direction.SOUTH))) {
            Robot.rc.placeRatTrap(mine.add(Direction.SOUTH));
            return true;
        }
        if (Robot.rc.canPlaceRatTrap(mine.add(Direction.WEST))) {
            Robot.rc.placeRatTrap(mine.add(Direction.WEST));
            return true;
        }

        return false;
    }

    public static boolean fuzzyPlaceRatTrap(MapLocation loc) throws GameActionException {
        if (Robot.rc.isActionReady()) {
            MapLocation tryLoc;
            for (Direction dir : directions) {
                tryLoc = loc.add(dir);
                if (Robot.rc.canPlaceRatTrap(tryLoc)) {
                    Robot.rc.placeRatTrap(tryLoc);
                    return true;
                }
            }
        }
        return false;
    }

    static void placeRatTrap() throws GameActionException {
        if (Robot.rc.getGlobalCheese() < 750)
            return;
        MapLocation me = Robot.rc.getLocation();
        MicroManager.MicroInfo bestMicro = null;
        for (Direction dir : directions) {
            MapLocation t = me.add(dir);
            if (!Robot.rc.canPlaceRatTrap(t))
                continue;
            MicroManager.MicroInfo m = MicroManager.microInfo[dir.ordinal()];
            if (m.minDistToEnemy > MAX_ENEMY_DISTANCE)
                continue;
            if (m.alliesDist5 >= m.enemiesInMoveRange)
                continue;
            if (m.minDistTrap <= 2)
                continue;
            bestMicro = compare(bestMicro, m);
        }
        if (bestMicro != null)
            Robot.rc.placeRatTrap(me.add(bestMicro.dir));
    }

    static MicroManager.MicroInfo compare(MicroManager.MicroInfo A, MicroManager.MicroInfo B) {
        if (A == null)
            return B;
        if (B == null)
            return A;
        // if (A.minDistToEnemy < B.minDistToEnemy)
        // return A;
        if (B.minDistToEnemy < A.minDistToEnemy)
            return B;
        // if (A.enemiesInRange > B.enemiesInRange)
        // return A;
        // if (A.enemiesInRange < B.enemiesInRange)
        // return B;
        // if (A.enemiesInMoveRange > B.enemiesInMoveRange)
        // return A;
        // if (A.enemiesInMoveRange < B.enemiesInMoveRange)
        // return B;
        return A;
    }

    private static boolean isTrapAdjacentToOther(MapLocation loc) throws GameActionException {
        for (Direction d : Robot.DIRECTIONS) {
            MapLocation adj = loc.add(d);
            if (!Robot.rc.onTheMap(adj))
                continue;

            if (!Robot.rc.canSenseLocation(adj))
                continue;

            MapInfo info = Robot.rc.senseMapInfo(adj);
            if (info.getTrap() == TrapType.NONE)
                continue;

            return true;
        }

        return false;
    }

    public static boolean fuzzyPlaceCatTrap(MapLocation loc) throws GameActionException {
        if (Robot.rc.isCooperation() && Robot.rc.isActionReady()) {
            MapLocation tryLoc;
            for (Direction dir : directions) {
                tryLoc = loc.add(dir);
                if (Robot.rc.canPlaceCatTrap(tryLoc)) {
                    Robot.rc.placeCatTrap(tryLoc);
                    return true;
                }
            }
        }
        return false;
    }
}
