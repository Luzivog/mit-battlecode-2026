package awubot_quals.robot.rat;

import battlecode.common.*;
import awubot_quals.Pathfinder;
import awubot_quals.robot.Strategy;
import static awubot_quals.Game.*;
import static awubot_quals.util.Util.*;

public class CatLureStrategy2 extends Strategy {
    private static final int LURE_DURATION = 30;
    private static final int MISSION_COMPLETE_DIST_SQ = 36;
    private static final int CAT_LOST_THRESHOLD = 20;

    private MapLocation enemyKingLoc = null;
    private int lureTTL = 0;
    private boolean isActive = false;

    public void setLureTarget(MapLocation aCatLoc, MapLocation aEnemyKingLoc) {
        enemyKingLoc = aEnemyKingLoc;
        lureTTL = LURE_DURATION;
        isActive = true;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if (!isActive) return false;

        lureTTL--;
        indicate("LURING CAT TO ENEMY!");

        moveTowardEnemy();

        if (shouldTerminate()) {
            reset();
        }
        return true;
    }

    private void moveTowardEnemy() throws GameActionException {
        if (!rc.isMovementReady() || enemyKingLoc == null) return;

        Direction myToEnemy = myLoc.directionTo(enemyKingLoc);
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
        if (enemyKingLoc != null && myLoc.distanceSquaredTo(enemyKingLoc) <= MISSION_COMPLETE_DIST_SQ) {
            indicate("REACHED ENEMY TERRITORY - mission complete!");
            return true;
        }

        if (lureTTL <= 0) return true;

        if (!isCatNearby() && lureTTL < CAT_LOST_THRESHOLD) return true;

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
        isActive = false;
        enemyKingLoc = null;
    }
}
