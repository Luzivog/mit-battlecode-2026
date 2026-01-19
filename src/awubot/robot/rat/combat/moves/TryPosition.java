package awubot.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;
import awubot.RobotTracker;


public final class TryPosition {
    private TryPosition() {
    }

    static Direction bestMove;
    static Direction bestFacing;
    static double bestScore;
    static Direction moveDir;
    static MapLocation newLoc;
    static double score;

    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        // Profiler.push();
        bestMove = null;
        bestFacing = null;
        bestScore = -1;
        selectMoves();
        assert(CombatStrategy.performMoves(bestMove, bestFacing));
        indicate("COMBAT STRAT: POSITION move " + bestMove + " face " + bestFacing + " score " + bestScore);
        return true;
    }

    private static void selectMoves() throws GameActionException {
        moveN();
        moveNE();
        moveE();
        moveSE();
        moveS();
        moveSW();
        moveW();
        moveNW();
        moveC();
    }

    private static void moveN() throws GameActionException {
        if (!CombatCommon.canMoveN) return;
        moveDir = Direction.NORTH;
        newLoc = myLoc.add(Direction.NORTH);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveNE() throws GameActionException {
        if (!CombatCommon.canMoveNE) return;
        moveDir = Direction.NORTHEAST;
        newLoc = myLoc.add(Direction.NORTHEAST);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveE() throws GameActionException {
        if (!CombatCommon.canMoveE) return;
        moveDir = Direction.EAST;
        newLoc = myLoc.add(Direction.EAST);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveSE() throws GameActionException {
        if (!CombatCommon.canMoveSE) return;
        moveDir = Direction.SOUTHEAST;
        newLoc = myLoc.add(Direction.SOUTHEAST);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveS() throws GameActionException {
        if (!CombatCommon.canMoveS) return;
        moveDir = Direction.SOUTH;
        newLoc = myLoc.add(Direction.SOUTH);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveSW() throws GameActionException {
        if (!CombatCommon.canMoveSW) return;
        moveDir = Direction.SOUTHWEST;
        newLoc = myLoc.add(Direction.SOUTHWEST);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveW() throws GameActionException {
        if (!CombatCommon.canMoveW) return;
        moveDir = Direction.WEST;
        newLoc = myLoc.add(Direction.WEST);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveNW() throws GameActionException {
        if (!CombatCommon.canMoveNW) return;
        moveDir = Direction.NORTHWEST;
        newLoc = myLoc.add(Direction.NORTHWEST);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }
    private static void moveC() throws GameActionException {
        if (!CombatCommon.canMoveC) return;
        moveDir = Direction.CENTER;
        newLoc = myLoc.add(Direction.CENTER);
        switch (myDir.ordinal()) {
            case 0 -> {
                faceN();
                faceNE();
                faceNW();
            }
            case 1 -> {
                faceNE();
                faceN();
                faceE();
            }
            case 2 -> {
                faceE();
                faceNE();
                faceSE();
            }
            case 3 -> {
                faceSE();
                faceS();
                faceE();
            }
            case 4 -> {
                faceS();
                faceSE();
                faceSW();
            }
            case 5 -> {
                faceSW();
                faceS();
                faceW();
            }
            case 6 -> {
                faceW();
                faceNW();
                faceSW();
            }
            case 7 -> {
                faceNW();
                faceN();
                faceW();
            }
            default -> { }
        }
    }

    private static void faceN() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTH)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH);
        if (Direction.NORTH != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.NORTH;
        }
    }
    private static void faceNE() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHEAST)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST);
        if (Direction.NORTHEAST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.NORTHEAST;
        }
    }
    private static void faceE() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.EAST)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.EAST);
        if (Direction.EAST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.EAST;
        }
    }
    private static void faceSE() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHEAST)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST);
        if (Direction.SOUTHEAST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHEAST;
        }
    }
    private static void faceS() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTH)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH);
        if (Direction.SOUTH != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.SOUTH;
        }
    }
    private static void faceSW() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHWEST)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST);
        if (Direction.SOUTHWEST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHWEST;
        }
    }
    private static void faceW() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.WEST)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.WEST);
        if (Direction.WEST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.WEST;
        }
    }
    private static void faceNW() throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHWEST)) return;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST);
        if (Direction.NORTHWEST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            bestMove = moveDir;
            bestFacing = Direction.NORTHWEST;
        }
    }
}