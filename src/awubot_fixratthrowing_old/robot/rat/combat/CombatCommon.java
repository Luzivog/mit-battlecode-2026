package awubot_fixratthrowing_old.robot.rat.combat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import static awubot_fixratthrowing_old.Game.*;
import static awubot_fixratthrowing_old.util.Util.*;
import awubot_fixratthrowing_old.util.Profiler;

public final class CombatCommon {
    private CombatCommon() {
    }


    public static boolean canMoveN;
    public static boolean canMoveNE;
    public static boolean canMoveE;
    public static boolean canMoveSE;
    public static boolean canMoveS;
    public static boolean canMoveSW;
    public static boolean canMoveW;
    public static boolean canMoveNW;
    public static boolean canMoveC;

    public static void precomp() throws GameActionException {
        canMoveN = canTraverse(Direction.NORTH);
        canMoveNE = canTraverse(Direction.NORTHEAST);
        canMoveE = canTraverse(Direction.EAST);
        canMoveSE = canTraverse(Direction.SOUTHEAST);
        canMoveS = canTraverse(Direction.SOUTH);
        canMoveSW = canTraverse(Direction.SOUTHWEST);
        canMoveW = canTraverse(Direction.WEST);
        canMoveNW = canTraverse(Direction.NORTHWEST);
        canMoveC = canTraverse(Direction.CENTER);
    }

    public static boolean canMove(Direction dir) {
        // if (dir == null) return false;
        return switch (dir.ordinal()) {
            case 0 -> canMoveN;
            case 1 -> canMoveNE;
            case 2 -> canMoveE;
            case 3 -> canMoveSE;
            case 4 -> canMoveS;
            case 5 -> canMoveSW;
            case 6 -> canMoveW;
            case 7 -> canMoveNW;
            case 8 -> canMoveC;
            default -> false;
        };
    }

    public static boolean canPerformMoves(Direction moveDir, Direction facing) {
        return computeCanPerform(moveDir, facing);
    }

    private static boolean computeCanPerform(Direction moveDir, Direction facing) {
        if (moveDir != Direction.CENTER && !canMove(moveDir)) return false;
        if (moveDir == Direction.CENTER) {
            return facing == myDir || rc.canTurn();
        }
        if (!rc.isMovementReady() || !canMove(moveDir)) return false;
        if (moveDir == myDir && facing == myDir) return true;
        if (moveDir == myDir) {
            return myDir == facing || rc.canTurn();
        }
        if (moveDir == facing && rc.canTurn()) return true;
        if (canStrafe()) {
            if (facing == myDir) return true;
            return rc.canTurn();
        }
        return false;
    }

    
    // -1 = impossible, 0 = strafe, 1 = no strafe
    public static int movementCost(Direction moveDir, Direction facing) {
        if (moveDir != Direction.CENTER && !canMove(moveDir)) return -1;
        if (moveDir == Direction.CENTER) {
            if (facing == myDir || rc.canTurn()) {
                return 1;
            } else {
                return -1;
            }
        }
        if (!rc.isMovementReady() || !canMove(moveDir)) {
            return -1;
        }
        if (moveDir == myDir && facing == myDir) return 1;
        if (moveDir == myDir) {
            if (myDir == facing || rc.canTurn()) {
                return 1;
            } else {
                return -1;
            }
        }
        if (moveDir == facing && rc.canTurn()) return 1;
        if (canStrafe()) {
            if (facing == myDir || rc.canTurn()) return 0;
        }
        return -1;
    }
}