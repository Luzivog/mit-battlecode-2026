package awubot_defensivespawning.robot.king;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import static awubot_defensivespawning.Game.*;
import static awubot_defensivespawning.util.Util.*;

public class ThreatContext {
    public enum DefenseState { NORMAL, ALERT, EVADING, CRITICAL }

    public static final int CAT_CRITICAL_DIST_SQ = 16;
    public static final int CAT_DANGER_DIST_SQ = 36;
    public static final int CRITICAL_HP = 150;
    public static final int MAX_TRACKED_CATS = 4;

    public RobotInfo theNearestCat = null;
    public RobotInfo theNearestEnemyRat = null;
    public RobotInfo theNearestEnemyRatKing = null;
    public int theCatDistSq = Integer.MAX_VALUE;
    public int theEnemyRatDistSq = Integer.MAX_VALUE;
    public int theEnemyRatKingDistSq = Integer.MAX_VALUE;

    public RobotInfo[] theTrackedCats = new RobotInfo[MAX_TRACKED_CATS];
    public int theTrackedCatCount = 0;
    public int theThreatVectorX = 0;
    public int theThreatVectorY = 0;

    public DefenseState theDefenseState = DefenseState.NORMAL;

    public void reset() {
        theTrackedCatCount = 0;
        theNearestCat = null;
        theNearestEnemyRat = null;
        theNearestEnemyRatKing = null;
        theCatDistSq = Integer.MAX_VALUE;
        theEnemyRatDistSq = Integer.MAX_VALUE;
        theEnemyRatKingDistSq = Integer.MAX_VALUE;
        theThreatVectorX = 0;
        theThreatVectorY = 0;
    }

    public void scanThreats() throws GameActionException {
        reset();

        RobotInfo[] myRobots = rc.senseNearbyRobots();
        for (RobotInfo myRobot : myRobots) {
            int myDistSq = myLoc.distanceSquaredTo(myRobot.location);

            if (myRobot.getType() == UnitType.CAT) {
                if (theTrackedCatCount < MAX_TRACKED_CATS) {
                    theTrackedCats[theTrackedCatCount] = myRobot;
                    theTrackedCatCount++;
                }
                if (myDistSq < theCatDistSq) {
                    theCatDistSq = myDistSq;
                    theNearestCat = myRobot;
                }
            } else if (myRobot.getTeam() != rc.getTeam()) {
                if (myRobot.getType() == UnitType.RAT_KING) {
                    if (myDistSq < theEnemyRatKingDistSq) {
                        theEnemyRatKingDistSq = myDistSq;
                        theNearestEnemyRatKing = myRobot;
                    }
                } else if (myRobot.getType() == UnitType.BABY_RAT) {
                    if (myDistSq < theEnemyRatDistSq) {
                        theEnemyRatDistSq = myDistSq;
                        theNearestEnemyRat = myRobot;
                    }
                }
            }
        }

        computeThreatVector();
    }

    public void computeThreatVector() {
        if (theTrackedCatCount == 0) return;

        int myWeightedX = 0;
        int myWeightedY = 0;
        int myTotalWeight = 0;

        for (int i = 0; i < theTrackedCatCount; i++) {
            MapLocation myCatLoc = theTrackedCats[i].location;
            int myDistSq = myLoc.distanceSquaredTo(myCatLoc);
            int myWeight = 10000 / (myDistSq + 1);

            myWeightedX += (myCatLoc.x - myLoc.x) * myWeight;
            myWeightedY += (myCatLoc.y - myLoc.y) * myWeight;
            myTotalWeight += myWeight;
        }

        if (myTotalWeight > 0) {
            theThreatVectorX = myWeightedX / myTotalWeight;
            theThreatVectorY = myWeightedY / myTotalWeight;
        }
    }

    public void updateDefenseState() {
        if (theNearestEnemyRatKing != null && theEnemyRatKingDistSq <= 25) {
            theDefenseState = DefenseState.EVADING;
            return;
        }

        int myHp = rc.getHealth();

        if (myHp <= CRITICAL_HP || theCatDistSq <= CAT_CRITICAL_DIST_SQ) {
            theDefenseState = DefenseState.CRITICAL;
        } else if (theCatDistSq <= CAT_DANGER_DIST_SQ ||
                   (theEnemyRatDistSq <= 8 && myHp <= 250)) {
            theDefenseState = DefenseState.EVADING;
        } else if (theNearestCat != null || theNearestEnemyRat != null) {
            theDefenseState = DefenseState.ALERT;
        } else {
            theDefenseState = DefenseState.NORMAL;
        }
    }
}
