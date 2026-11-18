package opp_obg;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;


/**
 * Main class used to explore the map. At a high level the getExplore3Target (there were two other versions before!) samples a random direction
 * and attempts to follow it until the edge of the map. Then it chooses another direction prioritizing those that are not immediately adjacent.
 */
public class ExplorationManager {

    static MapLocation explore3Target;
    static Direction exploreDir = null;

    static void initExploreDir() throws GameActionException {
        if (MyUnit.isKing){
            setCenterDir();
            return;
        }
        assignExplore3Dir(CommunicationManager.getKingLocation().directionTo(MovementManager.myLoc));
    }

    static void setCenterDir(){
        MapLocation mySym = new MapLocation(MyUnit.dim1 - MovementManager.myLoc.x - 1,  MyUnit.dim2 - MovementManager.myLoc.y - 1);
        Direction dir = MovementManager.myLoc.directionTo(mySym);
        if (dir != Direction.CENTER){
            assignExplore3Dir(dir);
            return;
        }
        assignExplore3Dir(Direction.NORTHEAST);
    }

    static MapLocation getExplore3Target(){
        checkDirection();
        return explore3Target;
    }

    static void assignExplore3Dir(Direction dir){
        exploreDir = dir;
        int off = MyUnit.isKing ? 1 : 0;
        int diffX = Constants.INF, diffY = Constants.INF;
        if (dir.dx > 0) diffX = MyUnit.dim1 - MyUnit.rc.getLocation().x - 1 - off;
        else if (dir.dx < 0) diffX = MyUnit.rc.getLocation().x - off;
        if (dir.dy > 0) diffY = MyUnit.dim2 - MyUnit.rc.getLocation().y - 1 - off;
        else if (dir.dy < 0) diffY = MyUnit.rc.getLocation().y - off;
        int diff = diffX;
        if (diffY < diffX) diff = diffY;
        explore3Target = MyUnit.rc.getLocation().translate(diff*dir.dx, diff*dir.dy);
    }

    static void checkDirection(){
        if (!movingOutOfMap(exploreDir)) return;
        resetDirection();

    }

    static void resetDirection(){
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

        int randomDir = MyUnit.random.nextInt(cont);

        assignExplore3Dir(possibleDirs[randomDir]);
    }


    /**
     * Can probably be done much better, but this was copied from an older code
     * in which we didn't know the size of the map.
     */
    static boolean movingOutOfMap(Direction dir){
        try {
            MapLocation loc = MyUnit.rc.getLocation().add(dir);
            if (!MyUnit.rc.onTheMap(loc)) {
                return true;
            }
            loc = loc.add(dir);
            if (!MyUnit.rc.onTheMap(loc)) {
                return true;
            }
            loc = loc.add(dir);
            if (!MyUnit.rc.onTheMap(loc)) {
                return true;
            }
            loc = loc.add(dir);
            if (MyUnit.rc.canSenseLocation(loc) && !MyUnit.rc.onTheMap(loc)) {
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


}
