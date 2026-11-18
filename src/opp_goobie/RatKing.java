package opp_goobie;

import battlecode.common.*;
import opp_goobie.Fast.FastIterableLocSet;

public class RatKing extends Robot {
    private static final int[] otherRatKingPreviousAlive = new int[GameConstants.MAX_NUMBER_OF_RAT_KINGS];
    private static final int[] otherRatKingTurnsWithoutFlip = new int[GameConstants.MAX_NUMBER_OF_RAT_KINGS];
    private int flippedAliveBitThisTurn = -1;
    boolean hasMicro = false;
    static int spawnedRats = 0;
    static int cheeseSpentSpawning = 0;
    static boolean spawnedRatKing = false;
    private static MapLocation target = null;
    private static MapLocation lastSeenRallyPoint = null;
    private static int rallyPointClearedTurn = -100;
    private static int rallyFirstSeenTurn = -1;
    private static MapLocation blacklistedRallyPoint = null;
    private static int blacklistExpiresTurn = -1;
    private static MapLocation centerMineTarget = null;
    private static int centerMineTargetSetTurn = -1;

    public RatKing(RobotController rc) throws GameActionException {
        super(rc);
        RatKingMicroManager.init();
        SpawnManager.doDirs();
    }

    @Override
    void beforeTurn() throws GameActionException {
        if ((turnCount >= 4) || !(CommsArray.ratKingIndex == 0 && SymmetryManager.isSymmetryDetermined()
                && CommsArray.isSymmetryGloballyKnown()
                && !CommsArray.isCheeseMinesRewritten())) {
            MapData.updateMapInfos();
        }

        if ((turnCount == 0) && !SymmetryManager.isSymmetryDetermined()) {
            SymmetryManager.setBase(Robot.centerLoc);
        }

        MovementManager.update();
        if (turnCount >= 2) {
            CommsArray.syncSymmetryFromGlobalArray();
            CommsSqueak.readSqueaks();
            CommsArray.writeSymmetryToSharedArrayIfKnown();
        }

        RatKingMicroManager.computeMicroArray();
        hasMicro = RatKingMicroManager.doMicro();
        Debug.addIndicatorString("HasMicro: " + hasMicro);

        int sharedArrayCount = CommsArray.getLocationsInSharedArraySize();
        boolean rewritten = CommsArray.isCheeseMinesRewritten();
        String cheeseMineInfo = "CM: " + sharedArrayCount;
        if (rewritten) {
            cheeseMineInfo += " (" + (sharedArrayCount * 2) + ")";
        }
        Debug.addIndicatorString(cheeseMineInfo);
    }

    @Override
    public void turn() throws GameActionException {
        MapLocation m = getThreat();
        if (m != null)
            SpawnManager.spawnDefense(m);

        tryBuild();
        trySpawn();
        AttackManager.attack();
        CheeseManager.tryGrabCheese();
        target = getTarget();
        Debug.addIndicatorString("Target: " + target);
        tryDig();
        move();
        tryDig();
    }

    @Override
    void afterTurn() throws GameActionException {
        flippedAliveBitThisTurn = CommsArray.writeRatKingLocHealthAndFlipAlive(flippedAliveBitThisTurn, rc.getHealth());

        trySetFormationRallyPoint();

        int spareBytecode = Clock.getBytecodesLeft() - 500;
        if (spareBytecode > 2000) {
            if (CommsArray.isCheeseMinesInitialized()) {
                if (CommsArray.ratKingIndex == 0 && SymmetryManager.isSymmetryDetermined()
                        && CommsArray.isSymmetryGloballyKnown()
                        && !CommsArray.isCheeseMinesRewritten()) {
                    Debug.println("RAT KING: Attempting to rewrite cheese mines. Spare bytecode: " + spareBytecode);
                    CommsArray.rewriteCheeseMines(spareBytecode);
                }
            } else {
                CommsArray.initCheeseMines(spareBytecode);
            }
        }

        Debug.addIndicatorString("[Rats S: " + spawnedRats + "|" + cheeseSpentSpawning + "]");

        if (turnCount > 1) {
            checkOtherRatKingsForDeath();
        }

        for (int i = 0; i < MapData.knownCheeseMines.size; i++) {
            MapLocation mine = MapData.knownCheeseMines.locs[i];
            if (mine == null)
                continue;

            if (SymmetryManager.isSymmetryDetermined()) {
                if (CommsArray.isOnOurSide(mine)) {
                    Debug.setIndicatorDot(mine, 0, 255, 0);
                } else {
                    Debug.setIndicatorDot(mine, 255, 0, 0);
                }
            } else {
                Debug.setIndicatorDot(mine, 0, 0, 255);
            }
        }

        // SymmetryManager.checkSym();
    }

