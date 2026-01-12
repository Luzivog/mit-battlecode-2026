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
        if(rc.getType() == UnitType.BABY_RAT) {
            BabyRat.init();
        } else {
            RatKing.init();
        }

        while (true) {
            try {
                Game.preTick();
                Util.preTick();
                Rng.init();
                RobotTracker.reset();
                GlobalArray.preTick();
                Squeaker.preTick();
                if(rc.getType() == UnitType.BABY_RAT) {
                    BabyRat.run();
                } else {
                    RatKing.run();
                }
                Squeaker.postTick();
                GlobalArray.postTick();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rc.getType() == UnitType.BABY_RAT) {
                    maxBytecode = Math.max(maxBytecode, Clock.getBytecodeNum());
                    // System.out.println(maxBytecode);
                }
                Clock.yield();
            }
        }
    }
}
