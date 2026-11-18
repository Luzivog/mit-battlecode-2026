package challenger;

import battlecode.common.*;
import challenger.comms.EnemyManager;
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
        Rng.seed();
        Rng.init();
        EnemyManager.init();
        GlobalArray.init();
        CellTracker.init();
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
            } catch (Throwable t) {
            } finally {// println
                Clock.yield();
            }
        }
    }
}
