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

public final class TryRatNap {
    private TryRatNap() {
    }

    static RobotInfo target;
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
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == team) continue;
            if (chebyshevDistance(myLoc, info.getLocation()) > 2) continue;
            if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill
            if (info.getType() != UnitType.BABY_RAT) continue;
            t = info.getLocation();
            // double weight = scoreEnemy(info);
            weight = 1;
            md = null;

            best = -1;
            for (Direction moveDir : allDirections) {
                MapLocation newLoc = myLoc.add(moveDir);
                if (newLoc.distanceSquaredTo(t) > 2) continue;
                if (!CombatCommon.canMove(moveDir)) continue;
                if (canSense(t, newLoc, info.getDirection()) && info.getHealth() >= rc.getHealth()) continue; // can i ratnap this guy
                // for (Direction facing : relatedDirections(newLoc.directionTo(t))) {
                for (Direction facing : directions) {
                    if (!canSense(newLoc, t, facing)) continue;
                    if (!CombatCommon.canPerformMoves(moveDir, facing)) continue;
                    double score = weight;
                    if (moveDir == Direction.CENTER) {
                        score *= 3; // prefer standing still
                    }
                    score -= CombatStrategy.getDangerFor(moveDir, facing, info) / 5;
                    // println("danger: " + getDangerFor(moveDir, facing, info));
                    if (score <= 0) continue;
                    if (facing != myDir) {
                        score *= 1.2; // prefer turning
                    }
                    if (score > best) {
                        best = score;
                        md = moveDir;
                        bestFacing = facing;
                    }
                }
            }
            if (best > bestScore) {
                bestScore = best;
                target = info;
                bestMove = md;
            }
        }
        if (bestScore < 0) return false;
        assert(CombatStrategy.performMoves(bestMove, bestFacing));
        if (rc.canCarryRat(target.getLocation())) {
            rc.carryRat(target.getLocation());
        } else {
            rc.attack(target.getLocation());
            println("was just shoved against a wall");
        }
        println("!!!!!!!!!!!!!!!!!!!!!!! napped rat");
        return true;
    }
}