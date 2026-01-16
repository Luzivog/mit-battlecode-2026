package awubot;

import awubot.util.string.FastMapLocationSet;
import battlecode.common.*;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class RobotTracker {
    public static int allyCount = 0;
    public static MapLocation[] allies = new MapLocation[55];
    public static FastMapLocationSet allySet, kingSet;
    private static MapLocation[][] pastKingLocs = new MapLocation[5][5];
    private static int[] pastKingCounts = new int[5];
    public static void addAlly(MapLocation loc) {
        if(!allySet.contains(loc)) {
            allies[allyCount++] = loc;
            allySet.add(loc);
        }
    }

    // IF TLE: optimise this
    public static void reset() throws GameActionException {
        allyCount = 0;
        allySet = new FastMapLocationSet();
        pastKingCounts[time % 5] = 0;
        for(var robot : rc.senseNearbyRobots(-1, team)) {
            var loc = robot.location;
            if(robot.type == UnitType.BABY_RAT) {
                addAlly(loc);
            } else {
                pastKingLocs[time % 5][pastKingCounts[time % 5]++] = loc;
            }
        }
    }
    public static void writeHistoricalKingLocs() {
        // hack to remember where kings are
        kingSet = new FastMapLocationSet();
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < pastKingCounts[i]; j++) {
                var loc = pastKingLocs[i][j];
                var locX = loc.x;
                var locY = loc.y;
                {
                    var kloc = new MapLocation(locX + -1, locY + -1);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + -1, locY + 0);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + -1, locY + 1);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + 0, locY + -1);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + 0, locY + 0);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + 0, locY + 1);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + 1, locY + -1);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + 1, locY + 0);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
                {
                    var kloc = new MapLocation(locX + 1, locY + 1);
                    addAlly(kloc);
                    kingSet.add(kloc);
                }
            }
        }
    }
}
