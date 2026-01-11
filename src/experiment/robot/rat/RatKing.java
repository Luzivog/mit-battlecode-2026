package experiment.robot.rat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import experiment.CellTracker;
import static experiment.Game.kingId;
import static experiment.Game.mapHeight;
import static experiment.Game.mapWidth;
import static experiment.Game.rc;
import experiment.comms.GlobalArray;
import experiment.comms.Squeaker;
import experiment.util.Rng;
import static experiment.util.Util.chebyshevDistance;

/**
 * RatKing: Spawns baby rats, moves toward cheese mines for efficiency, flees from cats.
 * Focused on backstab mode - spawns miners and rushers.
 */
public class RatKing extends Rat {
    private static final int CAT_FLEE_RADIUS_SQ = 144; // 12 tiles
    private static final int CAT_WARNING_RADIUS_SQ = 200; // React to warnings within this range
    private static final int MINE_UPDATE_INTERVAL = 10; // Check for mines frequently
    private static final int IDEAL_MINE_DIST_SQ = 16; // 4 tiles - get very close to mine

    private MapLocation mineTarget;     // Target mine location
    private MapLocation fallbackTarget; // Fallback if no mine known (corner)
    private MapLocation catLoc;
    private MapLocation lastKnownCatLoc; // Track cat even after losing sight
    private int lastMineUpdate = 0;

    @Override
    public void init() throws GameActionException {
        CellTracker.storeVisibleCells();
        fallbackTarget = findNearestCorner();
        mineTarget = null; // Will be set when we learn of mines
    }

    @Override
    public void run() throws GameActionException {
        CellTracker.storeVisibleCells();

        // Update king location in shared array
        if (kingId >= 0) {
            GlobalArray.writeRatLoc(kingId);
        }

        // Update cat info from direct sight AND from baby rat warnings
        updateCatInfo();
        
        // Periodically update our target mine location
        updateMineTarget();

        // If we have a cat warning but can't see it, turn toward it
        if (catLoc == null && lastKnownCatLoc != null) {
            turnTowardThreat(lastKnownCatLoc);
        }

        // Always try to spawn first
        trySpawnBabyRat();

        // Priority 1: Flee from cat if nearby (direct sight or recent warning)
        MapLocation threatLoc = catLoc != null ? catLoc : lastKnownCatLoc;
        if (threatLoc != null && rc.getLocation().distanceSquaredTo(threatLoc) <= CAT_FLEE_RADIUS_SQ) {
            fleeCat(threatLoc);
            return;
        }

        // Priority 2: Move toward target (mine if known, fallback otherwise)
        MapLocation target = getPositionTarget();
        if (target != null && !isNearTarget(target)) {
            moveToward(target);
            return;
        }

        // At target, shuffle a bit to avoid blocking
        shuffleNearTarget();
    }
    
    private void updateMineTarget() throws GameActionException {
        int round = rc.getRoundNum();
        
        // Update frequently to find mines quickly
        // Always update if we don't have a target yet
        if (mineTarget != null && round - lastMineUpdate < MINE_UPDATE_INTERVAL) {
            return;
        }
        lastMineUpdate = round;
        
        // Check for visible mines first (most reliable)
        MapLocation visibleMine = findVisibleMine();
        if (visibleMine != null) {
            mineTarget = visibleMine;
            return;
        }
        
        // Otherwise, use shared array to find nearest known mine
        MapLocation nearestMine = GlobalArray.getNearestCheeseMine(rc.getLocation());
        if (nearestMine != null) {
            mineTarget = nearestMine;
        }
        // If no mines known, mineTarget stays null and we use fallback
    }
    
