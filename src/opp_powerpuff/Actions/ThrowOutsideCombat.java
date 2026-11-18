package opp_powerpuff.Actions;

import opp_powerpuff.Helpers.BugNav;
import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Helpers.Vision;
import opp_powerpuff.Managers.VisionManager;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class ThrowOutsideCombat {
    private static int turnsCarried = 0;
    
    public static void toss() throws GameActionException {
        if (Rat.rc.getCarrying() == null)
            turnsCarried = 0;
        else {
            if (!Rat.rc.isActionReady()) {
                if (Rat.rc.canTurn() && VisionManager.closestEnemy != null) {
                    Direction toEnemy = Rat.dirTo(VisionManager.closestEnemy.getLocation());
                    if (toEnemy != Direction.CENTER) Rat.rc.turn(toEnemy);
                }
                BugNav.moveTo(VisionManager.allyRatKingLocation);
            }
            if (turnsCarried > 3) {
                throwPriorityList();
            }
            else {
                turnsCarried ++;
            }
        }
        
    }
    public static void throwPriorityList() throws GameActionException{
        if (throwAtCat()) {
            turnsCarried = 0;
            return;
        }
        if (throwAtWall()) {
            turnsCarried = 0;
            return;
        }
        if (throwAwayFromAllyKing()) {
            turnsCarried = 0;
            return;
        }
    }
    public static boolean throwAtWall() throws GameActionException {
        MapInfo wall = Vision.getClosestWallAllDirections(Rat.rc.getLocation(), Rat.rc.getMapWidth(), Rat.rc.getMapHeight());
        if (wall != null) {
            Direction d = Rat.rc.getLocation().directionTo(wall.getMapLocation());
            if (Rat.rc.canTurn()) {
                Rat.rc.turn(d);
            }
            MapInfo adjFront = Rat.rc.senseMapInfo(Rat.rc.getLocation().add(Rat.rc.getDirection()));
            RobotInfo robotInFront = Rat.rc.senseRobotAtLocation(Rat.rc.getLocation().add(Rat.rc.getDirection()));
            if (!Rat.rc.onTheMap(adjFront.getMapLocation()) || adjFront.isDirt() || adjFront.isWall() || robotInFront != null) {
                if (Rat.rc.canMove(Rat.rc.getDirection().opposite()))
                    Rat.rc.move(Rat.rc.getDirection().opposite());
                else
                    return false;
            }
            else if (Rat.rc.getLocation().distanceSquaredTo(wall.getMapLocation()) > 4 && Rat.rc.canMove(d)) {
                Rat.rc.move(d);
            }
            if (Rat.rc.canThrowRat()) {
                Rat.rc.throwRat();
                return true;
            }
        }
        return false;
    }
    public static boolean throwAtCat() throws GameActionException {
        if (VisionManager.lastSeenClosestCat == null)
            return false;
        if (Rat.rc.getLocation().distanceSquaredTo(VisionManager.lastSeenClosestCat.getLocation()) > 49)
            return false;
        if (Rat.rc.canThrowRat() && Rat.rc.canTurn()) {
            Rat.rc.turn(Rat.rc.getLocation().directionTo(VisionManager.lastSeenClosestCat.getLocation()));
            MapInfo adjFront = Rat.rc.senseMapInfo(Rat.rc.getLocation().add(Rat.rc.getDirection()));
            RobotInfo robotInFront = Rat.rc.senseRobotAtLocation(Rat.rc.getLocation().add(Rat.rc.getDirection()));
            if (!Rat.rc.onTheMap(adjFront.getMapLocation()) ||  adjFront.isDirt() || adjFront.isWall() || robotInFront != null) {
                if (Rat.rc.canMove(Rat.rc.getDirection().opposite()))
                    Rat.rc.move(Rat.rc.getDirection().opposite());
                else
                    return false;
            }
            Rat.rc.throwRat();
            return true;
        }
        return false;
    }
    public static boolean throwAwayFromAllyKing() throws GameActionException {
        Direction d = Rat.rc.getLocation().directionTo(VisionManager.allyRatKingLocation).opposite();
        if (Rat.rc.canTurn() && Rat.rc.canThrowRat()) {
            Rat.rc.turn(d);
            MapInfo adjFront = Rat.rc.senseMapInfo(Rat.rc.getLocation().add(Rat.rc.getDirection()));
            RobotInfo robotInFront = Rat.rc.senseRobotAtLocation(Rat.rc.getLocation().add(Rat.rc.getDirection()));
            if (!Rat.rc.onTheMap(adjFront.getMapLocation()) || adjFront.isDirt() || adjFront.isWall() || robotInFront != null) {
                if (Rat.rc.canMove(Rat.rc.getDirection().opposite()))
                    Rat.rc.move(Rat.rc.getDirection().opposite());
                else
                    return false;
            }
            Rat.rc.throwRat();
            return true;
        }
        return false;
    }
}
