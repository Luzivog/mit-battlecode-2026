package opp_gravy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class RobotPlayer {
    // === GLOBALS ===
    static int turnCount;
    public static RobotController rc;
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static int mapWidth;
    public static int mapHeight;

    public static enum MessageType {
        SYMMETRY(0), // Communicate symmetry type to king
        ENEMY_KING_RAT_LOCATION(1),
        ENEMY_BABY_RAT_LOCATION(2),
        BAITING(3), // Baiting cat
        CHEESE_MINE(4); // Communicate cheese mine location to king

        private final int code;

        MessageType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static MessageType fromCode(int code) {
            for (MessageType s : values()) {
                if (s.code == code) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown code: " + code);
        }
    };

    public static class DecodedMessage {
        MessageType type;
        int messageContent;

        DecodedMessage(MessageType type, int messageContent) {
            this.type = type;
            this.messageContent = messageContent;
        }
    };

    public static List<MapLocation> cheeseMineLocations = new ArrayList<>();
    public static FastSet cheeseMineSet = new FastSet();

    public static int smallMapThreshold = 1200;
    public static int mediumMapThreshold = 2000;
    public static int largeMapThreshold = 3600;
    static int earlyGameThresh = 150;
    static int midGameThresh = 400;

    public static boolean coopStatus;
    public static int dirtAmount, globalCheeseAmount, rawCheeseAmount, allCheeseAmount, curRound, myHealth;
    public static int numMines;
    public static int currentRatCost;
    public static MapLocation curLocation;
    public static MapLocation kingRatLocation;

    public static MapLocation[] targets = new MapLocation[9];
    public static int[] visitedTargets = new int[9];

    public static int symmetry = -1; // -1 = unknown, 0 = diagonal, 1 = vertical, 2 = horizontal
    public static int midX1, midX2, midY1, midY2;

    public static int myId, betterID;
    public static Team myTeam;
    public static Team opponentTeam;

    public static MapInfo[] nearbyTiles;
    public static Message[] nearbySqueaks, last5RoundSqueaks;
    public static RobotInfo[] nearbyRobots;

    public static Random rng = new Random(6767);

    public static void init(RobotController robotController) throws GameActionException {
        rc = robotController;

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        midX1 = (mapWidth - 1) / 2;
        midX2 = (mapWidth) / 2;
        midY1 = (mapHeight - 1) / 2;
        midY2 = (mapHeight) / 2;

        curLocation = rc.getLocation();
        myId = rc.getID();
        rng = new Random(curLocation.x * 36 + curLocation.y * 63 + myId * 67 + rc.getRoundNum() * 69);
        betterID = rng.nextInt(1000000);
        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();

        int id = 0;
        for (int x : new int[] { 2, mapWidth / 2, mapWidth - 3 }) {
            for (int y : new int[] { 2, mapHeight / 2, mapHeight - 3 }) {
                targets[id] = new MapLocation(x, y);
                visitedTargets[id] = -1;
                id++;
            }
        }

        if (mapWidth * mapHeight <= smallMapThreshold) {
            KingRat.buildRatThreshold = 800;
            KingRat.ratCostCap = 30;
            KingRat.cm_distToCenterLowerbound = 10;
            KingRat.cm_distToCenterUpperbound = 450;
            KingRat.cm_distToKingUpperbound = 75;
            BabyRat.campTrapRadiusSqr = 35;
            BabyRat.aggressiveThresh = 40;
            BabyRat.ratTrapThreshold = 2000;
            BabyRat.ratTrapThreshold2 = 1750;

            BabyRat.smallDistanceThresh = 15;
            BabyRat.midDistanceThresh = 25;
            BabyRat.largeDistanceThresh = 100;
            BabyRat.smallDistanceWeight = 3;
            BabyRat.midDistanceWeight = 2;
            BabyRat.largeDistanceWeight = 1;
            BabyRat.minMinesToAssign = 2;
        } else if (mapWidth * mapHeight <= mediumMapThreshold) {
            KingRat.buildRatThreshold = 800;
            KingRat.ratCostCap = 30;
            KingRat.cm_distToCenterLowerbound = 50;
            KingRat.cm_distToCenterUpperbound = 450;
            KingRat.cm_distToKingUpperbound = 125;
            BabyRat.campTrapRadiusSqr = 70;
            BabyRat.aggressiveThresh = 50;

            BabyRat.smallDistanceThresh = 17;
            BabyRat.midDistanceThresh = 30;
            BabyRat.largeDistanceThresh = 100;
            BabyRat.smallDistanceWeight = 3;
            BabyRat.midDistanceWeight = 2;
            BabyRat.largeDistanceWeight = 1;
        } else {
            KingRat.buildRatThreshold = 800;
            KingRat.ratCostCap = 30;
            KingRat.cm_distToCenterLowerbound = 90;
            KingRat.cm_distToCenterUpperbound = 900;
            KingRat.cm_distToKingUpperbound = 255;
            BabyRat.campTrapRadiusSqr = 115;
            BabyRat.aggressiveThresh = 60;

            BabyRat.smallDistanceThresh = 27;
            BabyRat.midDistanceThresh = 45;
            BabyRat.largeDistanceThresh = 100;
            BabyRat.smallDistanceWeight = 3;
            BabyRat.midDistanceWeight = 2;
            BabyRat.largeDistanceWeight = 1;
        }

        if (rc.getType().isBabyRatType()) {
            BabyRat.init();
        }
    }

    public static MapLocation updateKingRatLocation() throws GameActionException {
        // Read king rat location from shared array
        int kingValue = readGlobalArray(3, 14);
        int kingX = kingValue / 60;
        int kingY = kingValue % 60;
        return new MapLocation(kingX, kingY);
    }

    public static void updateGlobals() throws GameActionException {
        coopStatus = rc.isCooperation();
        dirtAmount = rc.getDirt();
        globalCheeseAmount = rc.getGlobalCheese();
        rawCheeseAmount = rc.getRawCheese();
        allCheeseAmount = rc.getAllCheese();
        curLocation = rc.getLocation();
        curRound = rc.getRoundNum();
        currentRatCost = rc.getCurrentRatCost();
        myHealth = rc.getHealth();
        numMines = getNumGlobalMines();
        kingRatLocation = updateKingRatLocation();

        if (curRound > earlyGameThresh) {
            if (globalCheeseAmount <= 600) {
                BabyRat.hypercheese = 20;
                BabyRat.maxCheeseAmount = 200;
            } else {
                BabyRat.hypercheese = 40;
                BabyRat.maxCheeseAmount = 200;
            }
        }

        if (curRound >= 1950) {
            KingRat.buildRatThreshold = 200;
            KingRat.ratCostCap = 10000;
        }
    }

    // Global array (640 bits) usage:
    // bit 0-62: Rat king position tracking

    // Returns int that starts from bit start to bit end (0-indexed)
    public static int readGlobalArray(int start, int end) throws GameActionException {
        int res;

        // same block
        if (start / 10 == end / 10) {
            res = rc.readSharedArray(start / 10);
            end %= 10;
            start %= 10;
            res = (res >> (9 - end)) & ((1 << (end - start + 1)) - 1);
            return res;
        }

        res = rc.readSharedArray(start / 10) & ((1 << (10 - start % 10)) - 1);
        for (int i = start / 10 + 1; i < end / 10; i++) {
            int t = rc.readSharedArray(i);
            res = (res << 10) + t;
        }
        res = (res << (end % 10 + 1)) + (rc.readSharedArray(end / 10) >> (9 - end % 10));
        return res;
    }

    public static boolean hasFoundEnemyKingGlobally() throws GameActionException {
        return readGlobalArray(66, 66) == 1;
    }

    public static MapLocation decodeLocation(int message) {
        int MAX_SIDE_LENGTH = 60;
        int x = message / MAX_SIDE_LENGTH;
        int y = message % MAX_SIDE_LENGTH;
        return new MapLocation(x, y);
    }

    public static MapLocation getEnemyKingLocation() throws GameActionException {
        if (!hasFoundEnemyKingGlobally())
            return null;
        int kingValue = readGlobalArray(67, 78);
        int kingX = kingValue / 60;
        int kingY = kingValue % 60;
        return new MapLocation(kingX, kingY);
    }

    public static MapLocation getMineLocation(int index) throws GameActionException {
        return decodeLocation(readGlobalArray(85 + 12 * index, 85 + 12 * (index + 1) - 1));
    }

    public static boolean similarMapLocation(MapLocation A, MapLocation B) throws GameActionException {
        return (A.x == B.x && A.y == B.y);
    }

    public static void move(Direction dir) throws GameActionException {
        move(dir, false);
    }

    public static void move(Direction dir, boolean breakDirt) throws GameActionException {
        if (breakDirt && rc.canRemoveDirt(curLocation.add(dir))) {
            rc.removeDirt(curLocation.add(dir));
        }
        if (rc.canMove(dir))
            rc.move(dir);
    }

    // Return location of nearest rat trap possible.
    public static MapLocation nearestRatTrapTo(MapLocation referenceLocation) {
        int minDist = 1000000000;
        MapLocation closestTrapPosition = null;
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            int tileDist = tileLocation.distanceSquaredTo(referenceLocation);
            if (minDist > tileDist && rc.canPlaceRatTrap(tileLocation)) {
                minDist = tileDist;
                closestTrapPosition = tileLocation;
            }
        }
        return closestTrapPosition;
    }

    // Return location of nearest rat trap possible. Capped at maxDistSqr
    public static MapLocation nearestRatTrapTo(MapLocation referenceLocation, int maxDistSqr) {
        int minDist = maxDistSqr;
        MapLocation closestTrapPosition = null;
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            int tileDist = tileLocation.distanceSquaredTo(referenceLocation);
            if (minDist > tileDist && rc.canPlaceRatTrap(tileLocation)) {
                minDist = tileDist;
                closestTrapPosition = tileLocation;
            }
        }
        return closestTrapPosition;
    }

    public static boolean isWithinRatSight(MapLocation curLocation, MapLocation enemyLocation,
            Direction enemyDirection) {
        if (curLocation.distanceSquaredTo(enemyLocation) > 20)
            return false;

        Direction dirToCur = enemyLocation.directionTo(curLocation);
        if (dirToCur == null)
            return false;

        return dirToCur == enemyDirection
                || dirToCur == enemyDirection.rotateLeft()
                || dirToCur == enemyDirection.rotateRight();
    }

    public static boolean isWithinCatSight(MapLocation curLocation, MapLocation enemyLocation, Direction enemyDirection) {
        final int CAT_VISION_RADIUS_SQR = 17;

        // Distance check
        if (enemyLocation.distanceSquaredTo(curLocation) > CAT_VISION_RADIUS_SQR) {
            return false;
        }

        // Vector from enemy → current location
        int vx = curLocation.x - enemyLocation.x;
        int vy = curLocation.y - enemyLocation.y;

        // Facing direction vector
        int fx = enemyDirection.dx;
        int fy = enemyDirection.dy;

        // Dot product: > 0 means within the forward half-circle
        return vx * fx + vy * fy >= 0;
    }

    // If cat continues to move, will it enter trap ?
    public static boolean willCatEnter3x3(MapLocation catPos, Direction dir, MapLocation trapPos) {
        int minX = trapPos.x;
        int minY = trapPos.y;
        int maxX = trapPos.x + 3;
        int maxY = trapPos.y + 3;

        int ox = catPos.x;
        int oy = catPos.y;

        int dx = dir.dx;
        int dy = dir.dy;

        // Not moving
        if (dx == 0 && dy == 0) return false;

        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        // X slab
        if (dx != 0) {
            double tx1 = (minX - ox) / (double) dx;
            double tx2 = (maxX - ox) / (double) dx;
            tMin = Math.max(tMin, Math.min(tx1, tx2));
            tMax = Math.min(tMax, Math.max(tx1, tx2));
        } else if (ox < minX || ox >= maxX) {
            return false;
        }

        // Y slab
        if (dy != 0) {
            double ty1 = (minY - oy) / (double) dy;
            double ty2 = (maxY - oy) / (double) dy;
            tMin = Math.max(tMin, Math.min(ty1, ty2));
            tMax = Math.min(tMax, Math.max(ty1, ty2));
        } else if (oy < minY || oy >= maxY) {
            return false;
        }

        // Intersection exists for t ≥ 0
        return tMax >= Math.max(0, tMin);
    }


    @SuppressWarnings("unused")
    private static void turn() throws GameActionException {
        // rc.setIndicatorString("Raw cheese amount: " + rc.getRawCheese());
        turnCount = rc.getRoundNum();
        updateGlobals();
        // rc.setIndicatorString("Symmetry is " + symmetry);

        // Try/catch blocks stop unhandled exceptions, which cause your robot to
        // explode.
        try {
            globalTurn();

            if (rc.getType().isRatKingType()) {
                KingRat.run();
            } else if (rc.getType().isBabyRatType()) {
                BabyRat.run();
            }
        } catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        } finally {
            Clock.yield();
        }
    }

    // All units perform this before doing their role-specific actions
    public static void globalTurn() throws GameActionException {
        // Global behavior here

        nearbyTiles = rc.senseNearbyMapInfos();
        nearbySqueaks = rc.readSqueaks(curRound - 1); // Read squeaks from last round
        last5RoundSqueaks = rc.readSqueaks(-1);
    }

    public static void tryToBreakDirt() throws GameActionException {
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();

            if (!tile.isDirt())
                continue;
            if (!rc.canRemoveDirt(tileLocation))
                continue;

            rc.removeDirt(tileLocation);
            return;
        }
    }

    public static void sendSqueak(MessageType squeakType, int messageContent) {
        int bitsForTypes = 4;
        int messageCode = squeakType.getCode() | (messageContent << bitsForTypes);
        rc.squeak(messageCode);
    }

    public static DecodedMessage readSqueak(int messageContent) {
        DecodedMessage deciphered = new DecodedMessage(null, 0);

        deciphered.type = MessageType.fromCode(messageContent & ((1 << 4) - 1));
        deciphered.messageContent = (messageContent >> 4);
        return deciphered;
    }

    public static int encodeLocation(MapLocation loc) throws GameActionException {
        int MAX_SIDE_LENGTH = 60;
        return loc.x * MAX_SIDE_LENGTH + loc.y;
    }

    public static int getNumGlobalMines() throws GameActionException {
        return readGlobalArray(79, 84);
    }

    public static boolean findLocationinGlobal(MapLocation loc) throws GameActionException {
        // System.out.println("Checking position " + loc.x + ", " + loc.y);
        for (int i = 0; i < numMines; i++) {
            MapLocation mineLocation = getMineLocation(i);
            // System.out.println("In global " + mineLocation.x + ", " + mineLocation.y);
            if (mineLocation.x == loc.x && mineLocation.y == loc.y) {
                // System.out.println("loc is in global array!?");
                return true;
            }
        }
        return false;
    }

    /// Add this location to list if
    public static boolean recordCheeseMine(MapLocation tileLocation) {
        if (cheeseMineSet.contains(tileLocation))
            return false;
        cheeseMineSet.add(tileLocation);
        cheeseMineLocations.add(tileLocation);
        return true;
    }

    public static void run(RobotController rc) throws GameActionException {
        init(rc);

        while (true) {
            turn();
        }
    }
}