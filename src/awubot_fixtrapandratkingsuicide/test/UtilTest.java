package awubot_fixtrapandratkingsuicide.test;

import battlecode.common.*;
import static awubot_fixtrapandratkingsuicide.util.Util.*;

import awubot_fixtrapandratkingsuicide.comms.GlobalArray;

import static awubot_fixtrapandratkingsuicide.Game.*;

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
