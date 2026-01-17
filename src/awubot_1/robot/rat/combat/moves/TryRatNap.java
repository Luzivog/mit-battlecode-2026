package awubot_1.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot_1.robot.rat.combat.CombatCommon;
import awubot_1.robot.rat.combat.CombatStrategy;
import static awubot_1.Game.*;
import static awubot_1.util.Util.*;
import static awubot_1.robot.rat.BabyRat.*;
import awubot_1.RobotTracker;

public final class TryRatNap {
    private TryRatNap() {
    }

    static RobotInfo target;
    static RobotInfo info;
    static Direction bestMove;
    static Direction bestFacing;
    static double bestScore;
    static Direction md;
    static double best;
    static double weight;
    static MapLocation t;

    // return true if successfully ratnapped someone
    // TODO: do danger levels and verify you dont die on the next turn
    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        target = null;
        bestMove = null;
        bestFacing = null;
        bestScore = -1;
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            info = RobotTracker.sortedEnemies[i];
            if (info.getTeam() == team) continue;
            if (chebyshevDistance(myLoc, info.location) > 2) continue;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            t = info.location;
            // double weight = scoreEnemy(info);
            weight = 30;

            for (Direction moveDir : allDirections) {
                MapLocation newLoc = myLoc.add(moveDir);
                if (newLoc.distanceSquaredTo(t) > 2) continue;
                if (!CombatCommon.canMove(moveDir)) continue;
                if (canSense(t, newLoc, info.direction) && info.getHealth() >= rc.getHealth()) continue; // can i ratnap this guy
                // for (Direction facing : relatedDirections(newLoc.directionTo(t))) {
                for (Direction facing : directions) {
                    if (!canSense(newLoc, t, facing)) continue;
                    if (!CombatCommon.canPerformMoves(moveDir, facing)) continue;
                    double score = weight;
                    if (moveDir == Direction.CENTER) {
                        score += 10; // prefer standing still
                    }
                    score += CombatStrategy.getCellScoreFor(moveDir, facing, info);
                    // println("danger: " + getDangerFor(moveDir, facing, info));
                    if (score <= 0.5) continue;
                    if (facing != myDir) {
                        score *= 1.2; // prefer turning
                    }
                    if (score > bestScore) {
                        bestScore = score;
                        target = info;
                        bestMove = moveDir;
                        bestFacing = facing;
                    }
                }
            }
        }
        if (bestScore < 0.5) return false;
        assert(CombatStrategy.performMoves(bestMove, bestFacing));
        if (rc.canCarryRat(target.location)) {
            rc.carryRat(target.location);
        } else {
            attack(target);
            println("was just shoved against a wall");
        }
        println("!!!!!!!!!!!!!!!!!!!!!!! napped rat");
        indicate("COMBAT STRAT: NAP");
        return true;
    }

    public static boolean miniRun() throws GameActionException {
        // first try to nap a rat looking at us
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            info = RobotTracker.sortedEnemies[i];
            if (info.getTeam() == team) continue;
            if (info.location.distanceSquaredTo(myLoc) > 2) continue;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            if (canSense(myLoc, info.location, info.direction)) {
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