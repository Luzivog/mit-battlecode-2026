package awubot.robot.king.strategies;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.rat.BabyRatRole;
import awubot.util.Rng;
import awubot.util.Util;
import static awubot.Game.*;

public class KingSpawningStrategy extends Strategy {
    private final ThreatContext theContext;

    public KingSpawningStrategy(ThreatContext aContext) {
        theContext = aContext;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        int myCost = rc.getCurrentRatCost();
        int myCheese = rc.getGlobalCheese();
        int myMinCheese = myCost + 50;

        if (myCheese < myMinCheese) return;

        int myMaxSpawns = myCheese >= 300 ? 3 : 1;

        for (int i = 0; i < myMaxSpawns && myCheese >= myMinCheese; i++) {
            if (trySpawnRat()) {
                myCheese = rc.getGlobalCheese();
                myCost = rc.getCurrentRatCost();
                myMinCheese = myCost + 50;
            } else {
                break;
            }
        }
    }

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
                rc.buildRat(mySpawnLoc);
                assignRole(rc.senseRobotAtLocation(mySpawnLoc).getID());
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

    private void assignRole(int id) throws GameActionException {
        double myRusherRatio = calculateRusherRatio();
        boolean myIsRusher = (Rng.mcg() & 0x7fffffff) < (int)(myRusherRatio * 0x7fffffff);
        int myRole = myIsRusher ? BabyRatRole.RUSHER.value : BabyRatRole.MINER.value;
        GlobalArray.writeSpawnStrategy(id, myRole);
        if (myIsRusher) {
            System.out.println("[SPAWN] K" + kingId + " t=" + time +
                " spawning RUSHER (ratio=" + myRusherRatio + ")");
        }
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
