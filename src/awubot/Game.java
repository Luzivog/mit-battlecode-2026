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
import awubot.util.string.FastLocSet;
import battlecode.common.*;

public class Game {
    public static RobotController rc;
    public static int mapWidth, mapHeight;

    // cheaper to access this way
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

        // BudgetMap.test();

        // FastLocSet fls = new FastLocSet();
        // for (int i = 0; i < 20; i++) fls.add(new MapLocation(i, i));

        // Profiler.begin();
        // fls.add(1, 2);
        // Profiler.end();

        // Profiler.begin();
        // boolean x = fls.contains(new MapLocation(1, 2));
        // Profiler.end();

        // Profiler.begin();
        // MapLocation pos = new MapLocation(1, 2);
        // Profiler.end();

        // Profiler.begin();
        // String s = "^" + (char) pos.x + (char) pos.y;
        // Profiler.end();

        // Profiler.begin();
        // Profiler.end();

        // rc.disintegrate();
    }

    public static void preTick() {
        time = rc.getRoundNum();
    }
}
