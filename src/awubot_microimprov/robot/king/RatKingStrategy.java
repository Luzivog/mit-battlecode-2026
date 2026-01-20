package awubot_microimprov.robot.king;

import battlecode.common.GameActionException;
import awubot_microimprov.robot.Strategy;
import awubot_microimprov.util.Profiler;

import static awubot_microimprov.Game.*;
import static awubot_microimprov.util.Util.*;

public class RatKingStrategy extends Strategy {
    private KingSpawningStrategy spawningStrategy;
    private KingCombatStrategy combatStrategy;
    private KingMiningStrategy miningStrategy;

    public RatKingStrategy() {
        spawningStrategy = new KingSpawningStrategy();
        combatStrategy = new KingCombatStrategy();
        miningStrategy = new KingMiningStrategy();
    }

    @Override
    public boolean runTick() throws GameActionException {
        spawningStrategy.runTick();
        combatStrategy.runTick();
        miningStrategy.runTick();
        return true;
    }
}
