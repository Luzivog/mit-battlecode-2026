package opp_gravy;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.UnitType;

public class KingRat extends RobotPlayer {

    public static int catTrapsInBeginning = 10;
    public static int lateGameStartTurn = 1900;
    public static int lateGameReserve = 400; // Late game, reserve this much cheese to allow king rats to live
    // For cheese mines (cm_), thresholds to consider this a viable cheese mine to
    // camp at. Init at RobotPlayer
    public static int cm_distToCenterLowerbound; // Less than this, don't consider
    public static int cm_distToKingUpperbound; // More than this, don't consider
    public static int cm_distToCenterUpperbound;
    // If we meet this, wait this many turns long before going to CAMPING again
    public static int catCooldown = 30;
    public static int enemyRatCooldown = 5;
    public static int maxGlobalMines = 45;

    static int buildRatThreshold = 1100; // Amount of cheese that building a rat is acceptable
    static int portionOnAttack = 1000; // Will spend (Global cheese) / portionOnAttack cheese for an attack
    static int catTrapThreshold = 400; // If sense cat and cheese >= this, spam cat traps
    static int maxTrapDistanceSqr = 4; // Will only place trap at place that's within this distance from cat
    static int lastRoundSinceCatTrap = -100; // Last round we placed a trap
    static int lastRoundSinceSpawn = -100; // Last round we spawned a baby rat
    static int ratTrapThreshold = 600; // Minimum cheese to build rat trap
    static int catTrapDelay = 3; // Minimum rounds between cat traps
    static int ratCostCap = 30; // Maximum rat cost (early game)
    static int buildDefendingRatThresh = 200;
    static int lowRatNumber = 20;
    static int midRatNumber = 60;
    static int catTrapsPlaced = 0;
    static Direction curCatTrapDirection = Direction.NORTH;
    static int sumContributions;

    private static enum KingState {
        RUN_FROM_RAT, // Save ourself from enemy rat
        RUN_FROM_CAT, // Save ourself from a cat
        NORMAL,
        CAMPING
    }

    private static int waitTimer = 20;
    private static KingState state = KingState.NORMAL;

    public static Random rng = new Random(6767);
    private static MapLocation previousSpawnLocation = null;

    // Positional spawn rates to avoid spawning too many rats on one side
    static int aboveSpawnRate = 25, belowSpawnRate = 25, leftSpawnRate = 25, rightSpawnRate = 25;

    static int lowHealth = 150, mediumHealth = 300;

    private static int getSpawnPosition() throws GameActionException {
        int pick = rng.nextInt(sumContributions);
        // rc.setIndicatorString("Current pick: " + pick);
        if (pick < aboveSpawnRate) {
            return 0; // Above
        } else if (pick < aboveSpawnRate + belowSpawnRate) {
            return 1; // Below
        } else if (pick < aboveSpawnRate + belowSpawnRate + leftSpawnRate) {
            return 2; // Left
        } else {
            return 3; // Right
        }
    }

    private static boolean checkPosition(MapLocation loc, int spawnPosition) throws GameActionException {
        switch (spawnPosition) {
            case 0: // Above
                return loc.y > curLocation.y;
            case 1: // Below
                return loc.y < curLocation.y;
            case 2: // Left
                return loc.x < curLocation.x;
            case 3: // Right
                return loc.x > curLocation.x;
            default:
                return false;
        }
    }

