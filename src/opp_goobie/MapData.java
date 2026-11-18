package opp_goobie;

import java.util.HashMap;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import opp_goobie.Fast.FastIterableLocSet;

public class MapData {
    final static int SEEN_BIT = 1;
    final static int CHEESE_MINE_BIT = 2;
    final static int WALL_BIT = 4;
    final static int DIRT_BIT = 8;

    static int[][] map;

    // Cache of known cheese mines - avoids O(W*H) scan
    // Uses FastIterableLocSet for efficient iteration over MapLocations
    static final FastIterableLocSet knownCheeseMines = new FastIterableLocSet(100);
    // cheese mines that get blacklisted, so we don't keep trying to explore to them
    static final HashMap<MapLocation, Integer> exploreBlacklistCheeseMines = new HashMap<MapLocation, Integer>();

    // Cheese mines on "our side" vs "enemy side" of symmetry line
    private static final FastIterableLocSet friendlySidedCheeseMines = new FastIterableLocSet(100);
    private static final FastIterableLocSet enemySidedCheeseMines = new FastIterableLocSet(100);

    public static void init() {
        map = new int[Robot.W][Robot.H];
    }

    public static void updateMapInfos() throws GameActionException {
        MapInfo[] infos = Robot.rc.senseNearbyMapInfos();

        for (MapInfo info : infos) {
            MapLocation loc = info.getMapLocation();
            int x = loc.x;
            int y = loc.y;

            if (info.hasCheeseMine()) {
                if (!hasSeen(loc) && Util.chebyshevDistanceTo(info.getMapLocation(), Robot.rc.getLocation()) > 3) {
                    Direction dirToInfo = Robot.rc.getLocation().directionTo(loc);
//                    if (Robot.rc.canTurn(dirToInfo))
//                        Robot.rc.turn(dirToInfo);
                    BugNav.moveTo(Robot.rc.getLocation().add(dirToInfo).add(dirToInfo));
                }

                if (Util.chebyshevDistanceTo(info.getMapLocation(), Robot.rc.getLocation()) <= 3) {
                    // Check seen bit and add to buffer BEFORE setting seen bit
                    if (!hasSeen(loc)) {
                        Debug.addIndicatorString("POI: " + info.getMapLocation());
                        CommsSqueak.addMineToPendingBuffer(loc);
                    }

                    addCheeseMineToCache(loc);

                    MapLocation symLoc = null;
                    // add symmetric loc too if we know symmetry locally
                    if (SymmetryManager.isSymmetryDetermined()) {
                        symLoc = SymmetryManager.getSymmetric(loc);
                        if (symLoc != null && !hasCheeseMine(symLoc)) {
                            addCheeseMineToCache(symLoc);
                        }
                    }

                    // Populate friendly/enemy sided structures if symmetry is known globally
                    if (SymmetryManager.isSymmetryDetermined() && CommsArray.isSymmetryGloballyKnown()
                        && CommsArray.isCheeseMinesRewritten()) {
                        CommsArray.addToSidedCheeseMines(loc);
                        if (symLoc != null) {
                            CommsArray.addToSidedCheeseMines(symLoc);
                        }
                    }

                    // Add symmetric location to buffer if first time seeing original location
                    if (!hasSeen(loc)) {
                        CommsSqueak.addMineToPendingBuffer(symLoc); // TODO: determine if we should do this or not
                    }

                    // Rat king writes to shared array
                    if (Robot.rc.getType().isRatKingType()) {
                        if (SymmetryManager.isSymmetryDetermined() && CommsArray.isSymmetryGloballyKnown()
                            && !CommsArray.isCheeseMinesRewritten()) {
                            if (symLoc != null) {
                                if (!CommsArray.isLocationInSharedArray(loc)) {
                                    CommsArray.writeCheeseMineToSharedArray(loc);
                                }
                                if (!CommsArray.isLocationInSharedArray(symLoc)) {
                                    CommsArray.writeCheeseMineToSharedArray(symLoc);
                                }
                            } else {
                                if (!CommsArray.isLocationInSharedArray(loc)) {
                                    CommsArray.writeCheeseMineToSharedArray(loc);
                                }
                            }
                        } else {
                            if (!CommsArray.isLocationInSharedArray(loc)) {
                                CommsArray.writeCheeseMineToSharedArray(loc);
                            }
                        }
                    }

                    // Set seen bit AFTER processing cheese mine (so addMineToPendingBuffer can
                    // check it)
                    map[x][y] |= SEEN_BIT;
                }
            } else {
                map[x][y] |= SEEN_BIT;
            }

            if (info.isWall()) {
                map[x][y] |= WALL_BIT;
            }

            int d = map[x][y] & DIRT_BIT;
            if (info.isDirt()) {
                if (d == 0) {
                    map[x][y] |= DIRT_BIT;
                }
            } else {
                if (d != 0) {
                    map[x][y] &= ~DIRT_BIT;
                }
            }
        }
    }

