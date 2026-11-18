package opp_goobie;

import battlecode.common.*;

public class MicroManager {
    static final int MAX_SENSED_CATS = 10;

    static RobotController rc;
    static int myRange;
    static boolean alwaysInRange = false;
    static boolean canAct = true;
    static boolean isCarrying = false;
    static MicroInfo[] microInfo;
    static Direction[] dirs = Direction.values();
    static int rangeExtended = 8;
    static RobotInfo[] sensedCats = new RobotInfo[MAX_SENSED_CATS];
    static int sensedCatsIdx = 0;

    static void init() {
        rc = Robot.rc;
        myRange = GameConstants.ATTACK_DISTANCE_SQUARED;
    }

    static boolean doMicro() throws GameActionException {
        boolean canMove = rc.isMovementReady();
        boolean canTurn = rc.canTurn();

        if (!canMove && !canTurn)
            return false;

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.opponent);
        if (nearbyEnemies.length == 0 && CommsSqueak.enemies.size == 0)
            return false;

        isCarrying = rc.getCarrying() != null;
        canAct = rc.isActionReady();
        alwaysInRange = !canAct;

        MicroInfo bestMicro = microInfo[8];
        for (int i = 7; i >= 0; --i) {
            if (microInfo[i].isBetter(bestMicro))
                bestMicro = microInfo[i];
        }

        boolean eir = bestMicro.minDistToEnemy <= rangeExtended;

        Debug.addIndicatorString(
                "best micro: " + bestMicro.dir + "; best dir: " +
                        (bestMicro.closestEnemy != null ? rc.getLocation().directionTo(bestMicro.closestEnemy)
                                : "NONE"));
        Direction d = rc.getLocation().directionTo(bestMicro.closestEnemy);
        if (canMove && bestMicro.dir == Direction.CENTER) {
            if (canTurn && bestMicro.closestEnemy != null) {
                if (d != Direction.CENTER && d != Robot.rc.getDirection())
                    rc.turn(d);
            }
            MapLocation myLoc = rc.getLocation();
            Direction myDir = rc.getDirection();
            MapLocation l1 = myLoc.add(myDir);
            MapLocation l2 = myLoc.add(myDir.rotateLeft());
            MapLocation l3 = myLoc.add(myDir.rotateRight());
            int score = 0;
            if (rc.onTheMap(l1) && rc.sensePassability(l1))
                score += 1;
            if (rc.onTheMap(l2) && rc.sensePassability(l2))
                score += 1;
            if (rc.onTheMap(l3) && rc.sensePassability(l3))
                score += 1;

            return score > 1;
        }

        if (!eir && canTurn && d != Direction.CENTER && d != Robot.rc.getDirection()) {
            canTurn = false;
            Robot.rc.turn(d);
        }

        if (canMove && bestMicro.dir != Direction.CENTER && rc.canMove(bestMicro.dir)) {
            rc.move(bestMicro.dir);
            if (canTurn && bestMicro.closestEnemy != null) {
                d = rc.getLocation().directionTo(bestMicro.closestEnemy);
                if (d != Direction.CENTER && d != Robot.rc.getDirection())
                    rc.turn(d);
            }
            return true;
        }

