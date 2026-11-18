package opp_powerpuff.Rats;

import opp_powerpuff.Utils.Encoder;
import opp_powerpuff.Rat;
import opp_powerpuff.Actions.AttackKing;
import opp_powerpuff.Actions.Cat;
import opp_powerpuff.Actions.Cheese;
import opp_powerpuff.Actions.Deliver;
import opp_powerpuff.Actions.MergeKing;
import opp_powerpuff.Actions.Search;
import opp_powerpuff.Actions.Squeak;
import opp_powerpuff.Actions.ThrowOutsideCombat;
import opp_powerpuff.Helpers.BugNav;
import opp_powerpuff.Helpers.ClosestUnseenEnemy;
import opp_powerpuff.Helpers.Comms;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Explore;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Helpers.Target;
import opp_powerpuff.Helpers.Vision;
import opp_powerpuff.Helpers.WallScan;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Micro.MicroBabyManager;
import battlecode.common.*;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;

public class BabyRat extends Rat {
    public BabyRat(RobotController rc) throws GameActionException {
        super(rc);
        MapData.init();
        VisionManager.init(rc);
        Explore.assignExplore(VisionManager.allyRatKingLocation, false);
        Target.targetLocation = Explore.getExploreTarget();
    }

    public void start() throws GameActionException {
        super.start();
        VisionManager.updateEnemiesAndCats();

        MapLocation enemyLoc = ClosestUnseenEnemy.run();
        if (enemyLoc != null && !rc.getLocation().equals(enemyLoc)) {
            if (
                (
                    VisionManager.closestEnemy == null ||
                    Rat.distTo(enemyLoc) < Rat.distTo(VisionManager.closestEnemy.getLocation()) + 1
                ) &&
                Rat.distTo(enemyLoc) < Constants.ALLY_ASSIST_RANGE
            ) {
                if (rc.canTurn()) rc.turn(Rat.dirTo(enemyLoc));
                VisionManager.updateEnemiesAndCats();
                if (VisionManager.closestEnemy == null)
                    BugNav.moveTo(enemyLoc);
            }
        }
            
        MapData.update(VisionManager.mapInfos);           
    }

    public void turn() throws GameActionException {
        RobotController rc = Rat.rc;
        boolean beingCarried = rc.isBeingCarried();

        if (beingCarried) {
            MapLocation currentLoc = Rat.rc.getLocation();
            Comms.squeakEnemyBabyRatLocation(currentLoc);
            return;
        }    
        
        // if (VisionManager.closestEnemy != null && VisionManager.closestEnemy.getType() == UnitType.RAT_KING) {
        //     AttackKing.run(VisionManager.closestEnemy);
        //     return;
        // }
        VisionManager.messages = rc.readSqueaks(rc.getRoundNum() - 1);
        
        if (VisionManager.closestEnemy != null && 
            shouldDoMicro(VisionManager.closestEnemy.getLocation()) && !shouldIgnore() &&
            ratCloserThanWall()) {
                
            MicroBabyManager.doMicro();
            Comms.squeakEnemyBabyRatLocation(VisionManager.closestEnemy.getLocation());
            
            if (VisionManager.closestEnemy.getType() == UnitType.RAT_KING){
                attackKing();
            }
            
            Cheese.afterPick();
            return;
        }
        else {
            VisionManager.updateCheese();
            VisionManager.updateAllies();
            VisionManager.updateKingLocation();
            if (Rat.rc.getRawCheese() < 20) {
                for (Message message : VisionManager.messages) {
                    if (Encoder.getType(message.getBytes()) == 3) {
                        MapLocation e = new MapLocation(Encoder.getX(message.getBytes()), Encoder.getY(message.getBytes()));
                        Nav.moveTo(e);
                        break;
                    }
                }
            }
        }

        ThrowOutsideCombat.toss();
        if (MergeKing.buildKing()) return;
        if (MergeKing.buildKingOnClump()) return;
        

        //perhaps change the conditions for Cat.run -- 
        if (VisionManager.enemies.length == 0) {
            if (Cat.run())
                return;
        }
        
        Cheese.afterPick();
        Deliver.transmit(VisionManager.allyRatKingLocation);
        if (!rc.isActionReady() && !rc.isMovementReady()) return;

        MapLocation rkLoc = VisionManager.allyRatKingLocation;
        if (Global.readAlarm() > 0 && rc.getLocation().distanceSquaredTo(rkLoc) <= Constants.EMERGENCY_RADIUS_SQUARED) {
            Nav.moveTo(rkLoc);
            Cheese.afterPick();
        }
        else {
            if (Cheese.check())
                Cheese.run();
            else if (Search.check()) {
                Search.run();
            }
            else if (Deliver.check()) {
                Deliver.run();           
            } else {
                MapLocation targetLoc = Target.getTargetLocation();
                rc.setIndicatorDot(targetLoc, 0, 255, 0);
                Nav.moveTo(targetLoc);
            }
        }

        Cheese.afterPick();
    }

