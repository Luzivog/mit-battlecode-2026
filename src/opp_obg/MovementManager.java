package opp_obg;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

/**
 * Class used to move our units.
 */
public abstract class MovementManager {

    static MapLocation myLoc;

    static boolean rotateRight = true;

    static Direction movedDir;

    static boolean[] forbidden;

    static MapLocation lastTurnLoc;

    static Direction pushDir = Direction.CENTER;

    static FastIntSet flyingIDs = new FastIntSet();

    static void updateLastTurnLoc(){
        lastTurnLoc = myLoc;
    }

    static void update(){
        forbidden = new boolean[9];
        myLoc = MyUnit.rc.getLocation();
        if (lastTurnLoc != null) pushDir = lastTurnLoc.directionTo(myLoc);
        flyingIDs.clear();
    }

    static void init() throws GameActionException {
        myLoc = MyUnit.rc.getLocation();
        if (MyUnit.isKing) CommunicationManager.kingLoc = myLoc;
        else CommunicationManager.readKingLocation();
    }

    static boolean canMove(Direction dir, boolean checkDirt) {
        return canMove(dir, checkDirt, true);
    }

    static boolean canMove(Direction dir, boolean checkDirt, boolean checkCats){
        if (!MyUnit.isKing && forbidden[dir.ordinal()]) return false;
        if (checkCats && !MyUnit.isKing){
            boolean b = switch(dir){
                case NORTH -> MicroManager.microInfo0.seenByCat();
                case NORTHEAST -> MicroManager.microInfo1.seenByCat();
                case EAST -> MicroManager.microInfo2.seenByCat();
                case SOUTHEAST -> MicroManager.microInfo3.seenByCat();
                case SOUTH -> MicroManager.microInfo4.seenByCat();
                case SOUTHWEST -> MicroManager.microInfo5.seenByCat();
                case WEST -> MicroManager.microInfo6.seenByCat();
                case NORTHWEST -> MicroManager.microInfo7.seenByCat();
                default -> MicroManager.microInfo8.seenByCat();
            };
            if (b) return false;
        }
        else {
            if (CatSurveillance.lastCatSeen.loc != null && CatSurveillance.round+15 > MyUnit.rc.getRoundNum()){
                MapLocation newLoc = myLoc.add(dir);
                if (newLoc.distanceSquaredTo(CatSurveillance.lastCatSeen.loc) < myLoc.distanceSquaredTo(CatSurveillance.lastCatSeen.loc)) return false;
            }
        }
        if (checkDirt && DirtManager.dirt(dir)) return true;
        return MyUnit.rc.canMove(dir);
    }

    static boolean canMoveNoChecks(Direction dir){
        return MyUnit.rc.canMove(dir);
    }

    private static void move(Direction dir) throws GameActionException{
        if (dir == MyUnit.rc.getDirection()) moveForwardWrapper();
        else moveWrapper(dir);
    }

