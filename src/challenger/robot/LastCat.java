package challenger.robot;

import battlecode.common.*;
import challenger.RobotTracker;
import challenger.comms.GlobalArray;
import challenger.util.Util;

import static challenger.Game.*;

/*
  Tune.CM_LAST_CAT_HOLD (PC1 A2; off in main, a book switch). In cooperation the game ends, scored at that instant,
  when the last cat dies (docs/rules.md §1-2); a king counts 30 points, so ending it at one king to two loses on any
  cheese. We cannot see whether a cat is the last one, so in cooperation with one king we never take a cat low enough
  that our own damage could kill it: after any bite or new trap it must keep more HP than all our cat traps firing on it
  would deal. With two kings we end it. On peaceinourtime seat A awubot kills its own cat first and the cat kills our
  first king, and our rats then end the game at 1 king to 2 (docs/theory/cm-cat-maps.md).
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
