package opp_powerpuff.Actions;

import opp_powerpuff.Rat;
import battlecode.common.*;
import opp_powerpuff.Helpers.Nav;

public class AttackKing {
    private static boolean left = true;
    // move around the king while hitting it
    public static void run(RobotInfo ratKing) throws GameActionException {
        MapLocation adjKing = ratKing.getLocation().add(ratKing.getLocation().directionTo(Rat.rc.getLocation()));
        Direction d = Rat.rc.getLocation().directionTo(adjKing);

        if (Rat.rc.getLocation().isAdjacentTo(adjKing)) {
            MapLocation diag = null;
            RobotInfo robot = null;

            // make sure that rats are set correctly to circle
            if (Rat.rc.getLocation().x == ratKing.getLocation().x || Rat.rc.getLocation().y == ratKing.getLocation().y) {
                if (left) {
                    if (Rat.rc.getDirection() != d.rotateRight().rotateRight())
                        Rat.rc.turn(d.rotateRight().rotateRight());
                }
                else {
                    if (Rat.rc.getDirection() != d.rotateLeft().rotateLeft())
                        Rat.rc.turn(d.rotateLeft().rotateLeft());
                }
            }
            MapLocation directInFront = Rat.rc.getLocation().add(Rat.rc.getDirection());
            if (!Rat.rc.onTheMap(directInFront)) {
                left = !left;
                if (Rat.rc.canTurn())
                    Rat.rc.turn(Rat.rc.getDirection().opposite());
                return;
            }
            // if (Rat.rc.senseMapInfo(directInFront).isWall() || Rat.rc.senseMapInfo(directInFront).isDirt() || Rat.rc.senseRobotAtLocation(directInFront) != null) {
            //     left = !left;
            //     if (Rat.rc.canTurn())
            //         Rat.rc.turn(Rat.rc.getDirection().opposite());
            //     return;
            // }
            if (Rat.rc.senseRobotAtLocation(directInFront) != null) {
                if (Rat.rc.canAttack(directInFront))
                    Rat.rc.attack(directInFront);
                Direction dirMove = null;
                if (left)
                    dirMove = Rat.rc.getDirection().rotateRight();
                else 
                    dirMove = Rat.rc.getDirection().rotateLeft();
                if (Rat.rc.canMove(dirMove))
                    Rat.rc.move(dirMove);
            }
            else if (Rat.rc.canMoveForward() && Rat.rc.canTurn()) {
                if (left)
                    diag = Rat.rc.getLocation().add(Rat.rc.getDirection().rotateLeft());
                else
                    diag = Rat.rc.getLocation().add(Rat.rc.getDirection().rotateRight());
                if (Rat.rc.senseRobotAtLocation(diag) != null) {
                    if (Rat.rc.canAttack(diag))
                        Rat.rc.attack(diag);
                }
                Rat.rc.moveForward();
                if (left)
                    diag = Rat.rc.getLocation().add(Rat.rc.getDirection().rotateLeft());
                else
                    diag = Rat.rc.getLocation().add(Rat.rc.getDirection().rotateRight());
                if (!Rat.rc.onTheMap(diag)) {
                    left = !left;
                    if (Rat.rc.canTurn())
                        Rat.rc.turn(Rat.rc.getDirection().opposite());
                    return;
                }
                if (Rat.rc.onTheMap(diag)) {
                    robot = Rat.rc.senseRobotAtLocation(diag);
                }
                if (robot == null) {
                    if (left)
                        Rat.rc.turn(Rat.rc.getDirection().rotateLeft().rotateLeft());
                    else
                        Rat.rc.turn(Rat.rc.getDirection().rotateRight().rotateRight());
                }
            }
        }
        
        else {
            Nav.moveTo(ratKing.getLocation());
            adjKing = ratKing.getLocation().add(ratKing.getLocation().directionTo(Rat.rc.getLocation()));
            if (Rat.rc.getLocation().isAdjacentTo(adjKing) && Rat.rc.canAttack(adjKing))
                Rat.rc.attack(adjKing);
        }
    }
}
