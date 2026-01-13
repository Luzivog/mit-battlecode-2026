package awubot.robot.king;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class KingCheeseStrategy extends Strategy {
    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        for (int myDx = -2; myDx <= 2; myDx++) {
            for (int myDy = -2; myDy <= 2; myDy++) {
                MapLocation myCheesePos = new MapLocation(myLoc.x + myDx, myLoc.y + myDy);
                if (rc.canPickUpCheese(myCheesePos)) {
                    rc.pickUpCheese(myCheesePos);
                }
            }
        }
    }
}
