package awubot_us.comms;

import battlecode.common.*;
import static awubot_us.util.Util.*;

import javax.management.RuntimeErrorException;

import awubot_us.CellTracker;
import awubot_us.Game;
import awubot_us.util.Profiler;
import awubot_us.util.Rng;
import awubot_us.util.string.FastIndexedIntSet;

import static awubot_us.Game.*;

public class GlobalArray {
    public static final int N = 64;
    private static final int M = 10;

    /*

    ts hasnt been updated in like 3 weeks

      LAYOUT:
      0: global rat counter
      1: symmetry
      2-6: rat king locations (upper 5 bits of x and y, rounded to 2x2)
      7: rat king LSBs (2 bits per king: bit0=x LSB, bit1=y LSB)
      8: rat king turn counter (00, 01, 10 for alive) and (11) for dead
      9: formation coordinator king ID
      10-11: formation target location (exact, 2 slots)
      12-13: formation mine location (exact, 2 slots)
      14-15: unused
      16-20: king mine assignments (5 slots for 5 kings)
      21-31: rat spawn roles
      32-63: cheese mines (count upwards from 32)
    */

    private static final int COUNTER = 0;
    private static final int SYMMETRY = 1;
    private static final int RAT_KING_LOC = 2;
    private static final int RAT_KING_LSBS = 7;
    private static final int RAT_KING_TURN_COUNTER = 8;
    private static final int FORMATION_COORDINATOR = 9;
    private static final int FORMATION_TARGET = 10;  // uses 2 slots: 10-11
    private static final int FORMATION_MINE = 12;    // uses 2 slots: 12-13
    private static final int ALIVE_RAT_COUNT = 14;
    private static final int KING_MINE_ASSIGNMENT = 16;  // 5 slots for 5 kings (indices 16-20)

    // layout: 
    // lower bits: store target rat id modulo 255 (255 = null)
    // upper bits: strategy index (0 to 3)
    private static final int KING_EXPAND = 21;
    private static final int RAT_SPAWN_INFO = 22;  // spam until MIN_CHEESE is hit
    private static final int MIN_CHEESE = 32;
    public static FastIndexedIntSet globalCheeseMines = new FastIndexedIntSet();
    public static MapLocation nearestRatKing;
    public static int nearestRatKingId;
    public static int ratCounter;
    public static int numberOfRatsAlive;

    private static MapLocation toSpawn0 = null, toSpawn1 = null;
    private static int pendingPayload0 = -1, pendingPayload1 = -1;

    public static void registerSpawnedRat(MapLocation loc, int payload) throws GameActionException {
        println("prepared rat to spawn at " + loc + " with payload " + payload);
        toSpawn1 = loc;
        pendingPayload1 = payload;
    }

    private static void _registerSpawnedRat(MapLocation loc, int payload) throws GameActionException {
        // println("actually registering spawned rat at " + loc + " with payload " + payload);
        assert(0 <= payload && payload < 8192);
        int dx = loc.x - myLocX + 4;
        assert(0 <= dx && dx < 9);
        int dy = loc.y - myLocY + 4;
        assert(0 <= dy && dy < 9);
        int marker = (dx * 9) + dy;

        int lo = payload & 1023, hi = payload >>> 10;
        assert(0 <= hi && hi < 8);
        assert(0 <= lo && lo < 1024);

        writeSharedArray(RAT_SPAWN_INFO + kingId * 2, (hi << 7) | marker);
        writeSharedArray(RAT_SPAWN_INFO + kingId * 2 + 1, lo);
        // println("registered spawned rat at " + loc + " with payload " + payload + " into slots " + (RAT_SPAWN_INFO + kingId * 2) + " and " + (RAT_SPAWN_INFO + kingId * 2 + 1));
        ratCounter++;
    }

    public static void expandKing() throws GameActionException {
        int x = rc.readSharedArray(KING_EXPAND);
        x |= (1 << kingId);
        rc.writeSharedArray(KING_EXPAND, x);
    }

    public static void contractKing() throws GameActionException {
        int x = rc.readSharedArray(KING_EXPAND);
        x &= ~(1 << kingId);
        rc.writeSharedArray(KING_EXPAND, x);
    }

    public static boolean isKingExpanded(int i) throws GameActionException {
        int x = rc.readSharedArray(KING_EXPAND);
        return (1 & x >> kingId) != 0;
    }

    public static int loadSpawnInfo() throws GameActionException {
        for (int i = RAT_SPAWN_INFO, king = 0; i < MIN_CHEESE; i += 2, king++) {
            int x = rc.readSharedArray(i);
            // println("looking for spawn info in slot " + i + ": " + x);
            if (x == 1023) continue;
            int marker = x & 127;
            MapLocation loc = getRatKing(king);
            // println("location: " + loc);
            if (loc == null) continue;
            int dx = (marker / 9) - 4;
            int dy = (marker % 9) - 4;
            // println("dx, dy: " + dx + ", " + dy);
            if (!loc.translate(dx, dy).equals(myLoc)) continue;
            int lo = rc.readSharedArray(i + 1);
            int hi = x >>> 7;
            int indexInList = (i - RAT_SPAWN_INFO) / 2;
            myIndex = (ratCounter - 1 - indexInList + 1024) % 1024;
            return (hi << 10) | lo;
        }
        println("FAILED TO FIND MY SPAWN INFO");
        return -1;
    }

