package opp_gravy;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;

public class BabyRat extends RobotPlayer {

    // Thresholds:
    public static int ratTrapThreshold = 1000;
    public static int ratTrapThreshold2 = 750;
    public static int baitingDistance = 36;
    public static int minRoundsBetweenTraps = 2;
    public static int breakDirtThresh = 1250;
    public static int hypercheese = 20; // Above this much cheese, RETURN, don't do anything else.
    public static int aggressiveThresh = 50;
    public static int enemyHealthToTrap = 20;
    public static int baitLocationThresh = 200;
    public static int diffHealthThreshToAttack = 11;
    public static int minHealthToPlaceTrap = 25;
    public static int minMinesToAssign = 3;
    public static int maxCheeseAmount = 120;
    public static int numMinesToConsider;
    public static int moreTAMThresh = 30;
    public static int threshToReturnEnemyKingLocation = 100;
    public static int smallDistanceThresh, midDistanceThresh, largeDistanceThresh;
    public static int smallDistanceWeight, midDistanceWeight, largeDistanceWeight;

    private static RatState ratStatus = RatState.SEARCHING;
    private static RatState originalState = RatState.SEARCHING;

    public static enum RatState {
        SEARCHING,
        RETURNING,
        ATTACKING,
        BAITING,
        AGGRESSIVE,
        TO_ASSIGNED_MINE,
        PROMOTING,

    }

    private static MapLocation targetCheeseLocation = null;
    private static MapLocation targetLocation = null;
    private static MapLocation originalKingLocation = null;
    private static MapLocation targetEnemyLocation = null;
    private static boolean breakDirt = false;
    public static boolean willNotMove = false;

    // RATNAPPING values
    private static int turnsCarrying = 0;

    // SEARCHING values
    private static boolean isReturningEnemyLocation = false;
    private static int enemyLocMessage = -1;
    public static MapLocation searchingLocation = null;

    // RETURNING values
    private static boolean isReturningSymmetry = false;
    private static final int SQUEAK_RADIUS_SQR = 26;

    // CAMPING values
    public static int campTrapRadiusSqr; // If camp is this distance from center, place traps there. Init at RobotPlayer
    private static boolean gettingCheese = false;
    private static MapLocation cheeseCampLocation = null;
    private static MapLocation campingPosition = null;
    private static boolean isReturningToCamp = false;
    private static boolean isScoutingNearby = false;
    private static int scoutingTurns = 0;

    // ATTACKING values
    private static boolean FoundEnemyKing = false;
    private static MapLocation enemyKingLocation = null;
    private static int lastRoundSinceRatTrap = -100;

    // BAITING values
    private static MapLocation baitCatLocation = null;
    private static int delayTurns = 0;
    private static MapLocation catLocation = null;

    // AGGRESSIVE values
    private static int targetIndex = -1;
    private static boolean hasGoneToSymLoc = false;
    private static int turnsMovingToTarget = 0;
    private static MapLocation oldSymLocation = null;

    // PROMOTING values
    private static MapLocation promoteLocation = null;
    private static boolean promoted = false;

    // TO_ASSIGNED_MINE values
    private static MapLocation assignedMine = null;
    private static boolean hasGoneToFirstMine = false;

    private static RatState getOriginalState() throws GameActionException {
        if ((symmetry == -1 && !hasFoundEnemyKingGlobally()) || currentRatCost <= aggressiveThresh) {
            if (numMines < minMinesToAssign) {
                return RatState.SEARCHING;
            } else {
                int numRats = (currentRatCost / 10 - 1) * 4;
                if (numRats >= numMines * 4) {
                    if (betterID % 4 == 0) {
                        return RatState.TO_ASSIGNED_MINE;
                    } else {
                        return RatState.SEARCHING;
                    }
                } else if (numRats >= numMines * 3) {
                    if (betterID % 3 == 0) {
                        return RatState.TO_ASSIGNED_MINE;
                    } else {
                        return RatState.SEARCHING;
                    }
                } else if (numRats >= numMines * 2) {
                    if (betterID % 2 == 0) {
                        return RatState.TO_ASSIGNED_MINE;
                    } else {
                        return RatState.SEARCHING;
                    }
                } else {
                    if (betterID % 3 != 2) {
                        return RatState.TO_ASSIGNED_MINE;
                    } else {
                        return RatState.SEARCHING;
                    }
                }
            }
        } else {
            if (betterID % 2 == 1)
                return RatState.AGGRESSIVE;
            else {
                if ((betterID % 3 == 0) || numMines == 0)
                    return RatState.SEARCHING;
                else
                    return RatState.TO_ASSIGNED_MINE;
            }
        }
    }

    public static void init() throws GameActionException {
        originalState = getOriginalState();
        ratStatus = originalState;
        originalKingLocation = updateKingRatLocation();
        SymmetryManager.setBase(originalKingLocation);
    }

    public static void run() throws GameActionException {
        // === GLOBAL BEHAVIOR ===
        willNotMove = false;
        tryToPickUpCheese();
        allCheeseAmount = rc.getRawCheese() + rc.getGlobalCheese(); // RACE CONDITIONS YALL THESE THINGS COST 1 BYTECODE
        // update symmetry
        if (symmetry == -1) {
            SymmetryManager.fetchSymmetry();
        }
        breakDirt = allCheeseAmount >= breakDirtThresh;
        if (numMines <= 8) {
            numMinesToConsider = numMines;
        } else {
            // Take Ceil 75% * numMines
            numMinesToConsider = (numMines * 3 + 3) / 4;
        }

        originalState = getOriginalState();

        if (ratStatus == RatState.AGGRESSIVE || ratStatus == RatState.SEARCHING
                || ratStatus == RatState.TO_ASSIGNED_MINE) {
            if (ratStatus != originalState) {
                ratStatus = originalState;
                targetIndex = -1;
                targetLocation = null;
            }
        }

        // Try to throw enemy but prioritize the throwing micro in attacking first
        if (rc.getCarrying() != null) {
            turnsCarrying++;
            // If holding too long then try to throw
            if (turnsCarrying >= 7 && willThrowRat()) {
                rc.throwRat();
            }
        } else {
            turnsCarrying = 0;
        }

        if (rc.getRoundNum() >= 1900) {
            if (betterID % 2 == 0) {
                if (promoted == false) {
                    ratStatus = RatState.PROMOTING;
                } else {
                    ratStatus = RatState.AGGRESSIVE;
                }
            } else {
                ratStatus = RatState.AGGRESSIVE;
            }
        }
        updateCheeseMinesList();

        // === STATE-SPECIFIC BEHAVIOR ===
        // The functions also contain transitions
        switch (ratStatus) {
            case RatState.SEARCHING -> Searching();
            case RatState.RETURNING -> Returning();
            case RatState.ATTACKING -> Attacking();
            case RatState.AGGRESSIVE -> Aggressive();
            case RatState.BAITING -> Baiting();
            case RatState.TO_ASSIGNED_MINE -> toAssignedMine();
            case RatState.PROMOTING -> Promoting();
        }

        if (symmetry == -1)
            SymmetryManager.checkSym();

        if (rc.getCarrying() != null) {
            if (willThrowRat()) {
                rc.throwRat();
            }
        } else {
            turnsCarrying = 0;
        }
        // rc.setIndicatorString("Current rat state " + ratStatus);
        // rc.setIndicatorString("Num mines is " + numMines);
        // if (hasFoundEnemyKingGlobally()) {
        // MapLocation enemyKingLoca = getEnemyKingLocation();
        // rc.setIndicatorString("Enemy king location globally is " + enemyKingLoca.x +
        // ", " + enemyKingLoca.y);
        // }
        // rc.setIndicatorString("Turns carrying is " + turnsCarrying);
    }

    private static int distanceToKing() throws GameActionException {
        MapLocation kingLocation = updateKingRatLocation();
        if (kingLocation == null) {
            return 1000000;
        }
        return rc.getLocation().distanceSquaredTo(kingLocation);
    }

    private static MapLocation isCatNearby() throws GameActionException {
        for (MapInfo tile : nearbyTiles) {
            if (!rc.canSenseRobotAtLocation(tile.getMapLocation())) {
                continue;
            }
            MapLocation tileLocation = tile.getMapLocation();
            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            if (tileRobot.getType().isCatType() && tileLocation.distanceSquaredTo(rc.getLocation()) <= 16) {
                return tileLocation;
            }
        }
        return null;
    }

    private static MapLocation isEnemyKingNearby() throws GameActionException {
        curLocation = rc.getLocation();
        int minDist = 1000000;
        MapLocation nearestKing = null;
        // String ret = "";
        for (MapInfo tile : rc.senseNearbyMapInfos()) {
            MapLocation tileLocation = tile.getMapLocation();
            if (!rc.canSenseRobotAtLocation(tileLocation)) {
                continue;
            }
            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            // ret += tileLocation.x + ", " + tileLocation.y + "; ";
            if (tileRobot.getType().isRatKingType() && tileRobot.getTeam() != myTeam) {
                int dist = tileLocation.distanceSquaredTo(curLocation);
                if (dist < minDist) {
                    minDist = dist;
                    nearestKing = tileLocation;
                }
                FoundEnemyKing = true;
            }
        }
        // rc.setIndicatorString(ret);
        if (nearestKing != null)
            return nearestKing;
        if (last5RoundSqueaks.length > 0) {
            for (Message msg : last5RoundSqueaks) {
                DecodedMessage message = readSqueak(msg.getBytes());
                if (message.type != MessageType.ENEMY_KING_RAT_LOCATION)
                    continue;
                return decodeLocation(message.messageContent);
            }
        }
        return null;
    }

    private static int numberBaitingRats() throws GameActionException {
        int count = 0;
        for (Message msg : nearbySqueaks) {
            if (readSqueak(msg.getBytes()).type == MessageType.BAITING)
                count++;
        }
        return count;
    }

    private static MapLocation findCampPosition(MapLocation cheeseMineLocation) throws GameActionException {
        // System.out.println("Finding camp position near cheese at: " + cheeseMineLocation.x + ", "
        //         + cheeseMineLocation.y);
        // Manually check 4 positions around the cheese mine
        int countRats = 4;
        boolean aboveClear = false, belowClear = false, leftClear = false, rightClear = false;

        MapLocation above = new MapLocation(cheeseMineLocation.x, cheeseMineLocation.y + 1);
        if (rc.canSenseLocation(above) && !rc.canSenseRobotAtLocation(above)) {
            countRats--;
            aboveClear = true;
        }

        MapLocation below = new MapLocation(cheeseMineLocation.x, cheeseMineLocation.y - 1);
        if (rc.canSenseLocation(below) && !rc.canSenseRobotAtLocation(below)) {
            countRats--;
            belowClear = true;
        }

        MapLocation left = new MapLocation(cheeseMineLocation.x - 1, cheeseMineLocation.y);
        if (rc.canSenseLocation(left) && !rc.canSenseRobotAtLocation(left)) {
            countRats--;
            leftClear = true;
        }

        MapLocation right = new MapLocation(cheeseMineLocation.x + 1, cheeseMineLocation.y);
        if (rc.canSenseLocation(right) && !rc.canSenseRobotAtLocation(right)) {
            countRats--;
            rightClear = true;
        }

        if (countRats >= 1)
            return null;

        if (aboveClear) {
            return above;
        } else if (belowClear) {
            return below;
        } else if (leftClear) {
            return left;
        } else if (rightClear) {
            return right;
        }
        return null;
    }

