package opp_cerr;

import battlecode.common.*;

public class BabyRatMicro {
    static RobotController rc;
    static RobotInfo[] enemyRobots, friendlyRobots, neutralRobots;
    static MapLocation spawn;
    static int roundNumber;
    static MapLocation myLoc;
    static int lastSawEnemyKing = 0;
    static MapLocation closestEnemy;
    static boolean calculated_carry = false;
    static final int THROW_DAMAGE = 32;

    static int myMoveNumber;

    public static void init(RobotController r) {
        rc = r;
    }

    public static boolean processTurn(
            RobotInfo[] er, RobotInfo[] nr, RobotInfo[] fr, MapLocation spawnLoc)
            throws GameActionException {
        try {
            setGlobals(er, nr, fr, -1, spawnLoc);
            return movementMicro();
        } catch (Exception e) {
            System.out.println("WHAT???????????????");
            e.printStackTrace();
            return false;
        }
    }

    public static void setGlobals(
            RobotInfo[] er, RobotInfo[] nr, RobotInfo[] fr, int moveNumber, MapLocation spawnLoc) {
        myMoveNumber = moveNumber;
        enemyRobots = er;
        neutralRobots = nr;
        friendlyRobots = fr;
        roundNumber = rc.getRoundNum();
        myLoc = rc.getLocation();
        spawn = spawnLoc;
        calculated_carry = false;
    }

    public static void turn(Direction d) throws GameActionException {
        if (d == null || d == Direction.CENTER) {
            return;
        }
        if (rc.canTurn()
                && d != rc.getDirection()
                && rc.getCarrying()
                        == null) { // if you can turn, youare not aready facing turning direction
            // and you arent carrying, then turn
            rc.turn(d);
        }
    }

    public static void doMovement(Direction d, Direction bestMicroDir) throws GameActionException {
        if (rc.getDirection() == bestMicroDir) {
            rc.moveForward();
            turn(d);
        } else {
            turn(d);
            if (rc.getDirection() == bestMicroDir) {
                rc.moveForward();
            } else {
                rc.move(bestMicroDir);
            }
        }
    }

    public static boolean movementMicro() throws Exception {
        rc.setIndicatorString("WOWW");
        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 9; i-- > 0; ) {
            microInfo[i] = new MicroInfo(Direction.allDirections()[i]);
        }
        closestEnemy = null;

        for (int i = enemyRobots.length; i-- > 0; ) {
            if (closestEnemy == null
                    || myLoc.distanceSquaredTo(closestEnemy)
                            > myLoc.distanceSquaredTo(enemyRobots[i].getLocation())) {
                closestEnemy = enemyRobots[i].getLocation();
            }
        }
        if (closestEnemy == null) {
            for (int i = neutralRobots.length; i-- > 0; ) {
                if (closestEnemy == null
                        || myLoc.distanceSquaredTo(closestEnemy)
                                > myLoc.distanceSquaredTo(neutralRobots[i].getLocation())) {
                    closestEnemy = neutralRobots[i].getLocation();
                }
            }
        }

