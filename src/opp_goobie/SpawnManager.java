package opp_goobie;

import battlecode.common.*;

public class SpawnManager {
    static int[] ratCount = new int[8];
    static double[] dists = new double[8];

    static void doDirs() {
        int dx, dy;
        MapLocation myLoc = Robot.rc.getLocation();
        dists[Direction.EAST.ordinal()] = Robot.W - myLoc.x - 1;
        dx = Robot.W - myLoc.x - 1;
        dy = Robot.H - myLoc.y - 1;
        dists[Direction.NORTHEAST.ordinal()] = Math.sqrt(2) * Math.min(dx, dy);
        dists[Direction.NORTH.ordinal()] = Robot.H - myLoc.y - 1;
        dx = Robot.W - myLoc.x - 1;
        dy = myLoc.y;
        dists[Direction.SOUTHEAST.ordinal()] = Math.sqrt(2) * Math.min(dx, dy);
        dists[Direction.WEST.ordinal()] = myLoc.x;
        dx = myLoc.x;
        dy = Robot.H - myLoc.y - 1;
        dists[Direction.NORTHWEST.ordinal()] = Math.sqrt(2) * Math.min(dx, dy);
        dx = myLoc.x;
        dy = myLoc.y;
        dists[Direction.SOUTHWEST.ordinal()] = Math.sqrt(2) * Math.min(dx, dy);
        dists[Direction.SOUTH.ordinal()] = myLoc.y;
    }

    static Direction getBestDir(int[] dirCount) {
        Direction ans = null;
        double dAns = -1;
        for (Direction dir : Robot.DIRECTIONS) {
            if (!Robot.rc.canBuildRat(Robot.rc.getLocation().add(dir).add(dir)))
                continue;
            double ndAns = dists[dir.ordinal()] / ((dirCount[dir.ordinal()]) + 1);
            if (ndAns > dAns) {
                dAns = ndAns;
                ans = dir;
            }
        }
        return ans;
    }

    static void spawn() throws GameActionException {
        if (Robot.turnCount < 3) {
            spawnDefense(SymmetryManager.getTarget());
            return;
        }

        if (Robot.rc.getAllCheese() - Robot.rc.getCurrentRatCost() < GameConstants.RAT_KING_CHEESE_CONSUMPTION
                * CommsArray.livingRatKingCount)
            return;

        Direction dir = getBestDir(ratCount);
        if (dir == null)
            return;
        MapLocation l = Robot.rc.getLocation().add(dir).add(dir);
        if (!Robot.rc.canBuildRat(l))
            return;

        RatKing.cheeseSpentSpawning += Robot.rc.getCurrentRatCost();
        RatKing.spawnedRats++;
        if (Robot.rc.canTurn(dir))
            Robot.rc.turn(dir);
        Robot.rc.buildRat(l);
        ratCount[dir.ordinal()]++;
    }

    static void spawnDefense(MapLocation target) throws GameActionException {
        MapLocation[] locs = Robot.rc.getAllLocationsWithinRadiusSquared(Robot.rc.getLocation(),
                GameConstants.RAT_KING_BUILD_DISTANCE_SQUARED);
        MapLocation bestLoc = null;
        int bestDist = 0;
        for (MapLocation loc : locs) {
            if (!Robot.rc.canBuildRat(loc))
                continue;
            int d = loc.distanceSquaredTo(target);
            if (d <= 4)
                continue;
            if (bestLoc == null || d < bestDist) {
                bestDist = d;
                bestLoc = loc;
            }
        }
        if (bestLoc == null || !Robot.rc.canBuildRat(bestLoc))
            return;

        Direction dir = Robot.rc.getLocation().directionTo(target);
        if (Robot.rc.canTurn(dir))
            Robot.rc.turn(dir);
        Robot.rc.buildRat(bestLoc);
    }
}
