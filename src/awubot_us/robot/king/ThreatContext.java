package awubot_us.robot.king;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot_us.RobotTracker;
import static awubot_us.Game.*;
import static awubot_us.util.Util.*;

public class ThreatContext {
    public enum DefenseState { NORMAL, ALERT, EVADING, CRITICAL }

    public static final int CAT_CRITICAL_DIST_SQ = 16;
    public static final int CAT_DANGER_DIST_SQ = 36;
    public static final int CRITICAL_HP = 150;
    public static final int MAX_TRACKED_CATS = 4;

    public RobotInfo nearestCat = null;
    public RobotInfo nearestEnemyRat = null;
    public RobotInfo nearestEnemyRatKing = null;
    public int catDistSq = Integer.MAX_VALUE;
    public int enemyRatDistSq = Integer.MAX_VALUE;
    public int enemyRatKingDistSq = Integer.MAX_VALUE;

    public RobotInfo[] trackedCats = new RobotInfo[MAX_TRACKED_CATS];
    public int trackedCatCount = 0;
    public int threatVectorX = 0;
    public int threatVectorY = 0;

    public DefenseState defenseState = DefenseState.NORMAL;

    public void reset() {
        trackedCatCount = 0;
        nearestCat = null;
        nearestEnemyRat = null;
        nearestEnemyRatKing = null;
        catDistSq = Integer.MAX_VALUE;
        enemyRatDistSq = Integer.MAX_VALUE;
        enemyRatKingDistSq = Integer.MAX_VALUE;
        threatVectorX = 0;
        threatVectorY = 0;
    }

    public void scanThreats() throws GameActionException {
        reset();

        for (int i = 0; i < RobotTracker.numCats && i < RobotTracker.sortedCats.length; i++) {
            RobotInfo myCat = RobotTracker.sortedCats[i];
            if (myCat == null) continue;

            int myDistSq = myLoc.distanceSquaredTo(myCat.location);
            if (trackedCatCount < MAX_TRACKED_CATS) {
                trackedCats[trackedCatCount] = myCat;
                trackedCatCount++;
            }
            if (myDistSq < catDistSq) {
                catDistSq = myDistSq;
                nearestCat = myCat;
            }
        }

        for (int i = 0; i < RobotTracker.numEnemies && i < RobotTracker.sortedEnemies.length; i++) {
            RobotInfo myEnemy = RobotTracker.sortedEnemies[i];
            if (myEnemy == null) continue;

            int myDistSq = myLoc.distanceSquaredTo(myEnemy.location);
            if (myEnemy.getType() == UnitType.RAT_KING) {
                if (myDistSq < enemyRatKingDistSq) {
                    enemyRatKingDistSq = myDistSq;
                    nearestEnemyRatKing = myEnemy;
                }
            } else if (myEnemy.getType() == UnitType.BABY_RAT) {
                if (myDistSq < enemyRatDistSq) {
                    enemyRatDistSq = myDistSq;
                    nearestEnemyRat = myEnemy;
                }
            }
        }

        computeThreatVector();
    }

    public void computeThreatVector() {
        if (trackedCatCount == 0) return;

        int myWeightedX = 0;
        int myWeightedY = 0;
        int myTotalWeight = 0;

        for (int i = 0; i < trackedCatCount; i++) {
            MapLocation myCatLoc = trackedCats[i].location;
            int myDistSq = myLoc.distanceSquaredTo(myCatLoc);
            int myWeight = 10000 / (myDistSq + 1);

            myWeightedX += (myCatLoc.x - myLoc.x) * myWeight;
            myWeightedY += (myCatLoc.y - myLoc.y) * myWeight;
            myTotalWeight += myWeight;
        }

        if (myTotalWeight > 0) {
            threatVectorX = myWeightedX / myTotalWeight;
            threatVectorY = myWeightedY / myTotalWeight;
        }
    }

    public void updateDefenseState() {
        if (nearestEnemyRatKing != null && enemyRatKingDistSq <= 25) {
            defenseState = DefenseState.EVADING;
            return;
        }

        int myHp = rc.getHealth();

        if (myHp <= CRITICAL_HP || catDistSq <= CAT_CRITICAL_DIST_SQ) {
            defenseState = DefenseState.CRITICAL;
        } else if (catDistSq <= CAT_DANGER_DIST_SQ ||
                   (enemyRatDistSq <= 8 && myHp <= 250)) {
            defenseState = DefenseState.EVADING;
        } else if (nearestCat != null || nearestEnemyRat != null) {
            defenseState = DefenseState.ALERT;
        } else {
            defenseState = DefenseState.NORMAL;
        }
    }
}
