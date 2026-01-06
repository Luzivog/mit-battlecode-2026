package awubot;

import battlecode.common.*;

public class Game {
    public static RobotController rc;
    public static int mapWidth, mapHeight;

    public static void init(RobotController rc) {
        Game.rc = rc;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
    }
}
