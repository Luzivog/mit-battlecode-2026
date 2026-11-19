package challenger.robot.king;

import battlecode.common.*;
import challenger.comms.GlobalArray;
import challenger.robot.Strategy;
import challenger.CellTracker;
import challenger.RobotTracker;
import challenger.Tune;
import static challenger.Game.*;
import static challenger.util.Util.*;

import challenger.Game;

public class KingFormationCoordinatorStrategy extends Strategy {
    private static final int[] MINE_THRESHOLDS = {3, Tune.MINES_KING2, 9, 15, 10000000};
    private static final int UPGRADE_COST = 50;
    private static final int MAX_FORMATION_DIST_TO_MINE_SQ = 25;
    private static final int COORDINATION_TIMEOUT = 120;

    // King lock-in: kings formed by RAT_KING_CUTOFF_ROUND survive to the end, after it a team with 1 king can form only
    // a second (rules.md §6). From LOCK_IN_ROUND, below KING_TARGET kings, form without the 20-per-king rat gate and
    // the far-mine floor.
    private static final int LOCK_IN_ROUND = Tune.LOCK_IN_ROUND;
    private static final int KING_TARGET = 3;
    private static final int LOCK_IN_MIN_RATS = 12;
    // A lock-in king pays upkeep to the end and its formation consumes ~8 rats to rebuild: before the cutoff, form only
    // with a 120-round upkeep reserve for 3 kings (720) + the formation (50) + the rebuild (~280). Measured: lock-in
    // formations started below 1000 global starved 22 of 38 games and won 5 fewer than main, above it 12 more.
    private static final int LOCK_IN_MIN_CHEESE = Tune.LOCK_IN_MIN_CHEESE;
    // Second king in cooperation (KB): a game that ends in cooperation scores kings at 30 (rules.md §1), so 2 kings to 1
    // is +10 points, and those endings are decided by a few points. A game still in cooperation at COOP_ROUND is
    // likely to end in it (the first backstab comes by r40 in 34 of 41 mirror games, T3 §3.1), and its bank is high.
    // So from COOP_ROUND, with one king, form a second without the 20-per-king rat gate and without the known-mine
    // threshold: kings learn mines late (TheHeist: none known by r200). The site is a mine of its own on our side (under
    // every symmetry still possible), far from our king when one is: the far-mine floor alone picked enemy-side mines
    // behind the walls of these maps, and the formations timed out. A backstab ends the reason, so it aborts such a
    // formation: earlier kings in contested games starve. From the lock-in round the lock-in's rules apply instead: on rose
    // (cooperation to r2000) no known mine was on our side under every symmetry still possible, the search found no
    // site all game, and main lost 5 of 6 games at 1 king to 2 (KF).
    private static final int COOP_ROUND = Tune.COOP_ROUND;
    // Danger (KD): a lone king that dies loses the game (rules.md §1), a second king formed before it dies plays on.
    // Our babies are scattered when the king dies: the 7th nearest reaches a free 3x3 beside the king about as soon as
    // the best one anywhere, and a far mine only after the king is dead (docs/theory/kd-danger.md). So the danger site is
    // a free 3x3 at Chebyshev distance 3 from the king (its 3x3 clear of the king's), toward our rats and away from
    // enemies, and the rats put it before combat (FormationStrategy). A formation needs 7 babies: numRats counts them in
    // fours, so 4 means 4 to 7, and the king's spawns toward the site fill the rest.
    private static final int DANGER_MIN_RATS = 4;
    private static final int DANGER_TIMEOUT = 30;
    public static MapLocation dangerSite = null;

    private static int MIN_DIST_FROM_KINGS_SQ;

    private final int tabooMinX;
    private final int tabooMaxX;
    private final int tabooMinY;
    private final int tabooMaxY;

    private FormationState state = FormationState.IDLE;
    private MapLocation formationTarget = null;
    private MapLocation targetMine = null;
    private int coordinationStartTurn = 0;
    private int kingCount = 1;
    private int kings = 1;
    private boolean lockIn = false;
    private boolean coop = false;
    private boolean coopFormation = false;
    private MapLocation home = null;
    private int coopSearchKey = -1;
    private RobotInfo nearestCat = null;

    private enum FormationState {
        IDLE,
        COORDINATING,
        MONITORING
    }

