package opp_powerpuff;

import battlecode.common.*;
import opp_powerpuff.Helpers.*;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.RatKing.KingManager;
import opp_powerpuff.Utils.RNG;

abstract public class Rat {
    public static RobotController rc;
    public static MapLocation currentLocation;
    public static MapLocation spawnLocation;

    protected enum RatState {
        SPAWN
    }

    public static int W;
    public static int H;

    protected RatState state = null;
    protected RatState prevState = null;

    public static int spawnRound;

    public Rat(RobotController robot) throws GameActionException {
        rc = robot;
        spawnRound = rc.getRoundNum();
        spawnLocation = rc.getLocation();
        W = rc.getMapWidth();
        H = rc.getMapHeight();

        // ORDER MATTERS
        RNG.init();
        // NAV INITIALIZATION
        BugNav.RIGHT = rc.getID() % 2 == 0;
        BugNav.rlook = rc.getRoundNum() % 2 == 0;
    }

    // ==========================================================================

    public void start() throws GameActionException {
        currentLocation = rc.getLocation(); 
        
    };

    abstract public void turn() throws GameActionException;
    abstract public void end() throws GameActionException;

    // ==========================================================================

    public static int distTo(MapLocation loc) throws GameActionException {
        return rc.getLocation().distanceSquaredTo(loc);
    }

    public static Direction dirTo(MapLocation loc) throws GameActionException {
        return rc.getLocation().directionTo(loc);
    }

    public static int startDuration() throws GameActionException {
        return (int)((Rat.H + Rat.W) * 1.5f);
    }
}
