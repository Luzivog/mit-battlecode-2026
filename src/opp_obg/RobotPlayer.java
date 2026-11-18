package opp_obg;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.UnitType;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        MyUnit u = switch(rc.getType()){
            case BABY_RAT -> new BabyRat(rc);
            default -> new RatKing(rc);
        };

        while (true) {
            try {
                if (!MyUnit.isKing && rc.getType() == UnitType.RAT_KING) u = new RatKing(rc);
                u.startTurn();
                u.runTurn();
                u.endTurn();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
}
