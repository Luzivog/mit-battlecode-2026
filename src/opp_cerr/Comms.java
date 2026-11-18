package opp_cerr;

import battlecode.common.*;

import opp_cerr.fast.FastIntSet;
import opp_cerr.fast.FastLocSet;

/*  TODO
   - Cheese mine locations
   - Alternate messaging
   - Squeaking
       - share critical info (enemy rat king,
       -
*/

/*  SQUEAKS ENCODING 32 bits LSB first
   : (4 bits) types, lower id is higher prio
       - cat loc
       - combat loc
       - cheese mine loc
       - form rat king
       - attract rat king (not impl'd)
       - ...
   : (16 bits) aux data
       - e.g. unit id
   : (12 bits) target location
*/

/* LOCATIONS ENCODING
   IF map width, height > 32
   ((x & 62) << 4) | ((y & 62) >> 1)
   ELSE
   (x << 5) | y
   location = ~0 represents invalid value
*/

/* --- COMMS ARRAY ALLOCATION --------------------------------------- //
    00      : UNUSED
    01      : rat king toggles
    02      : symmetry types
                LSB .......RVH
    03-07   : rat king locations (written by rat kings)
    08-17   : enemy rat king locations
    18-27   : cat locations (10 cats)
    28-37   : cheese mines
    38      : UNUSED
    39      : next rat king formation location
    40-45   : rat king metadata (5 rat kings, 1 slot each + 1 extra)
// ------------------------------------------------------------------ */

public class Comms {
    private static RobotController rc;
    private static int round;
    public static int WIDTH, HEIGHT;

    // --- ARRAY ALLOCATION --- //

    public static final int NULL_ENC10 = 1023;

    private static final int KINGS_TRACKER = 1;
    private static final int SYMMETRY_TRACKER = 2;
    private static final int ALLY_KINGS_HEAD = 3;
    private static final int ENEMY_KINGS_HEAD = 8;

    // if length changes, update readCatLocs()
    private static final int CAT_LOCS_HEAD = 18;
    private static final int CAT_LOCS_TAIL = 28;

    // if length changes, update readCheeseMines()
    private static final int CHEESE_MINES_HEAD = 28;
    private static final int CHEESE_MINES_TAIL = 38;

    private static final int KING_FORMATION_LOC = 39;

    private static final int RAT_KING_METADATA_HEAD = 40;

    private static final int PANIC_FLAG = 63;

    //    private static final int QUEUE_LAST_MESSAGE = 64;
    //    private static final int QUEUE_HEAD = 64;
    //    private static final int QUEUE_TAIL = 64;

    // --- SQUEAK ENCODING --- //

    private static final int SQUEAK_TYPE_MASK = 0x0000000F;
    private static final int SQUEAK_AUX_MASK = 0x000FFFF0;
    private static final int SQUEAK_AUX_OFFSET = 4;
    private static final int SQUEAK_LOC_MASK = 0xFFF00000;
    private static final int SQUEAK_LOC_OFFSET = 20;

    private static final int SQTYPE_NULL = 15;
    private static final int SQTYPE_CAT = 0;
    private static final int SQTYPE_COMBAT = 1;
    private static final int SQTYPE_SYMMETRY_UPDATE = 2;
    private static final int SQTYPE_CHEESE_MINE = 3;
    private static final int SQTYPE_MINE_SAFETY = 4;
    private static final int SQTYPE_ENEMY_KING = 5;

    // --- DATA --- //

    static int lastCheeseMineRefreshRound = -1; // in case we run out of compute

    static final FastLocSet allyRatKingLocs = new FastLocSet();
    static final FastLocSet enemyRatKingLocs = new FastLocSet();

    static final FastIntSet seenCats = new FastIntSet();
    static MapLocation[] catLocs = new MapLocation[60];

    static int numCheeseMines = 10;
    static MapLocation[] cheeseMineLocs = new MapLocation[60];

    //    private static int lastReadMessage = 63;
    //    private static int lastWrittenMessage = 63;
    //    private static final FastQueue<Integer> messagesToProcess = new FastQueue<>();

    static boolean isHSymPossible = true;
    static boolean isVSymPossible = true;
    static boolean isRSymPossible = true;

    private static final int[] bufferPool = new int[64];
    private static boolean[] dirtyFlags = new boolean[64];

    private static int plannedSqueakType;
    private static int squeakMessage;

    // ^ --------------------------------------------------------------------------------- //
    // ^ -------------------------------- PUBLIC METHODS --------------------------------- //
    // ^ --------------------------------------------------------------------------------- //

    public static void init(RobotController r) throws GameActionException {
        rc = r;

        WIDTH = rc.getMapWidth();
        HEIGHT = rc.getMapHeight();

        if (round <= 1 && rc.getType() == UnitType.RAT_KING) setupBuffer();
    }

    // * ------------------------------- //
    // * public methods                  //
    // * ------------------------------- //

