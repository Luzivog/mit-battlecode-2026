package awubot_dspawn.robot.king;

import battlecode.common.GameActionException;
import awubot_dspawn.robot.Strategy;
import awubot_dspawn.util.Profiler;

import static awubot_dspawn.Game.*;
import static awubot_dspawn.util.Util.*;

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
