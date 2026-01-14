package awubot.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot.Pathfinder;
import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.Game.*;

// TODO: refactor this (AI slop)

public class FormationStrategy extends Strategy {
    private static final int FORMATION_TIMEOUT = 45;
    private static final int MIN_ALLIES_FOR_FORMATION = 6;
    private static final int MAX_DIST_TO_JOIN = 144;

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
        if(isComplete()) return false;
        tryActivate();
        if (rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            return true;
        }
        if (RobotTracker.allyCount >= MIN_ALLIES_FOR_FORMATION) {
            if (myLoc.distanceSquaredTo(formationTarget) <= 2) {
                tryMoveToCentre();
                return true;
            }
        }
        Pathfinder.makeMove(formationTarget);
        return true;
    }

    public boolean tryActivate() throws GameActionException {
        MapLocation target = GlobalArray.getFormationTarget();
        if (target == null) return false;

        MapLocation mine = GlobalArray.getFormationMine();
        if (mine == null) return false;

        int distToMine = myLoc.distanceSquaredTo(mine);
        if (distToMine > MAX_DIST_TO_JOIN) return false;

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
               && robot.getLocation().distanceSquaredTo(formationTarget) <= 2) {
                return false;
            }
        }

        return true;
    }
}
