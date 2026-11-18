package opp_gst.BabyRatBehaviors;
import opp_gst.Communication.Communicator;
import opp_gst.Behavior;
import opp_gst.Pathfinding.Pathfinding;
import opp_gst.SymmetryManager;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static opp_gst.RobotPlayer.rc;

public class BRRush implements Behavior {

    //Singleton Stuff
    private static BRRush instance;
    private BRRush() {}
    public static BRRush getInstance() {
        if(instance == null) {
            instance = new BRRush();
        }

        return instance;
    }

    MapLocation enemyRatKingGuess = null;

    @Override
    public void execute() throws GameActionException {
        if(enemyRatKingGuess == null) {
            //System.out.println(SymmetryManager.getSymmetric(Communicator.getRatKings().get(0).loc()));
           // enemyRatKingGuess = SymmetryManager.getSymmetric(Communicator.getRatKings().get(0).loc());
        }
       // Pathfinding.attemptMove(enemyRatKingGuess, true);
    }
}