    private static void updateThresholds() throws GameActionException {
        rng = new Random(curLocation.x * 36 + curLocation.y * 63 + turnCount * 67);

        int curX = curLocation.x;
        int curY = curLocation.y;

        aboveSpawnRate = (mapHeight - curY + 1) >= 5 ? (mapHeight - curY + 1) : 0;
        belowSpawnRate = (curY >= 5 ? curY : 0);
        leftSpawnRate = (curX >= 5 ? curX : 0);
        rightSpawnRate = (mapWidth - curX + 1) >= 5 ? (mapWidth - curX + 1) : 0;

        aboveSpawnRate *= aboveSpawnRate;
        belowSpawnRate *= belowSpawnRate;
        leftSpawnRate *= leftSpawnRate;
        rightSpawnRate *= rightSpawnRate;

        sumContributions = aboveSpawnRate + belowSpawnRate + leftSpawnRate + rightSpawnRate;

        if (curRound > earlyGameThresh && curRound < midGameThresh) {
            ratCostCap = 80;
        } else if (curRound >= midGameThresh) {
            ratCostCap = 1000;
        }

        if (currentRatCost <= lowRatNumber) {
            buildRatThreshold = 0;
        } else if (currentRatCost <= 30) {
            buildRatThreshold = 500;
        } else if (currentRatCost <= midRatNumber) {
            buildRatThreshold = 1000;
        } else {
            buildRatThreshold = 1500;
        }

        int health = rc.getHealth();
        if (health <= lowHealth) {
            ratTrapThreshold = 300;
            catTrapThreshold = 300;
            catTrapDelay = 0;
        } else if (health <= mediumHealth) {
            ratTrapThreshold = 500;
            catTrapThreshold = 350;
            catTrapDelay = 1;
        } else {
            ratTrapThreshold = 700;
            catTrapThreshold = 400;
            catTrapDelay = 3;
        }
    }

    private static void handleSqueaks() throws GameActionException {
        for (Message message : nearbySqueaks) {
            DecodedMessage decoded = readSqueak(message.getBytes());
            int value = decoded.messageContent;
            switch (decoded.type) {
                case MessageType.SYMMETRY -> updateSymmetryBroken(value);
                case MessageType.ENEMY_KING_RAT_LOCATION -> updateEnemyKingLocation(value);
                case MessageType.CHEESE_MINE -> updateCheeseMinesList(value);
            }
        }
    }

    private static void updateSymmetryBroken(int value) throws GameActionException {
        // System.out.println(value);
        if ((readGlobalArray(63 + 0, 63 + 0) == 0) && (value & 1) != 0) {
            writeGlobalArray(63 + 0, 63 + 0, 1);
            // System.out.println("King received broken symmetry 0");
        }
        if ((readGlobalArray(63 + 1, 63 + 1) == 0) && (value & 2) != 0) {
            writeGlobalArray(63 + 1, 63 + 1, 1);
            // System.out.println("King received broken symmetry 1");
        }
        if ((readGlobalArray(63 + 2, 63 + 2) == 0) && (value & 4) != 0) {
            writeGlobalArray(63 + 2, 63 + 2, 1);
            // System.out.println("King received broken symmetry 2");
        }
    }

    private static void writeNewMine(MapLocation mineLocation) throws GameActionException {
        if (numMines >= maxGlobalMines)
            return;
        // System.out.println("Writing new mine location " + mineLocation.x + ", " + mineLocation.y);
        writeGlobalArray(85 + 12 * numMines, 85 + 12 * (numMines + 1) - 1, encodeLocation(mineLocation));
        numMines++;
        writeGlobalArray(79, 84, numMines);
    }

    private static void updateEnemyKingLocation(int value) throws GameActionException {
        // System.out.println(value);
        MapLocation enemyKingLocation = decodeLocation(value);
        writeGlobalArray(67, 78, encodeLocation(enemyKingLocation));
        if (!hasFoundEnemyKingGlobally())
            writeGlobalArray(66, 66, 1);
    }

    private static void updateNewSymmetricMines() throws GameActionException {
        for (int i = 0; i < numMines; i++) {
            MapLocation mineLocation = getMineLocation(i);
            MapLocation symMineLocation = SymmetryManager.getSymmetric(mineLocation);
            // System.out.println("Symmetric mine location is at " + symMineLocation.x + ",
            // " + symMineLocation.y);
            boolean isNewMine = recordCheeseMine(symMineLocation);
            if (isNewMine) {
                writeNewMine(symMineLocation);
            }
        }
    }

    private static void updateCheeseMinesList(int value) throws GameActionException {
        MapLocation mineLocation = decodeLocation(value);
        // System.out.println("Heard squeak for location " + mineLocation.x + ", " +
        // mineLocation.y);
        boolean isNewMine = recordCheeseMine(mineLocation);
        if (isNewMine) {
            writeNewMine(mineLocation);
            if (symmetry != -1) {
                MapLocation symLocation = SymmetryManager.getSymmetric(mineLocation);
                isNewMine = recordCheeseMine(symLocation);
                if (isNewMine)
                    writeNewMine(symLocation);
            }
        }
    }

