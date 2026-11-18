package opp_goobie;

import battlecode.common.MapLocation;

public class MapTracker {
    final static int ARRAY_SIZE = 60;

    static long[] visitedLocations = new long[ARRAY_SIZE];

    MapTracker() {
    }

    static void reset() {
        visitedLocations = new long[ARRAY_SIZE];
    }

    static void add(MapLocation loc) {
        visitedLocations[loc.x] |= (1L << loc.y);
    }

    static boolean check(MapLocation loc) {
        return (((visitedLocations[loc.x] >>> loc.y) & 1L)) != 0L;
    }
}
