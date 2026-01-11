package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.util.Rng;
import awubot.util.Util;

import static awubot.Game.*;

public class MineStrategy extends Strategy {
    private final CheeseStrategy cheeseStrategy;
    private MapLocation mineLocation;
    private MapLocation mineTarget;

    public MineStrategy(CheeseStrategy cheeseStrategy) {
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
        if (GlobalArray.cheeseMines.set.length() < 3 || (int)(Math.random() * 3) % 3 == 0) {
            do {
                nextTarget = new MapLocation(
                    mineLocation.x + Rng.mcg() % 7 - 3,
                    mineLocation.y + Rng.mcg() % 7 - 3
                );
            } while (!rc.onTheMap(nextTarget));
        } else {
            nextTarget = Util.decode2x2(GlobalArray.cheeseMines.at(MinerStrategy.nextMine));
            MinerStrategy.nextMine++;
            MinerStrategy.nextMine %= GlobalArray.cheeseMines.set.length();
        }
        mineTarget = nextTarget;
    }

    public MapLocation getMineLocation() {
        return mineLocation;
    }
}
