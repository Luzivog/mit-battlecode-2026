package awubot.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
import awubot.CellTracker;
import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;

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
        if (!rc.isActionReady()) return false;
        // Profiler.push();
        md = null;
        td = null;
        best = -1;
        for (Direction dir : allDirections) {
            moveDir = dir;
            if (moveDir != Direction.CENTER && !rc.canMove(moveDir)) continue;
            newLoc = myLoc.add(moveDir);
            for (Direction tdir : relatedDirections(myDir)) {
                throwDir = tdir;
                if (!CombatCommon.canPerformMoves(moveDir, throwDir)) continue;
                throwTarget = newLoc.add(throwDir);
                if (!rc.canSenseLocation(throwTarget)) continue; // make sure u know whats happening there
                if (!rc.senseMapInfo(throwTarget).isPassable()) continue; // need empty
                if (throwTarget != myLoc && rc.senseRobotAtLocation(throwTarget) != null) continue; // need empty
                score = CombatStrategy.getCellScoreFor(moveDir, throwDir) / 3;
                for (rem = 7; rem >= 0; rem--) {
                    throwTarget = throwTarget.add(throwDir);
                    if (!rc.onTheMap(throwTarget)) {
                        score += rem * 4;
                        break;
                    }
                    if (!CellTracker.isSeen(throwTarget)) break;
                    if (rc.canSenseLocation(throwTarget)) {
                        RobotInfo info = rc.senseRobotAtLocation(throwTarget);
                        if (info != null) {
                            if (info.getTeam() == team) {
                                if (info.getType() == UnitType.RAT_KING) {
                                    score = -100;
                                    break;
                                } else {
                                    if (rem == 7) score -= (rem * 4) / 2;
                                }
                            } else {
                                if (info.getType() == UnitType.RAT_KING) {
                                    score += 100;
                                    break;
                                } else {
                                    if (rem == 7) score += (rem * 4);
                                    else score += ((double) (rem * 4)) / (8 - rem);
                                }
                            }
                        }
                    }
                    if (CellTracker.isDirt(throwTarget) || !CellTracker.isPassable(throwTarget)) {
                        score += rem * 4;
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