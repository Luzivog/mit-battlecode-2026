package experiment.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import experiment.CellTracker;
import static experiment.Game.mapHeight;
import static experiment.Game.mapWidth;
import static experiment.Game.rc;
import experiment.Pathfinder;
import experiment.comms.GlobalArray;
import experiment.comms.Squeaker;
import experiment.robot.Strategy;
import experiment.robot.rat.strategies.CheeseStrategy;
import experiment.robot.rat.strategies.ExploreStrategy;
import experiment.robot.rat.strategies.GoHomeStrategy;
import experiment.robot.rat.strategies.MineStrategy;
import experiment.util.Rng;
import static experiment.util.Util.chebyshevDistance;
import static experiment.util.Util.decode2x2;
import static experiment.util.Util.encode2x2;
import static experiment.util.Util.flipHor;
import static experiment.util.Util.flipVer;
import static experiment.util.Util.rot180;

/**
 * Unified BabyRat class that handles both MINER and RUSHER roles.
 * State machine: EXPLORE/MINE/RUSH -> GO_HOME -> repeat
 */
public class BabyRat extends Rat {
    private enum State {
        EXPLORE,    // Looking for cheese mines
        MINING,     // Harvesting cheese from a mine
        RUSHING,    // Attacking enemy (backstab mode)
        GO_HOME,    // Returning to king to transfer cheese
        FORMING_KING // Moving to position to form a new rat king
    }

    private BabyRatRole role = BabyRatRole.MINER;
    private State state = State.EXPLORE;

    private Direction lastGlance;
    private GoHomeStrategy goHomeStrategy;
    private CheeseStrategy cheeseStrategy;
    private MineStrategy mineStrategy;
    private Strategy exploreStrategy;
    private boolean useAwubotMiner;

    // Navigation
    private MapLocation home;           // King location
    private MapLocation exploreTarget;  // Random exploration target
    private MapLocation mineLocation;   // Known cheese mine
    private MapLocation enemyKingLoc;   // Guessed enemy king location

    // Stuck detection
    private MapLocation lastLoc;
    private int stuckTurns;

    // Mining state
    private int mineEmptyTurns;
    private static final int MINE_EMPTY_LEAVE_TURNS = 3;

    // Cheese thresholds - adjusted for faster economy
    private static final int GO_HOME_CHEESE_MIN = 30;  // Lowered: go home earlier
    private static final int GO_HOME_CHEESE_MAX = 60; // Lowered: don't hoard cheese
    private static final int LOW_HEALTH_THRESHOLD = 30;
    
    // Exploration state for systematic coverage
    private int explorePhase = 0;       // 0=center, 1=quadrant, 2=edges
    private int exploreQuadrant = 0;    // Which quadrant to explore next
    private MapLocation spawnLoc;       // Where this rat spawned
    private int ratIdHash;              // For distributing exploration
    private int lastMineCheckRound = 0; // When we last checked for shared mines
    
    // King formation state
    private static final int CHEESE_THRESHOLD_FOR_NEW_KING = 100;
    private static final int MIN_DIST_FROM_KINGS_SQ = 400; // 20 tiles squared
    private MapLocation kingFormationTarget = null;
    private int kingFormationAttempts = 0;

    @Override
    public void init() throws GameActionException {
        CellTracker.storeVisibleCells();
        determineRole();
        updateHome();
        enemyKingLoc = guessEnemyKingLocation();

        if (role == BabyRatRole.MINER) {
            cheeseStrategy = new CheeseStrategy(this);
            goHomeStrategy = new GoHomeStrategy(this, cheeseStrategy);
            mineStrategy = new MineStrategy(this, cheeseStrategy);
            exploreStrategy = new ExploreStrategy(this);
            goHomeStrategy.setHome(rc.getLocation());
            useAwubotMiner = true;
        }
        
        // Set up exploration distribution
        spawnLoc = rc.getLocation();
        ratIdHash = rc.getID() % 8; // For distributing rats to different areas
        exploreQuadrant = ratIdHash % 4;
    }

