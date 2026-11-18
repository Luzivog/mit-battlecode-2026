package opp_gravy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class Movement extends RobotPlayer {
    static Random rng = new Random(3663);
    static int lookdir = rng.nextInt(4);
    static int[] dist;
    static boolean flag = false;
    static Direction fixDir = Direction.CENTER;
    static int radar;

    private static void updateDistribution(){
        int curX = curLocation.x;
        int curY = curLocation.y;
        dist = new int[8];
        dist[0] = (mapHeight - curY + 1) >= 5 ? (mapHeight - curY + 1) : 0; // NORTH
        dist[2] = (mapWidth - curX + 1) >= 5 ? (mapWidth - curX + 1) : 0; // EAST
        dist[4] = curY >= 5 ? curY : 0; // SOUTH
        dist[6] = curX >= 5 ? curX : 0; // WEST
        dist[1] = (dist[0] * dist[2]); // NORTHEAST
        dist[3] = (dist[2] * dist[4]); // SOUTHEAST
        dist[5] = (dist[4] * dist[6]); // SOUTHWEST
        dist[7] = (dist[6] * dist[0]); // NORTHWEST
        for (int i = 0; i < 8; i += 2){
            dist[i] = dist[i] * dist[i];
        }
    }

    private static Direction getNewDir(boolean breakDirt) throws GameActionException {
        radar = 0;
        boolean[] movable = new boolean[8];
        for (int i = 0; i < 8; i++){
            if (rc.canMove(directions[i]) && rc.onTheMap(curLocation.add(directions[i]))){
                movable[i] = true;
            }
        }
        if (breakDirt){
            for (int i = 0; i < 8; i++){
                if (rc.canSenseLocation(curLocation.add(directions[i]))){
                    MapInfo tile = rc.senseMapInfo(curLocation.add(directions[i]));
                    if (tile.isDirt()) movable[i] = true;
                }
            }
        }

        int sum = 0;
        for (int i = 0; i < 8; i++){
            if (movable[i]){
                sum += dist[i];
            }
        }
        if (sum == 0) return Direction.CENTER;
        int pick = rng.nextInt(sum);
        for (int i = 0; i < 8; i++) {
            if (!movable[i]) continue;
            pick -= dist[i];
            if (pick <= 0) {
                return directions[i];
            }
        }
        return Direction.CENTER;
    }

    public static void doRandomMove(boolean breakDirt) throws GameActionException {
        if (!flag){
            MapLocation curLocation = rc.getLocation();
            rng = new Random(curLocation.x * 36 + curLocation.y * 63 + turnCount*67);
            updateDistribution();
            flag = true;
            fixDir = getNewDir(breakDirt);
        }
        if (rc.isMovementReady()) {
            Direction dir = fixDir;

            int rngnum = rng.nextInt(8);
            if (rngnum > 0) {
                if (rc.canMove(dir)) {
                    moveWithTurn(dir, breakDirt);
                } 
                else if (breakDirt && rc.canRemoveDirt(curLocation.add(dir))){
                    moveWithTurn(dir, breakDirt);
                }
                else {
                    dir = getNewDir(breakDirt);
                    moveWithTurn(dir, breakDirt);
                }
            } else {
                updateDistribution();
                dir = getNewDir(breakDirt);
                moveWithTurn(dir, breakDirt);
            }
            fixDir = dir;
        }
    }

    public static void doRandomMove() throws GameActionException {
        doRandomMove(false);
    }

    private static void moveWithTurn(Direction dir, boolean breakDirt) throws GameActionException {
        if (dir == Direction.CENTER) return;
        if (rc.onTheMap(rc.getLocation().add(dir))) {
            if (radar == 0 || radar == 2){
                if (rc.canTurn()) rc.turn(dir);
                if (breakDirt && rc.canRemoveDirt(curLocation.add(dir))){
                    rc.removeDirt(curLocation.add(dir));
                }
                if (rc.canMove(dir)) rc.move(dir);
            }
            else if (radar == 1){
                if (breakDirt && rc.canRemoveDirt(curLocation.add(dir))){
                    rc.removeDirt(curLocation.add(dir));
                }
                if (rc.canMove(dir)) rc.move(dir);
                if (rc.canTurn()) rc.turn(dir.rotateLeft().rotateLeft());
            }
            else {
                if (breakDirt && rc.canRemoveDirt(curLocation.add(dir))){
                    rc.removeDirt(curLocation.add(dir));
                }
                if (rc.canMove(dir)) rc.move(dir);
                if (rc.canTurn()) rc.turn(dir.rotateRight().rotateRight());
            }
            radar = (radar + 1)%4;
        }
    }

    public static int getNewTarget() throws GameActionException {
        if (curRound <= 2 && visitedTargets[4] == -1) {
            return 4;
        }
        List<Integer> notVisitedLocations = new ArrayList<>();
        curLocation = rc.getLocation();

        int maxDist = 0, maxIndex = 4;
        for (int i = 0; i < 9; i++) {
            if (curRound - visitedTargets[i] <= 300)
                continue;
            int dist = curLocation.distanceSquaredTo(targets[i]);
            if (dist < maxDist) {
                maxDist = dist;
                maxIndex = i;
            }
            if (dist >= 100) {
                notVisitedLocations.add(i);
            }
        }

        if (!notVisitedLocations.isEmpty()) {
            return notVisitedLocations.get(myId % notVisitedLocations.size());
        } else {
            for (int i = 0; i < 9; i++) {
                visitedTargets[i] = -1;
            }
            return maxIndex;
        }
    }
}