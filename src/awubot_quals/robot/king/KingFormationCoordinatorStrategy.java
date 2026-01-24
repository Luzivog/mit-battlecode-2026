package awubot_quals.robot.king;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot_quals.comms.GlobalArray;
import awubot_quals.robot.Strategy;
import awubot_quals.RobotTracker;
import static awubot_quals.Game.*;
import static awubot_quals.util.Util.*;

import awubot_quals.Game;

public class KingFormationCoordinatorStrategy extends Strategy {
    private static final int[] CHEESE_THRESHOLDS = {800, 1000, 1200, 1800, 1000000000};
    private static final int UPGRADE_COST = 50;
    private static final int MIN_DIST_FROM_KINGS_SQ = 100;
    private static final int MAX_FORMATION_DIST_TO_MINE_SQ = 25;
    private static final int COORDINATION_TIMEOUT = 120;
    private static final int FORMATION_COOLDOWN = 150;
    private static final int MIN_TURN_FOR_FORMATION = 200;

    private final ThreatContext context;
    private final KingMiningStrategy miningStrategy;

    private final int tabooMinX;
    private final int tabooMaxX;
    private final int tabooMinY;
    private final int tabooMaxY;

    private FormationState state = FormationState.IDLE;
    private MapLocation formationTarget = null;
    private MapLocation targetMine = null;
    private int coordinationStartTurn = 0;
    private int lastFormationTurn = -FORMATION_COOLDOWN;
    private int kingCount = 1;

    private enum FormationState {
        IDLE,
        COORDINATING,
        MONITORING
    }

    public KingFormationCoordinatorStrategy(ThreatContext aContext, KingMiningStrategy aMiningStrategy) {
        context = aContext;
        miningStrategy = aMiningStrategy;

        if (mapWidth < 40 && mapHeight < 40) {
            tabooMinX = mapWidth / 3;
            tabooMaxX = 2 * mapWidth / 3;
            tabooMinY = mapHeight / 3;
            tabooMaxY = 2 * mapHeight / 3;
        } else if (mapWidth < 50 && mapHeight < 50) {
            tabooMinX = 3 * mapWidth / 7;
            tabooMaxX = 4 * mapWidth / 7;
            tabooMinY = 3 * mapHeight / 7;
            tabooMaxY = 4 * mapHeight / 7;
        } else {
            tabooMinX = 4 * mapWidth / 9;
            tabooMaxX = 5 * mapWidth / 9;
            tabooMinY = 4 * mapHeight / 9;
            tabooMaxY = 5 * mapHeight / 9;
        }
    }

    private boolean isInTabooZone(MapLocation aLoc) {
        return aLoc.x >= tabooMinX && aLoc.x <= tabooMaxX
            && aLoc.y >= tabooMinY && aLoc.y <= tabooMaxY;
    }

    private int getCheeseThreshold(int aCurrentKingCount) {
        int myIndex = Math.max(0, Math.min(aCurrentKingCount - 1, CHEESE_THRESHOLDS.length - 1));
        return CHEESE_THRESHOLDS[myIndex];
    }

    @Override
    public boolean runTick() throws GameActionException {
        int myKingCount = GlobalArray.countRatKings();
        int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;

        if (myKingCount >= 5 || time >= 1100 || numRats < 15 * myKingCount) {
            abortFormation();
            kingCount = myKingCount;
            return true;
        }

        if (myKingCount > kingCount) {
            abortFormation();
            kingCount = myKingCount;
            return true;
        }
        kingCount = myKingCount;

        if (context.defenseState != ThreatContext.DefenseState.NORMAL) {
            if (state == FormationState.COORDINATING) {
                abortFormation();
            }
            return true;
        }

        switch (state) {
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
        if (time - lastFormationTurn < FORMATION_COOLDOWN) return;
        int myKingCount = GlobalArray.countRatKings();
        if (myKingCount == 5) return;
        if (myGlobalCheese < getCheeseThreshold(myKingCount) + UPGRADE_COST) return;
        int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;
        if (numRats < 15 * myKingCount) return;

        if (GlobalArray.getFormationTarget() != null) return;

        MapLocation myBestMine = selectBestMineForFormation();
        if (myBestMine == null) return;

        MapLocation myFormationSpot = findFormationSpotNearMine(myBestMine);
        if (myFormationSpot == null) return;

        formationTarget = myFormationSpot;
        targetMine = myBestMine;
        coordinationStartTurn = time;
        state = FormationState.COORDINATING;

        GlobalArray.writeFormationTarget(formationTarget, targetMine, kingId);
        GlobalArray.writeKingMineAssignment(kingId + 1, miningStrategy.getMineTarget());

        indicate("FORMATION: coordinating at " + formationTarget);
    }

    private MapLocation selectBestMineForFormation() throws GameActionException {
        MapLocation myBestMine = null;
        int myBestScore = Integer.MIN_VALUE;

        int myMineCount = Game.knownCheeseMines.set.length();
        for (int i = 0; i < myMineCount && i < 10; i++) {
            MapLocation myMine = decode2x2(Game.knownCheeseMines.at(i));
            if (myMine == null) continue;

            if (GlobalArray.isMineAssignedToAnyKing(myMine)) continue;
            if (isInTabooZone(myMine)) continue;

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
                if (myDist < MIN_DIST_FROM_KINGS_SQ) {
                    myScore -= 500;
                }

                // we want to spawn other kings pretty far away
                myScore += myDist;
            }
        }

        if (rc.canSenseLocation(aMine)) {
            myScore += scoreNearbyAlliedRats(aMine, 49);  

            myScore += scoreNearbyEnemyRats(aMine, 49);
        }

        int myDistFromSelf = myLoc.distanceSquaredTo(aMine);
        if (myDistFromSelf > 400) {
            myScore -= 50;
        }

        return myScore;
    }

