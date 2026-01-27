

package awubot.robot.rat.combat.moves;

import battlecode.common.Clock;
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
    static Direction bestSecondaryMove;
    static int bestDamage;
    static double bestScore;
    static RobotInfo info;
    static MapLocation t;
    static double weight;
    static Direction moveDir;
    static MapLocation newLoc;
    static double score;
    static int bytecodeStart, bytecodeBudget;
    public static boolean isCat;
    public static int maxBite, enemyHealth, myHealth;

    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        // Profiler.push();
        target = null;
        bestMove = null;
        bestFacing = null;
        bestSecondaryMove = null;
        bestScore = -1e9;
        bytecodeStart = Clock.getBytecodeNum();
        bytecodeBudget = Math.min(4000, (int) ((Clock.getBytecodesLeft() - 1000) / 2));
        maxBite = CombatStrategy.getMaxBiteDamage();
        myHealth = rc.getHealth();
        isCat = false;
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            info = RobotTracker.sortedEnemies[i];
            considerEnemy();
        }
        if (target == null && RobotTracker.numCats > 0) {
            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget);
            else {
                info = RobotTracker.sortedCats[0];
                isCat = true;
                considerEnemy();
            }
        }
        // Profiler.pop("try attack");
        if (target == null) return false;
        if (bestScore < 0.5) {
            indicate("didnt find good attack");
            return false;
        }
        if (bestMove != null) {
            assert(CombatStrategy.performMoves(bestMove, bestFacing));
            attack(target, bestDamage);
        } else {
            attack(target, bestDamage);
            assert(CombatStrategy.performMoves(bestSecondaryMove, bestFacing));
        }
        println("!!!!!!!!!!!!!!!!!!!!!!! attacked rat");
        indicate("COMBAT STRAT: ATTACK score " + bestScore);
        return true;
    }

    private static void considerEnemy() throws GameActionException {
        if (am(13385, 30)) {
            println("considering attacking enemy at " + info.location + " health " + info.getHealth());
            println("max damage " + CombatStrategy.getMaxBiteDamage());
        }
        if (!CombatStrategy.shouldFight(info)) return;
        if (chebyshevDistance(myLoc, info) > 2) return;
        if (am(13385, 30)) {
            println("considering attacking enemy at " + info.location + " health " + info.getHealth() + " passed should fight and distance");
        }

        t = info.location;
        enemyHealth = info.getHealth();
        weight = 10;
        if (info.getHealth() <= CombatStrategy.getMaxBiteDamage()) {
            weight += 30; // prefer one tapping
        }

        selectMoves();
    }

    private static void selectMoves() throws GameActionException {
        boolean canPerformSecondary = myLoc.distanceSquaredTo(t) <= 2;
        switch (myDir.ordinal()) {
            case 0 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
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
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.WEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.WEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.NORTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.WEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.WEST, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.EAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveNE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHEAST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveE();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                        moveC();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveS();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTH, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.EAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.SOUTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
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
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.EAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveNW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveSW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.NORTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.SOUTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
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
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTHEAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                        moveN();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                        moveW();
                        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.EAST, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.EAST, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
                    if (canPerformSecondary) {
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTHWEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                            considerSecondaryMove(Direction.SOUTH, Direction.WEST);
                            if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        if (isCat) {
            int dxToTarget = info.location.x - newLoc.x;
            int dyToTarget = info.location.y - newLoc.y;
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
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
        int x = getSenseLevel(newLoc, t, Direction.NORTH);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTH);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.NORTH != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTH;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }
    private static void faceNE() throws GameActionException {
        int x = getSenseLevel(newLoc, t, Direction.NORTHEAST);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHEAST);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.NORTHEAST != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHEAST;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }
    private static void faceE() throws GameActionException {
        int x = getSenseLevel(newLoc, t, Direction.EAST);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.EAST);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.EAST != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.EAST, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.EAST;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }
    private static void faceSE() throws GameActionException {
        int x = getSenseLevel(newLoc, t, Direction.SOUTHEAST);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.SOUTHEAST != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHEAST;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }
    private static void faceS() throws GameActionException {
        int x = getSenseLevel(newLoc, t, Direction.SOUTH);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTH);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.SOUTH != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTH;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }
    private static void faceSW() throws GameActionException {
        int x = getSenseLevel(newLoc, t, Direction.SOUTHWEST);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.SOUTHWEST != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHWEST;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }
    private static void faceW() throws GameActionException {
        int x = getSenseLevel(newLoc, t, Direction.WEST);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.WEST);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.WEST != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.WEST, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.WEST;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }
    private static void faceNW() throws GameActionException {
        int x = getSenseLevel(newLoc, t, Direction.NORTHWEST);
        if (x == 0) return;
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHWEST);
        if (moveCost == -1) return;
        score = weight;
        if (x == 2) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (Direction.NORTHWEST != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST, info, damageToDeal);
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHWEST;
            bestSecondaryMove = null;
            bestDamage = damageToDeal;
        }
    }

    private static void considerSecondaryMove(Direction moveDir, Direction facing) throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, facing);
        if (moveCost == -1) return;
        double score = weight;
        if (canHardSense(newLoc, t, facing)) score += 5;
        if (moveCost == 0) score -= CombatCommon.strafeCost();
        if (facing != myDir) {
            score += 2; // prefer turning
        }
        int damageToDeal = CombatStrategy.getDamageFor(info);
        score += CombatStrategy.getCellScoreFor(moveDir, facing, info, damageToDeal);
        if (isCat) {
            int dxToTarget = info.location.x - (myLocX + moveDir.dx);
            int dyToTarget = info.location.y - (myLocY + moveDir.dy);
            if (canCatSense(-dxToTarget, -dyToTarget, info.direction)) {
                return; 
            }
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = null;
            bestSecondaryMove = moveDir;
            bestFacing = facing;
            bestDamage = damageToDeal;
        }
    }

    public static boolean miniRun() throws GameActionException {
        if (!rc.isActionReady()) return false;
        switch (myDir.ordinal()) {
            case 0 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTH));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTHEAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTHWEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            case 1 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTHEAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTH));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.EAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            case 2 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.EAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTHEAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTHEAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            case 3 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTHEAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTH));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.EAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            case 4 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTH));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTHEAST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTHWEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            case 5 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTHWEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTH));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.WEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            case 6 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.WEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTHWEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.SOUTHWEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            case 7 -> {
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTHWEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.NORTH));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
                {
                    RobotInfo ri = rc.senseRobotAtLocation(myLoc.add(Direction.WEST));
                    if (ri != null && ri.getTeam() != team) {
                        int damageToDeal;
                        enemyHealth = ri.getHealth();
                        if (enemyHealth - maxBite < myHealth) {
                            damageToDeal = Math.max(10, enemyHealth - myHealth + 1);
                        } else if (enemyHealth - maxBite == myHealth) {
                            damageToDeal = maxBite;
                        } else {
                            damageToDeal = 10;
                        }
                        attack(ri, damageToDeal);
                        println("!!!!!!!!!!!!!!mini attacked!!");
                        indicate("accepted mini attack");
                        return true;
                    }
                }
            }
            default -> { }
        }
        return false;
    }
}