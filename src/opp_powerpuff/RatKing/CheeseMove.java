package opp_powerpuff.RatKing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import opp_powerpuff.Managers.Global;
//import opp_powerpuff.Debug.*;
import opp_powerpuff.Rat;
import opp_powerpuff.DataStructures.Gain;

import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Mine;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Rats.RatKing;
import opp_powerpuff.Utils.RNG;

public class CheeseMove {
    public static boolean turnRight = true;

    public static int invalidCount = 0;
    public static MapLocation[] invalidMines = new MapLocation[60];
    public static MapLocation closestGlobalMine = null;

    public static void run() throws GameActionException {
        // Debug.printMapLocArray(invalidMines);
        // Debug.printMapLocArray(VisionManager.globalCheeseMines);
        if (moveToCheeseCondition())
            moveToCheese();
    }

    public static boolean moveToCheeseCondition() throws GameActionException {
        if (
            VisionManager.cats.length > 0 && 
            closestGlobalMine != null && 
            VisionManager.closestCat.getLocation().distanceSquaredTo(closestGlobalMine) <= 15
        ) {
            boolean alreadyInvalid = false;
            for (int i = 0; i < invalidCount + 1; i++) {
                if (invalidMines[i] != null && closestGlobalMine.equals(invalidMines[i])) {
                    alreadyInvalid = true;
                    break;
                }
            }
            if (!alreadyInvalid) {
                invalidMines[invalidCount++] = closestGlobalMine;
            }
        }

        return (
            Rat.rc.isMovementReady() &&
            VisionManager.enemies.length == 0 && 
            RNG.rng.nextInt(100) < Constants.RAT_KING_MOVE_SCORE &&
            RatKing.turnsSinceLastCat >= Constants.ROUNDS_AFTER_CAT &&
            Rat.rc.getRoundNum() - KingManager.lastSeenEnemyRound > Constants.ROUNDS_AFTER_ENEMY &&
            Rat.distTo(new MapLocation(Rat.W / 2, Rat.H / 2)) >= Constants.SAFE_DISTANCE_SQUARED_FROM_CENTER &&
            Gain.getAverage() < Constants.NEUTRAL_CHEESE_GAIN_THERSHOLD
        );
    }

    public static void moveToCheese() throws GameActionException {
        MapLocation centerOfMines = centerOfUncontrolledMines();
        if (VisionManager.globalCheeseMines.length == 0 || centerOfMines == null) {
            MapLocation target = new MapLocation(Rat.W / 2, Rat.H / 2);
            fuzzyMove(Rat.dirTo(target));
        } else {
            MapLocation closestRatKing = Rat.rc.getLocation();
            for (MapLocation ratKing: VisionManager.allAllyRatKingLocations) {
                if (ratKing.distanceSquaredTo(centerOfMines) < Rat.distTo(centerOfMines)) {
                    closestRatKing  = ratKing;
                }
            }

            if (Rat.distTo(closestRatKing) <= 8) {
                MapLocation targetLoc = centerOfMines;
                fuzzyMove(Rat.dirTo(targetLoc));
            } else {
                MapLocation backup = closestGlobalMine;
                closestGlobalMine = null;
    
                for (MapLocation loc: VisionManager.globalCheeseMines) {
                    if (validMine(loc)) {
                        if (closestGlobalMine == null) {
                            closestGlobalMine = loc;
                        } else if (Rat.distTo(loc) < Rat.distTo(closestGlobalMine)){
                            closestGlobalMine = loc;
                        }
                    }
                    for (MapLocation ref: MapData.possibleReflections(loc)) {
                        if (ref == null) continue;
                        if (closestGlobalMine == null) {
                            closestGlobalMine = ref;
                        } else if (Rat.distTo(ref) < Rat.distTo(closestGlobalMine)){
                            closestGlobalMine = ref;
                        }                   
                    }
                }
    
                if (closestGlobalMine == null) {
                    closestGlobalMine = backup;
                    System.out.println("RESTORED BACKUP");
                }
                fuzzyMove(Rat.dirTo(closestGlobalMine));
            }
        }
    }   

    public static void stutterMove() throws GameActionException {
        if (Rat.rc.isMovementReady()) {
            RobotInfo[] nearAllies = Rat.rc.senseNearbyRobots(9, Rat.rc.getTeam());
            for (RobotInfo ally: nearAllies) {
                if (ally.getRawCheeseAmount() > 0) {
                    Nav.fuzzyMove(Rat.dirTo(ally.getLocation()));
                    return;
                }
            }
            if (Rat.rc.isMovementReady()) {
                if (VisionManager.closestCheese != null) {
                    Nav.fuzzyMove(Rat.dirTo(VisionManager.closestCheese.getMapLocation()));
                }
            }
        }
    }

    public static MapLocation centerOfUncontrolledMines() throws GameActionException {
        MapLocation originalSpawn = Global.readSpawn();
        Mine.sortLocations();

        int x = 0;
        int y = 0;
        int cnt = 0;
        // debug.printMapLocArray(VisionManager.globalCheeseMines);
        for (int i = 0; i < Mine.farCount; i++) {
            MapLocation closestRef = MapData.nearestReflectionIncludingSelf(originalSpawn, Mine.far[i]);
            x += closestRef.x; y += closestRef.y; ++cnt;
        }

        x += (Rat.W / 2) * Mine.nearCount;
        y += (Rat.H / 2) * Mine.nearCount;
        cnt += Mine.nearCount;

        if (Mine.nearCount >= 2) {
            x += Rat.W / 2;
            y += Rat.H / 2;
            cnt++;
        }
        
        if (cnt == 0) return null;
        return new MapLocation(x / cnt, y / cnt);
    }

    public static boolean validMine(MapLocation mineLoc) throws GameActionException {
        if (Mine.kingOnMine(mineLoc, false)) return false;
        for (int i = 0; i < invalidCount; i++) {
            if (invalidMines[i].equals(mineLoc)) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateNextSquare(MapLocation targetLocation) throws GameActionException {
        return true;
        // RobotController rc = Rat.rc;
        // MapLocation currentLoc = rc.getLocation();
        // Direction dirToTarget = currentLoc.directionTo(targetLocation);
        // if (dirToTarget == Direction.CENTER) return true;
        // MapLocation nextLoc = currentLoc.add(dirToTarget);
        // return (!isNextToWall(nextLoc));
    }

    public static boolean isNextToWall(MapLocation location) throws GameActionException {

        int[][] ringAround = {
                {-1, -2}, {0, -2}, {1, -2},
        {-2, -1},                         {2, -1},
        {-2,  0},                         {2,  0},
        {-2,  1},                         {2,  1},
                {-1,  2}, {0,  2}, {1,  2}
        };

        for (int[] delta : ringAround) {
            MapLocation adj = location.translate(delta[0], delta[1]);
            if (!Rat.rc.onTheMap(adj)) return true;
            // if (!rc.onTheMap(adj)) return true; // consider off map a wall
            MapInfo info = MapData.getMapInfo(adj);
            if (info != null && info.isWall()) return true;
        }

        return false;
    }

    public static void fuzzyMove(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER) return;
        for(Direction d: fuzzyDirs(dir)){
            if (validateNextSquare(Rat.rc.getLocation().add(d)))
                Nav.simpleMove(d);
        }
    }

    public static Direction[] fuzzyDirs(Direction dir) throws GameActionException{
        if (!turnRight) {
            return new Direction[]{
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight()
            };
        } else {
            return new Direction[] {
                dir,
                dir.rotateRight(),
                dir.rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
            };
        }
    }
}
