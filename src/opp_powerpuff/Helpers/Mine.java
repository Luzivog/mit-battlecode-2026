package opp_powerpuff.Helpers;

import opp_powerpuff.Utils.RNG;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import opp_powerpuff.Rat;
import opp_powerpuff.Actions.Search;
import opp_powerpuff.Managers.*;

public class Mine {
    public static MapLocation targetCheeseMine = null;

    static final int SYM_HORIZONTAL = 1; // Binary 001
    static final int SYM_VERTICAL   = 2; // Binary 010
    static final int SYM_ROTATION   = 4; // Binary 100    

    public static MapLocation[] near;
    public static int nearCount = 0;

    public static MapLocation[] allyNear;
    public static int allyNearCount = 0;
    public static MapLocation[] enemyNear;
    public static int enemyNearCount = 0;
    
    public static MapLocation[] allyFar;
    public static int allyFarCount = 0;
    public static MapLocation[] enemyFar;
    public static int enemyFarCount = 0;

    public static MapLocation[] far;
    public static int farCount = 0;

    public static boolean expired() throws GameActionException {
        if (targetCheeseMine == null) return true;

        return (
            (
                Rat.rc.getLocation().distanceSquaredTo(targetCheeseMine) <= 1
            ) || 
            (
                Rat.rc.getLocation().distanceSquaredTo(targetCheeseMine) < 2 && 
                VisionManager.allies.length >= 1 &&
                VisionManager.enemies.length == 0 &&
                VisionManager.messages.length == 0  
            )
        );
    }

    public static MapLocation getTargetCheeseMine() throws GameActionException {
        return targetCheeseMine;
    }

    public static void assignTargetCheeseMine() throws GameActionException {
        Search.refill();
        targetCheeseMine = chooseUncontrolledCheeseMine(Rat.rc.getHealth());
    }

    public static MapLocation chooseUncontrolledCheeseMine(int health) throws GameActionException {
        sortLocations();

        int nearIdx = 0;
        int nearCapacity = near.length * (MapData.reflectionCount() + 1);
        MapLocation[] nearUncontrolled = new MapLocation[nearCapacity];
        for (MapLocation loc: near) {
            if (loc != null) {
                if (!kingOnMine(loc, true)) nearUncontrolled[nearIdx++] = loc;
                for (MapLocation ref: MapData.possibleReflections(loc)) {
                    if (ref != null && !kingOnMine(ref, true))
                        nearUncontrolled[nearIdx++] = ref;
                }
            } 
        }

        int farIdx = 0;
        int farCapacity = far.length * (MapData.reflectionCount() + 1);
        MapLocation[] farUncontrolled = new MapLocation[farCapacity];
        for (MapLocation loc: far) {
            if (loc != null) {
                if (!kingOnMine(loc, true)) farUncontrolled[farIdx++] = loc;
                for (MapLocation ref: MapData.possibleReflections(loc)) {
                    if (ref != null && !kingOnMine(ref, true))
                        farUncontrolled[farIdx++] = ref;
                }
            } 
        }

        if (MapData.reflectionCount() > 1) {
            if (health <= Constants.RETREAT_HEALTH) {
                if (farIdx > 0) {
                    return farUncontrolled[RNG.rng.nextInt(farIdx)];
                }
            }
    
            int nearWeight = 25;
            int farWeight = 15;
            int nearTotal = nearWeight * nearIdx;
            int farTotal = farWeight * farIdx;
            int total = nearTotal + farTotal;

            if (total == 0) return null;

            if (RNG.rng.nextInt(total) < nearTotal) {
                int idx = RNG.rng.nextInt(nearIdx);
                return nearUncontrolled[idx];
            } 
            if (farIdx != 0) {
                int idx = RNG.rng.nextInt(farIdx);
                return farUncontrolled[idx];
            }

            return null;
        } else {

            MapLocation allyRatKing = VisionManager.allyRatKingLocation;
            MapLocation enemyRatKing = MapData.existingReflection(allyRatKing);

            allyNear = new MapLocation[nearIdx];
            allyFar = new MapLocation[farIdx];
            allyNearCount = 0;
            allyFarCount = 0;

            enemyNear = new MapLocation[nearIdx];
            enemyFar = new MapLocation[farIdx];
            enemyNearCount = 0;
            enemyFarCount = 0;           

            for (int i = 0; i < nearIdx; i++) {
                MapLocation loc = nearUncontrolled[i];
                if (allyRatKing.distanceSquaredTo(loc) < enemyRatKing.distanceSquaredTo(loc)) {
                    allyNear[allyNearCount++] = loc;
                } else {
                    enemyNear[enemyNearCount++] = loc;
                }
            }

            for (int i = 0; i < farIdx; i++) {
                MapLocation loc = farUncontrolled[i];
                if (allyRatKing.distanceSquaredTo(loc) < enemyRatKing.distanceSquaredTo(loc)) {
                    allyFar[allyFarCount++] = loc;
                } else {
                    enemyFar[enemyFarCount++] = loc;
                }
            }

            int allyFarWeight = 10;
            int allyNearWeight = 30;
            int enemyNearWeight = 15;
            int enemyFarWeight = 2;

            int cheeseCount = Rat.rc.getAllCheese();
            if (cheeseCount < Constants.MIN_CHEESE_THRESHOLD || Rat.rc.getRoundNum() < Rat.startDuration()) {
                allyFarWeight = 12;
                allyNearWeight = 25;
                enemyNearWeight = 20;
                enemyFarWeight = 6;
            } else if (cheeseCount < Constants.MAX_CHEESE_THRESHOLD) {
                allyFarWeight = 10;
                allyNearWeight = 20;
                enemyNearWeight = 25;
                enemyFarWeight = 8;
            } else if (cheeseCount < Constants.WEALTHY_CHEESE_THRESHOLD) {
                allyFarWeight = 10;
                allyNearWeight = 25;
                enemyNearWeight = 35;
                enemyFarWeight = 16;
            } else {
                allyFarWeight = 10;
                allyNearWeight = 30;
                enemyNearWeight = 40;
                enemyFarWeight = 18;   
            }
            
            int allyFarTotal = allyFarWeight * allyFarCount;
            int allyNearTotal = allyNearWeight * allyNearCount;
            int enemyNearTotal = enemyNearWeight * enemyNearCount;
            int enemyFarTotal = enemyFarWeight * enemyFarCount;

            if (allyFarTotal + allyNearTotal + enemyNearTotal + enemyFarTotal == 0) return null;
            int chance = RNG.rng.nextInt(allyFarTotal + allyNearTotal + enemyNearTotal + enemyFarTotal);

            if (chance < allyFarTotal) {
                return allyFar[RNG.rng.nextInt(allyFarCount)];
            } else if (chance < allyFarTotal + allyNearTotal) {
                return allyNear[RNG.rng.nextInt(allyNearCount)];
            } else if (chance < allyFarTotal + allyNearTotal + enemyNearTotal) {
                return enemyNear[RNG.rng.nextInt(enemyNearCount)];
            } else if (chance < allyFarTotal + allyNearTotal + enemyNearTotal + enemyFarTotal) {
                return enemyFar[RNG.rng.nextInt(enemyFarCount)];
            }

            return null;
        }

    }

