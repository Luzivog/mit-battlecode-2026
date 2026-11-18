package opp_gst.BabyRatBehaviors;

import java.util.*;

import opp_gst.Behavior;
import opp_gst.Communication.FormRatKingSqueakInfo;
import opp_gst.Communication.Squeak;
import opp_gst.RatKing;
import opp_gst.RobotPlayer;
import opp_gst.Utilities;
import opp_gst.Communication.Communicator;
import opp_gst.Pathfinding.Pathfinding;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import static opp_gst.BabyRat.*;
import static opp_gst.RobotPlayer.rc;
import static opp_gst.Pathfinding.Pathfinding.passable;
public class BRFormRatKing implements Behavior {

    private static BRFormRatKing instance;
    private BRFormRatKing() {}
    public static BRFormRatKing getInstance() {
        if(instance == null) {
            instance = new BRFormRatKing();
        }

        return instance;
    }


    @Override
    public void execute() throws GameActionException {
        if (rc.canBecomeRatKing() && currentLocation.distanceSquaredTo(closestAllyRatKing.loc()) > 25) {
            rc.becomeRatKing();
            RatKing.initializeRatKing();
            rc.writeSharedArray(20, 0);
            rc.writeSharedArray(21, 0);
        }
        else if (rc.getLocation().distanceSquaredTo(targetMineForFormation) > 2){
            Pathfinding.attemptMove(targetMineForFormation, true);
        }
        else if (rc.getLocation().distanceSquaredTo(targetMineForFormation) <= 2 && rc.canSenseLocation(targetMineForFormation) && rc.senseRobotAtLocation(targetMineForFormation) == null) {
            if (rc.canTurn()) rc.turn(rc.getLocation().directionTo(targetMineForFormation));
            if (rc.canMove(rc.getLocation().directionTo(targetMineForFormation))) rc.move(rc.getLocation().directionTo(targetMineForFormation));
        }
        currentLocation = rc.getLocation();
        if (rc.canBecomeRatKing() && currentLocation.distanceSquaredTo(closestAllyRatKing.loc()) > 25) {
            rc.becomeRatKing();
            RatKing.initializeRatKing();
            rc.writeSharedArray(20, 0);
            rc.writeSharedArray(21, 0);
        }
        else if (currentLocation.distanceSquaredTo(targetMineForFormation) <= 8) {
            if (rc.canTurn()) rc.turn(rc.getDirection().rotateLeft());
            Utilities.attemptDig();
        }
    }

}
