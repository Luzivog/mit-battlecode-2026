package awubot.robot.rat.strategies;

import battlecode.common.GameActionException;
import battlecode.common.UnitType;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.robot.rat.Rat;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class GoHomeStrategy extends Strategy {
    @Override
    public boolean isComplete() {
        return !Rat.shouldGoHome();
    }

    @Override
    public void runTick() throws GameActionException {
        Rat.goingHome = true;
        Rat.exploreTarget = null;
        indicate("going home");
        for (var robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                Rat.home = robot.getLocation();
                break;
            }
        }
        rc.setIndicatorLine(rc.getLocation(), Rat.home, 0, 255, 0);
        Pathfinder.makeMove(Rat.home);
        var transferTarget = rc.getLocation().add(rc.getLocation().directionTo(Rat.home));
        if (rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
            rc.transferCheese(transferTarget, rc.getRawCheese());
        }
    }
}
