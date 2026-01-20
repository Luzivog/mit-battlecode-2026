package awubot_pp.robot.king;

import battlecode.common.GameActionException;
import awubot_pp.robot.Strategy;
import awubot_pp.util.Profiler;

import static awubot_pp.Game.*;
import static awubot_pp.util.Util.*;

public class RatKingStrategy extends Strategy {
    private KingSpawningStrategy spawningStrategy;
    private KingCombatStrategy combatStrategy;
    private KingMiningStrategy miningStrategy;
    private ThreatContext threatContext;
    private KingFormationCoordinatorStrategy formationStrategy;

    public RatKingStrategy() {
        spawningStrategy = new KingSpawningStrategy();
        combatStrategy = new KingCombatStrategy();
        miningStrategy = new KingMiningStrategy();
        threatContext = new ThreatContext();
        formationStrategy = new KingFormationCoordinatorStrategy(threatContext, miningStrategy);
    }

    @Override
    public boolean runTick() throws GameActionException {
        threatContext.scanThreats();
        threatContext.updateDefenseState();
        spawningStrategy.runTick();
        combatStrategy.runTick();
        formationStrategy.runTick();
        miningStrategy.runTick();
        return true;
    }
}
