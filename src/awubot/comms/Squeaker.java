package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import awubot.CellTracker;
import awubot.util.BudgetMap;
import awubot.util.Profiler;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.*;
import awubot.RobotTracker;

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

    private static FastIndexedIntSet minesToSend = new FastIndexedIntSet();

    public static void registerMine(MapLocation loc) {
        minesToSend.add(encode2x2(loc));
    }

    // im lazy so im only squeaking one for now
    private static boolean squeakMine() {
        while (minesToSend.set.length() > 0) {
            int i = time % minesToSend.set.length();
            if (GlobalArray.seenCheeseMine(minesToSend.at(i))) {
                minesToSend.remove(minesToSend.at(i));
            } else {
                rc.squeak(minesToSend.at(i));
                return true;
            }
        }
        return false;
    }

    private static void processMessage(Message msg) {
        if (msg.getSenderID() == rc.getID()) return;
        RobotTracker.addAlly(msg.getSource());
        // println(rc.getID() + " received from " + msg.getSenderID() + " content " + msg.getBytes());
        int bytes = msg.getBytes();
        int header = bytes >>> (32 - HEADER);
        switch (header) {
            case 0: // mine location
                // again, only processing one
                int loc = bytes & 1023;
                if (isRatKing) {
                    GlobalArray.cheeseMines.add(loc);
                    println(GlobalArray.cheeseMines.set.length());
                } else {
                    minesToSend.add(loc);
                }
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
                indicate("SQUEAKING SYMMETRY");
                return;
            }
        }
        if (!isRatKing) {
            squeakMine(); // can add other squeaks later
        }
    }
}
