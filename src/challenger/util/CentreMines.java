package challenger.util;

import battlecode.common.*;
import challenger.CellTracker;
import challenger.Game;
import static challenger.Game.*;
import static challenger.util.Util.*;

// Centre mines (docs/theory/w2-classes.md §1): known mines whose Chebyshev distances to the two starting kings differ
// by at most MARGIN. Our first king writes its start to shared slot 15 on round 1 (encode2x2 + 1); the enemy's start is
// its mirror under the known symmetry. Known mines are 2x2-rounded (encode2x2), so a mine is known to within a tile.
public final class CentreMines {
    private CentreMines() {
    }

    public static final int HOME_SLOT = 15;
    private static final int MARGIN = 3;
    private static final int BOX = 5; // the spawn box is Chebyshev 4 around the mine, +1 for the rounding

    private static MapLocation home, enemyHome;
    private static int lastKnown = -1;
    private static StringBuilder centre = new StringBuilder(); // encode2x2 of the centre mines

    public static void writeHome(MapLocation start) throws GameActionException {
        rc.writeSharedArray(HOME_SLOT, encode2x2(start) + 1);
    }

    // Rebuilds the centre list when the known mines change; false while our start or the symmetry is unknown.
    public static boolean refresh() throws GameActionException {
        if (enemyHome == null) {
            if (!CellTracker.isSymmetryKnown()) return false;
            if (home == null) {
                int v = rc.readSharedArray(HOME_SLOT);
                if (v == 0) return false;
                home = decode2x2(v - 1);
            }
            enemyHome = getSymmetricLocation(home);
            if (enemyHome == null) return false;
            lastKnown = -1;
        }
        int n = Game.knownCheeseMines.set.length();
        if (n != lastKnown) {
            lastKnown = n;
            centre = new StringBuilder();
            for (int i = 0; i < n; i++) {
                int e = Game.knownCheeseMines.at(i);
                MapLocation m = decode2x2(e);
                int d = cheb(m, home) - cheb(m, enemyHome);
                if (d <= MARGIN && d >= -MARGIN) centre.append((char) e);
            }
        }
        return true;
    }

    public static MapLocation enemyHome() {
        return enemyHome;
    }

    // The centre mine within Chebyshev `range` of loc (the nearest), or null. Call after refresh() returned true.
    public static MapLocation near(MapLocation loc, int range) {
        MapLocation best = null;
        int bd = range + 1;
        for (int i = centre.length() - 1; i >= 0; i--) {
            MapLocation m = decode2x2(centre.charAt(i));
            int d = cheb(m, loc);
            if (d < bd) {
                bd = d;
                best = m;
            }
        }
        return best;
    }

    public static MapLocation boxOf(MapLocation loc) {
        return near(loc, BOX);
    }

    private static int cheb(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }
}
