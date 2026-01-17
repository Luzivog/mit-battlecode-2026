package awu_old_naman.robot.rat.combat;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;
import awu_old_naman.CellTracker;
import awu_old_naman.Game;
import awu_old_naman.Pathfinder;
import awu_old_naman.comms.GlobalArray;
import awu_old_naman.comms.Squeaker;
import awu_old_naman.robot.Strategy;
import awu_old_naman.robot.rat.combat.moves.TryAttack;
import awu_old_naman.robot.rat.combat.moves.TryFight;
import awu_old_naman.robot.rat.combat.moves.TryRatNap;
import awu_old_naman.robot.rat.combat.moves.TryThrow;
import awu_old_naman.robot.rat.combat.moves.TryTrap;
import awu_old_naman.util.Profiler;
import awu_old_naman.util.Util;

import static awu_old_naman.robot.rat.BabyRat.*;
import static awu_old_naman.Game.*;
import static awu_old_naman.util.Util.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;




/*

TODO: 
    - add code for detecting enemies out of vision
    - consider avoiding cells within cheb dist of 1/2 of enemy rats
    - prefer cells that walk out of enemy vision
    - strongly prefer looking at enemies
    - offence squeak signals

*/

public class CombatStrategy extends Strategy {
    private static final double INF = 1e9;
    private static final int RAT_TRAP_COST = 30;
    private static final int RAT_TRAP_CAP = 25;

    public CombatStrategy() {
    }

    /*

      the returned score should be a multiplier for any "damage" dealt to it

    */



    // in cheese units
    public double trapCost() {
        return RAT_TRAP_COST; // tf i cant see how many rat traps i have placed?
    }

    public static double[] cellScore; // [movement direction]

