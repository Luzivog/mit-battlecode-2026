package challenger.robot;

import battlecode.common.*;
import challenger.RobotTracker;
import challenger.Tune;
import challenger.comms.GlobalArray;
import challenger.util.Util;

import static challenger.Game.*;

/*
  Cooperation last-cat hold (Tune.PM_COOP_HOLD legacy, or Tune.COOP_CTRL bit 3 = 8 dynamic hold).
  In cooperation the game ends, scored at that instant, when the last cat dies (docs/rules.md §1-2).
  Dynamic hold only delays the killing blow when we have a concrete, short-horizon reason:
  cooperation, single king, active formation or globalCheese >= 300, time < 1800, and the cat may be the last cat.
*/
public final class LastCat {
    private LastCat() {
    }

    private static final int TRAP_DAMAGE = TrapType.CAT_TRAP.damage;
    private static int lastLowCatRound = -100;

    public static boolean isHoldEnabled() {
        return Tune.PM_COOP_HOLD != 0 || (Tune.COOP_CTRL & 8) != 0;
    }

    // Whether `cat` could be the last living cat: false if another visible cat still has substantial HP.
    public static boolean isLastCat(RobotInfo cat) {
        if (cat == null) return false;
        for (int i = RobotTracker.numCats - 1; i >= 0; i--) {
            RobotInfo other = RobotTracker.sortedCats[i];
            if (other != null && other.ID != cat.ID && other.health > 200) return false;
        }
        return true;
    }

    // Whether the last-cat hold is currently active.
    public static boolean isHoldActive() {
        if (!rc.isCooperation()) return false;
        if (Tune.PM_COOP_HOLD != 0) {
            return GlobalArray.countRatKings() < 2;
        }
        if ((Tune.COOP_CTRL & 8) == 0) return false;
        if (time >= 1800) return false;
        if (GlobalArray.countRatKings() != 1) return false;
        return GlobalArray.hasActiveFormation() || rc.getGlobalCheese() >= 300;
    }

    public static void noteLowCat() {
        lastLowCatRound = time;
    }

    public static boolean isRecentlyThreatened() {
        return time - lastLowCatRound <= 25;
    }

    // May we deal `damage` to this cat now?
    public static boolean mayDamage(RobotInfo cat, int damage) {
        if (!isHoldActive()) return true;
        if (Tune.PM_COOP_HOLD == 0 && !isLastCat(cat)) return true;
        if (cat.health > damage + TRAP_DAMAGE * rc.getNumberCatTraps()) return true;
        lastLowCatRound = time;
        return false;
    }

    // Our traps already threaten to kill a cat in view: a baby rat removes its own cat traps around it (free, no
    // cooldown), so that bites, not traps, take the cat down to its last HP.
    public static void defuseTraps() throws GameActionException {
        if (!isHoldActive() || rc.getNumberCatTraps() == 0) return;
        int traps = TRAP_DAMAGE * rc.getNumberCatTraps();
        boolean threatened = false;
        for (int i = RobotTracker.numCats - 1; i >= 0; i--) {
            RobotInfo cat = RobotTracker.sortedCats[i];
            if (cat != null && cat.health <= traps && (Tune.PM_COOP_HOLD != 0 || isLastCat(cat))) {
                threatened = true;
                break;
            }
        }
        if (!threatened) return;
        lastLowCatRound = time;
        for (Direction d : Util.allDirections) {
            MapLocation loc = myLoc.add(d);
            if (rc.canRemoveCatTrap(loc)) rc.removeCatTrap(loc);
        }
    }
}
