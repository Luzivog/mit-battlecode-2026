package awubot.robot.rat.strategies;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

import static awubot.robot.rat.BabyRat.*;

public class GoHomeStrategy extends Strategy {
    private static final int CRITICAL_HP = 20;

    private final CheeseStrategy cheeseStrategy;
    private boolean goingHome;

    public GoHomeStrategy(CheeseStrategy cheeseStrategy) {
        this.cheeseStrategy = cheeseStrategy;
    }

    @Override
    public boolean isComplete() {
        return !shouldGoHome();
    }

    @Override
    public void runTick() throws GameActionException {
        goingHome = true;
        indicate("going home");

        rc.setIndicatorLine(myLoc, GlobalArray.nearestRatKing, 0, 255, 0);
        Pathfinder.makeMove(GlobalArray.nearestRatKing);

        var dir = myLoc.directionTo(GlobalArray.nearestRatKing);
        var transferTarget = myLoc.add(dir);

        if (myLoc.distanceSquaredTo(GlobalArray.nearestRatKing) <= 8) {
            shouldGlance = false;
            if (dir != Direction.CENTER && rc.canTurn()) turn(dir);
        }

        if (rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
            rc.transferCheese(transferTarget, rc.getRawCheese());
        }
    }

    public void setGoingHome(boolean goingHome) {
        this.goingHome = goingHome;
    }

    public boolean isGoingHome() {
        return goingHome && rc.getRawCheese() > 0;
    }

    private boolean shouldGoHome() {
        if (goingHome) return true;
        if (shouldReturnForHealth()) return true;

        int cheese = rc.getRawCheese();
        if (cheese >= 200) return true;
        if (cheese > 50 && cheeseStrategy.getLastSeenCheese() < rc.getRoundNum() - 3) return true;

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

    public boolean isEmergencyMode() {
        return rc.getHealth() <= CRITICAL_HP && rc.getRawCheese() > 0;
    }
}
