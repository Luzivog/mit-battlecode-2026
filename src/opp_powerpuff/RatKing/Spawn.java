package opp_powerpuff.RatKing;

import opp_powerpuff.Rat;
import opp_powerpuff.DataStructures.Gain;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Utils.Calc;
import opp_powerpuff.Utils.RNG;
import opp_powerpuff.Managers.Global;
import battlecode.common.*;

public class Spawn {
    public static void defaultSpawn() throws GameActionException {
        RobotController rc = Rat.rc;
        MapLocation currentLocation = rc.getLocation();

        MapLocation center = new MapLocation(Rat.H / 2, Rat.W / 2);
        if (currentLocation.equals(center)) {
            center = currentLocation.add(Constants.adjacentDirections[RNG.rng.nextInt(8)]);
        }

        Direction toCenter = Rat.dirTo(center);
        Direction[] fuzzyDirs = Nav.fuzzyDirs(toCenter.opposite());
        {
            int chance = RNG.rng.nextInt(100);
            Direction d;
            if (chance < 20) {
                d = toCenter;
            } else {
                d = fuzzyDirs[RNG.rng.nextInt(7)];
            }
    
            MapLocation spawnLocation = currentLocation.add(d).add(d);
                if (rc.canBuildRat(spawnLocation)) {
                rc.buildRat(spawnLocation);
                return;
            }
        }
        {
            int chance = RNG.rng.nextInt(100);
            Direction d;
            if (chance < 30) {
                d = toCenter;
            } else {
                d = fuzzyDirs[RNG.rng.nextInt(7)];
            }
    
            MapLocation spawnLocation = currentLocation.add(d).add(d);
                if (rc.canBuildRat(spawnLocation)) {
                rc.buildRat(spawnLocation);
                return;
            }
        }
        {
            int chance = RNG.rng.nextInt(100);
            Direction d;
            if (chance < 30) {
                d = toCenter;
            } else {
                d = fuzzyDirs[RNG.rng.nextInt(7)];
            }
    
            MapLocation spawnLocation = currentLocation.add(d).add(d);
                if (rc.canBuildRat(spawnLocation)) {
                rc.buildRat(spawnLocation);
                return;
            }
        }
    }

    /*
    RATS ALIVE  CHEESE
    0           10
    4           20
    8           30
    12          40      // BREAKEVEN POINT
    16          50
    20          60
    24          70
    28          80    
    
    
    */
    public static boolean check() throws GameActionException {
        RobotController rc = Rat.rc;
        MapLocation mapCenter = new MapLocation(Rat.W / 2, Rat.H / 2);

        MapLocation centralKingLoc = null;
        int minDistToCenter = Integer.MAX_VALUE;

        for (MapLocation kingLoc : VisionManager.allAllyRatKingLocations) {
            if (kingLoc == null) continue;

            int distToCenter = kingLoc.distanceSquaredTo(mapCenter);
            
            if (distToCenter < minDistToCenter) {
                minDistToCenter = distToCenter;
                centralKingLoc = kingLoc;
            }
        }

        if (centralKingLoc != null) {
            if (rc.getLocation().distanceSquaredTo(centralKingLoc) > 3) {
                System.out.println("FAILED SPAWN???");
                return false;
            }
        }

        int ratCost = rc.getCurrentRatCost();
        int ratCount = Calc.determineNumBabyRats(ratCost);
        int allCheese = rc.getAllCheese();
        float cheesePerRound = Gain.getAverage();

        if (rc.getRoundNum() <= Rat.startDuration()) {
            if (Rat.H + Rat.W < 40 + 40) {
                return (ratCount <= 12);
            } else {
                return (ratCount <= Constants.START_RAT_LIMIT);
            }
        } 

        if (allCheese < Constants.DEAD_CHEESE_THRESHOLD) {
            return ratCount <= Constants.DEAD_BABY_RAT_LIMIT;
        }

        if (allCheese < Constants.DANGER_CHEESE_THRESHOLD) {
            return ratCount <= Constants.DANGER_BABY_RAT_LIMIT;
        }
        
        if (allCheese < Constants.MIN_CHEESE_THRESHOLD) {
            if (cheesePerRound < Constants.GOOD_CHEESE_GAIN_THRESHOLD * Global.getLivingKings()) {
                return ratCount <= Constants.MIN_BABY_RAT_LIMIT;
            }
            return false;
        }

        if (allCheese < Constants.MAX_CHEESE_THRESHOLD) {
            if (cheesePerRound < Constants.GOOD_CHEESE_GAIN_THRESHOLD * Global.getLivingKings()) {
                return ratCount <= Constants.MAX_BABY_RAT_LIMIT;
            }
            return false;
        }

        if (allCheese < Constants.WEALTHY_CHEESE_THRESHOLD) {
            if (cheesePerRound < Constants.GOOD_CHEESE_GAIN_THRESHOLD * Global.getLivingKings()) {
                return ratCount <= Constants.WEALTHY_BABY_RAT_LIMIT;
            }
            return false;
        }

        if (allCheese < Constants.KING_CHEESE_THRESHOLD) {
            if (ratCount <= Constants.KING_BABY_RAT_LIMIT) {
                return true;
            }
            return false;
        }

        // PAST KING
        return (allCheese - ratCost > Constants.KING_CHEESE_THRESHOLD);
    }

    public static void emergencySpawn() throws GameActionException {
        RobotController rc = Rat.rc;
        if (VisionManager.closestEnemy == null) return;

        MapLocation currentLoc = rc.getLocation();
        MapLocation enemy = VisionManager.closestEnemy.getLocation();
        Direction dirToEnemy = currentLoc.directionTo(enemy);

        Direction[] priorityDirections = {
            dirToEnemy,
            dirToEnemy.rotateRight(),
            dirToEnemy.rotateLeft(),
            dirToEnemy.rotateRight().rotateRight(),
            dirToEnemy.rotateLeft().rotateLeft(),
            dirToEnemy.opposite().rotateRight(),
            dirToEnemy.opposite().rotateLeft(),
            dirToEnemy.opposite()
        };

        for (Direction d : priorityDirections ){
            MapLocation spawnLoc = currentLoc.add(d).add(d);
            if (!rc.canBuildRat(spawnLoc)) continue;

            Direction dirTowardEnemy = spawnLoc.directionTo(enemy);

            if (rc.canTurn() && rc.getDirection() != dirTowardEnemy) {
                rc.turn(dirTowardEnemy);
            }
            rc.setIndicatorDot(spawnLoc, 255, 255, 255);
            rc.buildRat(spawnLoc);
            return;
        }
    }
}
