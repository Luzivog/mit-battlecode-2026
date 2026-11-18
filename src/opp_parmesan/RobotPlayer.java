package opp_parmesan;

import opp_parmesan.Baby;
import opp_parmesan.King;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import opp_parmesan.Utils.Globals;

public class RobotPlayer {
    public static void run (RobotController rc) throws GameActionException {
        Globals.init(rc);

        while (true) {
            try {

                Globals.startTurn();

                switch (rc.getType()){
                    case UnitType.BABY_RAT:
                        Baby.run();
                        break;

                    case UnitType.RAT_KING:
                        King.run();
                        break;
                }

                Globals.endTurn();


                Clock.yield();
            }
            catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
            }
        }
    }
}