    // Check if there is an ally within throwing range
    private static boolean isGoodThrowingAngle() throws GameActionException {
        MapLocation loc = rc.getLocation();
        Direction dir = rc.getDirection();
        while (true) {
            loc = loc.add(dir);
            if (!rc.canSenseLocation(loc))
                break;
            MapInfo tile = rc.senseMapInfo(loc);
            if (!tile.isPassable()) {
                return true;
            }
            if (rc.canSenseRobotAtLocation(loc)) {
                RobotInfo locInfo = rc.senseRobotAtLocation(loc);
                if (locInfo.getTeam() == myTeam) {
                    return false;
                } else {
                    return true;
                }
            } else {
                MapInfo locInfo = rc.senseMapInfo(loc);
                if (!locInfo.isPassable())
                    return true;
            }
        }
        dir = dir.rotateLeft();
        loc = rc.getLocation();
        while (true) {
            loc = loc.add(dir);
            if (!rc.canSenseLocation(loc))
                break;
            MapInfo tile = rc.senseMapInfo(loc);
            if (!tile.isPassable()) {
                if (rc.canTurn()) {
                    rc.turn(dir);
                    return true;
                } else
                    return false;
            }
            if (rc.canSenseRobotAtLocation(loc)) {
                RobotInfo locInfo = rc.senseRobotAtLocation(loc);
                if (locInfo.getTeam() == myTeam) {
                    return false;
                } else {
                    if (rc.canTurn()) {
                        rc.turn(dir);
                        return true;
                    } else
                        return false;
                }
            } else {
                MapInfo locInfo = rc.senseMapInfo(loc);
                if (!locInfo.isPassable())
                    return true;
            }
        }
        dir = dir.rotateRight().rotateRight();
        loc = rc.getLocation();
        while (true) {
            loc = loc.add(dir);
            if (!rc.canSenseLocation(loc))
                break;
            MapInfo tile = rc.senseMapInfo(loc);
            if (!tile.isPassable()) {
                if (rc.canTurn()) {
                    rc.turn(dir);
                    return true;
                } else
                    return false;
            }
            if (rc.canSenseRobotAtLocation(loc)) {
                RobotInfo locInfo = rc.senseRobotAtLocation(loc);
                if (locInfo.getTeam() == myTeam) {
                    return false;
                } else {
                    if (rc.canTurn()) {
                        rc.turn(dir);
                        return true;
                    } else
                        return false;
                }
            } else {
                MapInfo locInfo = rc.senseMapInfo(loc);
                if (!locInfo.isPassable())
                    return true;
            }
        }

        return false;
    }

    private static boolean willThrowRat() throws GameActionException {
        if (!rc.canThrowRat())
            return false;
        if (turnsCarrying >= 9) {
            return true;
        } else {
            if (isGoodThrowingAngle())
                return true;
            else
                return false;
        }
    }

    private static MapLocation fetchNearbyEnemy() throws GameActionException {
        curLocation = rc.getLocation();
        MapLocation closestEnemyLocation = null;
        int minDist = 1000000;
        for (Message msg : nearbySqueaks) {
            DecodedMessage message = readSqueak(msg.getBytes());
            if (msg.getSenderID() == myId || message.type != MessageType.ENEMY_BABY_RAT_LOCATION)
                continue;
            MapLocation loc = decodeLocation(message.messageContent);
            // System.out.println("Enemy is at " + loc.x + ", " + loc.y);
            int dist = curLocation.distanceSquaredTo(loc);
            if (dist < minDist) {
                minDist = dist;
                closestEnemyLocation = loc;
            }
        }
        return closestEnemyLocation;
    }

    private static boolean canTurnToLocation(MapLocation loc, Direction myDirection, MapLocation targetLocation)
            throws GameActionException {
        if (isWithinRatSight(targetLocation, loc, myDirection)) {
            return true;
        }
        if (!rc.canTurn())
            return false;
        else
            return true;
    }