    void move() throws GameActionException {
        if (hasMicro || !rc.isMovementReady())
            return;
        BugNav.moveTo(target);
    }

    MapLocation getTarget() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        MapLocation center = Robot.centerLoc;
        MapLocation target = null;

        // Default: closest non-blacklisted mine (what we're "orbiting")
        MapLocation currentOrbitMine = MapData.getClosestNonBlacklistedCheeseMine(myLoc);

        // Single king: prioritize mine closest to center
        if (CommsArray.livingRatKingCount == 1) {
            // Use friendly mines if symmetry known, otherwise all known mines
            FastIterableLocSet mineList = SymmetryManager.isSymmetryDetermined()
                    ? MapData.getFriendlySidedCheeseMines()
                    : MapData.getKnownCheeseMines();

            MapLocation centerMine = MapData.getCheeseMineClosestToCenter(mineList);

            if (centerMine != null && currentOrbitMine != null) {
                int centerMineDist = Util.chebyshevDistanceTo(center, centerMine);
                int currentOrbitDist = Util.chebyshevDistanceTo(center, currentOrbitMine);

                // Only switch if center mine is at least 1 tile closer to center
                if (centerMineDist + 1 <= currentOrbitDist) {
                    // Track when we started targeting this mine
                    if (centerMineTarget == null || !centerMineTarget.equals(centerMine)) {
                        centerMineTarget = centerMine;
                        centerMineTargetSetTurn = Robot.turnCount;
                    }

                    // Timeout: if we've been trying for too long, blacklist it
                    int maxTurns = Util.chebyshevDistanceTo(myLoc, centerMine) * 5 + 10;
                    if (Robot.turnCount - centerMineTargetSetTurn > maxTurns) {
                        MapData.blacklistCheeseMineForExploration(centerMine);
                        centerMineTarget = null;
                        centerMineTargetSetTurn = -1;
                    } else {
                        target = centerMine;
                    }
                }
            } else if (centerMine != null) {
                // No current orbit mine, just use center mine
                target = centerMine;
            }
        }

        // Fallback to closest non-blacklisted mine
        if (target == null) {
            target = currentOrbitMine;
            centerMineTarget = null;
        }

        // If another king is closer to our target, blacklist and find another
        MapLocation closestKing = CommsArray.getClosestRatKingNotIncludingSelf(myLoc, CommsArray.ratKingIndex);
        if (closestKing != null && target != null) {
            if (closestKing.distanceSquaredTo(target) < myLoc.distanceSquaredTo(target)) {
                MapData.blacklistCheeseMineForExploration(target);
                target = MapData.getClosestNonBlacklistedCheeseMine(myLoc);
            }
        }

        // Grab nearby cheese on the way
        MapLocation cheeseTarget = CheeseManager.findNearestCheese();
        if (cheeseTarget != null && target != null) {
            if (Util.chebyshevDistanceTo(cheeseTarget, target) >= 2) {
                target = cheeseTarget;
            }
        }

        if (target == null)
            target = Robot.centerLoc;

