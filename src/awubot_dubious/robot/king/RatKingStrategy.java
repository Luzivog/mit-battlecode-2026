package awubot_dubious.robot.king;

import battlecode.common.GameActionException;
import awubot_dubious.robot.Strategy;
import awubot_dubious.util.Profiler;

import static awubot_dubious.Game.*;
import static awubot_dubious.util.Util.*;

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
