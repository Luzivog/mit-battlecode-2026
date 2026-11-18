package opp_powerpuff.Actions;

import opp_powerpuff.Helpers.Nav;
import opp_powerpuff.Helpers.Target;
import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Mine;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Utils.Calc;
import opp_powerpuff.Utils.RNG;


public class Search {
    public static final int RANGE = 3;
    public static final int DURATION = 12;

    public static int quadrant = 0;
    public static MapLocation assignedMine;
    public static int searchSetTurn = 0;

    public static boolean check() throws GameActionException {
        assignedMine = Mine.getTargetCheeseMine();
        
        if (
            // Rat.rc.getRoundNum() > Constants.START_MAINTAIN_DURATION &&
            !Target.exploring &&
            assignedMine != null &&
            Rat.distTo(assignedMine) <= 20 &&
            Rat.rc.getRoundNum() - searchSetTurn < DURATION &&
            VisionManager.allies.length == 0
        ) {
            return true;
        }
        return false;
    }

    public static void run() throws GameActionException {
        MapLocation targetLoc = nearbyLocation(assignedMine);
        Nav.moveTo(targetLoc);

        if (Rat.distTo(targetLoc) <= 2) {
            quadrant = (quadrant + 1) % 4;
        }
    }

    public static void refill() {
        searchSetTurn = Rat.rc.getRoundNum();
    }

    public static MapLocation nearbyLocation(MapLocation origin) throws GameActionException {
        int dx = RNG.rng.nextInt(RANGE);
        int dy = RNG.rng.nextInt(RANGE);
        int x = origin.x;
        int y = origin.y;

        MapLocation ret = origin;
        switch (quadrant) {
            case 0 -> {
                ret = new MapLocation(x + dx, y + dy);
            }
            case 1 -> {
                ret = new MapLocation(x - dx, y + dy);
            }
            case 2 -> {
                ret = new MapLocation(x - dx, y - dy);
            }
            case 3 -> {
                ret = new MapLocation(x + dx, y + dy);
            }
            default -> {}
        }

        if (!Rat.rc.onTheMap(origin)) {
            ret = origin;
        }

        return ret;
    }
}
