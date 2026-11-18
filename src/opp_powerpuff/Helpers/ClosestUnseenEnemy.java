package opp_powerpuff.Helpers;

import opp_powerpuff.Rat;
import opp_powerpuff.Utils.Encoder;
import battlecode.common.*;
import opp_powerpuff.Managers.VisionManager;

public class ClosestUnseenEnemy {
    public static MapLocation run() throws GameActionException {
        RobotController rc = Rat.rc;
        int roundNum = rc.getRoundNum();
        Message[] lastTurn = rc.readSqueaks(roundNum - 1);
        Message[] thisTurn = rc.readSqueaks(roundNum);


        MapLocation closest = null;
        for (Message m: lastTurn) {
            MapLocation enemyLoc = parse(m);
            if (enemyLoc == null) continue;
            if (closest == null)
                closest = enemyLoc;
            else if (Rat.distTo(enemyLoc) < Rat.distTo(closest))
                closest = enemyLoc;
        }

        for (Message m: thisTurn) {
            MapLocation enemyLoc = parse(m);
            if (enemyLoc == null) continue;
            if (closest == null)
                closest = enemyLoc;
            else if (Rat.distTo(enemyLoc) < Rat.distTo(closest))
                closest = enemyLoc;
        }

        return closest;
    }

    public static MapLocation parse(Message m) throws GameActionException {
        int bytes = m.getBytes();
        int type = Encoder.getType(bytes);
        if (type == Comms.ALERT_ALLY) {
            int x = Encoder.getX(bytes);
            int y = Encoder.getY(bytes);
            return new MapLocation(x, y);
        }
        return null;
    }
}
