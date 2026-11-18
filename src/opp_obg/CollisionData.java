package opp_obg;

import battlecode.common.Direction;

public class CollisionData{
    int collisionType;
    int collisionDist;
    Direction throwDir;
    boolean canGoBack;
    boolean canGoFront;
    Direction initialMoveDir;
    boolean canPerform;

    CollisionData(Direction moveDir, Direction dir){
        this.throwDir = dir;
        this.initialMoveDir = moveDir;
    }

    void reset(){
        collisionType = ThrowManager.NO_COLLISION;
        collisionDist = Constants.INF;
        canPerform = throwDir == MyUnit.rc.getDirection() || MyUnit.rc.canTurn();

        if (initialMoveDir == Direction.CENTER) {
            canGoBack = MovementManager.canMove(throwDir.opposite(), false, false);
            canGoFront = MovementManager.canMove(throwDir, false, false);
        }
        else {
            canGoBack = false;
            canGoFront = false;
            if (!MovementManager.canMove(initialMoveDir, false, false)) canPerform = false;
        }
    }

    void add(int dist){
        if (dist >= collisionDist) return;
        collisionDist = dist;
        collisionType = VisionManager.collisionType;
    }

    boolean canShoot(){
        return canPerform && (collisionDist > 1 || (collisionDist == 1 && canGoBack));
    }

    int getScore(){
        switch(collisionType){
            case ThrowManager.ENEMY_UNIT:
                return switch(collisionDist){
                    case 2 -> ThrowManager.ENEMY_UNIT_DIST_1;
                    case 1 -> canGoBack ? ThrowManager.ENEMY_UNIT_DIST_1 : ThrowManager.ENEMY_UNIT;
                    case 3 -> canGoFront ? ThrowManager.ENEMY_UNIT_DIST_1 : ThrowManager.ENEMY_UNIT;
                    default -> ThrowManager.ENEMY_UNIT;
                };
            default:
                return collisionType;
        }
    }

    boolean isBetterThan(CollisionData B){
        if (B == null) return true;

        if (!canShoot()) return false;
        if (!B.canShoot()) return true;

        int myScore = getScore(), theirScore = B.getScore();

        if (myScore > theirScore) return true;
        if (myScore < theirScore) return false;

        if (B.initialMoveDir == Direction.CENTER && initialMoveDir != Direction.CENTER) return false;
        if (initialMoveDir == Direction.CENTER && B.initialMoveDir != Direction.CENTER) return true;

        return collisionDist < B.collisionDist;
    }
}
