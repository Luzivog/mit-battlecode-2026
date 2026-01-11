package experiment.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import experiment.Pathfinder;
import experiment.robot.Strategy;
import experiment.robot.rat.Rat;
import static experiment.Game.rc;

public class CheeseStrategy extends Strategy {
    private final Rat rat;
    private MapLocation cheeseTarget;
    private int lastSeenCheese;

    public CheeseStrategy(Rat rat) {
        this.rat = rat;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        refreshCheeseTarget();
        return cheeseTarget == null;
    }

    @Override
    public void runTick() throws GameActionException {
        lastSeenCheese = rc.getRoundNum();
        if (rc.getLocation().isAdjacentTo(cheeseTarget)) {
            Pathfinder.turn(rc.getLocation().directionTo(cheeseTarget));
            if (rc.isActionReady()) {
                rc.pickUpCheese(cheeseTarget);
                cheeseTarget = null;
            }
            return;
        }
        rc.setIndicatorLine(rc.getLocation(), cheeseTarget, 255, 0, 0);
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
