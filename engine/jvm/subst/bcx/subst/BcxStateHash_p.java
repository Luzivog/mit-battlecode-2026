package bcx.subst;

import battlecode.common.Direction;
import battlecode.world.BcxStateHash;
import battlecode.world.GameWorld;
import battlecode.world.LiveMap;
import java.util.Random;

/** Fast paths of battlecode.world.BcxStateHash (originals kept as NAME$orig; FastPath.CHECK runs both).
 *
 *  bfsHash reads the world's BFS tables directly: getBfsDir(locs[from], locs[to], c) is table c at
 *  [from][to], as locs[i] is the tile of index i.
 *
 *  rngState: the Random's 48-bit state read from its seed field, where the
 *  original draws three ints, solves the LCG for the state and restores it with setSeed. Both leave the seed as it
 *  was and clear haveNextNextGaussian. The original stays as rngState$orig; FastPath.CHECK runs both. */
public final class BcxStateHash_p {
    public static long rngState(Random r) {
        if (r.getClass() != Random.class) return rngState$orig(r);
        if (!FastPath.CHECK) return FastPath.randomState(r);
        long o = rngState$orig(r);
        long s = FastPath.randomState(r);
        if (s != o) FastPath.fail("BcxStateHash.rngState: " + s + " against " + o);
        return s;
    }

    @Orig
    public static long rngState$orig(Random r) { return 0; }

    private static long bfsHash(GameWorld w, LiveMap map) {
        long f = bcxBfsHash(w, map);
        if (FastPath.CHECK) {
            long o = bfsHash$orig(w, map);
            if (o != f) FastPath.fail("BcxStateHash.bfsHash: " + f + " against " + o);
        }
        return f;
    }

    private static long bcxBfsHash(GameWorld w, LiveMap map) {
        GameWorld_p g = (GameWorld_p) (Object) w;
        int n = map.getWidth() * map.getHeight();
        if (map != w.getGameMap() || !g.bcxBfsTablesCover(n)) return bfsHash$orig(w, map);
        BcxStateHash h = BcxStateHash.chain();
        for (int c = 0; c < 2; c++) {
            Direction[][] t = g.bcxBfsTable(c);
            for (int from = 0; from < n; from++) {
                Direction[] row = t[from];
                for (int to = 0; to < n; to++) {
                    Direction d = row[to];
                    h.add(d == null ? -1 : d.ordinal());
                }
            }
        }
        return h.finish();
    }

    @Orig
    private static long bfsHash$orig(GameWorld w, LiveMap map) { return 0; }
}
