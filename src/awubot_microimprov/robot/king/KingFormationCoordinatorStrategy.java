package awubot_microimprov.robot.king;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot_microimprov.comms.GlobalArray;
import awubot_microimprov.robot.Strategy;
import static awubot_microimprov.Game.*;
import static awubot_microimprov.util.Util.*;

import awubot_microimprov.Game;

public class KingFormationCoordinatorStrategy extends Strategy {
    private static final int CHEESE_THRESHOLD = 150;
    private static final int UPGRADE_COST = 50;
    private static final int MIN_DIST_FROM_KINGS_SQ = 225;
    private static final int COORDINATION_TIMEOUT = 40;
    private static final int FORMATION_COOLDOWN = 80;
    private static final int MIN_TURN_FOR_FORMATION = 100;

    private final ThreatContext theContext;
    private final KingMiningStrategy theMiningStrategy;

    private FormationState theState = FormationState.IDLE;
    private MapLocation theFormationTarget = null;
    private MapLocation theTargetMine = null;
    private int theCoordinationStartTurn = 0;
    private int theLastFormationTurn = -FORMATION_COOLDOWN;

    private enum FormationState {
        IDLE,
        COORDINATING,
        MONITORING
    }

    public KingFormationCoordinatorStrategy(ThreatContext aContext, KingMiningStrategy aMiningStrategy) {
        theContext = aContext;
        theMiningStrategy = aMiningStrategy;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if (theContext.theDefenseState != ThreatContext.DefenseState.NORMAL) {
            if (theState == FormationState.COORDINATING) {
                abortFormation();
            }
            return true;
        }

        switch (theState) {
            case IDLE:
                tryStartFormation();
                break;
            case COORDINATING:
            case MONITORING:
                monitorFormation();
                break;
        }
        return true;
    }

    private void tryStartFormation() throws GameActionException {
        if (time < MIN_TURN_FOR_FORMATION) return;
        int myGlobalCheese = rc.getGlobalCheese();
        if (time - theLastFormationTurn < FORMATION_COOLDOWN) return;
        if (myGlobalCheese < CHEESE_THRESHOLD + UPGRADE_COST) return;
        int myKingCount = GlobalArray.countActiveKings();
        if (myKingCount >= 3) return;
        if (GlobalArray.getFormationTarget() != null) return;

        MapLocation myBestMine = selectBestMineForFormation();
        if (myBestMine == null) return;

        MapLocation myFormationSpot = findFormationSpotNearMine(myBestMine);
        if (myFormationSpot == null) return;

        theFormationTarget = myFormationSpot;
        theTargetMine = myBestMine;
        theCoordinationStartTurn = time;
        theState = FormationState.COORDINATING;

        GlobalArray.writeFormationTarget(theFormationTarget, theTargetMine, kingId);
        GlobalArray.writeKingMineAssignment(kingId, theMiningStrategy.getMineTarget());

        indicate("FORMATION: coordinating at " + theFormationTarget);
    }

    private MapLocation selectBestMineForFormation() throws GameActionException {
        MapLocation myBestMine = null;
        int myBestScore = Integer.MIN_VALUE;

        int myMineCount = Game.knownCheeseMines.set.length();
        for (int i = 0; i < myMineCount && i < 10; i++) {
            MapLocation myMine = decode2x2(Game.knownCheeseMines.at(i));
            if (myMine == null) continue;

            if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;

            int myScore = scoreMineForFormation(myMine);
            if (myScore > myBestScore) {
                myBestScore = myScore;
                myBestMine = myMine;
            }
        }

        return myBestMine;
    }