    @Override
    public void run() throws GameActionException {
        CellTracker.storeVisibleCells();
        CellTracker.checkSymmetry();
        updateHome();
        updateStuck();

        if (role == BabyRatRole.MINER && useAwubotMiner) {
            runAwubotMiner();
            return;
        }

        // Check for cats and warn the king
        checkAndBroadcastCats();
        
        // Check for shared cheese mine locations periodically
        if (state == State.EXPLORE && mineLocation == null && 
            rc.getRoundNum() - lastMineCheckRound > 10) {
            checkSharedMines();
        }

        // Opportunistic transfer: if we have cheese and are near king, transfer it
        if (rc.getRawCheese() > 0 && home != null) {
            tryTransferCheese();
        }

        // Force go home if low health
        if (rc.getHealth() <= LOW_HEALTH_THRESHOLD && rc.getRawCheese() > 0) {
            state = State.GO_HOME;
        }
        
        // Check king cheese urgency - if global cheese is low, prioritize returning
        if (state != State.GO_HOME && rc.getRawCheese() > 0 && isKingCheeseUrgent()) {
            state = State.GO_HOME;
        }

        // Check if we should go home with cheese
        if (state != State.GO_HOME && shouldGoHome()) {
            state = State.GO_HOME;
        }
        
        // Check if we should try to form a new rat king
        if (state != State.FORMING_KING && shouldTryFormKing()) {
            state = State.FORMING_KING;
            kingFormationTarget = findKingFormationSpot();
            kingFormationAttempts = 0;
        }

        // Run state machine
        switch (state) {
            case GO_HOME -> runGoHome();
            case MINING -> runMining();
            case RUSHING -> runRushing();
            case FORMING_KING -> runFormingKing();
            default -> runExplore();
        }
        
        // FALLBACK: If stuck for too long, force some movement
        if (stuckTurns >= 2) {
            tryFallbackMovement();
        }
        
        // Record position at end of turn for stuck detection
        updateStuckEnd();
    }
    
