package awubot;

import battlecode.common.*;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;
import awubot.robot.king.RatKing;
import awubot.robot.rat.BabyRat;

import static awubot.Game.*;
import awubot.util.Rng;
import awubot.util.Util;

public class RobotPlayer {
    static int maxBytecode = 0;

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Rng.seed();
        GlobalArray.initGlobalArray();
        if(isRatKing) {
            RatKing.init();
        } else {
            BabyRat.init();
        }

        while (true) {
            try {
                Game.preTick();
                Util.preTick();
                Rng.init();
                RobotTracker.reset();
                GlobalArray.preTick();
                Squeaker.preTick();
                if(isRatKing) {
                    RatKing.run();
                } else {
                    BabyRat.run();
                }
                Squeaker.postTick();
                GlobalArray.postTick();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
}
