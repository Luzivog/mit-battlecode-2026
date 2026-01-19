package awubot_fixratthrowing_old.robot.king;

import battlecode.common.GameActionException;
import awubot_fixratthrowing_old.robot.Strategy;
import awubot_fixratthrowing_old.util.Profiler;

import static awubot_fixratthrowing_old.Game.*;
import static awubot_fixratthrowing_old.util.Util.*;

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