    public static void writeKingLocation() throws GameActionException {
        MapLocation kingLoc = rc.getLocation();
        int idx = readGlobalArray(0, 2);
        writeGlobalArray(3 + idx * 12, 14 + idx * 12, encodeLocation(kingLoc));
        writeGlobalArray(0, 2, (idx + 1) % 2);

        kingRatLocation = kingLoc;
    }

    // Writes into array from bit start to bit end (0-indexed)
    public static void writeGlobalArray(int start, int end, int val) throws GameActionException {
        int t = -1;
        for (int i = end; i >= start; i--) {
            if (t == -1) {
                t = rc.readSharedArray(i / 10);
            }
            if (val % 2 == 0) {
                t = (t & (1023 - (1 << (9 - i % 10)))); // Also trust that this works
            } else {
                t = (t | (1 << (9 - i % 10)));
            }
            if (i % 10 == 0 || i == start) {
                rc.writeSharedArray(i / 10, t);
                t = -1;
            }
            val /= 2;
        }
    }

    public static void blockCatWithDirt(MapLocation catLocation) throws GameActionException {
        if (catLocation == null)
            return;

        MapLocation optimalLocation = null;
        int bestDist = Integer.MAX_VALUE;
        for (MapInfo tile : nearbyTiles) {
            int dist = curLocation.distanceSquaredTo(tile.getMapLocation());
            if (dist < 4 || dist > 8)
                continue;
            if (!rc.canPlaceDirt(tile.getMapLocation()))
                continue;
            dist = curLocation.distanceSquaredTo(tile.getMapLocation())
                    + catLocation.distanceSquaredTo(tile.getMapLocation());
            if (dist < bestDist) {
                bestDist = dist;
                optimalLocation = tile.getMapLocation();
            }
        }
        if (optimalLocation == null)
            return;

        int cnt = 0;
        for (MapInfo tile : nearbyTiles) {
            int dist = curLocation.distanceSquaredTo(tile.getMapLocation());
            if (dist < 4 || dist > 8)
                continue;
            dist = curLocation.distanceSquaredTo(tile.getMapLocation())
                    + catLocation.distanceSquaredTo(tile.getMapLocation());
            if (dist < bestDist)
                cnt++;
        }
        if (cnt >= 5)
            return;
        // rc.setIndicatorDot(optimalLocation, 255, 0, 0);
        rc.placeDirt(optimalLocation);
    }

    public static void run() throws GameActionException {
        // update symmetry
        if (symmetry == -1) {
            SymmetryManager.fetchSymmetry();
            if (symmetry != -1) {
                updateNewSymmetricMines();
            }
        }

        updateThresholds();
        handleSqueaks();

        // Late game spawning
        if (rc.getRoundNum() >= lateGameStartTurn) {
            for (MapInfo tile : nearbyTiles) {
                MapLocation tileLocation = tile.getMapLocation();
                if (rc.canBuildRat(tileLocation) && globalCheeseAmount - rc.getCurrentRatCost() >= lateGameReserve) {
                    rc.buildRat(tileLocation);
                }
            }
        }

        switch (state) {
            case KingState.NORMAL -> Normal();
            case KingState.RUN_FROM_CAT -> RunFromCat();
            case KingState.RUN_FROM_RAT -> RunFromRat();
            case KingState.CAMPING -> Camping();
        }
        writeKingLocation();

        if (symmetry == -1) {
            SymmetryManager.checkSym();
            int value = SymmetryManager.writeBrokenSymmetry();
            if (value > 0)
                updateSymmetryBroken(value);
        }
        String tmp = "Symmetry is " + symmetry + " Num mines is " + numMines;
        if (hasFoundEnemyKingGlobally()) {
            tmp = tmp + " Enemy king at " + getEnemyKingLocation().x + ", " + getEnemyKingLocation().y;
        } else {
            tmp = tmp + " Enemy king unknown";
        }
        // rc.setIndicatorString(tmp);
    }

