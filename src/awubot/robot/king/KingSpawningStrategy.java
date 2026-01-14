package awubot.robot.king;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.util.Rng;
import awubot.util.Util;
import static awubot.Game.*;
import static awubot.util.Util.println;

public class KingSpawningStrategy extends Strategy {
    private final ThreatContext theContext;

    public KingSpawningStrategy(ThreatContext aContext) {
        theContext = aContext;
    }

    @Override
    public boolean runTick() throws GameActionException {
        var enemies = rc.senseNearbyRobots(8, team.opponent());
        while((enemies.length > 0 || rc.getCurrentRatCost() < 50) && (rc.getGlobalCheese() >= 100 || rc.getCurrentRatCost() == 10)) {
            if(!trySpawnRat()) break;
        }
        return true;
    }

    // TODO: fix this
    private boolean trySpawnRat() throws GameActionException {
        Direction mySpawnDir = selectSpawnDirection();
        Direction[] myTryDirs = {
            mySpawnDir,
            mySpawnDir.rotateLeft(),
            mySpawnDir.rotateRight(),
            mySpawnDir.rotateLeft().rotateLeft(),
            mySpawnDir.rotateRight().rotateRight()
        };

        for (Direction myDir : myTryDirs) {
            MapLocation mySpawnLoc = myLoc.add(myDir).add(myDir);
            if (rc.canBuildRat(mySpawnLoc)) {
                spawnRat(mySpawnLoc, decideRole());
                return true;
            }
        }
        return false;
    }

    private Direction selectSpawnDirection() {
        if (theContext.theNearestCat != null) {
            return theContext.theNearestCat.getLocation().directionTo(myLoc);
        }

        if (theContext.theNearestEnemyRat != null) {
            return theContext.theNearestEnemyRat.getLocation().directionTo(myLoc);
        }

        if (theContext.theDefenseState == ThreatContext.DefenseState.NORMAL) {
            MapLocation myMine = getNearestMine();
            if (myMine != null) {
                return myLoc.directionTo(myMine);
            }
        }

        return Direction.NORTHEAST;
    }

    private int decideRole() throws GameActionException {
        double myRusherRatio = calculateRusherRatio();
        boolean myIsRusher = (Rng.mcg() & 0x7fffffff) < (int)(myRusherRatio * 0x7fffffff);
        // TODO
        return 0;
    }

    private double calculateRusherRatio() {
        if (time < 50) return 0.15;
        if (time < 150) return 0.35;
        if (time < 300) return 0.45;
        return 0.50;
    }

    private MapLocation getNearestMine() {
        if (GlobalArray.cheeseMines.set.length() == 0) return null;

        MapLocation myBest = null;
        int myBestDist = Integer.MAX_VALUE;

        int myMineCount = GlobalArray.cheeseMines.set.length();
        for (int i = 0; i < myMineCount; i++) {
            MapLocation myMine = Util.decode2x2(GlobalArray.cheeseMines.at(i));
            int myDist = myLoc.distanceSquaredTo(myMine);
            if (myDist < myBestDist) {
                myBestDist = myDist;
                myBest = myMine;
            }
        }
        return myBest;
    }
}
