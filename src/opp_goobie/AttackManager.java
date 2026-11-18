package opp_goobie;

import battlecode.common.*;

public class AttackManager {
    static void attack() throws GameActionException {
        if (!Robot.rc.isActionReady())
            return;

        RobotInfo target = null;
        MapLocation targetLoc = null;
        int myHealth = Robot.rc.getHealth();
        int rawCheese = Robot.rc.getRawCheese();

        int attackRadius = GameConstants.ATTACK_DISTANCE_SQUARED;
        if (Robot.rc.getType().isRatKingType())
            attackRadius = GameConstants.RAT_KING_ATTACK_DISTANCE_SQUARED;
        RobotInfo[] robots = Robot.rc.senseNearbyRobots(attackRadius);

        for (RobotInfo r : robots) {
            if (r.getTeam() == Robot.team)
                continue;

            if (r.getType().isRatKingType()) {
                MapLocation loc = getAttackableLocationNear(r.getLocation());
                if (loc == null)
                    continue;

                if (myHealth <= Constants.USE_ALL_CHEESE_THRESHOLD) {
                    if (rawCheese >= TrapType.RAT_TRAP.buildCost && rawCheese < TrapType.RAT_TRAP.buildCost * 2
                            && Robot.rc.canPlaceRatTrap(loc)) {
                        Robot.rc.placeRatTrap(loc);
                    } else {
                        Robot.rc.attack(loc, rawCheese);
                    }
                } else {
                    Robot.rc.attack(loc);
                }
                return;
            }

            if (r.getType().isCatType()) {
                MapLocation catLoc = getAttackableCatLocation(r.location);
                if (catLoc == null)
                    continue;
            } else if (!Robot.rc.canAttack(r.getLocation()))
                continue;

            target = compare(target, r);
            if (target.equals(r)) {
                if (target.getType().isCatType()) {
                    targetLoc = getAttackableCatLocation(r.location);
                } else {
                    targetLoc = target.location;
                }
            }
        }

        if (target == null)
            return;

        if (Robot.rc.canTurn())
            Robot.rc.turn(Robot.rc.getLocation().directionTo(targetLoc));

        if (!target.getType().isCatType() && Robot.rc.getAllCheese() >= Constants.USE_CHEESE_FOR_TRAPS_THRESHOLD
                && TrapManager.fuzzyPlaceRatTrap(targetLoc))
            return;

        int enemyHP = target.getHealth();

        int cheeseToSpend = 0;
        if (!target.getType().isCatType() && rawCheese > 0) {
            int needed = cheeseToAvoidPickup(enemyHP, myHealth, rawCheese);
            if (needed != Integer.MAX_VALUE && needed > 0) {
                cheeseToSpend = needed;
            }

            if (needed > 0 && myHealth <= Constants.USE_ALL_CHEESE_THRESHOLD) {
                cheeseToSpend = Math.max(cheeseToSpend, rawCheese);
            }
        }

        if (!target.getType().isCatType()
                && Robot.rc.getAllCheese() >= Constants.USE_CHEESE_FOR_TRAPS_THRESHOLD
                && TrapManager.fuzzyPlaceRatTrap(targetLoc)) {
            return;
        }

        if (target.getType().isCatType() && TrapManager.fuzzyPlaceCatTrap(targetLoc)) {
            return;
        }

        if (cheeseToSpend > 0) {
            Robot.rc.attack(targetLoc, cheeseToSpend);
        } else {
            Robot.rc.attack(targetLoc);
        }
    }

    static RobotInfo compare(RobotInfo A, RobotInfo B) {
        if (A == null)
            return B;
        if (B == null)
            return A;
        if (A.getHealth() < B.getHealth())
            return A;
        return B;
    }

    static MapLocation getAttackableLocationNear(MapLocation loc) {
        if (Robot.rc.canAttack(loc))
            return loc;

        for (Direction d : Robot.DIRECTIONS) {
            MapLocation adj = loc.add(d);
            if (Robot.rc.canAttack(adj)) {
                return adj;
            }
        }
        return null;
    }

    static MapLocation getAttackableCatLocation(MapLocation catLoc) {
        if (Robot.rc.canAttack(catLoc))
            return catLoc;

        MapLocation newLoc = catLoc.add(Direction.NORTH);
        if (Robot.rc.canAttack(newLoc))
            return newLoc;

        newLoc = catLoc.add(Direction.EAST);
        if (Robot.rc.canAttack(newLoc))
            return newLoc;

        newLoc = catLoc.add(Direction.NORTHEAST);
        if (Robot.rc.canAttack(newLoc))
            return newLoc;

        return null;
    }

    static int cheeseToAvoidPickup(int enemyHP, int myHealth, int availableCheese) {
        int baseDamage = 10;

        if (enemyHP == myHealth) {
            if (availableCheese < 5)
                return 1;
            if (availableCheese < 10)
                return 2;
            return 4;
        }

        int requiredDamage = enemyHP - myHealth + 1;
        if (requiredDamage <= baseDamage)
            return 0;

        int extraNeeded = requiredDamage - baseDamage;

        int cheese = extraNeeded * extraNeeded;
        if (cheese > availableCheese)
            return Integer.MAX_VALUE;

        return cheese;
    }
}
