package awubot.robot.rat.strategies;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.rat.BabyRat;
import awubot.robot.rat.BabyRatRole;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class FormationStrategy extends Strategy {
    private static final int FORMATION_TIMEOUT = 45;
    private static final int MIN_ALLIES_FOR_FORMATION = 6;
    private static final int MAX_DIST_TO_JOIN = 144;

    private MapLocation theFormationTarget = null;
    private MapLocation theTargetMine = null;
    private int theFormationStartTurn = 0;
    private boolean theActive = false;

    @Override
    public boolean isComplete() throws GameActionException {
        if (!theActive) return true;
        if (theFormationTarget == null) return true;
        if (time - theFormationStartTurn > FORMATION_TIMEOUT) return true;
        if (rc.getGlobalCheese() < 50) return true;
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        if (!theActive) return;

        if (rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            return;
        }

        MapLocation myCurrentTarget = GlobalArray.getFormationTarget();
        if (myCurrentTarget == null || !myCurrentTarget.equals(theFormationTarget)) {
            theActive = false;
            return;
        }

        int myNearbyAllies = countNearbyAllies();

        if (myNearbyAllies >= MIN_ALLIES_FOR_FORMATION) {
            if (myLoc.distanceSquaredTo(theFormationTarget) <= 2) {
                tryMoveToCenter();
                return;
            }
        }

        Pathfinder.makeMove(theFormationTarget);
    }

    public boolean tryActivate() throws GameActionException {
        if (BabyRat.role != BabyRatRole.MINER) return false;

        MapLocation myTarget = GlobalArray.getFormationTarget();
        if (myTarget == null) return false;

        MapLocation myMine = GlobalArray.getFormationMine();
        if (myMine == null) return false;

        int myDistToMine = myLoc.distanceSquaredTo(myMine);
        if (myDistToMine > MAX_DIST_TO_JOIN) return false;

        theFormationTarget = myTarget;
        theTargetMine = myMine;
        theFormationStartTurn = time;
        theActive = true;
        return true;
    }

    public void deactivate() {
        theActive = false;
        theFormationTarget = null;
        theTargetMine = null;
    }

    public boolean isActive() {
        return theActive;
    }

    private int countNearbyAllies() throws GameActionException {
        int myCount = 0;
        for (RobotInfo myRobot : rc.senseNearbyRobots(8, rc.getTeam())) {
            if (myRobot.getType() == UnitType.BABY_RAT) {
                myCount++;
            }
        }
        return myCount;
    }

    private void tryMoveToCenter() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (myLoc.equals(theFormationTarget)) return;

        int myDistSq = myLoc.distanceSquaredTo(theFormationTarget);
        if (myDistSq > 2) {
            Pathfinder.makeMove(theFormationTarget);
            return;
        }

        if (shouldBecomeCenter()) {
            Direction myDir = myLoc.directionTo(theFormationTarget);
            if (rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
            }
        }
    }

    private boolean shouldBecomeCenter() throws GameActionException {
        RobotInfo myRobotAtCenter = rc.senseRobotAtLocation(theFormationTarget);
        if (myRobotAtCenter != null) {
            return false;
        }

        int myLowerIdCount = 0;
        for (RobotInfo myRobot : rc.senseNearbyRobots(2, rc.getTeam())) {
            if (myRobot.getType() == UnitType.BABY_RAT && myRobot.getID() < rc.getID()) {
                if (myRobot.getLocation().distanceSquaredTo(theFormationTarget) <= 2) {
                    myLowerIdCount++;
                }
            }
        }

        return myLowerIdCount == 0;
    }
}
