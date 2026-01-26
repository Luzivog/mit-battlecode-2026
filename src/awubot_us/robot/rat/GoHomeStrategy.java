

package awubot_us.robot.rat;

import battlecode.common.*;
import awubot_us.Pathfinder;
import awubot_us.comms.GlobalArray;
import awubot_us.robot.Strategy;
import static awubot_us.Game.*;
import static awubot_us.util.Util.*;
import static awubot_us.robot.rat.BabyRat.*;

public class GoHomeStrategy extends Strategy {
    private int king;

    public GoHomeStrategy() {
        king = GlobalArray.nearestRatKingId;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getRawCheese() == 0) return false;
        indicate("going home");

                if(rc.getRawCheese() != 0 && rc.canTurn()) {
            var nearestKingLoc = GlobalArray.nearestRatKing;
            int dx = Integer.compare(myLoc.x, nearestKingLoc.x);
            int dy = Integer.compare(myLoc.y, nearestKingLoc.y);
            var transferTarget = new MapLocation(nearestKingLoc.x + dx, nearestKingLoc.y + dy);
            if(transferTarget.distanceSquaredTo(myLoc) <= 9) {
                turn(myLoc.directionTo(transferTarget));
                return false;
            }
        }


        var kingLoc = GlobalArray.getRatKing(king);

        if (kingLoc == null) {
            // king died, try to find another king
            king = GlobalArray.nearestRatKingId;
            kingLoc = GlobalArray.nearestRatKing;
            if (kingLoc == null) return false;
        }

        rc.setIndicatorLine(myLoc, kingLoc, 0, 255, 0);
        Pathfinder.makeMove(kingLoc);

                if(rc.getRawCheese() != 0 && rc.canTurn()) {
            var nearestKingLoc = GlobalArray.nearestRatKing;
            int dx = Integer.compare(myLoc.x, nearestKingLoc.x);
            int dy = Integer.compare(myLoc.y, nearestKingLoc.y);
            var transferTarget = new MapLocation(nearestKingLoc.x + dx, nearestKingLoc.y + dy);
            if(transferTarget.distanceSquaredTo(myLoc) <= 9) {
                turn(myLoc.directionTo(transferTarget));
                return false;
            }
        }

        return true;
    }
}