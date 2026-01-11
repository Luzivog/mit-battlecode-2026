package awubot.robot.rat.strategies.king;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class KingSpawningStrategy extends Strategy {
    private final ThreatContext theContext;
    private int theBuilt = 0;

    public KingSpawningStrategy(ThreatContext aContext) {
        theContext = aContext;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        spawnDefensiveRat();
    }

    private void spawnDefensiveRat() throws GameActionException {
        if (theBuilt >= 20) {
            theBuilt++;
            return;
        }

        Direction mySpawnDir = Direction.NORTHEAST;
        if (theContext.theNearestCat != null) {
            mySpawnDir = theContext.theNearestCat.getLocation().directionTo(loc);
        } else if (theContext.theNearestEnemyRat != null) {
            mySpawnDir = theContext.theNearestEnemyRat.getLocation().directionTo(loc);
        }

        Direction[] myTryDirs = {mySpawnDir, mySpawnDir.rotateLeft(), mySpawnDir.rotateRight(),
                                 mySpawnDir.rotateLeft().rotateLeft(), mySpawnDir.rotateRight().rotateRight()};

        for (Direction myDir : myTryDirs) {
            MapLocation myBuildLoc = loc.add(myDir).add(myDir);
            if (rc.canBuildRat(myBuildLoc)) {
                rc.buildRat(myBuildLoc);
                theBuilt++;
                return;
            }
        }
        theBuilt++;
    }
}
