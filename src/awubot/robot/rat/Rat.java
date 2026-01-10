package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.CellTracker;
import awubot.robot.Strategy;
import awubot.robot.rat.strategies.CheeseStrategy;
import awubot.robot.rat.strategies.ExploreStrategy;
import awubot.robot.rat.strategies.GoHomeStrategy;
import awubot.robot.rat.strategies.MineStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Rng;

public class Rat {
    public static MapLocation home, exploreTarget, mineLocation, mineTarget, cheeseTarget;
    public static boolean goingHome = false;
    public static int lastSeenCheese;

    static Direction lastGlance;
    static final Strategy goHomeStrategy = new GoHomeStrategy();
    static final Strategy cheeseStrategy = new CheeseStrategy();
    static final Strategy mineStrategy = new MineStrategy();
    static final Strategy exploreStrategy = new ExploreStrategy();

    public static void init() throws GameActionException {
        CellTracker.storeVisibleCells();
        home = rc.getLocation();
    }

    static void tryStrats() throws GameActionException {
        indicate("amount " + rc.getRawCheese());
        indicate("mineLoc " + mineLocation);
        if (rc.getRawCheese() == 0) {
            goingHome = false;
        }
        if (goHomeStrategy.tryStrategy(goHomeStrategy)) return;
        if (cheeseStrategy.tryStrategy(cheeseStrategy)) return;
        if (mineStrategy.tryStrategy(mineStrategy)) return;
        exploreStrategy.tryStrategy(exploreStrategy);
    }

    public static boolean shouldGoHome() {
        return goingHome
            || rc.getRawCheese() >= 50 && lastSeenCheese < rc.getRoundNum() - 3
            || rc.getRawCheese() >= 200;
    }

    public static void refreshCheeseTarget() throws GameActionException {
        if (cheeseTarget != null
            && rc.canSenseLocation(cheeseTarget)
            && rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0) {
            cheeseTarget = null;
        }
        if (cheeseTarget != null) return;
        int bestAmount = 0;
        for (var info : rc.senseNearbyMapInfos()) {
            if (info.getCheeseAmount() > bestAmount) {
                cheeseTarget = info.getMapLocation();
                bestAmount = info.getCheeseAmount();
            }
        }
    }

    public static void refreshMineLocation() throws GameActionException {
        if (mineLocation != null && lastSeenCheese >= rc.getRoundNum() - 20) return;
        for (var info : rc.senseNearbyMapInfos()) {
            if (info.hasCheeseMine()) {
                mineLocation = info.getMapLocation();
                break;
            }
        }
    }

    public static void updateMineTarget() throws GameActionException {
        if (mineTarget != null && rc.getLocation().distanceSquaredTo(mineTarget) > 2) return;
        do {
            mineTarget = new MapLocation(
                mineLocation.x + Rng.mcg() % 7 - 3,
                mineLocation.y + Rng.mcg() % 7 - 3
            );
        } while (!rc.onTheMap(mineTarget));
    }

    public static void updateExploreTarget() {
        if (exploreTarget != null && rc.getLocation().distanceSquaredTo(exploreTarget) > 2) return;
        exploreTarget = new MapLocation(
            (int)(Math.random() * rc.getMapWidth()),
            (int)(Math.random() * rc.getMapHeight())
        );
    }

    public static void run() throws GameActionException {
        CellTracker.checkSymmetry();
        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        tryStrats();
        if(rc.canTurn()) {
            indicate("glancing");
            var leftDir = rc.getDirection().rotateLeft().rotateLeft();
            var rightDir = rc.getDirection().rotateRight().rotateRight();
            if(lastGlance == leftDir) {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            } else if(lastGlance == rightDir || Rng.mcg() >> 30 != 0) {
                Pathfinder.turn(leftDir);
                lastGlance = leftDir;
            } else {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            }
        }
    }
}
