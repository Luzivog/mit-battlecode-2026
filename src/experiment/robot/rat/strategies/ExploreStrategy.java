package experiment.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import experiment.Pathfinder;
import experiment.robot.Strategy;
import experiment.robot.rat.Rat;
import experiment.util.Rng;
import static experiment.Game.mapHeight;
import static experiment.Game.mapWidth;
import static experiment.Game.rc;

public class ExploreStrategy extends Strategy {
    private final Rat rat;
    private MapLocation exploreTarget;

    public ExploreStrategy(Rat rat) {
        this.rat = rat;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        updateExploreTarget();
        rc.setIndicatorLine(rc.getLocation(), exploreTarget, 255, 0, 0);
        Pathfinder.makeMove(exploreTarget);
    }

    private void updateExploreTarget() {
        if (exploreTarget != null
            && rc.getLocation().distanceSquaredTo(exploreTarget) > 2) return;
        int x = Rng.mcg() % mapWidth;
        int y = Rng.mcg() % mapHeight;
        exploreTarget = new MapLocation(x, y);
    }
}
