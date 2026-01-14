package awubot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.UnitType;
import static awubot.util.Util.*;

import awubot.comms.GlobalArray;

public class Game {
    public static RobotController rc;
    public static int mapWidth, mapHeight;

    // Cached for cheaper access
    public static MapLocation myLoc;
    public static int myLocX, myLocY;
    public static int time;
    public static Direction myDir;
    public static Team team;
    public static int myIndex; // estimate of rat index

    public static MapLocation spawn;

    public static void rawMove(Direction dir) throws GameActionException {
        if(dir == myDir) {
            rawMove();
            return;
        }
        rc.move(dir);
        myLoc = rc.getLocation();
        myLocX = myLoc.x;
        myLocY = myLoc.y;
        // IF TLE: optimise this
        CellTracker.storeVisibleCells();
    }

    public static void turn(Direction dir) throws GameActionException {
        assert dir != Direction.CENTER;
        if (dir == myDir) return;
        rc.turn(dir);
        myDir = dir;
    }

    public static void rawMove() throws GameActionException {
        rc.moveForward();
        myLoc = rc.getLocation();
        myLocX = myLoc.x;
        myLocY = myLoc.y;
        CellTracker.storeVisibleCellsPostMoveForward();
    }

    public static boolean isRatKing;
    public static int kingId;

    public static void init(RobotController rc) {
        Game.rc = rc;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        myLoc = rc.getLocation();
        myLocX = myLoc.x;
        myLocY = myLoc.y;
        myDir = rc.getDirection();
        time = rc.getRoundNum();
        isRatKing = (rc.getType() == UnitType.RAT_KING);
        team = rc.getTeam();
        spawn = myLoc;
    }

    public static void preTick() throws GameActionException {
        time = rc.getRoundNum();
        myLoc = rc.getLocation();
        myLocX = myLoc.x;
        myLocY = myLoc.y;
        myDir = rc.getDirection();
        // Clear indicator string at the start of each turn
        indicatorString = "";
    }

    public static void spawnRat(MapLocation loc, int strat) throws GameActionException {
        rc.buildRat(loc);
        GlobalArray.writeSpawnStrategy(rc.senseRobotAtLocation(loc).ID, strat);
    }

    public static void postTick() {
        // if (!isRatKing) println("index: " + myIndex);
        // if (time != rc.getRoundNum()) {
        //     println("bytecode exceeded");
        //     rc.resign();
        // }
    }
}
