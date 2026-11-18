package opp_cerr;

import battlecode.common.*;

public class Utils {

    public record Offset(int dx, int dy) {}

    static final Direction[] DIRECTIONS = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    static final MapLocation[] KING_SURROUNDING_SQUARES = {
        new MapLocation(-1, 2),
        new MapLocation(0, 2),
        new MapLocation(1, 2),
        new MapLocation(2, 2),
        new MapLocation(2, 1),
        new MapLocation(2, 0),
        new MapLocation(2, -1),
        new MapLocation(2, -2),
        new MapLocation(1, -2),
        new MapLocation(0, -2),
        new MapLocation(-1, -2),
        new MapLocation(-2, -2),
        new MapLocation(-2, -1),
        new MapLocation(-2, 0),
        new MapLocation(-2, 1),
        new MapLocation(-2, 2),
        new MapLocation(-1, 2)
    };

    static final int[] BASIC_INT_MASKS = {
        (1 << 0) - 1,
        (1 << 1) - 1,
        (1 << 2) - 1,
        (1 << 3) - 1,
        (1 << 4) - 1,
        (1 << 5) - 1,
        (1 << 6) - 1,
        (1 << 7) - 1,
        (1 << 8) - 1,
        (1 << 9) - 1,
        (1 << 10) - 1,
        (1 << 11) - 1,
        (1 << 12) - 1,
        (1 << 13) - 1,
        (1 << 14) - 1,
        (1 << 15) - 1,
        (1 << 16) - 1,
        (1 << 17) - 1,
        (1 << 18) - 1,
        (1 << 19) - 1,
        (1 << 20) - 1,
        (1 << 21) - 1,
        (1 << 22) - 1,
        (1 << 23) - 1,
        (1 << 24) - 1,
        (1 << 25) - 1,
        (1 << 26) - 1,
        (1 << 27) - 1,
        (1 << 28) - 1,
        (1 << 29) - 1,
        (1 << 30) - 1,
        (1 << 31) - 1,
        ~0,
    };

    static final long[] BASIC_MASKS = {
        (1L << 0) - 1, (1L << 1) - 1, (1L << 2) - 1, (1L << 3) - 1, (1L << 4) - 1, (1L << 5) - 1,
                (1L << 6) - 1, (1L << 7) - 1, (1L << 8) - 1, (1L << 9) - 1,
        (1L << 10) - 1, (1L << 11) - 1, (1L << 12) - 1, (1L << 13) - 1, (1L << 14) - 1,
                (1L << 15) - 1, (1L << 16) - 1, (1L << 17) - 1, (1L << 18) - 1, (1L << 19) - 1,
        (1L << 20) - 1, (1L << 21) - 1, (1L << 22) - 1, (1L << 23) - 1, (1L << 24) - 1,
                (1L << 25) - 1, (1L << 26) - 1, (1L << 27) - 1, (1L << 28) - 1, (1L << 29) - 1,
        (1L << 30) - 1, (1L << 31) - 1, (1L << 32) - 1, (1L << 33) - 1, (1L << 34) - 1,
                (1L << 35) - 1, (1L << 36) - 1, (1L << 37) - 1, (1L << 38) - 1, (1L << 39) - 1,
        (1L << 40) - 1, (1L << 41) - 1, (1L << 42) - 1, (1L << 43) - 1, (1L << 44) - 1,
                (1L << 45) - 1, (1L << 46) - 1, (1L << 47) - 1, (1L << 48) - 1, (1L << 49) - 1,
        (1L << 50) - 1, (1L << 51) - 1, (1L << 52) - 1, (1L << 53) - 1, (1L << 54) - 1,
                (1L << 55) - 1, (1L << 56) - 1, (1L << 57) - 1, (1L << 58) - 1, (1L << 59) - 1,
        (1L << 60) - 1, (1L << 61) - 1, (1L << 62) - 1, (1L << 63) - 1, ~0,
    };

    static boolean catCanDetect(RobotInfo cat, MapLocation loc) {
        int dirDiff =
                Math.abs(
                        cat.getDirection().ordinal()
                                - cat.getLocation().directionTo(loc).ordinal());
        return cat.getLocation().distanceSquaredTo(loc) <= UnitType.CAT.visionConeRadiusSquared
                && (dirDiff < 2 || dirDiff > 6);
    }

    static boolean ratCanDetect(RobotInfo rat, MapLocation loc) {
        Direction ratDir = rat.getDirection();
        Direction ratLocDir = rat.getLocation().directionTo(loc);
        return ratDir == ratLocDir
                || ratDir == ratLocDir.rotateLeft()
                || ratDir == ratLocDir.rotateRight();
    }

    public static boolean unitCanReach(RobotController rc, RobotInfo unit, MapLocation location)
            throws GameActionException {
        MapLocation loc = unit.getLocation();
        if (loc.distanceSquaredTo(location) > 8) {
            return false;
        }
        Direction d = loc.directionTo(location);
        MapLocation test = loc.add(d);
        if (rc.canSenseLocation(test) && rc.senseMapInfo(test).isPassable()) {
            return true;
        }
        test = loc.add(d.rotateRight());

        if (rc.canSenseLocation(test) && rc.senseMapInfo(test).isPassable()) {
            return true;
        }
        test = loc.add(d.rotateLeft());

        if (rc.canSenseLocation(test) && rc.senseMapInfo(test).isPassable()) {
            return true;
        }
        return false;
    }
}
