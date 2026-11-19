package bcx.subst;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.world.CheeseMine;
import battlecode.world.InternalRobot;
import battlecode.world.Trap;
import java.util.ArrayList;
import battlecode.world.BcxStateHash;
import battlecode.world.GameWorld;
import battlecode.world.LiveMap;
import java.util.Random;

/** Fast paths of battlecode.world.BcxStateHash (originals kept as NAME$orig; FastPath.CHECK runs both).
 *
 *  bfsHash reads the world's BFS tables directly: getBfsDir(locs[from], locs[to], c) is table c at
 *  [from][to], as locs[i] is the tile of index i.
 *
 *  tiles absorbs what the original absorbs for every tile, in its order, reading the arrays GameWorld's accessors
 *  index (getWall, getDirt, getCheese, hasCheeseMine, getRobot, getFlyingRobot, getTrap, getTrapTriggers) once
 *  their sizes are checked; the dump and anything else go to the original.
 *
 *  rngState: the Random's 48-bit state read from its seed field, where the
 *  original draws three ints, solves the LCG for the state and restores it with setSeed. Both leave the seed as it
 *  was and clear haveNextNextGaussian. The original stays as rngState$orig; FastPath.CHECK runs both. */
public final class BcxStateHash_p {
    @Orig private long state;
    @Orig private long count;
    @Orig private StringBuilder sb;

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

    private void tiles(GameWorld w, LiveMap map) {
        if (sb != null || !FastPath.CHECK) {
            if (sb != null || !bcxTiles(w, map)) tiles$orig(w, map);
            return;
        }
        long s0 = state, c0 = count;
        tiles$orig(w, map);
        long s1 = state, c1 = count;
        state = s0;
        count = c0;
        if (!bcxTiles(w, map)) tiles$orig(w, map);
        if (state != s1 || count != c1) FastPath.fail("BcxStateHash.tiles: round state differs");
    }

    /** False, having absorbed nothing, when the arrays do not cover the map as the accessors read them. */
    private boolean bcxTiles(GameWorld w, LiveMap map) {
        if (map != w.getGameMap()) return false;
        GameWorld_p g = (GameWorld_p) (Object) w;
        int width = map.getWidth(), height = map.getHeight();
        int n = width * height;
        MapLocation origin = map.getOrigin();
        boolean[] walls = g.bcxWalls(), dirt = g.bcxDirt();
        int[] cheese = g.bcxCheese();
        CheeseMine[] mines = g.bcxMines();
        InternalRobot[][] robots = g.bcxRobots(), flying = g.bcxFlying();
        Trap[][] traps = g.bcxTraps();
        ArrayList<Trap>[] triggers = g.bcxTriggers();
        int ta = Team.A.ordinal(), tb = Team.B.ordinal();
        if (width <= 0 || height <= 0 || walls == null || walls.length < n || dirt == null || dirt.length < n
                || cheese == null || cheese.length < n || mines == null || mines.length < n || robots == null
                || robots.length < width || flying == null || flying.length < width || traps == null
                || traps.length <= ta || traps.length <= tb || traps[ta] == null || traps[ta].length < n
                || traps[tb] == null || traps[tb].length < n || triggers == null || triggers.length < n) return false;
        for (int x = 0; x < width; x++)
            if (robots[x] == null || robots[x].length < height || flying[x] == null || flying[x].length < height) return false;
        for (int i = 0; i < n; i++) if (triggers[i] == null) return false;
        Trap[] trapsA = traps[ta], trapsB = traps[tb];
        for (int i = 0; i < n; i++) {
            int x = i % width, y = i / width;
            InternalRobot robot = robots[x][y], fly = flying[x][y];
            absorb(12);
            absorb(i);
            absorb(x + origin.x);
            absorb(y + origin.y);
            absorb(walls[i] ? 1 : 0);
            absorb(dirt[i] ? 1 : 0);
            absorb(cheese[i]);
            absorb(mines[i] != null ? 1 : 0);
            absorb(robot == null ? -1 : robot.getID());
            absorb(fly == null ? -1 : fly.getID());
            trap("trap_a", trapsA[i]);
            trap("trap_b", trapsB[i]);
            ArrayList<Trap> tr = triggers[i];
            int k = tr.size();
            absorb(k);
            for (int j = 0; j < k; j++) trap("trigger", tr.get(j));
        }
        return true;
    }

    @Orig
    private void tiles$orig(GameWorld w, LiveMap map) {}

    @Orig
    private void absorb(long v) {}

    @Orig
    private void trap(String key, Trap t) {}
}