    public static boolean hasSeen(MapLocation loc) {
        return (map[loc.x][loc.y] & SEEN_BIT) != 0;
    }

    public static boolean hasCheeseMine(MapLocation loc) {
        return (map[loc.x][loc.y] & CHEESE_MINE_BIT) != 0;
    }

    public static int getKnownCheeseMinesCount() {
        return knownCheeseMines.size;
    }

    public static void addCheeseMineToCache(MapLocation loc) {
        int x = loc.x;
        int y = loc.y;

        // Bounds check to prevent ArrayIndexOutOfBoundsException
        if (x < 0 || x >= map.length || y < 0 || y >= map[0].length) {
            Debug.println("ERROR: Invalid location coordinates: " + loc + " (map size: " + map.length + "x"
                + (map.length > 0 ? map[0].length : 0) + ")");
            return;
        }

        if ((map[x][y] & CHEESE_MINE_BIT) == 0) {
            map[x][y] |= CHEESE_MINE_BIT | SEEN_BIT;
            // Add to FastIterableLocSet cache
            knownCheeseMines.add(loc);
            knownCheeseMines.updateIterable();
        }
    }

    public static MapLocation getClosestCheeseMine(MapLocation from) {
        if (knownCheeseMines.size == 0)
            return null;

        MapLocation closest = null;
        int minDist = Integer.MAX_VALUE;

        // Iterate through the MapLocation array (fast, no decoding needed)
        for (int i = 0; i < knownCheeseMines.size; i++) {
            MapLocation mineLoc = knownCheeseMines.locs[i];
            if (mineLoc == null)
                continue; // Skip null entries

            int dist = Util.chebyshevDistanceTo(from, mineLoc);
            if (dist < minDist) {
                minDist = dist;
                closest = mineLoc;
            }
        }

        return closest;
    }

    public static MapLocation getCheeseMineRally(MapLocation from) throws GameActionException {
        if (knownCheeseMines.size == 0)
            return null;

        MapLocation[] kings = CommsArray.getRatKings();

        MapLocation closest = null;
        int minDist = Constants.INF;

        for (int i = 0; i < knownCheeseMines.size; i++) {
            MapLocation mineLoc = knownCheeseMines.locs[i];
            if (mineLoc == null)
                continue;

            if (hasRatKing(mineLoc, kings))
                continue;

            int dist = Util.chebyshevDistanceTo(from, mineLoc);
            if (dist < minDist) {
                minDist = dist;
                closest = mineLoc;
            }
        }

        return closest;
    }

    static boolean hasRatKing(MapLocation mine, MapLocation[] kings) {
        if (kings == null)
            return false;

        for (int i = 0; i < kings.length; i++) {
            MapLocation king = kings[i];
            if (king == null)
                continue;

            if (king.distanceSquaredTo(mine) <= 8)
                return true;
            // if (king.isAdjacentTo(mine))
            // return true;
        }
        return false;
    }