    public static void writeSharedArray(int i, int x) throws GameActionException {
        rc.writeSharedArray(i, x);
    }

    public static void init() throws GameActionException {
        if (isRatKing && time == 1) {
            writeSharedArray(RAT_KING_TURN_COUNTER, 1023);
            writeSharedArray(RAT_KING_LSBS, 0);
            writeSharedArray(N - 1, 1023);
            for (int i = 0; i < 5; i++) {
                writeSharedArray(i + RAT_KING_LOC, 1023);
            }
            initRatKing();
        }
    }

    public static void initRatKing() throws GameActionException {
        int i = findRatKingSlot();
        kingId = i;
    }

    public static void preTick() throws GameActionException {
        ratCounter = rc.readSharedArray(COUNTER);
        numberOfRatsAlive = rc.readSharedArray(ALIVE_RAT_COUNT);
        if (isRatKing) {
            if (isFirstKingInRound()) {
                for (int i = RAT_SPAWN_INFO; i < MIN_CHEESE; i++) {
                    writeSharedArray(i, 1023);
                }
            }
            toSpawn0 = toSpawn1;
            pendingPayload0 = pendingPayload1;
            toSpawn1 = null;
            pendingPayload1 = -1;
            markKingAlive();
            updateRatKingTurnCounters();
        }
        checkSymmetry();
        readCheeseMines();
        findNearestRatKing();
    }

