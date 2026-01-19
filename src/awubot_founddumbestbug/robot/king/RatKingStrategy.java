package awubot_founddumbestbug.robot.king;

import battlecode.common.GameActionException;
import awubot_founddumbestbug.robot.Strategy;
import awubot_founddumbestbug.util.Profiler;

import static awubot_founddumbestbug.Game.*;
import static awubot_founddumbestbug.util.Util.*;

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
