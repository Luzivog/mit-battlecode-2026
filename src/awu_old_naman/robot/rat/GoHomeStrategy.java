package awu_old_naman.robot.rat;

import battlecode.common.*;
import awu_old_naman.Pathfinder;
import awu_old_naman.comms.GlobalArray;
import awu_old_naman.robot.Strategy;
import static awu_old_naman.Game.*;
import static awu_old_naman.util.Util.*;

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