        for (int i = enemyRobots.length; i-- > 0; ) {
            if (rc.getType() == UnitType.RAT_KING) {
                lastSawEnemyKing = rc.getRoundNum();
            }
            RobotInfo r = enemyRobots[i];

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

        for (int i = neutralRobots.length; i-- > 0; ) {
            RobotInfo r = neutralRobots[i];

            microInfo[0].updateNeutral(r);
            microInfo[1].updateNeutral(r);
            microInfo[2].updateNeutral(r);
            microInfo[3].updateNeutral(r);
            microInfo[4].updateNeutral(r);
            microInfo[5].updateNeutral(r);
            microInfo[6].updateNeutral(r);
            microInfo[7].updateNeutral(r);
            microInfo[8].updateNeutral(r);
        }

        for (int i = friendlyRobots.length; i-- > 0; ) {
            RobotInfo r = friendlyRobots[i];

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
        microInfo[0].updateDps();
        microInfo[1].updateDps();
        microInfo[2].updateDps();
        microInfo[3].updateDps();
        microInfo[4].updateDps();
        microInfo[5].updateDps();
        microInfo[6].updateDps();
        microInfo[7].updateDps();
        microInfo[8].updateDps();

        MicroInfo bestMicro = microInfo[8];
        for (int i = 0; i < 8; ++i) {
            if (microInfo[i].isBetter(bestMicro, rc.getActionCooldownTurns()))
                bestMicro = microInfo[i];
        }
        rc.setIndicatorString("best micro dir is " + bestMicro.dir);
        if (bestMicro.dir == Direction.CENTER && rc.getActionCooldownTurns() < 10) {
            rc.setIndicatorString(
                    "micro info is hte following: "
                            + microInfo[8].dir
                            + " nearby enemies "
                            + microInfo[8].enemiesNearby);
        }
        if (bestMicro.dir
                == Direction
                        .CENTER) { // if you cant move and not carrying, then turn to enemy robot
            turn(myLoc.directionTo(closestEnemy));
            return false; // not gonna count turning as moving
        }
        calculated_carry = rc.getHealth() - 10 <= bestMicro.dps || bestMicro.canBeCarried;
        // if carrying robot, then dpont turn
        // otherwise, turn to enemy so they cant go out of your sight
        if (rc.canMove(bestMicro.dir)) {
            rc.setIndicatorString("moving in that dir" + bestMicro.dir);
            MapLocation newLoc = myLoc.add(bestMicro.dir);
            Direction newDirToEnemy = newLoc.directionTo(closestEnemy);
            doMovement(newDirToEnemy, bestMicro.dir);
            if (bestMicro.canBeCarried
                    && rc.getHealth() <= 20
                    && !rc.isActionReady()
                    && rc.getRawCheese() == 0) {
                rc.disintegrate();
            }
            return true;
        } else {
            // if not carrying, turn to closest enemy
            return false;
            // rc.resign();
        }
        // rc.setIndicatorString("uh oh");
        // return false;
    }

    public static boolean shouldAttackCats() {
        int numRobots = ((rc.getCurrentRatCost() - 10) / 10) * 4;
        return numRobots > 20 && rc.getRawCheese() == 0;
    }

    static class MicroInfo {
        Direction dir;
        MapLocation location;
        boolean canMove = true;
        boolean canOneShot = false;
        int dps = 0;
        int enemiesNearby = 0;
        int friendsNearby = 0;
        int distToClosestEnemy = 100000;
        int distToClosestCat = 10000;
        boolean canCarry = false;
        boolean canBeCarried = false;
        boolean goodToAttack = false;

        public MicroInfo(Direction dir) throws GameActionException {
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            distToClosestEnemy = 10000;
            canCarry = false;
            canBeCarried = false;
            canOneShot = false;
            distToClosestCat = 10000;
            if (dir == rc.getDirection()) { // this is just as good as having an extra friend
                friendsNearby++;
            }
            if (dir != Direction.CENTER && !rc.canMove(dir)) {
                canMove = false;
            }
        }

        void updateDps() {
            if (canCarry && rc.isActionReady()) {
                dps -= 30;
            } else if (canCarry) {
                dps -= 5;
            }
            if (canOneShot) {
                dps -= 10;
            }
            if (canBeCarried) {
                dps += 30;
            }
        }

        void updateEnemy(RobotInfo unit) throws GameActionException {
            if (!canMove) {
                return;
            }

            boolean enemyCanSee = Utils.ratCanDetect(unit, location); // TODO USE UTILS
            boolean mogs = BabyRat.mogEnemy(unit) || friendlyRobots.length > enemyRobots.length;
            if (unit.getLocation().distanceSquaredTo(location) < 4) {
                if (unit.getType().isBabyRatType()) {
                    if (enemyCanSee) {
                        dps += 10;
                    } else {
                        dps += 3;
                    }
                    if (rc.getCarrying() == null
                            && (unit.getHealth() < rc.getHealth() || !enemyCanSee)) {
                        canCarry = true;
                    }
                }
                if (unit.getHealth() <= 12) {
                    canOneShot = true;
                }
                enemiesNearby++;
            } else if (Utils.unitCanReach(rc, unit, location) && !mogs) {
                if (unit.getType().isBabyRatType()) {
                    if (enemyCanSee && rc.getCarrying() == null) {
                        dps += 10;
                    } else if (rc.getCarrying() == null) {
                        dps += 3;
                    }
                }
            }
            if (unit.getType().isBabyRatType()
                    && (rc.getCarrying() == null
                            || unit.getHealth() - THROW_DAMAGE > rc.getHealth())
                    && enemyCanSee) { // if you're carrying then you throw first
                if (unit.getLocation().distanceSquaredTo(location) < 4) {
                    if (unit.getHealth() - 12 > rc.getHealth()) {
                        canBeCarried = true;
                    }
                } else if (Utils.unitCanReach(rc, unit, location)
                        && unit.getHealth() > rc.getHealth()) {
                    canBeCarried = true;
                }
            }
            distToClosestEnemy =
                    Math.min(distToClosestEnemy, unit.getLocation().distanceSquaredTo(location));
        }

        void updateNeutral(RobotInfo unit) {
            if (!canMove) {
                return;
            }

            MapLocation unitLoc = unit.getLocation();

            int curDist = unit.getLocation().distanceSquaredTo(location);
            distToClosestCat = Math.min(distToClosestCat, curDist);
            boolean catCanSee = Utils.catCanDetect(unit, location);

            if (!catCanSee) {
                if (unitLoc.distanceSquaredTo(location)
                        <= 20) { // TODO IMPLEMENT HELPER TO CHECK IF CAT CAN ATTACK you
                    dps += 5;
                }

                if (unitLoc.distanceSquaredTo(location) <= 9) {
                    dps += 10;
                }
                goodToAttack = true;

                return;
            }

            if (unitLoc.distanceSquaredTo(location) <= 20) {
                dps += 14;
            }
            if (unitLoc.distanceSquaredTo(location) <= 9) {
                dps += 36;
            }
            //            if (myLoc.distanceSquaredTo(spawn) < 30) {
            //                if (curDist < 4) {
            //                    enemiesNearby++;
            //                }
            //                curDist = -curDist;
            //            }
        }

        void updateAlly(RobotInfo unit) {
            if (!canMove) {
                return;
            }

            if (unit.getLocation().distanceSquaredTo(location) < 9) {
                friendsNearby++;
            }
        }

        boolean isBetter(MicroInfo M, int cooldown) {
            if (!canMove) return false;
            if (!M.canMove) return true;
            boolean shouldChicken =
                    (rc.getRoundNum() - lastSawEnemyKing <= 3
                                    && enemyRobots.length > friendlyRobots.length)
                            || (rc.readSqueaks(rc.getRoundNum()).length == 0
                                    && rc.getHealth() <= 30);
            //            boolean shouldChicken =
            // (rc.getRoundNum()-lastSawEnemyKing<=3&&myLoc.distanceSquaredTo(spawn)>64&&enemyRobots.length>1)||enemyRobots.length>friendlyRobots.length+1;
            if (!shouldChicken
                    && enemyRobots.length > 0
                    && (rc.getRawCheese() <= 33 || friendlyRobots.length == 0)
                    && (rc.isActionReady())) {
                if (rc.getCarrying() != null) {
                    if (dps < M.dps) {
                        return true;
                    } else if (M.dps < dps) {
                        return false;
                    } else {
                        return distToClosestEnemy <= M.distToClosestEnemy;
                    }
                }
                if (dps - M.dps >= 30 && rc.getHealth() > 30) {
                    return false;
                } else if (M.dps - dps >= 30 && rc.getHealth() > 30) {
                    return true;
                }
                if (enemiesNearby == 0 && M.enemiesNearby > 0) {
                    return false;
                } else if (M.enemiesNearby == 0 && 0 < enemiesNearby) {
                    return true;
                } else if (dps == 0 && M.dps == 0) {
                    return distToClosestEnemy <= M.distToClosestEnemy;
                } else {
                    if (dps < M.dps) {
                        return true;
                    } else if (M.dps < dps) {
                        return false;
                    } else {
                        return distToClosestEnemy <= M.distToClosestEnemy;
                    }
                }
            } else if (neutralRobots.length > 0) { // TODO: FIX NEUTRLA MICRO
                if (shouldAttackCats()) {
                    if (goodToAttack && !M.goodToAttack) {
                        return true;
                    } else if (!goodToAttack && M.goodToAttack) {
                        return false;
                    } else if (goodToAttack && M.goodToAttack) {
                        // approach cats when we have lots of robots and cheese to burn
                        // ignore DPS penalty from cats TODO: PLAY WITH LATER
                        return distToClosestCat <= M.distToClosestCat;
                    } else {
                        if (dps < M.dps) {
                            return true;
                        } else if (M.dps < dps) {
                            return false;
                        } else {
                            return distToClosestCat >= M.distToClosestCat;
                        }
                    }
                } else if (enemiesNearby == 0 && M.enemiesNearby > 0) {
                    return false;
                } else if (M.enemiesNearby == 0 && 0 < enemiesNearby) {
                    return true;
                } else if (dps < M.dps) {
                    return true;
                } else if (M.dps < dps) {
                    return false;
                } else {
                    return distToClosestCat >= M.distToClosestCat;
                }
            } else {
                if (dps < M.dps) {
                    return true;
                } else if (M.dps < dps) {
                    return false;
                } else {
                    return distToClosestEnemy
                            >= M.distToClosestEnemy; // prefers not moving to save cooldown
                }
            }
        }
    }
}