    public static void startTurn(int roundNum) throws GameActionException {
        round = roundNum;
        if (round < 2) return;

        initBufferPool();

        if (round == rc.getRoundNum() && rc.getRoundNum() % 2 == 0) readRatKingLocs();
        readCatLocs();
        readCheeseMines();
        readSymmetry();
        readKingFormationLoc();

        //        readMessageQueue();
        //        processMessages();

        plannedSqueakType = SQTYPE_NULL;
        processSqueaks();
    }

    public static void endTurn() throws GameActionException {
        // send squeak
        try {
            squeak();
        } catch (Exception e) {
            System.out.println("[COMMS ERROR] could not squeak: " + e.getMessage());
        }
        if (rc.getType() == UnitType.RAT_KING) {
            // update queue tail
            // writeToBufferPool(QUEUE_LAST_MESSAGE, lastWrittenMessage);

            // updates all buffer simultaneously
            if (round == rc.getRoundNum()) flushBufferPool();
        }
    }

    public static int locToInt12(MapLocation loc) {
        return (loc.x << 6) | loc.y;
    }

    public static int locToInt10(MapLocation loc) {
        return ((loc.x >> (WIDTH > 32 ? 1 : 0)) << 5) | (loc.y >> (HEIGHT > 32 ? 1 : 0));
    }

    public static int locToInt8(MapLocation loc) {
        return ((loc.x >> (WIDTH > 32 ? 2 : 1)) << 4) | (loc.y >> (HEIGHT > 32 ? 2 : 1));
    }

    public static MapLocation int12ToLoc(int enc) {
        return new MapLocation((enc & 0b111111000000) >> 6, (enc & 0b111111));
    }

    public static MapLocation int10ToLoc(int enc) {
        return new MapLocation(
                (enc & 0b1111100000) >> (WIDTH > 32 ? 4 : 5),
                (enc & 0b11111) << (HEIGHT > 32 ? 1 : 0));
    }

    public static MapLocation int8ToLoc(int enc) {
        return new MapLocation(
                (enc & 0b11110000) >> (WIDTH > 32 ? 2 : 3),
                (enc & 0b1111) << (HEIGHT > 32 ? 2 : 1));
    }

    public static void setPanicFlag(boolean panic) {
        writeToBufferPool(PANIC_FLAG, panic ? 1 : 0);
    }

    public static boolean isPanic() {
        return bufferPool[PANIC_FLAG] != 0;
    }

    public static void updateCatLocation(RobotInfo cat) throws Exception {
        if (cat.getType() != UnitType.CAT) throw new Exception("tried to update cat on non-cat");
        writeToBufferPool(cat.ID + CAT_LOCS_HEAD - 1, locToInt10(cat.getLocation()));
    }

    public static void updateCheeseMineLocation(int mineId, MapLocation loc) throws Exception {
        if (mineId < 1 || mineId > 10) throw new Exception("invalid cheese mine ID");
        writeToBufferPool(mineId + CHEESE_MINES_HEAD - 1, locToInt10(loc));
    }

    // 0 <= message < 1023 !!
    //    public static void writeToQueue(int message) {
    //        lastWrittenMessage += 1;
    //        if (lastWrittenMessage == QUEUE_TAIL) lastWrittenMessage = QUEUE_HEAD;
    //        lastReadMessage = lastWrittenMessage;
    //
    //        writeToBufferPool(lastWrittenMessage, message);
    //    }

    // TODO update symmetry based on info
    public static void updateSymmetry() {
        if (rc.getType() != UnitType.RAT_KING) return;
        int sym = 0;

        if (isRSymPossible) sym |= 0b100;
        if (isVSymPossible) sym |= 0b10;
        if (isHSymPossible) sym |= 0b1;

        if (sym == bufferPool[SYMMETRY_TRACKER]) return;
        System.out.println("WRITING NEW SYM: " + sym);
        writeToBufferPool(SYMMETRY_TRACKER, sym);

        if (Navigation.symLocked) {
            for (int i = 0; i < numCheeseMines; i++) {
                if (cheeseMineLocs[i] == null) break;
                MacroData.writeCheeseMineToGlobal(
                        cheeseMineLocs[i]); // should recheck for symmetrical
            }
        }
    }

    // King writes information about its state
    public static void updateThisKingMetadata() {
        if (rc.getRoundNum() != round || round % 2 == 0) return;
        assert (rc.getType() == UnitType.RAT_KING);

        int myIndex = determineThisKingIndex(rc.getLocation());

        assert (myIndex != -1);

        // 1. Update my Location
        int x = rc.getLocation().x;
        int y = rc.getLocation().y;
        writeToBufferPool(ALLY_KINGS_HEAD + myIndex, x);
        for (int i = ALLY_KINGS_HEAD + myIndex + 1; i < ALLY_KINGS_HEAD + 5; i++) {
            writeToBufferPool(i, NULL_ENC10);
        }

        // 2. Update Metadata Slot
        writeToBufferPool(RAT_KING_METADATA_HEAD + myIndex, y);
        for (int i = RAT_KING_METADATA_HEAD + myIndex + 1; i < RAT_KING_METADATA_HEAD + 5; i++) {
            writeToBufferPool(i, NULL_ENC10);
        }
    }

