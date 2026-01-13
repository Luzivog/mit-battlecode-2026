package awubot.robot.king;

import battlecode.common.GameActionException;
import awubot.robot.Strategy;
import awubot.util.Profiler;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKingStrategy extends Strategy {
    private final ThreatContext theContext;
    private final KingCheeseStrategy theCheeseStrategy;
    private final KingMiningStrategy theMiningStrategy;
    private final KingSpawningStrategy theSpawningStrategy;
    private final KingDefenseStrategy theDefenseStrategy;
    private final KingFormationCoordinatorStrategy theFormationStrategy;

    public RatKingStrategy() {
        theContext = new ThreatContext();
        theCheeseStrategy = new KingCheeseStrategy();
        theMiningStrategy = new KingMiningStrategy();
        theMiningStrategy.init();
        // Profiler.begin();
        theSpawningStrategy = new KingSpawningStrategy(theContext);
        // Profiler.end("spawning");
        // Profiler.begin();
        theDefenseStrategy = new KingDefenseStrategy(theContext, theMiningStrategy);
        // Profiler.end("defence");
        // Profiler.begin();
        theFormationStrategy = new KingFormationCoordinatorStrategy(theContext, theMiningStrategy);
        // Profiler.end("spawning");
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        theContext.scanThreats();
        theContext.updateDefenseState();

        theDefenseStrategy.runTick();

        if (theDefenseStrategy.isDefensiveActionTaken() || theDefenseStrategy.wantsToFlee()) {
            theCheeseStrategy.runTick();
            theSpawningStrategy.runTick();
            return;
        }

        theCheeseStrategy.runTick();
        theMiningStrategy.runTick();
        theSpawningStrategy.runTick();
        theFormationStrategy.runTick();
    }
}
