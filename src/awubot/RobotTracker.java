package awubot;

import battlecode.common.*;

public class RobotTracker {
    public static int allyCount = 0;
    public static MapLocation[] allies = new MapLocation[120];
    public static void addAlly(MapLocation loc) {
        allies[allyCount++] = loc;
    }

    public static void reset() {
        allyCount = 0;
    }
}
