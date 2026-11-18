package opp_goobie;

import battlecode.common.*;

public class CarryManager {
    static int pickupRound;
    static MapLocation ratKing;
    static MapLocation enemy;

    static void tryThrow() throws GameActionException {
        if (Robot.rc.getCarrying() == null || !Robot.rc.isActionReady())
            return;

        MapLocation me = Robot.rc.getLocation();

        if (Robot.rc.getRoundNum() - pickupRound >= 9) {
            attemptThrow(me.add(Robot.rc.getDirection()));
            return;
        }

        RobotInfo[] robots = Robot.rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.opponent);
        updateEnemy(me, robots);

        MapLocation target = chooseThrowTarget(me, robots);
        if (target == null)
            return;
        attemptThrow(target);
    }

    static MapLocation chooseThrowTarget(MapLocation me, RobotInfo[] robots) throws GameActionException {
        if (ratKing != null) {
            Debug.addIndicatorString("Throw Target: RAT KING at " + ratKing);
            return ratKing;
        }

        MapLocation cat = getCat();
        if (cat != null) {
            Debug.addIndicatorString("Throw Target: CAT at " + cat);
            return cat;
        }

        if (enemy != null) {
            Debug.addIndicatorString("Throw Target: ENEMY at " + enemy);
            return enemy;
        }

        MapLocation wall = findWall(me);
        if (wall != null && robots.length == 0) {
            Debug.addIndicatorString("Throw Target: WALL at " + wall + " (no enemy)");
            return wall;
        }

        Debug.addIndicatorString("Throw Target: NONE");
        return null;
    }

    static boolean attemptThrow(MapLocation loc) throws GameActionException {
        if (loc == null)
            return false;

        MapLocation me = Robot.rc.getLocation();
        Direction dir = me.directionTo(loc);

        if (isBlocked(me, loc))
            return false;

        boolean aligned = loc.equals(ratKing)
                ? isAlignedRatKing(me, loc)
                : isAligned(me, loc);

        if (!aligned) {
            return false;
        }

        if (Robot.rc.getDirection() != dir) {
            if (!Robot.rc.canTurn(dir))
                return false;
            Robot.rc.turn(dir);
        }

        if (me.isAdjacentTo(loc)) {
            Direction d = dir.opposite();
            if (!Robot.rc.canMove(d))
                return false;
            Robot.rc.move(d);
        }

        if (!Robot.rc.canThrowRat())
            return false;

        Robot.rc.throwRat();
        return true;
    }

    static void tryPickup() throws GameActionException {
        if (!Robot.rc.isActionReady() || Robot.rc.getCarrying() != null)
            return;

        RobotInfo target = null;
        RobotInfo[] robots = Robot.rc.senseNearbyRobots(GameConstants.ATTACK_DISTANCE_SQUARED, Robot.opponent);

        for (RobotInfo r : robots) {
            if (!Robot.rc.canCarryRat(r.getLocation()))
                continue;
            if (r.getHealth() <= 10)
                return;
            target = compare(target, r);
        }

        if (target == null)
            return;

        Robot.rc.carryRat(target.getLocation());
        pickupRound = Robot.rc.getRoundNum();
    }

    static RobotInfo compare(RobotInfo A, RobotInfo B) {
        if (A == null)
            return B;
        if (B == null)
            return A;

        if (A.getCarryingRobot() != null
                && A.getCarryingRobot().getTeam() == Robot.team)
            return A;
        if (B.getCarryingRobot() != null
                && B.getCarryingRobot().getTeam() == Robot.team)
            return B;

        if (A.getHealth() < B.getHealth())
            return A;
        return B;
    }

    static void updateEnemy(MapLocation me, RobotInfo[] robots) throws GameActionException {
        enemy = null;
        ratKing = null;

        int minDist = Constants.INF;
        // RobotInfo[] robots = Robot.rc.senseNearbyRobots(Robot.visionRadiusSquared,
        // Robot.opponent);

        for (RobotInfo r : robots) {
            MapLocation loc = r.getLocation();
            if (r.getType().isRatKingType()) {
                if (!isBlocked(me, loc)) {
                    ratKing = loc;
                    return;
                }
                continue;
            }

            if (isBlocked(me, loc))
                continue;

            int d = me.distanceSquaredTo(loc);
            if (enemy == null || d < minDist) {
                enemy = loc;
                minDist = d;
            }
        }
    }

    static MapLocation getCat() throws GameActionException {
        RobotInfo[] robots = Robot.rc.senseNearbyRobots(Robot.visionRadiusSquared);

        for (RobotInfo r : robots) {
            if (r.getType().isCatType())
                return r.getLocation();
        }
        return null;
    }

    static MapLocation findWall(MapLocation me) throws GameActionException {
        MapLocation wall = MapData.getClosestWall(me);
        if (wall != null)
            return wall;

        int minDist = Constants.INF;
        MapLocation cWall = null;
        MapInfo[] infos = Robot.rc.senseNearbyMapInfos(Robot.visionRadiusSquared);

        for (MapInfo info : infos) {
            if (info.isPassable())
                continue;

            MapLocation loc = info.getMapLocation();

            int d = me.distanceSquaredTo(loc);
            if (cWall == null || d < minDist) {
                cWall = loc;
                minDist = d;
            }
        }

        return cWall;
    }

    static boolean isAligned(MapLocation me, MapLocation target) {
        int dx = target.x - me.x;
        int dy = target.y - me.y;

        if (dx == 0 || dy == 0)
            return true;
        return Math.abs(dx) == Math.abs(dy);
    }

    static boolean isAlignedRatKing(MapLocation me, MapLocation center) {
        if (center == null)
            return false;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (isAligned(me,
                        new MapLocation(center.x + dx, center.y + dy)))
                    return true;
            }
        }
        return false;
    }

    static boolean isBlocked(MapLocation from, MapLocation to) throws GameActionException {
        Direction dir = from.directionTo(to);

        if (!isAligned(from, to))
            return true;

        MapLocation cur = from.add(dir);
        while (!cur.equals(to)) {
            if (!Robot.rc.onTheMap(cur))
                return true;

            if (Robot.rc.canSenseLocation(cur)) {
                if (!Robot.rc.sensePassability(cur))
                    return true;

                RobotInfo r = Robot.rc.senseRobotAtLocation(cur);
                if (r != null && r.getTeam() == Robot.team)
                    return true;
            }

            cur = cur.add(dir);
        }
        return false;
    }
}
