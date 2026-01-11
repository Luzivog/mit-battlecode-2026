package experiment;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.UnitType;

public class Game {
    public static RobotController rc;
    public static int mapWidth, mapHeight;

    // Cached for cheaper access
    public static MapLocation loc;
    public static int locX, locY;
    public static int time;
    public static boolean isRatKing;
    public static int kingId;

    public static void init(RobotController rc) {
        Game.rc = rc;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        locX = rc.getLocation().x;
        locY = rc.getLocation().y;
        loc = rc.getLocation();
        time = rc.getRoundNum();
        isRatKing = (rc.getType() == UnitType.RAT_KING);
    }

    public static void preTick() {
        time = rc.getRoundNum();
        loc = rc.getLocation();
        locX = loc.x;
        locY = loc.y;
        // Clear indicator string at the start of each turn
        experiment.util.Util.indicatorString = "";
    }
}
