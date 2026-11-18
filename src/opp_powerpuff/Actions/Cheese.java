package opp_powerpuff.Actions;

import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Helpers.Target;
import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Mine;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Utils.Calc;

// ASSUMES VISION MANAGER HAS BEEN UPDATED
public class Cheese {
    public static int roundsOnCheese = 0;
    public static MapLocation stuckCheeseLoc;

    public static MapInfo lastSeenClosestCheeseInfo;

    public static void run() throws GameActionException {
        RobotController rc = Rat.rc;

        boolean picked = afterPick();

        if (picked) {
            if (
                lastSeenClosestCheeseInfo != null && 
                lastSeenClosestCheeseInfo.getMapLocation().equals(VisionManager.closestCheese.getMapLocation())
            ) {
                lastSeenClosestCheeseInfo = null;
            }
        } else {
            if (lastSeenClosestCheeseInfo == null) {
                lastSeenClosestCheeseInfo = VisionManager.closestCheese;
                roundsOnCheese = 0;
            } else {
                MapLocation lastSeenLoc = lastSeenClosestCheeseInfo.getMapLocation();
                if (!rc.canSenseLocation(lastSeenLoc)) {
                    Direction dir = Rat.dirTo(lastSeenLoc);
                    if (rc.canTurn(dir)) rc.turn(dir);
                }
                if (rc.canSenseLocation(lastSeenLoc)) {
                    MapInfo sense = rc.senseMapInfo(lastSeenLoc);
                    if (sense.getCheeseAmount() == 0) lastSeenClosestCheeseInfo = null;
                }
            }
        }

        if (lastSeenClosestCheeseInfo != null) {
            if (stuckCheeseLoc == null) {
                stuckCheeseLoc = lastSeenClosestCheeseInfo.getMapLocation();
                roundsOnCheese = 0;
            } else {
                if (stuckCheeseLoc.equals(lastSeenClosestCheeseInfo.getMapLocation())) {
                    roundsOnCheese++;
                } else {
                    roundsOnCheese = 0;
                    stuckCheeseLoc = lastSeenClosestCheeseInfo.getMapLocation();
                }
            }
        } else {
            roundsOnCheese = 0;
        }

        // if (VisionManager.closestCheeseMine != null && VisionManager.enemies.length > 1) {
        //     MapLocation loc = VisionManager.closestCheeseMine.getMapLocation();
        //     if (rc.getAllCheese() > Constants.MAX_CHEESE_THRESHOLD)
        //         if (rc.canPlaceRatTrap(loc.add(Direction.NORTH).add(Direction.NORTH)))
        //             rc.placeRatTrap(loc.add(Direction.NORTH).add(Direction.NORTH));
        //         if (rc.canPlaceRatTrap(loc.add(Direction.SOUTH).add(Direction.SOUTH)))
        //             rc.placeRatTrap(loc.add(Direction.SOUTH).add(Direction.SOUTH));
        //         if (rc.canPlaceRatTrap(loc.add(Direction.EAST).add(Direction.EAST)))
        //             rc.placeRatTrap(loc.add(Direction.EAST).add(Direction.EAST));
        //         if (rc.canPlaceRatTrap(loc.add(Direction.WEST).add(Direction.WEST)))
        //             rc.placeRatTrap(loc.add(Direction.WEST).add(Direction.WEST));
        // }

        if (lastSeenClosestCheeseInfo != null) {
            Direction dir = Rat.dirTo(lastSeenClosestCheeseInfo.getMapLocation());
            if (rc.canTurn(dir)) rc.turn(dir);
            rc.setIndicatorDot(lastSeenClosestCheeseInfo.getMapLocation(), 255, 0, 255);
            Nav.moveTo(lastSeenClosestCheeseInfo.getMapLocation());
        } else {
            if (VisionManager.closestCheeseMine != null) {
                Direction toClosestCheeseMine = Rat.dirTo(VisionManager.closestCheeseMine.getMapLocation());
                if (toClosestCheeseMine != Direction.CENTER && toClosestCheeseMine != Rat.rc.getDirection() && Rat.rc.canTurn()) {
                    Rat.rc.turn(toClosestCheeseMine);
                }
            }
        }
    }

    public static boolean check() throws GameActionException {
        return !(
            roundsOnCheese > Constants.ROUNDS_STUCK_ON_CHEESE 
            ||
            (
                lastSeenClosestCheeseInfo == null &&
                VisionManager.closestCheese == null
            ) 
        );
    }

    // CAN UNROLL HERE
    public static boolean afterPick() throws GameActionException {
        Direction front = Rat.rc.getDirection();
        Direction left = front.rotateLeft();
        Direction right = front.rotateRight();

        MapLocation loc = Rat.rc.getLocation();
        MapLocation frontLoc = loc.add(front);
        MapLocation leftLoc = loc.add(left);
        MapLocation rightLoc = loc.add(right);

        int maxPick = 1000000; // Constants.CHEESE_ABSOLUTE_LIMIT - Rat.rc.getRawCheese();

        MapInfo centerInfo = Rat.rc.senseMapInfo(loc);
        int centerCheese = centerInfo.getCheeseAmount();
        if (centerCheese > 0 && Rat.rc.canPickUpCheese(loc)) {
            Rat.rc.pickUpCheese(loc, Math.min(centerCheese, maxPick));
            return true;
        }
        if (Rat.rc.onTheMap(frontLoc)) {
            MapInfo frontInfo = Rat.rc.senseMapInfo(frontLoc);
            int frontCheese = frontInfo.getCheeseAmount();
            if (frontInfo.getCheeseAmount() > 0 && Rat.rc.canPickUpCheese(frontLoc)) {
                Rat.rc.pickUpCheese(frontLoc, Math.min(frontCheese, maxPick));
                return true;
            }
        }
        if (Rat.rc.onTheMap(leftLoc)) {
            MapInfo leftInfo = Rat.rc.senseMapInfo(leftLoc);
            int leftCheese = leftInfo.getCheeseAmount();
            if (leftInfo.getCheeseAmount() > 0 && Rat.rc.canPickUpCheese(leftLoc)) {
                Rat.rc.pickUpCheese(leftLoc, Math.min(leftCheese, maxPick));
                return true;
            }
        }
        if (Rat.rc.onTheMap(rightLoc)) {
            MapInfo rightInfo = Rat.rc.senseMapInfo(rightLoc);
            int rightCheese = rightInfo.getCheeseAmount();
            if (rightInfo.getCheeseAmount() > 0 && Rat.rc.canPickUpCheese(rightLoc)) {
                Rat.rc.pickUpCheese(rightLoc, Math.min(rightCheese, maxPick));
                return true;
            }
        }
        // MapInfo[] nearby = Rat.rc.senseNearbyMapInfos(2);
        // for (MapInfo map: nearby) {
        //     if (map.getCheeseAmount() > 0 && Rat.rc.canPickUpCheese(map.getMapLocation())) {
        //         Rat.rc.pickUpCheese(map.getMapLocation());
        //     } 
        // }
        return false;
    }
}
