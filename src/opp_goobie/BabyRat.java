package opp_goobie;

import battlecode.common.*;

public class BabyRat extends Robot {
    public static MapLocation closestRatKing;
    static MapLocation lastSeenEnemy;
    static boolean hasMicro = false;
    private static boolean returningForCheese = false;

    // Counterclockwise order of offsets from center
    // N -> NW -> W -> SW -> S -> SE -> E -> NE
    private static final int[][] CCW_OFFSETS = {
        {0, 1},   // NORTH
        {-1, 1},  // NORTHWEST
        {-1, 0},  // WEST
        {-1, -1}, // SOUTHWEST
        {0, -1},  // SOUTH
        {1, -1},  // SOUTHEAST
        {1, 0},   // EAST
        {1, 1}    // NORTHEAST
    };

    BabyRat(RobotController rc) throws GameActionException {
        super(rc);
        MicroManager.init();
        Explore.init();
    }

    @Override
    void beforeTurn() throws GameActionException {
//        Debug.startBytecodeCheck();
        BugNav.allowedToTurnForFun = true;
        closestRatKing = CommsArray.getClosestRatKing(rc.getLocation());

        if (Robot.turnCount == 0) {
            if (!SymmetryManager.isSymmetryDetermined())
                SymmetryManager.setBase(rc.getLocation());
        }

        if (turnCount > 0) {
            CommsArray.syncSymmetryFromGlobalArray();
            CommsSqueak.purgeKingBuffersAtStartOfTurn();
            CommsSqueak.enemies.clear();
            CommsSqueak.readSqueaks();
        }
        MovementManager.update();
        updateHelpTarget();
        updateLastSeenEnemy();

        Explore.updateExploreTarget();
        Explore.updateClosestMineTarget();

        // CarryManager.tryPickup();
        // CarryManager.tryThrow();

        // AttackManager.attack();
        MicroManager.computeMicroArray();
        // hasMicro = MicroManager.doMicro();

        if (turnCount > 0) {
            MapData.updateMapInfos();
        }
        checkSpawnRatKingAlive();
    }

    @Override
    void turn() throws GameActionException {
        tryFormRatKing();
        CarryManager.tryPickup();
        CarryManager.tryThrow();
        TrapManager.placeRatTrap();
        AttackManager.attack();
        CheeseManager.tryGrabCheese();
        if (!MicroManager.doMicro())
            move();
        CheeseManager.tryGrabCheese();
        CheeseManager.tryTransfer();
        CarryManager.tryPickup();
        CarryManager.tryThrow();
        AttackManager.attack();
    }

    @Override
    void afterTurn() throws GameActionException {
        Debug.printBytecode("BEFORE AFTER TURN", Debug.CHECK_BYTECODE_ID);
        // if (Clock.getBytecodesLeft() > 1000) {
        CommsSqueak.squeakBufferedInfo();
        // }
        SymmetryManager.checkSym();
        Debug.printBytecode("AFTER AFTER TURN", Debug.CHECK_BYTECODE_ID);
//        Debug.checkBytecode();
    }

    void move() throws GameActionException {
        if (!rc.isMovementReady())
            return;
        MapLocation target = getTarget();
        Debug.addIndicatorString("Target: " + target);
        BugNav.moveTo(target);
    }

    void dance() throws GameActionException {
        int round = rc.getRoundNum();
        if (round < 10 || !rc.canTurn())
            return;

        int danceSpacing = 88;
        int dancePeriod = 8; // keep even
        int shouldDance = rc.getRoundNum() % danceSpacing;
        if (shouldDance < dancePeriod) {
            if (shouldDance < dancePeriod / 2) {
                rc.turn(Direction.WEST);
            } else {
                rc.turn(Direction.EAST);
            }
            if (rc.canMove(Direction.CENTER))
                rc.move(Direction.CENTER);
        }
    }

    MapLocation getTarget() throws GameActionException {
        MapLocation formationTarget = getFormationTarget();
        if (formationTarget != null) {
            return formationTarget;
        }

        MapLocation target = null;
        boolean needReturn = needsToReturn();

        if (needReturn) {
            return closestRatKing;
        }

        RobotInfo r = getClosestEnemy(rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.opponent));
        if (r != null) {
            return r.location;
            // lastSeenEnemy = r.getLocation();
            // return lastSeenEnemy;
        }

