package opp_powerpuff.Actions;

import battlecode.common.*;

import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Rat;
//import opp_powerpuff.Debug.debug;
import opp_powerpuff.Helpers.Comms;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Helpers.Target;
import opp_powerpuff.Managers.VisionManager;

// ASSUMES GLOBAL HAS BEEN UPDATED
public class Deliver {
    public static void run() throws GameActionException {
        Rat.rc.setIndicatorString("DELIVERING");

        if (VisionManager.unknownCheeseMine != null) {
            Rat.rc.setIndicatorDot(VisionManager.unknownCheeseMine, 255, 255, 255);
        }

        // if (Rat.rc.getHealth() <= Constants.RETREAT_HEALTH && far()) {
        //     Target.setAllyKingLocation(100);
        // }

        MapLocation kingLoc = VisionManager.allyRatKingLocation;
        Nav.moveTo(kingLoc);
        transmit(kingLoc);
    }

    public static boolean check() throws GameActionException {
        int rawCheese = Rat.rc.getRawCheese();
        return (         
            (
                rawCheese >= Constants.CHEESE_RETREAT_LIMIT
            )
            ||
            (
                rawCheese >= Constants.CHEESE_DEFAULT_LIMIT && 
                (
                    Rat.rc.getAllCheese() < Constants.DEAD_CHEESE_THRESHOLD ||
                    (
                        hasInfoToRelay() && 
                        Rat.rc.getRoundNum() > Rat.startDuration()
                    )
                )
            )
        );
    }

    public static void transmit(MapLocation kingLoc) throws GameActionException {
        RobotController rc = Rat.rc;
        int rawCheeseCount = rc.getRawCheese();
        if (hasInfoToRelay()) {     
            if (kingLoc.isWithinDistanceSquared(rc.getLocation(), GameConstants.SQUEAK_RADIUS_SQUARED)) {
                relayInfo();
            }
        }

        if (
            kingLoc.isWithinDistanceSquared(rc.getLocation(), GameConstants.CHEESE_TRANSFER_RADIUS_SQUARED) &&
            rawCheeseCount >= Constants.CHEESE_DEFAULT_LIMIT
        ) { 
            if (rc.canTurn() ) {//&& Rat.dirTo(kingLoc) != Direction.CENTER) 
                rc.turn(Rat.dirTo(kingLoc));
            }
            if (rc.canTransferCheese(kingLoc, rawCheeseCount)) {
                rc.transferCheese(kingLoc, rawCheeseCount);
            }
        }
    }

    public static void relayInfo() throws GameActionException {
        if ((Global.readSymmetry() & MapData.symmetry) < Global.readSymmetry()) {
            Comms.squeakSymmetry(MapData.symmetry);
            // System.out.println("SYMMETRY");
        }
        if (VisionManager.unknownCheeseMine != null) {
            Comms.squeakCheeseMine(VisionManager.unknownCheeseMine);
            // System.out.println("CHEESE MINE");
        }
    }

    public static boolean far() throws GameActionException {
        return (
            Rat.rc.getRawCheese() > Constants.CHEESE_DEFAULT_LIMIT && 
            Rat.distTo(VisionManager.allyRatKingLocation) >= 1.4f * (Math.max(Rat.H, Rat.W) * Math.max(Rat.H, Rat.W) / 4)
        );
    }

    public static boolean hasInfoToRelay() throws GameActionException {
        return (
            VisionManager.unknownCheeseMine != null || 
            (Global.readSymmetry() & MapData.symmetry) < Global.readSymmetry()
        );
    }
}
