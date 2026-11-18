package opp_powerpuff.Managers;

import battlecode.common.*;
import opp_powerpuff.Rat;

public class Global {
    public static final int MAX_RAT_KINGS = 5;
    public static final int MAX_CHEESE_MINES = 15;
    public static final int MAX_SUMMONS = 1;
    public static final int MAX_ENEMY_RAT_COORDS = 1; // Change if you want
    public static final int MAX_HEART_BEATS = 5;

    public static final int OFFSET_RAT_KINGS = 0;                                                           // 0
    public static final int OFFSET_CHEESE_MINES = OFFSET_RAT_KINGS + MAX_RAT_KINGS * 2;                     // 0-9
    public static final int OFFSET_SPAWN = OFFSET_CHEESE_MINES + MAX_CHEESE_MINES * 2;                    // 10-39
    public static final int OFFSET_SYMMETRY = OFFSET_SPAWN + MAX_SUMMONS * 2;                             // 40-41
    public static final int OFFSET_ALARM = OFFSET_SYMMETRY + 1;                                             // 42
    public static final int OFFSET_ENEMY_RAT_COORDS = OFFSET_ALARM + 1;                                     // 43
    public static final int OFFSET_GATHER_CHEESE_SCORE = OFFSET_ENEMY_RAT_COORDS + MAX_ENEMY_RAT_COORDS * 2;     // 44-45
    public static final int OFFSET_NEW_KING_FLAG = OFFSET_GATHER_CHEESE_SCORE + 1;
    public static final int OFFSET_KING_HEARTBEATS = OFFSET_NEW_KING_FLAG + 1;
    
    public static void init() throws GameActionException {
        for (int i = 0; i < 64; i++) {
            Rat.rc.writeSharedArray(i, 1023);
        }
        Rat.rc.writeSharedArray(OFFSET_SYMMETRY, 7);
    }

    // Returns success of operation
    public static boolean addCheeseMine(int x, int y) throws GameActionException {
        int idx = firstEmptyInRange(OFFSET_CHEESE_MINES, OFFSET_SPAWN);
        if (idx == OFFSET_SPAWN) return false;
        editPair(idx, x, y);
        return true;
    }

    public static MapLocation[] readCheeseMineLocations() throws GameActionException {
        int idx = firstEmptyInRange(OFFSET_CHEESE_MINES, OFFSET_SPAWN);
        int cheeseMineCount = (idx - OFFSET_CHEESE_MINES) / 2;
        MapLocation[] res = new MapLocation[cheeseMineCount];
        for (int i = 0; i < cheeseMineCount; i++) {
            res[i] = readPair(OFFSET_CHEESE_MINES + i * 2);
        }
        return res;
    }

    // Returns index in the global array
    public static int addRatKing(int x, int y) throws GameActionException {
        int idx = firstEmptyInRange(OFFSET_RAT_KINGS, OFFSET_CHEESE_MINES);
        // System.out.println("RAHH " + idx);
        editPair(idx, x, y);
        return idx;
    }

    // RatKing number goes from 1-5
    public static MapLocation readRatKingLoc(int ratKingNumber) throws GameActionException {
        MapLocation m = readPair(OFFSET_RAT_KINGS + ratKingNumber * 2);
        if (m.x == 1023) return null;
        return m;
    }

    // RatKing number goes from 1-5
    public static void editRatKingLoc(int ratKingNumber, MapLocation loc) throws GameActionException {
        editPair(OFFSET_RAT_KINGS + ratKingNumber * 2, loc.x, loc.y);
    }

    public static void writeToSpawn(int x, int y) throws GameActionException {
        editPair(OFFSET_SPAWN, x, y);
    }

    public static MapLocation readSpawn() throws GameActionException {
       return readPair(OFFSET_SPAWN);
    }

    // Returns index in global array
    public static void writeSymmetry(int symmetry) throws GameActionException {
        Rat.rc.writeSharedArray(OFFSET_SYMMETRY, symmetry);
    }

