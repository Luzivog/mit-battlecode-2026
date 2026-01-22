package awubot_mystery.robot.rat;

import battlecode.common.*;
import awubot_mystery.Pathfinder;
import awubot_mystery.comms.GlobalArray;
import awubot_mystery.robot.Strategy;
import static awubot_mystery.Game.*;
import static awubot_mystery.util.Util.*;

public class GoHomeStrategy extends Strategy {
    private int king;

    public GoHomeStrategy() {
        king = GlobalArray.nearestRatKingId;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getRawCheese() == 0) return false;
        indicate("going home");
        
        var kingLoc = GlobalArray.getRatKing(king);
        
        if (kingLoc == null) {
            // king died, try to find another king
            king = GlobalArray.nearestRatKingId;
            kingLoc = GlobalArray.nearestRatKing;
            if (kingLoc == null) return false;
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