    private void precomp() throws GameActionException {
        CombatCommon.precomp();
        CombatTracker.tick();

        StringBuilder sb = new StringBuilder();
        indicate(sb.toString());

        // Profiler.push();
        computeCellScores();
        // Profiler.pop("compute danger");
        allies = Squeaker.squeaksSinceLastTurn;
        enemies = 0;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getType() == UnitType.CAT) continue;
            if (info.getTeam() != rc.getTeam()) {
                enemies++;
                continue;
            }
            if (info.getLocation().distanceSquaredTo(myLoc) > 2) allies++;
        }
    }

    public boolean runTick() throws GameActionException {
        Profiler.push();
        boolean res = _runTick();
        Profiler.pop_ind("combat strategy");
        return res;
    }

    // idek why this is returning. check if can move anyways after to squeeze out some juice
    private boolean _runTick() throws GameActionException {
        if (rc.isBeingCarried()) return false;
        Profiler.push();
        precomp();
        Profiler.pop("precomp", 3500);

        if (rc.getCarrying() != null) {
            return tryThrow();
        }

        Profiler.push();
        boolean tryRatNapRes = tryRatNap();
        Profiler.pop("try ratnap", 1000);
        if (tryRatNapRes) return true;

        Profiler.push();
        boolean tryTrapRes = tryTrap();
        Profiler.pop("try trap", 3000);
        if (tryTrapRes) return true;

        Profiler.push();
        boolean tryAttackRes = tryAttack();
        Profiler.pop("try attack", 1000);
        if (tryAttackRes) return true;

        Profiler.push();
        boolean tryFightRes = tryFight();
        Profiler.pop("try fight", 1000);
        if (tryFightRes) {
            miniTick();
            return true;
        }
        indicate("COMBAT STRAT: NONE");
        // TODO: deal with cats (maybe naman's job?)
        return false;
    }

    public boolean miniTick() throws GameActionException {
        Profiler.push();
        boolean tryRatNapRes = miniRatNap();
        Profiler.pop("mini ratnap", 300);
        if (tryRatNapRes) return true;
        
        Profiler.push();
        boolean tryTrapRes = miniTrap();
        Profiler.pop("mini trap", 300);
        if (tryTrapRes) return true;
        
        boolean tryAttackRes = miniAttack();
        if (tryAttackRes) return true;
        return false;
    }

    public boolean tryFight() throws GameActionException {
        return TryFight.run();
    }


    public boolean tryThrow() throws GameActionException {
        return TryThrow.run();
    }


    // TODO: do danger levels and verify you dont die on the next turn
    public boolean tryRatNap() throws GameActionException {
        return TryRatNap.run();
    }

    public boolean miniRatNap() throws GameActionException {
        return TryRatNap.miniRun();
    }

    public boolean tryTrap() throws GameActionException {
        return TryTrap.run();
    }

    public boolean miniTrap() throws GameActionException {
        return TryTrap.miniRun();
    }

    public boolean tryAttack() throws GameActionException {
        return TryAttack.run();
    }

    public boolean miniAttack() throws GameActionException {
        return TryAttack.miniRun();
    }

    static boolean canPerformMoves(Direction moveDir, Direction facing) throws GameActionException {
        return CombatCommon.canPerformMoves(moveDir, facing);
    }

    public static boolean performMoves(Direction moveDir, Direction facing) throws GameActionException {
        if (moveDir != Direction.CENTER && !CombatCommon.canMove(moveDir)) return false;
        if (moveDir == Direction.CENTER) {
            if (facing != myDir) turn(facing);
            return true;
        }
        if (moveDir == myDir && facing == myDir) {
            rawMove();
            return true; // move
        }
        if (moveDir == myDir) { // move turn
            rawMove();
            if (facing != myDir) turn(facing);
            return true;
        }
        if (moveDir == facing && rc.canTurn()) { // turn move
            turn(moveDir);
            rawMove();
            return true;
        }
        if (canStrafe()) {
            if (facing == myDir) { // strafe
                rawMove(moveDir);
                return true;
            }
            // strafe turn
            rawMove(moveDir);
            turn(facing);
            return true;
        }
        return false;
    }

    private static StringBuilder sb;
    private static double readCellScore(RobotInfo target, Direction moveDir) {
        String key = "=" + target.getID() + "+" + moveDir.ordinal() + "|";
        int i = sb.indexOf(key);
        if (i == -1) return 0;
        else return ((int) sb.charAt(i - 1)) / 100.0;
    }
    private static double cacheCellScore(RobotInfo target, Direction moveDir, double value) {
        String key = ((char) ((int) value * 100)) + "=" + target.getID() + "+" + moveDir.ordinal() + "|";
        sb.append(key);
        return value;
    }

    public static void computeCellScores() throws GameActionException {
        sb = new StringBuilder();
        cellScore = new double[9];
        for (RobotInfo info : rc.senseNearbyRobots(9)) { // shrink radius to optimise
            if (info.getTeam() == rc.getTeam()) continue;
            cellScore[0] += getCellScoreForEnemy(info, Direction.NORTH);
            cellScore[1] += getCellScoreForEnemy(info, Direction.NORTHEAST);
            cellScore[2] += getCellScoreForEnemy(info, Direction.EAST);
            cellScore[3] += getCellScoreForEnemy(info, Direction.SOUTHEAST);
            cellScore[4] += getCellScoreForEnemy(info, Direction.SOUTH);
            cellScore[5] += getCellScoreForEnemy(info, Direction.SOUTHWEST);
            cellScore[6] += getCellScoreForEnemy(info, Direction.WEST);
            cellScore[7] += getCellScoreForEnemy(info, Direction.NORTHWEST);
            cellScore[8] += getCellScoreForEnemy(info, Direction.CENTER);
        }
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing, RobotInfo target) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing, target)) return -100;
        return cellScore[moveDir.ordinal()] - readCellScore(target, moveDir);
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing)) return -100;
        return cellScore[moveDir.ordinal()];
    }

    // TODO: cache this function 
    public static double getCellScoreForEnemy(RobotInfo info, Direction moveDir) {
        MapLocation to = myLoc.add(moveDir);
        if (!rc.onTheMap(to)) return 0;
        if (info.getType() == UnitType.CAT) {
            // TODO: implement vision check
            int dist = chebyshevDistance(to, info.getLocation()); 
            if (dist >= 4) return 0;
            else return cacheCellScore(info, moveDir, -5);
        }
        MapLocation enemyLoc = info.getLocation();
        int dist = chebyshevDistance(to, info.getLocation());
        if (dist > 3) return 0; // ignore
        if (info.getType() == UnitType.RAT_KING) {
            if (dist > 2) return 0; // is this correct?
            else return cacheCellScore(info, moveDir, 1);
        } else {
            double base = 0;
            // TODO: optimise with switch table
            Profiler.begin();
            boolean canCurrentlySense = canSense(enemyLoc, myLoc, info.getDirection());
            boolean canSenseAfterMove = canSense(enemyLoc, to, info.getDirection());
            Profiler.end_print("can see");

            if (canCurrentlySense) {
                if (!canSenseAfterMove) {
                    base += 3; // escaping vision
                } else {
                    base -= 1; // staying in vision
                }
            } else {
                if (canSenseAfterMove) {
                    base -= 3; // entering vision
                } else {
                    base = 0; // they dont see u
                }
            }

            if (canSenseAfterMove) {
                int dangerDistance;
                if (allies > enemies) dangerDistance = 0;
                else if (time % 4 == 0) dangerDistance = 1;
                else dangerDistance = 2;
                if (dist <= dangerDistance) base -= 10;
            }

            if (rc.getHealth() > 1.5 * info.getHealth() && rc.getHealth() > 20) {
                base *= 0.5;
                // weak
            }
            return cacheCellScore(info, moveDir, base);
        }
    }
    /*

      attacking another solo rat is safe as if:
      - you end the turn with at least as much health (no ratnap)
      - they can't see you
      if you are in a horde, it should be safe to run at them

    */
    public static int allies, enemies;
    public static boolean shouldFight(RobotInfo enemy) {
        if (enemy.getType() == UnitType.CAT) return false; // TODO
        if (enemy.getType() == UnitType.RAT_KING) return true;
        if (enemy.getHealth() <= rc.getHealth()) return true;
        if (!canSense(enemy.getLocation(), myLoc, enemy.getDirection())) return true;
        if (GlobalArray.nearestRatKing.distanceSquaredTo(myLoc) <= 36) return true;
        if (allies > enemies) return true;
        return false;
    }

    // currently given arbitrary weights
    public double scoreEnemy(RobotInfo info) {
        if (info.getType() == UnitType.RAT_KING) return 5;
        // one tap this guy
        if (info.getHealth() - GameConstants.RAT_BITE_DAMAGE <= 0) {
            return 3.0;
        }
        if (info.getHealth() <= 30) {
            // try to get rid of low hp rats
            return 1.3;
        }
        if (info.getHealth() >= rc.getHealth()) {
            // try to bring enemy rats into napping distance
            return 1.2;
        }
        return 1;
    }

    public static int getCheeseNeeded(int damageBuff) {
        if (damageBuff == 0) return 0;
        int base = damageBuff - 1;
        return base * base + 1;
    }

    public static int canEnemyReach(RobotInfo info, Direction moveDir) throws GameActionException {
        MapLocation to = myLoc.add(moveDir);
        int dist = chebyshevDistance(to, info.getLocation());
        if (dist > 2) return 0;
        if (!isCellMaybePassable(to)) return 0;;
        return 1;
    }

    public static RobotInfo getNearestTarget() {
        RobotInfo best = null;
        int bestDist = Integer.MAX_VALUE;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (!shouldFight(info)) continue;
            int dist = chebyshevDistance(myLoc, info.getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                best = info;
            }
        }
        return best;
    }   
}