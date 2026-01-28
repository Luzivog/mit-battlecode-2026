package awubot_sus.robot.rat;

import battlecode.common.*;
import awubot_sus.Pathfinder;
import awubot_sus.robot.Strategy;

import static awubot_sus.Game.*;
import static awubot_sus.util.Util.indicate;

// *just* in case we optimise this in the future
public class TraverseStrategy extends Strategy {
    public MapLocation target;
    int distSquared;

    public TraverseStrategy(MapLocation target, int distSquared) {
        this.target = target;
        this.distSquared = distSquared;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getLocation().distanceSquaredTo(target) <= distSquared) return false;
        indicate("TRAVELLING TO " + target);
        indicate("before move: " + Clock.getBytecodeNum());
        BabyRat.autoMove(Pathfinder.getMove(target));
        indicate("after move: " + Clock.getBytecodeNum());
        if(Pathfinder.givenUp()) {
            indicate("GIVEN UP ON " + target);
            return false;
        }
        rc.setIndicatorLine(myLoc, target, 255, 0, 0);
        return true;
    }
}
