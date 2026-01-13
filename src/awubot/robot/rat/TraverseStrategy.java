package awubot.robot.rat;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.robot.Strategy;

import static awubot.Game.*;
import static awubot.util.Util.indicate;

// *just* in case we optimise this in the future
public class TraverseStrategy extends Strategy {
    public MapLocation target;
    int distSquared;

    public TraverseStrategy(MapLocation target, int distSquared) {
        this.target = target;
        this.distSquared = distSquared;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return rc.getLocation().distanceSquaredTo(target) <= distSquared;
    }

    @Override
    public void runTick() throws GameActionException {
        indicate("TRAVELLING TO " + target);
        Pathfinder.makeMove(target);
        rc.setIndicatorLine(myLoc, target, 255, 0, 0);
    }
}
