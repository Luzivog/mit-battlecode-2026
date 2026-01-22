package awubot.robot.rat;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.Pathfinder;
import awubot.Pathfinder.BugnavInstance;
import awubot.robot.Strategy;

import static awubot.Game.*;
import static awubot.util.Util.indicate;

// *just* in case we optimise this in the future
public class TraverseStrategy extends Strategy {
    Pathfinder.BugnavInstance bug;
    int distSquared;

    public TraverseStrategy(MapLocation target, int distSquared) {
        this.bug = new BugnavInstance(target);
        this.distSquared = distSquared;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if(rc.getLocation().distanceSquaredTo(bug.target) <= distSquared) return false;
        indicate("TRAVELLING TO " + bug.target);
        Pathfinder.makeMove(bug);
        if(bug.givenUp) {
            indicate("GIVEN UP ON " + bug.target);
            return false;
        }
        rc.setIndicatorLine(myLoc, bug.target, 255, 0, 0);
        return true;
    }

    public void updateTarget(MapLocation target) {
        if(!target.equals(bug.target)) {
            if(target.isAdjacentTo(bug.target)) {
                indicate("target changed but adjacent, keep going");
                bug.target = target;
            } else {
                indicate("RETARGET");
                bug = new BugnavInstance(target);
            }
        }
    }
}
