package experiment.comms;

import battlecode.common.*;
import static experiment.util.Util.*;

import experiment.CellTracker;
import experiment.util.BudgetMap;

import static experiment.Game.*;

public class GlobalArray {
    public static final int N = 64;
    private static final int M = 10;

    /*

    LAYOUT:
    0: global rat counter
    1: symmetry
    2-6: rat king locations (rounded to 2x2) 
    7: rat king turn counter (00, 01, 10 for alive) and (11) for dead
    8-12: rat spawn roles per king
    13-17: rat spawn round per king
    18-22: previous rat spawn roles per king
    23-27: previous rat spawn round per king
    

    cheese mines count upwards

    */

    // private static final int COUNTER = 0;
    private static final int SYMMETRY = 1;
    private static final int RAT_KING_LOC = 2;
    private static final int RAT_KING_TURN_COUNTER = 7;
    private static final int MIN_CHEESE = 32;
    private static final int RAT_SPAWN_ROLE = 8;
    private static final int RAT_SPAWN_ROUND = 13;
    private static final int RAT_SPAWN_ROLE_PREV = 18;
    private static final int RAT_SPAWN_ROUND_PREV = 23;
    private static final int MAX_RAT_KINGS = 5;
    
    public static void initGlobalArray() throws GameActionException {
        // Initialize counter to 1023 (all slots "dead") on first round
        if (isRatKing && time == 1) {
            rc.writeSharedArray(RAT_KING_TURN_COUNTER, 1023);
        }

        if (isRatKing) {
            int i = findRatKingSlot();
            // Ensure we don't exceed max kings
            kingId = (i < MAX_RAT_KINGS) ? i : -1;
        } else {
            kingId = -1;
        }
    }

    public static void preTick() throws GameActionException {
        checkSymmetry();
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
        // Guard against invalid king ID
        if (i < 0 || i >= MAX_RAT_KINGS) return;

        try {
            rc.writeSharedArray(i + RAT_KING_LOC, encode2x2());
            // cycle counter
            int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
            int t = time % 3;
            x &= ~(0b11 << (2 * i));
            x |= t << (2 * i);
            rc.writeSharedArray(RAT_KING_TURN_COUNTER, x);

            // check living - mark kings as dead if they haven't updated in 2 rounds
            int staleTime = (time + 2) % 3;
            for (int j = 0; j < MAX_RAT_KINGS; j++) {
                if (i == j) continue;
                int kingBits = (x >> (2 * j)) & 0b11;
                if (kingBits != 0b11 && kingBits == staleTime) {
                    x |= (0b11 << (2 * j));
                    rc.writeSharedArray(RAT_KING_TURN_COUNTER, x);
                }
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static void writeSpawnRole(int kingId, int role, int round) {
        if (kingId < 0 || kingId >= MAX_RAT_KINGS) return;
        try {
            int roleIndex = RAT_SPAWN_ROLE + kingId;
            int roundIndex = RAT_SPAWN_ROUND + kingId;
            int prevRoleIndex = RAT_SPAWN_ROLE_PREV + kingId;
            int prevRoundIndex = RAT_SPAWN_ROUND_PREV + kingId;
            int prevRole = rc.readSharedArray(roleIndex);
            int prevRound = rc.readSharedArray(roundIndex);
            rc.writeSharedArray(prevRoleIndex, prevRole);
            rc.writeSharedArray(prevRoundIndex, prevRound);
            rc.writeSharedArray(roleIndex, role);
            rc.writeSharedArray(roundIndex, round & 1023);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static int readSpawnRoleForKing(MapLocation kingLoc, int round) {
        int kingId = findKingIdForLocation(kingLoc);
        if (kingId < 0) return 0;
        try {
            int roundKey = round & 1023;
            int roleIndex = RAT_SPAWN_ROLE + kingId;
            int roundIndex = RAT_SPAWN_ROUND + kingId;
            int role = rc.readSharedArray(roleIndex);
            int spawnRound = rc.readSharedArray(roundIndex);
            if (spawnRound == roundKey || spawnRound == ((round - 1) & 1023)) {
                return role;
            }
            int prevRoleIndex = RAT_SPAWN_ROLE_PREV + kingId;
            int prevRoundIndex = RAT_SPAWN_ROUND_PREV + kingId;
            int prevRole = rc.readSharedArray(prevRoleIndex);
            int prevRound = rc.readSharedArray(prevRoundIndex);
            if (prevRound == roundKey || prevRound == ((round - 1) & 1023)) {
                return prevRole;
            }
            return 0;
        } catch (GameActionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int findKingIdForLocation(MapLocation kingLoc) {
        if (kingLoc == null) return -1;
        int encoded = encode2x2(kingLoc);
        int counter;
        try {
            counter = rc.readSharedArray(RAT_KING_TURN_COUNTER);
        } catch (GameActionException e) {
            e.printStackTrace();
            return -1;
        }
        for (int i = 0; i < MAX_RAT_KINGS; i++) {
            if (((counter >> (2 * i)) & 0b11) == 0b11) continue;
            try {
                if (rc.readSharedArray(RAT_KING_LOC + i) == encoded) {
                    return i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return -1;
    }

    public static int findRatKingSlot() {
        try {
            int x = rc.readSharedArray(RAT_KING_TURN_COUNTER);
            int i = 0;
            while (i < 5) {
                if ((x & 0b11) == 0b11) return i;
                x >>>= 2;
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
    
    /**
     * Get the nearest known cheese mine location to a given position.
     * Returns null if no mines are known.
     */
    public static MapLocation getNearestCheeseMine(MapLocation from) {
        if (from == null) return null;
        
        MapLocation best = null;
        int bestDist = Integer.MAX_VALUE;
        
        int i = N - 1;
        try {
            while (i >= MIN_CHEESE) {
                int x = rc.readSharedArray(i);
                if (x == 1023) break;
                
                MapLocation mineLoc = decode2x2(x);
                if (mineLoc != null && rc.onTheMap(mineLoc)) {
                    int dist = from.distanceSquaredTo(mineLoc);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = mineLoc;
                    }
                }
                i--;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        
        return best;
    }
    
    /**
     * Get all known cheese mine locations.
     * Returns array of mine locations (may contain nulls at end).
     */
    public static MapLocation[] getAllCheeseMines() {
        MapLocation[] mines = new MapLocation[N - MIN_CHEESE];
        int count = 0;
        
        int i = N - 1;
        try {
            while (i >= MIN_CHEESE) {
                int x = rc.readSharedArray(i);
                if (x == 1023) break;
                
                MapLocation mineLoc = decode2x2(x);
                if (mineLoc != null && rc.onTheMap(mineLoc)) {
                    mines[count++] = mineLoc;
                }
                i--;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        
        return mines;
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
