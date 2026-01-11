package awubot.robot.rat.strategies;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.CellTracker;
import awubot.robot.Strategy;

import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Rng;

public class MinerStrategy extends Strategy {
    private Direction lastGlance;
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
        if (rc.canTurn()) {
            indicate("glancing");
            var leftDir = rc.getDirection().rotateLeft().rotateLeft();
            var rightDir = rc.getDirection().rotateRight().rotateRight();
            if (lastGlance == leftDir) {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            } else if (lastGlance == rightDir || Rng.mcg() >> 30 != 0) {
                Pathfinder.turn(leftDir);
                lastGlance = leftDir;
            } else {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            }
        }
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}

