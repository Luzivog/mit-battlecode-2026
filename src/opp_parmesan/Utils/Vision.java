package opp_parmesan.Utils;
import battlecode.common.*;
import opp_parmesan.Utils.AdditionalClasses.Thrown;
import opp_parmesan.Utils.Globals;

public class Vision extends Globals {

    public static long[] hasSeen = new long[60];
    public static long[] isWall = new long[60];
    public static long[] isDirt = new long[60];
    public static long[] hasCheeseMine = new long[60];
    public static long[] isReachable = new long[60];

    public static long[] isDangerous = new long[60];

    public static RobotInfo[] enemies;
    public static MapLocation[] walls;
    public static int numWalls = 0;
    public static MapLocation[] mines;
    public static int numMines = 0;

    public static void lookAround () throws GameActionException {

        //System.out.println(Clock.getBytecodeNum());

        for (MapInfo mi : rc.senseNearbyMapInfos()) {
            int x = mi.getMapLocation().x, y = mi.getMapLocation().y;

            if (((hasSeen[x]>>y)&1) == 0 || ((isWall[x]>>y)&1) == 0) {
                hasSeen[x] |= 1L << y;
                if (mi.isWall()) isWall[x] |= 1L << y;
                if (mi.hasCheeseMine()) hasCheeseMine[x] |= 1L << y;
                if (mi.isDirt()) isDirt[x] |= 1L << y;
                else isDirt[x] &= ~(1L << y);
            }
        }

        //System.out.println(Clock.getBytecodeNum());

    }

    public static void turn (Direction dir) throws GameActionException {
        if (dir.equals(Direction.CENTER) || dir.equals(rc.getDirection()) || cantTurn || !rc.canTurn()) return;
        rc.turn(dir);
        lookAround();
    }

    static boolean turnLeft = false;

    public static Direction bestTurn () throws GameActionException {
        int sumX = rc.getLocation().x, sumY = rc.getLocation().y;
        for (int i = 0; i < newestSqueaks.length; i++) {
            sumX += newestSqueaks[i].getSource().x;
            sumY += newestSqueaks[i].getSource().y;
        }
        sumX = (sumX + (newestSqueaks.length + 1) / 2) / (newestSqueaks.length + 1);
        sumY = (sumY + (newestSqueaks.length + 1) / 2) / (newestSqueaks.length + 1);
        Direction d = rc.getLocation().directionTo(new MapLocation(sumX, sumY)).opposite();
        if (d.equals(Direction.CENTER)) {
            turnLeft = !turnLeft;
            if (turnLeft) return rc.getDirection().rotateLeft().rotateLeft();
            return rc.getDirection().rotateRight().rotateRight();
        }
        return d;
    }

    public static void turnJustBecause () throws GameActionException {
        if (rc.canTurn() && enemyRobots.length == 0 && newestSqueaks.length == 0) turn(bestTurn());
    }

    public static void senseNearbyEnemies() throws GameActionException {
        enemies = rc.senseNearbyRobots(rc.getType().visionConeRadiusSquared, opponentTeam);
    }

    public static boolean seenByEnemies() throws GameActionException{
        for (RobotInfo ri: enemies) {
            if (rc.getLocation().isWithinDistanceSquared(ri.location, ri.getType().visionConeRadiusSquared, ri.getDirection(), ri.getType().visionConeAngle)) {
                return true;
            }
        }
        return false;
    }

    public static void digDirt (MapLocation loc) throws GameActionException {
        isDirt[loc.x] &= ~(1L<<loc.y);
    }

    public static void placeDirt (MapLocation loc) throws GameActionException {
        isDirt[loc.x] |= 1L<<loc.y;
    }

    public static boolean hasSeenLocation(MapLocation loc) {
        return rc.onTheMap(loc) && ((hasSeen[loc.x]>>loc.y)&1) > 0;
    }

    public static boolean hasSeenLocationKnownToBeOnMap (MapLocation loc) {
        return ((hasSeen[loc.x]>>loc.y)&1) > 0;
    }

    public static boolean sensePassability (MapLocation loc) {
        return ((isWall[loc.x]>>loc.y)&1) == 0 && ((isDirt[loc.x]>>loc.y)&1) == 0;
    }

    public static boolean isDirt (MapLocation loc) {
        return ((hasSeen[loc.x]>>loc.y)&1) > 0 && ((isDirt[loc.x]>>loc.y)&1) > 0;
    }

    public static boolean isWall (MapLocation loc) {
        return ((isWall[loc.x]>>loc.y)&1) > 0;
    }

    public static boolean canReach (MapLocation loc) {
        return ((isReachable[loc.x]>>loc.y)&1) > 0;
    }

    public static boolean isDangerous (MapLocation loc) {
        return ((isDangerous[loc.x]>>loc.y)&1) > 0;
    }

    public static boolean isPassableWithDig (MapLocation loc) {
        return ((isWall[loc.x]>>loc.y)&1) == 0;
    }

    public static boolean isMine (MapLocation loc) {
        return ((hasCheeseMine[loc.x]>>loc.y)&1) > 0;
    }

    public static void init () {
    }

    public static void startTurn () throws GameActionException {
        if (rc.getType() == UnitType.BABY_RAT)
            lookAround();
        // TODO: optimise by detecting canDig nearby or canMove nearby
        for (Thrown throwns: alliesThrown) {
            MapLocation loc = throwns.loc.add(throwns.dir);
            if (hasSeenLocation(loc)) {
                isDangerous[loc.x] |= (1L<<loc.y);
            }
            loc = loc.add(throwns.dir);
            if (hasSeenLocation(loc)) {
                isDangerous[loc.x] |= (1L<<loc.y);
            }
        }
    }

    public static void endTurn () throws GameActionException {
        for (Thrown throwns: alliesThrown) {
            MapLocation loc = throwns.loc.add(throwns.dir);
            if (hasSeenLocation(loc)) {
                isDangerous[loc.x] &= ~(1L<<loc.y);
            }
            loc = loc.add(throwns.dir);
            if (hasSeenLocation(loc)) {
                isDangerous[loc.x] &= ~(1L<<loc.y);
            }
        }
    }
}
