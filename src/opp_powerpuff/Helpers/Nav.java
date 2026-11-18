package opp_powerpuff.Helpers;

import opp_powerpuff.Utils.RNG;
import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.Managers.*;

// UPDATE VISION MANAGER AND MAPDATA
public class Nav {
    public static final int ROUNDS_STUCK_IN_FORD = 4;
    public static final int FORD_BYTECODE_REQ = 12000;
    public static final int FORD_BYTECODE_AFTER = 4000;
    public static final int ROUNDS_STUCK_SAME_POS = 3;

    public static final int STARTING_BUGNAV_ROUNDS = 0;
    public static final int BUGNAV_ROUNDS = 12;

    private static MapLocation currentTarget = null;
    private static int minDistanceToTarget;
    private static int roundsSinceMovingCloserToTarget;

    private static boolean turnRight = true;
    private static int bugnavRounds = STARTING_BUGNAV_ROUNDS;    



    public static void moveTo(MapLocation target) throws GameActionException {
        if (!Rat.rc.isMovementReady()) return;

        if (currentTarget == null || currentTarget.distanceSquaredTo(target) > 10) {
            reset();
            currentTarget = target;
        }

        //System.out.println("BYTES LEFT " + Clock.getBytecodesLeft());
        if (bugnavRounds > 0) {
            bugnavRounds--;
            BugNav.moveTo(target);
            return;
        }

        RobotController rc = Rat.rc;
        MapLocation myLocation = rc.getLocation();
        if (myLocation.equals(target)) {
            return;
        }

        if (Rat.distTo(target) <= GameConstants.CHEESE_TRANSFER_RADIUS_SQUARED) if (Rat.rc.canTurn()) Rat.rc.turn(Rat.dirTo(target));

        Rat.rc.setIndicatorLine(Rat.rc.getLocation(), target, 255, 255, 255);



        currentTarget = target;

        int distanceToTarget = myLocation.distanceSquaredTo(target);
        if (distanceToTarget < minDistanceToTarget) {
            minDistanceToTarget = distanceToTarget;
            roundsSinceMovingCloserToTarget = 0;
        } else {
            roundsSinceMovingCloserToTarget++;
        }

        if (roundsSinceMovingCloserToTarget < ROUNDS_STUCK_IN_FORD * (1 + Rat.rc.getRawCheese() * GameConstants.CHEESE_COOLDOWN_PENALTY) && Clock.getBytecodesLeft() > FORD_BYTECODE_REQ) {
            Direction bellmanFordDirection; //= BellmanFordNavigator.getBestDirection(target);;
            if (rc.getRawCheese() >= Constants.CHEESE_RETREAT_LIMIT)
                bellmanFordDirection = BellmanFordNavigator.getBestDirection(target);
            else
                bellmanFordDirection = BellmanFordCheeseNavigator.getBestDirection(target);
            if (bellmanFordDirection != null) {
                Direction turnDir;
                if (turnRight)
                    turnDir = bellmanFordDirection.rotateRight();
                else
                    turnDir = bellmanFordDirection.rotateLeft();
                rc.setIndicatorDot(rc.adjacentLocation(bellmanFordDirection), 255, 0, 0);

                removeDirtPriority(bellmanFordDirection);
                if (rc.getRawCheese() >= Constants.CHEESE_DEFAULT_LIMIT) {
                    simpleMove(bellmanFordDirection);
                } else {
                    boolean success = strafeMove(bellmanFordDirection, turnDir);
                    if (success)
                        turnRight = !turnRight;
                }
                // System.out.println("bf " + bellmanFordDirection);
                return;
            } else {

            }
        } else {
            //System.out.println("bf n/a");
        }

        BugNav.moveTo(target);
    }

    public static void reset() {
        currentTarget = null;
        bugnavRounds = (int) (BUGNAV_ROUNDS * (1 + Rat.rc.getRawCheese() * GameConstants.CHEESE_COOLDOWN_PENALTY));
        BugNav.reset();
        minDistanceToTarget = Integer.MAX_VALUE;
        roundsSinceMovingCloserToTarget = 0;
    }

