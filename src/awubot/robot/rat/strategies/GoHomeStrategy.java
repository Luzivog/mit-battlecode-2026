package awubot.robot.rat.strategies;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

import static awubot.robot.rat.BabyRat.*;

public class GoHomeStrategy extends Strategy {
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

        updateHomeToKingLocation();

        if (home == null) {
            indicate("no king found!");
            return;
        }

        rc.setIndicatorLine(rc.getLocation(), home, 0, 255, 0);
        Pathfinder.makeMove(home);

        var dir = loc.directionTo(home);
        var transferTarget = loc.add(dir);

        if (loc.distanceSquaredTo(home) <= 8) {
            shouldGlance = false;
            if (dir != Direction.CENTER && rc.canTurn()) turn(dir);
        }

        if (rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
            rc.transferCheese(transferTarget, rc.getRawCheese());
        }
    }

    private void updateHomeToKingLocation() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                home = robot.getLocation();
                return;
            }
        }

        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            MapLocation kingLoc = GlobalArray.getRat(i);
            if (kingLoc != null && rc.onTheMap(kingLoc)) {
                int dist = loc.distanceSquaredTo(kingLoc);
                if (dist < bestDist) {
                    bestDist = dist;
                    home = kingLoc;
                }
            }
        }
    }

    public void setGoingHome(boolean goingHome) {
        this.goingHome = goingHome;
    }

    private boolean shouldGoHome() {
        if (goingHome) return true;

        int cheese = rc.getRawCheese();
        if (cheese >= 200) return true;
        if (cheese >= 50 && cheeseStrategy.getLastSeenCheese() < rc.getRoundNum() - 3) return true;

        int globalCheese = rc.getGlobalCheese();
        if (globalCheese < 100 && cheese >= 20) return true;
        if (globalCheese < 30 && cheese > 0) return true;

        return false;
    }
}
