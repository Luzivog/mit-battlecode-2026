package awu_old_naman.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import awu_old_naman.robot.rat.combat.CombatCommon;
import awu_old_naman.robot.rat.combat.CombatStrategy;
import static awu_old_naman.Game.*;
import static awu_old_naman.util.Util.*;
import static awu_old_naman.robot.rat.BabyRat.*;

public final class TryThrow {
    private TryThrow() {
    }

    static Direction md;
    static Direction td;
    static double best;
    static Direction moveDir;
    static Direction throwDir;
    static MapLocation newLoc;
    static MapLocation throwTarget;
    static double score;
    static int rem;

    // return false if didnt throw -- some other process can figure out how to move
    // TODO: maybe weight targets based on how close they are to friendly rats? might be too effort
    public static boolean run() throws GameActionException {
        // Profiler.push();
        md = null;
        td = null;
        best = -1;
        for (Direction dir : allDirections) {
            moveDir = dir;
            if (moveDir != Direction.CENTER && !rc.canMove(moveDir)) continue;
            newLoc = myLoc.add(moveDir);
            for (Direction tdir : directions) {
                throwDir = tdir;
                if (!CombatCommon.canPerformMoves(moveDir, throwDir)) continue;
                throwTarget = newLoc.add(throwDir);
                if (!rc.canSenseLocation(throwTarget)) continue; // make sure u know whats happening there
                if (!rc.senseMapInfo(throwTarget).isPassable()) continue; // need empty
                if (throwTarget != myLoc && rc.senseRobotAtLocation(throwTarget) != null) continue; // need empty
                score = CombatStrategy.getCellScoreFor(moveDir, throwDir) + 10;
                // for now, allow the throw path to go through friendly rats
                for (rem = 7; rem >= 0; rem--) {
                    throwTarget = throwTarget.add(throwDir);
                    if (!rc.onTheMap(throwTarget)) {
                        score += rem * 4;
                        break;
                    }
                    if (rc.canSenseLocation(throwTarget)) {
                        if (!rc.senseMapInfo(throwTarget).isPassable()) {
                            score += rem * 4;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (score <= 0.1) continue;
                if (score > best) {
                    best = score;
                    md = moveDir;
                    td = throwDir;
                }
            }
        }
        // Profiler.pop("try throw");
        if (best < 0) return false;
        assert(CombatStrategy.performMoves(md, td));
        rc.throwRat();
        println("tried throwing rat");
        indicate("COMBAT STRAT: THROW");
        return true;
    }
}