package awubot_aggrosqueaks.robot.rat.goHome;

import battlecode.common.*;
import awubot_aggrosqueaks.Pathfinder;
import awubot_aggrosqueaks.comms.GlobalArray;
import awubot_aggrosqueaks.robot.Strategy;
import awubot_aggrosqueaks.robot.rat.TraverseStrategy;

import static awubot_aggrosqueaks.Game.*;
import static awubot_aggrosqueaks.util.Util.*;
import static awubot_aggrosqueaks.robot.rat.BabyRat.*;

public class GoHomeStrategy extends Strategy {
    private int king;
    private TraverseStrategy traverseStrategy = new TraverseStrategy(new MapLocation(0, 0), 0);

    public GoHomeStrategy() {
        king = GlobalArray.nearestRatKingId;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getRawCheese() == 0) return false;
        indicate("going home");
        if (rc.canTurn() && chebyshevDistance(myLoc, GlobalArray.nearestRatKing) <= 2) {
            turn(myLoc.directionTo(GlobalArray.nearestRatKing));
            return false;
        }

        var kingLoc = GlobalArray.getRatKing(king);

        if (kingLoc == null) {
            // king died, try to find another king
            king = GlobalArray.nearestRatKingId;
            kingLoc = GlobalArray.nearestRatKing;
            if (kingLoc == null) return false;
        }

        rc.setIndicatorLine(myLoc, kingLoc, 0, 255, 0);
        traverseStrategy.updateTarget(kingLoc);
        traverseStrategy.runTick();
        return true;
    }
}
