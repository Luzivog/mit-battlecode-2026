package challenger;

import battlecode.common.MapLocation;
import battlecode.common.Team;

import static challenger.Game.*;

// CM's per-map data until the book (agent MB) holds it: keyed by the map's size and our team, so only for the maps
// named here. docs/theory/cm-cat-maps.md gives each entry's evidence.
public class CatBook {
    // The second king's site (Tune.CM_KING2_ROUND): a 3x3 centre off every cat loop, on a mine.
    public static MapLocation king2Site() {
        boolean a = rc.getTeam() == Team.A;
        if (mapWidth == 60 && mapHeight == 50) return a ? new MapLocation(12, 13) : new MapLocation(12, 36); // tminusten
        return null;
    }
}
