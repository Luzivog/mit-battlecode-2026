package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.util.Rng;
import awubot.util.Util;

import static awubot.Game.*;
import static awubot.util.Util.indicate;

public class MineStrategy extends Strategy {
    private final CheeseStrategy cheeseStrategy;
    private MapLocation mineTarget;
    private MapLocation randomDestination;
    private MapLocation seenMine;

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
        // for (var info : rc.senseNearbyMapInfos()) {
        //     if (info.hasCheeseMine()) {
        //         mineTarget = info.getMapLocation();
        //         return;
        //     }
        // }
        if (randomDestination == null || myLoc.distanceSquaredTo(randomDestination) <= 4) {
            randomDestination = new MapLocation(
                Math.abs(Rng.mcg()) % mapWidth,
                Math.abs(Rng.mcg()) % mapHeight
            );
        }

        if ((seenMine == null || myLoc.distanceSquaredTo(seenMine) <= 4) && GlobalArray.cheeseMines.set.length() > 0) {
            seenMine = Util.decode2x2(GlobalArray.cheeseMines.at(Rng.mcg() % GlobalArray.cheeseMines.set.length()));
        }

        if (GlobalArray.cheeseMines.set.length() < 3 || Rng.mcg() % 5 == 0) {
            mineTarget = randomDestination;
            indicate("updated " + mineTarget + " to " + randomDestination);
        } else {
            mineTarget = seenMine;
        }
    }

    public MapLocation getMineLocation() {
        return mineTarget;
    }
}
