package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.util.Util;

import static awubot.Game.*;
import static awubot.util.Util.indicate;

public class ExploreStrategy extends Strategy {
    private MapLocation exploreTarget;

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        updateExploreTarget();
        rc.setIndicatorLine(myLoc, exploreTarget, 255, 0, 0);
        Pathfinder.makeMove(exploreTarget);
    }

    private void updateExploreTarget() {
        if (exploreTarget != null
            && myLoc.distanceSquaredTo(exploreTarget) > 2) return;

        if (GlobalArray.cheeseMines.set.length() < 3 || (int)(Math.random() * 3) % 3 == 0) {
            exploreTarget = new MapLocation(
                (int)(Math.random() * rc.getMapWidth()),
                (int)(Math.random() * rc.getMapHeight())
            );
        } else {
            indicate("nextmine: " + MinerStrategy.nextMine);
            exploreTarget = Util.decode2x2(GlobalArray.cheeseMines.at(MinerStrategy.nextMine));
            MinerStrategy.nextMine++;
            MinerStrategy.nextMine %= GlobalArray.cheeseMines.set.length();
        }
    }
}
