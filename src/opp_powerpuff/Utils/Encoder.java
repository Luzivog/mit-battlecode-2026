package opp_powerpuff.Utils;

import battlecode.common.MapLocation;

public class Encoder {

    public static int Encode(int type, MapLocation loc) {
        return type * 10000 + loc.x * 100 + loc.y;
    }

    public static int getType(int msg) {
        return msg / 10000;
    }

    public static int getX(int msg) {
        return (msg / 100) % 100;
    }

    public static int getY(int msg) {
        return msg % 100;
    }
}