        return target;
    }

    void trySpawn() throws GameActionException {
        int globalCheese = rc.getGlobalCheese();
        // int effectiveCheese = globalCheese + (knownCheeseMines * 100);

        // If above 3000 cheese, spawn regardless of limit
        if (globalCheese >= 3000) {
            SpawnManager.spawn();
            return;
        }

        // if (globalCheese < 100)
        // return;

        int maxRats = getMaxRatsForCheese(globalCheese);

        if (estimatedMaxRats > maxRats)
            return;
        SpawnManager.spawn();

        // TODO: if we still have action cooldown, then spawn FAILED, and we need to:
        // - Try spawn on every single spawnable tile
        // - If that fails, then SURELY blocked by dirt, so clear a dirt (any works,
        // maybe just closest to center idk)
    }

    int getMaxRatsForCheese(int globalCheese) {
        // if (globalCheese < 50)
        // return 0;
        // if (globalCheese < 200)
        // return 4;
        if (globalCheese < 400)
            return 8;
        // if (globalCheese < 800)
        // return 12;
        if (globalCheese < 1400)
            return 16;
        // if (globalCheese < 2000) // 55.81%
        // return 20;
        // 2000-2999: continue pattern with 24
        return 20;
    }

    void tryBuild() throws GameActionException {
        MapLocation me = rc.getLocation();
        MapLocation cat = null;
        RobotInfo[] robots = rc.senseNearbyRobots(Robot.visionRadiusSquared);
        for (RobotInfo r : robots) {
            if (r.getType() != UnitType.CAT)
                continue;
            cat = r.getLocation();
            break;
        }
        if (cat == null)
            return;

        Direction dir = me.directionTo(cat);
        MapLocation dirt = me.add(dir).add(dir);
        if (rc.canPlaceDirt(dirt))
            rc.placeDirt(dirt);

        BugNav.moveTo(rc.getLocation().add(dir.opposite()).add(dir.opposite()).add(dir.opposite()));
    }

    private void checkOtherRatKingsForDeath() throws GameActionException {
        CommsArray.livingRatKingCount = 1;

        for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
            if (i == CommsArray.ratKingIndex)
                continue;

            MapLocation ratKingLoc = CommsArray.readRatKingLoc(i);
            if (ratKingLoc == null) {
                otherRatKingPreviousAlive[i] = -1;
                otherRatKingTurnsWithoutFlip[i] = 0;
                continue;
            }

            boolean currentAlive = CommsArray.readRatKingAlive(i);
            int currentAliveInt = currentAlive ? 1 : 0;
            int previousAlive = otherRatKingPreviousAlive[i];

            if (previousAlive == -1) {
                // First time seeing this rat king
                otherRatKingPreviousAlive[i] = currentAliveInt;
                otherRatKingTurnsWithoutFlip[i] = 0;
            } else if (currentAliveInt == previousAlive) {
                otherRatKingTurnsWithoutFlip[i]++;
                if (otherRatKingTurnsWithoutFlip[i] >= 2 && Robot.turnCount > 1) {
                    // Bro is cooked
                    CommsArray.clearRatKingSlot(i);
                    otherRatKingPreviousAlive[i] = -1;
                    otherRatKingTurnsWithoutFlip[i] = 0;
                }
            } else {
                // Bit flipped, rat king is alive
                otherRatKingPreviousAlive[i] = currentAliveInt;
                otherRatKingTurnsWithoutFlip[i] = 0;
                ++CommsArray.livingRatKingCount;
            }
        }
    }

    MapLocation getThreat() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.opponent);
        if (enemies.length == 0)
            return null;

        MapLocation bestAns = null;
        int enemyRats = 0;
        int bestDist = 0;
        for (RobotInfo r : enemies) {
            if (r.getType() != UnitType.BABY_RAT)
                continue;
            ++enemyRats;
            int d = r.getLocation().distanceSquaredTo(rc.getLocation());
            if (bestAns == null || d < bestDist) {
                bestDist = d;
                bestAns = r.getLocation();
            }
        }

        RobotInfo[] allies = rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.team);
        if (allies.length < enemyRats + 1)
            return bestAns;
        return null;
    }

    void trySetFormationRallyPoint() throws GameActionException {
        MapLocation currentRally = CommsArray.readFormationRallyPoint();
        int ratKingsAlive = CommsArray.livingRatKingCount;
        int globalCheese = rc.getGlobalCheese();

        // Track rally point transitions
        if (lastSeenRallyPoint == null && currentRally != null) {
            rallyFirstSeenTurn = Robot.turnCount;
        }
        if (lastSeenRallyPoint != null && currentRally == null) {
            rallyPointClearedTurn = Robot.turnCount;
            rallyFirstSeenTurn = -1;
        }
        lastSeenRallyPoint = currentRally;

        // Draw white line to active rally point
        if (currentRally != null) {
            Debug.setIndicatorLine(rc.getLocation(), currentRally, 255, 255, 255);
        }

        boolean onCooldown = Robot.turnCount - rallyPointClearedTurn < 5;
        boolean atMaxKings = ratKingsAlive >= GameConstants.MAX_NUMBER_OF_RAT_KINGS;
        boolean pastCutoff = ratKingsAlive >= GameConstants.MAX_NUMBER_OF_RAT_KINGS_AFTER_CUTOFF
                && rc.getRoundNum() >= GameConstants.RAT_KING_CUTOFF_ROUND;

        if (atMaxKings || pastCutoff || globalCheese < 50) {
            CommsArray.clearFormationRallyPoint();
            return;
        }

        if (onCooldown)
            return;

        boolean hurt = rc.getHealth() < Constants.RAT_KING_HURT_HP && !spawnedRatKing;
        boolean lastMinute = ratKingsAlive < 2 && rc.getRoundNum() > 1900;
        boolean urgent = hurt || lastMinute;

        int knownCheeseMines = CommsArray.isSymmetryGloballyKnown() ? MapData.getKnownCheeseMinesCount()
                : MapData.getFriendlySidedCheeseMinesCount();
        boolean enoughMines = knownCheeseMines >= (ratKingsAlive * 3);
        // boolean ballin = Robot.avgGlobalCheeseDeltaPerTurn20 >= (3 + (ratKingsAlive -
        // 1) * 2)
        // && globalCheese >= (1000 + (ratKingsAlive - 1) * 200);
        boolean enoughEcon = Robot.avgGlobalCheeseDeltaPerTurn20 >= (3 + (ratKingsAlive - 1) * 2)
                && globalCheese >= (1000 + (ratKingsAlive - 1) * 200);
        boolean enoughCheese = Robot.avgGlobalCheeseDeltaPerTurn20 >= (2 + (ratKingsAlive - 1))
                && globalCheese >= (1975 + (ratKingsAlive - 1) * 200);
        boolean enoughRats = estimatedMaxRats > 40;
        boolean bypassEnoughMines = rc.getRoundNum() > 1000 && (enoughEcon && enoughCheese && enoughRats);
        // boolean shouldRally = urgent || (enoughMines && ballin);
        boolean shouldRally = urgent || (enoughMines && (enoughEcon || enoughCheese || enoughRats))
                || bypassEnoughMines;

        if (!shouldRally)
            return;

        // === HANDLE EXISTING RALLY ===
        if (currentRally != null) {
            // Clear if a rat king formed there
            if (rc.canSenseRobotAtLocation(currentRally)
                    && rc.senseRobotAtLocation(currentRally).getType().isRatKingType()) {
                CommsArray.clearFormationRallyPoint();
                return;
            }

            // Check if rally has been there too long (stale)
            int chebyshevDist = Util.chebyshevDistanceTo(rc.getLocation(), currentRally);
            int maxTurns = chebyshevDist * 6;

            if (rallyFirstSeenTurn >= 0 && Robot.turnCount - rallyFirstSeenTurn >= maxTurns) {
                // Blacklist this location for 300 turns
                blacklistedRallyPoint = currentRally;
                blacklistExpiresTurn = Robot.turnCount + 300;
                CommsArray.clearFormationRallyPoint();
                // Continue to set new rally
            } else {
                // Rally exists and isn't stale, don't overwrite
                return;
            }
        }

        // === FIND RALLY LOCATION ===
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(visionRadiusSquared, opponent);
        boolean underThreat = nearbyEnemies.length > 3;

        MapLocation rallyPoint;
        if (urgent) {
            // Urgent: find empty 3x3 beside existing RK
            rallyPoint = findRatKingFormationSpot(myLoc, nearbyEnemies, underThreat);
        } else {
            // Normal: find cheese mine
            rallyPoint = findBestMineForRally(myLoc);
            if (rallyPoint == null)
                rallyPoint = findRatKingFormationSpot(myLoc, nearbyEnemies, false);
        }

        // Skip if location is blacklisted (unless urgent)
        if (!urgent && rallyPoint != null && rallyPoint.equals(blacklistedRallyPoint)
                && Robot.turnCount < blacklistExpiresTurn) {
            return;
        }

        RatKing.spawnedRatKing = true;
        rallyFirstSeenTurn = Robot.turnCount;
        CommsArray.writeFormationRallyPoint(rallyPoint);
    }

    private MapLocation findBestMineForRally(MapLocation myLoc) throws GameActionException {
        MapLocation[] kings = CommsArray.getRatKings();

        // Single king: try opposite quadrant first for map control
        if (CommsArray.livingRatKingCount == 1 && SymmetryManager.isSymmetryDetermined()) {
            MapLocation loc = MapData.getMineInOppositeFriendlyQuadrant(myLoc, kings);
            if (loc != null)
                return loc;
        }

        // Try furthest friendly mine, then closest mine
        MapLocation loc = MapData.getFurthestFriendlyCheeseMineForRally(myLoc, kings);
        if (loc != null)
            return loc;

        return MapData.getCheeseMineRally(myLoc);
    }

    MapLocation findRatKingFormationSpot(MapLocation ratKingLoc, RobotInfo[] enemies, boolean underThreat) {
        Direction preferredDir = null;

        // If under threat, prefer direction away from enemies
        if (underThreat && enemies.length > 0) {
            int avgX = 0, avgY = 0;
            for (RobotInfo enemy : enemies) {
                avgX += enemy.getLocation().x;
                avgY += enemy.getLocation().y;
            }
            avgX /= enemies.length;
            avgY /= enemies.length;
            MapLocation enemyCenter = new MapLocation(avgX, avgY);
            preferredDir = enemyCenter.directionTo(ratKingLoc);
        }

        if (preferredDir != null) {
            for (int dist = 3; dist <= 5; dist++) {
                MapLocation candidate = ratKingLoc;
                for (int i = 0; i < dist; i++) {
                    candidate = candidate.add(preferredDir);
                }
                if (isValidFormationSpot(candidate)) {
                    return candidate;
                }
            }
        }

        Direction[] dirs = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST,
                Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST };
        for (int dist = 3; dist <= 5; dist++) {
            for (Direction dir : dirs) {
                MapLocation candidate = ratKingLoc;
                for (int i = 0; i < dist; i++) {
                    candidate = candidate.add(dir);
                }
                if (isValidFormationSpot(candidate)) {
                    return candidate;
                }
            }
        }

        return ratKingLoc.add(Direction.NORTH).add(Direction.NORTH).add(Direction.NORTH).add(Direction.NORTH);
    }

    boolean isValidFormationSpot(MapLocation center) {
        if (!rc.onTheMap(center)) {
            return false;
        }

        if (MapData.hasSeen(center)) {
            if ((MapData.map[center.x][center.y] & (MapData.WALL_BIT | MapData.DIRT_BIT)) != 0) {
                return false;
            }
        }

        if (rc.canSenseLocation(center)) {
            try {
                if (!rc.sensePassability(center)) {
                    return false;
                }

                if (rc.canSenseRobotAtLocation(center)) {
                    RobotInfo robot = rc.senseRobotAtLocation(center);
                    if (robot != null && robot.getType().isRatKingType()) {
                        return false;
                    }
                }
            } catch (GameActionException e) {
                return false;
            }
        }

        return true;
    }

    void tryDig() throws GameActionException {
        MapLocation me = Robot.rc.getLocation();
        Direction dir = me.directionTo(target);

        MapLocation front;
        MapLocation dig1;
        MapLocation dig2;
        MapLocation dig3;
        MapLocation dig4;
        MapLocation dig5;

        if (dir.dx != 0 && dir.dy != 0) {
            front = me.add(dir);
            dig1 = front.add(dir);
            dig2 = front.add(dir.rotateLeft());
            dig3 = front.add(dir.rotateRight());
            dig4 = front.add(dir.rotateLeft().rotateLeft());
            dig5 = front.add(dir.rotateRight().rotateLeft());
        } else {
            front = me.add(dir);
            dig1 = front.add(dir);
            dig2 = front.add(dir.rotateLeft());
            dig3 = front.add(dir.rotateRight());
            dig4 = dig2.add(dir.rotateLeft());
            dig5 = dig3.add(dir.rotateRight());
        }

        if (Robot.rc.canRemoveDirt(dig1)) {
            Robot.rc.removeDirt(dig1);
            return;
        }

        if (Robot.rc.canRemoveDirt(dig2)) {
            Robot.rc.removeDirt(dig2);
            return;
        }

        if (Robot.rc.canRemoveDirt(dig3)) {
            Robot.rc.removeDirt(dig3);
            return;
        }

        if (Robot.rc.canRemoveDirt(dig4)) {
            Robot.rc.removeDirt(dig4);
            return;
        }

        if (Robot.rc.canRemoveDirt(dig5)) {
            Robot.rc.removeDirt(dig5);
        }

    }
}
