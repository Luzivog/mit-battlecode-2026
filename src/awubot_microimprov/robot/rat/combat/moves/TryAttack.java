package awubot_microimprov.robot.rat.combat.moves;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot_microimprov.robot.rat.combat.CombatCommon;
import awubot_microimprov.robot.rat.combat.CombatStrategy;
import static awubot_microimprov.Game.*;
import static awubot_microimprov.util.Util.*;
import static awubot_microimprov.robot.rat.BabyRat.*;
import awubot_microimprov.RobotTracker;


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
    static int bytecodeStart, bytecodeBudget;

    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        // Profiler.push();
        target = null;
        bestMove = null;
        bestFacing = null;
        bestScore = -1;
        bytecodeStart = Clock.getBytecodeNum();
        bytecodeBudget = Math.min(3000, (int) (Clock.getBytecodesLeft() - 1000) / 2);
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            info = RobotTracker.sortedEnemies[i];
            considerEnemy();
        }
        if (target == null && RobotTracker.numCats > 0) {
            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget);
            else {
                info = RobotTracker.sortedCats[0];
                considerEnemy();
            }
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                moveC();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
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