package awubot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.UnitType;
import static awubot.util.Util.*;

import awubot.comms.GlobalArray;
import awubot.util.Profiler;

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

    public static void _rawMove(Direction dir) throws GameActionException {
        rc.move(dir);
        myLoc = rc.getLocation();
        myLocX = myLoc.x;
        myLocY = myLoc.y;
    }

    public static void _turn(Direction dir) throws GameActionException {
        assert dir != Direction.CENTER;
        if (dir == myDir) return;
        rc.turn(dir);
        myDir = dir;
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

        // Direction dir = Direction.NORTHEAST;
        // int[] arr = new int[2];

        // boolean a = false, b = false;
        // Profiler.begin();
        // if (a || b);
        // Profiler.end_print("combined");
        
        // Profiler.begin();
        // if (a);
        // if (b);
        // Profiler.end_print("separate");

        // rc.resign();
        // Profiler.begin();
        // int x = arr[dir.ordinal()];
        // Profiler.end_print("c1");

        // Profiler.begin();
        // int y;
        // switch (dir) {
        //     case NORTH:
        //         y = 0;
        //         break;
        //     case NORTHEAST:
        //         y = 1;
        //         break;
        //     case EAST:
        //         y = 2;
        //         break;
        //     default:
        //         y = -1;
        //         break;
        // };
        // Profiler.end_print("c2");

        // int z;
        // int i = 2;
        // Profiler.begin();
        // switch (i) {
        //     case 0:
        //         z = 0;
        //         break;
        //     case 1:
        //         z = 1;
        //         break;
        //     case 2:
        //         z = 2;
        //         break;
        //     case 3:
        //         z = 3;
        //         break;
        //     case 4:
        //         z = 4;
        //         break;
        //     default:
        //         z = -1;
        //         break;
        // }
        // Profiler.end_print("c3");
        // rc.resign();
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

    public static void postTick() throws GameActionException {

        Profiler.begin();
        MapInfo info = rc.senseMapInfo(myLoc);
        Profiler.end_print("sense");

        Profiler.begin();
        boolean asdf = rc.senseMapInfo(new MapLocation(myLocX, myLocY)).isPassable();
        Profiler.end_print("passable");

        Profiler.begin();
        boolean fdas = CellTracker.isPassable(myLocX, myLocY);
        Profiler.end_print("celltracker");

        StringBuilder sb = new StringBuilder();
        sb.repeat('1', 1000);
        Profiler.begin();
        int ans = sb.charAt(myLocX * 60 + myLocY);
        Profiler.end_print("thoery");

        rc.resign();

        // if (!isRatKing) println("index: " + myIndex);
        // if (time > 100) rc.resign();
        // if (time != rc.getRoundNum()) {
        //     println("bytecode exceeded");
        //     rc.resign();
        // }
    }
}
