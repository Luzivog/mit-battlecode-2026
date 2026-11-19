package challenger.robot.king;

import battlecode.common.*;
import challenger.comms.GlobalArray;
import challenger.robot.Strategy;
import challenger.RobotTracker;
import static challenger.Game.*;
import static challenger.util.Util.*;

import challenger.Game;

public class KingFormationCoordinatorStrategy extends Strategy {
    private static final int[] MINE_THRESHOLDS = {3, 5, 9, 15, 10000000};
    private static final int UPGRADE_COST = 50;
    private static final int MAX_FORMATION_DIST_TO_MINE_SQ = 25;
    private static final int COORDINATION_TIMEOUT = 120;

    // King lock-in: kings formed by RAT_KING_CUTOFF_ROUND survive to the end, after it a team with 1 king can form only
    // a second (rules.md §6). From LOCK_IN_ROUND, below KING_TARGET kings, form with only the rat floor.
    private static final int LOCK_IN_ROUND = 800;
    private static final int KING_TARGET = 3;
    private static final int LOCK_IN_MIN_RATS = 12;
    // A lock-in king pays upkeep to the end and its formation consumes ~8 rats to rebuild: before the cutoff, form only
    // with a 120-round upkeep reserve for 3 kings (720) + the formation (50) + the rebuild (~280). Measured: lock-in
    // formations started below 1000 global starved 22 of 38 games and won 5 fewer than main, above it 12 more.
    private static final int LOCK_IN_MIN_CHEESE = 1000;

    private static int MIN_DIST_FROM_KINGS_SQ;

    private final KingMiningStrategy miningStrategy;

    private final int tabooMinX;
    private final int tabooMaxX;
    private final int tabooMinY;
    private final int tabooMaxY;

    private FormationState state = FormationState.IDLE;
    private MapLocation formationTarget = null;
    private MapLocation targetMine = null;
    private int coordinationStartTurn = 0;
    private int kingCount = 1;
    private int lastSeenKingCount = 1;
    private int kings = 1;
    private boolean lockIn = false;
    private RobotInfo nearestCat = null;

    private enum FormationState {
        IDLE,
        COORDINATING,
        MONITORING
    }

