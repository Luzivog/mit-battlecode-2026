package awubot_microimprov.robot.rat.combat.moves;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import awubot_microimprov.robot.rat.combat.CombatCommon;
import awubot_microimprov.robot.rat.combat.CombatStrategy;
import static awubot_microimprov.Game.*;
import static awubot_microimprov.util.Util.*;
import static awubot_microimprov.robot.rat.BabyRat.*;


public final class TryFight {
    private TryFight() {
    }

    static Direction md;
    static Direction fd;
    static double best;
    static RobotInfo target;
    static Direction moveDir;
    static MapLocation newLoc;
    static double score;
    static boolean facingTarget;
    static int dxToTarget;
    static int dyToTarget;
    static int bytecodeStart, bytecodeBudget;

    public static boolean run() throws GameActionException {
        // Profiler.push();
        md = null;
        fd = null;
        best = -1e9;
        target = CombatStrategy.getNearestTarget();
        if (target == null) return false;
        bytecodeStart = Clock.getBytecodeNum();
        bytecodeBudget = Math.min(3000, (int) (Clock.getBytecodesLeft() - 1000) / 2);
        selectMoves();
        // Profiler.pop("try fight");
        // force this strategy
        if (md == null) return false;
        CombatStrategy.performMoves(md, fd);
        indicate("COMBAT STRAT: FIGHT");
        // try to squeeze some juice out
        return true;
    }

    private static void selectMoves() throws GameActionException {
        moveN();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveNE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveSE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveS();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveSW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveNW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void moveN() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 0)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveN) return;
        moveDir = Direction.NORTH;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveNE() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 1)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveNE) return;
        moveDir = Direction.NORTHEAST;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveE() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 2)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveE) return;
        moveDir = Direction.EAST;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveSE() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 3)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveSE) return;
        moveDir = Direction.SOUTHEAST;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveS() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 4)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveS) return;
        moveDir = Direction.SOUTH;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveSW() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 5)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveSW) return;
        moveDir = Direction.SOUTHWEST;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveW() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 6)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveW) return;
        moveDir = Direction.WEST;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveNW() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 7)) == 0) return;
        if (!rc.isMovementReady()) return;
        if (!CombatCommon.canMoveNW) return;
        moveDir = Direction.NORTHWEST;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void moveC() throws GameActionException {
        if ((CombatStrategy.moveMask & (1 << 8)) == 0) return;
        if (!CombatCommon.canMoveC) return;
        moveDir = Direction.CENTER;
        newLoc = myLoc.add(moveDir);
        dxToTarget = target.location.x - newLoc.x;
        dyToTarget = target.location.y - newLoc.y;
        switch (newLoc.directionTo(target.location).ordinal()) {
            case 0 -> {
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 1 -> {
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 2 -> {
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 3 -> {
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 4 -> {
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 5 -> {
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 6 -> {
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            case 7 -> {
                faceNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                faceW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
            }
            default -> { }
        }
    }

    private static void faceN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTH);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.NORTH);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.NORTH != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.NORTH;
        }
    }

    private static void faceNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHEAST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.NORTHEAST);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.NORTHEAST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.NORTHEAST;
        }
    }

    private static void faceE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.EAST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.EAST, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.EAST);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.EAST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.EAST;
        }
    }

    private static void faceSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.SOUTHEAST);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.SOUTHEAST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.SOUTHEAST;
        }
    }

    private static void faceS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTH);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.SOUTH);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.SOUTH != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.SOUTH;
        }
    }

    private static void faceSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.SOUTHWEST);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.SOUTHWEST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.SOUTHWEST;
        }
    }

    private static void faceW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.WEST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.WEST, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.WEST);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.WEST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.WEST;
        }
    }

    private static void faceNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(moveDir, Direction.NORTHWEST);
        if (moveCost == -1) return;
        score = CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST, target);
        if (moveCost == 1) score += 10;
        facingTarget = canSense(dxToTarget, dyToTarget, Direction.NORTHWEST);
        if (!facingTarget) {
            score -= 15;
        }
        if (Direction.NORTHWEST != myDir) {
            // prefer turning
            score += 2;
        }
        if (score > best) {
            best = score;
            md = moveDir;
            fd = Direction.NORTHWEST;
        }
    }

}