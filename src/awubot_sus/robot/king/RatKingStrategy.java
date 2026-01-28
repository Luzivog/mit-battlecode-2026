package awubot_sus.robot.king;

import battlecode.common.GameActionException;
import awubot_sus.robot.Strategy;
import awubot_sus.util.Profiler;

import static awubot_sus.Game.*;
import static awubot_sus.util.Util.*;

public class RatKingStrategy extends Strategy {
    private KingSpawningStrategy spawningStrategy;
    private KingCombatStrategy combatStrategy;
    private KingMiningStrategy miningStrategy;
    private KingFormationCoordinatorStrategy formationStrategy;

    public RatKingStrategy() {
        spawningStrategy = new KingSpawningStrategy();
        combatStrategy = new KingCombatStrategy();
        miningStrategy = new KingMiningStrategy();
        formationStrategy = new KingFormationCoordinatorStrategy(miningStrategy);
    }

    @Override
    public boolean runTick() throws GameActionException {
        spawningStrategy.runTick();
        combatStrategy.runTick();
        formationStrategy.runTick();
        miningStrategy.runTick();
        return true;
    }
}
