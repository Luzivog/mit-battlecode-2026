package opp_obg;

import battlecode.common.MapLocation;

public class Target {

    static final int EXPLORATION = 0;
    static final int CHEESE_MINE = 1;
    static final int RAT_KING = 2;
    static final int NEW_RAT_KING = 3;
    static final int CHEESE = 4;
    static final int ENEMY = 5;

    MapLocation location;
    int roundAssigned;
    int priority;
    int dist;

    Target (int priority, MapLocation location){
        this.priority = priority;
        this.location = location;
        this.roundAssigned = MyUnit.rc.getRoundNum();
        this.dist = MyUnit.rc.getLocation().distanceSquaredTo(location);
    }

    boolean isBetterThan(Target B){
        if (B == null) return true;
        if (priority == B.priority){
            if (priority == RAT_KING) return true;
            return dist < B.dist;
        }
        return priority > B.priority;
    }

}
