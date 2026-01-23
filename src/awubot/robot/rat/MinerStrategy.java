package awubot.robot.rat;

import battlecode.common.*;
import awubot.CellTracker;
import awubot.Game;
import awubot.robot.Strategy;
import awubot.comms.GlobalArray;
import awubot.util.Rng;
import static awubot.Game.*;
import static awubot.util.Util.*;



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

    public static boolean deterministicRand(int p, int q) {
        // return rc.getID() % q < p;
        return Rng.mcg(0, p - 1) < q;
    }

    public static boolean shouldExplore() {
        int p, q;
        int numKnownMines = Game.knownCheeseMines.set.length();
        int perim = mapWidth + mapHeight;
        if (numKnownMines == 0) {
            p = 1; 
            q = 1;
        } else if (time < 2 * perim) {
            p = 1;
            q = 2;
        } else if (time < 4 * perim) {
            p = 1;
            q = 2;
        } else {
            p = 1;
            q = 3;
        }
        return deterministicRand(p, q);
    }

    public static MapLocation pickExploreTarget() {
        return new MapLocation(
            Rng.mcg(5, mapWidth - 5),
            Rng.mcg(5, mapHeight - 5)
        );
    }

    @Override
    public boolean runTick() throws GameActionException {
        // RECOMMENT TO ENABLE

        // indicate all known cheese mines
        // for (int i = 0; i < Game.knownCheeseMines.set.length(); i++) {
        //     indicate(decode2x2(Game.knownCheeseMines.at(i)));
        // }
        
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

            if (shouldExplore()) {
                target = pickExploreTarget();
            } else {
                target = pickSeenMine();
            }
            goSomewhereStrategy = new TraverseStrategy(target, 2);
        }

        return true;
    }

    private MapLocation pickSeenMine() {
        // int mineCount = Game.knownCheeseMines.set.length();
        // double[] weights = new double[mineCount];
        // int weightCount = 0;
        // double totalWeight = 0;
        // for (int i = 0; i < mineCount; i++) {
        //     double weight = 1.0 / (double) myLoc.distanceSquaredTo(decode2x2(Game.knownCheeseMines.at(i)));
        //     weights[weightCount++] = weight;
        //     totalWeight += weight;
        // }
        // double rnd = (((double) Rng.mcg() / ((1 << 31) - 1)) * totalWeight);
        // double pref = 0;
        // for (int i = 0; i < weightCount; i++) {
        //     pref += weights[i];
        //     if (rnd <= pref) return decode2x2(Game.knownCheeseMines.at(i));
        // }
        return decode2x2(Game.knownCheeseMines.at(Rng.mcg(0, Game.knownCheeseMines.set.length() - 1)));
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
