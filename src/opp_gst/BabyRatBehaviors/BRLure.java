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

import java.util.Random;

import opp_gst.SymmetryManager;
import opp_gst.Communication.Communicator;

public class BRLure implements Behavior {
    //Constants
    static final int RANDOM_LOC_TURN_RESET = 30;

    //Instance Variables (persist over state changes)
    MapLocation targetEnemyLoc;

    //Singleton Stuff
    private static BRLure instance;
    private BRLure() {}
    public static BRLure getInstance() {
        if(instance == null) {
            instance = new BRLure();
        }

        return instance;
    }

    @Override
    public void execute() throws GameActionException {
        return;
    }
}