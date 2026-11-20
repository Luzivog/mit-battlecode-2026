package challenger.robot.king;

import battlecode.common.MapLocation;
import challenger.Tune;

/*
  The book switch Tune.PM_SKIP_FAILED (KF's arm T, docs/theory/pm-per-map.md): the formation coordinator remembers the
  mines whose formation timed out and picks another site. 1: the last one only (KF's T); 2: every one. On where_are_you
  a lock-in formation restarts at the site that just timed out, up to six times in a row, while the army is at its cap.
*/
final class FailedSites {
    private FailedSites() {
    }

    private static final MapLocation[] sites = new MapLocation[16];
    private static int n = 0;

    static void add(MapLocation mine) {
        if (mine == null) return;
        if (Tune.PM_SKIP_FAILED == 1) n = 0;
        if (n < sites.length) sites[n++] = mine;
    }

    static boolean has(MapLocation mine) {
        for (int i = n - 1; i >= 0; i--) {
            if (sites[i].equals(mine)) return true;
        }
        return false;
    }
}
