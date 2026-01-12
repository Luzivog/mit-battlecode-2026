package awubot.robot.rat.strategies;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.robot.Strategy;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class MinerStrategy extends Strategy {
    private final GoHomeStrategy goHomeStrategy;
    private final CheeseStrategy cheeseStrategy;
    private final MineStrategy mineStrategy;
    private final Strategy exploreStrategy;

    public static int nextMine = 0;

    public MinerStrategy() {
        cheeseStrategy = new CheeseStrategy();
        goHomeStrategy = new GoHomeStrategy(cheeseStrategy);
        mineStrategy = new MineStrategy(cheeseStrategy);
        exploreStrategy = new ExploreStrategy();
    }

    private void tryStrats() throws GameActionException {
        indicate("amount " + rc.getRawCheese());
        indicate("mineLoc " + mineStrategy.getMineLocation());
        if (rc.getRawCheese() == 0) {
            goHomeStrategy.setGoingHome(false);
        }
        if (goHomeStrategy.tryStrategy(goHomeStrategy)) return;
        if (cheeseStrategy.tryStrategy(cheeseStrategy)) return;
        if (mineStrategy.tryStrategy(mineStrategy)) return;
        exploreStrategy.tryStrategy(exploreStrategy);
    }

    @Override
    public void runTick() throws GameActionException {
        CellTracker.checkSymmetry();
        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        tryStrats();
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}

