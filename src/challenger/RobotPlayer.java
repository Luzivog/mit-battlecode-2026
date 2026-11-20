package challenger;

import battlecode.common.*;
import challenger.comms.EnemyManager;
import challenger.Tune;
import challenger.comms.GlobalArray;
import challenger.comms.MapManager;
import challenger.comms.Squeaker;
import challenger.robot.king.RatKing;
import challenger.robot.rat.BabyRat;

import static challenger.Game.*;
import static challenger.util.Util.decode2x2;

import challenger.util.Profiler;
import challenger.util.Rng;
import challenger.util.Util;

public class RobotPlayer {
    static int maxBytecode = 0;
    public static boolean hasInitialisedRobot = false;

    public static void resetHasInitialisedRobot() {
        hasInitialisedRobot = false;
    }

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Book.init(); // first: the classes that read Tune load after it (tools/book/AGENTS.md)
        Rng.seed();
        Rng.init();
        EnemyManager.init();
        GlobalArray.init();
        // A book switch (Tune.CM_MINE_BOOK): baby rats know the map's mines from the book.
        if (Tune.CM_MINE_BOOK != 0) MineBook.load();
        CellTracker.init();
        // A book switch (Tune.PM_SYM): on a book map-seat, the map's symmetry from its file, known from round 1.
        if (Tune.PM_SYM != 0) CellTracker.setSymmetry(Book.symmetry);
        MapManager.init();

        while (true) {
            try {
                Game.preTick();
                Util.preTick();
                CellTracker.preTick();
                GlobalArray.preTick();
                RobotTracker.preTick();
                EnemyManager.preTick();
                MapManager.preTick();
                Squeaker.preTick();

                // debug

                // if (isRatKing) {
                //     for (int i = 0; i < GlobalArray.globalCheeseMines.size(); i++) { 
                //         Util.indicate("global: " + decode2x2(GlobalArray.globalCheeseMines.at(i)));
                //     }
                // } else {
                //     for (int i = 0; i < GlobalArray.globalCheeseMines.size(); i++) { 
                //         Util.indicate("global: " + decode2x2(GlobalArray.globalCheeseMines.at(i)));
                //     }
                //     for (int i = 0; i < Squeaker.minesToSend.size(); i++) { 
                //         Util.indicate("to send: " + decode2x2(Squeaker.minesToSend.at(i)));
                //     }
                // }

                // end debug

                if (!hasInitialisedRobot) {
                    if(isRatKing) {
                        RatKing.init();
                    } else {
                        BabyRat.init();
                    }
                    hasInitialisedRobot = true;
                }

                if (isRatKing) {
                    RatKing.run();
                } else {
                    BabyRat.run();
                }
                
                if (Clock.getBytecodesLeft() > 1000) {
                    Squeaker.postTick();
                }

                GlobalArray.postTick();
                Game.postTick();
                // A book switch (Tune.AT_MAP): the map's walls and round-1 dirt from the book, every cell seen, loaded at
                // the end of the first turn with 5,000 bytecodes left (the load costs ~3k), so it causes no overrun.
                if (Tune.AT_MAP != 0 && !MapBook.loaded && Clock.getBytecodesLeft() > 5000) MapBook.load();
            } catch (Throwable t) {
            } finally {// println
                if (isRatKing) GlobalArray.endTurn();
                Clock.yield();
            }
        }
    }
}
