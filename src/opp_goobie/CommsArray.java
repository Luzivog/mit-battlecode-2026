package opp_goobie;

import battlecode.common.*;
import opp_goobie.Fast.FastIntSet;

/**
 * Memory Layout:
 * [0-84] : 5 Rat King locations (12 bits location + 4 bits health [0-500
 * scaled] + 1 bit alive each = 85 bits)
 * [85-87] : Symmetry (3 bits) (one bit per symmetry type disqualified)
 * [88] : Cheese mines rewritten flag (1 bit)
 * [89-100] : Formation rally point (12 bits location)
 * [101-628]: Cheese mine locations (44 × 12 bits each)
 */
public class CommsArray {
    private static final int TOTAL_BITS = 10 * GameConstants.SHARED_ARRAY_SIZE;
    private static final int LOCATION_BITS = 12;
    private static final int SYMMETRY_BITS = 3;
    private static final int REWRITTEN_FLAG_BITS = 1;

    private static final int RAT_KING_HEALTH_BITS = 4;
    private static final int RAT_KING_MAX_HEALTH = UnitType.RAT_KING.getHealth();
    private static final int RAT_KING_ALIVE_BIT = 1;
    private static final int RAT_KING_LOCATION_BITS = LOCATION_BITS + RAT_KING_HEALTH_BITS + RAT_KING_ALIVE_BIT;
    private static final int RAT_KING_SECTION_END = GameConstants.MAX_NUMBER_OF_RAT_KINGS * RAT_KING_LOCATION_BITS;
    private static final int SYMMETRY_SECTION_END = RAT_KING_SECTION_END + SYMMETRY_BITS;
    private static final int REWRITTEN_FLAG_SECTION_END = SYMMETRY_SECTION_END + REWRITTEN_FLAG_BITS;
    private static final int FORMATION_RALLY_POINT_OFFSET = REWRITTEN_FLAG_SECTION_END; // [89-100], 12 bits
    private static final int CHEESE_MINE_SECTION_START = REWRITTEN_FLAG_SECTION_END + LOCATION_BITS; // [101-...]
    private static final int CHEESE_MINE_LOCATION_BITS = LOCATION_BITS;
    private static final int MAX_CHEESE_MINE_LOCATIONS = (TOTAL_BITS - CHEESE_MINE_SECTION_START)
        / CHEESE_MINE_LOCATION_BITS;

    public static int ratKingIndex = -1;
    public static int livingRatKingCount = 0;

    // Track which side of the map we spawned on (determined by first rat king position)
    private static Boolean weAreOnLeftSide = null;  // For H symmetry
    private static Boolean weAreOnBottomSide = null; // For V symmetry
    private static Boolean weAreOnLeftOrBottomSide = null; // For R symmetry

    private static int cheeseMineInitProgress = 0;
    private static boolean isFirstRatKing = false;
    private static final int[] previousAliveBits = new int[]{-1, -1, -1, -1, -1};
    private static int cheeseMineReadingPointer = 0;
    private static final FastIntSet locationsInSharedArray = new FastIntSet();
    private static boolean isRewritingSharedArray = false;
    private static int rewritingReadPointer = 0;
    private static int rewritingWritePointer = 0;
    private static boolean shouldPrintRewrittenCheeseMines = false;
    private static boolean needsRereadAfterRewrite = false;
    private static boolean hasResetPointerForRewrite = false;
    private static Boolean cachedIsCheeseMinesRewritten = null;

    // region ============= INITIALIZATION =============

    public static void init() throws GameActionException {
        if (Robot.rc.getType().isRatKingType()) {
            assignRatKingIndex();
            // Determine which side of the map we're on based on our spawn position
            initializeOurSide();
        }
    }

    /**
     * Initialize which side of the map we're on based on a rat king's position.
     * For rat kings, uses own position. For baby rats, uses first rat king from shared array.
     */
    private static void initializeOurSide() {
        if (weAreOnLeftSide != null) return; // Already initialized

        MapLocation refLoc = null;

        if (Robot.rc.getType().isRatKingType()) {
            // Rat king uses its own position
            refLoc = Robot.rc.getLocation();
        } else {
            // Baby rat: look up first rat king from shared array
            try {
                refLoc = readRatKingLoc(0);
                if (refLoc == null) {
                    // Try other indices
                    for (int i = 1; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
                        refLoc = readRatKingLoc(i);
                        if (refLoc != null) break;
                    }
                }
            } catch (GameActionException e) {
                // Fall back to own position
                refLoc = Robot.rc.getLocation();
            }
        }

        if (refLoc == null) {
            // Fallback: use own position (shouldn't happen normally)
            refLoc = Robot.rc.getLocation();
        }

        int midX = Robot.W / 2;
        int midY = Robot.H / 2;

        weAreOnLeftSide = refLoc.x < midX;
        weAreOnBottomSide = refLoc.y < midY;
        weAreOnLeftOrBottomSide = (refLoc.x < midX) || (refLoc.x == midX && refLoc.y < midY);
    }