    public KingFormationCoordinatorStrategy(KingMiningStrategy aMiningStrategy) {
        MIN_DIST_FROM_KINGS_SQ = Math.max(400, mapWidth * mapHeight / 5);
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


    private boolean isNormal() {
        final int CAT_CRITICAL_DIST_SQ = 16;
        final int CAT_DANGER_DIST_SQ = 36;
        final int CRITICAL_HP = 150;

        RobotInfo nearestEnemyRat = null;
        RobotInfo nearestEnemyRatKing = null;
        nearestCat = null;

        int catDistSq = Integer.MAX_VALUE;
        int enemyRatDistSq = Integer.MAX_VALUE;
        int enemyRatKingDistSq = Integer.MAX_VALUE;

        int catLimit = RobotTracker.numCats;
        if (RobotTracker.sortedCats != null) catLimit = Math.min(catLimit, RobotTracker.sortedCats.length);

        for (int i = 0; i < catLimit; i++) {
            RobotInfo myCat = RobotTracker.sortedCats[i];
            if (myCat == null) continue;

            int myDistSq = myLoc.distanceSquaredTo(myCat.location);
            if (myDistSq < catDistSq) {
                catDistSq = myDistSq;
                nearestCat = myCat;
            }
        }

        int enemyLimit = RobotTracker.numEnemies;
        if (RobotTracker.sortedEnemies != null) enemyLimit = Math.min(enemyLimit, RobotTracker.sortedEnemies.length);

        for (int i = 0; i < enemyLimit; i++) {
            RobotInfo myEnemy = RobotTracker.sortedEnemies[i];
            if (myEnemy == null) continue;

            int myDistSq = myLoc.distanceSquaredTo(myEnemy.location);
            UnitType t = myEnemy.getType();

            if (t == UnitType.RAT_KING) {
                if (myDistSq < enemyRatKingDistSq) {
                    enemyRatKingDistSq = myDistSq;
                    nearestEnemyRatKing = myEnemy;
                }
            } else if (t == UnitType.BABY_RAT) {
                if (myDistSq < enemyRatDistSq) {
                    enemyRatDistSq = myDistSq;
                    nearestEnemyRat = myEnemy;
                }
            }
        }

        if (nearestEnemyRatKing != null && enemyRatKingDistSq <= 25) {
            return false;
        }

        int myHp = rc.getHealth();

        if (myHp <= CRITICAL_HP || catDistSq <= CAT_CRITICAL_DIST_SQ) {
            return false;
        } else if (catDistSq <= CAT_DANGER_DIST_SQ || (enemyRatDistSq <= 8 && myHp <= 250)) {
            return false;
        } else if (nearestCat != null || nearestEnemyRat != null) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public boolean runTick() throws GameActionException {
        // A king whose turn overran misses one liveness write and reads as dead for one round (GlobalArray
        // updateRatKingTurnCounters): count a king as gone only when it is missing two rounds in a row.
        int mySeenKingCount = GlobalArray.countRatKings();
        int myKingCount = Math.max(mySeenKingCount, lastSeenKingCount);
        lastSeenKingCount = mySeenKingCount;
        kings = myKingCount;
        int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;

        var req = 12;
        if(mapWidth * mapHeight <= 1200) {
            req = 8;
        }
        lockIn = time >= LOCK_IN_ROUND && myKingCount < KING_TARGET;
        int minRats = lockIn ? LOCK_IN_MIN_RATS : req * myKingCount;
        if (myKingCount >= 5
            || (time > GameConstants.RAT_KING_CUTOFF_ROUND && myKingCount >= GameConstants.MAX_NUMBER_OF_RAT_KINGS_AFTER_CUTOFF)
            || numRats < minRats) {
            // indicate("FORMATION: aborting due to king count or rat count");
            // indicate("numRats: " + numRats + ", myKingCount: " + myKingCount);
            abortFormation();
            kingCount = myKingCount;
            return true;
        }

        if (myKingCount > kingCount) {
            // indicate("FORMATION: aborting due to increased king count");
            abortFormation();
            kingCount = myKingCount;
            return true;
        }
        kingCount = myKingCount;

        // if (!isNormal()) {
        //     if (state == FormationState.COORDINATING) {
        //         abortFormation();
        //     }
        //     return true;
        // }

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

    boolean danger;
    private void tryStartFormation() throws GameActionException {
        danger = false;
        if(rc.getHealth() < 100 && kings == 1) {
            danger = true;
        } else {
            // indicate("A");
            int myGlobalCheese = rc.getGlobalCheese();
            if(myGlobalCheese < (lockIn && time <= GameConstants.RAT_KING_CUTOFF_ROUND ? LOCK_IN_MIN_CHEESE : UPGRADE_COST + 100)) return;
            // indicate("B");
            int myKingCount = kings;
            if (myKingCount == 5) return;
            // indicate("C");
            int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;
            if(!lockIn) {
                if (Game.knownCheeseMines.size() < MINE_THRESHOLDS[myKingCount]) return;
                // indicate("D");
                if(mapWidth * mapHeight > 1200) {
                    if (numRats < 20 * myKingCount) return;
                } else {
                    if (numRats < 20 * myKingCount - 4) return;
                }
            } else {
                if (numRats < LOCK_IN_MIN_RATS) return;
            }
        }
        // indicate("passed formation checks");

        if (GlobalArray.getFormationTarget() != null) return;

        MapLocation formationTarget = selectBestMineForFormation();
        if (formationTarget == null) return;

        targetMine = formationTarget;
        coordinationStartTurn = time;
        state = FormationState.COORDINATING;

        GlobalArray.writeFormationTarget(formationTarget, targetMine, kingId);
        GlobalArray.writeKingMineAssignment(kingId + 1, miningStrategy.getMineTarget());

        // indicate("FORMATION: coordinating at " + formationTarget);
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
        if(!lockIn && myBestScore < -40000) {
            return null;
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
                    myScore -= 50000;
                }

                myScore -= myDist;
            }
        }

        myScore -= aMine.distanceSquaredTo(new MapLocation(mapWidth / 2, mapHeight / 2));

        int myDistFromSelf = myLoc.distanceSquaredTo(aMine);
        if (myDistFromSelf > 400) {
            myScore -= 50;
        }

        return myScore;
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

        return true;
    }

    private void monitorFormation() throws GameActionException {
        if (time - coordinationStartTurn > COORDINATION_TIMEOUT) {
            // indicate("FORMATION: coordination timed out");
            abortFormation();
            return;
        }

        if (rc.getGlobalCheese() < UPGRADE_COST) {
            // indicate("FORMATION: insufficient cheese");
            abortFormation();
            return;
        }

        MapLocation myCurrentTarget = GlobalArray.getFormationTarget();
        if (myCurrentTarget == null) {
            state = FormationState.IDLE;
            // indicate("FORMATION: completed or cleared");
            return;
        }

        if (nearestCat != null &&
            formationTarget != null &&
            nearestCat.location.distanceSquaredTo(formationTarget) < 625) {
            abortFormation();
            return;
        }

        // indicate("FORMATION: monitoring " + formationTarget);
    }

    private void abortFormation() throws GameActionException {
        int myCoordinator = GlobalArray.getFormationCoordinator();
        if (myCoordinator == kingId) {
            GlobalArray.clearFormation();
        }
        state = FormationState.IDLE;
        formationTarget = null;
        targetMine = null;
        // indicate("FORMATION: aborted");
    }
}