        if (helpTarget != null) {
            return helpTarget;
        }

        // if (target == null) {
        // target = lastSeenEnemy;
        // }

        if (target == null) {
            target = CheeseManager.findNearestCheese();
            if (target != null)
                BugNav.allowedToTurnForFun = false;
        }

        if (target == null) {
            target = Explore.getEarlySymTarget();
            if (target != null) {
                Explore.earlyExploreTarget = target;
            } else {
                if (Explore.shouldExploitOverExplore()) {
                    // target = Explore.getExploitTarget();
                    target = Explore.closestMine;

                    if (target == null)
                        target = Explore.getExploreTarget();
                } else {
                    target = Explore.getExploreTarget();
                }
            }
        }

        return target;
    }

    boolean needsToReturn() throws GameActionException {
        if (returningForCheese) {
            Debug.addIndicatorString("returning ofr cheese");
            if (rc.getRawCheese() == 0) {
                returningForCheese = false;
            } else {
                return true;
            }
        }

        // if (CommsSqueak.hasNewSymmetryForKing &&
        // !CommsArray.isSymmetryGloballyKnown())
        // return true;

        if (CommsSqueak.discoveredPOILocsForKing.size() > 0)
            return true;

        int cheeseThreshold = calculateCheeseReturnThreshold();
        int currentCheese = rc.getRawCheese();
        MapLocation myLoc = rc.getLocation();

        // If we have cheese but don't see any more nearby, return with what we have
        if (currentCheese > 0) {
            MapLocation nearestCheese = CheeseManager.findNearestCheese();
            if (nearestCheese == null) {
                // No visible cheese - return with whatever we have
                returningForCheese = true;
                return true;
            }
        }

        // If we have enough cheese based on threshold, return unless there's more
        // cheese very nearby
        if (currentCheese >= cheeseThreshold) {
            if (currentCheese >= Constants.MAX_CHEESE_TO_RETURN) {
                Debug.addIndicatorString("Max cheese, returning");
                returningForCheese = true;
                return true;
            }
            MapLocation nearestCheese = CheeseManager.findNearestCheese();
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(15, opponent);

            if (nearestCheese != null && nearbyEnemies.length == 0) {
                int distToCheese = myLoc.distanceSquaredTo(nearestCheese);
                if (distToCheese <= 4) {
                    Debug.addIndicatorString("Grabbing nearby cheese before return");
                    return false;
                }
            }

            returningForCheese = true;
            return true;
        }

        return false;
    }

    // ugly aaaah code
    private int calculateCheeseReturnThreshold() throws GameActionException {
        if (closestRatKing == null) {
            return Constants.MIN_CHEESE_TO_RETURN;
        }

        // Calculate Chebyshev distance (max of |dx|, |dy|)
        MapLocation myLoc = rc.getLocation();
        int dx = Math.abs(myLoc.x - closestRatKing.x);
        int dy = Math.abs(myLoc.y - closestRatKing.y);
        int chebyshevDist = Math.max(dx, dy);

        // Linear interpolation: farther from king -> need more cheese to return (longer
        // journey);
        // closer -> return with less (shorter trip, get back faster)
        int distanceRange = Constants.CHEESE_TILES_TO_RETURN_MAX - Constants.CHEESE_TILES_TO_RETURN_MIN;
        int cheeseRange = Constants.MAX_CHEESE_TO_RETURN - Constants.MIN_CHEESE_TO_RETURN;
        int distFromMin = chebyshevDist - Constants.CHEESE_TILES_TO_RETURN_MIN;
        int cheeseThreshold = Constants.MIN_CHEESE_TO_RETURN + (cheeseRange * distFromMin / distanceRange);

        // Clamp for distances outside [MIN, MAX] so we don't return with < MIN or
        // require > MAX
        cheeseThreshold = Math.max(Constants.MIN_CHEESE_TO_RETURN,
            Math.min(Constants.MAX_CHEESE_TO_RETURN, cheeseThreshold));

        // When global cheese is low, lower the threshold so we return with less carried
        // cheese
        int reduction = 0;
        int global = rc.getGlobalCheese();
        if (global < Constants.CHEESE_GLOBAL_NO_EFFECT) {
            int range = Constants.CHEESE_GLOBAL_NO_EFFECT - Constants.CHEESE_GLOBAL_MAX_EFFECT;
            int maxReduction = Constants.MIN_CHEESE_TO_RETURN - Constants.CHEESE_GLOBAL_RETURN_FLOOR;
            int amount = Constants.CHEESE_GLOBAL_NO_EFFECT
                - Math.min(Constants.CHEESE_GLOBAL_NO_EFFECT,
                Math.max(Constants.CHEESE_GLOBAL_MAX_EFFECT, global));
            reduction = amount * maxReduction / range;
            cheeseThreshold = Math.max(Constants.CHEESE_GLOBAL_RETURN_FLOOR,
                cheeseThreshold - reduction);
        }

        Debug.addIndicatorString(" CT: " + cheeseThreshold + (reduction > 0 ? " (-" + reduction + ")" : ""));
        return cheeseThreshold;
    }

    RobotInfo getClosestEnemy(RobotInfo[] robots) {
        RobotInfo target = null;
        MapLocation me = Robot.rc.getLocation();
        int minDist = Constants.INF;

        for (RobotInfo r : robots) {
            MapLocation rloc = r.getLocation();
            int dist = me.distanceSquaredTo(rloc);
            if (target == null || dist < minDist) {
                target = r;
                minDist = dist;
            }
        }

        return target;
    }

    boolean isFull() {
        return rc.getRawCheese() >= Constants.MIN_CHEESE_RETURN;
    }

    void updateHelpTarget() {
        if (helpTarget == null || !rc.canSenseLocation(helpTarget))
            return;

        helpTarget = null;
    }

    void updateLastSeenEnemy() throws GameActionException {
        if (lastSeenEnemy == null)
            return;

        if (rc.canSenseLocation(lastSeenEnemy)) {
            if (rc.senseNearbyRobots(Robot.visionRadiusSquared).length == 0) {
                lastSeenEnemy = null;
            }
        }
    }

    private void checkSpawnRatKingAlive() throws GameActionException {
        if (spawnerRatKingIndex == null)
            return;

        MapLocation ratKingLoc = CommsArray.readRatKingLoc(spawnerRatKingIndex);
        if (ratKingLoc == null) {
            // Spawn rat king is dead, find closest
            if (closestRatKing != null) {
                for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
                    MapLocation otherRatKingLoc = CommsArray.readRatKingLoc(i);
                    if (otherRatKingLoc != null && otherRatKingLoc.equals(closestRatKing)) {
                        spawnerRatKingIndex = i;
                        Debug.addIndicatorString("switched to rk: " + spawnerRatKingIndex);
                        return;
                    }
                }
            }
        }
    }

    MapLocation getFormationTarget() throws GameActionException {
        if (rc.getRoundNum() < 10)
            return null;

        MapLocation rallyPoint = CommsArray.readFormationRallyPoint();
        if (rallyPoint == null) {
            return null;
        }

        MapLocation myLoc = rc.getLocation();

        if (Util.chebyshevDistanceTo(rallyPoint, myLoc) > 40) {
            return null;
        }

        if (rc.canSenseLocation(rallyPoint) && isRallyBlocked(rallyPoint)) {
            MapLocation nearbyValidSpot = findNearbyValidFormationSpot(rallyPoint);
            if (nearbyValidSpot != null) {
                rallyPoint = nearbyValidSpot;
            }
        }

        if (myLoc.distanceSquaredTo(rallyPoint) <= 2) {
            // We're in the 3x3 square around rally point

            // If we're at the center, stay there
            if (myLoc.equals(rallyPoint)) {
                return rallyPoint;
            }

            // Check if center is occupied by a baby rat
            boolean centerOccupied = false;
            if (rc.canSenseLocation(rallyPoint)) {
                RobotInfo centerRobot = rc.senseRobotAtLocation(rallyPoint);
                if (centerRobot != null && centerRobot.getType() == UnitType.BABY_RAT) {
                    centerOccupied = true;
                }
            }

            // If center is occupied, spin counterclockwise within the 3x3
            if (centerOccupied) {
                MapLocation ccwTarget = getCounterclockwiseTarget(myLoc, rallyPoint);
                if (ccwTarget != null) {
                    return ccwTarget;
                }
            }

            // Original logic: find closest open slot (used when center is not occupied)
            MapLocation myTargetSlot = null;
            int minDist = Constants.INF;

            RobotInfo[] nearbyRats = rc.senseNearbyRobots(10, team);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation slot = rallyPoint.translate(dx, dy);
                    if (!rc.onTheMap(slot))
                        continue;

                    if (rc.canSenseLocation(slot)) {
                        MapInfo info = rc.senseMapInfo(slot);
                        if (!info.isPassable())
                            continue;
                    }

                    boolean occupied = false;
                    for (RobotInfo r : nearbyRats) {
                        if (r.getLocation().equals(slot) && r.getType() == UnitType.BABY_RAT) {
                            occupied = true;
                            break;
                        }
                    }

                    if (!occupied) {
                        int dist = myLoc.distanceSquaredTo(slot);
                        if (dist < minDist) {
                            minDist = dist;
                            myTargetSlot = slot;
                        }
                    }
                }
            }

            if (myTargetSlot != null && !myLoc.equals(myTargetSlot)) {
                return myTargetSlot;
            }
        }

        return rallyPoint;
    }

    private MapLocation getCounterclockwiseTarget(MapLocation myLoc, MapLocation rallyPoint) throws GameActionException {
        int dx = myLoc.x - rallyPoint.x;
        int dy = myLoc.y - rallyPoint.y;

        // Find current CCW index
        int currentIdx = -1;
        for (int i = 0; i < 8; i++) {
            if (CCW_OFFSETS[i][0] == dx && CCW_OFFSETS[i][1] == dy) {
                currentIdx = i;
                break;
            }
        }

        if (currentIdx == -1) {
            // Should never happen if we're in the 3x3 and not at center
            return null;
        }

        RobotInfo[] nearbyRats = rc.senseNearbyRobots(10, team);

        // Try positions counterclockwise from current position
        for (int i = 1; i <= 8; i++) {
            int nextIdx = (currentIdx + i) % 8;
            MapLocation nextLoc = rallyPoint.translate(CCW_OFFSETS[nextIdx][0], CCW_OFFSETS[nextIdx][1]);

            if (!rc.onTheMap(nextLoc))
                continue;

            if (rc.canSenseLocation(nextLoc)) {
                MapInfo info = rc.senseMapInfo(nextLoc);
                if (!info.isPassable())
                    continue;
            }

            // Check if occupied by a baby rat
            boolean occupied = false;
            for (RobotInfo r : nearbyRats) {
                if (r.getLocation().equals(nextLoc) && r.getType() == UnitType.BABY_RAT) {
                    occupied = true;
                    break;
                }
            }

            if (!occupied) {
                return nextLoc;
            }
        }

        // All slots occupied, stay where we are
        return myLoc;
    }

    boolean isRallyBlocked(MapLocation center) throws GameActionException {
        int blockedTiles = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation tile = center.translate(dx, dy);
                if (!rc.onTheMap(tile))
                    return true;

                if (MapData.isWall(tile)) {
                    blockedTiles++;
                    continue;
                }

                if (rc.canSenseLocation(tile)) {
                    MapInfo info = rc.senseMapInfo(tile);
                    if (!info.isPassable()) {
                        blockedTiles++;
                    }

                    RobotInfo robot = rc.senseRobotAtLocation(tile);
                    if (robot != null && (robot.getType() == UnitType.RAT_KING || robot.getType() == UnitType.CAT)) {
                        return true;
                    }
                }
            }
        }

        return blockedTiles > 2;
    }

    MapLocation findNearbyValidFormationSpot(MapLocation blockedCenter) throws GameActionException {
        for (int radius = 1; radius <= 2; radius++) {
            for (Direction dir : DIRECTIONS) {
                MapLocation candidate = blockedCenter.add(dir);
                if (radius == 2) {
                    candidate = candidate.add(dir);
                }

                if (rc.canSenseLocation(candidate) && !isRallyBlocked(candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    void tryFormRatKing() throws GameActionException {
        Debug.addIndicatorString("Living Kings: " + CommsArray.livingRatKingCount);
        MapLocation rallyPoint = CommsArray.readFormationRallyPoint();
        if (rallyPoint == null) {
            return;
        }

        if (rc.getGlobalCheese() < 50) {
            return;
        }

        if (Util.chebyshevDistanceTo(rallyPoint, rc.getLocation()) <= 4 && rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            CommsArray.clearFormationRallyPoint();
        }
    }
}