    private int scoreMineForFormation(MapLocation aMine) throws GameActionException {
        int myScore = 0;

        for (int i = 0; i < 5; i++) {
            MapLocation myKingLoc = GlobalArray.getRatKing(i);
            if (myKingLoc != null) {
                int myDist = aMine.distanceSquaredTo(myKingLoc);
                myScore += Math.min(myDist, 400);
            }
        }

        int myDistFromSelf = myLoc.distanceSquaredTo(aMine);
        if (myDistFromSelf < 100) {
            myScore -= 200;
        }

        if (rc.canSenseLocation(aMine)) {
            int myNearbyRats = countNearbyAlliedRats(aMine, 25);
            myScore += myNearbyRats * 100;
        }

        return myScore;
    }

    private int countNearbyAlliedRats(MapLocation aCenter, int aRadiusSq) throws GameActionException {
        int myCount = 0;
        for (RobotInfo myRobot : rc.senseNearbyRobots(aCenter, aRadiusSq, rc.getTeam())) {
            if (myRobot.getType() == UnitType.BABY_RAT) {
                myCount++;
            }
        }
        return myCount;
    }

    private MapLocation findFormationSpotNearMine(MapLocation aMine) throws GameActionException {
        int myBestDistFromMine = Integer.MAX_VALUE;
        MapLocation myBestSpot = null;

        for (int myDx = -3; myDx <= 3; myDx++) {
            for (int myDy = -3; myDy <= 3; myDy++) {
                MapLocation myCandidate = aMine.translate(myDx, myDy);
                if (!rc.onTheMap(myCandidate)) continue;

                if (!isValidFormationCenter(myCandidate)) continue;

                int myDistToMine = myCandidate.distanceSquaredTo(aMine);
                if (myDistToMine < myBestDistFromMine) {
                    myBestDistFromMine = myDistToMine;
                    myBestSpot = myCandidate;
                }
            }
        }

        return myBestSpot;
    }

    private boolean isValidFormationCenter(MapLocation aCenter) throws GameActionException {
        for (int i = 0; i < 5; i++) {
            MapLocation myKingLoc = GlobalArray.getRatKing(i);
            if (myKingLoc != null && aCenter.distanceSquaredTo(myKingLoc) < MIN_DIST_FROM_KINGS_SQ) {
                return false;
            }
        }

        if (rc.canSenseLocation(aCenter)) {
            for (int myDx = -1; myDx <= 1; myDx++) {
                for (int myDy = -1; myDy <= 1; myDy++) {
                    MapLocation myTile = aCenter.translate(myDx, myDy);
                    if (!rc.onTheMap(myTile)) return false;

                    if (rc.canSenseLocation(myTile)) {
                        MapInfo myInfo = rc.senseMapInfo(myTile);
                        if (!myInfo.isPassable()) return false;

                        RobotInfo myRobot = rc.senseRobotAtLocation(myTile);
                        if (myRobot != null) {
                            if (myRobot.getType() == UnitType.RAT_KING || myRobot.getType() == UnitType.CAT) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private void monitorFormation() throws GameActionException {
        if (time - theCoordinationStartTurn > COORDINATION_TIMEOUT) {
            abortFormation();
            return;
        }

        if (rc.getGlobalCheese() < UPGRADE_COST) {
            abortFormation();
            return;
        }

        MapLocation myCurrentTarget = GlobalArray.getFormationTarget();
        if (myCurrentTarget == null) {
            theState = FormationState.IDLE;
            theLastFormationTurn = time;
            indicate("FORMATION: completed or cleared");
            return;
        }

        if (theContext.theNearestCat != null &&
            theFormationTarget != null &&
            theContext.theNearestCat.location.distanceSquaredTo(theFormationTarget) < 625) {
            abortFormation();
            return;
        }

        indicate("FORMATION: monitoring " + theFormationTarget);
    }

    private void abortFormation() throws GameActionException {
        int myCoordinator = GlobalArray.getFormationCoordinator();
        if (myCoordinator == kingId) {
            GlobalArray.clearFormation();
        }
        theState = FormationState.IDLE;
        theFormationTarget = null;
        theTargetMine = null;
        theLastFormationTurn = time;
        indicate("FORMATION: aborted");
    }
}