    public static int determineThisKingIndex(MapLocation loc) {
        int roundBits = round & 0b11;

        if (readBits(bufferPool[KINGS_TRACKER], 0, 2) != roundBits
                || bufferPool[ALLY_KINGS_HEAD] == NULL_ENC10) {
            writeToBufferPool(
                    KINGS_TRACKER, overwriteBits(bufferPool[KINGS_TRACKER], roundBits, 0, 2));
            return 0;
        } else if (readBits(bufferPool[KINGS_TRACKER], 2, 2) != roundBits
                || bufferPool[ALLY_KINGS_HEAD + 1] == NULL_ENC10) {

            writeToBufferPool(
                    KINGS_TRACKER, overwriteBits(bufferPool[KINGS_TRACKER], roundBits, 2, 2));
            return 1;
        } else if (readBits(bufferPool[KINGS_TRACKER], 4, 2) != roundBits
                || bufferPool[ALLY_KINGS_HEAD + 2] == NULL_ENC10) {

            writeToBufferPool(
                    KINGS_TRACKER, overwriteBits(bufferPool[KINGS_TRACKER], roundBits, 4, 2));
            return 2;
        } else if (readBits(bufferPool[KINGS_TRACKER], 6, 2) != roundBits
                || bufferPool[ALLY_KINGS_HEAD + 3] == NULL_ENC10) {

            writeToBufferPool(
                    KINGS_TRACKER, overwriteBits(bufferPool[KINGS_TRACKER], roundBits, 6, 2));
            return 3;
        } else if (readBits(bufferPool[KINGS_TRACKER], 8, 2) != roundBits
                || bufferPool[ALLY_KINGS_HEAD + 4] == NULL_ENC10) {

            writeToBufferPool(
                    KINGS_TRACKER, overwriteBits(bufferPool[KINGS_TRACKER], roundBits, 8, 2));
            return 4;
        }
        return -1;
    }

    public static void updateKingFormationLoc(MapLocation loc) throws GameActionException {
        if (loc == null
                || allyRatKingLocs.size == 5
                || (allyRatKingLocs.size > 1 && RobotPlayer.round > 1190)) {
            writeToBufferPool(KING_FORMATION_LOC, NULL_ENC10);
            return;
        }
        rc.setIndicatorDot(loc, 0, 255, 255);

        // avoid building on top of other rat kings CONSTANT TO UP
        for (MapLocation allyKingLoc : allyRatKingLocs.getKeys()) {
            if (allyKingLoc.distanceSquaredTo(loc) < MacroData.MIN_DIST_TO_ALLY_KING) return;
        }

        writeToBufferPool(KING_FORMATION_LOC, locToInt10(loc));
    }

    // TODO allows updating rat king position
    public static void updateEnemyRatKingLoc(RobotInfo ratKing) {}

    public static void squeak() throws Exception {
        // squeak if intended
        if (rc.getType() == UnitType.RAT_KING) {
            if (Navigation.needUpdateSymmetry)
                updateSymmetry(); // if king rat just bypass squeak pipeline
            return;
        }

        if (plannedSqueakType != SQTYPE_NULL) {
            rc.squeak(squeakMessage);
            // System.out.println("i squoke " + plannedSqueakType);
            if (plannedSqueakType == SQTYPE_SYMMETRY_UPDATE) {
                // System.out.println(squeakMessage);
            }
            plannedSqueakType = SQTYPE_NULL;
            return;
        }

        // for information we want to communicate to the rat king
        boolean inRange = false;
        for (MapLocation loc : allyRatKingLocs.getKeys()) {
            // rc.setIndicatorDot(loc, 255, 0, 255);

            Direction towardsMe = loc.directionTo(rc.getLocation());
            MapLocation kingTowardsMe = loc.add(towardsMe);

            rc.setIndicatorDot(loc, 255, 0, 0);

            rc.setIndicatorDot(kingTowardsMe, 0, 255, 0);

            if (rc.getLocation().distanceSquaredTo(kingTowardsMe) <= 16) {
                inRange = true;
                break;
            }
        }
        if (!inRange) return;

        // see if we can squeak cheese mines
        if (Navigation.needUpdateSymmetry) {
            rc.setIndicatorDot(rc.getLocation(), 255, 255, 0);
            squeakSymmetryUpdate();
        } else if (!MacroData.cheeseMineQueue.isEmpty()) {
            rc.setIndicatorDot(rc.getLocation(), 255, 0, 255);
            squeakCheeseMine(MacroData.cheeseMineQueue.poll());
        } else if (MacroData.furthestSafeMine != null) {
            squeakMineSafety(MacroData.furthestSafeMine, true);
        }

        if (plannedSqueakType != SQTYPE_NULL) {
            rc.squeak(squeakMessage);
            if (plannedSqueakType == SQTYPE_SYMMETRY_UPDATE) {
                System.out.println("WPOEFJOEPWJFOSIDJFOPSDF: " + squeakMessage);
            }
            // System.out.println("i squoke " + plannedSqueakType);
            plannedSqueakType = SQTYPE_NULL;
        }
    }

