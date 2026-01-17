package awu_old_naman.test;

import battlecode.common.*;
import static awu_old_naman.util.Util.*;

import awu_old_naman.comms.GlobalArray;

import static awu_old_naman.Game.*;

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
