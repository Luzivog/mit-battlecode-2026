package awubot.robot.king;

import battlecode.common.GameActionException;
import awubot.robot.Strategy;
import awubot.util.Profiler;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class RatKingStrategy extends Strategy {
    private KingSpawningStrategy spawningStrategy;
    private KingCombatStrategy combatStrategy;
    private KingMiningStrategy miningStrategy;
    private KingFormationCoordinatorStrategy coordinatorStrategy;

    public RatKingStrategy() {
        spawningStrategy = new KingSpawningStrategy();
        combatStrategy = new KingCombatStrategy();
        miningStrategy = new KingMiningStrategy();
        coordinatorStrategy = new KingFormationCoordinatorStrategy(new ThreatContext(), miningStrategy);
    }

    @Override
    public boolean runTick() throws GameActionException {
        spawningStrategy.runTick();
        combatStrategy.runTick();
        miningStrategy.runTick();
        coordinatorStrategy.runTick();
        return true;
    }
}
