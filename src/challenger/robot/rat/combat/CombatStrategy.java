

package challenger.robot.rat.combat;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;
import challenger.CellTracker;
import challenger.Game;
import challenger.Pathfinder;
import challenger.comms.GlobalArray;
import challenger.comms.Squeaker;
import challenger.comms.Enemy;
import challenger.comms.EnemyManager;
import challenger.robot.Strategy;
import challenger.robot.rat.combat.moves.TryFight;
import challenger.robot.rat.combat.moves.TryAttack;
import challenger.robot.rat.combat.moves.TryRatNap;
import challenger.robot.rat.combat.moves.TryPosition;
import challenger.robot.rat.combat.moves.TryThrow;
import challenger.robot.rat.combat.moves.TryTrap;
import challenger.util.Profiler;
import challenger.util.Util;

import static challenger.robot.rat.BabyRat.*;
import static challenger.Game.*;
import static challenger.util.Util.*;
import challenger.RobotTracker;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/*

  TODO:
  - add code for detecting enemies out of vision
  - consider avoiding cells within cheb dist of 1/2 of enemy rats
  - strongly prefer looking at enemies
  - offence squeak signals
  - combat tracker bugfix
  - better movement

*/

public class CombatStrategy extends Strategy {

    public CombatStrategy() {
    }

    private void precomp() throws GameActionException {
        CombatCommon.precomp();
        CombatTracker.tick();
    }

    public boolean runTick() throws GameActionException {
        try {
            return _runTick();
        } finally {
        }
    }

    public static int moveMask, sourceCnt;
    public MapLocation[] sources = new MapLocation[35];
    public static Enemy funnyTarget;

    // idek why this is returning. check if can move anyways after to squeeze out some juice
    private static int trollID = -1;
    private static boolean shouldTroll = true;
    private boolean _runTick() throws GameActionException {
        if (rc.isBeingCarried() || rc.isBeingThrown()) return false;
        // if (rc.getRawCheese() >= 50 && myLoc.distanceSquaredto(GlobalArray.nearestRatKing) <= 36) return false;
        // println("-- COMBAT STRAT TICK --- starting bytecode count " + Clock.getBytecodeNum() + "\n");

        funnyTarget = null;
        if (rc.getCarrying() == null || rc.getCarrying().getTeam() == team) {
            // Profiler.push();
            sourceCnt = 0;
            for (int i = 0; i < RobotTracker.numEnemies; i++) {
                sources[sourceCnt++] = RobotTracker.sortedEnemies[i].location;
            }
            for (int i = 0; i < RobotTracker.numCats; i++) {
                if (shouldFight(RobotTracker.sortedCats[i])) {
                    sources[sourceCnt++] = RobotTracker.sortedCats[i].location;
                }
            }
            if (sourceCnt == 0 && EnemyManager.numKnownEnemies > 0 && rc.canTurn()) {
                // find nearest
                int nearestDist = Integer.MAX_VALUE;
                for (int i = 0; i < EnemyManager.numKnownEnemies; i++) {
                    MapLocation enemyLoc = EnemyManager.knownEnemies[i].location;
                    int dist = myLoc.distanceSquaredTo(enemyLoc);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        funnyTarget = EnemyManager.knownEnemies[i];
                    }
                }
                sources[sourceCnt++] = funnyTarget.location;
            }
            if (sourceCnt == 0) return false;
            // for (int i = 0; i < sourceCnt; i++) {
            //     indicate("combat source prep: " + sources[i]);
            // }
            int[] bfsResult = Pathfinder.multisourceBfs(sources, sourceCnt);
            moveMask = bfsResult[0];    
            int bfsIters = bfsResult[1];
            // println("BFS iters: " + bfsIters);
            // // Profiler.pop("combat bfs");
            // indicate("MOVEMASK: " + Integer.toBinaryString(moveMask));
            if (moveMask == 0 && shouldTroll && rc.getID() % 10 == 7 && RobotTracker.numEnemies > 0) {
                if (trollID != -1) {
                    if (rc.canSenseRobot(trollID)) {
                        return true;
                    } else {
                        trollID = -1;
                        shouldTroll = false;
                    }
                } else {
                    RobotInfo info = RobotTracker.sortedEnemies[0];
                    boolean canFightYou = info.getType() == UnitType.BABY_RAT && canSense(info.location, myLoc, info.direction);
                    if (canFightYou) {
                        trollID = info.getID();
                        return true;
                    }
                }
            }
            if (moveMask == 0) return false; // no enemies nearby
            if (funnyTarget != null && bfsIters <= 5) {
                // println("AGGROED ON FUNNY TARGET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + funnyTarget.location);
                // indicate("funny pos: " + funnyTarget.location);
                turn(myLoc.directionTo(funnyTarget.location));
            }
        }

        try {
            precomp();
        } finally {
        }

        isLastMove = true;
        RobotInfo carried = rc.getCarrying();
        if (carried != null) {
            if (carried.getTeam() == team) {
                if (TryThrow.run()) return true;
            } else if (TryThrow.search()) {
                // an enemy: throw for a collision at step 2 or better, else hold it and act with the free action
                if (TryThrow.best >= TryThrow.STEP2_HIT || mustThrow()) {
                    if (TryThrow.execute()) return true;
                } else {
                    if (TryTrap.run()) return true;
                    if (TryAttack.run()) return true;
                    aimNextThrow();
                    if (TryPosition.search()) {
                        TryPosition.execute();
                        return true;
                    }
                    // nowhere safe to hold: a grab of the carrier would free the victim
                    if (TryThrow.execute()) return true;
                }
            } else if (!rc.isActionReady() && rc.isMovementReady()) {
                TryThrow.scan();
                aimNextThrow();
            }
            TryPosition.run();
            return true;
        }

