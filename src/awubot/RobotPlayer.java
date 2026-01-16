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
        GlobalArray.initGlobalArray();
        CellTracker.init();

        boolean hasInitialisedRobot = false;
        while (true) {
            try {
                Game.preTick();
                Util.preTick();
                Rng.init();
                RobotTracker.reset();
                GlobalArray.preTick();
                Squeaker.preTick();

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
                e.printStackTrace();
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
