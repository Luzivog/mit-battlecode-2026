package opp_parmesan;

import battlecode.common.*;
import opp_parmesan.Roles.*;
import opp_parmesan.Utils.*;

public class Baby extends Globals {

    static MapLocation spawn = null;

    public static boolean isNewKing = false;
    public static boolean explorer = false;

    static int roundsBeingHeld = 0;

    public static void evadeCats() throws GameActionException {
        // evade cats
        FastSet fst = new FastSet();
        for (RobotInfo cat: catRobots) {
            MapLocation fakeID = new MapLocation(cat.getID()%64, (cat.getID()%4096)>>6);
            if (!fst.contains(fakeID)) {
                fst.add(fakeID);
                CatsAreWalls.pretend(cat.location, cat.direction);
            }
        }
        for (RobotInfo cat: catsLastTurn) {
            MapLocation fakeID = new MapLocation(cat.getID()%64, (cat.getID()%4096)>>6);
            if (!fst.contains(fakeID)) {
                fst.add(fakeID);
                CatsAreWalls.pretend(cat.location, cat.direction);
                CatsAreWalls.pretend(cat.location.add(cat.direction), cat.direction);
            }
        }
    }

    public static void unevadeCats() throws GameActionException {
        // unevade cats
        FastSet fst = new FastSet();
        for (RobotInfo cat: catRobots) {
            MapLocation fakeID = new MapLocation(cat.getID()%64, (cat.getID()%4096)>>6);
            if (!fst.contains(fakeID)) {
                fst.add(fakeID);
                CatsAreWalls.unpretend(cat.location, cat.direction);
            }
        }
        for (RobotInfo cat: catsLastTurn) {
            MapLocation fakeID = new MapLocation(cat.getID()%64, (cat.getID()%4096)>>6);
            if (!fst.contains(fakeID)) {
                fst.add(fakeID);
                CatsAreWalls.unpretend(cat.location, cat.direction);
                CatsAreWalls.unpretend(cat.location.add(cat.direction), cat.direction);
            }
        }
    }

    public static void burnCooldown() throws GameActionException {
        if (closestEnemy == null || rc.getLocation().distanceSquaredTo(closestEnemy.location) > 8) {
            if (rc.getRawCheese() < maxCheese) {
                for (int i = 0; i < 9; i++) {
                    if (rc.canPickUpCheese(rc.getLocation().add(Direction.DIRECTION_ORDER[i]))) {
                        pickUpCheese(rc.getLocation().add(Direction.DIRECTION_ORDER[i]));
                        break;
                    }
                }
            }
        }

        if (enemyRobots.length == 0 && closestEnemy == null) {
            for (Direction direction : Direction.DIRECTION_ORDER) {
                if (rc.canAttack(rc.getLocation().add(direction))) {
                    rc.attack(rc.getLocation().add(direction));
                    break;
                }
            }
        }

        MapLocation transferLoc = closestKing.add(closestKing.directionTo(rc.getLocation()));
        if (rc.canTransferCheese(transferLoc, rc.getRawCheese()))
            rc.transferCheese(transferLoc, rc.getRawCheese());
    }

    public static void run() throws GameActionException {
        // newborn
        if (spawn == null) {
            spawn = rc.getLocation();

            explorer = true;
        }

        // disintegration to disarm opponent
        if (rc.isBeingCarried()) {
            roundsBeingHeld++;
            if (roundsBeingHeld >= 2 && squeaksReadLastRound >= 2 && rc.getHealth() < 50) rc.disintegrate();
        }
        else {
            roundsBeingHeld = 0;
        }

        // check for emergency mode
        if (rc.readSharedArray(60) > 0 && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            return;
        }

        if (!rc.isBeingThrown() && !rc.isBeingCarried()) {
            // return to king unless can SEE enemies
            if (Miner.returningToKing && enemyRobots.length == 0) {
                Miner.run();
                burnCooldown();
                return;
            }
            // attack micro
            boolean didAttack = Attack.attack();

            if (!didAttack) {
                evadeCats();

                if (isNewKing) {
                    Multiking.run();
                }
                else {
                    if (enemyRobots.length == 0 && closestEnemy == null) Hunter.run();

                    // miner
                    if (Miner.shouldBeMiner() && enemyRobots.length == 0) {
                        rc.setIndicatorDot(new MapLocation(0, 0), 255, 255, 0);
                        Miner.run();
                        if (Miner.returningToKing) hasSqueaked = true;
                        Vision.turnJustBecause();
                    }
                    // explorer
                    else if (explorer) {
                        rc.setIndicatorDot(new MapLocation(0, 0), 0, 255, 0);
                        Movement.explore();
                        Vision.turnJustBecause();
                    } else {
                        Rush.run(spawn);
                        Vision.turnJustBecause();
                    }
                }
                unevadeCats();
            }
            burnCooldown();
        }

        // Inform nearby allies of enemy information
        Comms.squeakEnemies();

        // symmetry stuff
        Symmetry.updateSymmetryFromGlobal();
        Symmetry.updateNearbySymmetry();
        Symmetry.hearNearbySymmetrySqueaks();
        Symmetry.commSymmetryToNearby();

    }
}