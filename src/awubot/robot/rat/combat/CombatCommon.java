package awubot.robot.rat.combat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Profiler;

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
        if (moveDir == Direction.CENTER) {
            if (facing == myDir || rc.canTurn()) return true;
            else return false;
        }
        if (!rc.isMovementReady() || !canMove(moveDir)) return false;
        if (facing != myDir && !rc.canTurn()) return false;
        if (moveDir == myDir && myDir == facing) return true; 
        if ((moveDir == myDir || moveDir == facing)) return true;
        return true;
    }

    // -1 = impossible, 0 = strafe, 1 = no strafe
    public static int movementCost(Direction moveDir, Direction facing) {
        if (moveDir == Direction.CENTER) {
            if (facing == myDir || rc.canTurn()) return 1;
            else return -1;
        }
        if (!rc.isMovementReady() || !canMove(moveDir)) return -1;
        if (facing != myDir && !rc.canTurn()) return -1;
        if (moveDir == myDir && myDir == facing) return 1; 
        if ((moveDir == myDir || moveDir == facing)) return 1;
        return 0;
    }
}