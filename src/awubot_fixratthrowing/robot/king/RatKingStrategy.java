package awubot_fixratthrowing.robot.king;

import battlecode.common.GameActionException;
import awubot_fixratthrowing.robot.Strategy;
import awubot_fixratthrowing.util.Profiler;

import static awubot_fixratthrowing.Game.*;
import static awubot_fixratthrowing.util.Util.*;

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
