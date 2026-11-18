package opp_obg;

import battlecode.common.*;

public class BugNav {


    static void init(){
        rc = MyUnit.rc;
    }

    static final int INF = 1000000;

    static RobotController rc;
    static int turnsMovingToObstacle;
    static final int MAX_TURNS_MOVING_TO_OBSTACLE = 3;
    static Boolean rotateRight = true; //if I should rotate right or left
    static MapLocation lastObstacleFound = null; //latest obstacle I've found in my way
    static int minDistToTarget = INF; //minimum distance I've been to the enemy while going around an obstacle
    static MapLocation minLocToTarget = null; //minimum distance I've been to the enemy while going around an obstacle
    static MapLocation prevTarget = null; //previous target
    static FastIntSet states = new FastIntSet();

    static void moveTo(MapLocation target) throws GameActionException {
        //No target? ==> bye!
        if (target == null) return;
        if (!rc.isMovementReady()) return;
        if (MovementManager.myLoc.distanceSquaredTo(target) == 0) return;

        int distBetweenTargets = 0;
        if (prevTarget == null) resetPathfinding(true);
        else distBetweenTargets = prevTarget.distanceSquaredTo(target);

        prevTarget = target;
        if (distBetweenTargets > 2) resetPathfinding(true);
        else if (distBetweenTargets > 0) softResetPathfinding();

        if (!MyUnit.isKing && MovementManager.myLoc.distanceSquaredTo(target) <= 20) DirtManager.canPassThroughDirt = true;

        checkStates();

        //If I'm at a minimum distance to the target, I'm free!
        MapLocation myLoc = rc.getLocation();
        int d = myLoc.distanceSquaredTo(target);
        if (d < minDistToTarget){
            resetPathfinding(false);
            minDistToTarget = d;
            minLocToTarget = rc.getLocation();
            //states.clear();
            //rc.setIndicatorString("Min dist achieved! " + minDistToTarget);
        }

        //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
        Direction dir = myLoc.directionTo(target);
        if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);

        //This should not happen for a single unit, but whatever
        //if (rccanMove(dir)) resetPathfinding();

        if (MovementManager.canMove(dir, DirtManager.canPassThroughDirt)){
            move(dir);
            if (lastObstacleFound != null) {
                ++turnsMovingToObstacle;
                lastObstacleFound = rc.getLocation().add(dir);
                if (turnsMovingToObstacle >= MAX_TURNS_MOVING_TO_OBSTACLE || !rc.onTheMap(lastObstacleFound)) resetPathfinding(false);
            }
            return;
        } else turnsMovingToObstacle = 0;

        updateRotation();

        //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
        //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
        for (int i = 0; i < 16; ++i){
            if (MovementManager.canMove(dir, DirtManager.canPassThroughDirt)){
                move(dir);
                return;
            }
            MapLocation newLoc = myLoc.add(dir);
            if (!rc.onTheMap(newLoc.add(dir))) rotateRight = !rotateRight; //Note that we are adding this because of kings
                //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
            else{
                lastObstacleFound = myLoc.add(dir);
                //rc.setIndicatorDot(newLoc, 0, 255, 0);
            }
            if (rotateRight) dir = dir.rotateRight();
            else dir = dir.rotateLeft();
        }

        if (MovementManager.canMove(dir, DirtManager.canPassThroughDirt)) move(dir);
    }

    //clear some of the previous data
    static void resetPathfinding(boolean resetRotation){
        lastObstacleFound = null;
        minDistToTarget = INF;
        turnsMovingToObstacle = 0;
        if (resetRotation) rotateRight = null;
        states.clear();
        if (resetRotation) MyUnit.rc.setIndicatorString("Resetting bugnav! " + resetRotation);
    }

    static void softResetPathfinding(){
        if (minLocToTarget != null){
            int dist = minLocToTarget.distanceSquaredTo(prevTarget);
            int currentDist = rc.getLocation().distanceSquaredTo(prevTarget);
            if (dist < currentDist) {
                minDistToTarget = dist;
            }
            else {
                minDistToTarget = currentDist;
                minLocToTarget = rc.getLocation();
            }
        }
        else resetPathfinding(false);
        MyUnit.rc.setIndicatorString("Soft-Resetting bugnav!");
    }

    static void checkStates(){
        if (lastObstacleFound == null) return;
        int boolEnc = 0;
        if (rotateRight != null) boolEnc = rotateRight ? 0 : 1;
        Direction dirToObstacle = MovementManager.myLoc.directionTo(lastObstacleFound);
        int code = ((MovementManager.myLoc.x) << 6) | (MovementManager.myLoc.y) | (boolEnc << 15) | (dirToObstacle.ordinal() << 12);
        if (states.contains(code)) {
            //MyUnit.rc.setIndicatorString("Found code! " + code);
            resetPathfinding(false);
        }
        else{
            states.add(code);
            //MyUnit.rc.setIndicatorString("Adding code! " + code);
        }
    }

    static void updateRotation(){
        if (rotateRight == null){
            Direction dir = MovementManager.myLoc.directionTo(prevTarget);
            Direction dirL = dir, dirR = dir;
            MapLocation locL = MovementManager.myLoc, locR = MovementManager.myLoc;
            for (int i = 0; i < 8; ++i) {
                dirL = dirL.rotateLeft();
                locL = MovementManager.myLoc.add(dirL);
                if (MovementManager.canMove(dirL, DirtManager.canPassThroughDirt)) break;
            }
            for (int i = 0; i < 8; ++i) {
                dirR = dirR.rotateRight();
                locR = MovementManager.myLoc.add(dirR);
                if (MovementManager.canMove(dirR, DirtManager.canPassThroughDirt)) break;
            }
            if (locL.distanceSquaredTo(prevTarget) < locR.distanceSquaredTo(prevTarget)){
                rotateRight = false;
                return;
            }
            rotateRight = true;
        }
    }

    static void move(Direction dir) throws GameActionException {
        int dist = 0;
        if (prevTarget != null) dist = MovementManager.myLoc.add(dir).distanceSquaredTo(prevTarget);
        if (dist > 20) MovementManager.moveFreeDir(dir);
        else MovementManager.moveWhileLooking(dir, prevTarget, DirtManager.canPassThroughDirt);
    }

}
