package awubot_sprint2.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot_sprint2.Game;
import awubot_sprint2.Pathfinder;
import awubot_sprint2.RobotPlayer;
import awubot_sprint2.RobotTracker;
import awubot_sprint2.comms.GlobalArray;
import awubot_sprint2.robot.Strategy;
import static awubot_sprint2.Game.*;
import static awubot_sprint2.util.Util.indicate;
import static awubot_sprint2.util.Util.println;

// TODO: refactor this (AI slop)

public class FormationStrategy extends Strategy {
    private static final int FORMATION_TIMEOUT = 45;
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
        indicate("RAT_FORM: active=" + active + " target=" + formationTarget + " allies=" + RobotTracker.allyCount + " canBeKing=" + rc.canBecomeRatKing());
        if (rc.canBecomeRatKing()) {
            indicate("RAT_FORM: BECOMING KING!");
            rc.becomeRatKing();
            Game.convertKing();
            GlobalArray.writeRatKing();
            RobotPlayer.resetHasInitialisedRobot();
            return true;
        }
        if (RobotTracker.allyCount >= MIN_ALLIES_FOR_FORMATION) {
            if (myLoc.distanceSquaredTo(formationTarget) <= 2) {
                indicate("RAT_FORM: at target, trying centre");
                tryMoveToCentre();
                return true;
            }
        }
        indicate("RAT_FORM: moving to " + formationTarget);
        Pathfinder.makeMove(formationTarget);
        return true;
    }

    public boolean tryActivate() throws GameActionException {
        MapLocation target = GlobalArray.getFormationTarget();
        if (target == null) return false;

        MapLocation mine = GlobalArray.getFormationMine();
        if (mine == null) return false;

        int distToMine = myLoc.distanceSquaredTo(mine);
        if (distToMine > MAX_DIST_TO_JOIN) {
            indicate("RAT_FORM_ACT: too far from mine dist=" + distToMine + " max=" + MAX_DIST_TO_JOIN);
            return false;
        }

        indicate("RAT_FORM_ACT: JOINED! target=" + target + " mine=" + mine);
        formationTarget = target;
        formationStartTurn = time;
        active = true;
        return true;
    }

    private void tryMoveToCentre() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (myLoc.equals(formationTarget)) return;

        if (myLoc.distanceSquaredTo(formationTarget) > 2) {
            Pathfinder.makeMove(formationTarget);
            return;
        }

        if (shouldBecomeCentre()) {
            Direction myDir = myLoc.directionTo(formationTarget);
            if (rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
            }
        }
    }

    private boolean shouldBecomeCentre() throws GameActionException {
        RobotInfo robotAtCentre = rc.senseRobotAtLocation(formationTarget);
        if (robotAtCentre != null) {
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
