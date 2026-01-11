package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.UnitType;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.robot.rat.Rat;
import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.Game;

public class GoHomeStrategy extends Strategy {
    private final Rat rat;
    private final CheeseStrategy cheeseStrategy;
    private MapLocation home;
    private boolean goingHome;

    public GoHomeStrategy(Rat rat, CheeseStrategy cheeseStrategy) {
        this.rat = rat;
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
        var transferTarget = rc.getLocation().add(rc.getLocation().directionTo(home));
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
        var opt = Math.sqrt(2 * Math.sqrt(home.distanceSquaredTo(Game.loc)));
        if (rc.getRawCheese() >= opt * 20) return true;
        if (rc.getRawCheese() >= 60 && cheeseStrategy.getLastSeenCheese() < rc.getRoundNum() - 4) return true;
        return false;
    }
}
