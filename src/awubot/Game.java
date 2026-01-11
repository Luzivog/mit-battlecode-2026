package awubot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import awubot.test.UtilTest;
import awubot.util.FastRng;
import awubot.util.Profiler;
import awubot.util.Util;
import awubot.util.Util.*;
import awubot.util.string.FastIntIntMap;
import awubot.util.string.FastIntSet;
import awubot.util.string.FastIndexedIntSet;
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
    public static MapLocation home;

    public static void init(RobotController rc) {
        Game.rc = rc;
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        loc = rc.getLocation();
        locX = loc.x;
        locY = loc.y;
        time = rc.getRoundNum();
        isRatKing = (rc.getType() == UnitType.RAT_KING);
        home = loc;

        StringBuilder set = new StringBuilder('|');

        Profiler.begin();
        set.append(10 + "|");
        Profiler.end("set.append(10 + \"|\")");

        Profiler.begin();
        boolean has = set.indexOf("|" + 10 + "|") >= 0;
        Profiler.end("boolean has = set.indexOf(\"|\" + 10 + \"|\") >= 0;");

        double a = 0.1, b = 0.2;
        Profiler.begin();
        double c = a + b;
        Profiler.end_print("bruh");

        // Profiler.begin();
        // char c = (char) a;
        // Profiler.end();

        // Profiler.begin();
        // MapLocation pos = new MapLocation(1, 2);
        // Profiler.end();

        // Profiler.begin();
        // String s = "^" + (char) pos.x + (char) pos.y;
        // Profiler.end();

        // Profiler.begin();
        // Profiler.end();

        //rc.disintegrate();
    }

    public static void preTick() {
        time = rc.getRoundNum();
    }
}