    private MapLocation findVisibleMine() throws GameActionException {
        MapLocation best = null;
        int bestDist = Integer.MAX_VALUE;
        MapLocation myLoc = rc.getLocation();
        
        for (battlecode.common.MapInfo info : rc.senseNearbyMapInfos()) {
            if (info.hasCheeseMine()) {
                int dist = myLoc.distanceSquaredTo(info.getMapLocation());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = info.getMapLocation();
                }
            }
        }
        return best;
    }
    
    private MapLocation getPositionTarget() {
        // Prefer mine if known, otherwise use fallback (corner)
        if (mineTarget != null) {
            return mineTarget;
        }
        return fallbackTarget;
    }
    
    private boolean isNearTarget(MapLocation target) {
        if (target == null) return true;
        return rc.getLocation().distanceSquaredTo(target) <= IDEAL_MINE_DIST_SQ;
    }

    private void updateCatInfo() throws GameActionException {
        catLoc = null;
        int bestDist = Integer.MAX_VALUE;

        // Check direct sight first
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() != UnitType.CAT) continue;
            int dist = rc.getLocation().distanceSquaredTo(robot.getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                catLoc = robot.getLocation();
            }
        }

        // Update last known position if we see the cat
        if (catLoc != null) {
            lastKnownCatLoc = catLoc;
        }

        // Also check for cat warnings from baby rats
        if (catLoc == null && Squeaker.hasCatWarning()) {
            MapLocation warnedCat = Squeaker.getCatLoc();
            if (warnedCat != null) {
                int warnDist = rc.getLocation().distanceSquaredTo(warnedCat);
                if (warnDist <= CAT_WARNING_RADIUS_SQ) {
                    lastKnownCatLoc = warnedCat;
                }
            }
        }

        // Decay last known cat location after a while (clear if >20 rounds old)
        // This is handled by Squeaker.hasCatWarning() timeout
    }

    private void turnTowardThreat(MapLocation threat) throws GameActionException {
        if (threat == null) return;
        Direction toThreat = rc.getLocation().directionTo(threat);
        if (toThreat != Direction.CENTER && rc.canTurn()) {
            rc.turn(toThreat);
        }
    }

    private void fleeCat(MapLocation threat) throws GameActionException {
        if (threat == null) return;

        MapLocation myLoc = rc.getLocation();

        // Move away from cat
        Direction awayFromCat = threat.directionTo(myLoc);
        if (awayFromCat == Direction.CENTER) {
            awayFromCat = Direction.NORTH;
        }

        moveInDirection(awayFromCat);

        // Try to place multiple dirt barriers
        tryPlaceDirtBarriers(threat);
    }

    private void tryPlaceDirtBarriers(MapLocation threat) throws GameActionException {
        if (threat == null) return;

        MapLocation myLoc = rc.getLocation();
        Direction toCat = myLoc.directionTo(threat);
        if (toCat == Direction.CENTER) return;

        // Place dirt in multiple positions to create a wall
        Direction[] spreadDirs = {toCat, toCat.rotateLeft(), toCat.rotateRight()};

        for (int dist = 2; dist <= 5; dist++) {
            for (Direction spread : spreadDirs) {
                if (!rc.isActionReady()) return;

                MapLocation candidate = myLoc.translate(spread.dx * dist, spread.dy * dist);
                if (rc.canPlaceDirt(candidate)) {
                    rc.placeDirt(candidate);
                    // Don't return - try to place more dirt
                }
            }
        }
    }

    private void moveToward(MapLocation target) throws GameActionException {
        if (target == null) return;
        Direction dir = rc.getLocation().directionTo(target);
        if (dir == Direction.CENTER) return;
        moveInDirection(dir);
    }

    private void moveInDirection(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER) return;

        // Turn toward direction first
        if (rc.canTurn()) {
            rc.turn(dir);
        }

        // Try to move in preferred direction, then adjacent directions
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(),
                            dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};

        for (Direction d : toTry) {
            if (rc.canMove(d)) {
                rc.move(d);
                return;
            }
        }
    }

    private void shuffleNearTarget() throws GameActionException {
        // Small random movement near target position
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
                           Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST};
        int idx = (Rng.mcg() & 0x7fffffff) % dirs.length;

        for (int i = 0; i < dirs.length; i++) {
            Direction d = dirs[(idx + i) % dirs.length];
            if (rc.canMove(d)) {
                rc.move(d);
                return;
            }
        }
    }

    private void trySpawnBabyRat() throws GameActionException {
        int cost = rc.getCurrentRatCost();
        int cheese = rc.getGlobalCheese();

        // Be more aggressive about spawning early game for economy
        int round = rc.getRoundNum();
        int minCheese = cost * 2;
        
        // Early game: spawn more aggressively for economy
        if (round < 100) {
            minCheese = cost + 50; // Lower threshold early
        } else if (round < 300) {
            minCheese = (int)(cost * 1.5);
        }
        
        if (cheese < minCheese) return;
        if (!rc.isActionReady()) return;

        // Dynamic role assignment based on game phase
        // Early game: 80% miners for economy, 20% rushers for scouting
        // Mid game: 60% miners, 40% rushers
        // Late game: 50% miners, 50% rushers
        double rusherChance;
        if (round < 150) {
            rusherChance = 0.2; // Early: focus on economy
        } else if (round < 400) {
            rusherChance = 0.35; // Mid: balanced
        } else {
            rusherChance = 0.45; // Late: more aggression
        }
        
        boolean spawnRusher = (Rng.mcg() & 0x7fffffff) < (int)(rusherChance * 0x7fffffff);
        int role = spawnRusher ? BabyRatRole.RUSH.value : BabyRatRole.MINER.value;

        // Spawn AWAY from corner (toward center) so rats don't block king's path
        // Spread spawns in different directions to help exploration
        MapLocation center = new MapLocation(mapWidth / 2, mapHeight / 2);
        MapLocation spawnLoc = findSpawnLocation(center);

        if (spawnLoc != null && rc.canBuildRat(spawnLoc)) {
            if (kingId >= 0) {
                GlobalArray.writeSpawnRole(kingId, role, rc.getRoundNum());
            }
            rc.buildRat(spawnLoc);
        }
    }

    private MapLocation findSpawnLocation(MapLocation bias) throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        MapLocation best = null;
        int bestDist = Integer.MAX_VALUE;
        
        // If we know of a mine, bias spawning toward it for faster cheese collection
        MapLocation spawnBias = bias;
        if (mineTarget != null) {
            spawnBias = mineTarget;
        }

        // Spawn adjacent to king, preferring direction toward bias (mine or center)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;

                MapLocation candidate = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if (!rc.onTheMap(candidate)) continue;
                if (!rc.canBuildRat(candidate)) continue;

                // Prefer spawning toward mine/center
                int dist = candidate.distanceSquaredTo(spawnBias);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private MapLocation findNearestCorner() {
        int inset = 3;

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
}
