package awubot.comms;

import battlecode.common.*;
import static awubot.util.Util.*;

import awubot.util.BudgetMap;

import static awubot.Game.*;

public class Squeaker {
    public final int N = 32;
    public final int HEADER = 3;   
    public final MapLocation NULL_LOC = new MapLocation(15, 15);

    /*
    
    00: cheese mine locations (up to 3) (rounded to 2x2)
    01: enemy rat king location (up to 3) (rounded to 2x2)
    1: everything else ig
    
    */

    private static int mineSize = 0;
    private static int[] mines = new int[167];

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

    private void processMessage(Message msg) {
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
            default:
                assert(false);
        }
    } 

    // TODO: verify that messages actually come in order so this is correct
    // also consider loading into a map
    private static int lastMessageCount = 0;
    public void tick() {
        Message[] msgs = rc.readSqueaks(time - 1);
        for (int i = lastMessageCount; i < msgs.length; i++) {
            processMessage(msgs[i]);
        }
        msgs = rc.readSqueaks(time);
        lastMessageCount = msgs.length;
        for (Message m : msgs) processMessage(m);

        squeakMine(); // can add other squeaks later
    }
}
