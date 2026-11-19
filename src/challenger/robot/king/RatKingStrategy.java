package challenger.robot.king;

import battlecode.common.*;
import challenger.robot.Strategy;
import challenger.util.Profiler;

import static challenger.Game.*;
import static challenger.util.Util.*;

public class RatKingStrategy extends Strategy {
    private KingSpawningStrategy spawningStrategy;
    private KingCombatStrategy combatStrategy;
    private KingMiningStrategy miningStrategy;
    private KingFormationCoordinatorStrategy formationStrategy;

    public RatKingStrategy() {
        spawningStrategy = new KingSpawningStrategy();
        combatStrategy = new KingCombatStrategy();
        miningStrategy = new KingMiningStrategy();
        formationStrategy = new KingFormationCoordinatorStrategy();
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
