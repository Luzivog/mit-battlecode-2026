package opp_goobie;

import battlecode.common.*;

public class CheeseManager {
    static void tryTransfer() throws GameActionException {
        int amount = Robot.rc.getRawCheese();
        MapLocation me = Robot.rc.getLocation();

        Direction d = BabyRat.closestRatKing.directionTo(me);
        MapLocation l = BabyRat.closestRatKing.add(d);

        if (me.distanceSquaredTo(l) > GameConstants.CHEESE_TRANSFER_RADIUS_SQUARED)
            return;

        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.NORTH);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.NORTHEAST);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.EAST);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.SOUTHEAST);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.SOUTH);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.SOUTHWEST);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.WEST);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
            return;
        }
        l = BabyRat.closestRatKing.add(Direction.NORTHWEST);
        if (Robot.rc.canTransferCheese(l, amount)) {
            Robot.rc.transferCheese(l, amount);
        }
    }

    static void tryGrabCheese() throws GameActionException {
        MapLocation me = Robot.rc.getLocation();
        MapLocation loc = me;
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(0, 1);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(1, 1);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(1, 0);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(1, -1);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(0, -1);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(-1, -1);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(-1, 0);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
        loc = me.translate(-1, 1);
        if (Robot.rc.canPickUpCheese(loc))
            Robot.rc.pickUpCheese(loc);
    }

    static MapLocation findNearestCheese() {
        MapLocation me = Robot.rc.getLocation();
        MapLocation target = null;
        int minDist = Constants.INF;

        MapInfo[] infos = Robot.rc.senseNearbyMapInfos();
        MapInfo info;
        MapLocation loc;
        int dist;
        for (int i = infos.length; --i >= 0; ) {
            info = infos[i];
            if (info.getCheeseAmount() <= 0)
                continue;

            loc = info.getMapLocation();
            dist = me.distanceSquaredTo(loc);
            if (target == null || dist < minDist) {
                minDist = dist;
                target = loc;
                break;
            }
        }

        return target;
    }
}
