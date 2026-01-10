package awubot;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.CellTracker;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Rng;

public class BabyRat {
    static MapLocation home, exploreTarget, mineLocation, mineTarget, cheeseTarget;
    static boolean goingHome = false;
    static int lastSeenCheese;

    static Direction lastGlance;

    public static void init() throws GameActionException {
        CellTracker.storeVisibleCells();
        home = rc.getLocation();
    }

    static void tryStrats() throws GameActionException {
        indicate("amount " + rc.getRawCheese());
        indicate("mineLoc " + mineLocation);
        if(rc.getRawCheese() == 0) {
            goingHome = false;
        }
        if(cheeseTarget == null || rc.canSenseLocation(cheeseTarget) && rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0) {
            cheeseTarget = null;
            int bestAmount = 0;
            for(var info : rc.senseNearbyMapInfos()) {
                if(info.getCheeseAmount() > bestAmount) {
                    cheeseTarget = info.getMapLocation();
                    bestAmount = info.getCheeseAmount();
                }
            }
        }
        if(goingHome || rc.getRawCheese() >= 50 && lastSeenCheese < rc.getRoundNum() - 3 || rc.getRawCheese() >= 200) {
            goingHome = true;
            if(Rng.mcg() >> 30 == 0) mineLocation = null;
            exploreTarget = null;
            indicate("going home");
            // Return home
            for(var robot : rc.senseNearbyRobots()) {
                if(robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                    home = robot.getLocation();
                    break;
                }
            }
            rc.setIndicatorLine(rc.getLocation(), home, 0, 255, 0);
            Pathfinder.makeMove(home);
            var homeDir = rc.getLocation().directionTo(home);
            if(rc.canTurn()) Pathfinder.turn(homeDir);
            var transferTarget = rc.getLocation().add(homeDir);
            if(rc.canTransferCheese(transferTarget, rc.getRawCheese())) {
                rc.transferCheese(transferTarget, rc.getRawCheese());
            }
            return;
        }
        if(mineLocation == null || lastSeenCheese < rc.getRoundNum() - 20) {
            for(var info : rc.senseNearbyMapInfos()) {
                if(info.hasCheeseMine()) {
                    mineLocation = info.getMapLocation();
                    break;
                }
            }
        }
        if(cheeseTarget != null) {
            lastSeenCheese = rc.getRoundNum();
            if(rc.getLocation().isAdjacentTo(cheeseTarget)) {
                Pathfinder.turn(rc.getLocation().directionTo(cheeseTarget));
                if(rc.isActionReady()) {
                    rc.pickUpCheese(cheeseTarget);
                    cheeseTarget = null;
                }
                return;
            }
            rc.setIndicatorLine(rc.getLocation(), cheeseTarget, 255, 0, 0);
            Pathfinder.makeMove(cheeseTarget);
            return;
        }

        if(mineLocation != null) {
            if(mineTarget == null || rc.getLocation().distanceSquaredTo(mineTarget) <= 2) {
                do {
                    mineTarget = new MapLocation(mineLocation.x + Rng.mcg() % 7 - 3, mineLocation.y + Rng.mcg() % 7 - 3);
                } while(!rc.onTheMap(mineTarget));
            }
            rc.setIndicatorLine(rc.getLocation(), mineTarget, 255, 0, 0);
            Pathfinder.makeMove(mineTarget);
            return;
        }

        // Finally explore
        if(exploreTarget == null || rc.getLocation().distanceSquaredTo(exploreTarget) <= 2) {
            exploreTarget = new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight()));
        }
        rc.setIndicatorLine(rc.getLocation(), exploreTarget, 255, 0, 0);
        Pathfinder.makeMove(exploreTarget);
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
