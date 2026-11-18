package opp_powerpuff.Actions;

import opp_powerpuff.Helpers.Constants;

import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Helpers.Target;
import opp_powerpuff.Helpers.Vision;

public class Cat {
    private static Direction oldCatDir = null;
    private static int catTrapCooldown = 0;
    // private static int radius = Constants.ALLY_RADIUS;
    // private static int allies = 3;
    public static boolean run() throws GameActionException {
        RobotController rc = Rat.rc;
        

        if (VisionManager.cats.length > 0) {
            MapLocation catLoc = VisionManager.closestCat.getLocation();
            Direction dirToCat = Rat.dirTo(catLoc);
            Direction catDir = VisionManager.closestCat.getDirection();
            // // change to entire radius
            // if (Rat.rc.getRoundNum() > Constants.ATTACK_CAT_ROUND_AGGRESSIVE) {
            //     allies = 7;
            //     threshold = Constants.MIN_CHEESE_THRESHOLD;
            // }
            if (Rat.rc.getGlobalCheese() > Constants.MIN_CHEESE_THRESHOLD &&
                    Rat.rc.getRawCheese() == 0 &&
                    Rat.rc.getRoundNum() > Rat.startDuration() * 2
                ) {
                if (weWillKillThisCat(VisionManager.closestCat))
                    return true;
            }

            if (rc.isActionReady()) {
                int excess = 0; //Math.max(0, rc.getRawCheese() - Constants.CHEESE_DEFAULT_LIMIT);
                if (rc.canAttack(catLoc, excess))
                    rc.attack(catLoc, excess);
                if (rc.canAttack(catLoc.add(Direction.NORTH), excess))
                    rc.attack(catLoc.add(Direction.NORTH), excess);
                if (rc.canAttack(catLoc.add(Direction.EAST), excess))
                    rc.attack(catLoc.add(Direction.EAST), excess);
                if (rc.canAttack(catLoc.add(Direction.NORTHEAST), excess))
                    rc.attack(catLoc.add(Direction.NORTHEAST), excess);
            }
            if (
                dirToCat == catDir.opposite() ||
                dirToCat.rotateLeft() == catDir.opposite() ||
                dirToCat.rotateRight() == catDir.opposite() ||
                dirToCat.rotateLeft() == catDir.opposite() ||
                dirToCat.rotateRight() == catDir.opposite()
            )
            Target.setAwayCatLocation(VisionManager.closestCat.getLocation());
            return false;
            
        }
        return false;

    }

