package opp_powerpuff.Helpers;

import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.DataStructures.Gain;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Utils.Calc;
import opp_powerpuff.Utils.RNG;

public class Target {
    public static MapLocation targetLocation;
    public static int targetSetRound = 0;

    public static boolean exploring = true;
    public static boolean runningFromCat = false;
    public static MapLocation targetBeforeCat = null;

    public static int prevSymm = 7;

    public static final float MAP_LENGTH_MULTIPLIER = 1.5f;

    // 0 to 100
    public static int gatherCheeseScore() throws GameActionException {
        return Global.readGatherCheeseScore();
    }

    public static MapLocation getTargetLocation() throws GameActionException {
        if (
            expiredTargetLocation() || 
            targetLocation.equals(Rat.rc.getLocation()) ||
            (
                Rat.distTo(targetLocation) > 81 && 
                prevSymm != MapData.symmetry && 
                Rat.rc.getRoundNum() > Rat.startDuration()
            ) //||
            // (
            //     VisionManager.enemies.length >= 3 &&
            //     Rat.rc.getRoundNum() - targetSetRound >= 10
            // )
        ) {
            prevSymm = MapData.symmetry;
            setTargetLocation();
        }
        // Rat.rc.setIndicatorDot(targetLocation, 0, 0, 255);
        return targetLocation;
    }

    private static void setTargetLocation() throws GameActionException {
        if (runningFromCat && targetBeforeCat != null) {
            runningFromCat = false;        
            Explore.assignExplore(targetBeforeCat, true);
            targetLocation = Explore.getExploreTarget();
            exploring = true;
            targetSetRound = Rat.rc.getRoundNum() - expiredRounds() + 20;
            targetBeforeCat = null;
        } else {
            targetLocation = null;
            int chance = gatherCheeseScore();
            // if (exploring) chance = Math.max(0, chance - 30);
            if (
                (
                    RNG.rng.nextInt(100) < chance &&
                    VisionManager.bigCheeseMine == null
                ) ||
                Rat.rc.getHealth() <= Constants.RETREAT_HEALTH
            ) {
                Mine.assignTargetCheeseMine();
                targetLocation = Mine.getTargetCheeseMine();
                exploring = false;
            } 
            
            if (targetLocation == null) {
                Explore.assignExplore(null, false);
                targetLocation = Explore.getExploreTarget();
                exploring = true;
            }

            targetSetRound = Rat.rc.getRoundNum();
        }
    }

    private static boolean expiredTargetLocation() throws GameActionException {
        if (targetLocation == null) return true;
        if (exploring && Explore.expired()) return true;
        if (!exploring && Mine.expired()) return true;
        return Rat.rc.getRoundNum() - targetSetRound > expiredRounds();
    }

    public static void setAwayCatLocation(MapLocation cat) throws GameActionException {
        Explore.assignExplore(cat, false);
        targetBeforeCat = targetLocation;
        targetLocation = Explore.getExploreTarget();
        exploring = true;
        targetSetRound = Rat.rc.getRoundNum() - expiredRounds() + Constants.ROUNDS_RUN_FROM_CAT;
        runningFromCat = true;
    }

    public static void setAllyKingLocation(int rounds) throws GameActionException {
        Explore.assignExplore(VisionManager.allyRatKingLocation, true);
        targetLocation = Explore.getExploreTarget();
        exploring = true;
        targetSetRound = Rat.rc.getRoundNum() - expiredRounds() + rounds;
    }

    private static int expiredRounds() throws GameActionException {
        return (int) ((float)(Rat.W + Rat.H) / 2 * MAP_LENGTH_MULTIPLIER);
    }

    public static boolean isRunningFromCat() throws GameActionException {
        return runningFromCat;
    }
}
