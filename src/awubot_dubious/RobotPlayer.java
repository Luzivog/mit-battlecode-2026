package awubot_dubious;

import battlecode.common.*;
import awubot_dubious.comms.GlobalArray;
import awubot_dubious.comms.Squeaker;
import awubot_dubious.robot.king.RatKing;
import awubot_dubious.robot.rat.BabyRat;

import static awubot_dubious.Game.*;
import static awubot_dubious.util.Util.println;

import awubot_dubious.util.Rng;
import awubot_dubious.util.Util;

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
                    println("BYTECODE EXCEEDED");
                    // rc.resign();
                }
                Clock.yield();
            }
        }
    }
}