    // ENCODED WITH 12 BIT PRECISION, NO ID
    public static void squeakCat(RobotInfo cat) throws Exception {
        if (cat.getType() != UnitType.CAT)
            throw new Exception("attempted to squeak cat but was not cat");

        if (plannedSqueakType <= SQTYPE_CAT) return;
        plannedSqueakType = SQTYPE_CAT;
        squeakMessage = SQTYPE_CAT;
        squeakMessage |= locToInt12(cat.location) << SQUEAK_LOC_OFFSET;
    }

    public static void squeakCombat(MapLocation combatLoc) {
        if (plannedSqueakType <= SQTYPE_COMBAT) return;
        plannedSqueakType = SQTYPE_COMBAT;
        squeakMessage = SQTYPE_COMBAT;
        squeakMessage |= locToInt12(combatLoc) << SQUEAK_LOC_OFFSET;
    }

    public static void squeakEnemyKing(RobotInfo king) throws Exception {
        if (king.getType() != UnitType.RAT_KING || king.team == rc.getTeam())
            throw new Exception("attempted to squeak invalid rat king");

        if (plannedSqueakType <= SQTYPE_ENEMY_KING) return;
        plannedSqueakType = SQTYPE_ENEMY_KING;
        squeakMessage = SQTYPE_ENEMY_KING;
        squeakMessage |= locToInt12(king.location) << SQUEAK_LOC_OFFSET;
    }

    public static void squeakCheeseMine(MapLocation mineLoc) throws Exception {
        if (plannedSqueakType <= SQTYPE_CHEESE_MINE) return;
        plannedSqueakType = SQTYPE_CHEESE_MINE;
        squeakMessage = SQTYPE_CHEESE_MINE;
        squeakMessage |= locToInt12(mineLoc) << SQUEAK_LOC_OFFSET;
    }

    public static void squeakMineSafety(MapLocation mineLoc, boolean isSafe) {
        if (plannedSqueakType <= SQTYPE_MINE_SAFETY) return;
        plannedSqueakType = SQTYPE_MINE_SAFETY;
        squeakMessage = SQTYPE_MINE_SAFETY;
        squeakMessage |= (isSafe ? 1 : 0) << SQUEAK_AUX_OFFSET;
        squeakMessage |= locToInt12(mineLoc) << SQUEAK_LOC_OFFSET;
    }

    public static void squeakSymmetryUpdate() {
        if (plannedSqueakType <= SQTYPE_SYMMETRY_UPDATE) return;
        plannedSqueakType = SQTYPE_SYMMETRY_UPDATE;
        squeakMessage = SQTYPE_SYMMETRY_UPDATE;
        squeakMessage |= (Navigation.canSymR ? 1 : 0) << (SQUEAK_AUX_OFFSET + 2);
        squeakMessage |= (Navigation.canSymV ? 1 : 0) << (SQUEAK_AUX_OFFSET + 1);
        squeakMessage |= (Navigation.canSymH ? 1 : 0) << (SQUEAK_AUX_OFFSET);
    }

    // * ----------------------------------- //
    // * internal methods                    //
    // * ----------------------------------- //

    private static void readRatKingLocs() {
        allyRatKingLocs.clear();
        enemyRatKingLocs.clear();
        if (bufferPool[ALLY_KINGS_HEAD] != NULL_ENC10) {
            int x = bufferPool[ALLY_KINGS_HEAD];
            int y = bufferPool[RAT_KING_METADATA_HEAD];
            allyRatKingLocs.add(new MapLocation(x, y));
        }
        if (bufferPool[ALLY_KINGS_HEAD + 1] != NULL_ENC10) {
            int x = bufferPool[ALLY_KINGS_HEAD + 1];
            int y = bufferPool[RAT_KING_METADATA_HEAD + 1];
            allyRatKingLocs.add(new MapLocation(x, y));
        }
        if (bufferPool[ALLY_KINGS_HEAD + 2] != NULL_ENC10) {
            int x = bufferPool[ALLY_KINGS_HEAD + 2];
            int y = bufferPool[RAT_KING_METADATA_HEAD + 2];
            allyRatKingLocs.add(new MapLocation(x, y));
        }
        if (bufferPool[ALLY_KINGS_HEAD + 3] != NULL_ENC10) {
            int x = bufferPool[ALLY_KINGS_HEAD + 3];
            int y = bufferPool[RAT_KING_METADATA_HEAD + 3];
            allyRatKingLocs.add(new MapLocation(x, y));
        }
        if (bufferPool[ALLY_KINGS_HEAD + 4] != NULL_ENC10) {
            int x = bufferPool[ALLY_KINGS_HEAD + 4];
            int y = bufferPool[RAT_KING_METADATA_HEAD + 4];
            allyRatKingLocs.add(new MapLocation(x, y));
        }
    }

