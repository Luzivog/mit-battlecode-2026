package awubot.robot.rat.strategies.king;

import battlecode.common.GameActionException;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKingStrategy extends Strategy {
    private final ThreatContext theContext;
    private final KingCheeseStrategy theCheeseStrategy;
    private final KingMiningStrategy theMiningStrategy;
    private final KingSpawningStrategy theSpawningStrategy;
    private final KingDefenseStrategy theDefenseStrategy;

    public RatKingStrategy() {
        theContext = new ThreatContext();
        theCheeseStrategy = new KingCheeseStrategy();
        theMiningStrategy = new KingMiningStrategy();
        theMiningStrategy.init();
        theSpawningStrategy = new KingSpawningStrategy(theContext);
        theDefenseStrategy = new KingDefenseStrategy(theContext, theMiningStrategy);
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

        if (theDefenseStrategy.isDefensiveActionTaken()) {
            theCheeseStrategy.runTick();
            theSpawningStrategy.runTick();
            return;
        }

        theCheeseStrategy.runTick();
        theMiningStrategy.runTick();
        theSpawningStrategy.runTick();
    }
}
