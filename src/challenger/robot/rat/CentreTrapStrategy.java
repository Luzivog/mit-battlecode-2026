package challenger.robot.rat;

import battlecode.common.*;
import challenger.Pathfinder;
import challenger.Tune;
import challenger.robot.Strategy;
import challenger.util.CentreMines;
import static challenger.Game.*;
import static challenger.util.Util.*;

// CD arm (a) (docs/theory/cd-centre.md): in r100-600, outside cooperation, a miner in a centre mine's box puts rat traps
// on the two tiles 2 and 3 steps from the mine toward the enemy's start. An enemy rat walking in from its side enters
// both 3x3 areas: 100 damage, a kill at full HP. A miner that sees such a site without our trap within WALK steps walks
// to it, faces it, places the trap and carries on with its turn.
public class CentreTrapStrategy extends Strategy {
    private static final int FIRST_ROUND = 100, LAST_ROUND = 600;
    private static final int MAX_TEAM_TRAPS = 15; // 10 of the team's 25 stay for combat
    private static final int WALK = 3;

    @Override
    public boolean runTick() throws GameActionException {
        if (time < FIRST_ROUND || time > LAST_ROUND || rc.isCooperation()) return false;
        if (!rc.isActionReady() || rc.getNumberRatTraps() >= MAX_TEAM_TRAPS) return false;
        if (rc.getRawCheese() < Tune.TRAP_RAW && rc.getGlobalCheese() < Tune.BAND_GLOBAL_CHEESE) return false;
        if (!CentreMines.refresh()) return false;
        MapLocation mine = CentreMines.boxOf(myLoc);
        if (mine == null) return false;
        Direction d = mine.directionTo(CentreMines.enemyHome());
        if (d == Direction.CENTER) return false;
        MapLocation near = mine.add(d).add(d), far = near.add(d);
        MapLocation site = needsTrap(near) ? near : needsTrap(far) ? far : null;
        if (site == null) return false;
        if (myLoc.distanceSquaredTo(site) <= 2) {
            if (!canSense(myLoc, site, myDir)) {
                if (!rc.canTurn()) return false;
                BabyRat.turn(myLoc.directionTo(site));
            }
            if (rc.canPlaceRatTrap(site)) rc.placeRatTrap(site);
            return false;
        }
        if (Math.max(Math.abs(site.x - myLocX), Math.abs(site.y - myLocY)) > WALK) return false;
        BabyRat.autoMove(Pathfinder.getMove(site));
        return true;
    }

    // A tile we see that can hold a rat trap and has none of ours.
    private static boolean needsTrap(MapLocation s) throws GameActionException {
        if (!rc.canSenseLocation(s)) return false;
        MapInfo info = rc.senseMapInfo(s);
        return info.isPassable() && !info.hasCheeseMine() && info.getTrap() != TrapType.RAT_TRAP
            && rc.senseRobotAtLocation(s) == null;
    }
}