    public static MapLocation getClosestNonBlacklistedCheeseMine(MapLocation from) {
        if (knownCheeseMines.size == 0)
            return null;

        int currentRound = Robot.rc.getRoundNum();
        MapLocation closest = null;
        int minDist = Integer.MAX_VALUE;

        // if we got TONS of baby rats, half chance u try exploiting only enemy sided cheese mine
        FastIterableLocSet cheeseMineList = knownCheeseMines;
        if (Robot.estimatedMinRats > 24 && Robot.RNG.nextBoolean())
            cheeseMineList = MapData.enemySidedCheeseMines;

        for (int i = 0; i < cheeseMineList.size; i++) {
            MapLocation mineLoc = cheeseMineList.locs[i];
            if (mineLoc == null)
                continue;

            if (!hasCheeseMine(mineLoc)) {
                Debug.println("ERROR: Location " + mineLoc + " in cheeseMineList but hasCheeseMine() returns false!");
                MapData.addCheeseMineToCache(mineLoc);
                continue;
            }

            Integer blacklistUntilRound = exploreBlacklistCheeseMines.get(mineLoc);
            if (blacklistUntilRound != null) {
                if (currentRound >= blacklistUntilRound) {
                    exploreBlacklistCheeseMines.remove(mineLoc);
                } else {
                    continue;
                }
            }

            int dist = Util.chebyshevDistanceTo(from, mineLoc);
            if (dist < minDist) {
                minDist = dist;
                closest = mineLoc;
            }
        }

        // All known mines are blacklisted -> clear blacklist and choose again
        if (closest == null && knownCheeseMines.size > 0 && !exploreBlacklistCheeseMines.isEmpty()) {
            exploreBlacklistCheeseMines.clear();
            closest = null;
            minDist = Integer.MAX_VALUE;
            for (int i = 0; i < knownCheeseMines.size; i++) {
                MapLocation mineLoc = knownCheeseMines.locs[i];
                if (mineLoc == null || !hasCheeseMine(mineLoc))
                    continue;
                int dist = Util.chebyshevDistanceTo(from, mineLoc);
                if (dist < minDist) {
                    minDist = dist;
                    closest = mineLoc;
                }
            }
        }

        // if (closest != null) {
        // if (!hasCheeseMine(closest)) {
        // Debug.println("ERROR: getClosestNonBlacklistedCheeseMine returning " +
        // closest
        // + " but hasCheeseMine() returns false!");
        // }
        // }

        return closest;
    }

    public static void blacklistCheeseMineForExploration(MapLocation loc) throws GameActionException {
        int baseCooldown = Constants.CHEESE_MINE_BLACKLIST_COOLDOWN;
        int cooldown = Constants.CHEESE_MINE_BLACKLIST_COOLDOWN;

        if (Robot.rc.getType().isBabyRatType()) {
            MapLocation closestRatKing = CommsArray.getClosestRatKing(loc);
            if (closestRatKing != null) {
                int distance = Util.chebyshevDistanceTo(loc, closestRatKing);

                int maxDistance = Robot.W + Robot.H;
                cooldown = baseCooldown + (baseCooldown * (maxDistance - distance)) / maxDistance;
            }
        } else {
            cooldown = 300;
        }

        int blacklistUntilRound = Robot.rc.getRoundNum() + cooldown;
        exploreBlacklistCheeseMines.put(loc, blacklistUntilRound);
    }

    public static void addFriendlySidedCheeseMine(MapLocation loc) {
        if (loc != null) {
            friendlySidedCheeseMines.add(loc);
            friendlySidedCheeseMines.updateIterable();
        }
    }

    public static void addEnemySidedCheeseMine(MapLocation loc) {
        if (loc != null) {
            enemySidedCheeseMines.add(loc);
            enemySidedCheeseMines.updateIterable();
        }
    }

    public static void clearSidedCheeseMines() {
        friendlySidedCheeseMines.clear();
        enemySidedCheeseMines.clear();
    }

    /**
     * Get the furthest friendly-sided cheese mine that isn't near any rat king.
     * Used for rally point selection - we want to expand to the furthest available mine on our side.
     */
    public static MapLocation getFurthestFriendlyCheeseMineForRally(MapLocation from, MapLocation[] kings) {
        MapLocation furthest = null;
        int maxDist = -1;

//        // If sided cheese mines have been populated, use them
//        if (friendlySidedCheeseMines.size > 0) {
//            for (int i = 0; i < friendlySidedCheeseMines.size; i++) {
//                MapLocation mineLoc = friendlySidedCheeseMines.locs[i];
//                if (mineLoc == null)
//                    continue;
//
//                // Skip if a king already claims this mine
//                if (hasRatKing(mineLoc, kings))
//                    continue;
//
//                int dist = Util.chebyshevDistanceTo(from, mineLoc);
//                if (dist > maxDist) {
//                    maxDist = dist;
//                    furthest = mineLoc;
//                }
//            }
//            return furthest;
//        }

        // Fallback: use knownCheeseMines, find first mine at least 15 tiles away
        for (int i = 0; i < knownCheeseMines.size; i++) {
            MapLocation mineLoc = knownCheeseMines.locs[i];
            if (mineLoc == null)
                continue;

            // Skip if a king already claims this mine
            if (hasRatKing(mineLoc, kings))
                continue;

            int dist = Util.chebyshevDistanceTo(from, mineLoc);
            if (dist >= 15) {
                return mineLoc;
            }
        }

        return null;
    }

