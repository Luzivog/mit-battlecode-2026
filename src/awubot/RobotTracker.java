package awubot;

import awubot.util.string.FastMapLocationSet;
import battlecode.common.*;
import static awubot.Game.*;

public class RobotTracker {
    public static int allyCount = 0;
    public static MapLocation[] allies = new MapLocation[55];
    private static FastMapLocationSet allySet;
    public static void addAlly(MapLocation loc) {
        if(!allySet.contains(loc)) {
            allies[allyCount++] = loc;
            allySet.add(loc);
        }
    }

    public static void reset() throws GameActionException {
        allyCount = 0;
        allySet = new FastMapLocationSet();
        for(var robot : rc.senseNearbyRobots(-1, team)) {
            var loc = robot.location;
            if(robot.type == UnitType.BABY_RAT) {
                addAlly(loc);
            } else {
                var locX = loc.x;
                var locY = loc.y;
                addAlly(new MapLocation(locX + -1, locY + -1));
                addAlly(new MapLocation(locX + -1, locY + 0));
                addAlly(new MapLocation(locX + -1, locY + 1));
                addAlly(new MapLocation(locX + 0, locY + -1));
                addAlly(new MapLocation(locX + 0, locY + 0));
                addAlly(new MapLocation(locX + 0, locY + 1));
                addAlly(new MapLocation(locX + 1, locY + -1));
                addAlly(new MapLocation(locX + 1, locY + 0));
                addAlly(new MapLocation(locX + 1, locY + 1));
            }
        }
    }
}