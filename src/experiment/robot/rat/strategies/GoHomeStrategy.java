package experiment.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.UnitType;
import experiment.Pathfinder;
import experiment.robot.Strategy;
import experiment.robot.rat.Rat;
import static experiment.Game.rc;
import static experiment.util.Util.indicate;

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
        return goingHome
            || rc.getRawCheese() >= 50 && cheeseStrategy.getLastSeenCheese() < rc.getRoundNum() - 3
            || rc.getRawCheese() >= 200;
    }
}
