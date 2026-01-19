package awubot_dubious.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot_dubious.robot.rat.combat.CombatCommon;
import awubot_dubious.robot.rat.combat.CombatStrategy;
import static awubot_dubious.Game.*;
import static awubot_dubious.util.Util.*;
import static awubot_dubious.robot.rat.BabyRat.*;
import awubot_dubious.RobotTracker;


public final class TryAttack {
    private TryAttack() {
    }

    static RobotInfo target;
    static Direction bestMove;
    static Direction bestFacing;
    static double bestScore;
    static RobotInfo info;
    static MapLocation t;
    static double weight;
    static Direction moveDir;
    static MapLocation newLoc;
    static double score;

    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        // Profiler.push();
        target = null;
        bestMove = null;
        bestFacing = null;
        bestScore = -1;
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            info = RobotTracker.sortedEnemies[i];
            considerEnemy();
        }
        if (target == null && RobotTracker.numCats > 0) {
            info = RobotTracker.sortedCats[0];
            considerEnemy();
        }
        // Profiler.pop("try attack");
        if (bestScore < 0.5) {
            indicate("didnt find good attack");
            return false;
        }
        assert(CombatStrategy.performMoves(bestMove, bestFacing));
        attack(target);
        println("!!!!!!!!!!!!!!!!!!!!!!! attacked rat");
        indicate("COMBAT STRAT: ATTACK");
        return true;
    }

    private static void considerEnemy() throws GameActionException {
        if (!CombatStrategy.shouldFight(info)) return;
        if (chebyshevDistance(myLoc, info) > 2) return;

        t = info.location;
        weight = 10;
        if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) {
            weight += 30; // prefer one tapping
        }

        selectMoves();
    }

    private static void selectMoves() throws GameActionException {
        switch (myLoc.directionTo(t).ordinal()) {
            case 0 -> {
                moveC();
                moveN();
                moveNE();
                moveNW();
                moveE();
                moveW();
            }
            case 1 -> {
                moveC();
                moveNE();
                moveN();
                moveE();
                moveNW();
                moveSE();
            }
            case 2 -> {
                moveC();
                moveE();
                moveNE();
                moveSE();
                moveN();
                moveS();
            }
            case 3 -> {
                moveC();
                moveSE();
                moveS();
                moveE();
                moveNE();
                moveSW();
            }
            case 4 -> {
                moveC();
                moveS();
                moveSE();
                moveSW();
                moveE();
                moveW();
            }
            case 5 -> {
                moveC();
                moveSW();
                moveS();
                moveW();
                moveNW();
                moveSE();
            }
            case 6 -> {
                moveC();
                moveW();
                moveNW();
                moveSW();
                moveN();
                moveS();
            }
            case 7 -> {
                moveC();
                moveNW();
                moveN();
                moveW();
                moveNE();
                moveSW();
            }
            default -> { }
        }
    }

    private static void moveN() throws GameActionException {
        if (!CombatCommon.canMoveN) return;
        moveDir = Direction.NORTH;
        newLoc = myLoc.add(Direction.NORTH);
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (chebyshevDistance(newLoc, info) > 1) return;
        switch (newLoc.directionTo(t).ordinal()) {
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
        if (!canSense(newLoc, t, Direction.NORTH)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTH)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH, info);
        if (Direction.NORTH != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTH;
        }
    }
    private static void faceNE() throws GameActionException {
        if (!canSense(newLoc, t, Direction.NORTHEAST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHEAST)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST, info);
        if (Direction.NORTHEAST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHEAST;
        }
    }
    private static void faceE() throws GameActionException {
        if (!canSense(newLoc, t, Direction.EAST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.EAST)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.EAST, info);
        if (Direction.EAST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.EAST;
        }
    }
    private static void faceSE() throws GameActionException {
        if (!canSense(newLoc, t, Direction.SOUTHEAST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHEAST)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST, info);
        if (Direction.SOUTHEAST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHEAST;
        }
    }
    private static void faceS() throws GameActionException {
        if (!canSense(newLoc, t, Direction.SOUTH)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTH)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH, info);
        if (Direction.SOUTH != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTH;
        }
    }
    private static void faceSW() throws GameActionException {
        if (!canSense(newLoc, t, Direction.SOUTHWEST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHWEST)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST, info);
        if (Direction.SOUTHWEST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHWEST;
        }
    }
    private static void faceW() throws GameActionException {
        if (!canSense(newLoc, t, Direction.WEST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.WEST)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.WEST, info);
        if (Direction.WEST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.WEST;
        }
    }
    private static void faceNW() throws GameActionException {
        if (!canSense(newLoc, t, Direction.NORTHWEST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHWEST)) return;
        score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST, info);
        if (Direction.NORTHWEST != myDir) {
            score *= 1.1; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHWEST;
        }
    }

    public static boolean miniRun() throws GameActionException {
        if (!rc.isActionReady()) return false;
        for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
            RobotInfo ri = RobotTracker.sortedEnemies[i];
            if (chebyshevDistance(myLoc, ri) > 1) break;
            attack(ri);
            println("!!!!!!!!!!!!!!mini attacked!!");
            indicate("accepted mini attack");
            return true;
        }
        indicate("rejected mini attac");
        return false;
    }
}