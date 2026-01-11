package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.UnitType;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.Game;

import static awubot.robot.rat.BabyRat.*;

public class GoHomeStrategy extends Strategy {
    private final CheeseStrategy cheeseStrategy;
    private MapLocation home;
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
        for (var robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                home = robot.getLocation();
                break;
            }
        }
        rc.setIndicatorLine(rc.getLocation(), home, 0, 255, 0);
        Pathfinder.makeMove(home);
        var dir = loc.directionTo(home);
        var transferTarget = loc.add(dir);
        if(loc.distanceSquaredTo(home) <= 8) {
            shouldGlance = false;
            if(rc.canTurn()) Pathfinder.turn(dir);
        }
        if (rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
            rc.transferCheese(transferTarget, rc.getRawCheese());
        }
    }

    public void setHome(MapLocation home) {
        this.home = home;
    }

    public void setGoingHome(boolean goingHome) {
        this.goingHome = goingHome;
    }

    private boolean shouldGoHome() {
        if (goingHome) return true;
        if (rc.getRawCheese() > 0 && cheeseStrategy.getLastSeenCheese() < rc.getRoundNum() - 5) return true;
        var opt = 25 / Math.sqrt((rc.getRawCheese() / 20) * Math.sqrt(home.distanceSquaredTo(Game.loc)));
        indicate(opt);
        if (opt < 1) return true;
        return false;
    }
}
