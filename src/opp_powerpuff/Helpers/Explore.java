package opp_powerpuff.Helpers;

import opp_powerpuff.Rat;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Utils.RNG;
import battlecode.common.*;

public class Explore {
    public static MapLocation explore3Target = null;
    public static Direction exploreDir = null;
    // public static int lastExploreRound;

    public static MapLocation getExploreTarget() {
        checkDirection();
        return explore3Target;
    }

    public static void assignExplore(MapLocation loc, boolean towards) throws GameActionException {
        if (loc != null)
            if (towards)
                assignExplore3Dir(Rat.dirTo(loc));
            else {
                assignExplore3Dir(Rat.dirTo(loc).opposite());
            }
        else 
            if (
                RNG.rng.nextInt(100) <= Constants.EXPLORE_SYMMETRY_SCORE ||
                (
                    Rat.rc.getRoundNum() > Rat.startDuration() && (
                        Rat.rc.getAllCheese() > Constants.WEALTHY_CHEESE_THRESHOLD
                    )
                )
            ) {
                MapLocation ref = MapData.priorityReflection(Global.readSpawn());
                if (ref != null) { 
                    assignExplore3Dir(Rat.dirTo(ref));
                } else {               
                    assignExplore3Dir(Constants.adjacentDirections[RNG.rng.nextInt(8)]);
                }
            }
            else
                assignExplore3Dir(Constants.adjacentDirections[RNG.rng.nextInt(8)]);
    }

    public static boolean expired() throws GameActionException {
        return movingOutOfMap(exploreDir);
    }

    public static void assignExplore3Dir(Direction dir){
        exploreDir = dir;
        int diffX = Constants.INF, diffY = Constants.INF;
        if (dir.dx > 0) diffX = Rat.W - Rat.rc.getLocation().x - 1;
        else if (dir.dx < 0) diffX = Rat.rc.getLocation().x;
        if (dir.dy > 0) diffY = Rat.H - Rat.rc.getLocation().y - 1;
        else if (dir.dy < 0) diffY = Rat.rc.getLocation().y;
        int diff = diffX;
        if (diffY < diffX) diff = diffY;
        explore3Target = Rat.rc.getLocation().translate(diff*dir.dx, diff*dir.dy);
        // lastExploreRound = Rat.rc.getRoundNum();
    }

    public static void checkDirection(){
        if (movingOutOfMap(exploreDir)) {
            Direction[] possibleDirs = new Direction[8];
            int cont = 0;
            Direction d = exploreDir.rotateLeft().rotateLeft();
            if (!movingOutOfMap(d)) possibleDirs[cont++] = d;
            d = d.rotateLeft();
            if (!movingOutOfMap(d)) possibleDirs[cont++] = d;
            d = d.rotateLeft();
            if (!movingOutOfMap(d)) possibleDirs[cont++] = d;
            d = d.rotateLeft();
            if (!movingOutOfMap(d)) possibleDirs[cont++] = d;
            d = d.rotateLeft();
            if (!movingOutOfMap(d)) possibleDirs[cont++] = d;
            if (cont == 0) {
                d = d.rotateLeft();
                if (!movingOutOfMap(d)) possibleDirs[cont++] = d;
                d = d.rotateLeft();
                d = d.rotateLeft();
                if (!movingOutOfMap(d)) possibleDirs[cont++] = d;
            }
    
            int randomDir = RNG.rng.nextInt(cont);
            assignExplore3Dir(possibleDirs[randomDir]);
        }

    }

    public static boolean movingOutOfMap(Direction dir){
        MapLocation loc = Rat.rc.getLocation().translate(dir.getDeltaX() * 3, dir.getDeltaY() * 3);
        return !Rat.rc.onTheMap(loc);
    }

    // public static boolean exploringForTooLong() {
    //     int maxExploreRounds = (int)((Rat.W + Rat.H) / 2f * MAX_EXPLORE_ROUNDS_MULTIPLIER);
    //     return Rat.rc.getRoundNum() - lastExploreRound > maxExploreRounds;
    // }
}