    public KingFormationCoordinatorStrategy() {
        MIN_DIST_FROM_KINGS_SQ = Math.max(400, mapWidth * mapHeight / 5);

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
        if (hpHistory[0] == 0) java.util.Arrays.fill(hpHistory, rc.getHealth());
        hpHistory[time & 15] = rc.getHealth();
        int myKingCount = GlobalArray.countRatKings();
        kings = myKingCount;
        int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;

        var req = 12;
        if(mapWidth * mapHeight <= 1200) {
            req = 8;
        }
        if (home == null) home = myLoc;
        lockIn = time >= LOCK_IN_ROUND && myKingCount < KING_TARGET;
        coop = myKingCount == 1 && time >= COOP_ROUND && time < LOCK_IN_ROUND && rc.isCooperation();
        danger = myKingCount == 1 && isDanger();
        int minRats = danger ? DANGER_MIN_RATS : lockIn ? LOCK_IN_MIN_RATS : req * myKingCount;
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

        // A danger formation whose coordinator died (the king it was to replace) stays in the array: rats would keep
        // leaving combat for it.
        if (GlobalArray.getFormationTarget() != null && GlobalArray.isDangerFormation()
            && GlobalArray.getRatKing(GlobalArray.getFormationCoordinator()) == null) {
            GlobalArray.clearFormation();
            state = FormationState.IDLE;
        }
        if (danger && state != FormationState.IDLE && dangerSite == null) {
            abortFormation();
        }

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

    // Our only king is about to die: below 200 HP, or losing HP at a rate that kills it within 20 rounds. In main's
    // king-kill losses this fires at least 10 rounds before the death with the bank >= 50 in 41 of 62 COMBAT and 22 of 25
    // CAT losses (below 100 HP alone: 17 and 18), and in 55 of 921 won games (KD step 0).
    private static final int DANGER_HP = 200;
    private static final int DANGER_ROUNDS = 20;
    private final int[] hpHistory = new int[16];

    private boolean isDanger() {
        int myHp = rc.getHealth();
        int myLost = hpHistory[(time - 10) & 15] - myHp;
        return myHp < DANGER_HP || myLost * DANGER_ROUNDS > myHp * 10;
    }

    private void tryStartFormation() throws GameActionException {
        if (danger) {
            if (rc.getGlobalCheese() < UPGRADE_COST) return;
            MapLocation mySite = selectDangerSite();
            if (mySite == null) return;
            if (GlobalArray.getFormationTarget() != null) GlobalArray.clearFormation();
            dangerSite = mySite;
            targetMine = mySite;
            coordinationStartTurn = time;
            coopFormation = false;
            state = FormationState.COORDINATING;
            GlobalArray.writeFormationTarget(mySite, mySite, kingId | GlobalArray.DANGER_FORMATION);
            return;
        } else {
            // indicate("A");
            boolean myPreCutoff = time <= GameConstants.RAT_KING_CUTOFF_ROUND;
            int myGlobalCheese = rc.getGlobalCheese();
            if(myGlobalCheese < (lockIn && myPreCutoff ? LOCK_IN_MIN_CHEESE : UPGRADE_COST + 100)) return;
            // indicate("B");
            int myKingCount = kings;
            if (myKingCount == 5) return;
            // indicate("C");
            int numRats = (rc.getCurrentRatCost() - 10) * 2 / 5;
            // A king formed before the cutoff pays upkeep to the end, so it needs the mines to carry it: a side earns
            // ~1.13 per mine per round against 2 per king (T1 §2.7). Measured without this gate, paired against main:
            // lock-in won 15 fewer of 62 games on maps with <= 4 mines, and 15 more of 246 on maps with >= 16.
            if (myPreCutoff && !coop && Game.knownCheeseMines.size() < MINE_THRESHOLDS[myKingCount]) return;
            if (coop) {
                if (numRats < LOCK_IN_MIN_RATS) return;
            } else if(!lockIn) {
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

        // A cooperation search that found no site is repeated only when its inputs grow: more known mines, fewer
        // symmetries. Repeated every turn, it cost the king 1.6k bytecode a turn on rose (in cooperation to r2000).
        int mySearchKey = coop ? Game.knownCheeseMines.size() | (CellTracker.rsym | CellTracker.hsym << 1 | CellTracker.vsym << 2) << 8 : -1;
        if (mySearchKey >= 0 && mySearchKey == coopSearchKey) return;
        MapLocation formationTarget = selectBestMineForFormation();
        if (formationTarget == null) {
            coopSearchKey = mySearchKey;
            return;
        }

        targetMine = formationTarget;
        coordinationStartTurn = time;
        coopFormation = coop;
        state = FormationState.COORDINATING;

        GlobalArray.writeFormationTarget(formationTarget, targetMine, kingId);

        // indicate("FORMATION: coordinating at " + formationTarget);
    }

    // A free 3x3 beside the king, clear of its own 3x3: the nearest ring (Chebyshev 3, then 4, then 5) holding one, and
    // in it the best by our babies within d2 8 of it, enemy babies within d2 13, cats within d2 25, and the distance to
    // the nearest enemy or cat.
    private MapLocation selectDangerSite() throws GameActionException {
        for (int myRing = 3; myRing <= 5; myRing++) {
            MapLocation myBest = null;
            int myBestScore = Integer.MIN_VALUE;
            for (int myDx = -myRing; myDx <= myRing; myDx++) {
                for (int myDy = -myRing; myDy <= myRing; myDy += (myDx == -myRing || myDx == myRing) ? 1 : 2 * myRing) {
                    MapLocation myCentre = myLoc.translate(myDx, myDy);
                    if (!isFreeDangerSite(myCentre)) continue;
                    int myScore = 0;
                    int myNearest = 64;
                    for (int i = RobotTracker.numAllies; --i >= 0; ) {
                        RobotInfo myAlly = RobotTracker.sortedAllies[i];
                        if (myAlly.type == UnitType.BABY_RAT && myAlly.location.distanceSquaredTo(myCentre) <= 8) myScore += 16;
                    }
                    for (int i = RobotTracker.numEnemies; --i >= 0; ) {
                        RobotInfo myEnemy = RobotTracker.sortedEnemies[i];
                        int myDist = myEnemy.location.distanceSquaredTo(myCentre);
                        if (myEnemy.type == UnitType.RAT_KING) myDist -= 8;
                        if (myDist <= 13) myScore -= 32;
                        if (myDist < myNearest) myNearest = myDist;
                    }
                    for (int i = RobotTracker.numCats; --i >= 0; ) {
                        int myDist = RobotTracker.sortedCats[i].location.distanceSquaredTo(myCentre);
                        if (myDist <= 25) myScore -= 64;
                        if (myDist < myNearest) myNearest = myDist;
                    }
                    myScore += myNearest;
                    if (myScore > myBestScore) {
                        myBestScore = myScore;
                        myBest = myCentre;
                    }
                }
            }
            if (myBest != null) return myBest;
        }
        return null;
    }

    // The 3x3 was seen free of walls and dirt, it is clear of this king's 3x3, and no enemy king or cat in sight is on it.
    private boolean isFreeDangerSite(MapLocation aCentre) throws GameActionException {
        if (Math.abs(aCentre.x - myLocX) < 3 && Math.abs(aCentre.y - myLocY) < 3) return false;
        if (!CellTracker.isFree3x3(aCentre)) return false;
        for (int i = RobotTracker.numEnemies; --i >= 0; ) {
            RobotInfo myEnemy = RobotTracker.sortedEnemies[i];
            if (myEnemy.type == UnitType.RAT_KING && Math.abs(myEnemy.location.x - aCentre.x) <= 2
                && Math.abs(myEnemy.location.y - aCentre.y) <= 2) return false;
        }
        for (int i = RobotTracker.numCats; --i >= 0; ) {
            MapLocation myCat = RobotTracker.sortedCats[i].location;
            if (myCat.x >= aCentre.x - 2 && myCat.x <= aCentre.x + 1 && myCat.y >= aCentre.y - 2 && myCat.y <= aCentre.y + 1) return false;
        }
        return true;
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
            if (coop && !isOnOurSide(myMine)) continue;

            int myScore = scoreMineForFormation(myMine);
            if (myScore > myBestScore) {
                myBestScore = myScore;
                myBestMine = myMine;
            }
        }
        if(!lockIn && !coop && myBestScore < -40000) {
            return null;
        }
        return myBestMine;
    }

    // Nearer our first king's start than its mirror, under every symmetry not yet ruled out.
    private boolean isOnOurSide(MapLocation aMine) {
        int myDist = aMine.distanceSquaredTo(home);
        return (CellTracker.rsym == 0 || myDist < aMine.distanceSquaredTo(rot180(home)))
            && (CellTracker.hsym == 0 || myDist < aMine.distanceSquaredTo(flipHor(home)))
            && (CellTracker.vsym == 0 || myDist < aMine.distanceSquaredTo(flipVer(home)));
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
        if (dangerSite != null && (time - coordinationStartTurn > DANGER_TIMEOUT || !isFreeDangerSite(dangerSite))) {
            abortFormation();
            return;
        }
        if (time - coordinationStartTurn > COORDINATION_TIMEOUT) {
            // indicate("FORMATION: coordination timed out");
            abortFormation();
            return;
        }

        if (rc.getGlobalCheese() < UPGRADE_COST || (coopFormation && !rc.isCooperation())) {
            // indicate("FORMATION: insufficient cheese");
            abortFormation();
            return;
        }

        MapLocation myCurrentTarget = GlobalArray.getFormationTarget();
        if (myCurrentTarget == null) {
            state = FormationState.IDLE;
            dangerSite = null;
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
        dangerSite = null;
        // indicate("FORMATION: aborted");
    }
}
