package awubot_fixratthrowing_old.test;

import battlecode.common.*;
import static awubot_fixratthrowing_old.util.Util.*;

import awubot_fixratthrowing_old.comms.GlobalArray;

import static awubot_fixratthrowing_old.Game.*;

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