        // Profiler.push();
        try {
            boolean tryRatNapRes = TryRatNap.run();
            if (tryRatNapRes) return true;
        } finally {
            // Profiler.pop("try ratnap", 5000);
        }

        // Profiler.push();
        try {
            boolean tryTrapRes = TryTrap.run();
            if (tryTrapRes) return true;
        } finally {
            // Profiler.pop("try trap", 5000);
        }

        // Profiler.push();
        try {
            boolean tryAttackRes = TryAttack.run();
            if (tryAttackRes) return true;
        } finally {
            // Profiler.pop("try attack", 5000);
        }

        // Profiler.push();
        boolean tryFightRes;
        try {
            tryFightRes = TryFight.run();
        } finally {
            // Profiler.pop("try fight");
        }
        if (tryFightRes) {
            // Profiler.push();
            try {
                miniTick();
            } finally {
                // Profiler.pop("mini tick", 500);
            }
            return true;
        }// println
        // TODO: deal with cats (maybe naman's job?)
        // return false;
        return false;
    }

    /*
      Throw whatever the line once the carry is 8 rounds old: the last possible throw is at grab + 10 in either turn
      order (docs/rules.md §9), and a bite or trap with raw cheese can leave the action unready for a turn. Also throw
      at 24 HP or less, two 12-damage bites, as the carrier's death frees the victim.
    */
    // the carrier's move lines up the best line for its next throw, when that line is worth a throw at all
    private static void aimNextThrow() {
        if (TryThrow.aimBest > 0) TryPosition.aim(TryThrow.aimMove, TryThrow.aimFacing, TryThrow.aimBest);
    }

    private static boolean mustThrow() {
        return time - carryStartTime >= 8 || rc.getHealth() <= 24;
    }

    public boolean miniTick() throws GameActionException {
        // // Profiler.push();
        boolean tryRatNapRes = TryRatNap.miniRun();
        // // Profiler.pop("mini ratnap");
        if (tryRatNapRes) return true;

        boolean tryAttackRes = TryAttack.miniRun();
        if (tryAttackRes) return true;

        // // Profiler.push();
        boolean tryTrapRes = TryTrap.miniRun();
        // // Profiler.pop("mini trap");
        if (tryTrapRes) return true;
        return false;
    }

    public static boolean performMoves(Direction moveDir, Direction facing) throws GameActionException {
        // The engine's answer now, not precomp's: since precomp, a rat we threw can stop in the tile ahead, and a turn
        // that ran out of bytecode resumes in a changed world. A refused move would throw and lose the turn.
        if (moveDir != Direction.CENTER && !rc.canMove(moveDir)) return false;
        if (moveDir == Direction.CENTER) {
            if (facing != myDir) {
                if (!rc.canTurn()) return false;
                turn(facing);
            }
            return true;
        }
        if (!rc.isMovementReady()) return false;
        if (moveDir == myDir && facing == myDir) {
            rawMove();
            return true; // move
        }
        if (moveDir == myDir) { // move turn
            rawMove();
            if (facing != myDir) {
                if (!rc.canTurn()) return false;
                turn(facing);
            }
            return true;
        }
        if (moveDir == facing) { // turn move
            if (!rc.canTurn()) return false;
            turn(moveDir);
            rawMove();
            return true;
        }
        if (facing == myDir) { // strafe
            rawMove(moveDir);
            return true;
        }
        rawMove(moveDir);
        if (!rc.canTurn()) return false;
        turn(facing);
        return true;
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing, RobotInfo target, int damage) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing, target, damage)) return -challenger.Tune.THREAT_PENALTY;
        return 0;
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing, RobotInfo target) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing, target)) return -challenger.Tune.THREAT_PENALTY;
        return 0;
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing)) return -challenger.Tune.THREAT_PENALTY;
        return 0;
    }

    public static int getBiteDamage() {
        if (rc.getAllCheese() < challenger.Tune.BITE_BONUS_CHEESE) return 10;
        else return 12;
    }
    
    /*

        attacking another solo rat is safe as if:
        - you end the turn with at least as much health (no ratnap)
        - they can't see you
        if you are in a horde, it should be safe to run at them

    */
    public static boolean shouldFight(RobotInfo enemy) {
        if (enemy.getType() == UnitType.CAT) {
            if (enemy.location.distanceSquaredTo(GlobalArray.nearestRatKing) <= 36) return true;
            return time > 500 && rc.getAllCheese() > 1000 && RobotTracker.numEnemies == 0 && rc.getRawCheese() == 0 && GlobalArray.numberOfRatsAlive >= 16 && (rc.getID() % 3 == 0 || time >= 1800);
        }
        if (enemy.getType() == UnitType.RAT_KING) return true;
        if (enemy.getHealth() - getBiteDamage() <= rc.getHealth()) return true;
        if (!canSense(enemy.location, myLoc, enemy.direction)) return true;
        if (GlobalArray.nearestRatKing != null && GlobalArray.nearestRatKing.distanceSquaredTo(myLoc) <= 36) return true;
        if (RobotTracker.numAllies > RobotTracker.numEnemies) return true;
        return false;
    }

    public static int getCheeseNeeded(int damageBuff) {
        if (damageBuff == 0) return 0;
        int base = damageBuff - 1;
        return base * base + 1;
    }
}