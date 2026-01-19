package awubot;

import battlecode.common.*;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;
import awubot.robot.king.RatKing;
import awubot.robot.rat.BabyRat;

import static awubot.Game.*;
import static awubot.util.Util.println;

import awubot.util.Rng;
import awubot.util.Util;

public class RobotPlayer {
    static int maxBytecode = 0;

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Rng.seed();
        Rng.init();
        GlobalArray.initGlobalArray();
        CellTracker.init();

        boolean hasInitialisedRobot = false;
        while (true) {
            try {
                Game.preTick();
                Util.preTick();
                CellTracker.preTick();
                GlobalArray.preTick();
                RobotTracker.reset();
                RobotTracker.preTick();
                Squeaker.preTick();
                CellTracker.storeVisibleCells();

                if (!hasInitialisedRobot) {
                    if(isRatKing) {
                        RatKing.init();
                    } else {
                        BabyRat.init();
                    }
                    hasInitialisedRobot = true;
                }

                if(isRatKing) {
                    RatKing.run();
                } else {
                    BabyRat.run();
                }
                Squeaker.postTick();
                GlobalArray.postTick();
                Game.postTick();
            } catch (Exception e) {
                if (!Game.DISABLE_OUTPUT) e.printStackTrace();
                // rc.resign();
            } finally {
                if (time != rc.getRoundNum()) {
                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! BYTECODE EXCEEDED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    // rc.resign();
                }
                Clock.yield();
            }
        }
    }
}
