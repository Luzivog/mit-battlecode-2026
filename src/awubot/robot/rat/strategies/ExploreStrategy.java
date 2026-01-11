package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.robot.rat.Rat;
import static awubot.Game.*;

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
        exploreTarget = new MapLocation(
            (int)(Math.random() * rc.getMapWidth()),
            (int)(Math.random() * rc.getMapHeight())
        );
    }
}
