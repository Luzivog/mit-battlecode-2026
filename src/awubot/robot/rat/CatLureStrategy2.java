package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class CatLureStrategy2 extends Strategy {
    private static final int LURE_DURATION = 30;
    private static final int MISSION_COMPLETE_DIST_SQ = 36;
    private static final int CAT_LOST_THRESHOLD = 20;

    private MapLocation theEnemyKingLoc = null;
    private int theLureTTL = 0;
    private boolean theIsActive = false;

    public void setLureTarget(MapLocation aCatLoc, MapLocation aEnemyKingLoc) {
        theEnemyKingLoc = aEnemyKingLoc;
        theLureTTL = LURE_DURATION;
        theIsActive = true;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return !theIsActive;
    }

    @Override
    public void runTick() throws GameActionException {
        if (!theIsActive) return;

        theLureTTL--;
        indicate("LURING CAT TO ENEMY!");

        moveTowardEnemy();

        if (shouldTerminate()) {
            reset();
        }
    }

    private void moveTowardEnemy() throws GameActionException {
        if (!rc.isMovementReady() || theEnemyKingLoc == null) return;

        Direction myToEnemy = myLoc.directionTo(theEnemyKingLoc);
        Direction[] myTryDirs = {
            myToEnemy,
            myToEnemy.rotateLeft(),
            myToEnemy.rotateRight(),
            myToEnemy.rotateLeft().rotateLeft(),
            myToEnemy.rotateRight().rotateRight()
        };

        for (Direction myDir : myTryDirs) {
            if (rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
                return;
            }
        }
    }

    private boolean shouldTerminate() {
        if (theEnemyKingLoc != null && myLoc.distanceSquaredTo(theEnemyKingLoc) <= MISSION_COMPLETE_DIST_SQ) {
            indicate("REACHED ENEMY TERRITORY - mission complete!");
            return true;
        }

        if (theLureTTL <= 0) return true;

        if (!isCatNearby() && theLureTTL < CAT_LOST_THRESHOLD) return true;

        return false;
    }

    private boolean isCatNearby() {
        for (RobotInfo myRobot : rc.senseNearbyRobots()) {
            if (myRobot.getType() == UnitType.CAT) {
                return true;
            }
        }
        return false;
    }

    private void reset() {
        theIsActive = false;
        theEnemyKingLoc = null;
    }
}
