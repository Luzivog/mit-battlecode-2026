package opp_powerpuff.Debug;

import java.util.Arrays;

import battlecode.common.*;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Rat;

public class debug {
    public static void printGlobalArray() throws GameActionException {
        String x = "GLOBAL ARRAY: ";
        for (int i = 0; i < 64; i ++) {
            x += i + ": (" + Rat.rc.readSharedArray(i) + ") ";
        }
        System.out.println(x);
    }

    public static void printMapLocArray(MapLocation[] arr) throws GameActionException {
        String x = "MapLocs: ";
        for (MapLocation m: arr) {
            x += m + ", ";
        } System.out.println(x);
    }
}
