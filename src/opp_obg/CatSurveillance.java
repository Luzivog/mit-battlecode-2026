package opp_obg;

import battlecode.common.*;

public class CatSurveillance {

    static int round = -100;
    static final int CACHE_CAT_TURNS = 5;

    static class CustomCatInfo{
        MapLocation loc;
        Direction dir;
        int id;
    }

    static CustomCatInfo lastCatSeen = new CustomCatInfo();

    static void updateCatInfo(MapLocation loc, Direction dir, int id){
        lastCatSeen.loc = loc;
        lastCatSeen.dir = dir;
        lastCatSeen.id = id;
        round = MyUnit.rc.getRoundNum();
    }

    static boolean recentSighting(){
        return MyUnit.rc.getRoundNum() <= round + CACHE_CAT_TURNS;
    }

}
