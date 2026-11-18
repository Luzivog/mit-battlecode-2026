package opp_parmesan.Utils.AdditionalClasses;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Threat {
    public static MapLocation loc;
    public static Direction dir;
    public static boolean threat;
    public Threat(MapLocation m, Direction d, boolean t) {
        loc = m;
        dir = d;
        threat = t;
    }
}