    public static int readSymmetry() throws GameActionException {
        return Rat.rc.readSharedArray(OFFSET_SYMMETRY);
    }

    public static void writeAlarm(int alarm) throws GameActionException {
        Rat.rc.writeSharedArray(OFFSET_ALARM, alarm);
    }

    public static int readAlarm() throws GameActionException {
        return Rat.rc.readSharedArray(OFFSET_ALARM);
    }

    public static void writeGatherCheeseScore(int count) throws GameActionException {
        Rat.rc.writeSharedArray(OFFSET_GATHER_CHEESE_SCORE, count);
    }

    public static int readGatherCheeseScore() throws GameActionException {
        return Rat.rc.readSharedArray(OFFSET_GATHER_CHEESE_SCORE);
    }

    public static void setBuildKingFlag(int flag) throws GameActionException {
        Rat.rc.writeSharedArray(OFFSET_NEW_KING_FLAG, flag);
    }
    public static int readBuildKingFlag() throws GameActionException {
        return Rat.rc.readSharedArray(OFFSET_NEW_KING_FLAG);
    }
    // read map locations of all kings
    public static MapLocation[] readKingLocations() throws GameActionException {
        MapLocation[] locations = new MapLocation[5];
        for (int i = 0; i < 5; i ++) {
            MapLocation l = readPair(i * 2);
            locations[i] = l;
        }
        return locations;
    }
    // return all heartbeats
    public static int[] readHeartbeats() throws GameActionException {
        int[] h = new int[5];
        for (int i = 0; i < 5; i ++) {
            h[i] = Rat.rc.readSharedArray(OFFSET_KING_HEARTBEATS + i);
        }
        return h;
    }
    public static void pumpHeartbeat(int idx) throws GameActionException {
        if (Rat.rc.readSharedArray(OFFSET_KING_HEARTBEATS + idx) == 1023 || Rat.rc.readSharedArray(OFFSET_KING_HEARTBEATS + idx) == 0) {
            Rat.rc.writeSharedArray(OFFSET_KING_HEARTBEATS + idx, 1);
        }
        else {
            Rat.rc.writeSharedArray(OFFSET_KING_HEARTBEATS + idx, 0);
        }
    }
    public static void killHeartbeat(int idx) throws GameActionException {
        Rat.rc.writeSharedArray(OFFSET_KING_HEARTBEATS + idx, 1023);
    }
    public static int getLivingKings() throws GameActionException {
        int count = 0;
        for (int i = 0; i < 5; i ++) {
            if (Rat.rc.readSharedArray(i * 2) != 1023)
                count ++;
        }
        return count;
    }
    public static int getFirstRatKingIdx() throws GameActionException {
        for (int i = 0; i < 5; i ++) {
            if (Rat.rc.readSharedArray(i*2) != 1023 && readBuildKingFlag() != i) {
                return i;
            }
        }
        return -1;
    }
    public static int getEmptyRatKingIdx() throws GameActionException {
        MapLocation[] locations = readKingLocations();
        if (locations[0].x == 1023)
            return 0;
        if (locations[1].x == 1023)
            return 1;
        if (locations[2].x == 1023)
            return 2;
        if (locations[3].x == 1023)
            return 3;
        if (locations[4].x == 1023)
            return 4;
        return -1;
    }
    // ==============================================================================
    private static void editPair(int idx, int x, int y) throws GameActionException {
        Rat.rc.writeSharedArray(idx, x);
        Rat.rc.writeSharedArray(idx + 1, y);
    }

    private static MapLocation readPair(int idx) throws GameActionException {
        return new MapLocation(
            Rat.rc.readSharedArray(idx),
            Rat.rc.readSharedArray(idx + 1)
        );
    }

    // Inclusive start, exclusive end
    private static int firstEmptyInRange(int start, int end) throws GameActionException {
        int idx = start;
        while (idx < end && Rat.rc.readSharedArray(idx) != 1023) idx++;
        return idx; 
    }
    
}