    private static boolean moveWrapper(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER) return false;
        boolean canTurn = true;
        MapLocation dirtLoc = DirtManager.getDirt(dir);
        if (dirtLoc != null){
            canTurn = false;
            if (MyUnit.rc.canRemoveDirt(dirtLoc)) {
                MyUnit.rc.removeDirt(dirtLoc);
                if (!MyUnit.rc.canMove(dir)) return false;
            }
            else return canTurn;
        }
        MyUnit.rc.move(dir);
        movedDir = dir;
        myLoc = MyUnit.rc.getLocation();
        if (MyUnit.isKing) CommunicationManager.kingLoc = myLoc;
        return canTurn;
    }

    private static boolean moveForwardWrapper() throws GameActionException {
        boolean canTurn = true;
        MapLocation dirtLoc = DirtManager.getDirt(MyUnit.rc.getDirection());
        if (dirtLoc != null){
            canTurn = false;
            if (MyUnit.rc.canRemoveDirt(dirtLoc)) {
                MyUnit.rc.removeDirt(dirtLoc);
                if (!MyUnit.rc.canMoveForward()) return false;
            }
            else return canTurn;
        }
        MyUnit.rc.moveForward();
        movedDir = MyUnit.rc.getDirection();
        myLoc = MyUnit.rc.getLocation();
        if (MyUnit.isKing) CommunicationManager.kingLoc = myLoc;
        //VisionManager.update();
        return canTurn;
    }

    /*tries to save CD by turning before/after*/
    static void moveFreeDir(Direction dir) throws GameActionException {
        if (dir == MyUnit.rc.getDirection()){
            if (!moveForwardWrapper()) return;
            Direction newDir;
            if (rotateRight) newDir = dir.rotateRight().rotateRight();
            else newDir = dir.rotateLeft().rotateLeft();
            if (canTurn(newDir)){
                turn(newDir);
                rotateRight = !rotateRight;
            }
        }
        else {
            if (canTurn(dir)){
                turn(dir);
                moveForwardWrapper();
            }
            else {
                moveWrapper(dir);
            }
        }
        VisionManager.updatePostMovement();
    }

    static boolean canTurn(Direction dir){
        if (dir == null || dir == Direction.CENTER) return false;
        return MyUnit.rc.canTurn();
    }

    static void turn(Direction dir) throws GameActionException {
        if (dir == null || dir == Direction.CENTER) return;
        MyUnit.rc.turn(dir);
    }

    static void moveWhileLooking(Direction dir, MapLocation loc, boolean checkDirt, boolean checkCats) throws GameActionException {
        if (!canMove(dir, checkDirt, checkCats)){
            Direction newDir = myLoc.directionTo(loc);
            if (canTurn(newDir)){
                turn(newDir);
                VisionManager.updatePostMovement();
            }
            return;
        }
        MapLocation newLoc = myLoc.add(dir);
        Direction newDir = newLoc.directionTo(loc);
        if (newDir == dir){
            if (canTurn(dir)) turn(dir);
            move(dir);
            VisionManager.updatePostMovement();
            return;
        }
        move(dir);
        if (canTurn(newDir)) turn(newDir);
        VisionManager.updatePostMovement();
    }

    static boolean moveWhileLooking(Direction moveDir, Direction dir) throws GameActionException {
        if (!canMove(moveDir, false, false)) return false;
        if (!MyUnit.rc.canTurn()) return false;
        if (moveDir == MyUnit.rc.getDirection()){
            moveForwardWrapper();
            if (dir == moveDir){
                VisionManager.updatePostMovement();
                return true;
            }
            turn(dir);
            VisionManager.updatePostMovement();
            return true;
        }
        turn(dir);
        move(moveDir);
        VisionManager.updatePostMovement();
        return true;
    }

    static void moveWhileLooking(Direction dir, MapLocation loc, boolean checkDirt) throws GameActionException {
        moveWhileLooking(dir, loc, checkDirt, true);
    }

    static void markFlying(MapLocation flyingLoc, Direction dir){
        MapLocation loc = MovementManager.myLoc;
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[8] = true;
        loc = MovementManager.myLoc.add(Direction.NORTH);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[0] = true;
        loc = MovementManager.myLoc.add(Direction.NORTHEAST);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[1] = true;
        loc = MovementManager.myLoc.add(Direction.EAST);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[2] = true;
        loc = MovementManager.myLoc.add(Direction.SOUTHEAST);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[3] = true;
        loc = MovementManager.myLoc.add(Direction.SOUTH);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[4] = true;
        loc = MovementManager.myLoc.add(Direction.SOUTHWEST);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[5] = true;
        loc = MovementManager.myLoc.add(Direction.WEST);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[6] = true;
        loc = MovementManager.myLoc.add(Direction.NORTHWEST);
        if ((loc.x - flyingLoc.x)*dir.dx - (loc.y - flyingLoc.y)*dir.dy == 0) forbidden[7] = true;
    }

}