    private static void assignRatKingIndex() throws GameActionException {
        int currentRound = Robot.rc.getRoundNum();

        if (currentRound == 1) {
            ratKingIndex = 0;
            isFirstRatKing = true;
            cheeseMineInitProgress = 0;
            writeRatKingLocHealthAndFlipAlive(-1, Robot.rc.getHealth());
            for (int j = 1; j < GameConstants.MAX_NUMBER_OF_RAT_KINGS; j++) {
                clearRatKingSlot(j);
            }
            return;
        }

        int firstEmptyIndex = -1;
        for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
            int bitOffset = i * RAT_KING_LOCATION_BITS;
            int combined = readBits(bitOffset, RAT_KING_LOCATION_BITS);
            boolean isLocEmpty = (combined >> (RAT_KING_HEALTH_BITS + RAT_KING_ALIVE_BIT)) == Util.NULL_LOCATION_INT;
            if (isLocEmpty) {
                firstEmptyIndex = i;
                break;
            }
        }

        if (Debug.FAIL_FAST && firstEmptyIndex == -1) {
            throw new IllegalStateException("rat kings should ALWAYS be able to find an empty rat king slot");
        }

        ratKingIndex = firstEmptyIndex;
        cheeseMineInitProgress = -1;
        writeRatKingLocHealthAndFlipAlive(-1, Robot.rc.getHealth());
    }

    public static void initCheeseMines(int maxBytecode) throws GameActionException {
        if (!isFirstRatKing || cheeseMineInitProgress < 0) {
            return;
        }

        int startBytecode = Clock.getBytecodeNum();

        if (cheeseMineInitProgress == 0) {
            clearFormationRallyPoint();
        }

        while (cheeseMineInitProgress < MAX_CHEESE_MINE_LOCATIONS) {
            if (Clock.getBytecodeNum() - startBytecode > maxBytecode - 500) {
                return;
            }

            clearCheeseMineLoc(cheeseMineInitProgress);
            cheeseMineInitProgress++;
        }

        cheeseMineInitProgress = -1;
        // Debug.println("Cheese mine initialization complete");
    }

    public static boolean isCheeseMinesInitialized() {
        if (ratKingIndex == 0 && isFirstRatKing) {
            return cheeseMineInitProgress < 0;
        }
        return true;
    }
    // endregion

    // region ============= RAT KING LOCATIONS =============
    // Bit layout: [location (12 bits)] [health (4 bits, 0-500 scaled)] [alive (1
    // bit)]

    public static int readRatKingHealth(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.MAX_NUMBER_OF_RAT_KINGS)
            return -1;

        int bitOffset = index * RAT_KING_LOCATION_BITS;
        int combined = readBits(bitOffset, RAT_KING_LOCATION_BITS);
        int healthValue = (combined >> RAT_KING_ALIVE_BIT) & ((1 << RAT_KING_HEALTH_BITS) - 1);
        return (healthValue * RAT_KING_MAX_HEALTH) / ((1 << RAT_KING_HEALTH_BITS) - 1);
    }

    public static boolean readRatKingAlive(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.MAX_NUMBER_OF_RAT_KINGS)
            return false;

        int bitOffset = index * RAT_KING_LOCATION_BITS;
        int combined = readBits(bitOffset, RAT_KING_LOCATION_BITS);
        return (combined & RAT_KING_ALIVE_BIT) != 0;
    }

    public static int writeRatKingLocHealthAndFlipAlive(int prevAliveBit, int health) throws GameActionException {
        if (!Robot.rc.getType().isRatKingType()) {
            Debug.println("ERROR: Baby rat tried to write rat king location, health, and flip alive bit!");
            return -1;
        }

        MapLocation myLoc = Robot.rc.getLocation();
        int bitOffset = ratKingIndex * RAT_KING_LOCATION_BITS;
        int combined = readBits(bitOffset, RAT_KING_LOCATION_BITS);
        int flippedAliveBit;
        if (prevAliveBit == -1) {
            int currentAliveBit = combined & RAT_KING_ALIVE_BIT;
            flippedAliveBit = currentAliveBit ^ 1;
        } else {
            flippedAliveBit = prevAliveBit ^ 1;
        }

        int encodedLoc = Util.locationToInt(myLoc);
        int healthValue = Math.min((1 << RAT_KING_HEALTH_BITS) - 1,
            (health * ((1 << RAT_KING_HEALTH_BITS) - 1)) / RAT_KING_MAX_HEALTH);
        int newCombined = (encodedLoc << (RAT_KING_HEALTH_BITS + RAT_KING_ALIVE_BIT))
            | (healthValue << RAT_KING_ALIVE_BIT) | flippedAliveBit;
        writeBits(bitOffset, RAT_KING_LOCATION_BITS, newCombined);
        return flippedAliveBit;
    }

    public static void updateLivingRatKings() throws GameActionException {
        livingRatKingCount = 0;
        for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
            MapLocation loc = readRatKingLoc(i);
            if (loc != null)
                livingRatKingCount++;
        }
    }

    public static boolean isRatKingAlive(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.MAX_NUMBER_OF_RAT_KINGS)
            return false;

        int bitOffset = index * RAT_KING_LOCATION_BITS;
        int combined = readBits(bitOffset, RAT_KING_LOCATION_BITS);
        int currentBit = combined & RAT_KING_ALIVE_BIT;
        int encodedLoc = combined >> (RAT_KING_HEALTH_BITS + RAT_KING_ALIVE_BIT);
        if (encodedLoc == Util.NULL_LOCATION_INT || encodedLoc >= 0xFFC) {
            previousAliveBits[index] = -1;
            return false;
        }

        int previousBit = previousAliveBits[index];
        if (previousBit == -1) {
            previousAliveBits[index] = currentBit;
            return true;
        }

        if (currentBit != previousBit) {
            previousAliveBits[index] = currentBit;
            return true;
        }

        return false;
    }

    public static MapLocation readRatKingLoc(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.MAX_NUMBER_OF_RAT_KINGS)
            return null;

        int bitOffset = index * RAT_KING_LOCATION_BITS;
        int combined = readBits(bitOffset, RAT_KING_LOCATION_BITS);
        int encodedLoc = combined >> (RAT_KING_HEALTH_BITS + RAT_KING_ALIVE_BIT);
        return Util.intToLocation(encodedLoc);
    }

    public static void clearRatKingSlot(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.MAX_NUMBER_OF_RAT_KINGS)
            return;

        int bitOffset = index * RAT_KING_LOCATION_BITS;
        writeBits(bitOffset, RAT_KING_LOCATION_BITS,
            Util.NULL_LOCATION_INT << (RAT_KING_HEALTH_BITS + RAT_KING_ALIVE_BIT));
    }

    public static MapLocation getClosestRatKing(MapLocation from) throws GameActionException {
        MapLocation closest = null;
        int closestDist = Integer.MAX_VALUE;

        for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
            MapLocation loc = readRatKingLoc(i);
            if (loc == null)
                continue;

            int dist = from.distanceSquaredTo(loc);
            if (dist < closestDist) {
                closest = loc;
                closestDist = dist;
            }
        }

        return closest;
    }

    public static MapLocation getClosestRatKingNotIncludingSelf(MapLocation from, int ratKingIndex)
        throws GameActionException {
        MapLocation closest = null;
        int closestDist = Integer.MAX_VALUE;

        for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
            if (ratKingIndex == i)
                continue;

            MapLocation loc = readRatKingLoc(i);
            if (loc == null)
                continue;

            int dist = from.distanceSquaredTo(loc);
            if (dist < closestDist) {
                closest = loc;
                closestDist = dist;
            }
        }

        return closest;
    }

    public static MapLocation[] getRatKings() throws GameActionException {
        MapLocation[] kings = new MapLocation[GameConstants.MAX_NUMBER_OF_RAT_KINGS];
        for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
            MapLocation loc = readRatKingLoc(i);
            if (loc == null)
                continue;

            kings[i] = loc;
        }
        return kings;
    }

    public static MapLocation getClosestVisibleRatKing(MapLocation from) throws GameActionException {
        MapLocation closest = null;
        int closestDist = Integer.MAX_VALUE;

        for (int i = 0; i < GameConstants.MAX_NUMBER_OF_RAT_KINGS; i++) {
            MapLocation loc = readRatKingLoc(i);
            if (loc == null)
                continue;

            int dist = from.distanceSquaredTo(loc);
            if (dist > GameConstants.SQUEAK_RADIUS_SQUARED)
                continue;

            if (dist < closestDist) {
                closest = loc;
                closestDist = dist;
            }
        }

        return closest;
    }
    // endregion

    // region ============= CHEESE MINE LOCATIONS =============

    // --- Regular Operations ---

    public static void clearCheeseMineLoc(int index) throws GameActionException {
        if (index < 0 || index >= MAX_CHEESE_MINE_LOCATIONS)
            return;

        int bitOffset = CHEESE_MINE_SECTION_START + (index * CHEESE_MINE_LOCATION_BITS);
        writeBits(bitOffset, CHEESE_MINE_LOCATION_BITS, Util.NULL_LOCATION_INT);
    }

    public static boolean isLocationInSharedArray(MapLocation loc) {
        return locationsInSharedArray.contains(Util.locationToInt(loc));
    }

    public static int getLocationsInSharedArraySize() {
        return locationsInSharedArray.size();
    }

    private static int findFirstNullCheeseMineSlot() throws GameActionException {
        // Find the first null slot in the shared array (where we can write)
        for (int i = 0; i < MAX_CHEESE_MINE_LOCATIONS; i++) {
            int bitOffset = CHEESE_MINE_SECTION_START + (i * CHEESE_MINE_LOCATION_BITS);
            int encodedLoc = readBits(bitOffset, CHEESE_MINE_LOCATION_BITS);
            if (encodedLoc >= 0xFFC) {
                // Found null slot
                return i;
            }
        }
        // No null slot found - array is full
        return MAX_CHEESE_MINE_LOCATIONS;
    }

    public static void writeCheeseMineToSharedArray(MapLocation loc) throws GameActionException {
        boolean rewritten = isCheeseMinesRewritten();

        if (SymmetryManager.isSymmetryDetermined() && isSymmetryGloballyKnown() && rewritten) {
            MapLocation symLoc = SymmetryManager.getSymmetric(loc);
            if (symLoc != null) {
                MapLocation locToWrite = chooseLocFromPair(loc, symLoc);
                if (isLocationInSharedArray(locToWrite)) {
                    return;
                }
                int writeIndex = findFirstNullCheeseMineSlot();
                if (writeIndex >= MAX_CHEESE_MINE_LOCATIONS) {
                    return;
                }
                int bitOffset = CHEESE_MINE_SECTION_START + (writeIndex * CHEESE_MINE_LOCATION_BITS);
                int encodedLoc = Util.locationToInt(locToWrite);
                writeBits(bitOffset, CHEESE_MINE_LOCATION_BITS, encodedLoc);
                locationsInSharedArray.add(encodedLoc);
                addToSidedCheeseMines(locToWrite);
                addToSidedCheeseMines(symLoc);
                return;
            }
        }

        if (isLocationInSharedArray(loc)) {
            return;
        }

        int writeIndex = findFirstNullCheeseMineSlot();
        if (writeIndex >= MAX_CHEESE_MINE_LOCATIONS) {
            return;
        }

        int bitOffset = CHEESE_MINE_SECTION_START + (writeIndex * CHEESE_MINE_LOCATION_BITS);
        int encodedLoc = Util.locationToInt(loc);
        writeBits(bitOffset, CHEESE_MINE_LOCATION_BITS, encodedLoc);
        locationsInSharedArray.add(encodedLoc);

        if (SymmetryManager.isSymmetryDetermined() && isSymmetryGloballyKnown() && rewritten) {
            addToSidedCheeseMines(loc);
        }
    }

    public static void readCheeseMinesFromSharedArray() throws GameActionException {
        // Only rat king index 0 checks if initialization is done
        // Baby rats and other rat kings assume initialization is always done
        // boolean isRatKingIndex0 = Robot.rc.getType().isRatKingType() && ratKingIndex
        // == 0;
        if (Robot.rc.getType().isRatKingType() && ratKingIndex == 0 && !isCheeseMinesInitialized()) {
            return;
        }

        if (isRewritingSharedArray) {
            return;
        }

        // boolean isBabyRat = !Robot.rc.getType().isRatKingType();
        // int startBytecode = isBabyRat ? Clock.getBytecodeNum() : 0;
        boolean symmetryKnown = SymmetryManager.isSymmetryDetermined() && isSymmetryGloballyKnown();
        boolean rewritten = isCheeseMinesRewritten();
        Debug.println(true, "POINTER BEFORE: " + cheeseMineReadingPointer, Debug.CHECK_BYTECODE_ID);

        int encodedLoc;
        MapLocation mapLoc;
        // int x;
        // int y;
        MapLocation symLoc;
        // Read one location at a time from the pointer until we hit null or end
        while (cheeseMineReadingPointer < MAX_CHEESE_MINE_LOCATIONS) {
            encodedLoc = readBits(CHEESE_MINE_SECTION_START + (cheeseMineReadingPointer * CHEESE_MINE_LOCATION_BITS),
                CHEESE_MINE_LOCATION_BITS);

            // If we hit null (0xFFC or higher), stop reading
            if (encodedLoc >= 0xFFC) {
                break;
            }

            // x = (encodedLoc >> 6) & 0x3F;
            // y = encodedLoc & 0x3F;
            // if (x >= 60 || y >= 60) {
            // break;
            // }

            mapLoc = new MapLocation((encodedLoc >> 6) & 0x3F, encodedLoc & 0x3F);
            locationsInSharedArray.add(encodedLoc);

            // if (!MapData.hasCheeseMine(mapLoc)) {
            MapData.addCheeseMineToCache(mapLoc);
            // }

            if (symmetryKnown && rewritten) {
                symLoc = SymmetryManager.getSymmetric(mapLoc);
                if (symLoc != null) {
                    MapData.addCheeseMineToCache(symLoc);
                }
                addToSidedCheeseMines(mapLoc);
                if (symLoc != null) {
                    addToSidedCheeseMines(symLoc);
                }
            }

            ++cheeseMineReadingPointer;

            // if (isBabyRat && Clock.getBytecodeNum() - startBytecode > 2500) {
            // break;
            // }
        }

        Debug.println(true, "POINTER BEFORE: " + cheeseMineReadingPointer, Debug.CHECK_BYTECODE_ID);
    }

    private static MapLocation chooseLocFromPair(MapLocation loc1, MapLocation loc2) {
        if (loc1 == null)
            return loc2;
        if (loc2 == null)
            return loc1;

        // Prefer the location on our side
        boolean loc1OnOurSide = isOnOurSide(loc1);
        boolean loc2OnOurSide = isOnOurSide(loc2);
        if (loc1OnOurSide && !loc2OnOurSide)
            return loc1;
        if (!loc1OnOurSide && loc2OnOurSide)
            return loc2;

        // Tiebreaker: prefer lower coordinates
        if (loc1.x < loc2.x)
            return loc1;
        if (loc1.x > loc2.x)
            return loc2;
        if (loc1.y < loc2.y)
            return loc1;
        return loc2;
    }

    public static boolean isOnOurSide(MapLocation loc) {
        // Ensure our side is initialized
        if (weAreOnLeftSide == null) {
            initializeOurSide();
        }

        int sym = SymmetryManager.getSym();
        int midX = Robot.W / 2;
        int midY = Robot.H / 2;

        if (sym == SymmetryManager.H) {
            boolean locOnLeftSide = loc.x < midX;
            // If we're on left, our side is left. If we're on right, our side is right.
            return weAreOnLeftSide == locOnLeftSide;
        } else if (sym == SymmetryManager.V) {
            boolean locOnBottomSide = loc.y < midY;
            return weAreOnBottomSide == locOnBottomSide;
        } else if (sym == SymmetryManager.R) {
            boolean locOnLeftOrBottom = (loc.x < midX) || (loc.x == midX && loc.y < midY);
            return weAreOnLeftOrBottomSide == locOnLeftOrBottom;
        }
        return true;
    }

    private static boolean isOnSymmetryLine(MapLocation loc) {
        int sym = SymmetryManager.getSym();
        if (sym == SymmetryManager.H) {
            int midX = Robot.W / 2;
            return loc.x == midX;
        } else if (sym == SymmetryManager.V) {
            int midY = Robot.H / 2;
            return loc.y == midY;
        } else if (sym == SymmetryManager.R) {
            int midX = Robot.W / 2;
            int midY = Robot.H / 2;
            return loc.x == midX && loc.y == midY;
        }
        return false;
    }

    public static void addToSidedCheeseMines(MapLocation loc) {
        if (loc == null)
            return;
        if (isOnSymmetryLine(loc)) {
            MapData.addFriendlySidedCheeseMine(loc);
            MapData.addEnemySidedCheeseMine(loc);
        } else if (isOnOurSide(loc)) {
            MapData.addFriendlySidedCheeseMine(loc);
        } else {
            MapData.addEnemySidedCheeseMine(loc);
        }
    }

    // --- Rewriting Logic ---

    public static void checkForRewriteAndResetPointer() throws GameActionException {
        boolean isRewritten = isCheeseMinesRewritten();

        if (isRewritten && !hasResetPointerForRewrite) {
            hasResetPointerForRewrite = true;
            needsRereadAfterRewrite = true;
            cheeseMineReadingPointer = 0;
            locationsInSharedArray.clear();
            MapData.clearSidedCheeseMines();
        }

        // If rewrite bit is cleared (shouldn't happen, but handle it), reset the flag
        if (!isRewritten && hasResetPointerForRewrite) {
            hasResetPointerForRewrite = false;
        }
    }

    public static void rereadCheeseMinesAfterRewrite(int maxBytecode) throws GameActionException {
        // After rewrite, we just use the normal readCheeseMinesFromSharedArray
        // which will read from pointer 0 (reset in checkForRewriteAndResetPointer)
        // So we can just call the normal read function
        if (needsRereadAfterRewrite && isCheeseMinesRewritten()) {
            // The normal readCheeseMinesFromSharedArray will handle reading
            // We just mark that we've started the reread process
            needsRereadAfterRewrite = false;
        }
    }

    public static void rewriteCheeseMines(int maxBytecode) throws GameActionException {
        if (!Robot.rc.getType().isRatKingType() || ratKingIndex != 0) {
            return;
        }

        if (isCheeseMinesRewritten()) {
            return;
        }

        if (!SymmetryManager.isSymmetryDetermined() || !isSymmetryGloballyKnown()) {
            return;
        }

        if (!isRewritingSharedArray) {
            isRewritingSharedArray = true;
            rewritingReadPointer = 0;
            rewritingWritePointer = 0;
            locationsInSharedArray.clear();
        }

        int startBytecode = Clock.getBytecodeNum();
        FastIntSet seenPairs = new FastIntSet();
        boolean hitEnd = false;

        while (rewritingReadPointer < MAX_CHEESE_MINE_LOCATIONS) {
            if (Clock.getBytecodeNum() - startBytecode > maxBytecode - 300) {
                return;
            }

            int readBitOffset = CHEESE_MINE_SECTION_START + (rewritingReadPointer * CHEESE_MINE_LOCATION_BITS);
            int encodedLoc = readBits(readBitOffset, CHEESE_MINE_LOCATION_BITS);

            if (encodedLoc >= 0xFFC) {
                hitEnd = true;
                break;
            }

            int x = (encodedLoc >> 6) & 0x3F;
            int y = encodedLoc & 0x3F;
            if (x >= 60 || y >= 60) {
                rewritingReadPointer++;
                continue;
            }

            MapLocation loc = new MapLocation(x, y);
            MapLocation symLoc = SymmetryManager.getSymmetric(loc);

            if (symLoc != null) {
                if (!MapData.hasCheeseMine(loc))
                    MapData.addCheeseMineToCache(loc);
                if (!MapData.hasCheeseMine(symLoc))
                    MapData.addCheeseMineToCache(symLoc);

                int loc1Int = Util.locationToInt(loc);
                int loc2Int = Util.locationToInt(symLoc);
                int pairKey = Math.min(loc1Int, loc2Int);

                if (seenPairs.contains(pairKey)) {
                    clearCheeseMineLoc(rewritingReadPointer);
                    rewritingReadPointer++;
                    continue;
                }

                seenPairs.add(pairKey);
                MapLocation locToKeep = chooseLocFromPair(loc, symLoc);
                int locToKeepInt = Util.locationToInt(locToKeep);

                if (loc.equals(locToKeep)) {
                    if (rewritingReadPointer != rewritingWritePointer) {
                        int writeBitOffset = CHEESE_MINE_SECTION_START
                            + (rewritingWritePointer * CHEESE_MINE_LOCATION_BITS);
                        writeBits(writeBitOffset, CHEESE_MINE_LOCATION_BITS, locToKeepInt);
                        locationsInSharedArray.add(locToKeepInt);
                        if (rewritingReadPointer < rewritingWritePointer) {
                            clearCheeseMineLoc(rewritingReadPointer);
                        }
                    } else {
                        locationsInSharedArray.add(locToKeepInt);
                    }
                    rewritingWritePointer++;
                } else {
                    clearCheeseMineLoc(rewritingReadPointer);
                    int writeBitOffset = CHEESE_MINE_SECTION_START
                        + (rewritingWritePointer * CHEESE_MINE_LOCATION_BITS);
                    writeBits(writeBitOffset, CHEESE_MINE_LOCATION_BITS, locToKeepInt);
                    locationsInSharedArray.add(locToKeepInt);
                    rewritingWritePointer++;
                }
            } else {
                if (!MapData.hasCheeseMine(loc)) {
                    MapData.addCheeseMineToCache(loc);
                }
                if (rewritingReadPointer != rewritingWritePointer) {
                    int writeBitOffset = CHEESE_MINE_SECTION_START
                        + (rewritingWritePointer * CHEESE_MINE_LOCATION_BITS);
                    writeBits(writeBitOffset, CHEESE_MINE_LOCATION_BITS, encodedLoc);
                    locationsInSharedArray.add(encodedLoc);
                    if (rewritingReadPointer < rewritingWritePointer) {
                        clearCheeseMineLoc(rewritingReadPointer);
                    }
                } else {
                    locationsInSharedArray.add(encodedLoc);
                }
                rewritingWritePointer++;
            }

            rewritingReadPointer++;
        }

        if (hitEnd || rewritingReadPointer >= MAX_CHEESE_MINE_LOCATIONS) {
            // Reading pointers will be reset in checkForRewriteAndResetPointer() when
            // rewrite is detected
            isRewritingSharedArray = false;
            setCheeseMinesRewritten();
            shouldPrintRewrittenCheeseMines = true;
            Debug.println("RAT KING: Rewrite completed - " + rewritingWritePointer + " cheese mines");
        }
    }

    public static void printRewrittenCheeseMinesIfNeeded() throws GameActionException {
        if (!shouldPrintRewrittenCheeseMines || !Robot.rc.getType().isRatKingType() || ratKingIndex != 0) {
            return;
        }

        shouldPrintRewrittenCheeseMines = false;
        Debug.println("=== REWRITTEN CHEESE MINES ===");

        StringBuilder keys = locationsInSharedArray.keys;
        for (int i = 0; i < keys.length(); i++) {
            int encodedLoc = keys.charAt(i);
            MapLocation loc = Util.intToLocation(encodedLoc);
            if (loc == null)
                continue;

            MapLocation symLoc = SymmetryManager.getSymmetric(loc);
            if (symLoc != null) {
                Debug.println("Loc: " + loc + " | Sym: " + symLoc);
            } else {
                Debug.println("Loc: " + loc + " | Sym: (none)");
            }
        }

        Debug.println("=== END REWRITTEN CHEESE MINES ===");
    }
    // endregion

    // region ============= BIT MANIPULATION =============

    private static void writeBits(int bitOffset, int numBits, int value) throws GameActionException {
        int bitsRemaining = numBits;
        int currentBitPos = bitOffset;
        int valueShift = numBits;

        while (bitsRemaining > 0) {
            int slotIndex = currentBitPos / 10;
            int bitInSlot = currentBitPos % 10;
            int bitsAvailableInSlot = 10 - bitInSlot;
            int bitsToWrite = Math.min(bitsRemaining, bitsAvailableInSlot);

            int slotValue = Robot.rc.readSharedArray(slotIndex);
            valueShift -= bitsToWrite;
            int bitsFromValue = (value >> valueShift) & ((1 << bitsToWrite) - 1);
            int mask = ((1 << bitsToWrite) - 1) << (bitsAvailableInSlot - bitsToWrite);
            slotValue = (slotValue & ~mask) | (bitsFromValue << (bitsAvailableInSlot - bitsToWrite));

            Robot.rc.writeSharedArray(slotIndex, slotValue);

            currentBitPos += bitsToWrite;
            bitsRemaining -= bitsToWrite;
        }
    }

    private static int readBits(int bitOffset, int numBits) throws GameActionException {
        int result = 0;
        int bitsRemaining = numBits;
        int currentBitPos = bitOffset;

        int bitsAvailableInSlot;
        int bitsToRead;
        while (bitsRemaining > 0) {
            bitsAvailableInSlot = 10 - (currentBitPos % 10);
            bitsToRead = Math.min(bitsRemaining, bitsAvailableInSlot);

            result = (result << bitsToRead)
                | (((Robot.rc.readSharedArray(currentBitPos / 10)) >> (bitsAvailableInSlot - bitsToRead))
                & ((1 << bitsToRead) - 1));

            currentBitPos += bitsToRead;
            bitsRemaining -= bitsToRead;
        }

        return result;
    }
    // endregion

    // region ============= SYMMETRY =============

    public static boolean isSymmetryGloballyKnown() throws GameActionException {
        int symmInfo = readBits(RAT_KING_SECTION_END, SYMMETRY_BITS);
        return symmInfo == 0b011 || symmInfo == 0b101 || symmInfo == 0b110;
    }

    public static int readSymmetryBitsFromSharedArray() throws GameActionException {
        return readBits(RAT_KING_SECTION_END, SYMMETRY_BITS);
    }

    public static boolean isCheeseMinesRewritten() throws GameActionException {
        if (cachedIsCheeseMinesRewritten != null) {
            return cachedIsCheeseMinesRewritten;
        }
        boolean result = readBits(SYMMETRY_SECTION_END, REWRITTEN_FLAG_BITS) != 0;
        if (result) {
            cachedIsCheeseMinesRewritten = true;
        }
        return result;
    }

    private static void setCheeseMinesRewritten() throws GameActionException {
        writeBits(SYMMETRY_SECTION_END, REWRITTEN_FLAG_BITS, 1);
        cachedIsCheeseMinesRewritten = true;
    }

    public static void syncSymmetryFromGlobalArray() throws GameActionException {
        if (SymmetryManager.isSymmetryDetermined()) {
            return;
        }

        if (isSymmetryGloballyKnown()) {
            int symmInfo = readBits(RAT_KING_SECTION_END, SYMMETRY_BITS);
            if ((symmInfo & 0x1) != 0) {
                SymmetryManager.discardR();
            }
            if ((symmInfo & 0x2) != 0) {
                SymmetryManager.discardV();
            }
            if ((symmInfo & 0x4) != 0) {
                SymmetryManager.discardH();
            }
        }
    }

    public static void writeSymmetryToSharedArrayIfKnown() throws GameActionException {
        if (!Robot.rc.getType().isRatKingType()) {
            Debug.println("ERROR: Baby rat tried to write symmetry to shared array!");
            if (Debug.FAIL_FAST)
                throw new IllegalStateException("ERROR: Baby rat tried to write symmetry to shared array!");
            return;
        }

        if (SymmetryManager.isSymmetryDetermined()) {
            if (!isSymmetryGloballyKnown()) {
                int discardedSyms = SymmetryManager.getDiscardedSyms();
                writeSymmToSharedArray(discardedSyms);
            }
        }
    }

    public static void writeSymmToSharedArray(int discardedSyms) throws GameActionException {
        if (!Robot.rc.getType().isRatKingType()) {
            Debug.println("ERROR: Baby rat tried to write symmetry to shared array!");
            if (Debug.FAIL_FAST)
                throw new IllegalStateException("ERROR: Baby rat tried to write symmetry to shared array!");
            return;
        }

        int symmetryBits = 0;
        if ((discardedSyms & SymmetryManager.R) != 0)
            symmetryBits |= 0b001;
        if ((discardedSyms & SymmetryManager.V) != 0)
            symmetryBits |= 0b010;
        if ((discardedSyms & SymmetryManager.H) != 0)
            symmetryBits |= 0b100;

        writeBits(RAT_KING_SECTION_END, SYMMETRY_BITS, symmetryBits);
        Debug.println("RAT KING WROTE SYMMETRY TO SHARED ARRAY: " + SymmetryManager.getSymString());
    }
    // endregion

    // region ============= FORMATION RALLY POINT =============

    public static MapLocation readFormationRallyPoint() throws GameActionException {
        int encodedLoc = readBits(FORMATION_RALLY_POINT_OFFSET, LOCATION_BITS);
        if (encodedLoc >= 0xFFC || encodedLoc == 0) {
            return null;
        }
        return Util.intToLocation(encodedLoc);
    }

    public static void writeFormationRallyPoint(MapLocation loc) throws GameActionException {
        if (!Robot.rc.getType().isRatKingType()) {
            return;
        }
        int encodedLoc = (loc == null) ? Util.NULL_LOCATION_INT : Util.locationToInt(loc);
        writeBits(FORMATION_RALLY_POINT_OFFSET, LOCATION_BITS, encodedLoc);
    }

    public static void clearFormationRallyPoint() throws GameActionException {
        writeBits(FORMATION_RALLY_POINT_OFFSET, LOCATION_BITS, Util.NULL_LOCATION_INT);
    }

    // endregion
}
