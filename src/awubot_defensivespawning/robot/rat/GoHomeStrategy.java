package awubot_defensivespawning.robot.rat;

import battlecode.common.*;
import awubot_defensivespawning.Pathfinder;
import awubot_defensivespawning.comms.GlobalArray;
import awubot_defensivespawning.robot.Strategy;
import static awubot_defensivespawning.Game.*;
import static awubot_defensivespawning.util.Util.*;

public class GoHomeStrategy extends Strategy {
    private int king;

    public GoHomeStrategy() {
        king = GlobalArray.nearestRatKingId;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getRawCheese() == 0) return false;
        indicate("going home");
        // TODO: handle when king dies
        var kingLoc = GlobalArray.getRatKing(king);

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
