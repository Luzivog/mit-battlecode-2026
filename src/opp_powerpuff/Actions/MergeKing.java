package opp_powerpuff.Actions;

import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Helpers.Vision;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.RatKing.KingManager;


public class MergeKing {

    public static boolean buildKing() throws GameActionException {
        if (Deliver.hasInfoToRelay()) return false;
        // do not attempt to use this rat for making kings if it is currently close to a cat
        if (VisionManager.closestCat != null && Rat.rc.getLocation().distanceSquaredTo(VisionManager.closestCat.getLocation()) <= 5) return false;
        int idx = Global.readBuildKingFlag();
        // return if no need to build king
        if (idx == 1023)
            return false;
        //System.out.println(idx);
        MapLocation loc = Global.readRatKingLoc(idx);
        // if we are already there or we're out of range to build the rat king
        if (Rat.rc.getLocation().distanceSquaredTo(loc) > Constants.MERGE_RAT_DISTANCE)
            return false;
        if (loc.equals(Rat.rc.getLocation())) {
            if (Rat.rc.canBecomeRatKing()) {
                Rat.rc.becomeRatKing();
            }
            return true;
        }
        else if (Rat.rc.getLocation().isAdjacentTo(loc) && !Rat.rc.canMove(Rat.rc.getLocation().directionTo(loc))) {
            if (Rat.rc.canTurn()) {
                Direction d = loc.directionTo(Rat.rc.getLocation());
                if (d != Direction.CENTER)
                    Rat.rc.turn(d);
            }
            if (Rat.rc.canBecomeRatKing()) {
                Rat.rc.becomeRatKing();
            }
            return true;
        }
        else {
            Nav.moveTo(loc);
        }
        return false;
    }

    public static boolean buildKingOnClump() throws GameActionException {
        if (Global.readBuildKingFlag() != 1023)
            return false;
        // if (VisionManager.allies.length < 2)
        //     return false;
        // if (Global.getLivingKings() > 1)
        //     return false;
        if (Rat.rc.getRawCheese() < Constants.CHEESE_ABSOLUTE_LIMIT) 
            return false;
        if (Rat.rc.getGlobalCheese() > Constants.WEALTHY_CHEESE_THRESHOLD)
            return false;
        // for (RobotInfo ally : VisionManager.allies) {
        //     if (ally.getRawCheeseAmount() < 150)
        //         return false;
        // }
        if (Rat.rc.canBecomeRatKing()) {
            Rat.rc.becomeRatKing();
            return true;
        }
        return false;
    }
}
