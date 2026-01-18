package awubot_defensivespawning.robot.king;

import battlecode.common.GameActionException;
import awubot_defensivespawning.robot.Strategy;
import awubot_defensivespawning.util.Profiler;

import static awubot_defensivespawning.Game.*;
import static awubot_defensivespawning.util.Util.*;

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
