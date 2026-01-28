package awubot_sus.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot_sus.Game;
import awubot_sus.Pathfinder;
import awubot_sus.RobotPlayer;
import awubot_sus.RobotTracker;
import awubot_sus.comms.GlobalArray;
import awubot_sus.robot.Strategy;
import static awubot_sus.Game.*;
import static awubot_sus.util.Util.indicate;

// TODO: refactor this (AI slop)

public class FormationStrategy extends Strategy {
    private static final int FORMATION_TIMEOUT = 100;
    private static final int MIN_ALLIES_FOR_FORMATION = 7;
    private static final int MAX_DIST_TO_JOIN = 625; 

    private MapLocation formationTarget = null;
    private int formationStartTurn = 0;
    private boolean active = false;

    private boolean isComplete() throws GameActionException {
        if(!active) return true;
        if(formationTarget == null
           || time - formationStartTurn > FORMATION_TIMEOUT
           || rc.getGlobalCheese() < 50) {
            active = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean runTick() throws GameActionException {
        tryActivate();
        if(isComplete()) return false;

        indicate("FORM: active, target=" + formationTarget + " allies=" + RobotTracker.allyCount + " dist=" + myLoc.distanceSquaredTo(formationTarget));

        if (rc.canBecomeRatKing()) {
            indicate("FORM: BECOMING KING!");
            rc.becomeRatKing();
            Game.convertKing();
            GlobalArray.clearFormation();  
            RobotPlayer.resetHasInitialisedRobot();
            return true;
        }
        if (RobotTracker.allyCount >= MIN_ALLIES_FOR_FORMATION) {
            if (myLoc.distanceSquaredTo(formationTarget) <= 2) {
                indicate("FORM: trying to move to centre");
                tryMoveToCentre();
                return true;
            }
        }
        BabyRat.autoMove(Pathfinder.getMove(formationTarget));
        return true;
    }

    public boolean tryActivate() throws GameActionException {
        MapLocation target = GlobalArray.getFormationTarget();
        if (target == null) {
            if (active) {
                active = false;
                formationTarget = null;
            }
            return false;
        }

        MapLocation mine = GlobalArray.getFormationMine();
        if (mine == null) {
            if (active) {
                active = false;
                formationTarget = null;
            }
            return false;
        }

        formationTarget = target;

        if (active) return true;

        int distToMine = myLoc.distanceSquaredTo(mine);
        if (distToMine > MAX_DIST_TO_JOIN) return false;

        formationStartTurn = time;
        active = true;
        return true;
    }

    private void tryMoveToCentre() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (myLoc.equals(formationTarget)) return;

        if (myLoc.distanceSquaredTo(formationTarget) > 2 || shouldBecomeCentre()) {
            BabyRat.autoMove(Pathfinder.getMove(formationTarget));
            return;
        }
    }

    private boolean shouldBecomeCentre() throws GameActionException {
        
        if (rc.canSenseRobotAtLocation(formationTarget)) {
            RobotInfo robotAtCentre = rc.senseRobotAtLocation(formationTarget);
            if (robotAtCentre != null) return false;
        } else {
            return false;
        }

        for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
            if(robot.getType() == UnitType.BABY_RAT && robot.getID() < rc.getID()
               && robot.location.distanceSquaredTo(formationTarget) <= 2) {
                return false;
            }
        }

        return true;
    }
}
