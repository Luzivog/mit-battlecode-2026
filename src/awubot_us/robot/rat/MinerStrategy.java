package awubot_us.robot.rat;

import battlecode.common.*;
import awubot_us.CellTracker;
import awubot_us.Game;
import awubot_us.robot.Strategy;
import awubot_us.comms.GlobalArray;
import awubot_us.util.Rng;
import awubot_us.util.string.FastIndexedIntSet;
import static awubot_us.Game.*;
import static awubot_us.util.Util.*;

import java.util.ArrayList;
import java.util.List;

public class MinerStrategy extends Strategy {
    private static final int CRITICAL_HP = 20;

    private FormationStrategy formationStrategy;
    private GoHomeStrategy goHomeStrategy;
    private CheeseStrategy cheeseStrategy;
    private TraverseStrategy goSomewhereStrategy; // either go to a mine or explore; TODO: make the mine strategy smarter

    private FastIndexedIntSet bannedMines;

    public MinerStrategy() {
        formationStrategy = new FormationStrategy();
        goHomeStrategy = new GoHomeStrategy();
        cheeseStrategy = new CheeseStrategy();
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(GlobalArray.nearestRatKing.distanceSquaredTo(myLoc) <= 25) {
            bannedMines = new FastIndexedIntSet();
        }
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

            int knownMines = Game.knownCheeseMines.set.length();
            if(knownMines == 0
               || knownMines <= 2 && time <= mapWidth * mapHeight / 8) {
                target = pickExplorationTarget();
            } else {
                // var p = Math.exp(-12.0 * time / (mapWidth * mapHeight));
                var p = 0.2;
                if(Rng.mcg() < p * (1l << 31)) {
                    target = pickExplorationTarget();
                } else {
                    target = pickSeenMine();
                }
            }
            goSomewhereStrategy = new TraverseStrategy(target, 2);
        }

        return true;
    }

    private MapLocation pickSeenMine() {
        System.out.println("picking seen mine");
        boolean nearKing = GlobalArray.nearestRatKing.distanceSquaredTo(myLoc) <= 25;
        var mines = new StringBuilder();
        outerLoop:
        for(int i = Game.knownCheeseMines.set.length() - 1; i >= 0; i--) {
            var iloc = Game.knownCheeseMines.at(i);
            var loc = decode2x2(iloc);
            if(canSense(myLoc, loc, myDir)) continue;
            if(bannedMines.contains(iloc)) continue;
            for(int j = 0; j < 5; j++) {
                var kingLoc = GlobalArray.getRatKing(j);
                if(kingLoc != null && kingLoc.distanceSquaredTo(loc) <= 2) {
                    continue outerLoop;
                }
            }
            // if(nearKing) {
            //     mines.append((char)iloc);
            // } else {
            mines.repeat(iloc, Math.min(100, Math.max(1, (int)(1000 / myLoc.distanceSquaredTo(loc)))));
            // }
        }
        if(mines.length() == 0) {
            return pickExplorationTarget();
        }
        var chosen = mines.charAt(Rng.mcg() % mines.length());
        bannedMines.add(chosen);
        return decode2x2(chosen);
    }

    private MapLocation pickExplorationTarget() {
        System.out.println("picking exploration target");
        MapLocation target = null;
        outerLoop:
        for(int i = 5; i >= 0; i--) { // tle guard
            target = new MapLocation(Rng.mcg() % mapWidth, Rng.mcg() % mapHeight);
            // not seen
            if(CellTracker.isSeen(target)) continue;
            // not too close to a king
            for(int j = 0; j < 5; j++) {
                var kingLoc = GlobalArray.getRatKing(j);
                if(kingLoc != null && kingLoc.distanceSquaredTo(target) <= 36) {
                    continue outerLoop;
                }
            }
            return target;
        }
        return target;
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
