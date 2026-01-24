package awubot_quals.robot.rat;

import battlecode.common.*;
import awubot_quals.CellTracker;
import awubot_quals.Game;
import awubot_quals.robot.Strategy;
import awubot_quals.comms.GlobalArray;
import awubot_quals.util.Rng;
import static awubot_quals.Game.*;
import static awubot_quals.util.Util.*;

import java.util.ArrayList;
import java.util.List;

public class MinerStrategy extends Strategy {
    private static final int CRITICAL_HP = 20;

    private FormationStrategy formationStrategy;
    private GoHomeStrategy goHomeStrategy;
    private CheeseStrategy cheeseStrategy;
    private TraverseStrategy goSomewhereStrategy; // either go to a mine or explore; TODO: make the mine strategy smarter

    public MinerStrategy() {
        formationStrategy = new FormationStrategy();
        goHomeStrategy = new GoHomeStrategy();
        cheeseStrategy = new CheeseStrategy();
    }

    @Override
    public boolean runTick() throws GameActionException {
        // RECOMMENT TO ENABLE
        
        if(rc.getRawCheese() == 0) {
            goHomeStrategy = null;
        } else if(goHomeStrategy == null && shouldGoHome()) {
            goHomeStrategy = new GoHomeStrategy();
            cheeseStrategy.cheeseTarget = null;
        }
        if(tryStrategy(goHomeStrategy)) return true;

        if(tryStrategy(formationStrategy)) return true;

        if(tryStrategy(cheeseStrategy)) return true;

        while(goSomewhereStrategy == null || !goSomewhereStrategy.runTick()) {
            MapLocation target;

            if (Game.knownCheeseMines.set.length() < 3 || Rng.mcg() % 5 == 0) {
                target = new MapLocation(
                    Math.abs(Rng.mcg()) % mapWidth,
                    Math.abs(Rng.mcg()) % mapHeight
                );
            } else {
                target = pickSeenMine();
            }
            goSomewhereStrategy = new TraverseStrategy(target, 2);
        }

        return true;
    }

    private MapLocation pickSeenMine() {
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;
        for (int i = 0; i < Game.knownCheeseMines.set.length(); i++) {
            weights.add(1.0 / (double) myLoc.distanceSquaredTo(decode2x2(Game.knownCheeseMines.at(i))));
            totalWeight += weights.get(weights.size() - 1);
        }
        double rnd = (((double) Rng.mcg() / ((1 << 31) - 1)) * totalWeight);
        double pref = 0;
        for (int i = 0; i < Game.knownCheeseMines.set.length(); i++) {
            pref += weights.get(i);
            if (rnd <= pref) return decode2x2(Game.knownCheeseMines.at(i));
        }
        return decode2x2(Game.knownCheeseMines.at(Game.knownCheeseMines.set.length() - 1));
    }

    private boolean shouldGoHome() {
        if (shouldReturnForHealth()) return true;

        int cheese = rc.getRawCheese();
        int distSq = myLoc.distanceSquaredTo(GlobalArray.nearestRatKing);
        int thresh;
        if(distSq <= 25) thresh = 10;
        else if(distSq <= 100) thresh = 25;
        else if(distSq <= 225) thresh = 40;
        else if(distSq <= 400) thresh = 60;
        else if(distSq <= 900) thresh = 80;
        else thresh = 105;
        
        if (cheese >= thresh) return true;
        if (cheese * 2 >= thresh && cheeseStrategy.lastSeenCheese < time - 3) return true;

        int globalCheese = rc.getGlobalCheese();
        if (globalCheese < 100 && cheese >= 20) return true;
        if (globalCheese < 30 && cheese > 0) return true;

        return false;
    }

    private boolean shouldReturnForHealth() {
        int hp = rc.getHealth();
        int cheese = rc.getRawCheese();

        if (cheese == 0) return false;
        if (hp <= CRITICAL_HP) return true;

        int hpThreshold;
        if (cheese >= 150) hpThreshold = 70;
        else if (cheese >= 100) hpThreshold = 60;
        else if (cheese >= 50) hpThreshold = 50;
        else if (cheese >= 20) hpThreshold = 40;
        else return false;

        return hp <= hpThreshold;
    }
}
