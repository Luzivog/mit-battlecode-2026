package awubot.test;

import battlecode.common.*;
import static awubot.util.Util.*;

import awubot.comms.GlobalArray;

import static awubot.Game.*;

public class UtilTest {
    public static void test() {
        locX = 12;
        locY = 23;
        GlobalArray.writeRatLoc(1);
        MapLocation read = GlobalArray.getRat(1);
        println(locX, locY, read.x, read.y);
        assert(read.x == (locX / 2 * 2) && read.y == (locY / 2 * 2));
    }
}
