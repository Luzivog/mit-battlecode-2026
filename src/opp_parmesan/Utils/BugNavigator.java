package opp_parmesan.Utils;


import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import opp_parmesan.Utils.FastSet;
import opp_parmesan.Utils.Globals;
import opp_parmesan.Utils.Vision;

public class BugNavigator extends Globals {
    private static MapLocation currentTarget;

    private static int minDistanceToTarget;
    private static MapLocation closestPositionToTarget;

    private static boolean obstacleOnRight;
    private static MapLocation currentObstacle;
    private static opp_parmesan.Utils.FastSet visitedStates;

    public static void moveTo(MapLocation target, boolean canTurn) throws GameActionException {
        if (currentTarget == null || currentTarget.distanceSquaredTo(target) > 2) {
            reset();
            closestPositionToTarget = rc.getLocation();
            minDistanceToTarget = rc.getLocation().distanceSquaredTo(target);
        } else if (!target.equals(currentTarget)) {
            int curDist = distance1d(rc.getLocation(), target);
            int closestDist = distance1d(closestPositionToTarget, target);
            if (curDist < closestDist) {
                minDistanceToTarget = curDist;
                closestPositionToTarget = rc.getLocation();
            } else
                minDistanceToTarget = closestDist;
        }

        if (!rc.isMovementReady()) return;

        boolean hasOptions = false;
        for (int i = adjacentDirections.length; --i >= 0; ) {
            if (canMoveWithDig(adjacentDirections[i])) {
                hasOptions = true;
                break;
            }
        }

        if (!hasOptions) {
            return;
        }

        MapLocation myLocation = rc.getLocation();

        int distanceToTarget = distance1d(myLocation, target);
        if (distanceToTarget < minDistanceToTarget) {
            rc.setIndicatorString("watesiggma");
            reset();
            minDistanceToTarget = distanceToTarget;
            closestPositionToTarget = rc.getLocation();
        }

        if (currentObstacle != null && opp_parmesan.Utils.Vision.hasSeenLocation(currentObstacle) && opp_parmesan.Utils.Vision.sensePassability(currentObstacle)) {
            reset();
        }

        if (!visitedStates.add(getState(target))) {
            reset();
        }

        currentTarget = target;

        if (currentObstacle == null) {
            Direction forward = myLocation.directionTo(target);
            if (canMoveWithDig(forward)) {
                moveWithDig(forward, canTurn);
                return;
            }

            setInitialDirection();
        }

        followWall(true, canTurn);
    }

    public static void reset() {
        currentTarget = null;
        obstacleOnRight = true;
        currentObstacle = null;
        visitedStates = new FastSet();
    }

    private static void setInitialDirection() throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        Direction forward = myLocation.directionTo(currentTarget);

        Direction left = forward.rotateLeft();
        for (int i = 8; --i >= 0; ) {
            MapLocation location = rc.adjacentLocation(left);
            if (opp_parmesan.Utils.Vision.hasSeenLocation(location) && opp_parmesan.Utils.Vision.sensePassability(location)) {
                break;
            }

            left = left.rotateLeft();
        }

        Direction right = forward.rotateRight();
        for (int i = 8; --i >= 0; ) {
            MapLocation location = rc.adjacentLocation(right);
            if (opp_parmesan.Utils.Vision.hasSeenLocation(location) && opp_parmesan.Utils.Vision.sensePassability(location)) {
                break;
            }

            right = right.rotateRight();
        }

        MapLocation leftLocation = rc.adjacentLocation(left);
        MapLocation rightLocation = rc.adjacentLocation(right);

        int leftDistance = distance1d(leftLocation, currentTarget);
        int rightDistance = distance1d(rightLocation, currentTarget);

        if (leftDistance < rightDistance) {
            obstacleOnRight = true;
        } else if (rightDistance < leftDistance) {
            obstacleOnRight = false;
        } else {
            obstacleOnRight = myLocation.distanceSquaredTo(leftLocation) < myLocation.distanceSquaredTo(rightLocation);
        }

        if (obstacleOnRight) {
            currentObstacle = rc.adjacentLocation(left.rotateRight());
        } else {
            currentObstacle = rc.adjacentLocation(right.rotateLeft());
        }
    }

    private static void followWall(boolean canRotate, boolean canTurn) throws GameActionException {
        Direction direction = rc.getLocation().directionTo(currentObstacle);

        for (int i = 8; --i >= 0; ) {
            direction = obstacleOnRight ? direction.rotateLeft() : direction.rotateRight();
            if (canMoveWithDig(direction)) {
                moveWithDig(direction, canTurn);
                return;
            }

            MapLocation location = rc.adjacentLocation(direction);
            if (canRotate && !rc.onTheMap(location)) {
                obstacleOnRight = !obstacleOnRight;
                followWall(false, canTurn);
                return;
            }

            if (opp_parmesan.Utils.Vision.hasSeenLocation(location) && !opp_parmesan.Utils.Vision.sensePassability(location)) {
                currentObstacle = location;
            }
        }
    }

    private static char getState(MapLocation target) {
        MapLocation myLocation = rc.getLocation();
        Direction direction = myLocation.directionTo(currentObstacle != null ? currentObstacle : target);
        int rotation = obstacleOnRight ? 1 : 0;

        return (char) ((((myLocation.x << 6) | myLocation.y) << 4) | (direction.ordinal() << 1) | rotation);
    }

    public static int distance1d(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    private static boolean canMoveWithDig(Direction direction) {
        return (rc.canMove(direction) || rc.canRemoveDirt(rc.adjacentLocation(direction))) && !opp_parmesan.Utils.Vision.isDangerous(rc.adjacentLocation(direction));
    }

    private static void moveWithDig(Direction direction, boolean canTurn) throws GameActionException {
        MapLocation DigLocation = rc.adjacentLocation(direction);
        if (rc.canRemoveDirt(DigLocation)) {
            rc.removeDirt(DigLocation);
            opp_parmesan.Utils.Vision.digDirt(DigLocation);
        }

        if (rc.canMove(direction)) {
            if (canTurn && rc.canTurn()) {
                Vision.turn(direction);
            }
            rc.move(direction);
        }

        //Logger.log("bug " + direction);
    }
}