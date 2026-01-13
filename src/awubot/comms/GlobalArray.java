package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.FastArrayList;

import awubot.CellTracker;
import awubot.util.Profiler;
import awubot.util.Rng;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.*;

public class GlobalArray {
    public static final int N = 64;
    private static final int M = 10;

    /*
      LAYOUT:
      0: (unused - global rat counter)
      1: symmetry
      2-6: rat king locations (rounded to 2x2)
      7: rat king turn counter (00, 01, 10 for alive) and (11) for dead
      8-12: rat spawn roles (5 slots for 5 kings)
      13: formation target location (encoded 2x2)
      14: formation mine location (encoded 2x2)
      15: formation coordinator king ID
      16-20: king mine assignments (5 slots for 5 kings)
      32-63: cheese mines (count upwards from 32)
    */

    // private static final int COUNTER = 0;
    private static final int SYMMETRY = 1;
    private static final int RAT_KING_LOC = 2;
    private static final int RAT_KING_TURN_COUNTER = 7;
    // empty space 8 - 12 for now
    private static final int FORMATION_TARGET = 13;
    private static final int FORMATION_MINE = 14;
    private static final int FORMATION_COORDINATOR = 15;
    private static final int KING_MINE_ASSIGNMENT = 16;  // 5 slots for 5 kings (indices 16-20)

    // layout: 
    // lower bits: store target rat id modulo 255 (255 = null)
    // upper bits: strategy index (0 to 3)
    private static final int RAT_SPAWN_ROLE = 21;  // spam until MIN_CHEESE is hit
    private static final int MIN_CHEESE = 32;
    public static FastIndexedIntSet cheeseMines = new FastIndexedIntSet();
    public static MapLocation nearestRatKing;
    public static int nearestRatKingId;

    private static boolean isFirstKingInTurn;

    private static List<Integer> strategyQueue = new ArrayList<>();

    public static void writeSharedArray(int i, int x) throws GameActionException {
        // if (i == 21) {
            // println("WROTE TO " + i + " " + x);
        // }
        rc.writeSharedArray(i, x);
    }

    public static void initGlobalArray() throws GameActionException {
        if (isRatKing && time == 1) {
            writeSharedArray(RAT_KING_TURN_COUNTER, 1023);
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
        if (isRatKing) {
            updateRatKingTurnCounters();
            if (isFirstKingInTurn) {
                for (int i = RAT_SPAWN_ROLE; i < MIN_CHEESE; i++) {
                    writeSharedArray(i, 1023);
                }
            }
        }
        if (isRatKing) {
            for (int x : strategyQueue) _writeSpawnStrategy(x);
            strategyQueue.clear();
        }
        checkSymmetry();
        readCheeseMines();
        findNearestRatKing();
        if (!isRatKing) {

        }
    }

    public static void postTick() throws GameActionException {
        if (isRatKing) {
            if (cur_sym == 0 && CellTracker.isSymmetryKnown()) {
                writeSharedArray(SYMMETRY, CellTracker.getSymmetry().ordinal());
            }
            writeCheeseMines();
            writeRatLoc(kingId);
        }
    }

    private static void updateRatKingTurnCounters() throws GameActionException {
        // check living
        isFirstKingInTurn = true;
        int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        for (int j = 0; j < 5; j++, x >>= 2) {
            int t = (x & 0b11);
            if (t == time % 3) isFirstKingInTurn = false;
            if (t == (time + 2) % 3) {
                writeSharedArray(RAT_KING_TURN_COUNTER, rc.readSharedArray(RAT_KING_TURN_COUNTER) | (0b11 << (2 * j)));
            }
        }
        if (isFirstKingInTurn) {
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
    }
    
    public static void readCheeseMines() throws GameActionException {
        int i = N - 1;
        while (i >= MIN_CHEESE) {
            int x  = rc.readSharedArray(i);
            if (x == 1023) break;
            cheeseMines.add(x);
            i--;
        }
    }

    public static void writeCheeseMines() throws GameActionException {
        int n = cheeseMines.set.length();
        if (n > 0) {
            int i = (time * (N - MIN_CHEESE)) % n;
            for (int j = 0; j < Math.min(n, N - MIN_CHEESE); j++) {
                int k = (i + j) % n;
                writeSharedArray(N - k - 1, cheeseMines.at(k));
            }
        }
        if (n < N - MIN_CHEESE) {
            writeSharedArray(N - 1 - n, 1023);
        }
    }

    public static boolean seenCheeseMine(int loc) {
        return cheeseMines.contains(loc);
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
            return decode2x2(pos);
        } catch (GameActionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CellTracker.Symmetry peepSymmetry() {
        return CellTracker.Symmetry.values()[cur_sym];
    }

    public static void writeSpawnStrategy(int babyRatID, int stratNum) throws GameActionException {
        assert(0 <= stratNum && stratNum < 4);
        strategyQueue.add((babyRatID % 255) | (stratNum << 8));
    }
    public static void _writeSpawnStrategy(int payload) throws GameActionException {
        int id = payload & 255;
        for (int i = RAT_SPAWN_ROLE; i < MIN_CHEESE; i++) {
            int x = rc.readSharedArray(i);
            int lo = x & 255;
            if (lo == 255) {
                // can write
                writeSharedArray(i, payload);
                return;
            } else if (lo == id) {
                // println("unlucky clashing id's");
                break;
            }
        }
        // println("no more space to write strategies");
    }

    public static int readSpawnStrategy() throws GameActionException {
        int id = rc.getID() % 255;
        // println("reading strategy for " + rc.getID() + " modded " + id);
        for (int i = RAT_SPAWN_ROLE; i < MIN_CHEESE; i++) {
            int x = rc.readSharedArray(i);
            int lo = x & 255;
            // println("read " + i + " for " + lo);
            if (lo == id) {
                return x >>> 8; // retrieve role id
            }
            if (lo == 255) { // null (shouldnt happen)
                assert(false);
            }
        }
        // didnt find - pick one at random
        int i = Rng.mcg() % (MIN_CHEESE - RAT_SPAWN_ROLE);
        // println("didnt find strategy");
        return rc.readSharedArray(i) >>> 8;
    }

    public static void writeRatLoc(int i) {
        try {
            writeSharedArray(i + RAT_KING_LOC, encode2x2() + 1);
            // cycle counter
            int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
            int t = time % 3;
            x &= ~(0b11 << (2 * i));
            x |= t << (2 * i);
            // can be optimised to xor'ing lmao
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
        writeSharedArray(FORMATION_TARGET, aTarget != null ? encode2x2(aTarget) + 1 : 0);
        writeSharedArray(FORMATION_MINE, aMine != null ? encode2x2(aMine) + 1 : 0);
        writeSharedArray(FORMATION_COORDINATOR, aCoordinatorKingId);
    }

    public static MapLocation getFormationTarget() throws GameActionException {
        int myEncoded = rc.readSharedArray(FORMATION_TARGET);
        if (myEncoded == 0) return null;
        return decode2x2(myEncoded - 1);
    }

    public static MapLocation getFormationMine() throws GameActionException {
        int myEncoded = rc.readSharedArray(FORMATION_MINE);
        if (myEncoded == 0) return null;
        return decode2x2(myEncoded - 1);
    }

    public static int getFormationCoordinator() throws GameActionException {
        return rc.readSharedArray(FORMATION_COORDINATOR);
    }

    public static void clearFormation() throws GameActionException {
        writeSharedArray(FORMATION_TARGET, 0);
        writeSharedArray(FORMATION_MINE, 0);
        writeSharedArray(FORMATION_COORDINATOR, 0);
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
