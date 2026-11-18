package opp_gravy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class Explore extends RobotPlayer {
    static Random rng = new Random(3663);
    static int[] dist;
    static boolean flag = false;
    static MapLocation[] candidateLocation;

    private static void init(){
        candidateLocation = new MapLocation[9];
        candidateLocation[0] = new MapLocation(0, 0);
        candidateLocation[1] = new MapLocation(midX1, 0);
        candidateLocation[2] = new MapLocation(mapWidth-1, 0);
        candidateLocation[3] = new MapLocation(mapWidth-1, midY1);
        candidateLocation[4] = new MapLocation(mapWidth-1, mapHeight-1);
        candidateLocation[5] = new MapLocation(midX2, mapHeight-1);
        candidateLocation[6] = new MapLocation(0, mapHeight-1);
        candidateLocation[7] = new MapLocation(0, midY2);
        candidateLocation[8] = new MapLocation(midX1, midY1);
    }

    private static void updateSearchTarget(){
        dist = new int[9];
        dist[0] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[0])/2) + 0.5); // Bottom left corner
        dist[1] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[1])) + 0.5);   // Bottom edge
        dist[2] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[2])/2) + 0.5); // Bottom right corner
        dist[3] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[3])) + 0.5);   // Right edge
        dist[4] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[4])/2) + 0.5); // Top right corner
        dist[5] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[5])) + 0.5);   // Top edge
        dist[6] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[6])/2) + 0.5); // Top left corner
        dist[7] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[7])) + 0.5);   // Left edge
        dist[8] = (int)(Math.sqrt(curLocation.distanceSquaredTo(candidateLocation[8])*2) + 0.5); // Center

        int sum = dist[0] + dist[1] + dist[2] + dist[3] + dist[4] + dist[5] + dist[6] + dist[7] + dist[8];
        int rand = rng.nextInt(sum);
        if (rand < dist[0]){
            BabyRat.searchingLocation = candidateLocation[0];
            return;
        }
        rand -= dist[0];
        if (rand < dist[1]){
            BabyRat.searchingLocation = candidateLocation[1];
            return;
        }
        rand -= dist[1];
        if (rand < dist[2]){
            BabyRat.searchingLocation = candidateLocation[2];
            return;
        }
        rand -= dist[2];
        if (rand < dist[3]){
            BabyRat.searchingLocation = candidateLocation[3];
            return;
        }
        rand -= dist[3];
        if (rand < dist[4]){
            BabyRat.searchingLocation = candidateLocation[4];
            return;
        }
        rand -= dist[4];
        if (rand < dist[5]){
            BabyRat.searchingLocation = candidateLocation[5];
            return;
        }
        rand -= dist[5];
        if (rand < dist[6]){
            BabyRat.searchingLocation = candidateLocation[6];
            return;
        }
        rand -= dist[6];
        if (rand < dist[7]){
            BabyRat.searchingLocation = candidateLocation[7];
            return;
        }
        rand -= dist[7];
        if (rand < dist[8]){
            BabyRat.searchingLocation = candidateLocation[8];
            return;
        }
        BabyRat.searchingLocation = new MapLocation(midX1, midY1);
    }

    public static void reset(){
        MapLocation curLocation = rc.getLocation();
        rng = new Random(curLocation.x * 36 + curLocation.y * 63 + turnCount*67 + myId * 69);
        updateSearchTarget();
    }

    public static void doRandomMove(boolean breakDirt) throws GameActionException {
        if (BabyRat.willNotMove) 
            return;
        if (!flag){
            init();
            reset();
            flag = true;
        }
        if (rc.getLocation().distanceSquaredTo(BabyRat.searchingLocation) <= 8){
            reset();
        }
        BugNavigator.moveTo(BabyRat.searchingLocation, breakDirt, true);
    }

    public static void doRandomMove() throws GameActionException {
        doRandomMove(false);
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
            if (dist > maxDist) {
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