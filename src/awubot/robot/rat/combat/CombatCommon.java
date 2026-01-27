

package awubot.robot.rat.combat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Profiler;
import awubot.RobotTracker;
import awubot.comms.Enemy;
import awubot.comms.EnemyManager;

public final class CombatCommon {
    private CombatCommon() {
    }


    public static boolean canMoveN;
    public static boolean canMoveNE;
    public static boolean canMoveE;
    public static boolean canMoveSE;
    public static boolean canMoveS;
    public static boolean canMoveSW;
    public static boolean canMoveW;
    public static boolean canMoveNW;
    public static boolean canMoveC;
    public static int[] minDistToEnemy = new int[9];
    public static double[] totDistToEnemy = new double[9];

    public static void precomp() throws GameActionException {
        canMoveN = rc.canMove(Direction.NORTH);
        canMoveNE = rc.canMove(Direction.NORTHEAST);
        canMoveE = rc.canMove(Direction.EAST);
        canMoveSE = rc.canMove(Direction.SOUTHEAST);
        canMoveS = rc.canMove(Direction.SOUTH);
        canMoveSW = rc.canMove(Direction.SOUTHWEST);
        canMoveW = rc.canMove(Direction.WEST);
        canMoveNW = rc.canMove(Direction.NORTHWEST);
        canMoveC = true;

        minDistToEnemy[0] = 999;
        totDistToEnemy[0] = 0;
        minDistToEnemy[1] = 999;
        totDistToEnemy[1] = 0;
        minDistToEnemy[2] = 999;
        totDistToEnemy[2] = 0;
        minDistToEnemy[3] = 999;
        totDistToEnemy[3] = 0;
        minDistToEnemy[4] = 999;
        totDistToEnemy[4] = 0;
        minDistToEnemy[5] = 999;
        totDistToEnemy[5] = 0;
        minDistToEnemy[6] = 999;
        totDistToEnemy[6] = 0;
        minDistToEnemy[7] = 999;
        totDistToEnemy[7] = 0;
        minDistToEnemy[8] = 999;
        totDistToEnemy[8] = 0;

        MapLocation toN = myLoc.add(Direction.NORTH);
        MapLocation toNE = myLoc.add(Direction.NORTHEAST);
        MapLocation toE = myLoc.add(Direction.EAST);
        MapLocation toSE = myLoc.add(Direction.SOUTHEAST);
        MapLocation toS = myLoc.add(Direction.SOUTH);
        MapLocation toSW = myLoc.add(Direction.SOUTHWEST);
        MapLocation toW = myLoc.add(Direction.WEST);
        MapLocation toNW = myLoc.add(Direction.NORTHWEST);
        MapLocation toC = myLoc;

        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo enemy = RobotTracker.sortedEnemies[i];
            {
                int dist = chebyshevDistance(toN, enemy.location);
                totDistToEnemy[0] += dist;
                if (dist < minDistToEnemy[0]) minDistToEnemy[0] = dist;
            }
            {
                int dist = chebyshevDistance(toNE, enemy.location);
                totDistToEnemy[1] += dist;
                if (dist < minDistToEnemy[1]) minDistToEnemy[1] = dist;
            }
            {
                int dist = chebyshevDistance(toE, enemy.location);
                totDistToEnemy[2] += dist;
                if (dist < minDistToEnemy[2]) minDistToEnemy[2] = dist;
            }
            {
                int dist = chebyshevDistance(toSE, enemy.location);
                totDistToEnemy[3] += dist;
                if (dist < minDistToEnemy[3]) minDistToEnemy[3] = dist;
            }
            {
                int dist = chebyshevDistance(toS, enemy.location);
                totDistToEnemy[4] += dist;
                if (dist < minDistToEnemy[4]) minDistToEnemy[4] = dist;
            }
            {
                int dist = chebyshevDistance(toSW, enemy.location);
                totDistToEnemy[5] += dist;
                if (dist < minDistToEnemy[5]) minDistToEnemy[5] = dist;
            }
            {
                int dist = chebyshevDistance(toW, enemy.location);
                totDistToEnemy[6] += dist;
                if (dist < minDistToEnemy[6]) minDistToEnemy[6] = dist;
            }
            {
                int dist = chebyshevDistance(toNW, enemy.location);
                totDistToEnemy[7] += dist;
                if (dist < minDistToEnemy[7]) minDistToEnemy[7] = dist;
            }
            {
                int dist = chebyshevDistance(toC, enemy.location);
                totDistToEnemy[8] += dist;
                if (dist < minDistToEnemy[8]) minDistToEnemy[8] = dist;
            }
        }

        for (int i = 0; i < EnemyManager.numKnownEnemies; i++) {
            Enemy enemy = EnemyManager.knownEnemies[i];
            {
                int dist = chebyshevDistance(toN, enemy.location);
                totDistToEnemy[0] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toNE, enemy.location);
                totDistToEnemy[1] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toE, enemy.location);
                totDistToEnemy[2] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toSE, enemy.location);
                totDistToEnemy[3] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toS, enemy.location);
                totDistToEnemy[4] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toSW, enemy.location);
                totDistToEnemy[5] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toW, enemy.location);
                totDistToEnemy[6] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toNW, enemy.location);
                totDistToEnemy[7] += dist * 0.33333334;
            }
            {
                int dist = chebyshevDistance(toC, enemy.location);
                totDistToEnemy[8] += dist * 0.33333334;
            }
        }
    }

    public static boolean canMove(Direction dir) {
        // if (dir == null) return false;
        return switch (dir.ordinal()) {
            case 0 -> canMoveN;
            case 1 -> canMoveNE;
            case 2 -> canMoveE;
            case 3 -> canMoveSE;
            case 4 -> canMoveS;
            case 5 -> canMoveSW;
            case 6 -> canMoveW;
            case 7 -> canMoveNW;
            case 8 -> canMoveC;
            default -> false;
        };
    }
    
    // TODO: tune
    public static int strafeCost() {
        return (rc.getMovementCooldownTurns() < 2 ? 3 : 8);
    }


    // -1 = impossible, 0 = strafe, 1 = no strafe
    public static int movementCost(Direction moveDir, Direction facing) {
        if (moveDir == Direction.CENTER) {
            if (facing == myDir || rc.canTurn()) return 1;
            else return -1;
        }
        if (!rc.isMovementReady() || !canMove(moveDir)) return -1;
        if (facing != myDir && !rc.canTurn()) return -1;
        if (moveDir == myDir && myDir == facing) return 1; 
        if ((moveDir == myDir || moveDir == facing)) return 1;
        return 0;
    }
}