    private static boolean canBeStrafedByEnemy(MapLocation myLocation, Direction myDirection, MapLocation enemyLocation)
            throws GameActionException {
        Direction enemyDirection = enemyLocation.directionTo(myLocation);
        for (Direction dir : new Direction[] { enemyDirection, enemyDirection.rotateLeft(),
                enemyDirection.rotateRight() }) {
            MapLocation newLocation = enemyLocation.add(dir);
            if (!isWithinRatSight(newLocation, myLocation, myDirection)
                    && newLocation.distanceSquaredTo(myLocation) <= 2) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkOtherEnemies(MapLocation myLocation, Direction myDirection, int targetEnemyID)
            throws GameActionException {
        // int bytecodesBefore = Clock.getBytecodesLeft();
        for (RobotInfo info : nearbyRobots) {
            if (info.getID() == targetEnemyID)
                continue;
            if (info.getTeam() == myTeam || !info.getType().isBabyRatType())
                continue;
            MapLocation enemyLocation = info.getLocation();
            // System.out.println("Enemy locations at " + enemyLocation.x + ", " + enemyLocation.y);
            if (canBeStrafedByEnemy(myLocation, myDirection, enemyLocation))
                return true;
        }
        // int bytecodesAfter = Clock.getBytecodesLeft();
        // System.out.println("checkOtherEnemies used bytecodes: " + (bytecodesBefore - bytecodesAfter));
        return false;
    }

    // Check if you can strafe out of enemy vision
    private static Direction getStrafeDirection(MapLocation myLocation, MapLocation enemyLocation,
            Direction enemyDirection, int targetEnemyID)
            throws GameActionException {
        if (!rc.isMovementReady())
            return null;
        Direction moveDirection = null, curDirection = rc.getDirection();
        Direction toEnemy = myLocation.directionTo(enemyLocation);
        for (Direction dir : new Direction[] { toEnemy.rotateLeft(), toEnemy.rotateRight(),
                toEnemy.rotateLeft().rotateLeft(), toEnemy.rotateRight().rotateRight() }) {
            MapLocation newLocation = myLocation.add(dir);
            if (rc.canMove(dir) && !isWithinRatSight(newLocation, enemyLocation, enemyDirection)
                    && newLocation.distanceSquaredTo(enemyLocation) <= 2
                    && canTurnToLocation(newLocation, curDirection, enemyLocation)
                    && !checkOtherEnemies(newLocation, newLocation.directionTo(enemyLocation), targetEnemyID)) {
                moveDirection = dir;
                break;
            }
        }
        return moveDirection;
    }

    private static int numTrapsNearLocation(MapLocation loc) throws GameActionException {
        int count = 0;
        for (Direction dir : directions) {
            MapLocation adjLocation = loc.add(dir);
            if (rc.canSenseLocation(adjLocation)) {
                MapInfo adjInfo = rc.senseMapInfo(adjLocation);
                if (adjInfo.getTrap() == TrapType.RAT_TRAP) {
                    count++;
                }
            }
        }
        return count;
    }

    private static MapLocation getBaitLocation() throws GameActionException {
        MapLocation kingLocation = updateKingRatLocation();
        if (kingLocation == null) {
            // If we can't find the king rat, just pick a default location
            return new MapLocation(0, 0);
        } else {
            // Try each corners
            int maxDistance = -1;
            MapLocation loc = null;
            for (int x : new int[] { 0, mapWidth - 1 }) {
                for (int y : new int[] { 0, mapHeight - 1 }) {
                    MapLocation corner = new MapLocation(x, y);
                    int distance = corner.distanceSquaredTo(kingLocation);
                    if (distance > maxDistance
                            && curLocation.distanceSquaredTo(corner) < catLocation.distanceSquaredTo(corner)
                            && distance > baitLocationThresh) {
                        maxDistance = distance;
                        loc = corner;
                    }
                }
            }
            return loc;
        }
    }

    // Return true if there IS a wall between a and b (excluding endpoints
    // optionally).
    // Callers expect: true == wall present, false == clear sight.
    private static boolean checkWallOrDirtBetween(MapLocation a, MapLocation b) throws GameActionException {
        int x0 = a.x, y0 = a.y;
        int x1 = b.x, y1 = b.y;
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        int x = x0, y = y0;
        while (true) {
            // skip checking the source tile; optionally skip target as well if you want
            if (!(x == x0 && y == y0) && !(x == x1 && y == y1)) {
                MapLocation loc = new MapLocation(x, y);
                if (rc.canSenseLocation(loc)) {
                    MapInfo tileInfo = rc.senseMapInfo(loc);
                    if (tileInfo.isWall() || tileInfo.isDirt()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }

            if (x == x1 && y == y1)
                break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
        return false;
    }

    private static boolean checkWallBetween(MapLocation a, MapLocation b) throws GameActionException {
        int x0 = a.x, y0 = a.y;
        int x1 = b.x, y1 = b.y;
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        int x = x0, y = y0;
        while (true) {
            // skip checking the source tile; optionally skip target as well if you want
            if (!(x == x0 && y == y0) && !(x == x1 && y == y1)) {
                MapLocation loc = new MapLocation(x, y);
                if (rc.canSenseLocation(loc)) {
                    MapInfo tileInfo = rc.senseMapInfo(loc);
                    if (tileInfo.isWall()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }

            if (x == x1 && y == y1)
                break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
        return false;
    }

    private static MapLocation getClosestEnemy() throws GameActionException {
        MapLocation closestEnemyLocation = null;
        int minBabyDist = 1000000, minHealth = 1000000, minEuclidDist = 1000000;

        curLocation = rc.getLocation();

        int carryID = -1;
        RobotInfo carryInfo = rc.getCarrying();
        if (carryInfo != null)
            carryID = carryInfo.getID();

        // Find nearest enemy
        nearbyTiles = rc.senseNearbyMapInfos();

        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (!rc.canSenseRobotAtLocation(tileLocation)) {
                continue;
            }
            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            if (tileRobot.getType().isBabyRatType() && tileRobot.getTeam() != myTeam && tileRobot.getID() != carryID) {
                int dist = Math.max(Math.abs(tileLocation.x - curLocation.x), Math.abs(tileLocation.y - curLocation.y));
                int euclidDist = tileLocation.distanceSquaredTo(curLocation);
                int enemyHealth = tileRobot.getHealth();
                if (dist < minBabyDist) {
                    closestEnemyLocation = tileLocation;
                    minBabyDist = dist;
                    minHealth = enemyHealth;
                    minEuclidDist = euclidDist;
                } else if (dist == minBabyDist) {
                    if (enemyHealth < minHealth) {
                        closestEnemyLocation = tileLocation;
                        minBabyDist = dist;
                        minHealth = enemyHealth;
                        minEuclidDist = euclidDist;
                    } else if (enemyHealth == minHealth) {
                        if (euclidDist < minEuclidDist) {
                            closestEnemyLocation = tileLocation;
                            minBabyDist = dist;
                            minHealth = enemyHealth;
                            minEuclidDist = euclidDist;
                        }
                    }
                }
            }
        }
        return closestEnemyLocation;
    }

    private static int getNumEnemies(int carryID) throws GameActionException {
        int count = 0;

        curLocation = rc.getLocation();

        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (!rc.canSenseRobotAtLocation(tileLocation)) {
                continue;
            }
            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            if (tileRobot.getType().isBabyRatType() && tileRobot.getTeam() != myTeam && tileRobot.getID() != carryID) {
                count++;
            }
        }
        return count;
    }

    private static int getNumAllies() throws GameActionException {
        int allies = 0;
        for (Message msg : nearbySqueaks) {
            DecodedMessage message = readSqueak(msg.getBytes());
            if (msg.getSenderID() == myId || message.type != MessageType.ENEMY_BABY_RAT_LOCATION)
                continue;
            MapLocation loc = decodeLocation(message.messageContent);
            if (rc.canSenseLocation(loc)) {
                allies++;
            }
        }
        return allies;
    }

    private static boolean isGoodAngle(MapLocation curLocation, MapLocation enemyLocation) throws GameActionException {
        Direction toEnemy = curLocation.directionTo(enemyLocation);
        MapLocation checkLocation = curLocation.add(toEnemy).add(toEnemy);
        if (checkLocation.equals(enemyLocation)) {
            return true;
        } else {
            return false;
        }
    }

    // Try to attack and trap nearby enemy baby rats & cats
    private static void tryAttackingEnemies(MapLocation closestEnemyLocation) throws GameActionException {
        // int bytecodesLeft1 = Clock.getBytecodesLeft();
        // boolean hasStrafed = false;
        curLocation = rc.getLocation();
        nearbyRobots = rc.senseNearbyRobots();
        int carryID = -1;
        RobotInfo carryInfo = rc.getCarrying();
        if (carryInfo != null)
            carryID = carryInfo.getID();
        // int bytecodesBefore = Clock.getBytecodesLeft(), bytecodesAfter;
        // System.out.println("Starting tryAttackingEnemies with bytecodes: " + (bytecodesLeft1 - bytecodesBefore));
        int numEnemies = getNumEnemies(carryID);
        // bytecodesAfter = Clock.getBytecodesLeft();
        // System.out.println("getNumEnemies used bytecodes: " + (bytecodesBefore - bytecodesAfter));
        // bytecodesBefore = Clock.getBytecodesLeft();
        int numAllies = getNumAllies() + 1;
        // bytecodesAfter = Clock.getBytecodesLeft();
        // System.out.println("getNumAllies used bytecodes: " + (bytecodesBefore - bytecodesAfter));
        // rc.setIndicatorString("Number of enemies + allies is " + numEnemies +
        // numAllies);
        // if (closestEnemyLocation != null)
        // rc.setIndicatorString("Closest enemy is at " + closestEnemyLocation.x + ", "
        // + closestEnemyLocation.y);
        if (closestEnemyLocation != null) {
            // Don't break dirt when enemy is nearby
            breakDirt = false;
        }

        // If number of allies >= number of enemies
        if (closestEnemyLocation != null && Math.max(Math.abs(curLocation.x - closestEnemyLocation.x),
                Math.abs(curLocation.y - closestEnemyLocation.y)) <= 3) {
            // bytecodesBefore = Clock.getBytecodesLeft();
            boolean hasWallsBetween = checkWallOrDirtBetween(curLocation, closestEnemyLocation);
            // bytecodesAfter = Clock.getBytecodesLeft();
            // System.out.println("checkWallOrDirtBetween used bytecodes: " + (bytecodesBefore - bytecodesAfter));
            RobotInfo enemyInfo = rc.senseRobotAtLocation(closestEnemyLocation);
            int distToEnemy = curLocation.distanceSquaredTo(closestEnemyLocation);
            Direction enemyDirection = enemyInfo.getDirection();
            int enemyHealth = enemyInfo.getHealth();
            int numTrapsNearEnemy = numTrapsNearLocation(closestEnemyLocation);
            // bytecodesBefore = Clock.getBytecodesLeft();
            Direction strafeDirection = getStrafeDirection(curLocation, closestEnemyLocation, enemyDirection,
                    enemyInfo.getID());
            // bytecodesAfter = Clock.getBytecodesLeft();
            // System.out.println("getStrafeDirection used bytecodes: " + (bytecodesBefore - bytecodesAfter));
            Direction toEnemy = curLocation.directionTo(closestEnemyLocation);
            int enemyID = enemyInfo.getID();
            // int getBytecodesLeft2 = Clock.getBytecodesLeft();
            // System.out.println("tryAttackingEnemies used bytecodes: " + (bytecodesLeft1 - getBytecodesLeft2));
            // rc.setIndicatorString("Closest enemy is at " + closestEnemyLocation.x + ", "
            // + closestEnemyLocation.y + " with health " + enemyHealth);
            // If still carrying enemy
            if (carryID != -1 && rc.isActionReady()) {
                // rc.setIndicatorString("I am carrying a rat " + carryID);
                Direction moveDirection = null;
                if (!isGoodAngle(curLocation, closestEnemyLocation)) {
                    for (Direction dir : directions) {
                        if (rc.canMove(dir) && isGoodAngle(curLocation.add(dir), closestEnemyLocation)) {
                            moveDirection = dir;
                            break;
                        }
                    }
                    if (moveDirection != null && rc.canMove(moveDirection)) {
                        // rc.setIndicatorString("Strafing direction is " + moveDirection);
                        rc.move(moveDirection);
                    } else {
                        // rc.setIndicatorString("Strafing direction is null");
                    }
                }
                curLocation = rc.getLocation();
                if (isGoodAngle(curLocation, closestEnemyLocation)) {
                    Direction toEnemy1 = curLocation.directionTo(closestEnemyLocation);
                    if (toEnemy1 != Direction.CENTER && toEnemy1 != rc.getDirection() && rc.canTurn()) {
                        rc.turn(toEnemy1);
                    }
                    if (rc.canThrowRat())
                        rc.throwRat();

                }
            }
            if (rc.canAttack(closestEnemyLocation) && enemyHealth <= 12) {
                if (enemyHealth <= 10)
                    rc.attack(closestEnemyLocation);
                else if (enemyHealth <= 11)
                    rc.attack(closestEnemyLocation, 1);
                else
                    rc.attack(closestEnemyLocation, 2);
            } else if (rc.canCarryRat(closestEnemyLocation)) {
                // Case 1: Close enough & health is low enough to attack + carry without
                // strafing
                // rc.setIndicatorString("Case 1");
                rc.carryRat(closestEnemyLocation);
            }

            if (numAllies >= numEnemies) {
                // if (strafeDirection == null) {
                // rc.setIndicatorString("Strafe direction is null");
                // } else
                // rc.setIndicatorString("Strafe direction is " + strafeDirection);

                if (strafeDirection != null) {
                    // Case 3: Can strafe out of enemy vision & carry
                    // rc.setIndicatorString("Case 3");
                    rc.move(strafeDirection);
                    curLocation = rc.getLocation();
                    if (!rc.canSenseLocation(closestEnemyLocation)) {
                        toEnemy = curLocation.directionTo(closestEnemyLocation);
                        if (rc.canTurn()) {
                            rc.turn(toEnemy);
                        }
                    }
                    if (rc.canAttack(closestEnemyLocation) && enemyHealth <= 12) {
                        if (enemyHealth <= 10)
                            rc.attack(closestEnemyLocation);
                        else if (enemyHealth <= 11)
                            rc.attack(closestEnemyLocation, 1);
                        else
                            rc.attack(closestEnemyLocation, 2);
                    } else if (rc.canCarryRat(closestEnemyLocation))
                        rc.carryRat(closestEnemyLocation);
                } else if (distToEnemy <= 8 && enemyHealth < myHealth
                        && (rc.canMove(toEnemy) || rc.canMove(toEnemy.rotateLeft())
                                || rc.canMove(toEnemy.rotateRight()))
                        && carryID == -1 && rc.isActionReady()) {
                    // Case 2: Can carry rat without strafing
                    // rc.setIndicatorString("Case 2");
                    if (rc.canMove(toEnemy))
                        rc.move(toEnemy);
                    else if (rc.canMove(toEnemy.rotateLeft()))
                        rc.move(toEnemy.rotateLeft());
                    else
                        rc.move(toEnemy.rotateRight());
                    curLocation = rc.getLocation();
                    if (!rc.canSenseLocation(closestEnemyLocation) && rc.canTurn()) {
                        rc.turn(curLocation.directionTo(closestEnemyLocation));
                    }
                    if (enemyHealth <= 12 && rc.canAttack(closestEnemyLocation)) {
                        if (enemyHealth <= 10)
                            rc.attack(closestEnemyLocation);
                        else if (enemyHealth <= 11)
                            rc.attack(closestEnemyLocation, 1);
                        else
                            rc.attack(closestEnemyLocation, 2);
                    } else if (rc.canCarryRat(closestEnemyLocation)) {
                        rc.carryRat(closestEnemyLocation);
                    } else {
                        // if flopped (we got baited & trapped)
                        myHealth = rc.getHealth();
                        int diffHealth = enemyHealth - myHealth;
                        if (diffHealth <= diffHealthThreshToAttack && rc.canAttack(closestEnemyLocation)) {
                            rc.attack(closestEnemyLocation, 2);
                        }
                        if (allCheeseAmount >= ratTrapThreshold2
                                && curRound - lastRoundSinceRatTrap >= minRoundsBetweenTraps
                                && numTrapsNearEnemy <= 1
                                && enemyHealth >= minHealthToPlaceTrap) {
                            MapLocation trapLocation = nearestRatTrapTo(closestEnemyLocation);
                            if (trapLocation != null && trapLocation.distanceSquaredTo(closestEnemyLocation) <= 2) {
                                rc.placeRatTrap(trapLocation);
                                lastRoundSinceRatTrap = curRound;
                            }
                        }
                    }
                    if (rc.canAttack(closestEnemyLocation)) {
                        rc.attack(closestEnemyLocation, 2);
                    }
                } else if (Math.max(Math.abs(curLocation.x - closestEnemyLocation.x),
                        Math.abs(curLocation.y - closestEnemyLocation.y)) == 3 && !hasWallsBetween) {
                    // Enemy is far away then move to enemy & place rat trap in between
                    // rc.setIndicatorString("Case 4");
                    boolean canMoveStraight = true;
                    MapLocation newLocation = curLocation.add(toEnemy);
                    if (!rc.canMove(toEnemy)
                            || checkOtherEnemies(newLocation, newLocation.directionTo(closestEnemyLocation), enemyID)) {
                        canMoveStraight = false;
                    }

                    boolean canMoveLeft = true;
                    newLocation = curLocation.add(toEnemy.rotateLeft());
                    if (!rc.canMove(toEnemy.rotateLeft())
                            || checkOtherEnemies(newLocation, newLocation.directionTo(closestEnemyLocation), enemyID)) {
                        canMoveLeft = false;
                    }

                    boolean canMoveRight = true;
                    newLocation = curLocation.add(toEnemy.rotateRight());
                    if (!rc.canMove(toEnemy.rotateRight())
                            || checkOtherEnemies(newLocation, newLocation.directionTo(closestEnemyLocation), enemyID)) {
                        canMoveRight = false;
                    }

                    if ((myHealth > enemyHealth || !isWithinRatSight(curLocation, closestEnemyLocation, enemyDirection) || numAllies > numEnemies) && (canMoveStraight || canMoveLeft || canMoveRight)) {
                        if (canMoveStraight) {
                            rc.move(toEnemy);
                        } else if (canMoveLeft) {
                            rc.move(toEnemy.rotateLeft());
                        } else {
                            rc.move(toEnemy.rotateRight());
                        }
                        if (rc.canTurn()) {
                            curLocation = rc.getLocation();
                            rc.turn(curLocation.directionTo(closestEnemyLocation));
                        }
                        myHealth = rc.getHealth();
                        if (myHealth < enemyHealth && allCheeseAmount >= ratTrapThreshold
                                && curRound - lastRoundSinceRatTrap >= minRoundsBetweenTraps
                                && numTrapsNearEnemy <= 1
                                && enemyHealth >= minHealthToPlaceTrap) {
                            MapLocation trapLocation = nearestRatTrapTo(closestEnemyLocation);
                            if (trapLocation != null && trapLocation.distanceSquaredTo(closestEnemyLocation) <= 2) {
                                rc.placeRatTrap(trapLocation);
                                lastRoundSinceRatTrap = curRound;
                            }
                        }
                    } else {
                        willNotMove = true;
                        if (rc.canTurn()) {
                            rc.turn(toEnemy);
                        }
                    }
                } else if (distToEnemy <= 8 && !hasWallsBetween) { // THIS IS NOT A TUNABLE VARIABLE
                    // If can't strafe and enemy is close enough
                    // rc.setIndicatorString("Case 5");
                    if (distToEnemy <= 2) {
                        // Enemy is right next to us
                        // Since we can't carry the rat --> rat has >= health --> maneuver
                        // Enemy rat MUST BE diagonal to our rat & is looking diagonally
                        // I think side maneuver is better because it messes with toDirection
                        int diffHealth = enemyHealth - myHealth;
                        if (rc.canAttack(closestEnemyLocation) && diffHealth <= diffHealthThreshToAttack) {
                            rc.attack(closestEnemyLocation, 2);
                        }

                        if (allCheeseAmount >= ratTrapThreshold2
                                && rc.isActionReady() && curRound - lastRoundSinceRatTrap >= minRoundsBetweenTraps
                                && numTrapsNearEnemy <= 1
                                && enemyHealth >= minHealthToPlaceTrap) {
                            MapLocation closestTrapLocation = nearestRatTrapTo(closestEnemyLocation);
                            if (closestTrapLocation != null
                                    && closestTrapLocation.distanceSquaredTo(closestEnemyLocation) <= 2) {
                                rc.placeRatTrap(closestTrapLocation);
                            }
                        }

                        if (rc.canAttack(closestEnemyLocation)) {
                            rc.attack(closestEnemyLocation, 2);
                        }

                        Direction oppToEnemy = toEnemy.opposite();
                        if (rc.canMove(oppToEnemy)) {
                            rc.move(oppToEnemy);
                        } else if (rc.canMove(oppToEnemy.rotateLeft())) {
                            rc.move(oppToEnemy.rotateLeft());
                        } else if (rc.canMove(oppToEnemy.rotateRight())) {
                            rc.move(oppToEnemy.rotateRight());
                        }

                        curLocation = rc.getLocation();
                    } else {
                        // rc.setIndicatorString("Case 5.2");
                        if (myHealth < enemyHealth) {
                            if (allCheeseAmount >= ratTrapThreshold2
                                    && rc.isActionReady() && curRound - lastRoundSinceRatTrap >= minRoundsBetweenTraps
                                    && numTrapsNearEnemy <= 1
                                    && enemyHealth >= minHealthToPlaceTrap) {
                                MapLocation closestTrapLocation = null;
                                int minDist = 1000000;

                                for (Direction dir : directions) {
                                    MapLocation trapLocation = closestEnemyLocation.add(dir);
                                    if (rc.canPlaceRatTrap(trapLocation)) {
                                        int dist = trapLocation.distanceSquaredTo(closestEnemyLocation);
                                        if (dist < minDist) {
                                            minDist = dist;
                                            closestTrapLocation = trapLocation;
                                        }
                                    }
                                }
                                if (closestTrapLocation != null
                                        && closestTrapLocation.distanceSquaredTo(closestEnemyLocation) <= 2) {
                                    rc.placeRatTrap(closestTrapLocation);
                                }
                            }

                            Direction oppToEnemy = toEnemy.opposite();
                            // rc.setIndicatorString("Opposite to enemy direction is " + oppToEnemy);
                            if (rc.canMove(oppToEnemy)) {
                                rc.move(oppToEnemy);
                            } else if (rc.canMove(oppToEnemy.rotateLeft())) {
                                rc.move(oppToEnemy.rotateLeft());
                            } else if (rc.canMove(oppToEnemy.rotateRight())) {
                                rc.move(oppToEnemy.rotateRight());
                            }
                        } else {
                            if (numAllies > numEnemies && rc.isActionReady()) {
                                // Move towards enemy if we have more allies than enemies
                                Direction moveDirection = null;
                                for (Direction dir : new Direction[] { toEnemy, toEnemy.rotateLeft(),
                                        toEnemy.rotateRight() }) {
                                    MapLocation newLocation = curLocation.add(dir);
                                    if (rc.canMove(dir) && newLocation.distanceSquaredTo(closestEnemyLocation) == 2) {
                                        moveDirection = dir;
                                        break;
                                    }
                                }
                                // If can move optimally, move
                                if (moveDirection != null) {
                                    rc.move(moveDirection);
                                    curLocation = rc.getLocation();
                                    if (rc.canTurn()) {
                                        rc.turn(curLocation.directionTo(closestEnemyLocation));
                                    }
                                    myHealth = rc.getHealth();
                                    if (myHealth < enemyHealth && allCheeseAmount >= ratTrapThreshold
                                            && curRound - lastRoundSinceRatTrap >= minRoundsBetweenTraps
                                            && numTrapsNearEnemy <= 1
                                            && enemyHealth >= minHealthToPlaceTrap) {
                                        MapLocation trapLocation = nearestRatTrapTo(closestEnemyLocation);
                                        if (trapLocation != null
                                                && trapLocation.distanceSquaredTo(closestEnemyLocation) <= 2) {
                                            rc.placeRatTrap(trapLocation);
                                            lastRoundSinceRatTrap = curRound;
                                        }
                                    }
                                    if (rc.canAttack(closestEnemyLocation)) {
                                        rc.attack(closestEnemyLocation, 2);
                                    }
                                } else { // else retreat
                                    Direction oppToEnemy = toEnemy.opposite();
                                    // rc.setIndicatorString("Opposite to enemy direction is " + oppToEnemy);
                                    if (rc.canMove(oppToEnemy)) {
                                        rc.move(oppToEnemy);
                                    } else if (rc.canMove(oppToEnemy.rotateLeft())) {
                                        rc.move(oppToEnemy.rotateLeft());
                                    } else if (rc.canMove(oppToEnemy.rotateRight())) {
                                        rc.move(oppToEnemy.rotateRight());
                                    }
                                }
                            } else {
                                // Retreat if we have equalw allies than enemies
                                Direction oppToEnemy = toEnemy.opposite();
                                // rc.setIndicatorString("Opposite to enemy direction is " + oppToEnemy);
                                if (rc.canMove(oppToEnemy)) {
                                    rc.move(oppToEnemy);
                                } else if (rc.canMove(oppToEnemy.rotateLeft())) {
                                    rc.move(oppToEnemy.rotateLeft());
                                } else if (rc.canMove(oppToEnemy.rotateRight())) {
                                    rc.move(oppToEnemy.rotateRight());
                                }
                            }
                        }
                    }
                } else if (distToEnemy <= 8 && !checkWallBetween(curLocation, closestEnemyLocation)) {
                    // If enemy is close enough and between them is a dirt block
                    if (myHealth < enemyHealth) {
                        // Move opposite to enemy
                        Direction oppToEnemy = toEnemy.opposite();
                        if (rc.canMove(oppToEnemy)) {
                            rc.move(oppToEnemy);
                        } else if (rc.canMove(oppToEnemy.rotateLeft())) {
                            rc.move(oppToEnemy.rotateLeft());
                        } else if (rc.canMove(oppToEnemy.rotateRight())) {
                            rc.move(oppToEnemy.rotateRight());
                        }
                        curLocation = rc.getLocation();
                        Direction dirToEnemyDirection = curLocation.directionTo(closestEnemyLocation);
                        if (rc.canTurn()) {
                            rc.turn(dirToEnemyDirection);
                        }
                    } else {
                        willNotMove = true;
                        Direction dirToEnemyDirection = curLocation.directionTo(closestEnemyLocation);
                        if (rc.canTurn()) {
                            rc.turn(dirToEnemyDirection);
                        }
                    }
                }
            } else if (!hasWallsBetween) {

                int diffHealth = enemyHealth - myHealth;
                if (rc.canAttack(closestEnemyLocation) && diffHealth <= diffHealthThreshToAttack) {
                    rc.attack(closestEnemyLocation, 2);
                }

                if (allCheeseAmount >= ratTrapThreshold2
                        && rc.isActionReady() && curRound - lastRoundSinceRatTrap >= minRoundsBetweenTraps
                        && numTrapsNearEnemy <= 1
                        && enemyHealth >= minHealthToPlaceTrap) {
                    MapLocation closestTrapLocation = null;
                    int minDist = 1000000;
                    for (Direction dir : directions) {
                        MapLocation trapLocation = closestEnemyLocation.add(dir);
                        if (rc.canPlaceRatTrap(trapLocation)) {
                            int dist = trapLocation.distanceSquaredTo(closestEnemyLocation);
                            if (dist < minDist) {
                                minDist = dist;
                                closestTrapLocation = trapLocation;
                            }
                        }
                    }
                    if (closestTrapLocation != null && closestTrapLocation.distanceSquaredTo(closestEnemyLocation) <= 2) {
                        rc.placeRatTrap(closestTrapLocation);
                    }
                }

                if (rc.canAttack(closestEnemyLocation)) {
                    rc.attack(closestEnemyLocation, 2);
                }

                Direction oppToEnemy = toEnemy.opposite();
                if (rc.canMove(oppToEnemy)) {
                    rc.move(oppToEnemy);
                } else if (rc.canMove(oppToEnemy.rotateLeft())) {
                    rc.move(oppToEnemy.rotateLeft());
                } else if (rc.canMove(oppToEnemy.rotateRight())) {
                    rc.move(oppToEnemy.rotateRight());
                }

                curLocation = rc.getLocation();
            }
        } else if (closestEnemyLocation != null) {
            // If enemy is far away, move towards enemy if have more allies than enemies
            RobotInfo enemyInfo = rc.senseRobotAtLocation(closestEnemyLocation);
            int enemyHealth = enemyInfo.getHealth();
            boolean hasWallsBetween = checkWallOrDirtBetween(curLocation, closestEnemyLocation);
            if (numAllies >= numEnemies && !hasWallsBetween && myHealth >= enemyHealth) {
                // Move towards enemy
                Direction toEnemy = curLocation.directionTo(closestEnemyLocation);
                if (rc.canMove(toEnemy)) {
                    rc.move(toEnemy);
                } else if (rc.canMove(toEnemy.rotateLeft())) {
                    rc.move(toEnemy.rotateLeft());
                } else if (rc.canMove(toEnemy.rotateRight())) {
                    rc.move(toEnemy.rotateRight());
                }
                curLocation = rc.getLocation();
            } else if (!hasWallsBetween) {
                willNotMove = true;
            }
        }
        curLocation = rc.getLocation();
        closestEnemyLocation = getClosestEnemy();
        if (closestEnemyLocation != null && rc.canTurn()
                && !checkWallOrDirtBetween(curLocation, closestEnemyLocation)) {
            Direction toEnemy = curLocation.directionTo(closestEnemyLocation);
            if (toEnemy != Direction.CENTER) {
                rc.turn(toEnemy);
            }
        }

        if (closestEnemyLocation != null) {
            sendSqueak(MessageType.ENEMY_BABY_RAT_LOCATION, encodeLocation(closestEnemyLocation));
        }

        if (rc.getCarrying() != null) {
            if (willThrowRat()) {
                rc.throwRat();
            }
        }
        if (rc.isActionReady()) {
            curLocation = rc.getLocation();
            for (Direction dir : directions) {
                MapLocation tileLocation = curLocation.add(dir);
                if (!rc.canSenseRobotAtLocation(tileLocation)) {
                    continue;
                }
                RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
                if (tileRobot.getType().isCatType()) {
                    rc.attack(tileLocation);
                    break;
                }
            }
        }
        // int getBytecodesLeft3 = Clock.getBytecodesLeft();
        // System.out.println("tryAttackingEnemies total used bytecodes: " + (bytecodesLeft1 - getBytecodesLeft3));
    }


    private static void tryComboEnemies() throws GameActionException {
        // int bytecodesBefore = Clock.getBytecodesLeft();
        MapLocation closestEnemyLocation = getClosestEnemy();
        RobotInfo carryInfo = rc.getCarrying();
        int carryID = -1;
        if (carryInfo != null)
            carryID = carryInfo.getID();
        if ((closestEnemyLocation == null || Math.max(Math.abs(closestEnemyLocation.x - curLocation.x),
                Math.abs(closestEnemyLocation.y - curLocation.y)) > 3) && carryID != -1 && rc.isActionReady()) {
            curLocation = rc.getLocation();
            MapLocation targetComboLocation = null, closestLocation = null;
            int minDist = 1_000_000;
            for (MapInfo tile : nearbyTiles) {
                MapLocation tileLocation = tile.getMapLocation();
                if (tile.isWall() || tile.isDirt()) {
                    if (isGoodAngle(curLocation, tileLocation)) {
                        targetComboLocation = tileLocation;
                        break;
                    }
                    int dist = tileLocation.distanceSquaredTo(curLocation);
                    if (dist < minDist) {
                        minDist = dist;
                        closestLocation = tileLocation;
                    }
                }
            }
            // if (closestLocation == null) {
            //     rc.setIndicatorString("closest location is null");
            // } else {
            //     rc.setIndicatorString("closest location is " + closestLocation.x + ", " + closestLocation.y);
            // }
            if (targetComboLocation == null && closestLocation != null) {
                Direction moveDirection = null;
                for (Direction dir : directions) {
                    MapLocation newLocation = curLocation.add(dir);
                    if (rc.canMove(dir) && isGoodAngle(newLocation, closestLocation)) {
                        moveDirection = dir;
                        break;
                    }
                }
                if (moveDirection != null) {
                    rc.move(moveDirection);
                    targetComboLocation = closestLocation;
                }
            }
            if (targetComboLocation == null) {
                return;
            }
            // rc.setIndicatorString("Target combo location is " + targetComboLocation.x +
            // ", " + targetComboLocation.y);
            curLocation = rc.getLocation();
            Direction toComboLocation = curLocation.directionTo(targetComboLocation);
            if (toComboLocation != Direction.CENTER && rc.canTurn()) {
                rc.turn(toComboLocation);
            }
            if (rc.canThrowRat()) {
                rc.throwRat();
            }
        }
        // int bytecodesAfter = Clock.getBytecodesLeft();
        // System.out.println("tryComboEnemies used bytecodes: " + (bytecodesBefore - bytecodesAfter));
    }

    private static void squeakMineLocationToKing() throws GameActionException {
        if (cheeseMineLocations.isEmpty())
            return;
        // rc.setIndicatorString("Distance to king is " + rc.getLocation().distanceSquaredTo(kingRatLocation));
        if (rc.getLocation().distanceSquaredTo(kingRatLocation) > SQUEAK_RADIUS_SQR)
            return;
        // rc.setIndicatorString("Trying to squeak mine location to king");
        // Deleting element in the middle of the list... Playing god today are we ?
        // Prioritize communicating closest mines first
        int id = -1;
        int maxDist = 0;
        MapLocation furthestMineLocation = null;
        for (int i = 0; i < cheeseMineLocations.size(); i++) {
            MapLocation mineLocation = cheeseMineLocations.get(i);
            // System.out.println("Trying position " + mineLocation.x + ", " + mineLocation.y);
            boolean hasAppearedInGlobalArray = findLocationinGlobal(mineLocation);
            if (hasAppearedInGlobalArray)
                continue;
            int distanceToKing = mineLocation.distanceSquaredTo(kingRatLocation);
            if (maxDist < distanceToKing) {
                maxDist = distanceToKing;
                id = i;
                furthestMineLocation = mineLocation;
            }
        }

        if (id == -1)
            return;
        // System.out.println("I will squeak position " + furthestMineLocation.x + ", " + furthestMineLocation.y);
        sendSqueak(MessageType.CHEESE_MINE, encodeLocation(furthestMineLocation));
        cheeseMineLocations.remove(id);
    }

    private static void Searching() throws GameActionException {
        if (searchingLocation == null) {
            Explore.flag = false;
        }
        if (targetEnemyLocation == null)
            targetEnemyLocation = fetchNearbyEnemy();

        MapLocation closestEnemyLocation = getClosestEnemy();
        if (closestEnemyLocation == null) {
            if (targetEnemyLocation != null && rc.getLocation().distanceSquaredTo(targetEnemyLocation) <= 20
                    && rc.canTurn()) {
                // rc.setIndicatorString(
                //         "targetEnemyLocation is at " + targetEnemyLocation.x + ", " + targetEnemyLocation.y);
                Direction toEnemy = rc.getLocation().directionTo(targetEnemyLocation);
                if (toEnemy != Direction.CENTER)
                    rc.turn(toEnemy);
                closestEnemyLocation = getClosestEnemy();
            }
        }
        tryAttackingEnemies(closestEnemyLocation);
        tryComboEnemies();

        // If there is no cheese at target anymore, reset targetCheeseLocation
        if (targetCheeseLocation != null) {
            if (rc.canSenseLocation(targetCheeseLocation)) {
                MapInfo tileInfo = rc.senseMapInfo(targetCheeseLocation);
                if (tileInfo.getCheeseAmount() == 0) {
                    targetCheeseLocation = null;
                }
            }
        }

        // Pick up cheese at targetCheeseLocation and reset to null
        if (targetCheeseLocation != null && rc.canPickUpCheese(targetCheeseLocation)) {
            rc.pickUpCheese(targetCheeseLocation);
            targetCheeseLocation = null;
        }

        // Look for cheese mines to camp at
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (tile.getCheeseAmount() > 0) {
                if (rc.canPickUpCheese(tileLocation)) {
                    rc.pickUpCheese(tileLocation);
                } else if (targetCheeseLocation == null) {
                    targetCheeseLocation = tileLocation;
                }
            }
        }

        tryTransferCheeseToKing(kingRatLocation);

        if (targetEnemyLocation != null) {
            BugNavigator.moveTo(targetEnemyLocation, true);
        } else if (targetCheeseLocation == null) {
            Explore.doRandomMove(breakDirt);
        } else {
            BugNavigator.moveTo(targetCheeseLocation, true);
        }

        if (targetEnemyLocation != null && rc.canSenseLocation(targetEnemyLocation)) {
            targetEnemyLocation = null;
        }

        if (closestEnemyLocation != null && rc.canTurn()) {
            Direction toEnemy = rc.getLocation().directionTo(closestEnemyLocation);
            if (toEnemy != Direction.CENTER)
                rc.turn(toEnemy);
        }

        curLocation = rc.getLocation();

        // Transit SEARCHING -> ?
        boolean hasFoundSymmetryGlobally = SymmetryManager.hasFoundSymmetryGlobally();

        MapLocation tempEnemyKingLocation = isEnemyKingNearby();

        nearbyTiles = rc.senseNearbyMapInfos();
        catLocation = isCatNearby();
        // TOO Too much cheese, ABSOLUTE RETURN
        if (rc.getRawCheese() >= hypercheese) {
            ratStatus = RatState.RETURNING;
            targetCheeseLocation = null;
            targetEnemyLocation = null;
            searchingLocation = null;
            // Bait cat away from king
        } else if (catLocation != null
                && isWithinCatSight(curLocation, catLocation, rc.senseRobotAtLocation(catLocation).getDirection())
                && distanceToKing() >= baitingDistance && numberBaitingRats() == 0) {
            MapLocation baitLocation = getBaitLocation();
            if (baitLocation != null) {
                ratStatus = RatState.BAITING;
                baitCatLocation = baitLocation;
                searchingLocation = null;
                targetEnemyLocation = null;
                delayTurns = 1;
            }
        }
        // If has found symmetry
        else if (symmetry != -1 && !hasFoundSymmetryGlobally) {
            isReturningSymmetry = true;
            ratStatus = RatState.RETURNING;
            targetCheeseLocation = null;
            targetEnemyLocation = null;
            searchingLocation = null;
        }
        // Found enemy king, start attacking
        else if (tempEnemyKingLocation != null) {
            // System.out.println("Enemy king is at " + tempEnemyKingLocation.x + ", " + tempEnemyKingLocation.y);
            // Has not found symmetry, attack
            // System.out.println("Enemy king location is " + tempEnemyKingLocation.x + ", " + tempEnemyKingLocation.y);
            if (!hasFoundEnemyKingGlobally()) {
                ratStatus = RatState.RETURNING;
                targetCheeseLocation = null;
                targetEnemyLocation = null;
                enemyLocMessage = encodeLocation(tempEnemyKingLocation);
                isReturningEnemyLocation = true;
                searchingLocation = null;
            } else {
                MapLocation oldEnemyLocation = getEnemyKingLocation();
                if (tempEnemyKingLocation.distanceSquaredTo(oldEnemyLocation) > threshToReturnEnemyKingLocation) {
                    ratStatus = RatState.RETURNING;
                    targetCheeseLocation = null;
                    targetEnemyLocation = null;
                    enemyLocMessage = encodeLocation(tempEnemyKingLocation);
                    isReturningEnemyLocation = true;
                    searchingLocation = null;
                } else {
                    ratStatus = RatState.ATTACKING;
                    targetCheeseLocation = null;
                    targetEnemyLocation = null;
                    searchingLocation = null;
                }
            }
        }
    }

    private static void Returning() throws GameActionException {
        MapLocation closestEnemyLocation = getClosestEnemy();
        // avoid unnecessary combat
        if (closestEnemyLocation != null) {
            if (curLocation.distanceSquaredTo(closestEnemyLocation) > 8 && closestEnemyLocation
                    .distanceSquaredTo(kingRatLocation) > curLocation.distanceSquaredTo(kingRatLocation)) {
                // Don't attack
            } else {
                tryAttackingEnemies(closestEnemyLocation);

                if (targetEnemyLocation == null)
                    targetEnemyLocation = fetchNearbyEnemy();
                if (targetEnemyLocation != null) {
                    // rc.setIndicatorString("Moving to closest enemy location at " +
                    // targetEnemyLocation.x + ", "
                    // + targetEnemyLocation.y);
                    BugNavigator.moveTo(targetEnemyLocation, true);
                }
            }
        }
        tryComboEnemies();
        // tryAttackingEnemies(closestEnemyLocation);

        // If there is no cheese at target anymore, reset targetCheeseLocation
        if (targetCheeseLocation != null) {
            if (rc.canSenseLocation(targetCheeseLocation)) {
                MapInfo tileInfo = rc.senseMapInfo(targetCheeseLocation);
                if (tileInfo.getCheeseAmount() == 0) {
                    targetCheeseLocation = null;
                }
            }
        }

        if (targetEnemyLocation != null && rc.canSenseLocation(targetEnemyLocation)) {
            targetEnemyLocation = null;
        }

        // Pick up cheese at targetCheeseLocation and reset to null
        if (targetCheeseLocation != null && rc.canPickUpCheese(targetCheeseLocation)) {
            rc.pickUpCheese(targetCheeseLocation);
            targetCheeseLocation = null;
        }

        nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (tile.getCheeseAmount() > 0 && rc.getRawCheese() <= maxCheeseAmount) {
                if (rc.canPickUpCheese(tileLocation)) {
                    rc.pickUpCheese(tileLocation);
                } else if (targetCheeseLocation == null) {
                    targetCheeseLocation = tileLocation;
                }
            }
        }

        nearbyTiles = rc.senseNearbyMapInfos();

        Direction toKingRat = curLocation.directionTo(kingRatLocation);
        if (curLocation.distanceSquaredTo(kingRatLocation) <= 8 && rc.getDirection() != toKingRat && rc.canTurn()) {
            rc.turn(toKingRat);
        }
        boolean transferedCheese = false, transferedSymmetry = false, transferredEnemyLocation = false;
        // rc.setIndicatorString("Is returning " + isReturningSymmetry + " " +
        // isReturningEnemyLocation + " raw cheese "
        // + rawCheeseAmount);
        if (!isReturningSymmetry && !isReturningEnemyLocation) { // returning cheese
            if (tryTransferCheeseToKing(kingRatLocation)) {
                if (!SymmetryManager.hasFoundSymmetryGlobally()) {
                    int symSqueak = SymmetryManager.writeBrokenSymmetry();
                    if (symSqueak > 0)
                        sendSqueak(MessageType.SYMMETRY, symSqueak);
                }

                transferedCheese = true;
                gettingCheese = false;
                targetCheeseLocation = null;
                campingPosition = null;
                cheeseCampLocation = null;
                isReturningToCamp = false;
                isScoutingNearby = false;
                scoutingTurns = 0;
            }
        } else if (isReturningSymmetry) { // returning symmetry
            if (curLocation.distanceSquaredTo(kingRatLocation) <= 16) {
                tryTransferCheeseToKing(kingRatLocation);
                if (!SymmetryManager.hasFoundSymmetryGlobally()) {
                    int symSqueak = SymmetryManager.writeBrokenSymmetry();
                    if (symSqueak > 0)
                        sendSqueak(MessageType.SYMMETRY, symSqueak);
                    transferedSymmetry = true;
                    gettingCheese = false;
                    targetCheeseLocation = null;
                    campingPosition = null;
                    cheeseCampLocation = null;
                    isReturningToCamp = false;
                    isScoutingNearby = false;
                    scoutingTurns = 0;
                }
            }
        } else { // returning enemy location
            // rc.setIndicatorString("Is returning enemy king location at " +
            // decodeLocation(enemyLocMessage).x + ", " +
            // decodeLocation(enemyLocMessage).y);
            if (curLocation.distanceSquaredTo(kingRatLocation) <= 16) {
                sendSqueak(MessageType.ENEMY_KING_RAT_LOCATION, enemyLocMessage);
                enemyLocMessage = -1;
                transferredEnemyLocation = true;
                enemyLocMessage = -1;
                gettingCheese = false;
                targetCheeseLocation = null;
                campingPosition = null;
                cheeseCampLocation = null;
                isReturningToCamp = false;
                isScoutingNearby = false;
                scoutingTurns = 0;
            }
        }
        if (isReturningSymmetry && SymmetryManager.hasFoundSymmetryGlobally()) {
            transferedSymmetry = true;
        }

        if (isReturningEnemyLocation && hasFoundEnemyKingGlobally() && enemyLocMessage != -1
                && decodeLocation(enemyLocMessage).distanceSquaredTo(getEnemyKingLocation()) <= 8) {
            transferredEnemyLocation = true;
        }
        if (enemyLocMessage == -1) {
            transferredEnemyLocation = true;
        }
        squeakMineLocationToKing(); // Prefer squeaking symmetry types over cheese mines

        // rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);

        if (targetCheeseLocation != null) {
            // rc.setIndicatorString(
            // "Moving to target cheese location at " + targetCheeseLocation.x + ", " +
            // targetCheeseLocation.y);
            BugNavigator.moveTo(targetCheeseLocation, true);
        } else {
            // rc.setIndicatorString("Moving to king rat location at " + kingRatLocation.x +
            // ", " + kingRatLocation.y);
            BugNavigator.moveTo(kingRatLocation, true);
        }

        if (closestEnemyLocation != null && rc.canTurn()) {
            Direction toEnemy = rc.getLocation().directionTo(closestEnemyLocation);
            if (toEnemy != Direction.CENTER)
                rc.turn(toEnemy);
        }

        // Transit RETURNING -> ?
        boolean transitionedToBait = false;
        nearbyTiles = rc.senseNearbyMapInfos();
        // Transfered, or cannot find king rat location (lmao get orphaned), switch back
        // to searching
        catLocation = isCatNearby();
        // DO NOT BAIT CAT OVER TO OUR HOME, except when we have a fuck ton of cheese
        if (catLocation != null
                && isWithinCatSight(curLocation, catLocation, rc.senseRobotAtLocation(catLocation).getDirection())
                && distanceToKing() >= baitingDistance && numberBaitingRats() == 0) {
            MapLocation baitLocation = getBaitLocation();
            if (baitLocation != null) {
                ratStatus = RatState.BAITING;
                targetEnemyLocation = null;
                targetCheeseLocation = null;
                baitCatLocation = baitLocation;
                delayTurns = 1;
                transitionedToBait = true;
            }
        }
        if (!transitionedToBait
                && (!isReturningSymmetry && !isReturningEnemyLocation && (transferedCheese || rc.getRawCheese() == 0))
                || (isReturningSymmetry && transferedSymmetry) || (isReturningEnemyLocation && transferredEnemyLocation)
                || kingRatLocation == null) {
            ratStatus = originalState;
            targetCheeseLocation = null;
            targetEnemyLocation = null;
            isReturningSymmetry = false;
            isReturningEnemyLocation = false;
        }
        // Only -> SEARCHING for SEARCHING has transitions to all other states with the
        // proper conditions
    }

    private static void Baiting() throws GameActionException {
        // Try attacking first
        MapLocation closestEnemyLocation = getClosestEnemy();
        if (targetEnemyLocation == null)
            targetEnemyLocation = fetchNearbyEnemy();
        if (closestEnemyLocation == null) {
            if (targetEnemyLocation != null && rc.getLocation().distanceSquaredTo(targetEnemyLocation) <= 20
                    && rc.canTurn()) {
                Direction toEnemy = rc.getLocation().directionTo(targetEnemyLocation);
                if (toEnemy != Direction.CENTER)
                    rc.turn(toEnemy);
                closestEnemyLocation = getClosestEnemy();
            }
        }
        tryAttackingEnemies(closestEnemyLocation);
        tryComboEnemies();

        // Try transfer first
        tryTransferCheeseToKing(kingRatLocation);

        if (targetEnemyLocation != null) {
            BugNavigator.moveTo(targetEnemyLocation, true);
        }
        nearbyTiles = rc.senseNearbyMapInfos();

        // rc.setIndicatorDot(rc.getLocation(), 0, 124, 255);

        // Find nearby cat
        RobotInfo catInfo = null;
        MapLocation catLocation = null;
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            RobotInfo robotTile = rc.senseRobotAtLocation(tileLoc);

            if (robotTile == null)
                continue;
            if (!robotTile.getType().isCatType())
                continue;
            catInfo = robotTile;
            catLocation = tileLoc; // Because of the stupid 2x2 box goddammit
            break;
        }
        handleCat(catInfo, catLocation);

        if (targetEnemyLocation != null && rc.canSenseLocation(targetEnemyLocation)) {
            targetEnemyLocation = null;
        }

        if (closestEnemyLocation != null && rc.canTurn()) {
            Direction toEnemy = rc.getLocation().directionTo(closestEnemyLocation);
            if (toEnemy != Direction.CENTER)
                rc.turn(toEnemy);
        }

        int distToBaitLocation = rc.getLocation().distanceSquaredTo(baitCatLocation);
        // Transit BAITING -> ?
        // TOO Too much cheese, ABSOLUTE RETURN
        if (rc.getRawCheese() >= hypercheese) {
            baitCatLocation = null;
            ratStatus = RatState.RETURNING;
            targetEnemyLocation = null;
        } else if (isEnemyKingNearby() != null) {
            baitCatLocation = null;
            ratStatus = RatState.ATTACKING; // Go attack enemy king, cat will hopefully follow us to the enemy king.
            targetEnemyLocation = null;
        } else if (numberBaitingRats() > 1 || distToBaitLocation <= 16 || catLocation == null) {

            // If there are other rats already squeaking, switch back to searching
            int minId = 1_000_000_000;
            for (Message message : nearbySqueaks) {
                if (readSqueak(message.getBytes()).type != MessageType.BAITING)
                    continue;
                minId = Math.min(minId, message.getSenderID());
            }

            // We leave only the rat with minimum id to bait
            if (myId != minId || distToBaitLocation <= 16 || catLocation == null) {
                baitCatLocation = null;
                ratStatus = originalState;
                targetEnemyLocation = null;
            }
        }
    }

    private static void handleCat(RobotInfo catInfo, MapLocation catLocation) throws GameActionException {
        if (catInfo == null)
            return;
        Direction catDirection = catInfo.getDirection();
        // rc.setIndicatorString("Cat at " + catLocation + " facing " + catDirection);

        // First, try placing traps
        int catTrapThresh = 700;
        if (allCheeseAmount >= catTrapThresh) {
            int minDist = 9; // Only place in radius 9
            MapLocation trapLocation = null;
            for (MapInfo tile : nearbyTiles) {
                MapLocation tileLoc = tile.getMapLocation();
                if (!rc.canPlaceCatTrap(tileLoc))
                    continue;
                if (!isWithinCatSight(tileLoc, catLocation, catDirection)
                        || !willCatEnter3x3(catLocation, catDirection, tileLoc))
                    continue;

                int dist = tileLoc.distanceSquaredTo(catLocation);
                // Try placing trap in front
                if (dist <= minDist) {
                    minDist = dist;
                    trapLocation = tileLoc;
                }
            }
            if (trapLocation != null && rc.canPlaceCatTrap(trapLocation))
                rc.placeCatTrap(trapLocation);
        }

        // Try attacking
        tryAttackingCat(catLocation);

        boolean inCatSight = isWithinCatSight(rc.getLocation(), catLocation, catDirection);
        if (!inCatSight)
            BugNavigator.moveTo(catLocation);
        else {
            // Turn and carry on with life
            if (rc.canTurn())
                rc.turn(rc.getLocation().directionTo(catLocation).opposite());
        }
        // Try attacking again
        tryAttackingCat(catLocation);
    }

    private static void tryAttackingCat(MapLocation catLocation) throws GameActionException {
        for (MapLocation potentialCatLoc : rc.getAllLocationsWithinRadiusSquared(catLocation, 2)) {
            RobotInfo robotInfo = rc.senseRobotAtLocation(potentialCatLoc);
            if (robotInfo == null)
                continue;
            if (!robotInfo.getType().isCatType())
                continue;
            if (!rc.canAttack(potentialCatLoc))
                continue;

            rc.attack(potentialCatLoc);
            return;
        }
    }

    private static void Attacking() throws GameActionException {
        MapLocation closestEnemyLocation = getClosestEnemy();
        tryAttackingEnemies(closestEnemyLocation);
        tryComboEnemies();

        // Update enemy king rat location
        enemyKingLocation = isEnemyKingNearby();
        // if (enemyKingLocation != null) {
        //     rc.setIndicatorDot(enemyKingLocation, 0, 0, 0);
        // }

        // Transit ATTACKING -> SEARCHING
        // Check if enemy king rat location is known & visible. Else -> SEARCHING
        if (enemyKingLocation == null || !rc.canSenseLocation(enemyKingLocation)) {
            FoundEnemyKing = false;
            ratStatus = originalState;
            return;
        }

        int squeakMessage = encodeLocation(enemyKingLocation);
        sendSqueak(MessageType.ENEMY_KING_RAT_LOCATION, squeakMessage);

        // Find nearest trap position
        if (allCheeseAmount >= ratTrapThreshold && (curRound - lastRoundSinceRatTrap) >= minRoundsBetweenTraps) {
            MapLocation closestTrapPosition = nearestRatTrapTo(enemyKingLocation);

            if (closestTrapPosition != null) {
                rc.placeRatTrap(closestTrapPosition);
                lastRoundSinceRatTrap = curRound;
                // System.out.println("Squeaked position " + squeakMessage);
                MapLocation enemyLocation = decodeLocation(squeakMessage);
                // System.out.println("Enemy king location is at " + enemyLocation.x + ", " + enemyLocation.y);
            }
        }

        // Try transfer first
        tryTransferCheeseToKing(kingRatLocation);

        if (curLocation.distanceSquaredTo(enemyKingLocation) > 2) {
            // Move closer to enemy king rat
            BugNavigator.moveTo(enemyKingLocation, true);
        }

        if (closestEnemyLocation != null && rc.canTurn()) {
            Direction toEnemy = rc.getLocation().directionTo(closestEnemyLocation);
            if (toEnemy != Direction.CENTER)
                rc.turn(toEnemy);
        }

        Direction toEnemyKing = curLocation.directionTo(enemyKingLocation);
        if (toEnemyKing != Direction.CENTER && rc.canTurn())
            rc.turn(toEnemyKing);

        // If can attack enemy king rat, do so
        if (rc.canAttack(enemyKingLocation)) {
            rc.attack(enemyKingLocation);
            // System.out.println("Squeaked position " + squeakMessage);
            MapLocation enemyLocation = decodeLocation(squeakMessage);
            // System.out.println("Enemy king location is at " + enemyLocation.x + ", " + enemyLocation.y);
        }
    }

    private static void Aggressive() throws GameActionException {

        MapLocation closestEnemyLocation = getClosestEnemy();
        if (targetEnemyLocation == null)
            targetEnemyLocation = fetchNearbyEnemy();
        if (closestEnemyLocation == null) {
            if (targetEnemyLocation != null && rc.getLocation().distanceSquaredTo(targetEnemyLocation) <= 20
                    && rc.canTurn()) {
                Direction toEnemy = rc.getLocation().directionTo(targetEnemyLocation);
                if (toEnemy != Direction.CENTER)
                    rc.turn(toEnemy);
                closestEnemyLocation = getClosestEnemy();
            }
        }
        tryAttackingEnemies(closestEnemyLocation);
        tryComboEnemies();
        // Try transfer first
        tryTransferCheeseToKing(kingRatLocation);

        if (targetEnemyLocation != null) {
            // rc.setIndicatorString(
            // "Closest Enemy location is " + targetEnemyLocation.x + ", " +
            // targetEnemyLocation.y);
            BugNavigator.moveTo(targetEnemyLocation, true);
        }
        nearbyTiles = rc.senseNearbyMapInfos();

        // rc.setIndicatorDot(rc.getLocation(), 255, 255, 255);
        MapLocation enemyKingRatLocation = getEnemyKingLocation();
        if (targetLocation == null) {
            if (hasGoneToSymLoc == false) { // Go to symmetric location
                if (enemyKingRatLocation == null)
                    targetLocation = SymmetryManager.getSymmetricLocation(originalKingLocation, symmetry);
                else
                    targetLocation = enemyKingRatLocation;
                oldSymLocation = targetLocation;
            } else { // Search other positions
                if (enemyKingRatLocation != null && oldSymLocation.distanceSquaredTo(enemyKingRatLocation) > 20) {
                    targetLocation = enemyKingRatLocation;
                    oldSymLocation = targetLocation;
                } else {
                    targetIndex = Movement.getNewTarget();
                    targetLocation = targets[targetIndex];
                }
            }
            // Reset counter;
            turnsMovingToTarget = 0;
        }
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (tile.getCheeseAmount() > 0 && tile.getCheeseAmount() + rawCheeseAmount <= 50) {
                if (rc.canPickUpCheese(tileLocation)) {
                    rc.pickUpCheese(tileLocation);
                }
            }
        }

        turnsMovingToTarget++;
        BugNavigator.moveTo(targetLocation, breakDirt);

        if (targetEnemyLocation != null && rc.canSenseLocation(targetEnemyLocation)) {
            targetEnemyLocation = null;
        }

        if (closestEnemyLocation != null && rc.canTurn()) {
            Direction toEnemy = rc.getLocation().directionTo(closestEnemyLocation);
            if (toEnemy != Direction.CENTER)
                rc.turn(toEnemy);
        }

        curLocation = rc.getLocation();

        if (curLocation.distanceSquaredTo(targetLocation) <= 2 || turnsMovingToTarget >= 100
                || (rc.canSenseLocation(targetLocation) && rc.senseMapInfo(targetLocation).isWall())) {
            if (targetIndex != -1)
                visitedTargets[targetIndex] = curRound;
            targetIndex = -1;
            targetLocation = null;
            hasGoneToSymLoc = true;
        }
        // Transition to other states
        nearbyTiles = rc.senseNearbyMapInfos();
        catLocation = isCatNearby();
        if (catLocation != null
                && isWithinCatSight(curLocation, catLocation, rc.senseRobotAtLocation(catLocation).getDirection())
                && distanceToKing() >= baitingDistance && numberBaitingRats() == 0) {
            MapLocation baitLocation = getBaitLocation();
            if (baitLocation != null) {
                ratStatus = RatState.BAITING;
                targetEnemyLocation = null;
                targetLocation = null;
                baitCatLocation = baitLocation;
                delayTurns = 1;
            }
        } else if (isEnemyKingNearby() != null) {
            ratStatus = RatState.ATTACKING;
            targetEnemyLocation = null;
            targetLocation = null;
        }
    }

    private static void Promoting() throws GameActionException {
        // rc.setIndicatorDot(curLocation, 255, 0, 255);
        if (promoteLocation == null) {
            int minDist = 100000000;
            MapLocation kingLocation = updateKingRatLocation();
            for (int i = 0; i <= 1; i++) {
                for (int j = 0; j <= 1; j++) {
                    MapLocation checking = new MapLocation(i * (mapWidth - 1), j * (mapHeight - 1));
                    if (kingLocation == null) {
                        continue;
                    }
                    int dist = checking.distanceSquaredTo(kingLocation);
                    if (dist < minDist) {
                        minDist = dist;
                        promoteLocation = checking;
                    }
                }
            }
        }

        // Try transfer first
        tryTransferCheeseToKing(kingRatLocation);

        if (curLocation.distanceSquaredTo(promoteLocation) > 49) {
            BugNavigator.moveTo(promoteLocation, true);
        } else {
            Direction toPromote = curLocation.directionTo(promoteLocation);
            if (rc.canTurn() && rc.getDirection() != toPromote) {
                rc.turn(toPromote);
            }
            while (true) {
                boolean canMoveCloser = false;
                int dist = curLocation.distanceSquaredTo(promoteLocation);
                if (rc.canMove(toPromote) && curLocation.add(toPromote).distanceSquaredTo(promoteLocation) < dist) {
                    rc.move(toPromote);
                    canMoveCloser = true;
                }

                toPromote = toPromote.rotateLeft();

                if (rc.canMove(toPromote) && curLocation.add(toPromote).distanceSquaredTo(promoteLocation) < dist) {
                    rc.move(toPromote);
                    canMoveCloser = true;
                }

                toPromote = toPromote.rotateRight().rotateRight();

                if (rc.canMove(toPromote) && curLocation.add(toPromote).distanceSquaredTo(promoteLocation) < dist) {
                    rc.move(toPromote);
                    canMoveCloser = true;
                }

                if (!canMoveCloser) {
                    break;
                }
            }
        }

        if (rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            promoted = true;
        }

        if (rc.canSenseLocation(promoteLocation)) {
            RobotInfo checkingRobot = rc.senseRobotAtLocation(promoteLocation);
            if (checkingRobot.getType() == UnitType.RAT_KING) {
                promoted = true;
            }
        }
    }

    private static void toAssignedMine() throws GameActionException {
        // rc.setIndicatorDot(curLocation, 255, 255, 0);
        MapLocation closestEnemyLocation = getClosestEnemy();
        if (targetEnemyLocation == null)
            targetEnemyLocation = fetchNearbyEnemy();
        if (closestEnemyLocation == null) {
            if (targetEnemyLocation != null && rc.getLocation().distanceSquaredTo(targetEnemyLocation) <= 20
                    && rc.canTurn()) {
                Direction toEnemy = rc.getLocation().directionTo(targetEnemyLocation);
                if (toEnemy != Direction.CENTER)
                    rc.turn(toEnemy);
                closestEnemyLocation = getClosestEnemy();
            }
        }
        tryAttackingEnemies(closestEnemyLocation);
        tryComboEnemies();

        if (assignedMine == null) {
            assignedMine = getNewMine();
        }

        // rc.setIndicatorString("Current assigned mine is " + assignedMine.x + ", " +
        // assignedMine.y);

        // If there is no cheese at target anymore, reset targetCheeseLocation
        if (targetCheeseLocation != null) {
            if (rc.canSenseLocation(targetCheeseLocation)) {
                MapInfo tileInfo = rc.senseMapInfo(targetCheeseLocation);
                if (tileInfo.getCheeseAmount() == 0) {
                    targetCheeseLocation = null;
                }
            }
        }

        // Pick up cheese at targetCheeseLocation and reset to null
        if (targetCheeseLocation != null && rc.canPickUpCheese(targetCheeseLocation)) {
            rc.pickUpCheese(targetCheeseLocation);
            targetCheeseLocation = null;
        }

        // Look for cheese mines to camp at
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (tile.getCheeseAmount() > 0) {
                if (rc.canPickUpCheese(tileLocation)) {
                    rc.pickUpCheese(tileLocation);
                } else if (targetCheeseLocation == null) {
                    targetCheeseLocation = tileLocation;
                }
            }
        }

        // Try transfer first
        tryTransferCheeseToKing(kingRatLocation);

        if (targetEnemyLocation != null) {
            BugNavigator.moveTo(targetEnemyLocation, true);
            // rc.setIndicatorString(
            //         "Moving to closest enemy location at " + targetEnemyLocation.x + ", " + targetEnemyLocation.y);
        } else if (targetCheeseLocation == null) {
            BugNavigator.moveTo(assignedMine, breakDirt);
            // rc.setIndicatorString("Moving to assigned mine at " + assignedMine.x + ", " + assignedMine.y);
        } else {
            BugNavigator.moveTo(targetCheeseLocation, true);
            // rc.setIndicatorString(
            //         "Moving to target cheese location at " + targetCheeseLocation.x + ", " + targetCheeseLocation.y);
        }

        if (targetEnemyLocation != null && rc.canSenseLocation(targetEnemyLocation)) {
            targetEnemyLocation = null;
        }

        if (closestEnemyLocation != null && rc.canTurn()) {
            Direction toEnemy = rc.getLocation().directionTo(closestEnemyLocation);
            if (toEnemy != Direction.CENTER)
                rc.turn(toEnemy);
        }

        // rc.setIndicatorString("Distance to assigned mine is " +
        // rc.getLocation().distanceSquaredTo(assignedMine));
        if (rc.getLocation().distanceSquaredTo(assignedMine) <= 2) {
            assignedMine = null;
            hasGoneToFirstMine = true;
        }

        curLocation = rc.getLocation();

        // Transit TAM -> ?
        boolean hasFoundSymmetryGlobally = SymmetryManager.hasFoundSymmetryGlobally();

        MapLocation tempEnemyKingLocation = isEnemyKingNearby();
        // if (tempEnemyKingLocation != null) {
        // rc.setIndicatorString("Enemy king is at " + tempEnemyKingLocation.x + ", " +
        // tempEnemyKingLocation.y);
        // System.out.println("Enemy king is at " + tempEnemyKingLocation.x + ", " +
        // tempEnemyKingLocation.y);
        // } else {
        // rc.setIndicatorString("Enemy king is at null");
        // }

        nearbyTiles = rc.senseNearbyMapInfos();
        catLocation = isCatNearby();
        // TOO Too much cheese, ABSOLUTE RETURN
        if (rc.getRawCheese() >= hypercheese) {
            ratStatus = RatState.RETURNING;
            targetCheeseLocation = null;
            targetEnemyLocation = null;
            assignedMine = null;
            // Bait cat away from king
        } else if (catLocation != null
                && isWithinCatSight(curLocation, catLocation, rc.senseRobotAtLocation(catLocation).getDirection())
                && distanceToKing() >= baitingDistance && numberBaitingRats() == 0) {
            MapLocation baitLocation = getBaitLocation();
            if (baitLocation != null) {
                ratStatus = RatState.BAITING;
                targetCheeseLocation = null;
                targetEnemyLocation = null;
                baitCatLocation = baitLocation;
                delayTurns = 1;
            }
        }
        // Carrying too much cheese, return to king
        else if (symmetry != -1 && !hasFoundSymmetryGlobally) {
            isReturningSymmetry = true;
            ratStatus = RatState.RETURNING;
            targetCheeseLocation = null;
            targetEnemyLocation = null;
            assignedMine = null;
        } else if (tempEnemyKingLocation != null) {
            // rc.setIndicatorString("Enemy king is at " + tempEnemyKingLocation.x + ", " + tempEnemyKingLocation.y);
            // System.out.println("Enemy king is at " + tempEnemyKingLocation.x + ", " + tempEnemyKingLocation.y);
            if (!hasFoundEnemyKingGlobally()) {
                // rc.setIndicatorString("new location is " + tempEnemyKingLocation.x + ", " + tempEnemyKingLocation.y);
                ratStatus = RatState.RETURNING;
                targetCheeseLocation = null;
                targetEnemyLocation = null;
                assignedMine = null;
                enemyLocMessage = encodeLocation(tempEnemyKingLocation);
                isReturningEnemyLocation = true;
            } else {
                MapLocation oldEnemyLocation = getEnemyKingLocation();
                // rc.setIndicatorString("Old enemy king location is " + oldEnemyLocation.x + ", " + oldEnemyLocation.y
                //         + ", new location is " + tempEnemyKingLocation.x + ", " + tempEnemyKingLocation.y);
                if (tempEnemyKingLocation.distanceSquaredTo(oldEnemyLocation) > threshToReturnEnemyKingLocation) {
                    ratStatus = RatState.RETURNING;
                    targetCheeseLocation = null;
                    targetEnemyLocation = null;
                    assignedMine = null;
                    enemyLocMessage = encodeLocation(tempEnemyKingLocation);
                    isReturningEnemyLocation = true;
                }
            }
        }
    }

    private static MapLocation[] getClosestMines(int k) throws GameActionException {
        if (k <= 0)
            return new MapLocation[0];

        MapLocation[] topLoc = new MapLocation[k];
        int[] topDist = new int[k];
        int count = 0;

        // initialize distances to large value
        for (int i = 0; i < k; i++)
            topDist[i] = 1000000;
        curLocation = rc.getLocation();

        for (int i = 0; i < numMines; i++) {
            MapLocation mine = getMineLocation(i);
            int d = mine.distanceSquaredTo(kingRatLocation);
            int d1 = mine.distanceSquaredTo(curLocation);

            // skip too-close mines if that's desired
            if (d <= 8 || d1 <= 4)
                continue;

            if (count < k) {
                topLoc[count] = mine;
                topDist[count] = d;
                count++;
            } else {
                // find index of the farthest (worst) current top element
                int idxMax = 0;
                int maxD = topDist[0];
                for (int j = 1; j < k; j++) {
                    if (topDist[j] > maxD) {
                        maxD = topDist[j];
                        idxMax = j;
                    }
                }
                // if this mine is closer than the current farthest, replace it
                if (d < maxD) {
                    topLoc[idxMax] = mine;
                    topDist[idxMax] = d;
                }
            }
        }

        // shrink result if there were fewer than k valid mines
        if (count == k)
            return topLoc;
        MapLocation[] res = new MapLocation[count];
        for (int i = 0; i < count; i++)
            res[i] = topLoc[i];
        return res;
    }

    private static MapLocation getNewMine() throws GameActionException {
        // int remBytecodes1 = Clock.getBytecodesLeft();
        MapLocation[] candidates = getClosestMines(numMinesToConsider);
        int n = candidates.length;
        if (n == 0) {
            // int remBytecodes2 = Clock.getBytecodesLeft();
            // System.out.println("Bytecodes used in getNewMine: " + (remBytecodes1 - remBytecodes2));
            return new MapLocation(mapWidth / 2, mapHeight / 2);
        }
        int sumWeights = 0;
        for (int i = 0; i < n; i++) {
            int dist = candidates[i].distanceSquaredTo(kingRatLocation);
            // closer mines are more likely to be chosen
            if (dist <= smallDistanceThresh * smallDistanceThresh) {
                sumWeights += smallDistanceWeight;
            } else if (dist <= midDistanceThresh * midDistanceThresh) {
                sumWeights += midDistanceWeight;
            } else {
                sumWeights += largeDistanceWeight;
            }
        }
        rng = new Random(curLocation.x * 36 + curLocation.y * 63 + curRound * 67 + myId * 69);
        int value = rng.nextInt(sumWeights);
        int sum = 0;
        for (int i = 0; i < n; i++) {
            int dist = candidates[i].distanceSquaredTo(kingRatLocation);
            int weight;
            if (dist <= smallDistanceThresh * smallDistanceThresh) {
                weight = smallDistanceWeight;
            } else if (dist <= midDistanceThresh * midDistanceThresh) {
                weight = midDistanceWeight;
            } else {
                weight = largeDistanceWeight;
            }
            sum += weight;
            if (value < sum) {
                // int remBytecodes2 = Clock.getBytecodesLeft();
                // System.out.println("Bytecodes used in getNewMine: " + (remBytecodes1 - remBytecodes2));
                return candidates[i];
            }
        }
        // int remBytecodes2 = Clock.getBytecodesLeft();
        // System.out.println("Bytecodes used in getNewMine: " + (remBytecodes1 - remBytecodes2));
        return candidates[n - 1];
    }

    /// Find all cheese mines within vision and update our remembered list
    private static void updateCheeseMinesList() {
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();

            if (!tile.hasCheeseMine())
                continue;
            recordCheeseMine(tileLocation);
        }
    }

    private static void tryToPickUpCheese() throws GameActionException {
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (tile.getCheeseAmount() > 0 && rc.canPickUpCheese(tileLocation) && rc.getRawCheese() <= hypercheese) {
                rc.pickUpCheese(tileLocation);
            }
        }
    }

    private static boolean tryTransferCheeseToKing(MapLocation kingLoc) throws GameActionException {
        // We can transfer to any of the 3x3 square of the king
        if (rc.canTransferCheese(kingLoc, rawCheeseAmount)) {
            rc.transferCheese(kingLoc, rawCheeseAmount);
            return true;
        }
        for (Direction dir : directions) {
            MapLocation otherKingLoc = kingLoc.add(dir);
            if (rc.canTransferCheese(otherKingLoc, rawCheeseAmount)) {
                rc.transferCheese(otherKingLoc, rawCheeseAmount);
                return true;
            }
        }
        return false;
    }
}