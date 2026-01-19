package awubot_sprint2.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import awubot_sprint2.robot.rat.combat.CombatCommon;
import awubot_sprint2.robot.rat.combat.CombatStrategy;
import static awubot_sprint2.Game.*;
import static awubot_sprint2.util.Util.*;
import static awubot_sprint2.robot.rat.BabyRat.*;

public final class TryFight {
    private TryFight() {
    }

    static Direction md;
    static Direction fd;
    static double best;
    static RobotInfo target;
    static Direction moveDir;
    static Direction facing;
    static MapLocation newLoc;
    static double score;
    static boolean facingTarget;

    public static boolean run() throws GameActionException {
        // Profiler.push();
        md = null;
        fd = null;
        best = -1;
        target = CombatStrategy.getNearestTarget();
        if (target == null) return false;
        for (Direction dir : allDirections) {
            if ((CombatStrategy.moveMask & (1 << dir.ordinal())) == 0) continue;
            moveDir = dir;
            if (moveDir != Direction.CENTER && !rc.isMovementReady()) continue;
            if (!CombatCommon.canMove(moveDir)) continue;
            newLoc = myLoc.add(moveDir);
            for (Direction facing : relatedDirections(newLoc.directionTo(target.location))) { // maybe shrink search space if TLE
                int moveCost = CombatCommon.movementCost(moveDir, facing);
                if (moveCost == -1) continue;
                score = CombatStrategy.getCellScoreFor(moveDir, facing, target);
                if (moveCost == 1) score += 10; 
                facingTarget = canSense(newLoc, target.location, facing);
                if (!facingTarget) {
                    score -= 15;
                }
                if (facing != myDir) {
                    // prefer turning 
                    score += 2;
                }
                if (score > best) {
                    best = score;
                    md = moveDir;
                    fd = facing;
                }
            }
        }
        // Profiler.pop("try fight");
        // force this strategy
        if (best < 0.5) return false;
        CombatStrategy.performMoves(md, fd);
        indicate("COMBAT STRAT: FIGHT");
        // try to squeeze some juice out
        
        return true;
    }
}