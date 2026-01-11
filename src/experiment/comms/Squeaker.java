package experiment.comms;

import battlecode.common.*;
import static experiment.util.Util.*;

import experiment.CellTracker;
import experiment.util.BudgetMap;

import static experiment.Game.*;

public class Squeaker {
    public static final int N = 32;
    public static final int HEADER = 2;   
    public static final MapLocation NULL_LOC = new MapLocation(15, 15);
    public static final int SQUEAK_RAD = 16; // radius squared

    /*

    00: cheese mine locations (up to 3) (rounded to 2x2)
    01: enemy rat king location (up to 3) (rounded to 2x2)
    10: symmetry
    11: cat location (danger warning to king)

    */

    private static int mineSize = 0;
    private static int[] mines = new int[167];
    private static int enemyKingLoc = -1;
    private static int enemyKingRound = -1;
    private static int lastEnemyKingBroadcastRound = -1000;
    private static int lastEnemyKingBroadcastLoc = -1;

    // Cat tracking
    private static int catLoc = -1;
    private static int catRound = -1;

    // mystery bytecode usage
    // private static void refreshMines() {
    //     GlobalArray.writeCheeseMines();
    //     for (int i = 0; i < mineSize;) {
    //         if (BudgetMap.has(mines[i].x, mines[i].y)) {
    //             mines[i] = mines[--mineSize];
    //         } else {
    //             i++;
    //         }
    //     }   
    // }

    // im lazy so im only squeaking one for now
    private static boolean squeakMine() {
        while (mineSize > 0) {
            int i = time % mineSize;
            if (GlobalArray.seenCheeseMine(mines[i])) {
                mines[i] = mines[--mineSize];
            } else {
                rc.squeak(mines[i]);
                return true;
            }
        }
        return false;
    }

    private static void processMessage(Message msg) {
        if (msg.getSenderID() == rc.getID()) return;
        // println(rc.getID() + " received from " + msg.getSenderID() + " content " + msg.getBytes());
        int bytes = msg.getBytes();
        int header = bytes >>> (32 - HEADER);
        switch (header) {
            // again, only processing one
            case 0: // mine location
                int loc = bytes & 1023;
                for (int i = 0; i < mineSize; i++) {
                    if (mines[i] == loc) return;
                }
                mines[mineSize++] = loc;
                break;
            case 1: // rat king location
                int kingLoc = bytes & 1023;
                enemyKingLoc = kingLoc;
                enemyKingRound = time;
                break;
            case 2: // symmetry
                if (!isRatKing) return;
                CellTracker.setSymmetry(bytes & 0b11);
                break;
            case 3: // cat location warning
                int cLoc = bytes & 1023;
                catLoc = cLoc;
                catRound = time;
                break;
            default:
                assert(false);
        }
    } 

    // messages come in chronological order
    // there is currently a bug where fat units get sent the same message multiple times
    private static int lastMessageCount = 0;
    public static void preTick() {
        Message[] msgs = rc.readSqueaks(time - 1);
        for (int i = lastMessageCount; i < msgs.length; i++) {
            processMessage(msgs[i]);
        }
        msgs = rc.readSqueaks(time);
        lastMessageCount = msgs.length;
        for (Message m : msgs) {
            processMessage(m);
        }
    }

    public static boolean nearRatKing() throws GameActionException {
        RobotInfo bots[] = rc.senseNearbyRobots(SQUEAK_RAD);
        for (RobotInfo bot : bots) {
            if (bot.getType() == UnitType.RAT_KING) {
                return true;
            }
        }
        return false;
    }
    
    public static void postTick() throws GameActionException {
        if (enemyKingLoc >= 0
            && (time - lastEnemyKingBroadcastRound >= 5 || enemyKingLoc != lastEnemyKingBroadcastLoc)) {
            rc.squeak((0b01 << (32 - HEADER)) | (enemyKingLoc & 1023));
            lastEnemyKingBroadcastRound = time;
            lastEnemyKingBroadcastLoc = enemyKingLoc;
            return;
        }
        if (nearRatKing()) {
            if (GlobalArray.peepSymmetry() == CellTracker.Symmetry.Unknown && CellTracker.isSymmetryKnown()) {
                rc.squeak((0b10 << (32 - HEADER)) + CellTracker.getSymmetry().ordinal()); 
                println("SQUEAKING SYMMETRY");
                return;
            }
        }
        // squeakMine(); // can add other squeaks later
    }

    public static void noteEnemyKing(MapLocation loc) {
        if (loc == null) return;
        enemyKingLoc = encode2x2(loc);
        enemyKingRound = time;
    }

    public static boolean hasEnemyKingLoc() {
        return enemyKingLoc >= 0 && enemyKingRound >= 0;
    }

    public static MapLocation getEnemyKingLoc() {
        if (enemyKingLoc < 0) return null;
        return decode2x2(enemyKingLoc);
    }

    // Cat location methods
    public static void broadcastCatLocation(MapLocation loc) throws GameActionException {
        if (loc == null) return;
        int encoded = encode2x2(loc);
        rc.squeak((0b11 << (32 - HEADER)) | (encoded & 1023));
    }

    public static boolean hasCatWarning() {
        // Cat warnings are valid for 10 rounds
        return catLoc >= 0 && catRound >= 0 && (time - catRound) <= 10;
    }

    public static MapLocation getCatLoc() {
        if (!hasCatWarning()) return null;
        return decode2x2(catLoc);
    }
}
