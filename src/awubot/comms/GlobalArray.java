package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import org.apache.commons.collections.FastArrayList;

import awubot.CellTracker;
import awubot.util.BudgetMap;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.*;

public class GlobalArray {
    public static final int N = 64;
    private static final int M = 10;

    /*

    LAYOUT:
    // 0: global rat counter
    1: symmetry
    2-6: rat king locations (rounded to 2x2) 
    7: rat king turn counter (00, 01, 10 for alive) and (11) for dead
    

    cheese mines count upwards

    */

    // private static final int COUNTER = 0;
    private static final int SYMMETRY = 1;
    private static final int RAT_KING_LOC = 2;
    private static final int RAT_KING_TURN_COUNTER = 7;
    private static final int MIN_CHEESE = 32;
    public static FastIndexedIntSet cheeseMines = new FastIndexedIntSet();
    
    public static void initGlobalArray() throws GameActionException {
        if (isRatKing) {
            int i = findRatKingSlot();
            kingId = i;
        } else {
            kingId = -1;
        }

        if (isRatKing && time == 1) {
            println("YO IM THE FIRST RAT");
            rc.writeSharedArray(RAT_KING_TURN_COUNTER, 1023);
        }
    }

    public static void preTick() throws GameActionException {
        checkSymmetry();
        if (!isRatKing) {
            
        }
    }

    public static void postTick() throws GameActionException {
        if (isRatKing) {
            if (cur_sym == 0 && CellTracker.isSymmetryKnown()) {
                rc.writeSharedArray(SYMMETRY, CellTracker.getSymmetry().ordinal());
            }
            
        }
    }

    private static int cur_sym = 0;
    public static void checkSymmetry() throws GameActionException {
        if (cur_sym != 0) return;
        cur_sym = rc.readSharedArray(SYMMETRY);
        println("\n\n\n\nFOUND SYMMETRY " + cur_sym + "\n\n\n");
        if (cur_sym != 0) {
            CellTracker.setSymmetry(cur_sym);
        }
    }

    public static MapLocation getRat(int i) {
        try {
            int pos = rc.readSharedArray(i + RAT_KING_LOC);
            return decode2x2(pos);
        } catch (GameActionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CellTracker.Symmetry peepSymmetry() {
        return CellTracker.Symmetry.values()[cur_sym];
    }

    public static void writeRatLoc(int i) {
        try {
            rc.writeSharedArray(i + RAT_KING_LOC, encode2x2());
            // cycle counter
            int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
            int t = time % 3;
            x &= ~(0b11 << (2 * i));
            x |= t << (2 * i);
            // can be optimised to xor'ing lmao

            // check living
            for (int j = 0; j < 5; j++, x >>= 2) {
                if (i == j) continue;
                if ((x & 0b11) == (time + 2) % 3) {
                    rc.writeSharedArray(RAT_KING_TURN_COUNTER, rc.readSharedArray(RAT_KING_TURN_COUNTER) | (0b11 << (2 * j)));
                }
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static int findRatKingSlot() {
        try {
            int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
            int i = 0;
            while (i < 5) {
                if ((x & 0b11) == 0b11) return i;
                i++;
            }
            return i;
        } catch (GameActionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // loc is 2x2 encoded position
    public static boolean seenCheeseMine(int loc) {
        int i = N - 1;
        try {
            while (i >= MIN_CHEESE) {
                int x = rc.readSharedArray(i);
                if (x == 1023) break;
                if (x == loc) return true;
                i--;
            }
            return false;
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }
    }

    // public static void writeCheeseMines() {
    //     BudgetMap.clear();
    //     int i = N - 1;
    //     try {
    //         while (i >= MIN_CHEESE) {
    //             int x = rc.readSharedArray(i);
    //             if (x == 1023) break;
    //             MapLocation loc = decode2x2(x);
    //             BudgetMap.set(loc.x, loc.y, 1);
    //             i--;
    //         }
    //     } catch (GameActionException e) {
    //         e.printStackTrace();
    //     }
    // }
    
}
