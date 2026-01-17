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
        if (info.getType() == UnitType.RAT_KING) {
            indicate("saw rat king");
        }
        if (chebyshevDistance(myLoc, info) > 2) return;

        t = info.location;
        weight = 10;
        if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) {
            weight += 30; // prefer one tapping
        }

        if (selectMoves()) return;
    }

    private static boolean selectMoves() throws GameActionException {
        switch (myLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (moveC()) return true;
                if (moveN()) return true;
                if (moveNE()) return true;
                if (moveNW()) return true;
                if (moveE()) return true;
                if (moveW()) return true;
            }
            case 1 -> {
                if (moveC()) return true;
                if (moveNE()) return true;
                if (moveN()) return true;
                if (moveE()) return true;
                if (moveNW()) return true;
                if (moveSE()) return true;
            }
            case 2 -> {
                if (moveC()) return true;
                if (moveE()) return true;
                if (moveNE()) return true;
                if (moveSE()) return true;
                if (moveN()) return true;
                if (moveS()) return true;
            }
            case 3 -> {
                if (moveC()) return true;
                if (moveSE()) return true;
                if (moveS()) return true;
                if (moveE()) return true;
                if (moveNE()) return true;
                if (moveSW()) return true;
            }
            case 4 -> {
                if (moveC()) return true;
                if (moveS()) return true;
                if (moveSE()) return true;
                if (moveSW()) return true;
                if (moveE()) return true;
                if (moveW()) return true;
            }
            case 5 -> {
                if (moveC()) return true;
                if (moveSW()) return true;
                if (moveS()) return true;
                if (moveW()) return true;
                if (moveNW()) return true;
                if (moveSE()) return true;
            }
            case 6 -> {
                if (moveC()) return true;
                if (moveW()) return true;
                if (moveNW()) return true;
                if (moveSW()) return true;
                if (moveN()) return true;
                if (moveS()) return true;
            }
            case 7 -> {
                if (moveC()) return true;
                if (moveNW()) return true;
                if (moveN()) return true;
                if (moveW()) return true;
                if (moveNE()) return true;
                if (moveSW()) return true;
            }
            default -> { }
        }
        return false;
    }

    private static boolean moveN() throws GameActionException {
        moveDir = Direction.NORTH;
        if (!CombatCommon.canMoveN) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveNE() throws GameActionException {
        moveDir = Direction.NORTHEAST;
        if (!CombatCommon.canMoveNE) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveE() throws GameActionException {
        moveDir = Direction.EAST;
        if (!CombatCommon.canMoveE) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveSE() throws GameActionException {
        moveDir = Direction.SOUTHEAST;
        if (!CombatCommon.canMoveSE) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveS() throws GameActionException {
        moveDir = Direction.SOUTH;
        if (!CombatCommon.canMoveS) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveSW() throws GameActionException {
        moveDir = Direction.SOUTHWEST;
        if (!CombatCommon.canMoveSW) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveW() throws GameActionException {
        moveDir = Direction.WEST;
        if (!CombatCommon.canMoveW) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveNW() throws GameActionException {
        moveDir = Direction.NORTHWEST;
        if (!CombatCommon.canMoveNW) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }
    private static boolean moveC() throws GameActionException {
        moveDir = Direction.CENTER;
        if (!CombatCommon.canMoveC) return true;
        newLoc = myLoc.add(moveDir);
        if (chebyshevDistance(newLoc, info) > 1) return true;
        switch (newLoc.directionTo(t).ordinal()) {
            case 0 -> {
                if (faceN()) return true;
                if (faceNE()) return true;
                if (faceNW()) return true;
            }
            case 1 -> {
                if (faceNE()) return true;
                if (faceN()) return true;
                if (faceE()) return true;
            }
            case 2 -> {
                if (faceE()) return true;
                if (faceNE()) return true;
                if (faceSE()) return true;
            }
            case 3 -> {
                if (faceSE()) return true;
                if (faceS()) return true;
                if (faceE()) return true;
            }
            case 4 -> {
                if (faceS()) return true;
                if (faceSE()) return true;
                if (faceSW()) return true;
            }
            case 5 -> {
                if (faceSW()) return true;
                if (faceS()) return true;
                if (faceW()) return true;
            }
            case 6 -> {
                if (faceW()) return true;
                if (faceNW()) return true;
                if (faceSW()) return true;
            }
            case 7 -> {
                if (faceNW()) return true;
                if (faceN()) return true;
                if (faceW()) return true;
            }
            default -> { }
        }
        return false;
    }

    private static boolean faceN() throws GameActionException {
        if (!canSense(newLoc, t, Direction.NORTH)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTH)) return true;
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
        return false;
    }
    private static boolean faceNE() throws GameActionException {
        if (!canSense(newLoc, t, Direction.NORTHEAST)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHEAST)) return true;
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
        return false;
    }
    private static boolean faceE() throws GameActionException {
        if (!canSense(newLoc, t, Direction.EAST)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.EAST)) return true;
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
        return false;
    }
    private static boolean faceSE() throws GameActionException {
        if (!canSense(newLoc, t, Direction.SOUTHEAST)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHEAST)) return true;
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
        return false;
    }
    private static boolean faceS() throws GameActionException {
        if (!canSense(newLoc, t, Direction.SOUTH)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTH)) return true;
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
        return false;
    }
    private static boolean faceSW() throws GameActionException {
        if (!canSense(newLoc, t, Direction.SOUTHWEST)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHWEST)) return true;
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
        return false;
    }
    private static boolean faceW() throws GameActionException {
        if (!canSense(newLoc, t, Direction.WEST)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.WEST)) return true;
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
        return false;
    }
    private static boolean faceNW() throws GameActionException {
        if (!canSense(newLoc, t, Direction.NORTHWEST)) return true;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHWEST)) return true;
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
        return false;
    }

    public static boolean miniRun() throws GameActionException {
        if (!rc.isActionReady()) return false;
        for (RobotInfo ri : rc.senseNearbyRobots(2)) {
            if (ri.getTeam() == team) continue;
            attack(ri);
            println("!!!!!!!!!!!!!!mini attacked!!");
            return true;
        }
        return false;
    }
}