    /**
     * Get the cheese mine closest to the center of the map from the given list.
     * Used for single rat king strategy to control the most central mine.
     */
    public static MapLocation getCheeseMineClosestToCenter(FastIterableLocSet mineList) {
        MapLocation center = Robot.centerLoc;
        MapLocation closest = null;
        int minDist = Constants.INF;

        for (int i = 0; i < mineList.size; i++) {
            MapLocation mineLoc = mineList.locs[i];
            if (mineLoc == null)
                continue;

            // Skip blacklisted mines
            if (exploreBlacklistCheeseMines.containsKey(mineLoc)
                && exploreBlacklistCheeseMines.get(mineLoc) > Robot.rc.getRoundNum()) {
                continue;
            }

            int dist = Util.chebyshevDistanceTo(center, mineLoc);
            if (dist < minDist) {
                minDist = dist;
                closest = mineLoc;
            }
        }

        return closest;
    }

    public static FastIterableLocSet getFriendlySidedCheeseMines() {
        return friendlySidedCheeseMines;
    }

    public static FastIterableLocSet getKnownCheeseMines() {
        return knownCheeseMines;
    }

    public static int getFriendlySidedCheeseMinesCount() {
        return friendlySidedCheeseMines.size;
    }

    /**
     * Find a friendly-sided cheese mine that is at least minDist tiles away from ALL rat kings.
     * Returns null if no such mine exists.
     */
    public static MapLocation getFriendlyMineAtLeastXFromKings(MapLocation[] kings, int minChebDist) {
        // First try friendlySidedCheeseMines if populated
        if (friendlySidedCheeseMines.size > 0) {
            for (int i = 0; i < friendlySidedCheeseMines.size; i++) {
                MapLocation mineLoc = friendlySidedCheeseMines.locs[i];
                if (mineLoc == null)
                    continue;

                if (isAtLeastXFromAllKings(mineLoc, kings, minChebDist)) {
                    return mineLoc;
                }
            }
            return null;
        }

        // Fallback: check knownCheeseMines with isOnOurSide
        if (SymmetryManager.isSymmetryDetermined()) {
            for (int i = 0; i < knownCheeseMines.size; i++) {
                MapLocation mineLoc = knownCheeseMines.locs[i];
                if (mineLoc == null)
                    continue;

                if (!CommsArray.isOnOurSide(mineLoc))
                    continue;

                if (isAtLeastXFromAllKings(mineLoc, kings, minChebDist)) {
                    return mineLoc;
                }
            }
        }

        return null;
    }