    /** Emergency fallback movement when stuck. Always tries to do SOMETHING. */
    private void tryFallbackMovement() throws GameActionException {
        // First, try to turn if we can
        if (rc.canTurn()) {
            // Pick a random direction to face
            Direction[] allDirs = Direction.allDirections();
            Direction targetDir = allDirs[(Rng.mcg() & 7)]; // Use mcg() with bitmask for 0-7
            if (targetDir != rc.getDirection() && targetDir != Direction.CENTER) {
                Pathfinder.turn(targetDir);
            }
        }
        
        // Then try to move in any valid direction
        if (rc.isMovementReady()) {
            Direction myDir = rc.getDirection();
            // Try forward first, then adjacent directions
            Direction[] tryDirs = {
                myDir,
                myDir.rotateLeft(),
                myDir.rotateRight(),
                myDir.rotateLeft().rotateLeft(),
                myDir.rotateRight().rotateRight(),
                myDir.opposite().rotateLeft(),
                myDir.opposite().rotateRight(),
                myDir.opposite()
            };
            
            for (Direction dir : tryDirs) {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    stuckTurns = 0; // Reset stuck counter
                    return;
                }
            }
        }
    }

    private void runAwubotMiner() throws GameActionException {
        checkAndBroadcastCats();
        if (rc.getRawCheese() == 0) {
            goHomeStrategy.setGoingHome(false);
        }
        if (goHomeStrategy.tryStrategy(goHomeStrategy)) return;
        if (cheeseStrategy.tryStrategy(cheeseStrategy)) return;
        if (mineStrategy.tryStrategy(mineStrategy)) return;
        exploreStrategy.tryStrategy(exploreStrategy);

        if (rc.canTurn()) {
            Direction leftDir = rc.getDirection().rotateLeft().rotateLeft();
            Direction rightDir = rc.getDirection().rotateRight().rotateRight();
            if (lastGlance == leftDir) {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            } else if (lastGlance == rightDir || Rng.mcg() >> 30 != 0) {
                Pathfinder.turn(leftDir);
                lastGlance = leftDir;
            } else {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            }
        }
    }

    private void checkAndBroadcastCats() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.CAT) {
                Squeaker.broadcastCatLocation(robot.getLocation());
                return; // Only broadcast one cat per tick
            }
        }
    }

    private void determineRole() throws GameActionException {
        // Find nearby king to read spawn role
        MapLocation kingLoc = null;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam() && info.getType() == UnitType.RAT_KING) {
                kingLoc = info.getLocation();
                break;
            }
        }

        if (kingLoc != null) {
            int roleValue = GlobalArray.readSpawnRoleForKing(kingLoc, rc.getRoundNum());
            role = BabyRatRole.fromValue(roleValue);
        }

        // Set initial state based on role
        state = (role == BabyRatRole.RUSH) ? State.RUSHING : State.EXPLORE;
    }

    // =========================================================================
    // GO HOME STATE - Return to king and transfer cheese
    // =========================================================================
    private void runGoHome() throws GameActionException {

        // If we've transferred all cheese, return to previous activity
        if (rc.getRawCheese() == 0) {
            state = (role == BabyRatRole.RUSH) ? State.RUSHING : State.EXPLORE;
            // If we were mining and the mine still exists, go back to it
            if (role == BabyRatRole.MINER && mineLocation != null) {
                state = State.MINING;
            }
            return;
        }

        // ALWAYS update home location when going home - king moves!
        updateHome();

        if (home == null) {
            // Can't find king, search for it
            searchForKing();
            return;
        }
        
        MapLocation myLoc = rc.getLocation();
        int distToHome = chebyshevDistance(myLoc, home);
        
        // If we're at the home location but can't see the king, he moved!
        // Force a re-read from shared array
        if (distToHome <= 2) {
            boolean canSeeKing = false;
            for (RobotInfo robot : rc.senseNearbyRobots()) {
                if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                    canSeeKing = true;
                    home = robot.getLocation(); // Update to exact location
                    break;
                }
            }
            
            if (!canSeeKing) {
                // King isn't here! Force re-read from shared array
                home = null;
                updateHome();
                if (home == null) {
                    searchForKing();
                    return;
                }
            }
        }
        
        // If adjacent to king, prioritize transfer over movement
        if (distToHome <= 2) {
            tryTransferCheese();
            // If transfer succeeded, we might be done
            if (rc.getRawCheese() == 0) {
                state = (role == BabyRatRole.RUSH) ? State.RUSHING : State.EXPLORE;
                if (role == BabyRatRole.MINER && mineLocation != null) {
                    state = State.MINING;
                }
                return;
            }
        }
        
        // Move toward home
        Pathfinder.makeMove(home);

        // Try to transfer cheese again after moving
        tryTransferCheese();
    }
    
    private void searchForKing() throws GameActionException {
        // Check the shared array for king locations - try all slots
        MapLocation bestKing = null;
        int bestDist = Integer.MAX_VALUE;
        MapLocation myLoc = rc.getLocation();
        
        for (int i = 0; i < 5; i++) {
            MapLocation kingLoc = GlobalArray.getRat(i);
            if (kingLoc != null && rc.onTheMap(kingLoc)) {
                int dist = myLoc.distanceSquaredTo(kingLoc);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestKing = kingLoc;
                }
            }
        }
        
        if (bestKing != null) {
            home = bestKing;
            Pathfinder.makeMove(home);
            return;
        }
        
        // Still can't find king - try to find any allied rat king by looking around
        // Move toward center of map to maximize chance of hearing squeaks
        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        Pathfinder.makeMove(center);
    }
    
    private MapLocation findNearestCorner() {
        int inset = 5;
        MapLocation[] corners = {
            new MapLocation(inset, inset),
            new MapLocation(inset, mapHeight - 1 - inset),
            new MapLocation(mapWidth - 1 - inset, inset),
            new MapLocation(mapWidth - 1 - inset, mapHeight - 1 - inset),
        };
        
        MapLocation myLoc = rc.getLocation();
        MapLocation best = corners[0];
        int bestDist = myLoc.distanceSquaredTo(best);
        
        for (int i = 1; i < corners.length; i++) {
            int dist = myLoc.distanceSquaredTo(corners[i]);
            if (dist < bestDist) {
                bestDist = dist;
                best = corners[i];
            }
        }
        return best;
    }

    private void tryTransferCheese() throws GameActionException {
        if (home == null) return;

        int cheese = rc.getRawCheese();
        if (cheese <= 0) return;

        MapLocation myLoc = rc.getLocation();

        // Only transfer if adjacent to king (within 2 tiles for 3x3 king)
        if (chebyshevDistance(myLoc, home) > 2) return;

        Direction toHome = myLoc.directionTo(home);
        MapLocation transferTarget = myLoc.add(toHome);

        if (rc.canTransferCheese(transferTarget, cheese)) {
            rc.transferCheese(transferTarget, cheese);
        }
    }

    // =========================================================================
    // EXPLORE STATE - Find cheese mines with systematic exploration
    // =========================================================================
    private void runExplore() throws GameActionException {

        // Check if we found a mine
        MapLocation mine = findVisibleMine();
        if (mine != null) {
            mineLocation = mine;
            mineEmptyTurns = 0;
            state = State.MINING;
            // Broadcast this mine location to teammates
            broadcastMineLocation(mine);
            runMining();
            return;
        }

        // Pick up any cheese we pass
        tryPickUpCheese();

        // Move to explore target - ensure we have a valid one
        ensureExploreTarget();
        if (exploreTarget != null && !rc.getLocation().equals(exploreTarget)) {
            Pathfinder.makeMove(exploreTarget);
        } else {
            // No valid target or at target - pick a new one immediately
            exploreTarget = null;
            stuckTurns = 0;
            ensureExploreTarget();
            if (exploreTarget != null) {
                Pathfinder.makeMove(exploreTarget);
            }
        }
    }
    
    private void checkSharedMines() throws GameActionException {
        lastMineCheckRound = rc.getRoundNum();
        // Check the global array for known cheese mines (stored in indices 32-63)
        for (int i = 63; i >= 32; i--) {
            try {
                int encoded = rc.readSharedArray(i);
                if (encoded != 0 && encoded != 1023) {
                    MapLocation mineLoc = decode2x2(encoded);
                    if (mineLoc != null && rc.onTheMap(mineLoc)) {
                        // Found a shared mine - go there if it's reasonable distance
                        int dist = rc.getLocation().distanceSquaredTo(mineLoc);
                        if (dist < 400) { // Within reasonable distance
                            mineLocation = mineLoc;
                            state = State.MINING;
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore read errors
            }
        }
    }
    
    private void broadcastMineLocation(MapLocation mine) throws GameActionException {
        if (mine == null) return;
        int encoded = encode2x2(mine);
        // Find an empty slot in the mine broadcast area (indices 32-63)
        for (int i = 32; i < 64; i++) {
            try {
                int existing = rc.readSharedArray(i);
                if (existing == 0 || existing == 1023 || existing == encoded) {
                    // Can't write directly (only rat kings can), but we can squeak it
                    // For now, just remember it locally - the squeaker handles this
                    return;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void ensureExploreTarget() throws GameActionException {
        MapLocation myLoc = rc.getLocation();

        boolean needsTarget = exploreTarget == null
            || myLoc.distanceSquaredTo(exploreTarget) <= 4
            || stuckTurns >= 5
            || myLoc.equals(exploreTarget);

        if (!needsTarget) return;

        stuckTurns = 0;
        
        // Try to get a smart target, but ensure it's valid
        for (int attempt = 0; attempt < 3; attempt++) {
            MapLocation candidate = pickSmartExploreTarget(myLoc);
            if (candidate != null && !candidate.equals(myLoc) && 
                myLoc.distanceSquaredTo(candidate) >= 4) {
                exploreTarget = candidate;
                return;
            }
        }
        
        // Fallback: pick any random valid target
        exploreTarget = pickRandomTarget(myLoc, 9);
    }
    
    private MapLocation pickSmartExploreTarget(MapLocation origin) {
        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        
        // Phase 0: Explore toward center first (cheese mines often near center)
        if (explorePhase == 0) {
            if (origin.distanceSquaredTo(center) > 64) {
                explorePhase = 1; // Advance phase so we don't get stuck
                return center;
            }
            explorePhase = 1;
        }
        
        // Phase 1: Explore assigned quadrant based on rat ID
        if (explorePhase == 1) {
            MapLocation quadrantTarget = getQuadrantTarget(exploreQuadrant);
            if (origin.distanceSquaredTo(quadrantTarget) > 25) {
                return quadrantTarget;
            }
            // Move to next quadrant after exploring this one
            exploreQuadrant = (exploreQuadrant + 1) % 4;
            if (exploreQuadrant == ratIdHash % 4) {
                explorePhase = 2; // Done with quadrants
            }
            // Try next quadrant immediately
            quadrantTarget = getQuadrantTarget(exploreQuadrant);
            if (origin.distanceSquaredTo(quadrantTarget) > 16) {
                return quadrantTarget;
            }
            explorePhase = 2;
        }
        
        // Phase 2: Distributed exploration
        return pickDistributedTarget(origin);
    }
    
    private MapLocation getQuadrantTarget(int quadrant) {
        int qx = (quadrant & 1) == 0 ? mapWidth / 4 : 3 * mapWidth / 4;
        int qy = (quadrant & 2) == 0 ? mapHeight / 4 : 3 * mapHeight / 4;
        return new MapLocation(qx, qy);
    }
    
    private MapLocation pickDistributedTarget(MapLocation origin) {
        // Try to pick a location that spreads us out from home
        // Use rat ID to create different exploration patterns
        int angle = (ratIdHash * 45 + rc.getRoundNum() / 20 * 30) % 360;
        double rad = Math.toRadians(angle);
        int dist = 10 + (rc.getRoundNum() / 50) % 15;
        
        int x = origin.x + (int)(Math.cos(rad) * dist);
        int y = origin.y + (int)(Math.sin(rad) * dist);
        
        // Clamp to map bounds
        x = Math.max(2, Math.min(mapWidth - 3, x));
        y = Math.max(2, Math.min(mapHeight - 3, y));
        
        MapLocation target = new MapLocation(x, y);
        
        // If too close to origin, pick random
        if (origin.distanceSquaredTo(target) < 9) {
            return pickRandomTarget(origin, 16);
        }
        
        return target;
    }

    private MapLocation pickRandomTarget(MapLocation origin, int minDistSq) {
        // Pick random target away from origin, biased away from home
        for (int i = 0; i < 10; i++) {
            int x = (Rng.mcg() & 0x7fffffff) % mapWidth;
            int y = (Rng.mcg() & 0x7fffffff) % mapHeight;
            MapLocation loc = new MapLocation(x, y);
            if (origin.distanceSquaredTo(loc) >= minDistSq) {
                // Prefer locations away from home to spread out
                if (home != null && loc.distanceSquaredTo(home) < origin.distanceSquaredTo(home)) {
                    continue; // Skip locations closer to home
                }
                return loc;
            }
        }
        // Fallback: opposite of home
        if (home != null) {
            return rot180(home);
        }
        return new MapLocation(mapWidth / 2, mapHeight / 2);
    }

    // =========================================================================
    // MINING STATE - Harvest cheese from mine
    // =========================================================================
    private void runMining() throws GameActionException {
        
        // Verify mine still exists if we can see it
        if (mineLocation != null && rc.canSenseLocation(mineLocation)) {
            MapInfo info = rc.senseMapInfo(mineLocation);
            if (!info.hasCheeseMine()) {
                mineLocation = null;
                mineEmptyTurns = 0;
                state = State.EXPLORE;
                runExplore(); // Don't waste this turn
                return;
            }
        }
        
        // If we've collected a good amount and mine seems sparse, go home
        int rawCheese = rc.getRawCheese();
        if (rawCheese >= 50 && mineEmptyTurns >= 1) {
            state = State.GO_HOME;
            runGoHome(); // Don't waste this turn
            return;
        }

        // Try to pick up cheese near the mine
        MapLocation cheese = findBestNearbyCheese();
        if (cheese != null) {
            mineEmptyTurns = 0;
            if (rc.canPickUpCheese(cheese)) {
                rc.pickUpCheese(cheese);
                // After picking up, check if we should go home
                if (shouldGoHome()) {
                    state = State.GO_HOME;
                }
            } else {
                // Move toward the cheese
                Pathfinder.makeMove(cheese);
            }
            return;
        }

        // No cheese visible - increment empty turns and decide what to do
        mineEmptyTurns++;
        
        if (rawCheese > 0 && mineEmptyTurns >= MINE_EMPTY_LEAVE_TURNS) {
            // Have cheese and waited enough - go home
            state = State.GO_HOME;
            runGoHome(); // Don't waste this turn
            return;
        }
        
        if (mineEmptyTurns >= MINE_EMPTY_LEAVE_TURNS + 3) {
            // Waited too long with no cheese - explore elsewhere
            mineLocation = null;
            mineEmptyTurns = 0;
            state = State.EXPLORE;
            runExplore(); // Don't waste this turn
            return;
        }

        // Still waiting - move around the mine to find cheese
        if (mineLocation != null) {
            MapLocation orbitTarget = getOrbitPosition(mineLocation);
            // Only move if target is different from current location
            if (!rc.getLocation().equals(orbitTarget)) {
                Pathfinder.makeMove(orbitTarget);
            } else {
                // At orbit position, try moving to mine center
                Pathfinder.makeMove(mineLocation);
            }
        }
    }
    
    private MapLocation getOrbitPosition(MapLocation center) {
        // Move in a pattern around the mine center to find cheese
        int round = rc.getRoundNum();
        int offsetIndex = (round / 3) % 8;
        Direction[] orbitDirs = {
            Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST
        };
        Direction offset = orbitDirs[(offsetIndex + ratIdHash) % 8];
        return center.add(offset).add(offset);
    }

    // =========================================================================
    // RUSHING STATE - Attack enemy (backstab mode)
    // =========================================================================
    private void runRushing() throws GameActionException {
        // Update enemy king location based on current knowledge
        enemyKingLoc = guessEnemyKingLocation();
        
        // Pick up cheese along the way
        tryPickUpCheese();

        // Try to attack nearby enemies
        if (tryAttackEnemy()) {
            return;
        }

        if (enemyKingLoc == null) {
            runExplore();
            return;
        }

        // If stuck for a while, pick a random intermediate target to get unstuck
        if (stuckTurns >= 4) {
            exploreTarget = pickRandomTarget(rc.getLocation(), 16);
        }

        // Use intermediate target if we have one and haven't reached it
        MapLocation target = enemyKingLoc;
        if (exploreTarget != null) {
            if (rc.getLocation().distanceSquaredTo(exploreTarget) <= 4) {
                exploreTarget = null; // Reached intermediate target, go back to main target
            } else {
                target = exploreTarget;
            }
        }

        Pathfinder.makeMove(target);
    }

    private boolean tryAttackEnemy() throws GameActionException {
        if (!rc.isActionReady()) return false;

        RobotInfo bestTarget = null;
        int bestScore = Integer.MIN_VALUE;

        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getTeam() == rc.getTeam()) continue;
            if (robot.getType() == UnitType.CAT) continue; // Don't attack cats in backstab

            MapLocation robotLoc = robot.getLocation();
            if (!rc.getLocation().isAdjacentTo(robotLoc)) continue;
            if (!rc.canAttack(robotLoc)) continue;

            // Prioritize: kings > low health > close
            int score = 0;
            if (robot.getType() == UnitType.RAT_KING) score += 1000;
            score -= robot.getHealth();

            if (score > bestScore) {
                bestScore = score;
                bestTarget = robot;
            }
        }

        if (bestTarget != null) {
            // Spend some cheese for bonus damage (1/20 of reserves)
            int cheeseToSpend = Math.min(rc.getRawCheese() / 20, 100);
            rc.attack(bestTarget.getLocation(), cheeseToSpend);
            return true;
        }

        return false;
    }

    // =========================================================================
    // KING FORMATION STATE - Form new rat kings when economy is strong
    // =========================================================================
    private boolean shouldTryFormKing() throws GameActionException {
        // Only try if we have enough cheese
        int globalCheese = rc.getGlobalCheese();
        if (globalCheese < CHEESE_THRESHOLD_FOR_NEW_KING) return false;
        
        // 1/4 of rats are eligible to form kings (based on ID)
        // This spreads the task while ensuring enough rats can attempt it
        if ((rc.getID() % 4) != (rc.getRoundNum() / 30) % 4) return false;
        
        // Check if we already have max kings (5)
        int kingCount = countOurKings();
        if (kingCount >= 5) return false;
        
        // Higher cheese = more aggressive about forming kings
        // At 100 cheese: 20% chance per eligible rat
        // At 200+ cheese: 50% chance
        double prob = Math.min(0.5, 0.1 + (globalCheese - 50) / 300.0);
        if ((Rng.mcg() & 0x7fffffff) > (int)(prob * 0x7fffffff)) return false;
        
        return true;
    }
    
    private int countOurKings() throws GameActionException {
        int count = 0;
        for (int i = 0; i < 5; i++) {
            MapLocation kingLoc = GlobalArray.getRat(i);
            if (kingLoc != null && rc.onTheMap(kingLoc)) {
                count++;
            }
        }
        return count;
    }
    
    private MapLocation findKingFormationSpot() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        MapLocation bestSpot = null;
        int bestScore = Integer.MIN_VALUE;
        
        // Get all king locations (ours and enemy)
        MapLocation[] ourKings = new MapLocation[5];
        int numOurKings = 0;
        for (int i = 0; i < 5; i++) {
            MapLocation kingLoc = GlobalArray.getRat(i);
            if (kingLoc != null && rc.onTheMap(kingLoc)) {
                ourKings[numOurKings++] = kingLoc;
            }
        }
        
        // Estimate enemy king location
        MapLocation enemyKing = enemyKingLoc;
        if (enemyKing == null && numOurKings > 0) {
            enemyKing = guessEnemyKingLocation();
        }
        
        // Search for good spots - prioritize areas far from all kings
        // Try several candidate locations
        MapLocation[] candidates = new MapLocation[] {
            // Corners (often safe)
            new MapLocation(5, 5),
            new MapLocation(5, mapHeight - 6),
            new MapLocation(mapWidth - 6, 5),
            new MapLocation(mapWidth - 6, mapHeight - 6),
            // Mid-edges
            new MapLocation(mapWidth / 2, 5),
            new MapLocation(mapWidth / 2, mapHeight - 6),
            new MapLocation(5, mapHeight / 2),
            new MapLocation(mapWidth - 6, mapHeight / 2),
            // Current area (opportunistic if allies nearby)
            myLoc,
            // Slightly offset from current
            myLoc.translate(10, 10),
            myLoc.translate(-10, 10),
            myLoc.translate(10, -10),
            myLoc.translate(-10, -10),
        };
        
        for (MapLocation candidate : candidates) {
            // Clamp to map bounds
            int x = Math.max(2, Math.min(mapWidth - 3, candidate.x));
            int y = Math.max(2, Math.min(mapHeight - 3, candidate.y));
            MapLocation spot = new MapLocation(x, y);
            
            // Calculate minimum distance to any of our kings
            int minDistToOurKing = Integer.MAX_VALUE;
            for (int i = 0; i < numOurKings; i++) {
                int dist = spot.distanceSquaredTo(ourKings[i]);
                minDistToOurKing = Math.min(minDistToOurKing, dist);
            }
            
            // Calculate distance to enemy king
            int distToEnemy = enemyKing != null ? spot.distanceSquaredTo(enemyKing) : Integer.MAX_VALUE;
            
            // Score: maximize distance from our kings, maximize distance from enemy
            // But also factor in distance from current location (closer is better)
            int distFromMe = spot.distanceSquaredTo(myLoc);
            
            int score = 0;
            score += minDistToOurKing * 2;  // Want to be far from our kings
            score += distToEnemy;            // Want to be far from enemy
            score -= distFromMe / 2;         // Prefer closer targets
            
            // Penalty if too close to any king
            if (minDistToOurKing < MIN_DIST_FROM_KINGS_SQ) {
                score -= 10000;
            }
            if (distToEnemy < MIN_DIST_FROM_KINGS_SQ / 2) {
                score -= 5000; // Don't form right next to enemy
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestSpot = spot;
            }
        }
        
        return bestSpot != null ? bestSpot : myLoc;
    }
    
    private void runFormingKing() throws GameActionException {
        kingFormationAttempts++;
        
        // Check if we can become a king right now
        if (rc.canBecomeRatKing()) {
            rc.becomeRatKing();
            return; // This rat is now a king!
        }
        
        // Abort if taking too long or conditions changed
        if (kingFormationAttempts > 100 || rc.getGlobalCheese() < 50) {
            state = State.EXPLORE;
            kingFormationTarget = null;
            return;
        }
        
        // Count nearby allied rats
        int nearbyAllies = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(8, rc.getTeam())) {
            if (robot.getType() == UnitType.BABY_RAT) {
                nearbyAllies++;
            }
        }
        
        // If we have enough allies nearby, try to cluster
        if (nearbyAllies >= 5) {
            // We're close! Try to get allies into a 3x3
            // Just stay put and let others gather
            // Check passability of surrounding tiles
            MapLocation myLoc = rc.getLocation();
            boolean allPassable = true;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation check = myLoc.translate(dx, dy);
                    if (rc.canSenseLocation(check) && !rc.sensePassability(check)) {
                        allPassable = false;
                        break;
                    }
                }
                if (!allPassable) break;
            }
            
            if (!allPassable) {
                // Need to find a better spot with passable tiles
                kingFormationTarget = findNearbyPassableSpot();
                if (kingFormationTarget != null) {
                    Pathfinder.makeMove(kingFormationTarget);
                }
            }
            // Otherwise stay put and wait for more allies
            return;
        }
        
        // Not enough allies nearby - move toward formation target
        if (kingFormationTarget != null) {
            if (rc.getLocation().distanceSquaredTo(kingFormationTarget) <= 4) {
                // Reached target but not enough allies - try squeaking to attract allies
                // Just wait here for now
                kingFormationAttempts++;
                if (kingFormationAttempts > 50) {
                    // Taking too long, give up and explore
                    state = State.EXPLORE;
                    kingFormationTarget = null;
                }
            } else {
                Pathfinder.makeMove(kingFormationTarget);
            }
        } else {
            // No target, abort
            state = State.EXPLORE;
        }
        
        // Pick up cheese along the way
        tryPickUpCheese();
    }
    
    private MapLocation findNearbyPassableSpot() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        for (int dist = 1; dist <= 3; dist++) {
            for (Direction dir : Direction.allDirections()) {
                MapLocation check = myLoc.translate(dir.dx * dist, dir.dy * dist);
                if (!rc.canSenseLocation(check)) continue;
                
                // Check if all 9 tiles around this spot are passable
                boolean allPassable = true;
                for (int dx = -1; dx <= 1 && allPassable; dx++) {
                    for (int dy = -1; dy <= 1 && allPassable; dy++) {
                        MapLocation tile = check.translate(dx, dy);
                        if (!rc.onTheMap(tile)) {
                            allPassable = false;
                        } else if (rc.canSenseLocation(tile) && !rc.sensePassability(tile)) {
                            allPassable = false;
                        }
                    }
                }
                
                if (allPassable) return check;
            }
        }
        return null;
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================
    private void updateHome() throws GameActionException {
        // First check if we can see the king directly (most reliable)
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                home = robot.getLocation();
                return;
            }
        }

        // ALWAYS read from shared array - king updates every turn
        // This is critical for finding the king after he moves
        MapLocation bestKing = null;
        int bestDist = Integer.MAX_VALUE;
        MapLocation myLoc = rc.getLocation();
        
        for (int i = 0; i < 5; i++) {
            MapLocation kingLoc = GlobalArray.getRat(i);
            if (kingLoc != null && rc.onTheMap(kingLoc)) {
                int dist = myLoc.distanceSquaredTo(kingLoc);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestKing = kingLoc;
                }
            }
        }
        
        if (bestKing != null) {
            home = bestKing;
        }
        // If no kings found in array, keep the old home value as fallback
    }

    private MapLocation guessEnemyKingLocation() {
        if (home == null) {
            return new MapLocation(mapWidth / 2, mapHeight / 2);
        }

        return switch (CellTracker.getSymmetry()) {
            case Horizontal -> flipHor(home);
            case Vertical -> flipVer(home);
            case Rotational -> rot180(home);
            default -> rot180(home); // Default to rotational
        };
    }

    private boolean shouldGoHome() {
        int cheese = rc.getRawCheese();
        if (cheese >= GO_HOME_CHEESE_MAX) return true;
        if (cheese < GO_HOME_CHEESE_MIN) return false;

        // Calculate distance to home
        int distToHome = 30;
        if (home != null) {
            int distSq = rc.getLocation().distanceSquaredTo(home);
            distToHome = 1;
            while (distToHome * distToHome < distSq) distToHome++;
        }
        
        // Key insight: factor in TRAVEL TIME
        // If far away, need to start heading back earlier
        // Formula: go home when cheese * efficiency > threshold
        // Efficiency decreases with distance (takes time to deliver)
        int travelRounds = distToHome; // Approximate rounds to reach home
        
        // If travel time is high, lower our threshold to ensure timely delivery
        int adjustedMin = GO_HOME_CHEESE_MIN;
        int adjustedMax = GO_HOME_CHEESE_MAX;
        
        if (travelRounds > 15) {
            // Far away - be more aggressive about returning
            adjustedMin = 20;
            adjustedMax = 60;
        } else if (travelRounds > 8) {
            // Medium distance
            adjustedMin = 25;
            adjustedMax = 80;
        }
        
        if (cheese >= adjustedMax) return true;
        if (cheese < adjustedMin) return false;

        // Probabilistic return based on cheese amount
        double p = (double)(cheese - adjustedMin) / (adjustedMax - adjustedMin);
        return (Rng.mcg() & 0x7fffffff) < (int)(p * 0x7fffffff);
    }
    
    private boolean isKingCheeseUrgent() {
        // Check if global cheese is running low - king needs 2 cheese/round
        int globalCheese = rc.getGlobalCheese();
        int rawCheese = rc.getRawCheese();
        
        // If global cheese is low and we have some, rush home
        if (globalCheese < 100 && rawCheese >= 20) {
            return true;
        }
        
        // If global cheese is critically low, return with any cheese
        if (globalCheese < 30 && rawCheese > 0) {
            return true;
        }
        
        return false;
    }

    private MapLocation findVisibleMine() throws GameActionException {
        for (MapInfo info : rc.senseNearbyMapInfos()) {
            if (info.hasCheeseMine()) {
                return info.getMapLocation();
            }
        }
        return null;
    }

    private MapLocation findBestNearbyCheese() throws GameActionException {
        MapLocation best = null;
        int bestScore = Integer.MIN_VALUE;
        MapLocation myLoc = rc.getLocation();

        for (MapInfo info : rc.senseNearbyMapInfos()) {
            int amount = info.getCheeseAmount();
            if (amount <= 0) continue;

            MapLocation cheeseLoc = info.getMapLocation();
            int dist = myLoc.distanceSquaredTo(cheeseLoc);

            // Score: more cheese is better, closer is better
            int score = amount * 100 - dist;
            if (score > bestScore) {
                bestScore = score;
                best = cheeseLoc;
            }
        }
        return best;
    }

    private void tryPickUpCheese() throws GameActionException {
        for (MapInfo info : rc.senseNearbyMapInfos()) {
            if (info.getCheeseAmount() > 0) {
                MapLocation cheeseLoc = info.getMapLocation();
                if (rc.canPickUpCheese(cheeseLoc)) {
                    rc.pickUpCheese(cheeseLoc);
                    return;
                }
            }
        }
    }

    private void updateStuck() {
        MapLocation myLoc = rc.getLocation();
        if (lastLoc != null && lastLoc.equals(myLoc)) {
            stuckTurns++;
        } else {
            stuckTurns = 0;
        }
        // Update lastLoc at the END of the turn via updateStuckEnd()
    }
    
    private void updateStuckEnd() {
        lastLoc = rc.getLocation();
    }
}