    public void end() throws GameActionException {
        if (Clock.getBytecodesLeft() > Constants.BYTECODE_REQ_SYMMETRY) {
            MapData.updateSymmmetry(VisionManager.mapInfos);
        }
    }

    public boolean shouldDoMicro(MapLocation closestEnemyLoc) throws GameActionException {
        if (VisionManager.enemies.length == 0) return false;
        
        Direction toAllyKing = Rat.dirTo(VisionManager.allyRatKingLocation);
        Direction toClosestEnemy = Rat.dirTo(closestEnemyLoc);
        int distToClosestEnemy = Rat.distTo(closestEnemyLoc);

        return !(
            Deliver.check() &&
            (
                toAllyKing != toClosestEnemy &&
                toAllyKing != toClosestEnemy.rotateLeft() &&
                toAllyKing != toClosestEnemy.rotateRight()
            ) &&
            distToClosestEnemy >= Constants.ALLY_ASSIST_RANGE
        );
    }
    
    public boolean shouldIgnore() throws GameActionException {
        if (VisionManager.closestEnemy.getLocation().isAdjacentTo(Rat.rc.getLocation()))
            return false;
        if (WallScan.adjacentWall()) {
            return true;
        }
        Direction d = Rat.rc.getDirection();
        Direction dirToEnemy = Rat.rc.getLocation().directionTo(VisionManager.closestEnemy.getLocation());
        if (dirToEnemy == d && (d == Direction.NORTH || d == Direction.WEST || d == Direction.SOUTH || d == Direction.EAST)) {
            return WallScan.frontWall();
        }
        if ((d == Direction.NORTH || d == Direction.WEST || d == Direction.SOUTH || d == Direction.EAST)) {
            if (dirToEnemy == d.rotateLeft() || dirToEnemy == d.rotateLeft().rotateLeft())
                return WallScan.sideWall(d.rotateLeft().rotateLeft());
            return WallScan.sideWall(d.rotateRight().rotateRight());
        }
        else {
            return WallScan.diagWall();
        }
    }
    public boolean ratCloserThanWall() throws GameActionException {
        Direction d = Rat.rc.getDirection();
        MapLocation location = Rat.rc.getLocation().add(d);
        MapInfo wall = null;
        while (Rat.rc.onTheMap(location) && Rat.rc.canSenseLocation(location)) {
            MapInfo info = Rat.rc.senseMapInfo(location);
            if (!info.isPassable()) {
                wall = info;
                break;
            }
            location = location.add(d);
        }
        if (wall == null) {
            return true;
        }
        if (Rat.rc.getLocation().distanceSquaredTo(VisionManager.closestEnemy.getLocation()) 
            <= Rat.rc.getLocation().distanceSquaredTo(wall.getMapLocation())) {
                
            return true;
        }
        return false;
    }
    public boolean attackKing() throws GameActionException {
        Direction dirToKing = Rat.rc.getLocation().directionTo(VisionManager.closestEnemy.getLocation());
        MapLocation adjKing = VisionManager.closestEnemy.getLocation().add(dirToKing.opposite());
        if (Rat.rc.getLocation().isAdjacentTo(adjKing) && Rat.rc.canAttack(adjKing)) {
            Rat.rc.attack(adjKing);
            return true;
        }
        return false;
    }
}