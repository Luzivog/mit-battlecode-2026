package awubot.robot.rat.combat.moves;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
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
    static double damage;
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
                damage = -CombatStrategy.getDangerFor(moveDir, throwDir) / 5;
                // for now, allow the throw path to go through friendly rats
                for (rem = 7; rem >= 0; rem--) {
                    throwTarget = throwTarget.add(throwDir);
                    if (!rc.onTheMap(throwTarget)) {
                        damage += rem;
                        break;
                    }
                    if (rc.canSenseLocation(throwTarget)) {
                        if (!rc.senseMapInfo(throwTarget).isPassable()) {
                            damage += rem;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (damage <= 0.1) continue;
                // account for cell danger levels
                damage -= CombatStrategy.basicDanger[moveDir.ordinal()] / 7;
                if (damage > best) {
                    best = damage;
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
        return true;
    }
}