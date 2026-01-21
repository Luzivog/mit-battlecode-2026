package awubot_bugfixes.robot.king;

import battlecode.common.*;
import awubot_bugfixes.robot.Strategy;
import awubot_bugfixes.CellTracker;
import awubot_bugfixes.RobotTracker;
import awubot_bugfixes.comms.GlobalArray;
import awubot_bugfixes.comms.Squeaker;

import static awubot_bugfixes.Game.*;
import static awubot_bugfixes.util.Util.*;

public class RatKing {
    public static Strategy primaryStrategy;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
    }

    public static void run() throws GameActionException {
        indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");

        CellTracker.storeVisibleCellsKing();

        tryPickUpCheese();
        //tryUnburyIfStuck();

        primaryStrategy.runTick();
    }

    public static void tryPickUpCheese() throws GameActionException {
        for(var dir : allDirections) {
            var loc = myLoc.add(dir);
            if(rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
            }
        }
    }

    public static void rawMove(Direction dir) throws GameActionException {
        _rawMove(dir);
        tryPickUpCheese();
    }

    public static void turn(Direction dir) throws GameActionException {
        _turn(dir);
    }

    private static void tryUnburyIfStuck() throws GameActionException {
        if(!rc.isActionReady()) return;
        if(rc.getAllCheese() < GameConstants.DIG_DIRT_CHEESE_COST) return;

        for (MapInfo info: rc.senseNearbyMapInfos(1)) {
            if(info.isPassable()) return;
        }

        for(var dir : directions) {
            var dest = rc.senseMapInfo(myLoc.add(dir));
            if (dest.isPassable()) return;
            indicate(myLoc.add(dir).toString() + " is blocked");
            
        }

        MapLocation best = null;
        int bestScore = Integer.MIN_VALUE;

        for(int dx = -2; dx <= 2; dx++) {
            for(int dy = -2; dy <= 2; dy++) {
                if(Math.max(Math.abs(dx), Math.abs(dy)) > 2) continue;

                var loc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if(!rc.onTheMap(loc) || !rc.canSenseLocation(loc)) continue;

                MapInfo info = rc.senseMapInfo(loc);
                if(!info.isDirt() || !rc.canRemoveDirt(loc)) continue;

                int score = 0;
                for(var dir : directions) {
                    var dest = myLoc.add(dir);
                    if(!rc.onTheMap(dest)) continue;
                    if(Math.abs(loc.x - dest.x) <= 2 && Math.abs(loc.y - dest.y) <= 2) {
                        score += 25;
                    }
                }
                
                if(CellTracker.isWallAdjacent(loc)) {
                    score += 200;
                }

                score -= 10 * myLoc.distanceSquaredTo(loc);

                if(score > bestScore) {
                    bestScore = score;
                    best = loc;
                }
            }
        }

        if(best != null) {
            rc.removeDirt(best);
            CellTracker.storeVisibleCellsKing();
        }
    }

    
}
