package opp_parmesan.Roles;

import battlecode.common.*;
import opp_parmesan.Baby;
import opp_parmesan.King;
import opp_parmesan.Utils.*;

public class Multiking extends Globals {
    public static MapLocation newKingPos = null;
    public static int roundsCircled = 0;
    public static boolean isMultiking () throws GameActionException {
        newKingPos = Comms.needNewKing();
        return Comms.getNumberBots() <= botsForNewKing && newKingPos != null;
    }
    
    public static MapLocation targetPos = null; 
    
    public static void moveTo () throws GameActionException {
        if (rc.getLocation().equals(newKingPos)) return;
        if (rc.getLocation().distanceSquaredTo(newKingPos) <= 2) {
            Direction dir = rc.getLocation().directionTo(newKingPos);
            if (rc.canMove(dir)) {
                Vision.turn(dir);
                rc.move(dir);
            }
            return;
        }
        if (targetPos != null && rc.canSenseRobotAtLocation(targetPos)) {
            targetPos = null;
        }
        if (targetPos == null && rc.getLocation().distanceSquaredTo(newKingPos) <= 8) {
            Vision.turn(rc.getLocation().directionTo(newKingPos));
            MapLocation pos = newKingPos;
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()));
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()).rotateLeft());
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()).rotateRight());
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()).rotateLeft().rotateLeft());
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()).rotateRight().rotateRight());
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()).opposite().rotateRight());
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()).opposite().rotateLeft());
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            pos = newKingPos.add(newKingPos.directionTo(rc.getLocation()).opposite());
            if (!rc.canSenseRobotAtLocation(pos)) {
                targetPos = pos;
                BugNavigator.moveTo(targetPos, true);
                return;
            }
            return;
        }
        if (targetPos == null) {
            Navigator.moveTo(newKingPos, true);
            return;
        }
        BugNavigator.moveTo(targetPos, true);
    }
    
    
    public static void run () throws GameActionException {
        for (MapLocation ml: kingPos) {
            if (ml == null) break;
            if (ml.distanceSquaredTo(newKingPos) <= closeToKing) {
                Baby.isNewKing = false;
                return;
            }
        }
        rc.setIndicatorString(newKingPos.toString());
        moveTo();
        if (rc.getLocation().distanceSquaredTo(newKingPos) <= 8) {
            roundsCircled++;
            Comms.squeakForSacrifices(newKingPos);
        }
        if (rc.getLocation().distanceSquaredTo(newKingPos) <= 8 && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            King.run();
            return;
        }
        if (roundsCircled >= giveUpNewKing) {
            Baby.isNewKing = false;
        }
    }
}
