package opp_parmesan.Roles;

import battlecode.common.*;
import opp_parmesan.Utils.*;

public class Hunter extends Globals {
    // Cat attacking micro
    public static MapLocation cat = null;
    public static Direction catDir = Direction.CENTER;
    public static int unknownRounds = 0;

    public static void run () throws GameActionException {

        if (!rc.isCooperation()) {
            if (catLoc != null) {
                MapLocation target = rc.getLocation().add(rc.getLocation().directionTo(catLoc));
                if (rc.canAttack(target)) {
                    rc.attack(target);
                }
            }
            return;
        }



        if (rc.readSharedArray(62) == 0) return;
        // Maintain cat position
        unknownRounds += 1;
        if (catLoc != null) {
            System.out.println("Target seen");
            cat = catLoc;
            catDir = rc.senseRobotAtLocation(cat).getDirection();
            unknownRounds = 0;
        }

        // Get the latest information if possible
//        MapLocation possible = Comms.getPreyLoc();
//        if (unknownRounds > 0 && possible.x > -1 && rc.getLocation().distanceSquaredTo(possible) < 20) {
//            cat = possible;
//            if (unknownRounds > 2) catDir = Direction.CENTER;
//            unknownRounds = 2;
//        }
        if (cat == null || unknownRounds > 3 || (rc.getRawCheese() == 0 && rc.getGlobalCheese() < safeCheeseReserve))
            return;
        // Try place cat trap
        MapLocation m1 = new MapLocation(cat.x, cat.y);
        MapLocation m2 = new MapLocation(cat.x + 1, cat.y);
        MapLocation m3 = new MapLocation(cat.x, cat.y + 1);
        MapLocation m4 = new MapLocation(cat.x + 1, cat.y + 1);
        if (rc.canPlaceCatTrap(m1)) rc.placeCatTrap(m1);
        if (rc.canPlaceCatTrap(m2)) rc.placeCatTrap(m2);
        if (rc.canPlaceCatTrap(m3)) rc.placeCatTrap(m3);
        if (rc.canPlaceCatTrap(m4)) rc.placeCatTrap(m4);
    }
}
