package awu_old_naman.robot.rat;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awu_old_naman.Pathfinder;
import awu_old_naman.robot.Strategy;

import static awu_old_naman.Game.*;
import static awu_old_naman.util.Util.indicate;

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
        rc.setIndicatorLine(myLoc, target, 255, 0, 0);
        return true;
    }
}
