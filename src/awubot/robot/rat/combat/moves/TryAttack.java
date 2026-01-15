package awubot.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;



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
    static Direction facing;
    static double score;

    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        // Profiler.push();
        target = null;
        bestMove = null;
        bestFacing = null;
        bestScore = -1;
        for (RobotInfo ri : rc.senseNearbyRobots()) {
            info = ri;
            if (info.getTeam() == team) continue;
            if (!CombatStrategy.shouldFight(info)) continue;
            if (chebyshevDistance(myLoc, info.getLocation()) > 2) continue;

            t = info.getLocation();
            // double weight = scoreEnemy(info);
            weight = 1;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) {
                weight += 2.0; // prefer one tapping
            }

            for (Direction dir : allDirections) {
                moveDir = dir;
                if (!CombatCommon.canMove(moveDir)) continue;
                newLoc = myLoc.add(moveDir);
                if (newLoc.distanceSquaredTo(t) > 2) continue;
switch (newLoc.directionTo(t).ordinal()) {
    case 0 -> {
                        {
                    facing = Direction.NORTH;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.NORTHEAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.NORTHWEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    case 1 -> {
                        {
                    facing = Direction.NORTHEAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.NORTH;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.EAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    case 2 -> {
                        {
                    facing = Direction.EAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.NORTHEAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.SOUTHEAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    case 3 -> {
                        {
                    facing = Direction.SOUTHEAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.SOUTH;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.EAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    case 4 -> {
                        {
                    facing = Direction.SOUTH;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.SOUTHEAST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.SOUTHWEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    case 5 -> {
                        {
                    facing = Direction.SOUTHWEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.SOUTH;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.WEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    case 6 -> {
                        {
                    facing = Direction.WEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.NORTHWEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.SOUTHWEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    case 7 -> {
                        {
                    facing = Direction.NORTHWEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.NORTH;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

                        {
                    facing = Direction.WEST;
                    if (canSense(newLoc, t, facing) && CombatCommon.canPerformMoves(moveDir, facing)) {
                        score = weight;
                        score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                        if (score > 0) {
                            if (facing != myDir) {
                                score *= 1.1; // prefer turning
                            }
                            if (score > bestScore) {
                                bestScore = score;
                                target = info;
                                bestMove = moveDir;
                                bestFacing = facing;
                            }
                        } else {
                            println("poop score!");
                        }
                    }
                }

    }
    default -> { }
}            }
        }
        // Profiler.pop("try attack");
        if (bestScore < 0) return false;
        assert(CombatStrategy.performMoves(bestMove, bestFacing));
        rc.attack(target.getLocation());
        println("!!!!!!!!!!!!!!!!!!!!!!! attacked rat");
        return true;
    }
}