    public static boolean weWillKillThisCat(RobotInfo cat) throws GameActionException {
        // next to cat logic here
        Direction dir = Rat.rc.getDirection();
        Direction dirToKing = Rat.rc.getLocation().directionTo(VisionManager.allyRatKingLocation);

        if (Vision.catIsFacingLocation(Rat.rc.getLocation(), cat) && Rat.rc.getLocation().distanceSquaredTo(closestCatTile(VisionManager.closestCat.getLocation())) > 4) {
            if (Rat.rc.canPlaceCatTrap(Rat.rc.getLocation().add(dir)) && catTrapCooldown == 0) {
                Rat.rc.placeCatTrap(Rat.rc.getLocation().add(dir));
                catTrapCooldown = Constants.CAT_TRAP_COOLDOWN;
                //Rat.rc.squeak(0);
            }
            catTrapCooldown --;
            return false;
        }
        if (oldCatDir == null) {
            oldCatDir = cat.getDirection();
        }
        else if (cat.getDirection() != oldCatDir) {
            if (turnedLeft(cat.getDirection())) {
                //System.out.println("circling counterclocwise");
                if (Rat.rc.canMove(dir.rotateRight())) {
                    Rat.rc.move(dir.rotateRight());
                    if (Rat.rc.canTurn())
                        Rat.rc.turn(Rat.rc.getLocation().directionTo(cat.getLocation()));
                }
                else if (Rat.rc.canMove(dir.rotateRight().rotateRight())) {
                    Rat.rc.move(dir.rotateRight().rotateRight());
                    if (Rat.rc.canTurn())
                        Rat.rc.turn(Rat.rc.getLocation().directionTo(cat.getLocation()));
                }
            } else {
                //System.out.println("circling clockwise");
                if (Rat.rc.canMove(dir.rotateLeft())) {
                    Rat.rc.move(dir.rotateLeft());
                    if (Rat.rc.canTurn())
                        Rat.rc.turn(Rat.rc.getLocation().directionTo(cat.getLocation()));
                }
                else if (Rat.rc.canMove(dir.rotateLeft().rotateLeft())) {
                    Rat.rc.move(dir.rotateLeft().rotateLeft());
                    if (Rat.rc.canTurn())
                        Rat.rc.turn(Rat.rc.getLocation().directionTo(cat.getLocation()));
                }
            }
            oldCatDir = cat.getDirection();
        }
        // if cat is in front of us
        if (adjToCat(cat.getLocation())) {
            if (Rat.rc.canTurn() && Rat.rc.getDirection() != Rat.rc.getLocation().directionTo(cat.getLocation()))
                Rat.rc.turn(Rat.rc.getLocation().directionTo(cat.getLocation()));
            // if right in front of us, we want to attack
            if (Rat.rc.canAttack(Rat.rc.getLocation().add(Rat.rc.getDirection())))
                Rat.rc.attack(Rat.rc.getLocation().add(Rat.rc.getDirection()));  
            else if (Rat.rc.canAttack(cat.getLocation()))
                Rat.rc.attack(cat.getLocation());         
        }
        // follow the cat logic here
        else {
            Nav.moveTo((closestCatTile(cat.getLocation())));
            MapLocation front = Rat.rc.getLocation().add(Rat.rc.getDirection());
            if (Rat.rc.onTheMap(front) && Rat.rc.senseRobotAtLocation(front) == cat) {
            // if right in front of us, we want to attack
            if (Rat.rc.canAttack(Rat.rc.getLocation().add(Rat.rc.getDirection()))  && Rat.rc.isActionReady())
                Rat.rc.attack(Rat.rc.getLocation().add(Rat.rc.getDirection()));            
            }
        }
        catTrapCooldown --;
        return true;
    }
    public static boolean turnedLeft(Direction newDir) throws GameActionException {
        if (oldCatDir == null)
            return false;
        if (newDir == oldCatDir.rotateLeft() || newDir == oldCatDir.rotateLeft().rotateLeft())
            return true;
        return false;
    }
    public static boolean adjToCat(MapLocation catLoc) throws GameActionException {
        MapLocation loc = Rat.rc.getLocation();
        if (loc.isAdjacentTo(catLoc) || 
            loc.isAdjacentTo(catLoc.add(Direction.NORTH)) || 
            loc.isAdjacentTo(catLoc.add(Direction.EAST)) || 
            loc.isAdjacentTo(catLoc.add(Direction.NORTHEAST)))
            return true;
        return false;
    }
    public static MapLocation closestCatTile(MapLocation catLoc) throws GameActionException {
        MapLocation loc = Rat.rc.getLocation();

        MapLocation best = catLoc;
        int bestDist = loc.distanceSquaredTo(catLoc);

        MapLocation north = catLoc.add(Direction.NORTH);
        if (Rat.rc.onTheMap(north)) {
            int d = loc.distanceSquaredTo(north);
            if (d < bestDist) {
                bestDist = d;
                best = north;
            }
        }

        MapLocation east = catLoc.add(Direction.EAST);
        if (Rat.rc.onTheMap(east)) {
            int d = loc.distanceSquaredTo(east);
            if (d < bestDist) {
                bestDist = d;
                best = east;
            }
        }

        MapLocation ne = catLoc.add(Direction.NORTHEAST);
        if (Rat.rc.onTheMap(ne)) {
            int d = loc.distanceSquaredTo(ne);
            if (d < bestDist) {
                bestDist = d;
                best = ne;
            }
        }

        return best;
    }
    
}