    private static void handleMovement(MapLocation catLocation, MapLocation enemyRatLocation)
            throws GameActionException {
        if (catLocation != null) {
            // Run away from cat if there is one nearby
            if (rc.canAttack(catLocation)) {
                rc.attack(catLocation);
            }
            Direction oppToCat = rc.getLocation().directionTo(catLocation).opposite();
            if (rc.canMove(oppToCat)) {
                move(oppToCat, true);
            } else if (rc.canMove(oppToCat.rotateLeft())) {
                move(oppToCat.rotateLeft(), true);
            } else if (rc.canMove(oppToCat.rotateRight())) {
                move(oppToCat.rotateRight(), true);
            } else if (rc.canMove(oppToCat.rotateLeft().rotateLeft())) {
                move(oppToCat.rotateLeft().rotateLeft(), true);
            } else if (rc.canMove(oppToCat.rotateRight().rotateRight())) {
                move(oppToCat.rotateRight().rotateRight(), true);
            }
        } else if (enemyRatLocation != null) {
            // Move towards enemy rat if there is one nearby
            Direction toEnemyRat = rc.getLocation().directionTo(enemyRatLocation).opposite();
            if (rc.canMove(toEnemyRat)) {
                move(toEnemyRat, true);
            } else if (rc.canMove(toEnemyRat.rotateLeft())) {
                move(toEnemyRat.rotateLeft(), true);
            } else if (rc.canMove(toEnemyRat.rotateRight())) {
                move(toEnemyRat.rotateRight(), true);
            } else if (rc.canMove(toEnemyRat.rotateLeft().rotateLeft())) {
                move(toEnemyRat.rotateLeft().rotateLeft(), true);
            } else if (rc.canMove(toEnemyRat.rotateRight().rotateRight())) {
                move(toEnemyRat.rotateRight().rotateRight(), true);
            }
        } else {
            // Movement.doRandomMove();
        }
    }

    private static void handleEnemy(MapLocation enemyRatLocation, UnitType enemyType) throws GameActionException {
        if (enemyRatLocation == null)
            return;

        if (enemyType.isBabyRatType()) {
            if (globalCheeseAmount - currentRatCost >= buildDefendingRatThresh) {
                // rc.setIndicatorString("Enemy baby rat at " + enemyRatLocation.x + ", " +
                // enemyRatLocation.y);
                MapLocation spawnLocation = null;
                for (Direction dir : directions) {
                    MapLocation tileLocation = enemyRatLocation.add(dir);
                    if (rc.canBuildRat(tileLocation) && !isWithinRatSight(tileLocation, enemyRatLocation,
                            rc.senseRobotAtLocation(enemyRatLocation).getDirection())) {
                        spawnLocation = tileLocation;
                        if (tileLocation.distanceSquaredTo(enemyRatLocation) == 2) {
                            break;
                        }
                    }
                }
                if (spawnLocation != null) {
                    // rc.setIndicatorString("Spawn at " + spawnLocation.x + ", " +
                    // spawnLocation.y);
                    rc.turn(spawnLocation.directionTo(enemyRatLocation));
                    rc.buildRat(spawnLocation);
                }
            }
        }

        // Try placing trap
        boolean placedTrap = false;
        if (globalCheeseAmount >= ratTrapThreshold) {
            // Try to place trap
            if (enemyType.isRatKingType()) {
                MapLocation trapLocation = nearestRatTrapTo(enemyRatLocation, 8);
                if (trapLocation != null) {
                    rc.placeRatTrap(trapLocation);
                    placedTrap = true;
                }
            } else {
                int minDist = 1000000;
                MapLocation trapLocation = null;
                // Find the trap placement closest to the king (more optimal i guess)
                for (Direction dir : directions) {
                    MapLocation tileLocation = enemyRatLocation.add(dir);
                    int dist = tileLocation.distanceSquaredTo(curLocation);
                    if (rc.canPlaceRatTrap(tileLocation) && dist < minDist) {
                        minDist = dist;
                        trapLocation = tileLocation;
                    }
                }
                if (trapLocation != null) {
                    rc.placeRatTrap(trapLocation);
                    placedTrap = true;
                }
            }
        }

        // Placed trap, can't do anything else
        if (placedTrap)
            return;
        // Try to attack instead
        if (rc.canAttack(enemyRatLocation))
            rc.attack(enemyRatLocation);
    }

