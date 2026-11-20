package challenger.robot;

import battlecode.common.*;
import challenger.RobotTracker;
import challenger.comms.GlobalArray;
import challenger.util.Util;

import static challenger.Game.*;

/*
  The book's cooperation hold (Tune.PM_COOP_HOLD, PC1's A2). In cooperation the game ends, scored at that instant, when
  the last cat dies (docs/rules.md §1-2). On maps where each team kills its own cat (TheHeist), our kill of our cat
  ends most games we lose, behind on cheese transferred, kings 1 to 1 (docs/theory/pm-per-map.md). With one king we
  never take a cat low enough that our own damage could kill it; with two we end it (2 kings to 1 is +10 points).
*/
public final class LastCat {
    private LastCat() {
    }

    private static final int TRAP_DAMAGE = TrapType.CAT_TRAP.damage;

    // May we deal `damage` to this cat now?
    public static boolean mayDamage(RobotInfo cat, int damage) {
        return !rc.isCooperation() || cat.health > damage + TRAP_DAMAGE * rc.getNumberCatTraps() || mayEnd();
    }

    private static boolean mayEnd() {
        return GlobalArray.countRatKings() >= 2;
    }

    // Our traps already threaten to kill a cat in view: a baby rat removes its own cat traps around it (free, no
    // cooldown), so that bites, not traps, take the cat down to its last HP.
    public static void defuseTraps() throws GameActionException {
        if (!rc.isCooperation() || rc.getNumberCatTraps() == 0) return;
        int traps = TRAP_DAMAGE * rc.getNumberCatTraps();
        boolean threatened = false;
        for (int i = RobotTracker.numCats - 1; i >= 0; i--) {
            if (RobotTracker.sortedCats[i].health <= traps) threatened = true;
        }
        if (!threatened || mayEnd()) return;
        for (Direction d : Util.allDirections) {
            MapLocation loc = myLoc.add(d);
            if (rc.canRemoveCatTrap(loc)) rc.removeCatTrap(loc);
        }
    }
}