    public static boolean isFirstKingInRound() throws GameActionException {
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);  
        for (int i = 0; i < 5; i++, x >>>= 2) {
            int bits = x & 0b11;
            if (bits == time % 3) return false;
        }
        return true;
    }

    public static void postTick() throws GameActionException {
        if (isRatKing) {
            if (cur_sym == 0 && CellTracker.isSymmetryKnown()) {
                writeSharedArray(SYMMETRY, CellTracker.getSymmetry().ordinal());
            }
            writeCheeseMines();
            writeMyLoc();
            if (toSpawn0 != null) {
                assert(pendingPayload0 != -1);
                _registerSpawnedRat(toSpawn0, pendingPayload0);
            }
            rc.writeSharedArray(COUNTER, ratCounter);
            rc.writeSharedArray(ALIVE_RAT_COUNT, (rc.getCurrentRatCost() - 10) * 2 / 5);

        }
    }

    private static void updateRatKingTurnCounters() throws GameActionException {
        // check living
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        for (int j = 0; j < 5; j++, x >>= 2) {
            int t = (x & 0b11);
            if (t == (time + 2) % 3) {
                if (j == kingId) {
                    rc.setTimelineMarker("RAT KING THOUGHT THEY DIED", 255, 0, 0);
                }
                writeSharedArray(RAT_KING_TURN_COUNTER, rc.readSharedArray(RAT_KING_TURN_COUNTER) | (0b11 << (2 * j)));
                writeSharedArray(RAT_KING_LOC + j, 1023);
            }
        }
    }

    public static void findNearestRatKing() {
        nearestRatKing = null;
        for (int i = 0; i < 5; i++) {
            MapLocation king = getRatKing(i);
            if (king == null) continue;
            if (nearestRatKing == null || nearestRatKing.distanceSquaredTo(myLoc) > king.distanceSquaredTo(myLoc)) {
                nearestRatKing = king;
                nearestRatKingId = i;
            }
        }
        if (nearestRatKing == null) {
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! DIDNT FIND ANY RAT KING!!");
        }
    }

    public static int countRatKings() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            MapLocation king = getRatKing(i);
            if (king == null) continue;
            ret++;
        }
        return ret;
    }

    public static void readCheeseMines() throws GameActionException {
        int i = N - 1;
        while (i >= MIN_CHEESE) {
            int x  = rc.readSharedArray(i);
            if (x == 1023) break;
            globalCheeseMines.add(x);
            Game.knownCheeseMines.add(x);
            i--;
        }
    }

    public static void writeCheeseMines() throws GameActionException {
        int n = globalCheeseMines.set.length();
        if (n > 0) {
            int i = (time * (N - MIN_CHEESE)) % n;
            for (int j = 0; j < Math.min(n, N - MIN_CHEESE); j++) {
                int k = (i + j) % n;
                writeSharedArray(N - k - 1, globalCheeseMines.at(k));
            }
        }
        if (n < N - MIN_CHEESE) {
            writeSharedArray(N - 1 - n, 1023);
        }
    }

    public static boolean seenCheeseMine(int loc) {
        return globalCheeseMines.contains(loc);
    }

    private static int cur_sym = 0;
    public static void checkSymmetry() throws GameActionException {
        if (cur_sym != 0) return;
        cur_sym = rc.readSharedArray(SYMMETRY);
        if (cur_sym != 0) {
            CellTracker.setSymmetry(cur_sym);
        }
    }

    public static MapLocation getRatKing(int i) {
        try {
            int pos = rc.readSharedArray(i + RAT_KING_LOC);
            if (pos == 1023) return null;
            int lsbs = rc.readSharedArray(RAT_KING_LSBS);
            int myLsb = (lsbs >>> (2 * i)) & 0b11;
            int x = ((pos & 0b11111) << 1) | (myLsb & 1);
            int y = ((pos >>> 5) << 1) | ((myLsb >>> 1) & 1);
            return new MapLocation(x, y);
        } catch (GameActionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CellTracker.Symmetry peepSymmetry() {
        return CellTracker.Symmetry.values()[cur_sym];
    }

    public static void markKingAlive() throws GameActionException {
        // cycle counter
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        int t = time % 3;
        x &= ~(0b11 << (2 * kingId));
        x |= t << (2 * kingId);
        writeSharedArray(RAT_KING_TURN_COUNTER, x);
    }

    public static void writeMyLoc() throws GameActionException {
        println("wrote my loc as rat king: " + myLoc);
        writeSharedArray(kingId + RAT_KING_LOC, encode2x2());
        // write LSBs for exact position
        int lsbs = rc.readSharedArray(RAT_KING_LSBS);
        int myLsb = (myLocX & 1) | ((myLocY & 1) << 1);
        lsbs &= ~(0b11 << (2 * kingId));
        lsbs |= myLsb << (2 * kingId);
        writeSharedArray(RAT_KING_LSBS, lsbs);
    }

    public static int findRatKingSlot() throws GameActionException {
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        int i = 0;
        while (i < 5) {
            if ((x & 0b11) == 0b11) {
                return i;
            }
            x >>= 2;
            i++;
        }
        return i;
    }

    // FORMATION

    public static void writeFormationTarget(MapLocation aTarget, MapLocation aMine, int aCoordinatorKingId) throws GameActionException {
        int[] targetEncoded = encodeExact(aTarget);
        int[] mineEncoded = encodeExact(aMine);
        writeSharedArray(FORMATION_TARGET, targetEncoded[0]);
        writeSharedArray(FORMATION_TARGET + 1, targetEncoded[1]);
        writeSharedArray(FORMATION_MINE, mineEncoded[0]);
        writeSharedArray(FORMATION_MINE + 1, mineEncoded[1]);
        writeSharedArray(FORMATION_COORDINATOR, aCoordinatorKingId);
    }

    public static MapLocation getFormationTarget() throws GameActionException {
        int slot1 = rc.readSharedArray(FORMATION_TARGET);
        int slot2 = rc.readSharedArray(FORMATION_TARGET + 1);
        return decodeExact(slot1, slot2);
    }

    public static MapLocation getFormationMine() throws GameActionException {
        int slot1 = rc.readSharedArray(FORMATION_MINE);
        int slot2 = rc.readSharedArray(FORMATION_MINE + 1);
        return decodeExact(slot1, slot2);
    }

    public static int getFormationCoordinator() throws GameActionException {
        return rc.readSharedArray(FORMATION_COORDINATOR);
    }

    public static void clearFormation() throws GameActionException {
        writeSharedArray(FORMATION_COORDINATOR, 0);
        writeSharedArray(FORMATION_TARGET, 0);
        writeSharedArray(FORMATION_TARGET + 1, 0);
        writeSharedArray(FORMATION_MINE, 0);
        writeSharedArray(FORMATION_MINE + 1, 0);
    }

    private static int[] encodeExact(MapLocation aLoc) {
        if (aLoc == null) return new int[] {0, 0};
        return new int[] {aLoc.x + 1, aLoc.y + 1};
    }

    private static MapLocation decodeExact(int aSlot1, int aSlot2) {
        if (aSlot1 == 0 && aSlot2 == 0) return null;
        return new MapLocation(aSlot1 - 1, aSlot2 - 1);
    }

    public static void writeKingMineAssignment(int aKingId, MapLocation aMine) throws GameActionException {
        if (aKingId >= 0 && aKingId < 5) {
            writeSharedArray(KING_MINE_ASSIGNMENT + aKingId, aMine != null ? encode2x2(aMine) + 1 : 0);
        }
    }

    public static MapLocation getKingMineAssignment(int aKingId) throws GameActionException {
        if (aKingId >= 0 && aKingId < 5) {
            int myEncoded = rc.readSharedArray(KING_MINE_ASSIGNMENT + aKingId);
            if (myEncoded == 0) return null;
            return decode2x2(myEncoded - 1);
        }
        return null;
    }

    public static boolean isMineAssignedToAnyKing(MapLocation aMine) throws GameActionException {
        if (aMine == null) return false;
        for (int i = 0; i < 5; i++) {
            MapLocation myKingMine = getKingMineAssignment(i);
            if (myKingMine != null && myKingMine.distanceSquaredTo(aMine) < 25) {
                return true;
            }
        }
        return false;
    }

    public static int countActiveKings() throws GameActionException {
        int myCount = 0;
        int myTurnCounter = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        for (int i = 0; i < 5; i++) {
            if ((myTurnCounter & 0b11) != 0b11) {
                myCount++;
            }
            myTurnCounter >>= 2;
        }
        return myCount;
    }
}
