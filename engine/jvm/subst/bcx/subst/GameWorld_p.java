package bcx.subst;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.world.CheeseMine;
import battlecode.world.InternalRobot;
import battlecode.world.LiveMap;
import battlecode.world.Trap;
import java.util.ArrayList;
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
    @Orig private boolean[] dirt;
    @Orig private int[] cheeseAmounts;
    @Orig private InternalRobot[][] robots;
    @Orig private InternalRobot[][] flyingRobots;
    @Orig private Trap[][] trapLocations;
    @Orig private ArrayList<Trap>[] trapTriggers;
    @Orig private CheeseMine[] cheeseMineLocs;

    // per symmetry: what the map passes for a 2x2 step (the four checked tiles on the map and not walls), the step's
    // deltas and recorded directions, in the original's order; built on first use from the map the world holds
    private int[][] bcxBfsNbr;  // per tile, its 8 steps' tiles in the original's order, -1 off the map or not passable
    private Direction[][] bcxBfsBack;
    private int[] bcxBfsQueue;
    private Direction[][][] bcxBfsRows;  // per symmetry, the table whose rows were checked for the native loop
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
        if (bcxBfsNbr == null || bcxBfsWalls != walls || bcxBfsQueue.length != n) bcxBfsSetup(w, h);
        if (bcxBfsRows[sym] != bfs) {
            // the native loop reads rows unchecked: every row present and n long
            for (int i = 0; i < n; i++) if (bfs[i] == null || bfs[i].length != n) return false;
            bcxBfsRows[sym] = bfs;
        }
        FastPath.bfs(bcxBfsNbr[sym], bfs, bcxBfsBack[sym], bcxBfsQueue, tx + ty * w, Direction.CENTER);
        return true;
    }

    private void bcxBfsSetup(int w, int h) {
        int n = w * h;
        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTHEAST,
                Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.NORTHWEST};
        Direction[] checks = {Direction.CENTER, Direction.NORTH, Direction.NORTHEAST, Direction.EAST};
        boolean[] pass = new boolean[n];
        int[] step = new int[16];
        bcxBfsNbr = new int[2][8 * n];
        bcxBfsBack = new Direction[2][8];
        for (int sym = 0; sym < 2; sym++) {
            for (int k = 0; k < 8; k++) {
                Direction d = sym == 1 ? flipDirBySymmetry(dirs[k]) : dirs[k];
                step[2 * k] = d.getDeltaX();
                step[2 * k + 1] = d.getDeltaY();
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
                    pass[x + y * w] = ok;
                }
            int[] nbr = bcxBfsNbr[sym];
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    for (int k = 0; k < 8; k++) {
                        int nx = x + step[2 * k], ny = y + step[2 * k + 1];
                        nbr[8 * (x + y * w) + k] = nx >= 0 && ny >= 0 && nx < w && ny < h && pass[nx + ny * w] ? nx + ny * w : -1;
                    }
        }
        bcxBfsQueue = new int[n];
        bcxBfsRows = new Direction[2][][];
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
        // the test is a function of the offset from center (the coordinates enter it as exact doubles and, for
        // offsets within r, exact ints), r2, dir and angle: each (r2, angle, dir) has a mask of the test's own
        // answers, evaluated at once, and each column's range of accepted offsets
        boolean memo = dir != null && angle == angle && r2 >= 0 && r2 <= 1 << 20 && center.x > -(1 << 28)
                && center.x < 1 << 28 && center.y > -(1 << 28) && center.y < 1 << 28;
        byte[] mask = null;
        int r;
        if (memo) {
            mask = bcxConeMask(dir, angle, r2, false);
            r = bcxMaskR;
        } else r = (int) Math.ceil(Math.sqrt((double) r2)) + 1;
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
        if (!memo) {
            for (int i = 0; i < nx; i++) {
                int x = revX ? x1 - i : x0 + i;
                for (int j = 0; j < ny; j++) {
                    MapLocation loc = new MapLocation(x, revY ? y1 - j : y0 + j);
                    if (center.isWithinDistanceSquared(loc, r2, dir, angle)) buf[n++] = loc;
                }
            }
        } else {
            int side = 2 * r + 1;
            int[] cols = bcxConeCols(mask, side, center, dir, angle, r2);
            int cx = center.x, cy = center.y;
            for (int i = 0; i < nx; i++) {
                int x = revX ? x1 - i : x0 + i;
                int c = x - cx + r;
                int lo = Math.max(y0, cy + cols[2 * c]), hi = Math.min(y1, cy + cols[2 * c + 1]);
                if (lo > hi) continue;
                int row = c * side - cy + r;
                if (revY) {
                    for (int y = hi; y >= lo; y--) if (mask[row + y] == 2) buf[n++] = new MapLocation(x, y);
                } else {
                    for (int y = lo; y <= hi; y++) if (mask[row + y] == 2) buf[n++] = new MapLocation(x, y);
                }
            }
        }
        MapLocation[] out = new MapLocation[n];
        System.arraycopy(buf, 0, out, 0, n);
        return out;
    }

    // remembered tests center.isWithinDistanceSquared(center + offset, r2, dir, angle, bottomLeft): per shape (angle,
    // r2, bottomLeft) and direction, side x side offsets around the center (side = 2 r + 1, r = ceil(sqrt(r2)) + 1,
    // the cone's box), 0 unknown, 1 outside, 2 inside. Shared by the cone fast path and
    // InternalRobot_p.canSenseLocation; bcxMaskR is the r of the last mask returned
    private double[] bcxShapeAngle;
    private int[] bcxShapeR2;
    private boolean[] bcxShapeBl;
    private int[] bcxShapeRadius;
    private byte[][][] bcxShapeMasks;
    private int[][][] bcxShapeCols;  // per shape and direction: per column, the lowest and highest accepted offset
    private int bcxShapeN, bcxShapeLast;
    public int bcxMaskR;

    public byte[] bcxConeMask(Direction dir, double angle, int r2, boolean bl) {
        int i = bcxShapeLast;
        if (i >= bcxShapeN || bcxShapeAngle[i] != angle || bcxShapeR2[i] != r2 || bcxShapeBl[i] != bl) {
            i = 0;
            while (i < bcxShapeN && (bcxShapeAngle[i] != angle || bcxShapeR2[i] != r2 || bcxShapeBl[i] != bl)) i++;
            if (i == bcxShapeN) bcxAddShape(angle, r2, bl);
            bcxShapeLast = i;
        }
        int r = bcxShapeRadius[i];
        bcxMaskR = r;
        byte[][] masks = bcxShapeMasks[i];
        int d = dir.ordinal();
        byte[] m = masks[d];
        if (m == null) masks[d] = m = new byte[(2 * r + 1) * (2 * r + 1)];
        return m;
    }

    /** The column ranges of the last mask bcxConeMask returned (bottomLeft false), evaluating the whole mask at
     *  center + offset on first use: per column dx + r, the lowest and highest dy whose test is true (empty: lo > hi). */
    private int[] bcxConeCols(byte[] mask, int side, MapLocation center, Direction dir, double angle, int r2) {
        int[][] byDir = bcxShapeCols[bcxShapeLast];
        int d = dir.ordinal();
        int[] cols = byDir[d];
        if (cols != null) return cols;
        int r = (side - 1) / 2;
        cols = new int[2 * side];
        for (int c = 0; c < side; c++) {
            int lo = r + 1, hi = -r - 1;
            for (int e = 0; e < side; e++) {
                int k = c * side + e;
                byte m = mask[k];
                if (m == 0) {
                    MapLocation loc = new MapLocation(center.x + c - r, center.y + e - r);
                    m = center.isWithinDistanceSquared(loc, r2, dir, angle, false) ? (byte) 2 : (byte) 1;
                    mask[k] = m;
                }
                if (m == 2) {
                    if (e - r < lo) lo = e - r;
                    hi = e - r;
                }
            }
            cols[2 * c] = lo;
            cols[2 * c + 1] = hi;
        }
        byDir[d] = cols;
        return cols;
    }

    private void bcxAddShape(double angle, int r2, boolean bl) {
        int n = bcxShapeN;
        if (bcxShapeAngle == null || n == bcxShapeAngle.length) {
            int cap = n == 0 ? 8 : 2 * n;
            double[] a = new double[cap];
            int[] q = new int[cap];
            boolean[] b = new boolean[cap];
            int[] r = new int[cap];
            byte[][][] m = new byte[cap][][];
            int[][][] cl = new int[cap][][];
            for (int i = 0; i < n; i++) {
                a[i] = bcxShapeAngle[i]; q[i] = bcxShapeR2[i]; b[i] = bcxShapeBl[i]; r[i] = bcxShapeRadius[i]; m[i] = bcxShapeMasks[i];
                cl[i] = bcxShapeCols[i];
            }
            bcxShapeCols = cl;
            bcxShapeRadius = r;
            bcxShapeAngle = a;
            bcxShapeR2 = q;
            bcxShapeBl = b;
            bcxShapeMasks = m;
        }
        bcxShapeAngle[n] = angle;
        bcxShapeR2[n] = r2;
        bcxShapeBl[n] = bl;
        bcxShapeRadius[n] = (int) Math.ceil(Math.sqrt((double) r2)) + 1;
        bcxShapeMasks[n] = new byte[Direction.values().length][];
        bcxShapeCols[n] = new int[Direction.values().length][];
        bcxShapeN = n + 1;
    }

    @Orig
    public MapLocation[] getAllLocationsWithinConeRadiusSquaredWithoutMap$orig(MapLocation origin, int width, int height,
            MapLocation center, Direction dir, double angle, int r2, int chirality) { return null; }

    @Orig
    public LiveMap getGameMap() { return null; }

    // the tile state BcxStateHash_p.tiles reads (GameWorld's accessors index these)
    public boolean[] bcxWalls() { return walls; }
    public boolean[] bcxDirt() { return dirt; }
    public int[] bcxCheese() { return cheeseAmounts; }
    public CheeseMine[] bcxMines() { return cheeseMineLocs; }
    public InternalRobot[][] bcxRobots() { return robots; }
    public InternalRobot[][] bcxFlying() { return flyingRobots; }
    public Trap[][] bcxTraps() { return trapLocations; }
    public ArrayList<Trap>[] bcxTriggers() { return trapTriggers; }

    /** The BFS table of a chirality, for BcxStateHash_p. */
    public Direction[][] bcxBfsTable(int c) { return c == 0 ? bfs_map_0 : bfs_map_1; }

    /** Whether both tables are n x n with every row present, and the map is n tiles wide times high with the
     *  index arithmetic locationToIndex uses (so getBfsDir of tiles i and j reads [i][j]). */
    public boolean bcxBfsTablesCover(int n) {
        if (gameMap.getWidth() * gameMap.getHeight() != n) return false;
        for (int c = 0; c < 2; c++) {
            Direction[][] t = bcxBfsTable(c);
            if (t == null || t.length != n) return false;
            for (int i = 0; i < n; i++) if (t[i] == null || t[i].length != n) return false;
        }
        return true;
    }

    @Orig
    public void bfsFromTarget$orig(MapLocation target, int sym) {}

    @Orig
    public int locationToIndex(MapLocation loc) { return 0; }

    @Orig
    public Direction flipDirBySymmetry(Direction d) { return null; }
}