    /**
     * Check if a location is at least minDist tiles (chebyshev) away from ALL rat kings.
     */
    private static boolean isAtLeastXFromAllKings(MapLocation loc, MapLocation[] kings, int minChebDist) {
        if (kings == null)
            return true;

        for (int i = 0; i < kings.length; i++) {
            MapLocation king = kings[i];
            if (king == null)
                continue;

            int dist = Util.chebyshevDistanceTo(loc, king);
            if (dist < minChebDist) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find a cheese mine in the opposite friendly quadrant from the given king location.
     * This helps spread out rat kings across our side of the map.
     * <p>
     * For H symmetry (left/right): our side has top and bottom halves
     * For V symmetry (top/bottom): our side has left and right halves
     * For R symmetry: use diagonal quadrants
     */
    public static MapLocation getMineInOppositeFriendlyQuadrant(MapLocation kingLoc, MapLocation[] kings) {
        if (!SymmetryManager.isSymmetryDetermined())
            return null;

        int midX = Robot.W / 2;
        int midY = Robot.H / 2;
        int sym = SymmetryManager.getSym();

        // Determine which sub-quadrant the king is in within our friendly side
        boolean kingInTopHalf = kingLoc.y >= midY;
        boolean kingInLeftHalf = kingLoc.x < midX;

        MapLocation best = null;
        int bestDist = -1;

        // Check friendlySidedCheeseMines first
        if (friendlySidedCheeseMines.size > 0) {
            for (int i = 0; i < friendlySidedCheeseMines.size; i++) {
                MapLocation mine = friendlySidedCheeseMines.locs[i];
                if (mine == null) continue;

                // Skip if a king is near this mine
                if (hasRatKing(mine, kings)) continue;

                // Check if mine is in the OPPOSITE sub-quadrant within our friendly side
                boolean mineInTopHalf = mine.y >= midY;
                boolean mineInLeftHalf = mine.x < midX;

                boolean inOppositeQuadrant = false;
                if (sym == SymmetryManager.H) {
                    // H symmetry: our side is left or right, sub-quadrants are top/bottom
                    inOppositeQuadrant = (mineInTopHalf != kingInTopHalf);
                } else if (sym == SymmetryManager.V) {
                    // V symmetry: our side is top or bottom, sub-quadrants are left/right
                    inOppositeQuadrant = (mineInLeftHalf != kingInLeftHalf);
                } else if (sym == SymmetryManager.R) {
                    // R symmetry: diagonal quadrants, opposite means different in both dimensions
                    inOppositeQuadrant = (mineInTopHalf != kingInTopHalf) && (mineInLeftHalf != kingInLeftHalf);
                }

                if (inOppositeQuadrant) {
                    int dist = Util.chebyshevDistanceTo(kingLoc, mine);
                    if (dist > bestDist) {
                        bestDist = dist;
                        best = mine;
                    }
                }
            }
            return best;
        }

        // Fallback: check knownCheeseMines with isOnOurSide
        for (int i = 0; i < knownCheeseMines.size; i++) {
            MapLocation mine = knownCheeseMines.locs[i];
            if (mine == null) continue;

            if (!CommsArray.isOnOurSide(mine)) continue;
            if (hasRatKing(mine, kings)) continue;

            boolean mineInTopHalf = mine.y >= midY;
            boolean mineInLeftHalf = mine.x < midX;

            boolean inOppositeQuadrant = false;
            if (sym == SymmetryManager.H) {
                inOppositeQuadrant = (mineInTopHalf != kingInTopHalf);
            } else if (sym == SymmetryManager.V) {
                inOppositeQuadrant = (mineInLeftHalf != kingInLeftHalf);
            } else if (sym == SymmetryManager.R) {
                inOppositeQuadrant = (mineInTopHalf != kingInTopHalf) && (mineInLeftHalf != kingInLeftHalf);
            }

            if (inOppositeQuadrant) {
                int dist = Util.chebyshevDistanceTo(kingLoc, mine);
                if (dist > bestDist) {
                    bestDist = dist;
                    best = mine;
                }
            }
        }

        return best;
    }

    public static boolean isWall(MapLocation loc) {
        int x = loc.x, y = loc.y;
        if (x < 0 || x >= map.length || y < 0 || y >= map[0].length) {
            return false;
        }
        return (map[x][y] & WALL_BIT) != 0;
    }

    public static boolean isWall(int x, int y) {
        if (x < 0 || x >= map.length || y < 0 || y >= map[0].length) {
            return false;
        }
        return (map[x][y] & WALL_BIT) != 0;
    }

    public static boolean isDirt(MapLocation loc) {
        int x = loc.x, y = loc.y;
        if (x < 0 || x >= map.length || y < 0 || y >= map[0].length) {
            return false;
        }
        return (map[x][y] & DIRT_BIT) != 0;
    }

    public static boolean isDirt(int x, int y) {
        if (x < 0 || x >= map.length || y < 0 || y >= map[0].length) {
            return false;
        }
        return (map[x][y] & DIRT_BIT) != 0;
    }

    public static boolean isPassable(MapLocation loc) {
        if (loc == null)
            return false;
        return !isWall(loc) && !isDirt(loc);
    }

    public static boolean isPassable(int x, int y) {
        return !isWall(x, y) && !isDirt(x, y);
    }

    static MapLocation getClosestWall(MapLocation c) {
        int x = c.x;
        int y = c.y;

        MapLocation best = null;
        int bestDist = Constants.INF;

        if (y - 1 >= 0 && (map[x][y - 1] & (WALL_BIT | DIRT_BIT)) != 0) {
            best = new MapLocation(x, y - 1);
            bestDist = 1;
        }
        if (y - 2 >= 0 && (map[x][y - 2] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x, y - 2);
            bestDist = 2;
        }
        if (y - 3 >= 0 && (map[x][y - 3] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x, y - 3);
            bestDist = 3;
        }

        if (x + 1 < Robot.W && y - 1 >= 0 &&
            (map[x + 1][y - 1] & (WALL_BIT | DIRT_BIT)) != 0 && 1 < bestDist) {
            best = new MapLocation(x + 1, y - 1);
            bestDist = 1;
        }
        if (x + 2 < Robot.W && y - 2 >= 0 &&
            (map[x + 2][y - 2] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x + 2, y - 2);
            bestDist = 2;
        }
        if (x + 3 < Robot.W && y - 3 >= 0 &&
            (map[x + 3][y - 3] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x + 3, y - 3);
            bestDist = 3;
        }

        if (x + 1 < Robot.W && (map[x + 1][y] & (WALL_BIT | DIRT_BIT)) != 0 && 1 < bestDist) {
            best = new MapLocation(x + 1, y);
            bestDist = 1;
        }
        if (x + 2 < Robot.W && (map[x + 2][y] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x + 2, y);
            bestDist = 2;
        }
        if (x + 3 < Robot.W && (map[x + 3][y] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x + 3, y);
            bestDist = 3;
        }

        if (x + 1 < Robot.W && y + 1 < Robot.H &&
            (map[x + 1][y + 1] & (WALL_BIT | DIRT_BIT)) != 0 && 1 < bestDist) {
            best = new MapLocation(x + 1, y + 1);
            bestDist = 1;
        }
        if (x + 2 < Robot.W && y + 2 < Robot.H &&
            (map[x + 2][y + 2] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x + 2, y + 2);
            bestDist = 2;
        }
        if (x + 3 < Robot.W && y + 3 < Robot.H &&
            (map[x + 3][y + 3] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x + 3, y + 3);
            bestDist = 3;
        }

        if (y + 1 < Robot.H && (map[x][y + 1] & (WALL_BIT | DIRT_BIT)) != 0 && 1 < bestDist) {
            best = new MapLocation(x, y + 1);
            bestDist = 1;
        }
        if (y + 2 < Robot.H && (map[x][y + 2] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x, y + 2);
            bestDist = 2;
        }
        if (y + 3 < Robot.H && (map[x][y + 3] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x, y + 3);
            bestDist = 3;
        }

        if (x - 1 >= 0 && y + 1 < Robot.H &&
            (map[x - 1][y + 1] & (WALL_BIT | DIRT_BIT)) != 0 && 1 < bestDist) {
            best = new MapLocation(x - 1, y + 1);
            bestDist = 1;
        }
        if (x - 2 >= 0 && y + 2 < Robot.H &&
            (map[x - 2][y + 2] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x - 2, y + 2);
            bestDist = 2;
        }
        if (x - 3 >= 0 && y + 3 < Robot.H &&
            (map[x - 3][y + 3] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x - 3, y + 3);
            bestDist = 3;
        }

        if (x - 1 >= 0 && (map[x - 1][y] & (WALL_BIT | DIRT_BIT)) != 0 && 1 < bestDist) {
            best = new MapLocation(x - 1, y);
            bestDist = 1;
        }
        if (x - 2 >= 0 && (map[x - 2][y] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x - 2, y);
            bestDist = 2;
        }
        if (x - 3 >= 0 && (map[x - 3][y] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x - 3, y);
            bestDist = 3;
        }

        if (x - 1 >= 0 && y - 1 >= 0 &&
            (map[x - 1][y - 1] & (WALL_BIT | DIRT_BIT)) != 0 && 1 < bestDist) {
            best = new MapLocation(x - 1, y - 1);
            bestDist = 1;
        }
        if (x - 2 >= 0 && y - 2 >= 0 &&
            (map[x - 2][y - 2] & (WALL_BIT | DIRT_BIT)) != 0 && 2 < bestDist) {
            best = new MapLocation(x - 2, y - 2);
            bestDist = 2;
        }
        if (x - 3 >= 0 && y - 3 >= 0 &&
            (map[x - 3][y - 3] & (WALL_BIT | DIRT_BIT)) != 0 && 3 < bestDist) {
            best = new MapLocation(x - 3, y - 3);
            bestDist = 3;
        }

        return best;
    }
}
