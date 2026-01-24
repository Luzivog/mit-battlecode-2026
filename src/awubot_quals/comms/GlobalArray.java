package awubot_quals.comms;

import battlecode.common.*;
import static awubot_quals.util.Util.*;

import awubot_quals.CellTracker;
import awubot_quals.Game;
import awubot_quals.util.Profiler;
import awubot_quals.util.Rng;
import awubot_quals.util.string.FastIndexedIntSet;

import static awubot_quals.Game.*;

public class GlobalArray {
    public static final int N = 64;
    private static final int M = 10;

    /*
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
    private static final int RAT_SPAWN_ROLE = 21;  // spam until MIN_CHEESE is hit
    private static final int MIN_CHEESE = 32;
    public static FastIndexedIntSet globalCheeseMines = new FastIndexedIntSet();
    public static MapLocation nearestRatKing;
    public static int nearestRatKingId;
    public static int ratCounter;
    public static int numberOfRatsAlive;

    private static boolean isFirstKingInRound;
    // private static List<Integer> strategyQueue = new ArrayList<>();

    public static void writeSharedArray(int i, int x) throws GameActionException {
        // if (i == 21) {
            // println("WROTE TO " + i + " " + x);
        // }
        rc.writeSharedArray(i, x);
    }

    public static void initGlobalArray() throws GameActionException {
        if (isRatKing && time == 1) {
            writeSharedArray(RAT_KING_TURN_COUNTER, 1023);
            writeSharedArray(RAT_KING_LSBS, 0);
            writeSharedArray(N - 1, 1023);
            for (int i = 0; i < 5; i++) {
                writeSharedArray(i + RAT_KING_LOC, 1023);
            }
        }

        if (isRatKing) {
            int i = findRatKingSlot();
            kingId = i;
        } else {
            kingId = -1;
        }
    }

    public static void preTick() throws GameActionException {
        ratCounter = rc.readSharedArray(COUNTER);
        numberOfRatsAlive = rc.readSharedArray(ALIVE_RAT_COUNT);
        if (isRatKing) {
            markKingAlive(kingId);
            updateRatKingTurnCounters();
            // if (isFirstKingInRound) {
            //     for (int i = RAT_SPAWN_ROLE; i < MIN_CHEESE; i++) {
            //         writeSharedArray(i, 1023);
            //     }
            // }
            // for (int x : strategyQueue) _writeSpawnStrategy(x);
            // strategyQueue.clear();
        }
        checkSymmetry();
        readCheeseMines();
        findNearestRatKing();
    }

    public static void postTick() throws GameActionException {
        if (isRatKing) {
            if (cur_sym == 0 && CellTracker.isSymmetryKnown()) {
                writeSharedArray(SYMMETRY, CellTracker.getSymmetry().ordinal());
            }
            writeCheeseMines();
            writeRatLoc(kingId);
            rc.writeSharedArray(COUNTER, ratCounter);
            rc.writeSharedArray(ALIVE_RAT_COUNT, (rc.getCurrentRatCost() - 10) * 2 / 5);
        }
    }

    private static void updateRatKingTurnCounters() throws GameActionException {
        // check living
        isFirstKingInRound = true;
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        for (int j = 0; j < 5; j++, x >>= 2) {
            int t = (x & 0b11);
            if (t == time % 3) isFirstKingInRound = false;
            if (t == (time + 2) % 3) {
                if (j == kingId) {
                    rc.setTimelineMarker("RAT KING THOUGHT THEY DIED", 255, 0, 0);
                }
                writeSharedArray(RAT_KING_TURN_COUNTER, rc.readSharedArray(RAT_KING_TURN_COUNTER) | (0b11 << (2 * j)));
                writeSharedArray(RAT_KING_LOC + j, 1023);
            }
        }
        if (isFirstKingInRound) {
            indicate("am first king in turn");
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

    public static void writeRatKing() throws GameActionException {
        for (int i = 0; i < 5; i++) {
            MapLocation king = getRatKing(i);
            if (king == null) {
                writeRatLoc(i);
                return;
            }
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

    // public static void writeSpawnStrategy(int babyRatID, int stratNum) throws GameActionException {
    //     assert(0 <= stratNum && stratNum < 4);
    //     // println("registered spawn strategy for " + babyRatID);
    //     strategyQueue.add((babyRatID % 255) | (stratNum << 8));
    // }
    // public static void _writeSpawnStrategy(int payload) throws GameActionException {
    //     // println("trying to write spawn strategy for payhload " + payload);
    //     ratCounter++;
    //     int id = payload & 255;
    //     for (int i = RAT_SPAWN_ROLE; i < MIN_CHEESE; i++) {
    //         int x = rc.readSharedArray(i);
    //         int lo = x & 255;
    //         if (lo == 255) {
    //             // can write
    //             // println("wrote strategy for " + (payload & 255));
    //             writeSharedArray(i, payload);
    //             return;
    //         } else if (lo == id) {
    //             // println("unlucky clashing id's");
    //             break;
    //         }
    //     }
    //     println("no more space to write");
    // }

    // MUST BE CALLED FOR EVERY RAT
    // public static int readSpawnStrategy() throws GameActionException {
    //     int id = rc.getID() % 255;
    //     // println("reading strategy for " + rc.getID() + " modded " + id);
    //     for (int i = RAT_SPAWN_ROLE; i < MIN_CHEESE; i++) {
    //         int x = rc.readSharedArray(i);
    //         int lo = x & 255;
    //         // println("read " + i + " for " + lo);
    //         if (lo == id) { 
    //             println("read " + i + " counter " + ratCounter);
    //             Game.myIndex = (ratCounter - (i - RAT_SPAWN_ROLE) - 1 + 1024) % 1024;
    //             return x >>> 8; // retrieve role id
    //         }
    //         if (lo == 255) { // null (shouldnt happen)
    //             assert(false);
    //         }
    //     }
    //     // didnt find - pick one at random
    //     int i = Rng.mcg() % (MIN_CHEESE - RAT_SPAWN_ROLE);
    //     // println("didnt find strategy");
    //     Game.myIndex = (ratCounter - i - 1 + 1024) % 1024;
    //     return rc.readSharedArray(i + RAT_SPAWN_ROLE) >>> 8;
    // }

    public static void markKingAlive(int i) throws GameActionException {
        // cycle counter
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        int t = time % 3;
        x &= ~(0b11 << (2 * i));
        x |= t << (2 * i);
        writeSharedArray(RAT_KING_TURN_COUNTER, x);
    }

    public static void writeRatLoc(int i) {
        try {
            writeSharedArray(i + RAT_KING_LOC, encode2x2());
            // write LSBs for exact position
            int lsbs = rc.readSharedArray(RAT_KING_LSBS);
            int myLsb = (myLocX & 1) | ((myLocY & 1) << 1);
            lsbs &= ~(0b11 << (2 * i));
            lsbs |= myLsb << (2 * i);
            writeSharedArray(RAT_KING_LSBS, lsbs);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static int findRatKingSlot() {
        try {
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
        } catch (GameActionException e) {
            e.printStackTrace();
            return -1;
        }
    }

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
