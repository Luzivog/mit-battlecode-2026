package challenger;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import challenger.comms.GlobalArray;

import static challenger.Game.*;

// CM's per-map data until the book (agent MB) holds it: keyed by the map's size and our team, so only for the maps
// named here. docs/theory/cm-cat-maps.md gives each entry's evidence.
public class CatBook {
    // Second-king sites (Tune.CM_KING2_ROUND), x, y pairs in order of preference: 3x3 centres off every cat loop.
    private static int[] king2Sites() {
        boolean a = rc.getTeam() == Team.A;
        if (mapWidth == 60 && mapHeight == 50) return a ? new int[]{12, 13} : new int[]{12, 36}; // tminusten
        return null;
    }

    // The first site with none of our kings within d² 8 (a formation completes within d² 8 of its site), or null.
    public static MapLocation king2Site() {
        int[] s = king2Sites();
        if (s == null) return null;
        sites:
        for (int i = 0; i < s.length; i += 2) {
            MapLocation c = new MapLocation(s[i], s[i + 1]);
            for (int k = 0; k < 5; k++) {
                MapLocation king = GlobalArray.getRatKing(k);
                if (king != null && king.distanceSquaredTo(c) <= 8) continue sites;
            }
            return c;
        }
        return null;
    }
}
