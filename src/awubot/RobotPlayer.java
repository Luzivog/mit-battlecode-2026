package awubot;

import battlecode.common.*;
import awubot.comms.GlobalArray;
import awubot.comms.Squeaker;
import awubot.robot.rat.MinerRat;
import awubot.robot.rat.Rat;
import awubot.robot.rat.RatKing;
import static awubot.Game.*;
import awubot.util.Rng;

public class RobotPlayer {
    static Rat rat;
    static int maxBytecode = 0;

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Rng.seed();
        GlobalArray.initGlobalArray();
        rat = (rc.getType() == UnitType.BABY_RAT ? new MinerRat() : new RatKing());
        rat.init();

        while (true) {
            try {
                GlobalArray.preTick();
                Squeaker.preTick();
                rat.run();
                Squeaker.postTick();
                GlobalArray.postTick();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(rc.getType() == UnitType.BABY_RAT) {
                    maxBytecode = Math.max(maxBytecode, Clock.getBytecodeNum());
                    System.out.println(maxBytecode);
                }
                Clock.yield();
            }
        }
    }
}
