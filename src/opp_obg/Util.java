package opp_obg;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Util {

    static MapLocation getClosestCatLoc(MapLocation catLoc){
        MapLocation closestLoc = catLoc;
        int dist = MovementManager.myLoc.distanceSquaredTo(closestLoc);

        MapLocation newLoc = catLoc.add(Direction.NORTH);
        int newDist = MovementManager.myLoc.distanceSquaredTo(newLoc);
        if (newDist < dist){
            closestLoc = newLoc;
            dist = newDist;
        }
        newLoc = catLoc.add(Direction.EAST);
        newDist = MovementManager.myLoc.distanceSquaredTo(newLoc);
        if (newDist < dist){
            closestLoc = newLoc;
            dist = newDist;
        }
        newLoc = catLoc.add(Direction.NORTHEAST);
        newDist = MovementManager.myLoc.distanceSquaredTo(newLoc);
        if (newDist < dist){
            closestLoc = newLoc;
        }
        return closestLoc;
    }

    static int catDist(MapLocation catLoc, MapLocation loc){
        int dx = 2*(catLoc.x - loc.x) + 1, dy =  2*(catLoc.y - loc.y) + 1;
        return dx*dx + dy*dy;
    }

}
