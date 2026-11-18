package opp_goobie;

import battlecode.common.*;

public class RatKingMicroManager {
    static RobotController rc;
    static int myRange;
    static boolean alwaysInRange = false;
    static MicroInfo[] microInfo;
    static Direction[] dirs = Direction.values();
    static int rangeExtended = 36;

    static void init() {
        rc = Robot.rc;
        myRange = GameConstants.RAT_KING_ATTACK_DISTANCE_SQUARED;
    }

    static boolean doMicro() throws GameActionException {
        if (!rc.isMovementReady())
            return false;

        RobotInfo[] robots = rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.opponent);
        if (robots.length == 0)
            return false;

        MicroInfo bestMicro = microInfo[8];

        for (int i = 8; i >= 0; --i) {
            if (microInfo[i].isBetter(bestMicro))
                bestMicro = microInfo[i];
        }

        if (bestMicro.dir == Direction.CENTER)
            return true;

        if (rc.canMove(bestMicro.dir)) {
            rc.move(bestMicro.dir);
            return true;
        }

        return false;
    }

    static void computeMicroArray() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.opponent);

        microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++)
            microInfo[i] = new MicroInfo(dirs[i]);

        for (RobotInfo r : robots) {
            microInfo[0].updateEnemy(r);
            microInfo[1].updateEnemy(r);
            microInfo[2].updateEnemy(r);
            microInfo[3].updateEnemy(r);
            microInfo[4].updateEnemy(r);
            microInfo[5].updateEnemy(r);
            microInfo[6].updateEnemy(r);
            microInfo[7].updateEnemy(r);
            microInfo[8].updateEnemy(r);
        }

        if (robots.length == 0)
            return;

        robots = rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.team);
        for (RobotInfo r : robots) {
            microInfo[0].updateAlly(r);
            microInfo[1].updateAlly(r);
            microInfo[2].updateAlly(r);
            microInfo[3].updateAlly(r);
            microInfo[4].updateAlly(r);
            microInfo[5].updateAlly(r);
            microInfo[6].updateAlly(r);
            microInfo[7].updateAlly(r);
            microInfo[8].updateAlly(r);
        }
    }

    static class MicroInfo {
        Direction dir;
        MapLocation loc;
        int minDistToEnemy = Constants.INF;
        int enemiesInRange = 0;
        int enemiesInMoveRange = 0;
        int alliesDist2 = 1;
        int alliesDist5 = 1;

        boolean canMove = true;

        MicroInfo(Direction dir) {
            this.dir = dir;
            this.loc = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir))
                canMove = false;
        }

        void updateEnemy(RobotInfo r) {
            int dist = r.getLocation().distanceSquaredTo(loc);
            if (dist < minDistToEnemy)
                minDistToEnemy = dist;
            if (dist <= myRange) {
                ++enemiesInRange;
            }
            if (dist <= rangeExtended) {
                ++enemiesInMoveRange;
            }
        }

        void updateAlly(RobotInfo r) {
            int dist = r.getLocation().distanceSquaredTo(loc);
            if (dist <= myRange)
                ++alliesDist2;
            if (dist <= rangeExtended)
                ++alliesDist5;
        }

        int safe() {
            if (!canMove)
                return -1;
            if (alliesDist2 <= 1 && enemiesInRange > 1)
                return 0;
            if (enemiesInRange > 2)
                return 0;
            if (enemiesInMoveRange > alliesDist5)
                return 1;
            return 2;
        }

        // M is current best micro
        boolean isBetter(MicroInfo M) {
            if (safe() > M.safe())
                return true;
            if (safe() < M.safe())
                return false;

            if (enemiesInRange < M.enemiesInRange)
                return true;
            if (enemiesInRange > M.enemiesInRange)
                return false;

            if (enemiesInMoveRange < M.enemiesInMoveRange)
                return true;
            if (enemiesInMoveRange > M.enemiesInMoveRange)
                return false;

            if (dir == Direction.CENTER)
                return true;
            if (M.dir == Direction.CENTER)
                return false;

            return true;
        }
    }
}
