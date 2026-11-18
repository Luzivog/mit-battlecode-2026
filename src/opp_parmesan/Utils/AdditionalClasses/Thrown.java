package opp_parmesan.Utils.AdditionalClasses;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Thrown {
    public MapLocation loc;
    public Direction dir;
    public Thrown (MapLocation m, Direction d) {
        loc = m;
        dir = d;
    }
    public boolean isSafe (MapLocation check) {
        if (check.equals(loc)) return false;
        check = check.subtract(dir);
        if (check.equals(loc)) return false;
        check = check.subtract(dir);
        if (check.equals(loc)) return false;
        return true;
    }
}
