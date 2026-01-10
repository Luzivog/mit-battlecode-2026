package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.CellTracker;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Rng;

public class Rat {
    static MapLocation home, exploreTarget, mineLocation, mineTarget, cheeseTarget;
    static boolean goingHome = false;
    static int lastSeenCheese;

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

    static boolean shouldGoHome() {
        return goingHome
            || rc.getRawCheese() >= 50 && lastSeenCheese < rc.getRoundNum() - 3
            || rc.getRawCheese() >= 200;
    }

    static void refreshCheeseTarget() throws GameActionException {
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

    static void refreshMineLocation() throws GameActionException {
        if (mineLocation != null && lastSeenCheese >= rc.getRoundNum() - 20) return;
        for (var info : rc.senseNearbyMapInfos()) {
            if (info.hasCheeseMine()) {
                mineLocation = info.getMapLocation();
                break;
            }
        }
    }

    static void updateMineTarget() throws GameActionException {
        if (mineTarget != null && rc.getLocation().distanceSquaredTo(mineTarget) > 2) return;
        do {
            mineTarget = new MapLocation(
                mineLocation.x + Rng.mcg() % 7 - 3,
                mineLocation.y + Rng.mcg() % 7 - 3
            );
        } while (!rc.onTheMap(mineTarget));
    }

    static void updateExploreTarget() {
        if (exploreTarget != null && rc.getLocation().distanceSquaredTo(exploreTarget) > 2) return;
        exploreTarget = new MapLocation(
            (int)(Math.random() * rc.getMapWidth()),
            (int)(Math.random() * rc.getMapHeight())
        );
    }

    static class GoHomeStrategy extends Strategy {
        @Override
        public boolean isComplete() {
            return !shouldGoHome();
        }

        @Override
        public void runTick() throws GameActionException {
            goingHome = true;
            exploreTarget = null;
            indicate("going home");
            for (var robot : rc.senseNearbyRobots()) {
                if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                    home = robot.getLocation();
                    break;
                }
            }
            rc.setIndicatorLine(rc.getLocation(), home, 0, 255, 0);
            Pathfinder.makeMove(home);
            var transferTarget = rc.getLocation().add(rc.getLocation().directionTo(home));
            if (rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
                rc.transferCheese(transferTarget, rc.getRawCheese());
            }
        }
    }

    static class CheeseStrategy extends Strategy {
        @Override
        public boolean isComplete() throws GameActionException {
            refreshCheeseTarget();
            return cheeseTarget == null;
        }

        @Override
        public void runTick() throws GameActionException {
            lastSeenCheese = rc.getRoundNum();
            if (rc.getLocation().isAdjacentTo(cheeseTarget)) {
                Pathfinder.turn(rc.getLocation().directionTo(cheeseTarget));
                if (rc.isActionReady()) {
                    rc.pickUpCheese(cheeseTarget);
                    cheeseTarget = null;
                }
                return;
            }
            rc.setIndicatorLine(rc.getLocation(), cheeseTarget, 255, 0, 0);
            Pathfinder.makeMove(cheeseTarget);
        }
    }

    static class MineStrategy extends Strategy {
        @Override
        public boolean isComplete() throws GameActionException {
            refreshMineLocation();
            return mineLocation == null;
        }

        @Override
        public void runTick() throws GameActionException {
            updateMineTarget();
            rc.setIndicatorLine(rc.getLocation(), mineTarget, 255, 0, 0);
            Pathfinder.makeMove(mineTarget);
        }
    }

    static class ExploreStrategy extends Strategy {
        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public void runTick() throws GameActionException {
            updateExploreTarget();
            rc.setIndicatorLine(rc.getLocation(), exploreTarget, 255, 0, 0);
            Pathfinder.makeMove(exploreTarget);
        }
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
