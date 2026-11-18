package opp_gst.BabyRatBehaviors;

import opp_gst.Behavior;
import opp_gst.Communication.RatKingInfo;
import opp_gst.Pathfinding.Pathfinding;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.*;

import static opp_gst.BabyRat.currentLocation;
import static opp_gst.BabyRat.enemyRatKingSeenLoc;
import static opp_gst.BabyRat.nearbyCats;
import static opp_gst.BabyRat.seeEnemyRatKing;
import static opp_gst.RobotPlayer.*;
import static opp_gst.BabyRat.*;

import java.util.Random;

import opp_gst.Utilities;
import opp_gst.SymmetryManager;
import opp_gst.Communication.Communicator;

public class BRHunt implements Behavior {
    
    //Instance Variables (persist over state changes)
    MapLocation targetEnemyLoc;

    //Singleton Stuff
    private static BRHunt instance;
    private BRHunt() {}
    public static BRHunt getInstance() {
        if(instance == null) {
            instance = new BRHunt();
        }

        return instance;
    }

    @Override
    public void execute() throws GameActionException {
        targetEnemyLoc = SymmetryManager.getTarget();
        Pathfinding.attemptMove(targetEnemyLoc, true);
        if (rc.isActionReady()) Utilities.attemptRatnap();
        if (rc.isActionReady()) Utilities.attemptThrow(turnsCarrying, health);
        if (rc.isActionReady()) Utilities.attemptAttack();
    }
}