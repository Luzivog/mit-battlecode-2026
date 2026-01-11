package experiment;

import battlecode.common.*;
import experiment.comms.GlobalArray;
import experiment.comms.Squeaker;
import experiment.robot.rat.BabyRat;
import experiment.robot.rat.Rat;
import experiment.robot.rat.RatKing;
import static experiment.Game.*;
import experiment.util.Rng;

public class RobotPlayer {
    static Rat rat;

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Rng.seed();
        GlobalArray.initGlobalArray();

        if (rc.getType() == UnitType.RAT_KING) {
            rat = new RatKing();
        } else {
            rat = new BabyRat();
        }
        rat.init();

        while (true) {
            try {
                Game.preTick();
                GlobalArray.preTick();
                Squeaker.preTick();
                rat.run();
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