    private static boolean foundNearbyCat() throws GameActionException {
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();

            if (!rc.canSenseRobotAtLocation(tileLocation))
                continue;

            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            UnitType tileType = tileRobot.getType();

            // Anticipate nearby cat
            if (tileType.isCatType()) {
                return true;
            }
        }
        return false;
    }

    private static boolean foundNearbyEnemyRat() throws GameActionException {
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();

            if (!rc.canSenseRobotAtLocation(tileLocation))
                continue;

            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            Team tileTeam = tileRobot.getTeam();

            if (tileTeam == myTeam.opponent()) {
                return true;
            }
        }
        return false;
    }

    private static int evaluateMine(MapLocation mineLocation) throws GameActionException {
        if (cheeseMineLocations.size() >= 10) {
            MapLocation centerLocation = new MapLocation(mapWidth / 2, mapHeight / 2);
            return centerLocation.distanceSquaredTo(mineLocation);
        }
        int sumDist = 0;
        for (MapLocation otherMineLocation : cheeseMineLocations)
            sumDist += otherMineLocation.distanceSquaredTo(mineLocation);
        return sumDist;
    }

    private static MapLocation findValidCampMine() throws GameActionException {
        if (cheeseMineLocations.isEmpty())
            return null;
        MapLocation destination = null;
        MapLocation centerLocation = new MapLocation(mapWidth / 2, mapHeight / 2);

        int minDist = 1_000_000_000;
        for (MapLocation mineLocation : cheeseMineLocations) {
            int distToKing = mineLocation.distanceSquaredTo(rc.getLocation());
            int distToCenter = mineLocation.distanceSquaredTo(centerLocation);

            if (distToCenter < cm_distToCenterLowerbound)
                continue;
            if (distToKing > cm_distToKingUpperbound)
                continue;

            // Test how good mine location is
            int sumDist = evaluateMine(mineLocation);

            if (sumDist < minDist) {
                minDist = sumDist;
                destination = mineLocation;
            }
        }
        if (destination != null)
            return destination;

        // If not, tend to the center of mines
        int sumX = 0;
        int sumY = 0;
        int considered = 0;
        for (MapLocation mineLocation : cheeseMineLocations) {
            int distToKing = mineLocation.distanceSquaredTo(rc.getLocation());
            // if (distToKing > cm_distToKingUpperbound) continue;

            sumX += mineLocation.x;
            sumY += mineLocation.y;
            considered++;
        }

        if (considered == 0)
            return null;
        // Take the average cuz that's the center of mass
        sumX /= considered;
        sumY /= considered;
        sumX = Math.clamp(sumX, 0, mapWidth - 1);
        sumY = Math.clamp(sumY, 0, mapHeight - 1);
        return new MapLocation(sumX, sumY);
    }

    private static void Normal() throws GameActionException {
        waitTimer--;
        boolean foundEnemyRat = foundNearbyEnemyRat();
        boolean foundCat = foundNearbyCat();

        tryToSpawnRat();
        tryToPickUpCheese();

        // Transit NORMAL -> ?

        if (foundCat)
            state = KingState.RUN_FROM_CAT;
        else if (foundEnemyRat)
            state = KingState.RUN_FROM_RAT;
        else if (waitTimer < 0)
            state = KingState.CAMPING;
        if (state != KingState.NORMAL)
            return;

        // Movement here. Currently, we don't move if not in danger
        handleMovement(null, null);
    }

    private static float lerp(float a, float b, float t) {
        return (1f-t) * a + t * b;
    }

    /// When camping, choose next direction to gain best profits
    private static Direction chooseMovementDirection(MapLocation mineLocation) throws GameActionException {
        MapLocation centerLocation = new MapLocation(mapWidth / 2, mapHeight / 2);

        double bestEval = 0f; // Ensure we always move
        Direction bestDirection = Direction.CENTER;

        float lowHealthScaleFactor = 2.5f;
        int toCenterLowerbound = (int)(cm_distToCenterLowerbound * lerp(lowHealthScaleFactor, 1f, (float)rc.getHealth() / 600f));

        for (Direction dir : directions) {
            if (!rc.canMove(dir))
                continue;
            MapLocation futureLocation = rc.getLocation().add(dir);
            int distToCenter = rc.getLocation().distanceSquaredTo(centerLocation);
            int futureDistToCenter = futureLocation.distanceSquaredTo(centerLocation);
            if (distToCenter >= toCenterLowerbound) {
                if (futureDistToCenter < toCenterLowerbound)
                    continue; // Don't step too close into center
            }
            else {
                // Try to step further away from center
                if (futureDistToCenter < distToCenter) continue;
            }

            // Prioritize positions close to the center with a weight
            double centerWeight = .5f;
            double eval = evaluatePosition(futureLocation)
                    + centerWeight * interpolate(centerLocation.distanceSquaredTo(futureLocation));
            if (eval > bestEval) {
                bestEval = eval;
                bestDirection = dir;
            }
        }
        return bestDirection;
    }

    private static double interpolate(double x) throws GameActionException {
        double minValue = 1;
        double maxValue = 20;
        double rateOfDecrease = .05f;

        // Graph this for better understanding
        return minValue + (maxValue - minValue) / (1f + rateOfDecrease / (maxValue - minValue) * x);
    }

    private static double evaluatePosition(MapLocation position) throws GameActionException {
        double evaluation = 0f;
        for (MapLocation mineLocation : cheeseMineLocations) {
            evaluation += interpolate(mineLocation.distanceSquaredTo(position));
        }
        return evaluation;
    }

    private static void Camping() throws GameActionException {
        // if (!cheeseMineLocations.isEmpty())
        // rc.setIndicatorString(cheeseMineLocations.get(0).x + ", " +
        // cheeseMineLocations.get(0).y);
        boolean foundEnemyRat = foundNearbyEnemyRat();
        boolean foundCat = foundNearbyCat();

        tryToSpawnRat();
        tryToPickUpCheese();

        MapLocation campLocation = findValidCampMine();
        Direction dir = chooseMovementDirection(campLocation);

        if (rc.canMove(dir)) {
            // If can move and after moving it's not too close to center, do it.
            rc.move(dir);
        }

        // Transit CAMPING -> ?
        if (foundCat)
            state = KingState.RUN_FROM_CAT;
        else if (foundEnemyRat)
            state = KingState.RUN_FROM_RAT;
    }

    private static void RunFromCat() throws GameActionException {
        waitTimer = Math.max(waitTimer, catCooldown);
        boolean needCatTrap = false;
        MapLocation catLocation = null;

        // TODO: Spawn baby rat opposite to cat and bait
        tryToSpawnRat();
        tryToPickUpCheese();

        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (!rc.canSenseRobotAtLocation(tileLocation)) {
                continue;
            }

            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            UnitType tileType = tileRobot.getType();

            // Anticipate nearby cat
            if (tileType.isCatType()) {
                if (rc.getGlobalCheese() >= catTrapThreshold)
                    needCatTrap = true;
                catLocation = tileLocation;
                break;
            }
        }

        // Transit RUN_FROM_CAT -> ?
        if (catLocation == null)
            state = KingState.NORMAL;
        if (state != KingState.RUN_FROM_CAT)
            return;

        if (coopStatus && needCatTrap && (curRound - lastRoundSinceCatTrap) >= catTrapDelay) {
            // Lay trap fortress to anticipate nearby cat
            MapLocation trapLocation = nearestCatTrapTo(catLocation);
            if (trapLocation != null && rc.canPlaceCatTrap(trapLocation)) {
                rc.placeCatTrap(trapLocation);
                lastRoundSinceCatTrap = curRound;
            }
        }

        // Block cat, and try to move away
        blockCatWithDirt(catLocation);

        handleMovement(catLocation, null);
    }

    private static void RunFromRat() throws GameActionException {
        waitTimer = Math.max(waitTimer, enemyRatCooldown);
        boolean foundCat = foundNearbyCat();
        MapLocation enemyRatLocation = null;
        UnitType enemyType = null;

        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (!rc.canSenseRobotAtLocation(tileLocation)) {
                continue;
            }

            RobotInfo tileRobot = rc.senseRobotAtLocation(tileLocation);
            UnitType tileType = tileRobot.getType();
            Team tileTeam = tileRobot.getTeam();

            // Find enemy rat location
            if (enemyRatLocation == null && tileType.isBabyRatType() && tileTeam == myTeam.opponent()) {
                enemyRatLocation = tileLocation;
                enemyType = tileType;
            }

            if (tileType.isRatKingType() && tileTeam == myTeam.opponent()) {
                enemyRatLocation = tileLocation;
                enemyType = tileType;
            }
        }

        handleEnemy(enemyRatLocation, enemyType);

        tryToSpawnRat();
        tryToPickUpCheese();

        // Transit RUN_FROM_RAT -> ?
        if (foundCat)
            state = KingState.RUN_FROM_CAT;
        else if (enemyRatLocation == null)
            state = KingState.NORMAL;

        if (state != KingState.RUN_FROM_RAT)
            return;

        // Movement here
        handleMovement(null, enemyRatLocation);
    }

    // Returns nearest square to cat we can build cat trap on. Return null if
    // distance >= maxTrapDist
    private static MapLocation nearestCatTrapTo(MapLocation referenceLocation) {
        int minDist = 1000000000;
        MapLocation closestTrapPosition = null;
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            int tileDist = tileLocation.distanceSquaredTo(referenceLocation);
            if (minDist > tileDist && rc.canPlaceCatTrap(tileLocation)) {
                minDist = tileDist;
                closestTrapPosition = tileLocation;
            }
        }

        if (minDist > maxTrapDistanceSqr)
            closestTrapPosition = null;
        return closestTrapPosition;
    }

    private static void tryToSpawnRat() throws GameActionException {
        if (!rc.isActionReady()) return;
        if (previousSpawnLocation != null) {
            if (rc.senseMapInfo(previousSpawnLocation).isDirt() && rc.canRemoveDirt(previousSpawnLocation)) {
                rc.removeDirt(previousSpawnLocation);
            }
            if (rc.canBuildRat(previousSpawnLocation)) {
                rc.buildRat(previousSpawnLocation);
                lastRoundSinceSpawn = curRound;
                previousSpawnLocation = null;
            }
        } else {
            MapLocation spawnLocationHasDirt = null;

            // Build rat if we're rich
            // Slightly controlled reproduction because we are not animals
            if (globalCheeseAmount >= buildRatThreshold
                && rc.getCurrentRatCost() <= ratCostCap){
                
                MapLocation bestSpawn = null;
                int bestEval = 0;
                for (MapInfo tile : nearbyTiles) {
                    MapLocation tileLocation = tile.getMapLocation();
                    int dist = tileLocation.distanceSquaredTo(curLocation);
                    if (dist < 4 || dist > 8) continue;

                    int eval = evalSpawnLocation(tileLocation);
                    if (eval > bestEval){
                        bestEval = eval;
                        bestSpawn = tileLocation;
                    }
                }
                if (bestSpawn == null)
                    return;
                if (rc.canBuildRat(bestSpawn)){
                    rc.buildRat(bestSpawn);
                    lastRoundSinceSpawn = curRound;
                    return;
                }
                else if (rc.senseMapInfo(bestSpawn).isDirt()){
                    spawnLocationHasDirt = bestSpawn;
                }
            }
            if (spawnLocationHasDirt != null) {
                previousSpawnLocation = spawnLocationHasDirt;
                if (rc.canRemoveDirt(previousSpawnLocation)) {
                    rc.removeDirt(previousSpawnLocation);
                }
            }
        }
    }

    private static int evalSpawnLocation(MapLocation spawnLocation) throws GameActionException{
        if (rc.canBuildRat(spawnLocation)){
            return 10000 + mapWidth + mapHeight - spawnLocation.distanceSquaredTo(new MapLocation(midX1, midY1));
        }
        if (rc.senseMapInfo(spawnLocation).isDirt()){
            return 10000 - spawnLocation.distanceSquaredTo(new MapLocation(midX1, midY1));
        }
        return 0;
    }

    private static void tryToPickUpCheese() throws GameActionException {
        for (MapInfo tile : nearbyTiles) {
            MapLocation tileLocation = tile.getMapLocation();
            if (tile.getCheeseAmount() > 0 && rc.canPickUpCheese(tileLocation)) {
                rc.pickUpCheese(tileLocation);
            }
        }
    }

    private static boolean checkSpotForRatSpawn(MapLocation tileLocation, int spawnPosition)
            throws GameActionException {
        return globalCheeseAmount >= buildRatThreshold
                && checkPosition(tileLocation, spawnPosition) && rc.canBuildRat(tileLocation)
                && rc.getCurrentRatCost() <= ratCostCap;
    }
}