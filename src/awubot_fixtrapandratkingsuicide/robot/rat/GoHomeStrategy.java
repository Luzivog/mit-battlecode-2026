package awubot_fixtrapandratkingsuicide.robot.rat;

import battlecode.common.*;
import awubot_fixtrapandratkingsuicide.Pathfinder;
import awubot_fixtrapandratkingsuicide.comms.GlobalArray;
import awubot_fixtrapandratkingsuicide.robot.Strategy;
import static awubot_fixtrapandratkingsuicide.Game.*;
import static awubot_fixtrapandratkingsuicide.util.Util.*;

public class GoHomeStrategy extends Strategy {
    private MapLocation kingLoc;

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getRawCheese() == 0) return false;
        indicate("going home");
        if(kingLoc == null) {
            kingLoc = GlobalArray.nearestRatKing;
        } else if(!GlobalArray.nearestRatKing.equals(kingLoc)) {
            if(GlobalArray.nearestRatKing.distanceSquaredTo(kingLoc) <= 8) {
                // the king moved
                kingLoc = kingLoc.add(kingLoc.directionTo(GlobalArray.nearestRatKing));
            } else {
                // different king
                kingLoc = GlobalArray.nearestRatKing;
            }
        }

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
