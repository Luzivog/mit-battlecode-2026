package opp_gst.BabyRatBehaviors;

import opp_gst.Behavior;
import opp_gst.Pathfinding.Pathfinding;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.*;
import opp_gst.Communication.RatKingInfo;
import static opp_gst.BabyRat.*;
import opp_gst.Communication.SymmetrySqueakInfo;

import static opp_gst.BabyRat.currentLocation;
import static opp_gst.BabyRat.nearbyCats;
import static opp_gst.RobotPlayer.*;
import opp_gst.SymmetryManager;

import opp_gst.Communication.Communicator;

public class BRReportSymmetry implements Behavior {
    //Singleton Stuff
    private static BRReportSymmetry instance;
    private BRReportSymmetry() {}
    public static BRReportSymmetry getInstance() {
        if(instance == null) {
            instance = new BRReportSymmetry();
        }

        return instance;
    }

    @Override
    public void execute() throws GameActionException {
        if (currentLocation.distanceSquaredTo(closestAllyRatKing.loc()) <= GameConstants.SQUEAK_RADIUS_SQUARED) {
            SymmetrySqueakInfo squeak = new SymmetrySqueakInfo(SymmetryManager.getSym());
            Communicator.sendSqueak(squeak);
        }
        else {
            Pathfinding.attemptMove(closestAllyRatKing.loc(), true);
        }
    }
}