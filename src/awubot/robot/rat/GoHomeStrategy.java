package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

import static awubot.robot.rat.BabyRat.*;

public class GoHomeStrategy extends Strategy {
    private MapLocation kingLoc;

    @Override
    public boolean isComplete() {
        return rc.getRawCheese() == 0;
    }

    @Override
    public void runTick() throws GameActionException {
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

        if (myLoc.distanceSquaredTo(kingLoc) <= 8) {
            shouldGlance = false;
            if (dir != Direction.CENTER && rc.canTurn()) turn(dir);
        }

        if (rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
            rc.transferCheese(transferTarget, rc.getRawCheese());
        }
    }
}
