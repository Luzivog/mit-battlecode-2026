package awubot.robot.rat.strategies;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import static awubot.Game.*;

public class CheeseStrategy extends Strategy {
    private MapLocation cheeseTarget;
    private int lastSeenCheese;

    @Override
    public boolean isComplete() throws GameActionException {
        refreshCheeseTarget();
        return cheeseTarget == null;
    }

    @Override
    public void runTick() throws GameActionException {
        lastSeenCheese = rc.getRoundNum();
        if (myLoc.isAdjacentTo(cheeseTarget)) {
            var dir = myLoc.directionTo(cheeseTarget);
            if (dir != Direction.CENTER && rc.canTurn()) turn(dir);
            if (rc.canPickUpCheese(cheeseTarget)) {
                rc.pickUpCheese(cheeseTarget);
                cheeseTarget = null;
            }
            return;
        }
        rc.setIndicatorLine(myLoc, cheeseTarget, 255, 0, 0);
        Pathfinder.makeMove(cheeseTarget);
    }

    private void refreshCheeseTarget() throws GameActionException {
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
    }

    public int getLastSeenCheese() {
        return lastSeenCheese;
    }
}
