package opp_powerpuff.Helpers;
import opp_powerpuff.Rat;
import battlecode.common.*;

public class WallScan {
    
    public static boolean adjacentWall() throws GameActionException {
        Direction d = Rat.rc.getDirection();
        MapLocation center = Rat.rc.getLocation().add(d);
        MapLocation left = Rat.rc.getLocation().add(d.rotateLeft());
        MapLocation right = Rat.rc.getLocation().add(d.rotateRight());
        if (!Rat.rc.onTheMap(center))
            return false;
        if (!Rat.rc.onTheMap(right))
            return (!Rat.rc.senseMapInfo(left).isPassable() && !Rat.rc.senseMapInfo(center).isPassable());
        if (!Rat.rc.onTheMap(left))
            return (!Rat.rc.senseMapInfo(right).isPassable() && !Rat.rc.senseMapInfo(center).isPassable());
        if (d != Direction.NORTH && d != Direction.EAST && d != Direction.SOUTH && d != Direction.WEST) {
            if (!Rat.rc.senseMapInfo(left).isPassable() && !Rat.rc.senseMapInfo(center).isPassable())
                return true;
            if (!Rat.rc.senseMapInfo(right).isPassable() && !Rat.rc.senseMapInfo(center).isPassable())
                return true;
        }
        return (!Rat.rc.senseMapInfo(left).isPassable() && !Rat.rc.senseMapInfo(right).isPassable() && !Rat.rc.senseMapInfo(center).isPassable());
    }
    public static boolean frontWall() throws GameActionException {
        Direction d = Rat.rc.getDirection();
        Direction dLeft = d.rotateLeft().rotateLeft();
        Direction dRight = d.rotateRight().rotateRight();

        // check two blocks in front, we skip adj
        MapLocation oneFront = Rat.rc.getLocation().add(d).add(d);

        MapLocation left = null;
        MapLocation right = null;

        MapInfo leftInfo = null;
        MapInfo rightInfo = null;
        // if not passable, check the left and right of this block
        if (!Rat.rc.senseMapInfo(oneFront).isPassable()) {
            left = oneFront.add(dLeft);
            right = oneFront.add(dRight);
            // check if both are on the map
            if (Rat.rc.onTheMap(left)) 
                leftInfo = Rat.rc.senseMapInfo(left);
            if (Rat.rc.onTheMap(right)) 
                rightInfo = Rat.rc.senseMapInfo(right);
            // if right side is not on map, only check wall at left and center
            if (rightInfo == null) {
                if (!leftInfo.isPassable() && rightInfo == null)
                    return true;
            }
            // if left side is not on map, only check wall at right and center
            else if (leftInfo == null) {
                if (!rightInfo.isPassable() && leftInfo == null)
                    return true;
            }
            // check if left and right are wall
            else if (!leftInfo.isPassable() && !rightInfo.isPassable()) {
                return true;
            }
        }
        oneFront = oneFront.add(d);
        if (!Rat.rc.onTheMap(oneFront))
            return false;
        if (!Rat.rc.senseMapInfo(oneFront).isPassable()) {
            left = oneFront.add(dLeft);
            right = oneFront.add(dRight);
            if (Rat.rc.onTheMap(left)) 
                leftInfo = Rat.rc.senseMapInfo(left);
            if (Rat.rc.onTheMap(right)) 
                rightInfo = Rat.rc.senseMapInfo(right);
            if (rightInfo == null) {
                if (!leftInfo.isPassable() && rightInfo == null)
                    return true;
            }
            else if (leftInfo == null) {
                if (!rightInfo.isPassable() && leftInfo == null)
                    return true;
            }
            else if (!leftInfo.isPassable() && !rightInfo.isPassable()) {
                return true;
            }
        }
        return false;
    }
    public static boolean sideWall(Direction side) throws GameActionException {
        Direction d = Rat.rc.getDirection();
        MapLocation one = Rat.rc.getLocation().add(d).add(d).add(side);
        MapLocation two = one.add(d);
        MapLocation three = two.add(d);

        if (!Rat.rc.onTheMap(one) || !Rat.rc.onTheMap(two) || !Rat.rc.onTheMap(three))
            return false;
        else if (!Rat.rc.senseMapInfo(one).isPassable() && !Rat.rc.senseMapInfo(two).isPassable() && !Rat.rc.senseMapInfo(three).isPassable())
            return true;

        return false;
    }
    public static boolean diagWall() throws GameActionException {
        Direction d = Rat.rc.getDirection();
        Direction left = d.rotateLeft();
        Direction right = d.rotateRight();
        Direction bLeft = left.opposite();
        Direction bRight = right.opposite();

        MapLocation center = Rat.rc.getLocation().add(d).add(d);
        MapLocation leftLoc = center.add(left);
        MapLocation rightLoc = center.add(right);
        MapLocation backLeft = center.add(bLeft);
        MapLocation backRight = center.add(bRight);
        
        if (!Rat.rc.onTheMap(center))
            return false;
        boolean centerWall = !Rat.rc.senseMapInfo(center).isPassable();
        boolean backLeftWall = !Rat.rc.senseMapInfo(backLeft).isPassable();
        boolean backRightWall = !Rat.rc.senseMapInfo(backRight).isPassable();
        
        if (centerWall && backLeftWall && backRightWall)
            return true;

        
        if (!Rat.rc.onTheMap(leftLoc) && centerWall && backLeftWall)
            return true;
        if (!Rat.rc.onTheMap(rightLoc) && centerWall && backRightWall)
            return true;
        if (!Rat.rc.onTheMap(leftLoc))
            return false;
        if (!Rat.rc.onTheMap(rightLoc))
            return false;

        boolean leftWall = !Rat.rc.senseMapInfo(leftLoc).isPassable();
        boolean rightWall = !Rat.rc.senseMapInfo(rightLoc).isPassable();
        if (leftWall && centerWall && backLeftWall)
            return true;
        if (rightWall && centerWall && backRightWall)
            return true;
        return false;
    }
}
