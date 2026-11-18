package opp_parmesan.Utils;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import opp_parmesan.Utils.BellmanFordNavigator;
import opp_parmesan.Utils.BellmanFordNavigatorCat;
import opp_parmesan.Utils.BugNavigator;
import opp_parmesan.Utils.Globals;

public class Navigator extends Globals {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static MapLocation closestPositionToTarget;
    private static int roundsSinceMovingCloserToTarget;

    public static void moveTo(MapLocation target, boolean canTurn) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        if (myLocation.equals(target)) {
            return;
        }


        if (currentTarget == null || currentTarget.distanceSquaredTo(target) > 2) {
            reset();
            closestPositionToTarget = rc.getLocation();
            minDistanceToTarget = rc.getLocation().distanceSquaredTo(target);
        } else if (!target.equals(currentTarget)) {
            int curDist = rc.getLocation().distanceSquaredTo(target);
            int closestDist = closestPositionToTarget.distanceSquaredTo(target);
            if (curDist < closestDist) {
                minDistanceToTarget = curDist;
                closestPositionToTarget = rc.getLocation();
                //roundsSinceMovingCloserToTarget = 0;
            } else
                minDistanceToTarget = closestDist;
        }

        currentTarget = target;

        /*
        MapLocation nextLocation = myLocation.add(myLocation.directionTo(target));
        if (rc.canSenseLocation(nextLocation) && rc.senseMapInfo(nextLocation).isDam()) {
            return;
        }
        */
        if (!cantMove && rc.isActionReady()) {
            int distanceToTarget = myLocation.distanceSquaredTo(target);
            if (distanceToTarget < minDistanceToTarget) {
                minDistanceToTarget = distanceToTarget;
                closestPositionToTarget = myLocation;
                roundsSinceMovingCloserToTarget = 0;
            } else {
                roundsSinceMovingCloserToTarget++;
            }
        }

        if (roundsSinceMovingCloserToTarget < 3 && enemyRobots.length == 0 && closestEnemy == null && newestSqueaks.length == 0 && rc.getCarrying() == null) {
            if (!rc.isMovementReady() || cantMove) return;
            rc.setIndicatorString("bellman");
            if (closestEnemy != null) rc.setIndicatorString(closestEnemy.toString());
            Direction bellmanFordDirection = catLoc != null ?
                    BellmanFordNavigatorCat.getBestDirection(target) :
                    BellmanFordNavigator.getBestDirection(target);
            if (bellmanFordDirection != null) {
                MapLocation bellmanFordLocation = rc.adjacentLocation(bellmanFordDirection);
                if (rc.canRemoveDirt(bellmanFordLocation)) {
                    rc.removeDirt(bellmanFordLocation);
                    opp_parmesan.Utils.Vision.digDirt(bellmanFordLocation);
                }
                if (rc.canMove(bellmanFordDirection)) {
                    if (canTurn && rc.canTurn()) {
                        opp_parmesan.Utils.Vision.turn(bellmanFordDirection);
                    }
                    if (rc.canMove(bellmanFordDirection)) {
                        rc.move(bellmanFordDirection);
                    }
                }

                //Logger.log("bf " + bellmanFordDirection);
                return;
            } else {
                //Logger.log("bf null");
            }
        } else {
            //Logger.log("bf n/a");
        }

        opp_parmesan.Utils.BugNavigator.moveTo(target, canTurn);
    }

    public static void reset() {
        currentTarget = null;

        roundsSinceMovingCloserToTarget = 0;

        BugNavigator.reset();
    }
}