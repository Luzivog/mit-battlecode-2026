package bcx.subst;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.world.LiveMap;

/** Fast path of battlecode.world.GameWorld.bfsFromTarget (the cats' BFS tables, one per target and symmetry, built
 *  at game start): the same breadth-first search on tile indices, with no allocation per step. The original stays as
 *  bfsFromTarget$orig; FastPath.CHECK runs both and compares the target's column. */
public final class GameWorld_p {
    @Orig private LiveMap gameMap;
    @Orig private boolean[] walls;
    @Orig private Direction[][] bfs_map_0;
    @Orig private Direction[][] bfs_map_1;

    // per symmetry: what the map passes for a 2x2 step (the four checked tiles on the map and not walls), the step's
    // deltas and recorded directions, in the original's order; built on first use from the map the world holds
    private boolean[][] bcxBfsPass;
    private int[][] bcxBfsStep;
    private Direction[][] bcxBfsBack;
    private int[] bcxBfsQueue;
    private boolean[] bcxBfsWalls;

    public void bfsFromTarget(MapLocation target, int sym) {
        if (!FastPath.CHECK) {
            if (!bcxBfs(target, sym)) bfsFromTarget$orig(target, sym);
            return;
        }
        Direction[][] bfs = sym == 0 ? bfs_map_0 : bfs_map_1;
        int t = locationToIndex(target);
        int n = bfs.length;
        Direction[] before = new Direction[n];
        for (int i = 0; i < n; i++) before[i] = bfs[i][t];
        bfsFromTarget$orig(target, sym);
        Direction[] ref = new Direction[n];
        for (int i = 0; i < n; i++) { ref[i] = bfs[i][t]; bfs[i][t] = before[i]; }
        if (!bcxBfs(target, sym)) bfsFromTarget$orig(target, sym);
        for (int i = 0; i < n; i++)
            if (bfs[i][t] != ref[i]) FastPath.fail("GameWorld.bfsFromTarget: tile " + i + " target " + t + " sym " + sym);
    }

    /** False when the fast path does not cover the call (the original then runs). */
    private boolean bcxBfs(MapLocation target, int sym) {
        if (sym != 0 && sym != 1) return false;
        LiveMap m = gameMap;
        int w = m.getWidth(), h = m.getHeight();
        MapLocation o = m.getOrigin();
        int tx = target.x - o.x, ty = target.y - o.y;
        if (tx < 0 || ty < 0 || tx >= w || ty >= h) return false;
        Direction[][] bfs = sym == 0 ? bfs_map_0 : bfs_map_1;
        int n = w * h;
        if (bfs.length != n || walls.length != n) return false;
        if (bcxBfsPass == null || bcxBfsWalls != walls || bcxBfsQueue.length != n) bcxBfsSetup(w, h);
        boolean[] pass = bcxBfsPass[sym];
        int[] step = bcxBfsStep[sym];
        Direction[] back = bcxBfsBack[sym];
        int[] q = bcxBfsQueue;
        int t = tx + ty * w;
        bfs[t][t] = Direction.CENTER;
        q[0] = t;
        int head = 0, tail = 1;
        while (head < tail) {
            int cur = q[head++];
            int cx = cur % w, cy = cur / w;
            for (int k = 0; k < 8; k++) {
                int nx = cx + step[2 * k], ny = cy + step[2 * k + 1];
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                int ni = nx + ny * w;
                Direction[] row = bfs[ni];
                if (row[t] != null || !pass[ni]) continue;
                row[t] = back[k];
                q[tail++] = ni;
            }
        }
        return true;
    }

    private void bcxBfsSetup(int w, int h) {
        int n = w * h;
        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTHEAST,
                Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST};
        Direction[] checks = {Direction.CENTER, Direction.NORTH, Direction.NORTHEAST, Direction.EAST};
        bcxBfsPass = new boolean[2][n];
        bcxBfsStep = new int[2][16];
        bcxBfsBack = new Direction[2][8];
        for (int sym = 0; sym < 2; sym++) {
            for (int k = 0; k < 8; k++) {
                Direction d = sym == 1 ? flipDirBySymmetry(dirs[k]) : dirs[k];
                bcxBfsStep[sym][2 * k] = d.getDeltaX();
                bcxBfsStep[sym][2 * k + 1] = d.getDeltaY();
                bcxBfsBack[sym][k] = d.opposite();
            }
            int[] c = new int[8];
            for (int j = 0; j < 4; j++) {
                Direction d = sym == 1 ? flipDirBySymmetry(checks[j]) : checks[j];
                c[2 * j] = d.getDeltaX();
                c[2 * j + 1] = d.getDeltaY();
            }
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    boolean ok = true;
                    for (int j = 0; j < 4 && ok; j++) {
                        int px = x + c[2 * j], py = y + c[2 * j + 1];
                        ok = px >= 0 && py >= 0 && px < w && py < h && !walls[px + py * w];
                    }
                    bcxBfsPass[sym][x + y * w] = ok;
                }
        }
        bcxBfsQueue = new int[n];
        bcxBfsWalls = walls;
    }

    @Orig
    public void bfsFromTarget$orig(MapLocation target, int sym) {}

    @Orig
    public int locationToIndex(MapLocation loc) { return 0; }

    @Orig
    public Direction flipDirBySymmetry(Direction d) { return null; }
}