    private int scoreNearbyAlliedRats(MapLocation aCenter, int aRadiusSq) throws GameActionException {
        int myCount = 0;
        for (int i = 0; i < RobotTracker.numAllies; i++) {
            RobotInfo myRobot = RobotTracker.sortedAllies[i];
            if (myRobot == null) continue;
            if (myRobot.getType() != UnitType.BABY_RAT) continue;
            if (myRobot.location.distanceSquaredTo(aCenter) > aRadiusSq) continue;
            myCount += 20;
        }
        return myCount;
    }

    private int scoreNearbyEnemyRats(MapLocation aCenter, int aRadiusSq) throws GameActionException {
        int myCount = 0;
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo myRobot = RobotTracker.sortedEnemies[i];
            if (myRobot == null) continue;
            if (myRobot.getType() == UnitType.RAT_KING) myCount -= 5000;
            if (myRobot.getType() != UnitType.BABY_RAT) continue;
            if (myRobot.location.distanceSquaredTo(aCenter) > aRadiusSq) continue;
            myCount -= 15;
        }
        return myCount;
    }

    private MapLocation findFormationSpotNearMine(MapLocation aMine) throws GameActionException {
        int myBestDistFromMine = Integer.MAX_VALUE;
        MapLocation myBestSpot = null;

        for (int myDx = -5; myDx <= 5; myDx++) {
            for (int myDy = -5; myDy <= 5; myDy++) {
                int myDistToMine = myDx * myDx + myDy * myDy;
                if (myDistToMine > MAX_FORMATION_DIST_TO_MINE_SQ) continue;

                MapLocation myCandidate = aMine.translate(myDx, myDy);
                if (!rc.onTheMap(myCandidate)) continue;

                if (!isValidFormationCenter(myCandidate)) continue;

                if (myDistToMine < myBestDistFromMine) {
                    myBestDistFromMine = myDistToMine;
                    myBestSpot = myCandidate;
                }
            }
        }

        return myBestSpot;
    }

    private boolean isValidFormationCenter(MapLocation aCenter) throws GameActionException {
        if (isInTabooZone(aCenter)) return false;

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
        if (time - coordinationStartTurn > COORDINATION_TIMEOUT) {
            abortFormation();
            return;
        }

        if (rc.getGlobalCheese() < UPGRADE_COST) {
            abortFormation();
            return;
        }

        MapLocation myCurrentTarget = GlobalArray.getFormationTarget();
        if (myCurrentTarget == null) {
            state = FormationState.IDLE;
            lastFormationTurn = time;
            indicate("FORMATION: completed or cleared");
            return;
        }

        if (context.nearestCat != null &&
            formationTarget != null &&
            context.nearestCat.location.distanceSquaredTo(formationTarget) < 625) {
            abortFormation();
            return;
        }

        indicate("FORMATION: monitoring " + formationTarget);
    }

    private void abortFormation() throws GameActionException {
        int myCoordinator = GlobalArray.getFormationCoordinator();
        if (myCoordinator == kingId) {
            GlobalArray.clearFormation();
        }
        state = FormationState.IDLE;
        formationTarget = null;
        targetMine = null;
        lastFormationTurn = time;
        indicate("FORMATION: aborted");
    }
}
