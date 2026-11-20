package challenger;

import static challenger.Game.*;

// Tune.AT_MAP (a book switch, 0 in main): the map's walls and round-1 dirt from the book (Book.AT_WALL, Book.AT_DIRT,
// row masks: bit x of [y]) into CellTracker at the end of the robot's first turn with room for it, every cell seen, a wall not passable (MS's
// ms_okb v2 oracle, docs/theory/ms-map-seats.md section 7, as book data).
public class MapBook {
    public static boolean loaded = false;

    public static void load() {
        loaded = true;
        long[] w = Book.AT_WALL, d = Book.AT_DIRT;
        if (w == null) return;
        long onMap = mapWidth >= 64 ? -1L : (1L << mapWidth) - 1;
        long[] seen = new long[60], pass = new long[60], dirt = new long[60];
        for (int y = 0; y < 60; y++) {
            seen[y] = -1L;
            if (y >= mapHeight) continue;
            pass[y] = onMap & ~(y < w.length ? w[y] : 0L);
            dirt[y] = d != null && y < d.length ? d[y] : 0L;
        }
        CellTracker.loadRows(seen, pass, dirt);
    }
}
