package opp_powerpuff.RatKing;

import java.awt.Robot;

import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Rats.RatKing;
import opp_powerpuff.Utils.Calc;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Escape {
    public static void fleeCat() throws GameActionException {        
        if (!Rat.rc.isMovementReady()) return;
        Direction fleeDirection = null;

        if (VisionManager.closestCat == null) {
            if (RatKing.turnsSinceLastCat < Constants.ROUNDS_AFTER_CAT)
                fleeDirection = getFleeDirection(Rat.dirTo(VisionManager.lastSeenClosestCat.getLocation()).opposite());
        }
        else
            fleeDirection = getFleeDirection(Rat.rc.getLocation().directionTo(VisionManager.closestCat.getLocation()).opposite());
        
        if (fleeDirection == null || fleeDirection == Direction.CENTER) return;

        removeDirtInWay(fleeDirection);
        Nav.fuzzyMove(fleeDirection);
    }

    public static void fleeEnemy() throws GameActionException {
        if (VisionManager.closestEnemy != null) {
            Direction fleeDirection = getFleeDirection((Rat.dirTo(VisionManager.closestEnemy.getLocation()).opposite()));
            Nav.fuzzyMove(fleeDirection);
        }
    }

    public static Direction getFleeDirection(Direction direction) throws GameActionException {

        RobotController rc = Rat.rc;
        MapLocation currentLoc = rc.getLocation();
        Direction[] dirs = new Direction[] {
            direction,
            direction.rotateLeft().rotateLeft(),
            direction.rotateLeft().rotateRight()
        };

        for (Direction d : dirs) {
            if (d == Direction.CENTER) continue;

            MapLocation next = currentLoc.add(d);

            if (violatesSymmetry(next))
                continue;

            return d;
        }

        return direction;
    }


    public static boolean violatesSymmetry(MapLocation nextLoc) throws GameActionException {

        RobotController rc = Rat.rc;
        int mapWidth = Rat.W;
        int mapHeight = Rat.H;
        int symmetry = MapData.symmetry;

        MapLocation spawnLoc = Global.readSpawn();
        MapLocation currentLoc = rc.getLocation();

        int originalSideX = getLineSide(spawnLoc.x, mapWidth / 2);
        int originalSideY = getLineSide(spawnLoc.y, mapHeight / 2);

        int nextSideX = getLineSide(nextLoc.x, mapWidth / 2);
        int nextSideY = getLineSide(nextLoc.y, mapHeight / 2);
        int currSideX = getLineSide(currentLoc.x, mapWidth / 2);
        int currSideY = getLineSide(currentLoc.y, mapHeight / 2);

        if (symmetry == 1) { // reflect across y
            if (currSideX == originalSideX &&
                nextSideX != 0 &&
                nextSideX != originalSideX)
                return true;

        } else if (symmetry == 2) { // reflect across x
            if (currSideY == originalSideY &&
                nextSideY != 0 &&
                nextSideY != originalSideY)
                return true;

        } else if (symmetry == 4) { // rotational
            if (nextSideX == -originalSideX &&
                nextSideY == -originalSideY)
                return true;
        }

        return false;
    }

    public static int getLineSide(int dot, int mid) {
        if (dot < mid) return -1; // left side, bottom side
        if (dot > mid) return 1;  // right side, top side
        return 0;                 // on line
    }


    public static void unClog() throws GameActionException {
        RobotController rc = Rat.rc;
        if (!rc.isMovementReady()) return;

        MapLocation cur = rc.getLocation();
        if (!couldBeClogging(cur)) return;

        boolean[] walls = getWallSides(cur);

        // diagonal escape
        if (walls[0] && walls[2]) { // top and left
            Nav.simpleMove(Direction.NORTHWEST);
            return;
        }
        if (walls[0] && walls[3]) { // top and right
            Nav.simpleMove(Direction.NORTHEAST);
            return;
        }
        if (walls[1] && walls[2]) { // bottom and left
            Nav.simpleMove(Direction.SOUTHWEST);
            return;
        }
        if (walls[1] && walls[3]) { // bottom and right
            Nav.simpleMove(Direction.SOUTHEAST);
            return;
        }

        // cardinal escape
        if (walls[0]) {
            Nav.simpleMove(Direction.SOUTH);
            return;
        }
        if (walls[1]) {
            Nav.simpleMove(Direction.NORTH);
            return;
        }
        if (walls[2]) {
            Nav.simpleMove(Direction.EAST);
            return;
        }
        if (walls[3]) {
            Nav.simpleMove(Direction.WEST);
            return;
        }

    }
    
    public static boolean couldBeClogging(MapLocation loc) throws GameActionException {
        int wallSides = countWallSides(loc);
        return wallSides >= 2;
    }

    public static int countWallSides(MapLocation loc) throws GameActionException {
        boolean top = false, bottom = false, left = false, right = false;
        for (int dx = -1; dx <= 1; dx++)
            if (isWall(loc.translate(dx, -2))) top = true;
        for (int dx = -1; dx <= 1; dx++)
            if (isWall(loc.translate(dx, 2))) bottom = true;
        for (int dy = -1; dy <= 1; dy++)
            if (isWall(loc.translate(-2, dy))) left = true;
        for (int dy = -1; dy <= 1; dy++)
            if (isWall(loc.translate(2, dy))) right = true;

        int count = 0;
        if (top) count++;
        if (bottom) count++;
        if (left) count++;
        if (right) count++;
        return count;
    }
        
    public static boolean[] getWallSides(MapLocation loc) throws GameActionException {
        boolean top = false, bottom = false, left = false, right = false;

        for (int dx = -1; dx <= 1; dx++) {
            if (isWall(loc.translate(dx, -2))) top = true;
            if (isWall(loc.translate(dx,  2))) bottom = true;
        }

        for (int dy = -1; dy <= 1; dy++) {
            if (isWall(loc.translate(-2, dy))) left = true;
            if (isWall(loc.translate( 2, dy))) right = true;
        }

        return new boolean[]{ top, bottom, left, right };
    }

    public static boolean isWall(MapLocation p) throws GameActionException {
        if (!Rat.rc.onTheMap(p)) return true; // off map counts as wall

        MapInfo info = MapData.getMapInfo(p);
        return info != null && info.isWall();
    }


    public static void blockEnemyWithDirt(MapLocation enemyLoc) throws GameActionException {
        RobotController rc = Rat.rc;
        if (!rc.isActionReady()) return;
        if (enemyLoc == null) return;

        Direction toEnemy = rc.getLocation().directionTo(enemyLoc);
        MapLocation blockLoc = rc.getLocation().add(toEnemy).add(toEnemy);
        if (rc.canPlaceDirt(blockLoc)) {
            rc.placeDirt(blockLoc);
            return;
        }

        blockLoc = rc.getLocation().add(toEnemy).add(toEnemy.rotateLeft());
        if (rc.canPlaceDirt(blockLoc)) {
            rc.placeDirt(blockLoc);
            return;
        }

        blockLoc = rc.getLocation().add(toEnemy).add(toEnemy.rotateRight());
        if (rc.canPlaceDirt(blockLoc)) {
            rc.placeDirt(blockLoc);
            return;
        }
    }

    public static void removeSurroundingDirt() throws GameActionException {
        RobotController rc = Rat.rc;
        if (rc.getAllCheese() > Constants.DANGER_CHEESE_THRESHOLD && rc.isActionReady()) {
            for (MapInfo mapInfo: VisionManager.mapInfos) {
                if (mapInfo.isDirt()) {
                    if (rc.canRemoveDirt(mapInfo.getMapLocation()))
                        rc.removeDirt(mapInfo.getMapLocation());
                }
            }
        }
    }

    public static void removeDirtInWay(Direction d) throws GameActionException {
        RobotController rc = Rat.rc;
        if (!rc.isActionReady()) return;
        if (d == null || d == Direction.CENTER) return;

        boolean diagonal = 
            d == Direction.NORTHEAST ||
            d == Direction.NORTHWEST ||
            d == Direction.SOUTHEAST ||
            d == Direction.SOUTHWEST;


        MapLocation currentLoc = rc.getLocation();
        MapLocation twoTilesAhead = currentLoc.add(d).add(d);
        MapLocation[] dirtSpots;


        if (!diagonal) { // break only 3 blocks
            dirtSpots = new MapLocation[] {
                twoTilesAhead, 
                twoTilesAhead.add(d.rotateLeft().rotateLeft()), 
                twoTilesAhead.add(d.rotateRight().rotateRight())
            };
            for (MapLocation spot : dirtSpots) {
                if (rc.canRemoveDirt(spot)) {
                    rc.removeDirt(spot);
                    return;
                }
            }
        }
        else { // break 5 blocks around
            dirtSpots = new MapLocation[] {
                twoTilesAhead,
                twoTilesAhead.add(d.opposite().rotateLeft()),
                twoTilesAhead.add(d.opposite().rotateRight()),
                twoTilesAhead.add(d.opposite().rotateLeft()).add(d.opposite().rotateLeft()),
                twoTilesAhead.add(d.opposite().rotateRight()).add(d.opposite().rotateRight())
            };
        }

        for (MapLocation spot : dirtSpots) {
            if (rc.canRemoveDirt(spot)) {
                rc.removeDirt(spot);
                return;
            }
        }
    }

    public static void killEnemyBabyRat() throws GameActionException {
        RobotController rc = Rat.rc;
        if (            
            VisionManager.enemies.length == 1 &&
            VisionManager.closestEnemy.getHealth() <= 30 &&
            rc.getAllCheese() >= 500  &&
            rc.isActionReady()
        ) {
            MapLocation closestEnemyLoc = VisionManager.closestEnemy.getLocation();
            int cheeseAmt = Calc.cheeseForDamage(VisionManager.closestEnemy.getHealth());

            if (rc.getLocation().isAdjacentTo(closestEnemyLoc)) {
                if (rc.canAttack(closestEnemyLoc, cheeseAmt)) {
                    rc.attack(closestEnemyLoc, cheeseAmt);
                    System.out.println("BOOOM");
                }
            } else {
                Direction dir = Rat.dirTo(closestEnemyLoc);
                MapLocation next = rc.getLocation().add(dir);
                if (
                    rc.canMove(dir) &&
                    next.isAdjacentTo(closestEnemyLoc) 
                ) {
                    rc.move(dir);
                    rc.attack(closestEnemyLoc, cheeseAmt);
                    System.out.println("BOOOM");
                }
            }
        }
    }
}
