

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
import awubot.comms.Enemy;
import awubot.comms.EnemyManager;
import awubot.robot.Strategy;
import awubot.robot.rat.combat.moves.TryFight;
import awubot.robot.rat.combat.moves.TryAttack;
import awubot.robot.rat.combat.moves.TryRatNap;
import awubot.robot.rat.combat.moves.TryPosition;
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

    static int precompStart, precompBudget;
    private void precomp() throws GameActionException {
        precompStart = Clock.getBytecodeNum();
        precompBudget = 5000;
        // negligable bytecode
        CombatCommon.precomp();

        // expensive
        Profiler.push();
        try {
            CombatTracker.tick();
        } finally {
            Profiler.pop("combat tracker");
        }


        Profiler.push();
        try {
            computeCellScores();
        } finally {
            Profiler.pop("compute cell scores");
        }
    }

    public boolean runTick() throws GameActionException {
        Profiler.push();
        try {
            return _runTick();
        } finally {
            Profiler.pop("combat strategy");
        }
    }

    public static int moveMask, sourceCnt;
    public MapLocation[] sources = new MapLocation[30];
    public static Enemy funnyTarget;

    // idek why this is returning. check if can move anyways after to squeeze out some juice
    private boolean _runTick() throws GameActionException {
        if (rc.isBeingCarried() || rc.isBeingThrown()) return false;
        // if (rc.getRawCheese() >= 50 && myLoc.distanceSquaredto(GlobalArray.nearestRatKing) <= 36) return false;
        // println("-- COMBAT STRAT TICK --- starting bytecode count " + Clock.getBytecodeNum() + "\n");

        funnyTarget = null;
        if (rc.getCarrying() == null) {
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
            // for (int i = 0; i < sourceCnt; i++) {
            //     indicate("combat source prep: " + sources[i]);
            // }
            int[] bfsResult = Pathfinder.multisourceBfs(sources, sourceCnt);
            moveMask = bfsResult[0];    
            int bfsIters = bfsResult[1];
            // println("BFS iters: " + bfsIters);
            // Profiler.pop("combat bfs");
            indicate("MOVEMASK: " + Integer.toBinaryString(moveMask));
            if (moveMask == 0) return false; // no enemies nearby
            if (funnyTarget != null && bfsIters <= 5) {
                println("AGGROED ON FUNNY TARGET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + funnyTarget.location);
                indicate("funny pos: " + funnyTarget.location);
                turn(myLoc.directionTo(funnyTarget.location));
            }
        }

        Profiler.push();
        try {
            precomp();
        } finally {
            Profiler.pop("precomp");
        }

        isLastMove = true;
        if (rc.getCarrying() != null) {
            Profiler.push();
            try {
                boolean tryThrowRes = TryThrow.run();
                if (tryThrowRes) return true;
            } finally {
                Profiler.pop("try throw", 4000);
            }
            Profiler.push();
            try {
                boolean tryPositionRes = TryPosition.run();
                if (tryPositionRes) return true;
            } finally {
                Profiler.pop("try position", 3000);
            }
            return true; // bruh
        }

        Profiler.push();
        try {
            boolean tryRatNapRes = TryRatNap.run();
            if (tryRatNapRes) return true;
        } finally {
            Profiler.pop("try ratnap", 5000);
        }

        Profiler.push();
        try {
            boolean tryTrapRes = TryTrap.run();
            if (tryTrapRes) return true;
        } finally {
            Profiler.pop("try trap", 5000);
        }

        Profiler.push();
        try {
            boolean tryAttackRes = TryAttack.run();
            if (tryAttackRes) return true;
        } finally {
            Profiler.pop("try attack", 5000);
        }

        Profiler.push();
        boolean tryFightRes;
        try {
            tryFightRes = TryFight.run();
        } finally {
            Profiler.pop("try fight");
        }
        if (tryFightRes) {
            Profiler.push();
            try {
                miniTick();
            } finally {
                Profiler.pop("mini tick", 500);
            }
            return true;
        }
        indicate("COMBAT STRAT: NONE");
        if (RobotTracker.numEnemies > 0) {
            println("ROBOT DIDNT MAKE A MOVE");
        }
        // TODO: deal with cats (maybe naman's job?)
        // return false;
        return false;
    }

    public boolean miniTick() throws GameActionException {
        // Profiler.push();
        boolean tryRatNapRes = TryRatNap.miniRun();
        // Profiler.pop("mini ratnap");
        if (tryRatNapRes) return true;

        boolean tryAttackRes = TryAttack.miniRun();
        if (tryAttackRes) return true;

        // Profiler.push();
        boolean tryTrapRes = TryTrap.miniRun();
        // Profiler.pop("mini trap");
        if (tryTrapRes) return true;
        return false;
    }

    public static boolean performMoves(Direction moveDir, Direction facing) throws GameActionException {
        if (moveDir != Direction.CENTER && !CombatCommon.canMove(moveDir)) return false;
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
        sb = new StringBuilder();
        cellScore = new double[9];
        if (1 == 1) return;
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
        for (int i = 0; i < RobotTracker.numEnemies && i < 3; i++) {
            RobotInfo info = RobotTracker.sortedEnemies[i];
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[0] += getCellScoreForEnemy(info, Direction.NORTH);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[1] += getCellScoreForEnemy(info, Direction.NORTHEAST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[2] += getCellScoreForEnemy(info, Direction.EAST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[3] += getCellScoreForEnemy(info, Direction.SOUTHEAST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[4] += getCellScoreForEnemy(info, Direction.SOUTH);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[5] += getCellScoreForEnemy(info, Direction.SOUTHWEST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[6] += getCellScoreForEnemy(info, Direction.WEST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[7] += getCellScoreForEnemy(info, Direction.NORTHWEST);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
            cellScore[8] += getCellScoreForEnemy(info, Direction.CENTER);
            if (Clock.getBytecodeNum() - precompStart > precompBudget) break;
        }

        StringBuilder indicateSb = new StringBuilder("Cell scores:\n");
        for (int dy = 1; dy >= -1; dy--) {
            for (int dx = -1; dx <= 1; dx++) {
                indicateSb.append(String.format("%5.2f ", cellScore[Util.dirFromDelta(dx, dy).ordinal()]));
            }
            indicateSb.append("\n");
        }
        indicate(indicateSb.toString());
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing, RobotInfo target, int damage) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing, target, damage)) return -200;
        return 0;
        // return cellScore[moveDir.ordinal()] - readCellScore(target, moveDir);
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing, RobotInfo target) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing, target)) return -200;
        return 0;
        // return cellScore[moveDir.ordinal()] - readCellScore(target, moveDir);
    }

    public static double getCellScoreFor(Direction moveDir, Direction facing) throws GameActionException {
        if (CombatTracker.hasThreat(moveDir, facing)) return -200;
        return 0;
        // return cellScore[moveDir.ordinal()];
    }

    // TODO: cache this function
    public static double getCellScoreForEnemy(RobotInfo info, Direction moveDir) {
        MapLocation to = myLoc.add(moveDir);
        if (!rc.onTheMap(to)) return 0;
        int dist = chebyshevDistance(to, info);
        if (info.getType() == UnitType.CAT) {
            // TODO: implement vision check
            if (dist >= 4 || shouldFight(info)) return 0;
            else return cacheCellScore(info, moveDir, -100);
        }
        if (dist > 3) return 0;
        MapLocation enemyLoc = info.location;
        if (info.getType() == UnitType.RAT_KING) {
            if (dist > 2) return 0; // is this correct?
            else return cacheCellScore(info, moveDir, 1);
        } else {
            double base = 0;
            Direction enemyDir = info.direction;
            int dx = myLocX - enemyLoc.x;
            int dy = myLocY - enemyLoc.y;
            boolean canCurrentlySense = canSense(dx, dy, enemyDir);
            dx += moveDir.dx;
            dy += moveDir.dy;
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
                // else if (time % 4 == 0) dangerDistance = 1;
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

    public static int getMaxBiteDamage() {
        if (rc.getAllCheese() < 300 || (rc.getRawCheese() % 20 < 2 && rc.getRawCheese() > 0)) return 10;
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
        if (enemy.getHealth() - getMaxBiteDamage() <= rc.getHealth()) return true;
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

    private static int maxBite;
    public static int getDamageFor(RobotInfo info) {
        if (info.getType() != UnitType.BABY_RAT) return 10;
        maxBite = getMaxBiteDamage();
        if (maxBite == 10) return 10;
        switch (rc.getHealth()) {
            case 1 -> { return getDamageFor1(info.getHealth()); }
            case 2 -> { return getDamageFor2(info.getHealth()); }
            case 3 -> { return getDamageFor3(info.getHealth()); }
            case 4 -> { return getDamageFor4(info.getHealth()); }
            case 5 -> { return getDamageFor5(info.getHealth()); }
            case 6 -> { return getDamageFor6(info.getHealth()); }
            case 7 -> { return getDamageFor7(info.getHealth()); }
            case 8 -> { return getDamageFor8(info.getHealth()); }
            case 9 -> { return getDamageFor9(info.getHealth()); }
            case 10 -> { return getDamageFor10(info.getHealth()); }
            case 11 -> { return getDamageFor11(info.getHealth()); }
            case 12 -> { return getDamageFor12(info.getHealth()); }
            case 13 -> { return getDamageFor13(info.getHealth()); }
            case 14 -> { return getDamageFor14(info.getHealth()); }
            case 15 -> { return getDamageFor15(info.getHealth()); }
            case 16 -> { return getDamageFor16(info.getHealth()); }
            case 17 -> { return getDamageFor17(info.getHealth()); }
            case 18 -> { return getDamageFor18(info.getHealth()); }
            case 19 -> { return getDamageFor19(info.getHealth()); }
            case 20 -> { return getDamageFor20(info.getHealth()); }
            case 21 -> { return getDamageFor21(info.getHealth()); }
            case 22 -> { return getDamageFor22(info.getHealth()); }
            case 23 -> { return getDamageFor23(info.getHealth()); }
            case 24 -> { return getDamageFor24(info.getHealth()); }
            case 25 -> { return getDamageFor25(info.getHealth()); }
            case 26 -> { return getDamageFor26(info.getHealth()); }
            case 27 -> { return getDamageFor27(info.getHealth()); }
            case 28 -> { return getDamageFor28(info.getHealth()); }
            case 29 -> { return getDamageFor29(info.getHealth()); }
            case 30 -> { return getDamageFor30(info.getHealth()); }
            case 31 -> { return getDamageFor31(info.getHealth()); }
            case 32 -> { return getDamageFor32(info.getHealth()); }
            case 33 -> { return getDamageFor33(info.getHealth()); }
            case 34 -> { return getDamageFor34(info.getHealth()); }
            case 35 -> { return getDamageFor35(info.getHealth()); }
            case 36 -> { return getDamageFor36(info.getHealth()); }
            case 37 -> { return getDamageFor37(info.getHealth()); }
            case 38 -> { return getDamageFor38(info.getHealth()); }
            case 39 -> { return getDamageFor39(info.getHealth()); }
            case 40 -> { return getDamageFor40(info.getHealth()); }
            case 41 -> { return getDamageFor41(info.getHealth()); }
            case 42 -> { return getDamageFor42(info.getHealth()); }
            case 43 -> { return getDamageFor43(info.getHealth()); }
            case 44 -> { return getDamageFor44(info.getHealth()); }
            case 45 -> { return getDamageFor45(info.getHealth()); }
            case 46 -> { return getDamageFor46(info.getHealth()); }
            case 47 -> { return getDamageFor47(info.getHealth()); }
            case 48 -> { return getDamageFor48(info.getHealth()); }
            case 49 -> { return getDamageFor49(info.getHealth()); }
            case 50 -> { return getDamageFor50(info.getHealth()); }
            case 51 -> { return getDamageFor51(info.getHealth()); }
            case 52 -> { return getDamageFor52(info.getHealth()); }
            case 53 -> { return getDamageFor53(info.getHealth()); }
            case 54 -> { return getDamageFor54(info.getHealth()); }
            case 55 -> { return getDamageFor55(info.getHealth()); }
            case 56 -> { return getDamageFor56(info.getHealth()); }
            case 57 -> { return getDamageFor57(info.getHealth()); }
            case 58 -> { return getDamageFor58(info.getHealth()); }
            case 59 -> { return getDamageFor59(info.getHealth()); }
            case 60 -> { return getDamageFor60(info.getHealth()); }
            case 61 -> { return getDamageFor61(info.getHealth()); }
            case 62 -> { return getDamageFor62(info.getHealth()); }
            case 63 -> { return getDamageFor63(info.getHealth()); }
            case 64 -> { return getDamageFor64(info.getHealth()); }
            case 65 -> { return getDamageFor65(info.getHealth()); }
            case 66 -> { return getDamageFor66(info.getHealth()); }
            case 67 -> { return getDamageFor67(info.getHealth()); }
            case 68 -> { return getDamageFor68(info.getHealth()); }
            case 69 -> { return getDamageFor69(info.getHealth()); }
            case 70 -> { return getDamageFor70(info.getHealth()); }
            case 71 -> { return getDamageFor71(info.getHealth()); }
            case 72 -> { return getDamageFor72(info.getHealth()); }
            case 73 -> { return getDamageFor73(info.getHealth()); }
            case 74 -> { return getDamageFor74(info.getHealth()); }
            case 75 -> { return getDamageFor75(info.getHealth()); }
            case 76 -> { return getDamageFor76(info.getHealth()); }
            case 77 -> { return getDamageFor77(info.getHealth()); }
            case 78 -> { return getDamageFor78(info.getHealth()); }
            case 79 -> { return getDamageFor79(info.getHealth()); }
            case 80 -> { return getDamageFor80(info.getHealth()); }
            case 81 -> { return getDamageFor81(info.getHealth()); }
            case 82 -> { return getDamageFor82(info.getHealth()); }
            case 83 -> { return getDamageFor83(info.getHealth()); }
            case 84 -> { return getDamageFor84(info.getHealth()); }
            case 85 -> { return getDamageFor85(info.getHealth()); }
            case 86 -> { return getDamageFor86(info.getHealth()); }
            case 87 -> { return getDamageFor87(info.getHealth()); }
            case 88 -> { return getDamageFor88(info.getHealth()); }
            case 89 -> { return getDamageFor89(info.getHealth()); }
            case 90 -> { return getDamageFor90(info.getHealth()); }
            case 91 -> { return getDamageFor91(info.getHealth()); }
            case 92 -> { return getDamageFor92(info.getHealth()); }
            case 93 -> { return getDamageFor93(info.getHealth()); }
            case 94 -> { return getDamageFor94(info.getHealth()); }
            case 95 -> { return getDamageFor95(info.getHealth()); }
            case 96 -> { return getDamageFor96(info.getHealth()); }
            case 97 -> { return getDamageFor97(info.getHealth()); }
            case 98 -> { return getDamageFor98(info.getHealth()); }
            case 99 -> { return getDamageFor99(info.getHealth()); }
            case 100 -> { return getDamageFor100(info.getHealth()); }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }

    private static int getDamageFor1(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor2(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor3(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor4(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor5(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor6(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor7(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor8(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor9(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor10(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor11(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor12(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor13(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor14(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor15(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor16(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor17(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor18(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor19(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor20(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor21(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor22(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor23(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor24(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor25(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 12;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor26(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 12;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor27(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 12;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor28(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 12;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor29(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 12;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor30(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 12;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor31(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 12;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor32(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 12;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor33(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 12;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor34(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 12;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor35(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 12;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor36(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 12;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor37(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 12;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor38(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 12;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor39(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 12;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor40(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 12;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor41(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 12;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor42(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 12;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor43(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 12;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor44(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 12;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor45(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 12;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor46(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 12;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor47(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 12;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor48(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 12;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor49(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 12;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor50(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 12;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor51(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 12;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor52(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 12;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor53(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 12;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor54(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 12;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor55(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 12;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor56(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 12;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor57(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 12;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor58(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 12;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor59(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 12;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor60(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 12;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor61(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 12;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor62(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 12;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor63(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 12;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor64(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 12;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor65(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 12;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor66(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 12;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor67(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 12;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor68(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 12;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor69(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 12;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor70(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 12;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor71(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 12;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor72(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 12;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor73(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 12;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor74(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 12;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor75(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 12;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 10;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor76(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 12;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 10;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor77(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 12;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 10;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor78(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 12;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 10;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor79(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 12;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 10;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor80(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 12;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 10;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor81(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 12;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 10;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor82(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 12;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 10;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor83(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 12;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 10;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor84(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 12;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 10;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor85(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 12;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 10;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor86(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 12;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 10;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor87(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 12;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 10;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor88(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 12;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor89(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 12;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor90(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 12;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor91(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 12;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor92(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 12;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor93(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 12;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor94(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 12;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor95(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 12;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor96(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 12;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor97(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 12;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor98(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 12;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor99(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 12;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
    private static int getDamageFor100(int enemyHealth) {
        switch (enemyHealth) {
            case 1 -> {
 return 1;
            }
            case 2 -> {
 return 2;
            }
            case 3 -> {
 return 3;
            }
            case 4 -> {
 return 4;
            }
            case 5 -> {
 return 5;
            }
            case 6 -> {
 return 6;
            }
            case 7 -> {
 return 7;
            }
            case 8 -> {
 return 8;
            }
            case 9 -> {
 return 9;
            }
            case 10 -> {
 return 10;
            }
            case 11 -> {
 return 11;
            }
            case 12 -> {
 return 12;
            }
            case 13 -> {
 return 10;
            }
            case 14 -> {
 return 10;
            }
            case 15 -> {
 return 10;
            }
            case 16 -> {
 return 10;
            }
            case 17 -> {
 return 10;
            }
            case 18 -> {
 return 10;
            }
            case 19 -> {
 return 10;
            }
            case 20 -> {
 return 10;
            }
            case 21 -> {
 return 10;
            }
            case 22 -> {
 return 10;
            }
            case 23 -> {
 return 10;
            }
            case 24 -> {
 return 10;
            }
            case 25 -> {
 return 10;
            }
            case 26 -> {
 return 10;
            }
            case 27 -> {
 return 10;
            }
            case 28 -> {
 return 10;
            }
            case 29 -> {
 return 10;
            }
            case 30 -> {
 return 10;
            }
            case 31 -> {
 return 10;
            }
            case 32 -> {
 return 10;
            }
            case 33 -> {
 return 10;
            }
            case 34 -> {
 return 10;
            }
            case 35 -> {
 return 10;
            }
            case 36 -> {
 return 10;
            }
            case 37 -> {
 return 10;
            }
            case 38 -> {
 return 10;
            }
            case 39 -> {
 return 10;
            }
            case 40 -> {
 return 10;
            }
            case 41 -> {
 return 10;
            }
            case 42 -> {
 return 10;
            }
            case 43 -> {
 return 10;
            }
            case 44 -> {
 return 10;
            }
            case 45 -> {
 return 10;
            }
            case 46 -> {
 return 10;
            }
            case 47 -> {
 return 10;
            }
            case 48 -> {
 return 10;
            }
            case 49 -> {
 return 10;
            }
            case 50 -> {
 return 10;
            }
            case 51 -> {
 return 10;
            }
            case 52 -> {
 return 10;
            }
            case 53 -> {
 return 10;
            }
            case 54 -> {
 return 10;
            }
            case 55 -> {
 return 10;
            }
            case 56 -> {
 return 10;
            }
            case 57 -> {
 return 10;
            }
            case 58 -> {
 return 10;
            }
            case 59 -> {
 return 10;
            }
            case 60 -> {
 return 10;
            }
            case 61 -> {
 return 10;
            }
            case 62 -> {
 return 10;
            }
            case 63 -> {
 return 10;
            }
            case 64 -> {
 return 10;
            }
            case 65 -> {
 return 10;
            }
            case 66 -> {
 return 10;
            }
            case 67 -> {
 return 10;
            }
            case 68 -> {
 return 10;
            }
            case 69 -> {
 return 10;
            }
            case 70 -> {
 return 10;
            }
            case 71 -> {
 return 10;
            }
            case 72 -> {
 return 10;
            }
            case 73 -> {
 return 10;
            }
            case 74 -> {
 return 10;
            }
            case 75 -> {
 return 10;
            }
            case 76 -> {
 return 10;
            }
            case 77 -> {
 return 10;
            }
            case 78 -> {
 return 10;
            }
            case 79 -> {
 return 10;
            }
            case 80 -> {
 return 10;
            }
            case 81 -> {
 return 10;
            }
            case 82 -> {
 return 10;
            }
            case 83 -> {
 return 10;
            }
            case 84 -> {
 return 10;
            }
            case 85 -> {
 return 10;
            }
            case 86 -> {
 return 10;
            }
            case 87 -> {
 return 10;
            }
            case 88 -> {
 return 12;
            }
            case 89 -> {
 return 12;
            }
            case 90 -> {
 return 12;
            }
            case 91 -> {
 return 12;
            }
            case 92 -> {
 return 12;
            }
            case 93 -> {
 return 12;
            }
            case 94 -> {
 return 12;
            }
            case 95 -> {
 return 12;
            }
            case 96 -> {
 return 12;
            }
            case 97 -> {
 return 12;
            }
            case 98 -> {
 return 12;
            }
            case 99 -> {
 return 12;
            }
            case 100 -> {
 return 12;
            }
            default -> { throw new RuntimeException("unreachable"); }
        }
    }
}