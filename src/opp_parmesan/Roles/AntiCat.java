package opp_parmesan.Roles;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import opp_parmesan.Utils.*;

public class AntiCat extends Globals {
    public static int lastTime = -1, inARow = 0;
    public static void suicide () throws GameActionException {
        Attack.attackCat();
    }
    public static boolean run (MapLocation catLoc) throws GameActionException {
        if (lastTime == rc.getRoundNum()-1) {
            inARow++;
        } else
            inARow = 1;
        MapLocation kingLoc = kingPos[0];
        if (rc.getLocation().distanceSquaredTo(kingLoc) < 8) {
            Direction dir1 = kingLoc.directionTo(catLoc);
            Direction dir2 = kingLoc.add(dir1).directionTo(catLoc);
            MapLocation attempt = kingLoc.add(dir1).add(dir2);
            attempt = attempt.add(attempt.directionTo(catLoc));
            if (rc.canTurn()) {
                Vision.turn(rc.getLocation().directionTo(catLoc));
            }
            if (Vision.isDirt(attempt)) {
                dir2 = dir2.rotateLeft();
                attempt = kingLoc.add(dir1).add(dir2);
                if (Vision.isDirt(attempt)) {
                    dir2 = dir2.rotateRight().rotateRight();
                    attempt = kingLoc.add(dir1).add(dir2);
                }
            }
            if (rc.getLocation() == attempt) {
                Movement.circle(kingLoc);
            } else if (!rc.getLocation().isWithinDistanceSquared(attempt, 2))
                Navigator.moveTo(attempt, false);
            if (rc.canPlaceDirt(attempt)) {
                rc.placeDirt(attempt);
                Vision.placeDirt(attempt);
            }
            return true;
        }
        if (inARow > 10) {
            suicide();
            return true;
        }
        return false;
    }
}
