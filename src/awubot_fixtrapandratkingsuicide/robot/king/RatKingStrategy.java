package awubot_fixtrapandratkingsuicide.robot.king;

import battlecode.common.GameActionException;
import awubot_fixtrapandratkingsuicide.robot.Strategy;
import awubot_fixtrapandratkingsuicide.util.Profiler;

import static awubot_fixtrapandratkingsuicide.Game.*;
import static awubot_fixtrapandratkingsuicide.util.Util.*;

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
