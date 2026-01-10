package awubot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import awubot.test.UtilTest;
import awubot.util.BudgetMap;
import awubot.util.FastRng;
import awubot.util.Profiler;
import battlecode.common.*;

public class Game {
    public static RobotController rc;
    public static int mapWidth, mapHeight;

    // cheaper to access this way
    public static MapLocation loc;
    public static int locX, locY;
    public static int time;

    public static void init(RobotController rc) {
        Game.rc = rc;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        locX = rc.getLocation().x;
        locY = rc.getLocation().y;
        time = rc.getRoundNum();

        // BudgetMap.test();

        Profiler.begin();
        FastRng.nextInt();
        Profiler.end();

        // Profiler.begin();
        // Profiler.end();

        // Profiler.begin();
        // BudgetMap.get(0, 0);
        // Profiler.end();

        // Profiler.begin();
        // Profiler.end();

        // Profiler.begin();
        // Profiler.end();

        rc.disintegrate();
    }
}
