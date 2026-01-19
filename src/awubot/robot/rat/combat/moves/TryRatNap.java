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




public final class TryRatNap {
    private TryRatNap() {
    }

    static RobotInfo target;
    static RobotInfo info;
    static Direction bestMove;
    static Direction bestFacing;
    static Direction bestSecondaryMove;
    static double bestScore;
    static Direction moveDir;
    static int dxToTarget;
    static int dyToTarget;
    static double best;
    static double weight;
    static MapLocation t;

    private static int BYTECODE_BUDGET = 3000;

    // return true if successfully ratnapped someone
    // TODO: do danger levels and verify you dont die on the next turn
    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        target = null;
        bestMove = null;
        bestFacing = null;
        bestSecondaryMove = null;
        bestScore = -1;
        int bytecodeStart = Clock.getBytecodeNum();
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            if (Clock.getBytecodeNum() - bytecodeStart > BYTECODE_BUDGET) break;
            info = RobotTracker.sortedEnemies[i];
            t = info.location;
            if (chebyshevDistance(myLoc, t) > 2) continue;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            // double weight = scoreEnemy(info);
            weight = 30;
            selectMoves();
            if (Clock.getBytecodeNum() - bytecodeStart > BYTECODE_BUDGET) break;
            selectSecondaryMoves();
        }
        if (bestScore < 0.5) return false;
        if (bestMove != null) {
            assert(CombatStrategy.performMoves(bestMove, bestFacing));
            if (rc.canCarryRat(target.location)) {
                rc.carryRat(target.location);
            } else {
                attack(target);
            }
        } else {
            if (rc.canCarryRat(target.location)) {
                rc.carryRat(target.location);
            } else {
                attack(target);
            }
            assert(CombatStrategy.performMoves(bestSecondaryMove, bestFacing));
        }
        println("!!!!!!!!!!!!!!!!!!!!!!! napped rat");
        indicate("COMBAT STRAT: NAP");
        return true;
    }

    private static void selectMoves() throws GameActionException {
        switch (myDir.ordinal()) {
            case 0 -> {
                moveN();
                moveNE();
                moveNW();
                moveE();
                moveW();
            }
            case 1 -> {
                moveNE();
                moveN();
                moveE();
                moveNW();
                moveSE();
            }
            case 2 -> {
                moveE();
                moveNE();
                moveSE();
                moveN();
                moveS();
            }
            case 3 -> {
                moveSE();
                moveS();
                moveE();
                moveNE();
                moveSW();
            }
            case 4 -> {
                moveS();
                moveSE();
                moveSW();
                moveE();
                moveW();
            }
            case 5 -> {
                moveSW();
                moveS();
                moveW();
                moveNW();
                moveSE();
            }
            case 6 -> {
                moveW();
                moveNW();
                moveSW();
                moveN();
                moveS();
            }
            case 7 -> {
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
        dxToTarget = t.x - (myLocX );
        dyToTarget = t.y - (myLocY  + 1);
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void moveNE() throws GameActionException {
        if (!CombatCommon.canMoveNE) return;
        moveDir = Direction.NORTHEAST;
        dxToTarget = t.x - (myLocX  + 1);
        dyToTarget = t.y - (myLocY  + 1);
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void moveE() throws GameActionException {
        if (!CombatCommon.canMoveE) return;
        moveDir = Direction.EAST;
        dxToTarget = t.x - (myLocX  + 1);
        dyToTarget = t.y - (myLocY );
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void moveSE() throws GameActionException {
        if (!CombatCommon.canMoveSE) return;
        moveDir = Direction.SOUTHEAST;
        dxToTarget = t.x - (myLocX  + 1);
        dyToTarget = t.y - (myLocY  + -1);
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void moveS() throws GameActionException {
        if (!CombatCommon.canMoveS) return;
        moveDir = Direction.SOUTH;
        dxToTarget = t.x - (myLocX );
        dyToTarget = t.y - (myLocY  + -1);
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void moveSW() throws GameActionException {
        if (!CombatCommon.canMoveSW) return;
        moveDir = Direction.SOUTHWEST;
        dxToTarget = t.x - (myLocX  + -1);
        dyToTarget = t.y - (myLocY  + -1);
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void moveW() throws GameActionException {
        if (!CombatCommon.canMoveW) return;
        moveDir = Direction.WEST;
        dxToTarget = t.x - (myLocX  + -1);
        dyToTarget = t.y - (myLocY );
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void moveNW() throws GameActionException {
        if (!CombatCommon.canMoveNW) return;
        moveDir = Direction.NORTHWEST;
        dxToTarget = t.x - (myLocX  + -1);
        dyToTarget = t.y - (myLocY  + 1);
        if (dxToTarget * dxToTarget + dyToTarget * dyToTarget > 2) return;
        if (info.getHealth() >= rc.getHealth() && canSense(-dxToTarget, -dyToTarget, info.direction)) return; // can i ratnap this guy
switch (dxToTarget) {
            case -1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSW();
                        faceS();
                        faceW();
}
                    case 0 -> {                        faceW();
                        faceNW();
                        faceSW();
}
                    case 1 -> {                        faceNW();
                        faceN();
                        faceW();
}
                    default -> { }
                }
            }
            case 0 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceS();
                        faceSE();
                        faceSW();
}
                    case 0 -> {}
                    case 1 -> {                        faceN();
                        faceNE();
                        faceNW();
}
                    default -> { }
                }
            }
            case 1 -> {
                switch (dyToTarget) {
                    case -1 -> {                        faceSE();
                        faceS();
                        faceE();
}
                    case 0 -> {                        faceE();
                        faceNE();
                        faceSE();
}
                    case 1 -> {                        faceNE();
                        faceN();
                        faceE();
}
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void faceN() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.NORTH)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTH)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTH, info);
        if (score <= 0.5) return;
        if (Direction.NORTH != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTH;
            bestSecondaryMove = null;
        }
    }

    private static void faceNE() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.NORTHEAST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHEAST)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHEAST, info);
        if (score <= 0.5) return;
        if (Direction.NORTHEAST != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHEAST;
            bestSecondaryMove = null;
        }
    }

    private static void faceE() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.EAST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.EAST)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.EAST, info);
        if (score <= 0.5) return;
        if (Direction.EAST != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.EAST;
            bestSecondaryMove = null;
        }
    }

    private static void faceSE() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.SOUTHEAST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHEAST)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHEAST, info);
        if (score <= 0.5) return;
        if (Direction.SOUTHEAST != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHEAST;
            bestSecondaryMove = null;
        }
    }

    private static void faceS() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.SOUTH)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTH)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTH, info);
        if (score <= 0.5) return;
        if (Direction.SOUTH != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTH;
            bestSecondaryMove = null;
        }
    }

    private static void faceSW() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.SOUTHWEST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.SOUTHWEST)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.SOUTHWEST, info);
        if (score <= 0.5) return;
        if (Direction.SOUTHWEST != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.SOUTHWEST;
            bestSecondaryMove = null;
        }
    }

    private static void faceW() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.WEST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.WEST)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.WEST, info);
        if (score <= 0.5) return;
        if (Direction.WEST != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.WEST;
            bestSecondaryMove = null;
        }
    }

    private static void faceNW() throws GameActionException {
        if (!canSense(dxToTarget, dyToTarget, Direction.NORTHWEST)) return;
        if (!CombatCommon.canPerformMoves(moveDir, Direction.NORTHWEST)) return;
        double score = weight;
        score += CombatStrategy.getCellScoreFor(moveDir, Direction.NORTHWEST, info);
        if (score <= 0.5) return;
        if (Direction.NORTHWEST != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = moveDir;
            bestFacing = Direction.NORTHWEST;
            bestSecondaryMove = null;
        }
    }

    private static void selectSecondaryMoves() throws GameActionException {
        int dx = t.x - myLocX;
        int dy = t.y - myLocY;
        if (dx * dx + dy * dy > 2) return;
        if (info.getHealth() >= rc.getHealth()) return; // can i ratnap this guy
        switch (myDir.ordinal()) {
            case 0 -> {
                considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTH);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTH);
                considerSecondaryMove(Direction.SOUTH, Direction.NORTHEAST);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHEAST);
                considerSecondaryMove(Direction.SOUTH, Direction.NORTHWEST);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHWEST);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
            }
            case 1 -> {
                considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                considerSecondaryMove(Direction.SOUTH, Direction.NORTHEAST);
                considerSecondaryMove(Direction.WEST, Direction.NORTHEAST);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTH);
                considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                considerSecondaryMove(Direction.WEST, Direction.NORTH);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.EAST);
                considerSecondaryMove(Direction.SOUTH, Direction.EAST);
                considerSecondaryMove(Direction.WEST, Direction.EAST);
            }
            case 2 -> {
                considerSecondaryMove(Direction.WEST, Direction.EAST);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.EAST);
                considerSecondaryMove(Direction.NORTHWEST, Direction.EAST);
                considerSecondaryMove(Direction.WEST, Direction.NORTHEAST);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.NORTHEAST);
                considerSecondaryMove(Direction.NORTHWEST, Direction.NORTHEAST);
                considerSecondaryMove(Direction.WEST, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.SOUTHWEST, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
            }
            case 3 -> {
                considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.NORTH, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.WEST, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTH);
                considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                considerSecondaryMove(Direction.WEST, Direction.SOUTH);
                considerSecondaryMove(Direction.NORTHWEST, Direction.EAST);
                considerSecondaryMove(Direction.NORTH, Direction.EAST);
                considerSecondaryMove(Direction.WEST, Direction.EAST);
            }
            case 4 -> {
                considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTH);
                considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTH);
                considerSecondaryMove(Direction.NORTH, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHEAST);
                considerSecondaryMove(Direction.NORTH, Direction.SOUTHWEST);
                considerSecondaryMove(Direction.NORTHWEST, Direction.SOUTHWEST);
                considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
            }
            case 5 -> {
                considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
                considerSecondaryMove(Direction.NORTH, Direction.SOUTHWEST);
                considerSecondaryMove(Direction.EAST, Direction.SOUTHWEST);
                considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTH);
                considerSecondaryMove(Direction.NORTH, Direction.SOUTH);
                considerSecondaryMove(Direction.EAST, Direction.SOUTH);
                considerSecondaryMove(Direction.NORTHEAST, Direction.WEST);
                considerSecondaryMove(Direction.NORTH, Direction.WEST);
                considerSecondaryMove(Direction.EAST, Direction.WEST);
            }
            case 6 -> {
                considerSecondaryMove(Direction.EAST, Direction.WEST);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.WEST);
                considerSecondaryMove(Direction.NORTHEAST, Direction.WEST);
                considerSecondaryMove(Direction.EAST, Direction.NORTHWEST);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                considerSecondaryMove(Direction.NORTHEAST, Direction.NORTHWEST);
                considerSecondaryMove(Direction.EAST, Direction.SOUTHWEST);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.SOUTHWEST);
                considerSecondaryMove(Direction.NORTHEAST, Direction.SOUTHWEST);
            }
            case 7 -> {
                considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTHWEST);
                considerSecondaryMove(Direction.SOUTH, Direction.NORTHWEST);
                considerSecondaryMove(Direction.EAST, Direction.NORTHWEST);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.NORTH);
                considerSecondaryMove(Direction.SOUTH, Direction.NORTH);
                considerSecondaryMove(Direction.EAST, Direction.NORTH);
                considerSecondaryMove(Direction.SOUTHEAST, Direction.WEST);
                considerSecondaryMove(Direction.SOUTH, Direction.WEST);
                considerSecondaryMove(Direction.EAST, Direction.WEST);
            }
            default -> { }
        }
    }

    private static void considerSecondaryMove(Direction moveDir, Direction facing) throws GameActionException {
        if (!CombatCommon.canPerformMoves(moveDir, facing)) return;
        double score = weight + 5;
        score += CombatStrategy.getCellScoreFor(moveDir, facing, info);
        if (score <= 0.5) return;
        if (facing != myDir) {
            score *= 1.2; // prefer turning
        }
        if (score > bestScore) {
            bestScore = score;
            target = info;
            bestMove = null;
            bestFacing = facing;
            bestSecondaryMove = moveDir;
        }
    }

    public static boolean miniRun() throws GameActionException {
        // first try to nap a rat looking at us
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            info = RobotTracker.sortedEnemies[i];
            if (info.getTeam() == team) continue;
            if (info.location.distanceSquaredTo(myLoc) > 2) continue;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            int dx = info.location.x - myLocX;
            int dy = info.location.y - myLocY;
            if (canSense(dx, dy, info.direction)) {
                if (rc.canCarryRat(info.location)) {
                    rc.carryRat(info.location);
                    println("!!!!!!!!!!!!!!!!!!!!!!! mini napped rat");
                    return true;
                }
            }
        }
        // nap anyone
        for (RobotInfo info : rc.senseNearbyRobots(2)) {
            if (info.getTeam() == team) continue;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            if (rc.canCarryRat(info.location)) {
                rc.carryRat(info.location);
                println("!!!!!!!!!!!!!!!!!!!!!!! mini napped rat");
                indicate("COMBAT STRAT: MINI NAP");
                return true;
            }
        }
        return false;
    }
}