    // TODO always does all 10 cats!
    private static void readCatLocs() {
        if (bufferPool[CAT_LOCS_HEAD] != NULL_ENC10) {
            seenCats.add(0);
            catLocs[0] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 0]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 1] != NULL_ENC10) {
            seenCats.add(1);
            catLocs[1] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 1]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 2] != NULL_ENC10) {
            seenCats.add(2);
            catLocs[2] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 2]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 3] != NULL_ENC10) {
            seenCats.add(3);
            catLocs[3] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 3]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 4] != NULL_ENC10) {
            seenCats.add(4);
            catLocs[4] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 4]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 5] != NULL_ENC10) {
            seenCats.add(5);
            catLocs[5] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 5]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 6] != NULL_ENC10) {
            seenCats.add(6);
            catLocs[6] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 6]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 7] != NULL_ENC10) {
            seenCats.add(7);
            catLocs[7] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 7]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 8] != NULL_ENC10) {
            seenCats.add(8);
            catLocs[8] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 8]);
        }
        if (bufferPool[CAT_LOCS_HEAD + 9] != NULL_ENC10) {
            seenCats.add(9);
            catLocs[9] = int10ToLoc(bufferPool[CAT_LOCS_HEAD + 9]);
        }
    }

    public static void writeCheeseMine(MapLocation loc, int index) {
        System.out.println("I wrote the cheese mine globally");
        writeToBufferPool(CHEESE_MINES_HEAD + index, locToInt10(loc));
    }

    // TODO always does all 10 cheese mines!
    private static void readCheeseMines() throws GameActionException {
        if (bufferPool[CHEESE_MINES_HEAD] != NULL_ENC10) {
            if (cheeseMineLocs[0] == null) {
                cheeseMineLocs[0] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 0]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[0]);
            }
            rc.setIndicatorDot(cheeseMineLocs[0], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 1] != NULL_ENC10) {
            if (cheeseMineLocs[1] == null) {
                cheeseMineLocs[1] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 1]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[1]);
            }
            rc.setIndicatorDot(cheeseMineLocs[1], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 2] != NULL_ENC10) {
            if (cheeseMineLocs[2] == null) {
                cheeseMineLocs[2] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 2]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[2]);
            }
            rc.setIndicatorDot(cheeseMineLocs[2], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 3] != NULL_ENC10) {
            if (cheeseMineLocs[3] == null) {
                cheeseMineLocs[3] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 3]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[3]);
            }
            rc.setIndicatorDot(cheeseMineLocs[3], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 4] != NULL_ENC10) {
            if (cheeseMineLocs[4] == null) {
                cheeseMineLocs[4] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 4]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[4]);
            }
            rc.setIndicatorDot(cheeseMineLocs[4], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 5] != NULL_ENC10) {
            if (cheeseMineLocs[5] == null) {
                cheeseMineLocs[5] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 5]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[5]);
            }
            rc.setIndicatorDot(cheeseMineLocs[5], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 6] != NULL_ENC10) {
            if (cheeseMineLocs[6] == null) {
                cheeseMineLocs[6] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 6]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[6]);
            }
            rc.setIndicatorDot(cheeseMineLocs[6], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 7] != NULL_ENC10) {
            if (cheeseMineLocs[7] == null) {
                cheeseMineLocs[7] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 7]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[7]);
            }
            rc.setIndicatorDot(cheeseMineLocs[7], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 8] != NULL_ENC10) {
            if (cheeseMineLocs[8] == null) {
                cheeseMineLocs[8] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 8]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[8]);
            }
            rc.setIndicatorDot(cheeseMineLocs[8], 255, 0, 255);
        }
        if (bufferPool[CHEESE_MINES_HEAD + 9] != NULL_ENC10) {
            if (cheeseMineLocs[9] == null) {
                cheeseMineLocs[9] = int10ToLoc(bufferPool[CHEESE_MINES_HEAD + 9]);
                MacroData.addCheeseMineLocationToSet(cheeseMineLocs[9]);
            }
            rc.setIndicatorDot(cheeseMineLocs[9], 255, 0, 255);
        }

        lastCheeseMineRefreshRound = round;
    }

    private static void readKingFormationLoc() throws GameActionException {
        if (allyRatKingLocs.size > 1 && round > 1200) {
            updateKingFormationLoc(null);
            return;
        }
        MacroData.ratKingFormationLoc =
                bufferPool[KING_FORMATION_LOC] == NULL_ENC10
                        ? null
                        : int10ToLoc(bufferPool[KING_FORMATION_LOC]);
    }

    private static void readSymmetry() {
        int symmMask = bufferPool[SYMMETRY_TRACKER] & 0b111;
        isHSymPossible = (symmMask & 0b1) != 0;
        isVSymPossible = (symmMask & 0b10) != 0;
        isRSymPossible = (symmMask & 0b100) != 0;

        if (!isHSymPossible && Navigation.canSymH) Navigation.elimSymH();
        if (!isVSymPossible && Navigation.canSymV) Navigation.elimSymV();
        if (!isRSymPossible && Navigation.canSymR) Navigation.elimSymR();
    }

    // TODO opt
    //    private static void readMessageQueue() {
    //        lastWrittenMessage = bufferPool[QUEUE_LAST_MESSAGE];
    //        if (lastWrittenMessage < QUEUE_HEAD || lastWrittenMessage >= QUEUE_TAIL)
    //            lastWrittenMessage = QUEUE_HEAD;
    //
    //        while (lastReadMessage != lastWrittenMessage) {
    //            lastReadMessage += 1;
    //            if (lastReadMessage >= QUEUE_TAIL) lastReadMessage = QUEUE_HEAD;
    //
    //            messagesToProcess.add(bufferPool[lastReadMessage]);
    //        }
    //    }

    //    // TODO impl + opt
    //    private static void processMessages() {
    //        while (!messagesToProcess.isEmpty()) {
    //            int message = messagesToProcess.poll();
    //            // TODO
    //        }
    //    }

    private static void processSqueaks() throws GameActionException {
        for (Message squeak : rc.readSqueaks(round - 1)) {
            switch (squeak.getBytes() & SQUEAK_TYPE_MASK) {
                case SQTYPE_CAT:
                    processSqueakCat(squeak);
                    break;
                case SQTYPE_COMBAT:
                    processSqueakCombat(squeak);
                    break;
                case SQTYPE_CHEESE_MINE:
                    processSqueakCheeseMine(squeak);
                    break;
                case SQTYPE_MINE_SAFETY:
                    processSqueakMineSafety(squeak);
                    break;
                case SQTYPE_ENEMY_KING:
                    processSqueakEnemyKing(squeak);
                    break;
                case SQTYPE_SYMMETRY_UPDATE:
                    processSqueakSymmetryUpdate(squeak);
                    break;
                default:
                    break;
            }
        }
    }

    private static void processSqueakCat(Message squeak) {
        MacroData.updateCatWarning(int12ToLoc(squeak.getBytes() >> SQUEAK_LOC_OFFSET));
    }

    private static void processSqueakCombat(Message squeak) {
        MacroData.updateNearbyCombat(int12ToLoc(squeak.getBytes() >> SQUEAK_LOC_OFFSET));
    }

    private static void processSqueakEnemyKing(Message squeak) {}

    private static void processSqueakCheeseMine(Message squeak) {
        // for now, ignore if we are not rat king
        // the data will be populated to the global array, in which then baby rats will read
        if (rc.getType() != UnitType.RAT_KING) return;

        int locBytes = squeak.getBytes() >> SQUEAK_LOC_OFFSET;
        MapLocation cheeseMineLoc = int12ToLoc(locBytes);
        MacroData.writeCheeseMineToGlobal(cheeseMineLoc);
    }

    private static void processSqueakMineSafety(Message squeak) throws GameActionException {
        if (rc.getType() != UnitType.RAT_KING) return;

        boolean isSafe = ((squeak.getBytes() >> SQUEAK_AUX_OFFSET) & 1) == 1;
        MapLocation mineLoc = int12ToLoc(squeak.getBytes() >> SQUEAK_LOC_OFFSET);
        MacroData.updateMineSafety(mineLoc, isSafe);
    }

    private static void processSqueakSymmetryUpdate(Message squeak) {
        if (rc.getType() != UnitType.RAT_KING) return;
        int bytes = squeak.getBytes();
        bytes >>= SQUEAK_AUX_OFFSET;
        if (isRSymPossible && (bytes & 0b100) == 0) {
            isRSymPossible = false;
            Navigation.elimSymR();
        }
        if (isVSymPossible && (bytes & 0b10) == 0) {
            isVSymPossible = false;
            Navigation.elimSymV();
        }
        if (isHSymPossible && (bytes & 0b1) == 0) {
            isHSymPossible = false;
            Navigation.elimSymH();
        }
        updateSymmetry();
    }

    // * ----------------------------------- //
    // * util methods                        //
    // * ----------------------------------- //

    private static int wipeBits(int message, int left, int length) {
        int mask = ~(Utils.BASIC_INT_MASKS[length] << left);
        return message & mask;
    }

    private static int overwriteBits(int message, int newValue, int left, int length) {
        int sectionWiped = wipeBits(message, left, length);
        return (sectionWiped | (newValue << left));
    }

    private static int readBits(int message, int left, int length) {
        return (message >> left) & Utils.BASIC_INT_MASKS[length];
    }

    // 0 <= message < 1024
    private static void writeToBufferPool(int idx, int message) {
        assert (message >= 0 && message < 1024);
        bufferPool[idx] = message;
        dirtyFlags[idx] = true;
    }

    private static void initBufferPool() throws GameActionException {
        dirtyFlags = new boolean[64];
        bufferPool[0] = rc.readSharedArray(0);
        bufferPool[1] = rc.readSharedArray(1);
        bufferPool[2] = rc.readSharedArray(2);
        bufferPool[3] = rc.readSharedArray(3);
        bufferPool[4] = rc.readSharedArray(4);
        bufferPool[5] = rc.readSharedArray(5);
        bufferPool[6] = rc.readSharedArray(6);
        bufferPool[7] = rc.readSharedArray(7);
        bufferPool[8] = rc.readSharedArray(8);
        bufferPool[9] = rc.readSharedArray(9);
        bufferPool[10] = rc.readSharedArray(10);
        bufferPool[11] = rc.readSharedArray(11);
        bufferPool[12] = rc.readSharedArray(12);
        bufferPool[13] = rc.readSharedArray(13);
        bufferPool[14] = rc.readSharedArray(14);
        bufferPool[15] = rc.readSharedArray(15);
        bufferPool[16] = rc.readSharedArray(16);
        bufferPool[17] = rc.readSharedArray(17);
        bufferPool[18] = rc.readSharedArray(18);
        bufferPool[19] = rc.readSharedArray(19);
        bufferPool[20] = rc.readSharedArray(20);
        bufferPool[21] = rc.readSharedArray(21);
        bufferPool[22] = rc.readSharedArray(22);
        bufferPool[23] = rc.readSharedArray(23);
        bufferPool[24] = rc.readSharedArray(24);
        bufferPool[25] = rc.readSharedArray(25);
        bufferPool[26] = rc.readSharedArray(26);
        bufferPool[27] = rc.readSharedArray(27);
        bufferPool[28] = rc.readSharedArray(28);
        bufferPool[29] = rc.readSharedArray(29);
        bufferPool[30] = rc.readSharedArray(30);
        bufferPool[31] = rc.readSharedArray(31);
        bufferPool[32] = rc.readSharedArray(32);
        bufferPool[33] = rc.readSharedArray(33);
        bufferPool[34] = rc.readSharedArray(34);
        bufferPool[35] = rc.readSharedArray(35);
        bufferPool[36] = rc.readSharedArray(36);
        bufferPool[37] = rc.readSharedArray(37);
        bufferPool[38] = rc.readSharedArray(38);
        bufferPool[39] = rc.readSharedArray(39);
        bufferPool[40] = rc.readSharedArray(40);
        bufferPool[41] = rc.readSharedArray(41);
        bufferPool[42] = rc.readSharedArray(42);
        bufferPool[43] = rc.readSharedArray(43);
        bufferPool[44] = rc.readSharedArray(44);
        bufferPool[45] = rc.readSharedArray(45);
        bufferPool[46] = rc.readSharedArray(46);
        bufferPool[47] = rc.readSharedArray(47);
        bufferPool[48] = rc.readSharedArray(48);
        bufferPool[49] = rc.readSharedArray(49);
        bufferPool[50] = rc.readSharedArray(50);
        bufferPool[51] = rc.readSharedArray(51);
        bufferPool[52] = rc.readSharedArray(52);
        bufferPool[53] = rc.readSharedArray(53);
        bufferPool[54] = rc.readSharedArray(54);
        bufferPool[55] = rc.readSharedArray(55);
        bufferPool[56] = rc.readSharedArray(56);
        bufferPool[57] = rc.readSharedArray(57);
        bufferPool[58] = rc.readSharedArray(58);
        bufferPool[59] = rc.readSharedArray(59);
        bufferPool[60] = rc.readSharedArray(60);
        bufferPool[61] = rc.readSharedArray(61);
        bufferPool[62] = rc.readSharedArray(62);
        bufferPool[63] = rc.readSharedArray(63);
    }

    private static void flushBufferPool() throws GameActionException {
        if (dirtyFlags[0]) rc.writeSharedArray(0, bufferPool[0]);
        if (dirtyFlags[1]) rc.writeSharedArray(1, bufferPool[1]);
        if (dirtyFlags[2]) rc.writeSharedArray(2, bufferPool[2]);
        if (dirtyFlags[3]) rc.writeSharedArray(3, bufferPool[3]);
        if (dirtyFlags[4]) rc.writeSharedArray(4, bufferPool[4]);
        if (dirtyFlags[5]) rc.writeSharedArray(5, bufferPool[5]);
        if (dirtyFlags[6]) rc.writeSharedArray(6, bufferPool[6]);
        if (dirtyFlags[7]) rc.writeSharedArray(7, bufferPool[7]);
        if (dirtyFlags[8]) rc.writeSharedArray(8, bufferPool[8]);
        if (dirtyFlags[9]) rc.writeSharedArray(9, bufferPool[9]);
        if (dirtyFlags[10]) rc.writeSharedArray(10, bufferPool[10]);
        if (dirtyFlags[11]) rc.writeSharedArray(11, bufferPool[11]);
        if (dirtyFlags[12]) rc.writeSharedArray(12, bufferPool[12]);
        if (dirtyFlags[13]) rc.writeSharedArray(13, bufferPool[13]);
        if (dirtyFlags[14]) rc.writeSharedArray(14, bufferPool[14]);
        if (dirtyFlags[15]) rc.writeSharedArray(15, bufferPool[15]);
        if (dirtyFlags[16]) rc.writeSharedArray(16, bufferPool[16]);
        if (dirtyFlags[17]) rc.writeSharedArray(17, bufferPool[17]);
        if (dirtyFlags[18]) rc.writeSharedArray(18, bufferPool[18]);
        if (dirtyFlags[19]) rc.writeSharedArray(19, bufferPool[19]);
        if (dirtyFlags[20]) rc.writeSharedArray(20, bufferPool[20]);
        if (dirtyFlags[21]) rc.writeSharedArray(21, bufferPool[21]);
        if (dirtyFlags[22]) rc.writeSharedArray(22, bufferPool[22]);
        if (dirtyFlags[23]) rc.writeSharedArray(23, bufferPool[23]);
        if (dirtyFlags[24]) rc.writeSharedArray(24, bufferPool[24]);
        if (dirtyFlags[25]) rc.writeSharedArray(25, bufferPool[25]);
        if (dirtyFlags[26]) rc.writeSharedArray(26, bufferPool[26]);
        if (dirtyFlags[27]) rc.writeSharedArray(27, bufferPool[27]);
        if (dirtyFlags[28]) rc.writeSharedArray(28, bufferPool[28]);
        if (dirtyFlags[29]) rc.writeSharedArray(29, bufferPool[29]);
        if (dirtyFlags[30]) rc.writeSharedArray(30, bufferPool[30]);
        if (dirtyFlags[31]) rc.writeSharedArray(31, bufferPool[31]);
        if (dirtyFlags[32]) rc.writeSharedArray(32, bufferPool[32]);
        if (dirtyFlags[33]) rc.writeSharedArray(33, bufferPool[33]);
        if (dirtyFlags[34]) rc.writeSharedArray(34, bufferPool[34]);
        if (dirtyFlags[35]) rc.writeSharedArray(35, bufferPool[35]);
        if (dirtyFlags[36]) rc.writeSharedArray(36, bufferPool[36]);
        if (dirtyFlags[37]) rc.writeSharedArray(37, bufferPool[37]);
        if (dirtyFlags[38]) rc.writeSharedArray(38, bufferPool[38]);
        if (dirtyFlags[39]) rc.writeSharedArray(39, bufferPool[39]);
        if (dirtyFlags[40]) rc.writeSharedArray(40, bufferPool[40]);
        if (dirtyFlags[41]) rc.writeSharedArray(41, bufferPool[41]);
        if (dirtyFlags[42]) rc.writeSharedArray(42, bufferPool[42]);
        if (dirtyFlags[43]) rc.writeSharedArray(43, bufferPool[43]);
        if (dirtyFlags[44]) rc.writeSharedArray(44, bufferPool[44]);
        if (dirtyFlags[45]) rc.writeSharedArray(45, bufferPool[45]);
        if (dirtyFlags[46]) rc.writeSharedArray(46, bufferPool[46]);
        if (dirtyFlags[47]) rc.writeSharedArray(47, bufferPool[47]);
        if (dirtyFlags[48]) rc.writeSharedArray(48, bufferPool[48]);
        if (dirtyFlags[49]) rc.writeSharedArray(49, bufferPool[49]);
        if (dirtyFlags[50]) rc.writeSharedArray(50, bufferPool[50]);
        if (dirtyFlags[51]) rc.writeSharedArray(51, bufferPool[51]);
        if (dirtyFlags[52]) rc.writeSharedArray(52, bufferPool[52]);
        if (dirtyFlags[53]) rc.writeSharedArray(53, bufferPool[53]);
        if (dirtyFlags[54]) rc.writeSharedArray(54, bufferPool[54]);
        if (dirtyFlags[55]) rc.writeSharedArray(55, bufferPool[55]);
        if (dirtyFlags[56]) rc.writeSharedArray(56, bufferPool[56]);
        if (dirtyFlags[57]) rc.writeSharedArray(57, bufferPool[57]);
        if (dirtyFlags[58]) rc.writeSharedArray(58, bufferPool[58]);
        if (dirtyFlags[59]) rc.writeSharedArray(59, bufferPool[59]);
        if (dirtyFlags[60]) rc.writeSharedArray(60, bufferPool[60]);
        if (dirtyFlags[61]) rc.writeSharedArray(61, bufferPool[61]);
        if (dirtyFlags[62]) rc.writeSharedArray(62, bufferPool[62]);
        if (dirtyFlags[63]) rc.writeSharedArray(63, bufferPool[63]);
    }

    private static void setupBuffer() throws GameActionException {
        System.out.println("set up buffer!");

        // 1023 represents null location
        for (int i = ALLY_KINGS_HEAD; i < CHEESE_MINES_TAIL; i++) {
            rc.writeSharedArray(i, NULL_ENC10);
        }
        rc.writeSharedArray(SYMMETRY_TRACKER, 0b111);
        rc.writeSharedArray(KING_FORMATION_LOC, NULL_ENC10);

        //        rc.writeSharedArray(QUEUE_LAST_MESSAGE, QUEUE_TAIL - 1);
    }
}
