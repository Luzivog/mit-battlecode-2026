package awubot_sus;

import battlecode.common.*;
import awubot_sus.comms.EnemyManager;
import awubot_sus.comms.GlobalArray;
import awubot_sus.comms.MapManager;
import awubot_sus.comms.Squeaker;
import awubot_sus.robot.king.RatKing;
import awubot_sus.robot.rat.BabyRat;

import static awubot_sus.Game.*;
import static awubot_sus.util.Util.println;

import awubot_sus.util.Profiler;
import awubot_sus.util.Rng;
import awubot_sus.util.Util;

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
                if (!Game.DISABLE_OUTPUT) {
                    e.printStackTrace();
                    rc.setTimelineMarker("RE " + rc.getID() + " " + rc.getLocation(), 255, 0, 0);
                    // if (isRatKing) rc.resign();
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
