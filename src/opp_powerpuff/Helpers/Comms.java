package opp_powerpuff.Helpers;

import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Utils.Encoder;

public class Comms {
    public static final int CHEESE_MINE_VALUE = 1;
    public static final int CARRIED_VALUE = 2;
    public static final int ALERT_ALLY = 3;
    public static final int SYMMETRY_VALUE = 5;
    public static final int ENEMY_LOCATION = 7;

    public static void squeakCheeseMine(MapLocation loc) throws GameActionException {
        int message = Encoder.Encode(CHEESE_MINE_VALUE, loc);
        // System.out.println("DELIVERED CHEESE MINE: " + loc.x + " " + loc.y);
        Rat.rc.squeak(message);
    }

    public static void squeakSymmetry(int symmetry) throws GameActionException {
        int message = Encoder.Encode(SYMMETRY_VALUE, new MapLocation(99, symmetry));
        // System.out.println("DELIVERED " + symmetry);
        Rat.rc.squeak(message);
    }

    public static void squeakBeingCarried(MapLocation loc) throws GameActionException {
        int message = Encoder.Encode(CARRIED_VALUE, loc);
        Rat.rc.squeak(message);
    }

    public static void squeakEnemyKingLocation(MapLocation enemyLocation) {
        Rat.rc.squeak(Encoder.Encode(ENEMY_LOCATION, enemyLocation));
    }

    public static void squeakEnemyBabyRatLocation(MapLocation enemyLocation) {
        Rat.rc.squeak(Encoder.Encode(ALERT_ALLY, enemyLocation));
    }

    public static MapLocation getClosestEnemyKingLocationSqueaked(Message[] messages) {
        MapLocation location = null;
        for (int i = 0; i < messages.length; i ++) {
            if (Encoder.getType(messages[i].getBytes()) == 7) {
                MapLocation enemyLocation = new MapLocation(Encoder.getX(messages[i].getBytes()), Encoder.getY(messages[i].getBytes()));
                if (location == null || Rat.rc.getLocation().distanceSquaredTo(enemyLocation) < Rat.rc.getLocation().distanceSquaredTo(location))
                    location = enemyLocation;
            }
        }
        return location;
    }

    public static Message[] getSqueaksLastTurn() {
        return Rat.rc.readSqueaks(Rat.rc.getRoundNum() - 1);
    }

    public static void ratKingParse(int msg) throws GameActionException {
        int type = Encoder.getType(msg);
        int x = Encoder.getX(msg);
        int y = Encoder.getY(msg);

        if (type == CHEESE_MINE_VALUE) {
            MapLocation cheeseMineLoc = new MapLocation(x, y);
            if (!Mine.knownCheeseMine(cheeseMineLoc))
                Global.addCheeseMine(x, y);
        } else if (type == SYMMETRY_VALUE) {
            int currSymm = Global.readSymmetry();
            if (currSymm != y) {
                currSymm &= y;
                Global.writeSymmetry(currSymm);
            }
        }
    }
}
