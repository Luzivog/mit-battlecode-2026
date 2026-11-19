package challenger;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import static challenger.Game.*;

// Cat footprint memory: bit x of lane[y] is set once a cat's 2x2 has been seen on tile (x, y). A cat walks a fixed loop
// between its waypoints (BFS on walls, rules §12), so a tile it covered once is covered again on every loop
// (docs/theory/t4-maps.md §2.3).
public class CatLanes {
    public static final long[] lane = new long[60];
    // Bumped whenever a footprint tile is added; 0 while none is known.
    public static int version;

    // Start with every cat loop of the map (Book.CAT_LANE, docs/theory/cm-cat-maps.md §3).
    public static void loadBook() {
        long[] b = Book.CAT_LANE;
        for (int y = b.length; --y >= 0; ) lane[y] = b[y];
        version = 1;
    }

    // The 2x2 of every visible cat (RobotInfo.location is its bottom-left tile).
    public static void markVisibleCats() {
        for (int i = RobotTracker.numCats; --i >= 0; ) {
            MapLocation c = RobotTracker.sortedCats[i].location;
            long m = 3L << c.x;
            if ((lane[c.y] & lane[c.y + 1] & m) != m) {
                lane[c.y] |= m;
                lane[c.y + 1] |= m;
                version++;
            }
        }
    }

    // Footprint tiles under a king's 3x3 centred at (x, y); 9 off the map.
    public static int kingOverlap(int x, int y) {
        if (version == 0) return 0;
        if (x < 1 || y < 1 || x > mapWidth - 2 || y > mapHeight - 2) return 9;
        long m = 7L << (x - 1);
        return Long.bitCount(lane[y - 1] & m) + Long.bitCount(lane[y] & m) + Long.bitCount(lane[y + 1] & m);
    }

    public static boolean onLane(MapLocation loc) {
        return version != 0 && (lane[loc.y] >>> loc.x & 1) != 0;
    }

    // Whether a footprint lies in the 5x5 around c: a king centred on c, or one step from it, would cover it.
    public static boolean nearLane(MapLocation c) {
        if (version == 0) return false;
        int x = c.x - 2;
        long m = x >= 0 ? 31L << x : 31L >>> -x;
        int y1 = Math.min(mapHeight - 1, c.y + 2);
        for (int y = Math.max(0, c.y - 2); y <= y1; y++) {
            if ((lane[y] & m) != 0) return true;
        }
        return false;
    }
}