    public static boolean knownCheeseMine(MapLocation mineLoc) throws GameActionException {
        MapLocation[] cheeseMines = VisionManager.globalCheeseMines;
        for (MapLocation loc: cheeseMines) {
            if (mineLoc.equals(loc)) return true;
            for (MapLocation ref: MapData.possibleReflections(loc)) {
                if (ref != null && ref.equals(mineLoc)) return true;    
            }
        }
        return false;
    }

    public static boolean kingOnMine(MapLocation mineLoc, boolean includeSelf) throws GameActionException {
        if (includeSelf) {
            for (MapLocation kingLoc: VisionManager.allAllyRatKingLocations) {
                if (kingLoc.distanceSquaredTo(mineLoc) <= 15) {
                    return true;
                }
            }
            return false;
        }

        for (MapLocation kingLoc: VisionManager.allAllyRatKingLocations) {
            if (kingLoc.distanceSquaredTo(Rat.rc.getLocation()) <= 2 && Rat.rc.getType() == UnitType.RAT_KING) continue;
            if (kingLoc.distanceSquaredTo(mineLoc) <= 15) {
                return true;
            }
        }
        return false;          
    }

    public static void sortLocations() throws GameActionException {
        int symmetryFlag = MapData.symmetry;
        MapLocation[] locations = Global.readCheeseMineLocations();
        RobotController rc = Rat.rc;
        nearCount = 0;
        farCount = 0;
        
        if (near == null || near.length < locations.length) {
            near = new MapLocation[locations.length];
            far = new MapLocation[locations.length];
        }

        int width = rc.getMapWidth();
        int height = rc.getMapHeight();
        
        double centerX = (width - 1) / 2;
        double centerY = (height - 1) / 2;

        for (MapLocation loc : locations) {
            if (loc == null) continue; 

            boolean isNear = false;

            if ((symmetryFlag & SYM_HORIZONTAL) != 0) {
                if (Math.abs(loc.x - centerX) <= Rat.W / 5) {
                    isNear = true;
                }
            }
            else if ((symmetryFlag & SYM_VERTICAL) != 0) {
                if (Math.abs(loc.y - centerY) <= Rat.H / 5) {
                    isNear = true;
                }
            }
            else if ((symmetryFlag & SYM_ROTATION) != 0) {
                int temp = (Rat.H + Rat.W) / 5;
                double distSq = (loc.x - centerX) * (loc.x - centerX) + 
                                (loc.y - centerY) * (loc.y - centerY);
                if (distSq <= (temp * temp)) {
                    isNear = true;
                }
            }

            if (isNear) {
                near[nearCount++] = loc;
            } else {
                far[farCount++] = loc;
            }
        }
    }
}
