package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.util.Rng;

import static awubot.Game.*;

public class MineStrategy extends Strategy {
    private final CheeseStrategy cheeseStrategy;
    private MapLocation mineTarget;
    private MapLocation randomDestination;

    public MineStrategy(CheeseStrategy cheeseStrategy) {
        this.cheeseStrategy = cheeseStrategy;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        updateMineTarget();
        if (mineTarget == null) return;
        rc.setIndicatorLine(myLoc, mineTarget, 255, 0, 0);
        Pathfinder.makeMove(mineTarget);
    }

    private void updateMineTarget() throws GameActionException {
        for (var info : rc.senseNearbyMapInfos()) {
            if (info.hasCheeseMine()) {
                mineTarget = info.getMapLocation();
                return;
            }
        }

        if (randomDestination == null || myLoc.distanceSquaredTo(randomDestination) <= 4) {
            randomDestination = new MapLocation(
                Math.abs(Rng.mcg()) % mapWidth,
                Math.abs(Rng.mcg()) % mapHeight
            );
        }
        mineTarget = randomDestination;
    }

    public MapLocation getMineLocation() {
        return mineTarget;
    }
}
