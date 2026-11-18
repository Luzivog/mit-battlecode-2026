package opp_powerpuff;
import opp_powerpuff.Rats.BabyRat;
import opp_powerpuff.Rats.RatKing;
import battlecode.common.*;

public class RunRat {
    private static RobotController rc;
    private static Rat rat = null;
    private static boolean changeType = false;
    public static void init(RobotController robot) {
        rc = robot;
    }
    public static void run() throws GameActionException {
        while (true) {
            if (rc.getType() == UnitType.BABY_RAT) {
                if (rat == null)
                    rat = new BabyRat(rc);
                rat.start();
                rat.turn();
                rat.end();
            }
            else if (rc.getType() == UnitType.RAT_KING) {
                if (changeType == false) {
                    rat = new RatKing(rc);
                    changeType = true;
                }
                rat.start();
                rat.turn();
                rat.end();
            }
            Clock.yield();
        }
    }
}
