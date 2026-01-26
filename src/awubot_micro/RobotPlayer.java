package awubot_micro;

import battlecode.common.*;
import awubot_micro.comms.EnemyManager;
import awubot_micro.comms.GlobalArray;
import awubot_micro.comms.Squeaker;
import awubot_micro.robot.king.RatKing;
import awubot_micro.robot.rat.BabyRat;

import static awubot_micro.Game.*;
import static awubot_micro.util.Util.println;

import awubot_micro.util.Rng;
import awubot_micro.util.Util;

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
        GlobalArray.init();
        CellTracker.init();

        while (true) {
            try {
                Game.preTick();
                Util.preTick();
                CellTracker.preTick();
                GlobalArray.preTick();
                RobotTracker.preTick();
                EnemyManager.preTick();
                Squeaker.preTick();

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
                EnemyManager.postTick();
                Game.postTick();
            } catch (Exception e) {
                if (!Game.DISABLE_OUTPUT) {
                    e.printStackTrace();
                    rc.setTimelineMarker("RE " + rc.getID() + " " + rc.getLocation(), 255, 0, 0);
                }
                // rc.resign();
            } finally {
                if (time != rc.getRoundNum()) {
                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! BYTECODE EXCEEDED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    if (!Game.DISABLE_OUTPUT) {
                        rc.setTimelineMarker("TLE " + rc.getID() + " " + rc.getLocation(), 0, 0, 255);
                    }
                    // rc.resign();
                }
                rc.setIndicatorString(Util.indicatorString);
                Clock.yield();
            }
        }
    }
}
