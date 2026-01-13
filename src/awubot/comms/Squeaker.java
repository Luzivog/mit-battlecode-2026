package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import awubot.CellTracker;
import awubot.robot.rat.BabyRat;
import awubot.robot.rat.BabyRatRole;
import awubot.robot.rat.strategies.HybridStrategy;
import awubot.util.Profiler;
import awubot.util.string.FastIndexedIntSet;

import static awubot.Game.*;
import awubot.RobotTracker;

public class Squeaker {
    public static final int N = 32;
    public static final MapLocation NULL_LOC = new MapLocation(15, 15);
    public static final int SQUEAK_RAD = 16; // radius squared

    /*

    // special cases
    10: cheese mine locations (up to 3) (rounded to 2x2)
    11: enemy rat king location (up to 3) (rounded to 2x2)

    rest will be prefixed with 0:
    (0)00: symmetry
    (0)01: strategy
    (0)10: lure signal
      

    */
    public static final int HEADER = 3; // can be extended later
    public static final int BODY = 31 - HEADER;

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
                rc.squeak((1 << 31) + minesToSend.at(i));
                return true;
            }
        }
        return false;
    }

    private static void processMessage(Message msg) {
        if (msg.getSenderID() == rc.getID()) return;
        squeaksSinceLastTurn++;
        RobotTracker.addAlly(msg.getSource());
        // println(rc.getID() + " received from " + msg.getSenderID() + " content " + msg.getBytes());
        int bytes = msg.getBytes();
        if (bytes < 0) {
            switch (1 & (bytes >>> 30)) {
                case 0:
                    // again, only processing one
                    int loc = bytes & 1023;
                    GlobalArray.cheeseMines.add(loc);
                    if (!isRatKing) {
                        minesToSend.add(loc);
                    }
                    break;
                case 1:
                    break;
                default:
                    assert(false);
            }
        } else {
            int header = bytes >>> (31 - HEADER);
            bytes &= (1 << (31 - HEADER)) - 1;
            switch (header) {
                case 0: // set symmetry
                    if (!isRatKing) return; // probably dont need this line
                    CellTracker.setSymmetry(bytes & 0b11);
                    break;
                case 1: // set strategy
                    // TODO: decide strategies
                    break;
                case 2: // Lure signal - cat location + enemy king location from rat king
                    if (isRatKing) return;
                    int catLocEncoded = (bytes >> 10) & 1023;
                    int enemyKingLocEncoded = bytes & 1023;
                    MapLocation catLoc = decode2x2(catLocEncoded);
                    MapLocation enemyKingLoc = decode2x2(enemyKingLocEncoded);
                    if (BabyRat.role == BabyRatRole.RUSHER &&
                        BabyRat.primaryStrategy instanceof HybridStrategy) {
                        HybridStrategy myHybrid = (HybridStrategy) BabyRat.primaryStrategy;
                        myHybrid.getCatLuringStrategy().activateLure(catLoc, enemyKingLoc);
                        System.out.println("[SQUEAK] R" + rc.getID() + " t=" + time +
                            " received lure signal cat=" + catLoc + " target=" + enemyKingLoc);
                    }
                    break;
                default:
                    println("recieved bad packet " + msg);
                    println("header is " + header);
                    assert(false);
            }
        }
    }

    public static void squeakLureSignal(MapLocation myCatLoc, MapLocation myEnemyKingLoc) {
        int myEncodedCatLoc = encode2x2(myCatLoc);
        int myEncodedEnemyLoc = encode2x2(myEnemyKingLoc);
        int mySqueakContent = (0b10 << BODY) | (myEncodedCatLoc << 10) | myEncodedEnemyLoc;
        rc.squeak(mySqueakContent);
    }

    // messages come in chronological order
    // there is currently a bug where fat units get sent the same message multiple times
    private static int lastMessageCount = 0;
    public static int squeaksSinceLastTurn = 0;
    public static void preTick() {
        squeaksSinceLastTurn = 0;
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
                rc.squeak((0b00 << BODY) + CellTracker.getSymmetry().ordinal());
                indicate("SQUEAKING SYMMETRY");
                return;
            }
        }
        if (!isRatKing) {
            squeakMine(); // can add other squeaks later
        }
    }
}
