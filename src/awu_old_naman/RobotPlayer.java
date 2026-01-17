package awu_old_naman;

import battlecode.common.*;
import awu_old_naman.comms.GlobalArray;
import awu_old_naman.comms.Squeaker;
import awu_old_naman.robot.king.RatKing;
import awu_old_naman.robot.rat.BabyRat;

import static awu_old_naman.Game.*;
import static awu_old_naman.util.Util.println;

import awu_old_naman.util.Rng;
import awu_old_naman.util.Util;

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
                // // e.printStackTrace();
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
