package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

// TODO: go after a different target if given the opportunity
public class CheeseStrategy extends Strategy {
    public MapLocation cheeseTarget;
    public int lastSeenCheese;

    @Override
    public boolean runTick() throws GameActionException {
        refreshCheeseTarget();
        if(cheeseTarget == null) return false;
        lastSeenCheese = rc.getRoundNum();
        if (myLoc.isAdjacentTo(cheeseTarget)) {
            var dir = myLoc.directionTo(cheeseTarget);
            if (dir != Direction.CENTER && rc.canTurn()) turn(dir);
            if (rc.canPickUpCheese(cheeseTarget)) {
                rc.pickUpCheese(cheeseTarget);
                cheeseTarget = null;
            }
            return true;
        }
        rc.setIndicatorLine(myLoc, cheeseTarget, 255, 0, 0);
        Pathfinder.makeMove(cheeseTarget);
        return true;
    }

    private void refreshCheeseTarget() throws GameActionException {
        indicate("HERE " + cheeseTarget);
        if (cheeseTarget != null
            && rc.canSenseLocation(cheeseTarget)
            && rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0) {
            cheeseTarget = null;
        }
        if (cheeseTarget != null) return;
        int bestAmount = 0;
        for (var info : rc.senseNearbyMapInfos()) {
            if (info.getCheeseAmount() > bestAmount) {
                cheeseTarget = info.getMapLocation();
                bestAmount = info.getCheeseAmount();
            }
        }
        indicate("New cheese target: " + cheeseTarget);
    }
}
