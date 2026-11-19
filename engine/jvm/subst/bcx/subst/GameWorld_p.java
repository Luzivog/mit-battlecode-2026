package bcx.subst;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.world.LiveMap;
import battlecode.world.MapSymmetry;

/** Fast paths of battlecode.world.GameWorld (originals kept as NAME$orig; FastPath.CHECK runs both).
 *
 *  bfsFromTarget (the cats' BFS tables, one per target and symmetry, built
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

    /** getAllLocationsWithinConeRadiusSquaredWithoutMap without its boxed coordinate lists and iterators: the same
     *  box, the same visiting order (each axis reversed as the map's symmetry says when chirality is 1), the same
     *  per-tile test, the accepted tiles in order. */
    public MapLocation[] getAllLocationsWithinConeRadiusSquaredWithoutMap(MapLocation origin, int width, int height,
            MapLocation center, Direction dir, double angle, int r2, int chirality) {
        MapLocation[] f = bcxCone(origin, width, height, center, dir, angle, r2, chirality);
        if (!FastPath.CHECK) return f;
        MapLocation[] o = getAllLocationsWithinConeRadiusSquaredWithoutMap$orig(origin, width, height, center, dir, angle, r2, chirality);
        boolean same = o.length == f.length;
        for (int i = 0; same && i < o.length; i++) same = o[i].x == f[i].x && o[i].y == f[i].y;
        if (!same) FastPath.fail("GameWorld.getAllLocationsWithinConeRadiusSquaredWithoutMap: " + center + " " + dir + " " + angle + " " + r2 + " " + chirality);
        return f;
    }

    private MapLocation[] bcxConeBuf;

    private MapLocation[] bcxCone(MapLocation origin, int width, int height, MapLocation center, Direction dir,
            double angle, int r2, int chirality) {
        MapSymmetry sym = null;
        if (chirality == 1) sym = getGameMap().getSymmetry();
        if (origin == null || center == null || (chirality == 1 && sym == null))
            return getAllLocationsWithinConeRadiusSquaredWithoutMap$orig(origin, width, height, center, dir, angle, r2, chirality);
        int r = (int) Math.ceil(Math.sqrt((double) r2)) + 1;
        int x0 = Math.max(center.x - r, origin.x), y0 = Math.max(center.y - r, origin.y);
        int x1 = Math.min(center.x + r, origin.x + width - 1), y1 = Math.min(center.y + r, origin.y + height - 1);
        long nx = (long) x1 - x0 + 1, ny = (long) y1 - y0 + 1;
        if (nx <= 0 || ny <= 0) return new MapLocation[0];
        if (nx * ny > 1 << 16)
            return getAllLocationsWithinConeRadiusSquaredWithoutMap$orig(origin, width, height, center, dir, angle, r2, chirality);
        boolean revX = sym == MapSymmetry.VERTICAL || sym == MapSymmetry.ROTATIONAL;
        boolean revY = sym == MapSymmetry.HORIZONTAL || sym == MapSymmetry.ROTATIONAL;
        MapLocation[] buf = bcxConeBuf;
        if (buf == null || buf.length < nx * ny) bcxConeBuf = buf = new MapLocation[(int) (nx * ny)];
        int n = 0;
        if (dir == null) {
            for (int i = 0; i < nx; i++) {
                int x = revX ? x1 - i : x0 + i;
                for (int j = 0; j < ny; j++) {
                    MapLocation loc = new MapLocation(x, revY ? y1 - j : y0 + j);
                    if (center.isWithinDistanceSquared(loc, r2, dir, angle)) buf[n++] = loc;
                }
            }
        } else {
            // the test is a function of the offset from center (the coordinates enter it as exact doubles and, for
            // offsets within r, exact ints), r2, dir and angle: each offset's answer is the test's own, remembered
            int side = 2 * r + 1;
            byte[] mask = bcxConeMask(dir, angle, r2, side);
            for (int i = 0; i < nx; i++) {
                int x = revX ? x1 - i : x0 + i;
                int row = (x - center.x + r) * side - center.y + r;
                for (int j = 0; j < ny; j++) {
                    int y = revY ? y1 - j : y0 + j;
                    byte m = mask[row + y];
                    if (m == 1) continue;
                    MapLocation loc = new MapLocation(x, y);
                    if (m == 0) {
                        m = center.isWithinDistanceSquared(loc, r2, dir, angle) ? (byte) 2 : (byte) 1;
                        mask[row + y] = m;
                        if (m == 1) continue;
                    }
                    buf[n++] = loc;
                }
            }
        }
        MapLocation[] out = new MapLocation[n];
        System.arraycopy(buf, 0, out, 0, n);
        return out;
    }

    // remembered cone tests: per key (dir, angle, r2), side x side offsets, 0 unknown, 1 outside, 2 inside
    private Direction[] bcxMaskDir;
    private double[] bcxMaskAngle;
    private int[] bcxMaskR2;
    private byte[][] bcxMask;
    private int bcxMaskN, bcxMaskLast;

    private byte[] bcxConeMask(Direction dir, double angle, int r2, int side) {
        int last = bcxMaskLast;
        if (last < bcxMaskN && bcxMaskDir[last] == dir && bcxMaskAngle[last] == angle && bcxMaskR2[last] == r2) return bcxMask[last];
        for (int i = 0; i < bcxMaskN; i++)
            if (bcxMaskDir[i] == dir && bcxMaskAngle[i] == angle && bcxMaskR2[i] == r2) {
                bcxMaskLast = i;
                return bcxMask[i];
            }
        if (bcxMaskDir == null || bcxMaskN == bcxMaskDir.length) {
            int cap = bcxMaskDir == null ? 16 : 2 * bcxMaskN;
            Direction[] d = new Direction[cap];
            double[] a = new double[cap];
            int[] q = new int[cap];
            byte[][] m = new byte[cap][];
            for (int i = 0; i < bcxMaskN; i++) { d[i] = bcxMaskDir[i]; a[i] = bcxMaskAngle[i]; q[i] = bcxMaskR2[i]; m[i] = bcxMask[i]; }
            bcxMaskDir = d;
            bcxMaskAngle = a;
            bcxMaskR2 = q;
            bcxMask = m;
        }
        int i = bcxMaskN++;
        bcxMaskDir[i] = dir;
        bcxMaskAngle[i] = angle;
        bcxMaskR2[i] = r2;
        bcxMask[i] = new byte[side * side];
        bcxMaskLast = i;
        return bcxMask[i];
    }

    @Orig
    public MapLocation[] getAllLocationsWithinConeRadiusSquaredWithoutMap$orig(MapLocation origin, int width, int height,
            MapLocation center, Direction dir, double angle, int r2, int chirality) { return null; }

    @Orig
    public LiveMap getGameMap() { return null; }

    @Orig
    public void bfsFromTarget$orig(MapLocation target, int sym) {}

    @Orig
    public int locationToIndex(MapLocation loc) { return 0; }

    @Orig
    public Direction flipDirBySymmetry(Direction d) { return null; }
}
