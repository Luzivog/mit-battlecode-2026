package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class GoHomeStrategy extends Strategy {
    private MapLocation kingLoc;

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getRawCheese() == 0) return false;
        indicate("going home");
        if(kingLoc == null) {
            kingLoc = GlobalArray.nearestRatKing;
        }
        // TODO: handle when king dies

        rc.setIndicatorLine(myLoc, kingLoc, 0, 255, 0);
        Pathfinder.makeMove(kingLoc);

        var dir = myLoc.directionTo(kingLoc);
        var transferTarget = myLoc.add(dir);

        if (rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
            rc.transferCheese(transferTarget, rc.getRawCheese());
        }
        return true;
    }
}