    public static void removeDirtPriority(Direction d) throws GameActionException {
        RobotController rc = Rat.rc;
        Direction currDir = rc.getDirection();
        MapLocation loc = rc.adjacentLocation(d);
        MapInfo knownInfo = MapData.getMapInfo(loc);
        if (
            knownInfo != null && 
            knownInfo.isDirt() && (
                d != currDir &&
                d != currDir.rotateLeft() &&
                d != currDir.rotateRight()
            )
        ) {
            if (rc.canTurn())
                rc.turn(d);
        }
        if (rc.canRemoveDirt(loc))
            rc.removeDirt(loc);
    }

    public static void fuzzyMove(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER) return;
        for(Direction d: fuzzyDirs(dir)){
            Nav.simpleMove(d);
        }
    }

    public static void fuzzyMove(MapLocation loc) throws GameActionException{
        fuzzyMove(Rat.dirTo(loc));
    }

    // CAN UNROLL
    public static Direction[] fuzzyDirs(Direction dir) throws GameActionException{
        Direction l = dir.rotateLeft();
        Direction ll = l.rotateLeft();
        Direction lll = ll.rotateLeft();
        Direction r = dir.rotateRight();
        Direction rr = r.rotateRight();
        Direction rrr = rr.rotateRight();
        if (!turnRight) {
            return new Direction[]{
                dir,
                l,
                r,
                ll,
                rr,
                lll,
                rrr
            };
        } else {
            return new Direction[] {
                dir,
                r,
                l,
                rr,
                ll,
                rrr,
                lll,
            };
        }
    }

    // Return strafe
    public static boolean strafeMove(Direction moveDir, Direction turnDir) throws GameActionException {
        if (moveDir == Direction.CENTER) return false;
        RobotController rc = Rat.rc;
        Direction currDir = rc.getDirection();
        if (rc.canMove(moveDir)) {
            if (rc.canTurn()) {
                if (currDir == moveDir) {
                    rc.move(moveDir);
                    rc.turn(turnDir);
                    return false;
                } else {
                    rc.turn(moveDir);
                    rc.move(moveDir);
                    return true;
                }
            } else {
                rc.move(moveDir);
                return moveDir != currDir;
            }
        }
        return false;
    }

    public static boolean simpleMove(Direction d) throws GameActionException {
        if (d == Direction.CENTER) return false;
        RobotController rc = Rat.rc;   
        if (rc.canMove(d)) {
            if (rc.canTurn() && rc.getDirection() != d)
                rc.turn(d);
            if (rc.canRemoveDirt(rc.getLocation()))
                rc.removeDirt(rc.getLocation());
            rc.move(d);
            return true;
        }
        return false;
    }

    // General valid function
    public static boolean valid(MapLocation loc) throws GameActionException {
        if (!Rat.rc.onTheMap(loc)) return false;
        MapInfo mapInfo = MapData.mapInfos[loc.x][loc.y];
        if (mapInfo != null) {
            if (mapInfo.isWall()) return false;
            // if (mapInfo.isDirt() && Rat.rc.getAllCheese() < Constants.WEALTHY_CHEESE_THRESHOLD) return false;
        }
        if (VisionManager.lastSeenClosestCat != null && Rat.distTo(VisionManager.lastSeenClosestCat.getLocation()) <= Constants.NAV_AVOID_CAT_DISTANCE)
            return false;
        MapLocation allyRatKingLocation = VisionManager.allyRatKingLocation;
        if (
            Math.abs(allyRatKingLocation.x - loc.x) <= 1 &&
            Math.abs(allyRatKingLocation.y - loc.y) <= 1
        )
            return false;

        if (Rat.rc.canSenseLocation(loc)) {
            RobotInfo robot = Rat.rc.senseRobotAtLocation(loc);
            if (robot != null) {
                return false;
            }
        }

        return true;
    }

    public static void randomMove() throws GameActionException {
        int idx = RNG.rng.nextInt(8);
        for (int i = 0; i < 8; i++) {
            boolean success = simpleMove(Constants.adjacentDirections[(idx + i) % 8]);
            if (success) return;
        }
    }
}