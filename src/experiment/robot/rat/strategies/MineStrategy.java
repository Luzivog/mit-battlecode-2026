package experiment.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import experiment.Pathfinder;
import experiment.robot.Strategy;
import experiment.robot.rat.Rat;
import experiment.util.Rng;
import static experiment.Game.rc;

public class MineStrategy extends Strategy {
    private final Rat rat;
    private final CheeseStrategy cheeseStrategy;
    private MapLocation mineLocation;
    private MapLocation mineTarget;

    public MineStrategy(Rat rat, CheeseStrategy cheeseStrategy) {
        this.rat = rat;
        this.cheeseStrategy = cheeseStrategy;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        refreshMineLocation();
        return mineLocation == null;
    }

    @Override
    public void runTick() throws GameActionException {
        updateMineTarget();
        rc.setIndicatorLine(rc.getLocation(), mineTarget, 255, 0, 0);
        Pathfinder.makeMove(mineTarget);
    }

    private void refreshMineLocation() throws GameActionException {
        if (mineLocation != null && cheeseStrategy.getLastSeenCheese() >= rc.getRoundNum() - 20) return;
        for (var info : rc.senseNearbyMapInfos()) {
            if (info.hasCheeseMine()) {
                mineLocation = info.getMapLocation();
                break;
            }
        }
    }

    private void updateMineTarget() throws GameActionException {
        if (mineTarget != null && rc.getLocation().distanceSquaredTo(mineTarget) > 2) return;
        MapLocation nextTarget;
        do {
            nextTarget = new MapLocation(
                mineLocation.x + Rng.mcg() % 7 - 3,
                mineLocation.y + Rng.mcg() % 7 - 3
            );
        } while (!rc.onTheMap(nextTarget));
        mineTarget = nextTarget;
    }

    public MapLocation getMineLocation() {
        return mineLocation;
    }
}
