package awubot.robot.rat.combat;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;
import awubot.CellTracker;
import awubot.Game;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;
import awubot.robot.Strategy;
import awubot.robot.rat.combat.moves.TryAttack;
import awubot.robot.rat.combat.moves.TryFight;
import awubot.robot.rat.combat.moves.TryRatNap;
import awubot.robot.rat.combat.moves.TryThrow;
import awubot.robot.rat.combat.moves.TryTrap;
import awubot.util.Profiler;
import awubot.util.Util;

import static awubot.robot.rat.BabyRat.*;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.RobotTracker;

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
    public static boolean ENABLE_PROFILE = true;


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
        // negligable bytecode
        CombatCommon.precomp();

        // expensive
        Profiler.push();
        CombatTracker.tick();
        Profiler.pop("combat tracker");


        Profiler.push();
        computeCellScores();
        Profiler.pop("compute cell scores");
    }

    public boolean runTick() throws GameActionException {
        Profiler.push();
        boolean res = _runTick();
        Profiler.pop_ind("combat strategy");
        return res;
    }

    public static int moveMask, sourceCnt;
    public MapLocation[] sources = new MapLocation[9];

    // idek why this is returning. check if can move anyways after to squeeze out some juice
    private boolean _runTick() throws GameActionException {
        if (rc.isBeingCarried() || rc.isBeingThrown()) return false;

        if (rc.getCarrying() == null) {
            Profiler.push();
            sourceCnt = 0;
            for (int i = 0; i < RobotTracker.numEnemies; i++) {
                if (shouldFight(RobotTracker.sortedEnemies[i])) {
                    sources[sourceCnt++] = RobotTracker.sortedEnemies[i].location;
                }
            }
            for (int i = 0; i < RobotTracker.numCats; i++) {
                if (shouldFight(RobotTracker.sortedCats[i])) {
                    sources[sourceCnt++] = RobotTracker.sortedCats[i].location;
                }
            }
            for (int i = 0; i < sourceCnt; i++) {
                indicate("combat source prep: " + sources[i]);
            }
            moveMask = Pathfinder.multisourceBfs(sources, sourceCnt);
            Profiler.pop("combat bfs");
            indicate("MOVEMASK: " + Integer.toBinaryString(moveMask));
            if (moveMask == 0) return false; // no enemies nearby
        }

        Profiler.push();
        precomp();
        Profiler.pop("precomp", 3500);

        if (rc.getCarrying() != null) {
            Profiler.push();
            boolean tryThrowRes = TryThrow.run();
            Profiler.pop("try throw", 1000);
            if (tryThrowRes) return true;
            return false;
        }

        Profiler.push();
        boolean tryRatNapRes = TryRatNap.run();
        Profiler.pop("try ratnap", 1000);
        if (tryRatNapRes) return true;

        Profiler.push();
        boolean tryAttackRes = TryAttack.run();
        Profiler.pop("try attack", 1000);
        if (tryAttackRes) return true;

        Profiler.push();
        boolean tryTrapRes = TryTrap.run();
        Profiler.pop("try trap", 3000);
        if (tryTrapRes) return true;

        Profiler.push();
        boolean tryFightRes = TryFight.run();
        Profiler.pop("try fight", 1000);
        if (tryFightRes) {
            miniTick();
            return true;
        }
        indicate("COMBAT STRAT: NONE");
        if (RobotTracker.numEnemies > 0) {
            println("ROBOT DIDNT MAKE A MOVE");
        }
        // TODO: deal with cats (maybe naman's job?)
        return false;
    }

    public boolean miniTick() throws GameActionException {
        Profiler.push();
        boolean tryRatNapRes = TryRatNap.miniRun();
        Profiler.pop("mini ratnap", 300);
        if (tryRatNapRes) return true;

        boolean tryAttackRes = TryAttack.miniRun();
        if (tryAttackRes) return true;

        Profiler.push();
        boolean tryTrapRes = TryTrap.miniRun();
        Profiler.pop("mini trap", 300);
        if (tryTrapRes) return true;
        return false;
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
        else return (((int) sb.charAt(i - 1)) - 32000) / 100.0;
    }
    private static double cacheCellScore(RobotInfo target, Direction moveDir, double value) {
        String key = ((char) ((int) value * 100 + 32000)) + "=" + target.getID() + "+" + moveDir.ordinal() + "|";
        sb.append(key);
        return value;
    }

    public static void computeCellScores() throws GameActionException {
        Profiler.push();
        sb = new StringBuilder();
        cellScore = new double[9];
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            RobotInfo info = RobotTracker.sortedEnemies[i];
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
        for (int i = 0; i < RobotTracker.numCats && i < 3; i++) {
            RobotInfo info = RobotTracker.sortedCats[i];
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
        Profiler.pop("compute cell scores", 1000);
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
        int dist = chebyshevDistance(to, info.location);
        if (info.getType() == UnitType.CAT) {
            // TODO: implement vision check
            if (dist >= 4 || shouldFight(info)) return 0;
            else return cacheCellScore(info, moveDir, -40);
        }
        MapLocation enemyLoc = info.location;
        if (dist > 3) return 0; // ignore
        if (info.getType() == UnitType.RAT_KING) {
            if (dist > 2) return 0; // is this correct?
            else return cacheCellScore(info, moveDir, 1);
        } else {
            double base = 0;
            Direction enemyDir = info.direction;
            int dx = enemyLoc.x - myLocX;
            int dy = enemyLoc.y - myLocY;
            boolean canCurrentlySense = canSense(dx, dy, enemyDir);
            dx -= moveDir.dx;
            dy -= moveDir.dy;
            boolean canSenseAfterMove = canSense(dx, dy, enemyDir);

            if (canCurrentlySense) {
                if (!canSenseAfterMove) {
                    base += 4; // escaping vision
                } else {
                    base -= 1; // staying in vision
                }
            } else {
                if (canSenseAfterMove) {
                    base -= 4; // entering vision
                } else {
                    base = 0; // they dont see u
                }
            }

            if (canSenseAfterMove) {
                int dangerDistance;
                if (RobotTracker.numAllies > RobotTracker.numEnemies) dangerDistance = 0;
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
        public static boolean shouldFight(RobotInfo enemy) {
            if (enemy.getType() == UnitType.CAT) {
                return time > 1000 && rc.getAllCheese() > 300 && RobotTracker.numEnemies == 0 && rc.getHealth() > 50 && rc.getRawCheese() == 0;
            }
            if (enemy.getType() == UnitType.RAT_KING) return true;
            if (enemy.getHealth() <= rc.getHealth()) return true;
            if (!canSense(enemy.location, myLoc, enemy.direction)) return true;
            if (GlobalArray.nearestRatKing != null && GlobalArray.nearestRatKing.distanceSquaredTo(myLoc) <= 36) return true;
            if (RobotTracker.numAllies > RobotTracker.numEnemies) return true;
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
            int dist = chebyshevDistance(to, info.location);
            if (dist > 2) return 0;
            if (!isCellMaybePassable(to)) return 0;;
            return 1;
        }

        public static RobotInfo getNearestTarget() {
            for (int i = 0; i < RobotTracker.numEnemies; i++) {
                RobotInfo info = RobotTracker.sortedEnemies[i];
                if (info.getTeam() == rc.getTeam()) continue;
                if (shouldFight(info)) return info;
            }
            for  (int i = 0; i < RobotTracker.numCats; i++) {
                RobotInfo info = RobotTracker.sortedCats[i];
                if (shouldFight(info)) return info;
            }
            return null;
        }
    }