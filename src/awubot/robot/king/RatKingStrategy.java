package awubot.robot.king;

import battlecode.common.GameActionException;
import awubot.robot.Strategy;
import awubot.util.Profiler;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKingStrategy extends Strategy {
    private final KingCheeseStrategy cheeseStrategy;
    private final KingMiningStrategy miningStrategy;
    private final KingSpawningStrategy spawningStrategy;

    public RatKingStrategy() {
        cheeseStrategy = new KingCheeseStrategy();
        miningStrategy = new KingMiningStrategy();
        miningStrategy.init();
        // Profiler.begin();
        spawningStrategy = new KingSpawningStrategy();
    }

    @Override
    public boolean runTick() throws GameActionException {
        cheeseStrategy.runTick();
        miningStrategy.runTick();
        spawningStrategy.runTick();
        return true;
    }
}