        return false;
    }

    static void computeMicroArray() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(Robot.visionRadiusSquared);
        sensedCatsIdx = 0;
        microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++)
            microInfo[i] = new MicroInfo(dirs[i]);

        for (RobotInfo r : robots) {
            if (r.getTeam() == Robot.team)
                continue;
            if (r.getType().isCatType())
                sensedCats[sensedCatsIdx++] = r;

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

        for (int i = 0; i < CommsSqueak.enemies.size; i++) {
            int c = CommsSqueak.enemies.keys.charAt(i);
            int li = c & 0xFFF;
            int di = (c >> 12) & 0x7;
            MapLocation l = Util.intToLocation(li);
            Direction d = dirs[di];
            microInfo[0].updateEnemy(l, d);
            microInfo[1].updateEnemy(l, d);
            microInfo[2].updateEnemy(l, d);
            microInfo[3].updateEnemy(l, d);
            microInfo[4].updateEnemy(l, d);
            microInfo[5].updateEnemy(l, d);
            microInfo[6].updateEnemy(l, d);
            microInfo[7].updateEnemy(l, d);
            microInfo[8].updateEnemy(l, d);
        }

        if (robots.length == 0 && CommsSqueak.enemies.size == 0)
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

        microInfo[0].checkIfInCatExclusionZone();
        microInfo[1].checkIfInCatExclusionZone();
        microInfo[2].checkIfInCatExclusionZone();
        microInfo[3].checkIfInCatExclusionZone();
        microInfo[4].checkIfInCatExclusionZone();
        microInfo[5].checkIfInCatExclusionZone();
        microInfo[6].checkIfInCatExclusionZone();
        microInfo[7].checkIfInCatExclusionZone();
        microInfo[8].checkIfInCatExclusionZone();
    }

    static void updateTrap(MapLocation l) {
        microInfo[0].updateTrap(l);
        microInfo[1].updateTrap(l);
        microInfo[2].updateTrap(l);
        microInfo[3].updateTrap(l);
        microInfo[4].updateTrap(l);
        microInfo[5].updateTrap(l);
        microInfo[6].updateTrap(l);
        microInfo[7].updateTrap(l);
        microInfo[8].updateTrap(l);
    }

    static class MicroInfo {
        Direction dir;
        MapLocation loc;
        int minDistToEnemy = Constants.INF;
        int minDistToCat = Constants.INF;
        int minDistTrap = Constants.INF;
        int minDistToUnawareRat = Constants.INF;
        MapLocation closestEnemy;
        MapLocation closestCat;
        boolean canRatNap = false;
        boolean catInRange = false;
        boolean inCatExclusionZone = false;
        int enemiesInRange = 0;
        int enemiesInMoveRange = 0;
        int catsInMoveRange = 0;
        int alliesDist2 = 1 + CommsSqueak.allies2;
        int alliesDist5 = 1 + CommsSqueak.allies5;
        int enemyRatsInRange = 0;
        int weakEnemiesInRange = 0;

        boolean canMove = true;

        MicroInfo(Direction dir) {
            this.dir = dir;
            this.loc = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir))
                canMove = false;
        }

        void updateEnemy(RobotInfo r) {
            if (!canMove)
                return;
            int dist = r.location.distanceSquaredTo(loc);
            boolean isCat = r.getType().isCatType();
            boolean isFacingMe = r.location.isWithinDistanceSquared(loc, Robot.visionRadiusSquared, r.direction,
                    Robot.visionAngle);

            if (!isCat && !isFacingMe && dist <= myRange) {
                canRatNap = true;
            }

            if (!isCat && !isFacingMe && dist < minDistToUnawareRat) {
                minDistToUnawareRat = dist;
            }

            if (dist < minDistToEnemy) {
                minDistToEnemy = dist;
                closestEnemy = r.location;
            }

            if (isCat && dist < minDistToCat) {
                minDistToCat = dist;
                closestCat = r.location;
            }

            if (dist <= myRange) {
                ++enemiesInRange;
                if (!isCat) {
                    ++enemyRatsInRange;
                    if (r.getHealth() <= 30) {
                        ++weakEnemiesInRange;
                    }
                }
            }

            if (dist <= rangeExtended) {
                ++enemiesInMoveRange;
                if (isCat) {
                    catInRange = true;
                    ++catsInMoveRange;
                }
            }

        }

        void updateEnemy(MapLocation l, Direction d) {
            if (!canMove)
                return;
            int dist = l.distanceSquaredTo(loc);
            boolean isFacingMe = l.isWithinDistanceSquared(loc, Robot.visionRadiusSquared, d, Robot.visionAngle);

            canRatNap = !isFacingMe && dist <= myRange;

            if (dist < minDistToEnemy) {
                minDistToEnemy = dist;
                closestEnemy = l;
            }
            if (dist <= myRange)
                ++enemiesInRange;
            if (dist <= rangeExtended)
                ++enemiesInMoveRange;
        }

        void updateAlly(RobotInfo r) {
            if (!canMove)
                return;
            int dist = r.getLocation().distanceSquaredTo(loc);
            if (dist <= myRange)
                ++alliesDist2;
            if (dist <= rangeExtended)
                ++alliesDist5;
        }

        void updateTrap(MapLocation l) {
            if (!canMove)
                return;
            int dist = l.distanceSquaredTo(loc);
            if (dist < minDistTrap) {
                minDistTrap = dist;
            }
        }

        int safe() {
            if (!canMove)
                return -2;
            if (inCatExclusionZone)
                return -1;
            if (catInRange && minDistToCat <= 4)
                return -1;

            if (enemyRatsInRange >= alliesDist2 + 3)
                return 0;
            if (catsInMoveRange > 0 && alliesDist5 < 2)
                return 0;
            if (enemyRatsInRange > 0 && alliesDist2 <= 1 && enemyRatsInRange > 1)
                return 0;
            if (enemyRatsInRange >= 2 && alliesDist2 < enemyRatsInRange)
                return 0;

            if (enemyRatsInRange > alliesDist2 + 1)
                return 1;
            if (enemiesInMoveRange > alliesDist5 + 1)
                return 1;

            if (weakEnemiesInRange > 0 && alliesDist2 >= weakEnemiesInRange)
                return 2;
            return 2;
        }

        boolean inRange() {
            if (alwaysInRange)
                return true;
            return minDistToEnemy <= myRange;
        }

        // M is current best micro
        boolean isBetter(MicroInfo M) {
            int mySafe = safe();
            int theirSafe = M.safe();

            if (mySafe > theirSafe)
                return true;
            if (mySafe < theirSafe)
                return false;

            if (inRange() && !M.inRange())
                return true;
            if (!inRange() && M.inRange())
                return false;

            if (inRange() && canAct && !isCarrying) {
                if (weakEnemiesInRange > M.weakEnemiesInRange)
                    return true;
                if (weakEnemiesInRange < M.weakEnemiesInRange)
                    return false;

                if (canRatNap && !M.canRatNap)
                    return true;
                if (!canRatNap && M.canRatNap)
                    return false;
            }

            if (alliesDist2 > M.alliesDist2 && enemiesInRange > 0)
                return true;
            if (alliesDist2 < M.alliesDist2 && M.enemiesInRange > 0)
                return false;

            if (!inRange() && !isCarrying) {
                if (minDistToUnawareRat < M.minDistToUnawareRat)
                    return true;
                if (minDistToUnawareRat > M.minDistToUnawareRat)
                    return false;

                if (minDistToEnemy < M.minDistToEnemy)
                    return true;
                if (minDistToEnemy > M.minDistToEnemy)
                    return false;
            }

            if (enemiesInRange < M.enemiesInRange)
                return true;
            if (enemiesInRange > M.enemiesInRange)
                return false;

            if (catsInMoveRange < M.catsInMoveRange)
                return true;
            if (catsInMoveRange > M.catsInMoveRange)
                return false;

            if (enemiesInMoveRange < M.enemiesInMoveRange)
                return true;
            if (enemiesInMoveRange > M.enemiesInMoveRange)
                return false;

            if (minDistToCat > M.minDistToCat)
                return true;
            if (minDistToCat < M.minDistToCat)
                return false;

            if (dir == Direction.CENTER)
                return true;
            if (M.dir == Direction.CENTER)
                return false;

            return false;
        }

        void checkIfInCatExclusionZone() {
            if (sensedCatsIdx == 0) {
                inCatExclusionZone = false;
                return;
            }
            for (int i = 0; i < sensedCatsIdx; i++) {
                if (sensedCats[i].location.isWithinDistanceSquared(loc, UnitType.CAT.visionConeRadiusSquared,
                        sensedCats[i].direction, UnitType.CAT.visionConeAngle, true)) {
                    inCatExclusionZone = true;
                    return;
                }
            }
            inCatExclusionZone = false;
        }
    }
}
