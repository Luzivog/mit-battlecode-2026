package opp_obg;


import battlecode.common.*;

public class ThrowManager {

    static final int MY_RAT_KING = 0;
    static final int MY_UNIT = 1;
    static final int NO_COLLISION = 2;
    static final int ENEMY_UNIT = 3;
    static final int IMPASSABLE = 4;
    static final int CAT = 5;
    static final int ENEMY_UNIT_DIST_1 = 6;
    static final int ENEMY_RAT_KING = 7;

    static final int MIN_CARRYING_TURNS = 2;


    static boolean tryThrow(boolean emergency) throws GameActionException {
        if (MyUnit.rc.getCarrying() == null || !MyUnit.rc.isActionReady()) return true;

        ThrowHelper.findBestCollision();

        CollisionData bestCollision = ThrowHelper.bestCollision;

        if (!bestCollision.canShoot()) return false;

        MyUnit.rc.setIndicatorString(bestCollision.initialMoveDir + " " + bestCollision.throwDir + " " + bestCollision.collisionDist + " " + bestCollision.collisionType);

        if (!emergency) {
            int minScore = MyUnit.carryingTurns >= MIN_CARRYING_TURNS ? ENEMY_UNIT : NO_COLLISION;
            //if (MyUnit.carryingTurns >= 7) minScore = IMPASSABLE;
            if (bestCollision.getScore() < minScore) return false;
        }

        if (bestCollision.initialMoveDir != Direction.CENTER) {

            if (!MovementManager.moveWhileLooking(bestCollision.initialMoveDir, bestCollision.throwDir)) return false;

        }

        else {
            if (bestCollision.throwDir != MyUnit.rc.getDirection()) {
                if (!MyUnit.rc.canTurn(bestCollision.throwDir)) return false;
                MyUnit.rc.turn(bestCollision.throwDir);
            }

            if (bestCollision.collisionDist == 1) {
                if (!MovementManager.canMove(bestCollision.throwDir.opposite(), false, false)) return false;
                MovementManager.moveWhileLooking(bestCollision.throwDir.opposite(), MovementManager.myLoc.add(bestCollision.throwDir), false, false);
            } else if (bestCollision.collisionDist == 3) {
                if (!MovementManager.canMove(bestCollision.throwDir, false, false)) return false;
                MovementManager.moveWhileLooking(bestCollision.throwDir, MovementManager.myLoc.add(bestCollision.throwDir).add(bestCollision.throwDir), false, false);
            }
        }

        if (MyUnit.rc.canThrowRat()){
            MyUnit.rc.throwRat();
            return true;
        }
        return false;
    }

}
