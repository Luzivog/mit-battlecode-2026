package awubot_dspawn;

import battlecode.common.*;
import awubot_dspawn.comms.EnemyManager;
import awubot_dspawn.comms.GlobalArray;
import awubot_dspawn.comms.Squeaker;
import awubot_dspawn.robot.king.RatKing;
import awubot_dspawn.robot.rat.BabyRat;

import static awubot_dspawn.Game.*;
import static awubot_dspawn.util.Util.println;

import awubot_dspawn.util.Rng;
import awubot_dspawn.util.Util;

public class RobotPlayer {
    static int maxBytecode = 0;
    static boolean hasInitialisedRobot = false;

    public static void resetHasInitialisedRobot() {
        hasInitialisedRobot = false;
    }

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Rng.seed();
        Rng.init();
        EnemyManager.init();
        GlobalArray.initGlobalArray();
        CellTracker.init();

        while (true) {
            try {
                Game.preTick();
                Util.preTick();
                CellTracker.preTick();
                GlobalArray.preTick();
                RobotTracker.reset();
                RobotTracker.preTick();
                EnemyManager.preTick();
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

                if (isRatKing) {
                    RatKing.run();
                } else {
                    BabyRat.run();
                }
                if (Clock.getBytecodesLeft() > 1000) {
                    Squeaker.postTick();
                } else {
                    Util.indicate("skipped squeaker");
                }
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
                rc.setIndicatorString(Util.indicatorString);
                Clock.yield();
            }
        }
    }
}
