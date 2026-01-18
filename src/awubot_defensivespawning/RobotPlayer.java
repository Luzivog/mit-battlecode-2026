package awubot_defensivespawning;

import battlecode.common.*;
import awubot_defensivespawning.comms.GlobalArray;
import awubot_defensivespawning.comms.Squeaker;
import awubot_defensivespawning.robot.king.RatKing;
import awubot_defensivespawning.robot.rat.BabyRat;

import static awubot_defensivespawning.Game.*;
import static awubot_defensivespawning.util.Util.println;

import awubot_defensivespawning.util.Rng;
import awubot_defensivespawning.util.Util;

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
