package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

// TODO: go after a different target if given the opportunity
// this strategy doesn't pick up cheese (that's handled in BabyRat
// since we want to pick up in any circumstances since it doesn't cost action cd)
public class CheeseStrategy extends Strategy {
    public MapLocation cheeseTarget;
    public int lastSeenCheese;

    @Override
    public boolean runTick() throws GameActionException {
        refreshCheeseTarget();
        if(cheeseTarget == null) return false;
        lastSeenCheese = rc.getRoundNum();
        rc.setIndicatorLine(myLoc, cheeseTarget, 255, 0, 0);
        Pathfinder.makeMove(cheeseTarget);
        return true;
    }

    private void refreshCheeseTarget() throws GameActionException {
        if (cheeseTarget != null
            && rc.canSenseLocation(cheeseTarget)
            && rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0) {
            cheeseTarget = null;
        }
        int bestAmount = 0;
        for (var info : rc.senseNearbyMapInfos()) {
            if(cheeseTarget == null && info.getCheeseAmount() > bestAmount) {
                cheeseTarget = info.getMapLocation();
                bestAmount = info.getCheeseAmount();
            }
        }
    }
}
