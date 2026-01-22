package awubot_mystery.robot.king;

import battlecode.common.*;
import awubot_mystery.robot.Strategy;
import awubot_mystery.CellTracker;
import awubot_mystery.RobotTracker;
import awubot_mystery.comms.GlobalArray;
import awubot_mystery.comms.Squeaker;

import static awubot_mystery.Game.*;
import static awubot_mystery.util.Util.*;

public class RatKing {
    public static Strategy primaryStrategy;

    public static void init() {
        primaryStrategy = new RatKingStrategy();
    }

    public static void run() throws GameActionException {
        indicate(rc.getGlobalCheese() + " cheese, " + rc.getHealth() + " HP");

        CellTracker.storeVisibleCellsKing();

        tryPickUpCheese();
        tryUnburyIfStuck();

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

        indicate("stuck, trying to unbury");

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                
                if (Math.max(Math.abs(dx), Math.abs(dy)) < 2) continue;
                indicate("checking " + dx + ", " + dy);

                var loc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if (!rc.onTheMap(loc) || !rc.canSenseLocation(loc)) continue;

                MapInfo info = rc.senseMapInfo(loc);
                if (info.isPassable()) {
                    indicate(loc.toString() + " is passable, no need to remove dirt");
                    return;
                }

                indicate(loc.toString() + " is unpassable, trying to remove dirt");
            }
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

                // check how close it is to free space
                int minDistToFree = Integer.MAX_VALUE;
                for(int ddx = -3; ddx <= 3; ddx++) {
                    for(int ddy = -3; ddy <= 3; ddy++) {
                        var checkLoc = new MapLocation(loc.x + ddx, loc.y + ddy);
                        if(!rc.onTheMap(checkLoc) || !rc.canSenseLocation(checkLoc))
                            continue;
                        
                        // make sure euclidean dist^2 with rat king is > 2
                        
                        int newx = ddx + dx;
                        int newy = ddy + dy;
                        if (newx * newx + newy * newy <= 2)
                            continue;
                        
                        MapInfo checkInfo = rc.senseMapInfo(checkLoc);
                        if(checkInfo.isPassable()) {
                            int dist = ddx * ddx + ddy * ddy;
                            if(dist < minDistToFree) {
                                minDistToFree = dist;
                            }
                        }
                    }
                }

                score -= 10 * minDistToFree;

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
