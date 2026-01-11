package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import awubot.CellTracker;
import awubot.util.BudgetMap;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.*;

public class Squeaker {
    public static final int N = 32;
    public static final int HEADER = 2;   
    public static final MapLocation NULL_LOC = new MapLocation(15, 15);
    public static final int SQUEAK_RAD = 16; // radius squared

    /*
    
    00: cheese mine locations (up to 3) (rounded to 2x2)
    01: enemy rat king location (up to 3) (rounded to 2x2)
    10: symmetry
    
    */

    private static FastIndexedIntSet mines = new FastIndexedIntSet();

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
        while (mines.set.length() > 0) {
            int i = time % mines.set.length();
            if (GlobalArray.seenCheeseMine(mines.at(i))) {
                mines.remove(i);
            } else {
                rc.squeak(mines.at(i));
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
                mines.add(loc);
                break;
            case 1: // rat king location
                break;
            case 2: // symmetry
                if (!isRatKing) return;
                CellTracker.setSymmetry(bytes & 0b11);
                break;
            case 3:
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
        if (nearRatKing()) {
            indicate("NEAR RAT KING");
            if (GlobalArray.peepSymmetry() == CellTracker.Symmetry.Unknown && CellTracker.isSymmetryKnown()) {
                rc.squeak((0b10 << (32 - HEADER)) + CellTracker.getSymmetry().ordinal()); 
                println("SQUEAKING SYMMETRY");
                return;
            }
        }
        squeakMine(); // can add other squeaks later
    }
}
