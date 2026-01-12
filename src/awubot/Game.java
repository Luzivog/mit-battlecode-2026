package awubot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.UnitType;
import static awubot.util.Util.*;

public class Game {
    public static RobotController rc;
    public static int mapWidth, mapHeight;

    // Cached for cheaper access
    public static MapLocation loc;
    public static int locX, locY;
    public static int time;
    public static Direction facing;
    public static Team team;
    public static MapLocation home;

    public static void rawMove(Direction dir) throws GameActionException {
        if(dir == facing) {
            rawMove();
            return;
        }
        rc.move(dir);
        // IF TLE: optimise this
        CellTracker.storeVisibleCells();
        loc = rc.getLocation();
        locX = loc.x;
        locY = loc.y;
    }

    public static void turn(Direction dir) throws GameActionException {
        assert dir != Direction.CENTER;
        if (dir == facing) return;
        rc.turn(dir);
        facing = dir;
    }

    public static void rawMove() throws GameActionException {
        rc.moveForward();
        CellTracker.storeVisibleCellsPostMoveForward();
        loc = rc.getLocation();
        locX = loc.x;
        locY = loc.y;
    }

    public static boolean isRatKing;
    public static int kingId;

    public static void init(RobotController rc) {
        Game.rc = rc;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        locX = rc.getLocation().x;
        locY = rc.getLocation().y;
        loc = rc.getLocation();
        facing = rc.getDirection();
        time = rc.getRoundNum();
        isRatKing = (rc.getType() == UnitType.RAT_KING);
        team = rc.getTeam();
    }

    public static void preTick() {
        time = rc.getRoundNum();
        loc = rc.getLocation();
        locX = loc.x;
        locY = loc.y;
        facing = rc.getDirection();
        // Clear indicator string at the start of each turn
        indicatorString = "";
    }
}
