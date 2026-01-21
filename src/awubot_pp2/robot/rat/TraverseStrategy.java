package awubot_pp2.robot.rat;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot_pp2.Pathfinder;
import awubot_pp2.robot.Strategy;

import static awubot_pp2.Game.*;
import static awubot_pp2.util.Util.indicate;

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
        Pathfinder.makeMove(target);
        if(Pathfinder.givenUp()) {
            indicate("GIVEN UP ON " + target);
            return false;
        }
        rc.setIndicatorLine(myLoc, target, 255, 0, 0);
        return true;
    }
}
