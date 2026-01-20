package awubot_microtrapless.test;

import battlecode.common.*;
import static awubot_microtrapless.util.Util.*;

import awubot_microtrapless.comms.GlobalArray;

import static awubot_microtrapless.Game.*;

public class UtilTest {
    public static void test() {
        myLocX = 12;
        myLocY = 23;
        GlobalArray.writeRatLoc(1);
        MapLocation read = GlobalArray.getRatKing(1);
        println(myLocX, myLocY, read.x, read.y);
        assert(read.x == (myLocX / 2 * 2) && read.y == (myLocY / 2 * 2));
    }
}
