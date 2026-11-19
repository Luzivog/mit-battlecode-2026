package fuzz_resign;

import battlecode.common.*;

// Plays a plain economy game and resigns at a fixed round (90 on maps of at most 900 tiles, else 150): the king on
// maps with an even width + height, otherwise every baby rat (the first one to act resigns for the team).
public class RobotPlayer {
    static RobotController rc;
    static MapLocation home;
    static final Direction[] D8 = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

    public static void run(RobotController r) {
        rc = r;
        boolean kingResigns = (rc.getMapWidth() + rc.getMapHeight()) % 2 == 0;
        int resignRound = rc.getMapWidth() * rc.getMapHeight() <= 900 ? 90 : 150;
        while (true) {
            try {
                int round = rc.getRoundNum();
                boolean king = rc.getType().isRatKingType();
                if (round >= resignRound && king == kingResigns) {
                    System.out.println("FZT fuzz_resign " + (rc.getTeam() == Team.A ? 'A' : 'B') + " " + rc.getID()
                            + " r=" + round + " resign resign=1,0");
                    rc.resign();
                    // the robot was destroyed by resign(): this never runs
                    rc.setIndicatorString("after resign");
                }
                if (king) king();
                else rat();
            } catch (GameActionException e) {
                rc.setIndicatorString(e.getMessage());
            }
            Clock.yield();
        }
    }

    static void king() throws GameActionException {
        MapLocation me = rc.getLocation();
        if (home == null) {
            home = me;
            rc.writeSharedArray(0, me.x + 1);
            rc.writeSharedArray(1, me.y + 1);
        }
        for (MapLocation l : rc.getAllPartLocations()) if (rc.canPickUpCheese(l)) rc.pickUpCheese(l);
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo e : enemies) {
            if (rc.canAttack(e.getLocation())) {
                rc.attack(e.getLocation());
                break;
            }
        }
        if (rc.getCurrentRatCost() <= 20 && rc.getGlobalCheese() > 300) {
            for (Direction d : D8) {
                MapLocation t = me.add(d).add(d);
                if (rc.canBuildRat(t)) {
                    rc.buildRat(t);
                    break;
                }
            }
        }
    }

    static void rat() throws GameActionException {
        if (home == null) {
            int x = rc.readSharedArray(0), y = rc.readSharedArray(1);
            if (x > 0) home = new MapLocation(x - 1, y - 1);
        }
        MapLocation me = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        for (RobotInfo e : enemies) {
            if (rc.canAttack(e.getLocation())) {
                rc.attack(e.getLocation());
                break;
            }
        }
        if (rc.getRawCheese() >= 20 && home != null) {
            for (Direction d : Direction.allDirections()) {
                MapLocation t = home.add(d);
                if (rc.canTransferCheese(t, rc.getRawCheese())) {
                    rc.transferCheese(t, rc.getRawCheese());
                    return;
                }
            }
            step(me.directionTo(home));
            return;
        }
        MapLocation best = null;
        int bd = 1 << 30;
        for (MapInfo mi : rc.senseNearbyMapInfos()) {
            if (mi.getCheeseAmount() <= 0) continue;
            int d = me.distanceSquaredTo(mi.getMapLocation());
            if (d < bd) {
                bd = d;
                best = mi.getMapLocation();
            }
        }
        if (best != null) {
            if (rc.canPickUpCheese(best)) rc.pickUpCheese(best);
            else step(me.directionTo(best));
            return;
        }
        step(D8[(rc.getID() + rc.getRoundNum() / 20) % 8]);
    }

    static void step(Direction d) throws GameActionException {
        if (d == Direction.CENTER) return;
        for (Direction x : new Direction[]{d, d.rotateLeft(), d.rotateRight()}) {
            if (rc.canMove(x)) {
                if (x != rc.getDirection() && rc.canTurn()) rc.turn(x);
                rc.move(x);
                return;
            }
        }
        if (rc.canTurn()) rc.turn(d.rotateRight().rotateRight());
    }
}
