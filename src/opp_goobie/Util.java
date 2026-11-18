package opp_goobie;

import battlecode.common.*;

public class Util {
    private static final int NULL_COORD = 63;
    public static final int NULL_LOCATION_INT = (NULL_COORD << 6) | NULL_COORD;
    private static final int NULL_COORD_2BY2 = 31;
    public static final int NULL_LOCATION_2BY2_INT = (NULL_COORD_2BY2 << 5) | NULL_COORD_2BY2;

    public static int locationToInt(MapLocation location) {
        if (location == null) {
            return NULL_LOCATION_INT;
        }
        return (location.x << 6) | location.y;
    }

    public static MapLocation intToLocation(int encoded) {
        int y = encoded & 0x3F;
        int x = (encoded >> 6) & 0x3F;
        // Coords 60-63 are out of bounds, treat as null
        if (x >= 60 || y >= 60) {
            return null;
        }
        return new MapLocation(x, y);
    }

    public static int locationTo2by2Int(MapLocation location) {
        if (location == null) {
            return NULL_LOCATION_2BY2_INT;
        }
        return ((location.x >> 1) << 5) | (location.y >> 1);
    }

    public static MapLocation int2By2ToLocation(int int2By2) {
        int x2 = (int2By2 >> 5) & 0x1F;
        int y2 = int2By2 & 0x1F;
        // 2x2 coords 30-31 are out of bounds
        if (x2 >= 30 || y2 >= 30) {
            return null;
        }
        return new MapLocation(x2 << 1, y2 << 1);
    }

    public static int chebyshevDistanceTo(MapLocation loc1, MapLocation loc2) {
        return Math.max(Math.abs(loc1.x - loc2.x), Math.abs(loc1.y - loc2.y));
    }

    /**
     * Calculates minimum bits needed to represent n distinct values.
     *
     * @param n Number of distinct values
     * @return Number of bits needed (ceiling of log2)
     */
    public static int bitsNeeded(int n) {
        if (n <= 1)
            return 1; // At minimum 1 bit
        // Calculate ceiling of log2(n)
        return 32 - Integer.numberOfLeadingZeros(n - 1);
    }

    public static MapLocation getClosestEnemy(RobotInfo[] robots) {
        MapLocation target = null;
        MapLocation me = Robot.rc.getLocation();
        int minDist = Constants.INF;

        for (RobotInfo r : robots) {
            MapLocation rloc = r.getLocation();
            int dist = me.distanceSquaredTo(rloc);
            if (target == null || dist < minDist) {
                target = rloc;
                minDist = dist;
            }
        }

        return target;
    }

    public static RobotInfo getClosestEnemyRobot(RobotInfo[] robots) {
        RobotInfo target = null;
        MapLocation me = Robot.rc.getLocation();
        int minDist = Constants.INF;

        for (RobotInfo r : robots) {
            int i = Util.locationToInt(r.location);
            if (CommsSqueak.enemies.contains(i))
                continue;
            int dist = me.distanceSquaredTo(r.getLocation());
            if (target == null || dist < minDist) {
                target = r;
                minDist = dist;
            }
        }

        return target;
    }
}
