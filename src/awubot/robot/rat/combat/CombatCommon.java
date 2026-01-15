package awubot.robot.rat.combat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import static awubot.Game.*;
import static awubot.util.Util.*;

public final class CombatCommon {
    private CombatCommon() {
    }


    static boolean canMoveN;
    static boolean canMoveNE;
    static boolean canMoveE;
    static boolean canMoveSE;
    static boolean canMoveS;
    static boolean canMoveSW;
    static boolean canMoveW;
    static boolean canMoveNW;
    static boolean canMoveC;

    static boolean canPerformN_N;
    static boolean canPerformN_NE;
    static boolean canPerformN_E;
    static boolean canPerformN_SE;
    static boolean canPerformN_S;
    static boolean canPerformN_SW;
    static boolean canPerformN_W;
    static boolean canPerformN_NW;
    static boolean canPerformNE_N;
    static boolean canPerformNE_NE;
    static boolean canPerformNE_E;
    static boolean canPerformNE_SE;
    static boolean canPerformNE_S;
    static boolean canPerformNE_SW;
    static boolean canPerformNE_W;
    static boolean canPerformNE_NW;
    static boolean canPerformE_N;
    static boolean canPerformE_NE;
    static boolean canPerformE_E;
    static boolean canPerformE_SE;
    static boolean canPerformE_S;
    static boolean canPerformE_SW;
    static boolean canPerformE_W;
    static boolean canPerformE_NW;
    static boolean canPerformSE_N;
    static boolean canPerformSE_NE;
    static boolean canPerformSE_E;
    static boolean canPerformSE_SE;
    static boolean canPerformSE_S;
    static boolean canPerformSE_SW;
    static boolean canPerformSE_W;
    static boolean canPerformSE_NW;
    static boolean canPerformS_N;
    static boolean canPerformS_NE;
    static boolean canPerformS_E;
    static boolean canPerformS_SE;
    static boolean canPerformS_S;
    static boolean canPerformS_SW;
    static boolean canPerformS_W;
    static boolean canPerformS_NW;
    static boolean canPerformSW_N;
    static boolean canPerformSW_NE;
    static boolean canPerformSW_E;
    static boolean canPerformSW_SE;
    static boolean canPerformSW_S;
    static boolean canPerformSW_SW;
    static boolean canPerformSW_W;
    static boolean canPerformSW_NW;
    static boolean canPerformW_N;
    static boolean canPerformW_NE;
    static boolean canPerformW_E;
    static boolean canPerformW_SE;
    static boolean canPerformW_S;
    static boolean canPerformW_SW;
    static boolean canPerformW_W;
    static boolean canPerformW_NW;
    static boolean canPerformNW_N;
    static boolean canPerformNW_NE;
    static boolean canPerformNW_E;
    static boolean canPerformNW_SE;
    static boolean canPerformNW_S;
    static boolean canPerformNW_SW;
    static boolean canPerformNW_W;
    static boolean canPerformNW_NW;
    static boolean canPerformC_N;
    static boolean canPerformC_NE;
    static boolean canPerformC_E;
    static boolean canPerformC_SE;
    static boolean canPerformC_S;
    static boolean canPerformC_SW;
    static boolean canPerformC_W;
    static boolean canPerformC_NW;

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
    canPerformN_N = computeCanPerform(Direction.NORTH, Direction.NORTH);
    canPerformN_NE = computeCanPerform(Direction.NORTH, Direction.NORTHEAST);
    canPerformN_E = computeCanPerform(Direction.NORTH, Direction.EAST);
    canPerformN_SE = computeCanPerform(Direction.NORTH, Direction.SOUTHEAST);
    canPerformN_S = computeCanPerform(Direction.NORTH, Direction.SOUTH);
    canPerformN_SW = computeCanPerform(Direction.NORTH, Direction.SOUTHWEST);
    canPerformN_W = computeCanPerform(Direction.NORTH, Direction.WEST);
    canPerformN_NW = computeCanPerform(Direction.NORTH, Direction.NORTHWEST);
    canPerformNE_N = computeCanPerform(Direction.NORTHEAST, Direction.NORTH);
    canPerformNE_NE = computeCanPerform(Direction.NORTHEAST, Direction.NORTHEAST);
    canPerformNE_E = computeCanPerform(Direction.NORTHEAST, Direction.EAST);
    canPerformNE_SE = computeCanPerform(Direction.NORTHEAST, Direction.SOUTHEAST);
    canPerformNE_S = computeCanPerform(Direction.NORTHEAST, Direction.SOUTH);
    canPerformNE_SW = computeCanPerform(Direction.NORTHEAST, Direction.SOUTHWEST);
    canPerformNE_W = computeCanPerform(Direction.NORTHEAST, Direction.WEST);
    canPerformNE_NW = computeCanPerform(Direction.NORTHEAST, Direction.NORTHWEST);
    canPerformE_N = computeCanPerform(Direction.EAST, Direction.NORTH);
    canPerformE_NE = computeCanPerform(Direction.EAST, Direction.NORTHEAST);
    canPerformE_E = computeCanPerform(Direction.EAST, Direction.EAST);
    canPerformE_SE = computeCanPerform(Direction.EAST, Direction.SOUTHEAST);
    canPerformE_S = computeCanPerform(Direction.EAST, Direction.SOUTH);
    canPerformE_SW = computeCanPerform(Direction.EAST, Direction.SOUTHWEST);
    canPerformE_W = computeCanPerform(Direction.EAST, Direction.WEST);
    canPerformE_NW = computeCanPerform(Direction.EAST, Direction.NORTHWEST);
    canPerformSE_N = computeCanPerform(Direction.SOUTHEAST, Direction.NORTH);
    canPerformSE_NE = computeCanPerform(Direction.SOUTHEAST, Direction.NORTHEAST);
    canPerformSE_E = computeCanPerform(Direction.SOUTHEAST, Direction.EAST);
    canPerformSE_SE = computeCanPerform(Direction.SOUTHEAST, Direction.SOUTHEAST);
    canPerformSE_S = computeCanPerform(Direction.SOUTHEAST, Direction.SOUTH);
    canPerformSE_SW = computeCanPerform(Direction.SOUTHEAST, Direction.SOUTHWEST);
    canPerformSE_W = computeCanPerform(Direction.SOUTHEAST, Direction.WEST);
    canPerformSE_NW = computeCanPerform(Direction.SOUTHEAST, Direction.NORTHWEST);
    canPerformS_N = computeCanPerform(Direction.SOUTH, Direction.NORTH);
    canPerformS_NE = computeCanPerform(Direction.SOUTH, Direction.NORTHEAST);
    canPerformS_E = computeCanPerform(Direction.SOUTH, Direction.EAST);
    canPerformS_SE = computeCanPerform(Direction.SOUTH, Direction.SOUTHEAST);
    canPerformS_S = computeCanPerform(Direction.SOUTH, Direction.SOUTH);
    canPerformS_SW = computeCanPerform(Direction.SOUTH, Direction.SOUTHWEST);
    canPerformS_W = computeCanPerform(Direction.SOUTH, Direction.WEST);
    canPerformS_NW = computeCanPerform(Direction.SOUTH, Direction.NORTHWEST);
    canPerformSW_N = computeCanPerform(Direction.SOUTHWEST, Direction.NORTH);
    canPerformSW_NE = computeCanPerform(Direction.SOUTHWEST, Direction.NORTHEAST);
    canPerformSW_E = computeCanPerform(Direction.SOUTHWEST, Direction.EAST);
    canPerformSW_SE = computeCanPerform(Direction.SOUTHWEST, Direction.SOUTHEAST);
    canPerformSW_S = computeCanPerform(Direction.SOUTHWEST, Direction.SOUTH);
    canPerformSW_SW = computeCanPerform(Direction.SOUTHWEST, Direction.SOUTHWEST);
    canPerformSW_W = computeCanPerform(Direction.SOUTHWEST, Direction.WEST);
    canPerformSW_NW = computeCanPerform(Direction.SOUTHWEST, Direction.NORTHWEST);
    canPerformW_N = computeCanPerform(Direction.WEST, Direction.NORTH);
    canPerformW_NE = computeCanPerform(Direction.WEST, Direction.NORTHEAST);
    canPerformW_E = computeCanPerform(Direction.WEST, Direction.EAST);
    canPerformW_SE = computeCanPerform(Direction.WEST, Direction.SOUTHEAST);
    canPerformW_S = computeCanPerform(Direction.WEST, Direction.SOUTH);
    canPerformW_SW = computeCanPerform(Direction.WEST, Direction.SOUTHWEST);
    canPerformW_W = computeCanPerform(Direction.WEST, Direction.WEST);
    canPerformW_NW = computeCanPerform(Direction.WEST, Direction.NORTHWEST);
    canPerformNW_N = computeCanPerform(Direction.NORTHWEST, Direction.NORTH);
    canPerformNW_NE = computeCanPerform(Direction.NORTHWEST, Direction.NORTHEAST);
    canPerformNW_E = computeCanPerform(Direction.NORTHWEST, Direction.EAST);
    canPerformNW_SE = computeCanPerform(Direction.NORTHWEST, Direction.SOUTHEAST);
    canPerformNW_S = computeCanPerform(Direction.NORTHWEST, Direction.SOUTH);
    canPerformNW_SW = computeCanPerform(Direction.NORTHWEST, Direction.SOUTHWEST);
    canPerformNW_W = computeCanPerform(Direction.NORTHWEST, Direction.WEST);
    canPerformNW_NW = computeCanPerform(Direction.NORTHWEST, Direction.NORTHWEST);
    canPerformC_N = computeCanPerform(Direction.CENTER, Direction.NORTH);
    canPerformC_NE = computeCanPerform(Direction.CENTER, Direction.NORTHEAST);
    canPerformC_E = computeCanPerform(Direction.CENTER, Direction.EAST);
    canPerformC_SE = computeCanPerform(Direction.CENTER, Direction.SOUTHEAST);
    canPerformC_S = computeCanPerform(Direction.CENTER, Direction.SOUTH);
    canPerformC_SW = computeCanPerform(Direction.CENTER, Direction.SOUTHWEST);
    canPerformC_W = computeCanPerform(Direction.CENTER, Direction.WEST);
    canPerformC_NW = computeCanPerform(Direction.CENTER, Direction.NORTHWEST);
    }

    public static boolean canMove(Direction dir) {
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
        switch (moveDir.ordinal()) {
            case 0 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformN_N;
                    }
                    case 1 -> {
                        return canPerformN_NE;
                    }
                    case 2 -> {
                        return canPerformN_E;
                    }
                    case 3 -> {
                        return canPerformN_SE;
                    }
                    case 4 -> {
                        return canPerformN_S;
                    }
                    case 5 -> {
                        return canPerformN_SW;
                    }
                    case 6 -> {
                        return canPerformN_W;
                    }
                    case 7 -> {
                        return canPerformN_NW;
                    }
                    default -> { return false; }
                }
            }
            case 1 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformNE_N;
                    }
                    case 1 -> {
                        return canPerformNE_NE;
                    }
                    case 2 -> {
                        return canPerformNE_E;
                    }
                    case 3 -> {
                        return canPerformNE_SE;
                    }
                    case 4 -> {
                        return canPerformNE_S;
                    }
                    case 5 -> {
                        return canPerformNE_SW;
                    }
                    case 6 -> {
                        return canPerformNE_W;
                    }
                    case 7 -> {
                        return canPerformNE_NW;
                    }
                    default -> { return false; }
                }
            }
            case 2 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformE_N;
                    }
                    case 1 -> {
                        return canPerformE_NE;
                    }
                    case 2 -> {
                        return canPerformE_E;
                    }
                    case 3 -> {
                        return canPerformE_SE;
                    }
                    case 4 -> {
                        return canPerformE_S;
                    }
                    case 5 -> {
                        return canPerformE_SW;
                    }
                    case 6 -> {
                        return canPerformE_W;
                    }
                    case 7 -> {
                        return canPerformE_NW;
                    }
                    default -> { return false; }
                }
            }
            case 3 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformSE_N;
                    }
                    case 1 -> {
                        return canPerformSE_NE;
                    }
                    case 2 -> {
                        return canPerformSE_E;
                    }
                    case 3 -> {
                        return canPerformSE_SE;
                    }
                    case 4 -> {
                        return canPerformSE_S;
                    }
                    case 5 -> {
                        return canPerformSE_SW;
                    }
                    case 6 -> {
                        return canPerformSE_W;
                    }
                    case 7 -> {
                        return canPerformSE_NW;
                    }
                    default -> { return false; }
                }
            }
            case 4 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformS_N;
                    }
                    case 1 -> {
                        return canPerformS_NE;
                    }
                    case 2 -> {
                        return canPerformS_E;
                    }
                    case 3 -> {
                        return canPerformS_SE;
                    }
                    case 4 -> {
                        return canPerformS_S;
                    }
                    case 5 -> {
                        return canPerformS_SW;
                    }
                    case 6 -> {
                        return canPerformS_W;
                    }
                    case 7 -> {
                        return canPerformS_NW;
                    }
                    default -> { return false; }
                }
            }
            case 5 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformSW_N;
                    }
                    case 1 -> {
                        return canPerformSW_NE;
                    }
                    case 2 -> {
                        return canPerformSW_E;
                    }
                    case 3 -> {
                        return canPerformSW_SE;
                    }
                    case 4 -> {
                        return canPerformSW_S;
                    }
                    case 5 -> {
                        return canPerformSW_SW;
                    }
                    case 6 -> {
                        return canPerformSW_W;
                    }
                    case 7 -> {
                        return canPerformSW_NW;
                    }
                    default -> { return false; }
                }
            }
            case 6 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformW_N;
                    }
                    case 1 -> {
                        return canPerformW_NE;
                    }
                    case 2 -> {
                        return canPerformW_E;
                    }
                    case 3 -> {
                        return canPerformW_SE;
                    }
                    case 4 -> {
                        return canPerformW_S;
                    }
                    case 5 -> {
                        return canPerformW_SW;
                    }
                    case 6 -> {
                        return canPerformW_W;
                    }
                    case 7 -> {
                        return canPerformW_NW;
                    }
                    default -> { return false; }
                }
            }
            case 7 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformNW_N;
                    }
                    case 1 -> {
                        return canPerformNW_NE;
                    }
                    case 2 -> {
                        return canPerformNW_E;
                    }
                    case 3 -> {
                        return canPerformNW_SE;
                    }
                    case 4 -> {
                        return canPerformNW_S;
                    }
                    case 5 -> {
                        return canPerformNW_SW;
                    }
                    case 6 -> {
                        return canPerformNW_W;
                    }
                    case 7 -> {
                        return canPerformNW_NW;
                    }
                    default -> { return false; }
                }
            }
            case 8 -> {
                switch (facing.ordinal()) {
                    case 0 -> {
                        return canPerformC_N;
                    }
                    case 1 -> {
                        return canPerformC_NE;
                    }
                    case 2 -> {
                        return canPerformC_E;
                    }
                    case 3 -> {
                        return canPerformC_SE;
                    }
                    case 4 -> {
                        return canPerformC_S;
                    }
                    case 5 -> {
                        return canPerformC_SW;
                    }
                    case 6 -> {
                        return canPerformC_W;
                    }
                    case 7 -> {
                        return canPerformC_NW;
                    }
                    default -> { return false; }
                }
            }
            default -> { return false; }
        }
    }

    private static boolean computeCanPerform(Direction moveDir, Direction facing) throws GameActionException {
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
}