package challenger.robot.king;

import battlecode.common.*;
import challenger.CellTracker;
import challenger.Pathfinder;
import static challenger.Game.*;

// The king's route to a target, planned over its own map instead of the bug's walk along walls. The bug turned left at
// every obstacle, so on a map with mirror symmetry the two kings went round mirrored walls on opposite sides and the seat
// decided which king took the long way, walked back into a dead end or off along the map edge (docs/theory/ms-map-seats.md).
//
// The plan: a breadth-first search over 3x3 king centres from the king. Seen walls block; unseen cells are open; a step
// onto a centre whose 3x3 holds seen dirt costs 2 (the king digs it first), any other step 1. A centre whose 3x3 touches
// an unseen cell is an exit into the unknown, scored by its cost plus its Chebyshev distance to the target (the unknown
// counted empty); the target scores its cost. A cell whose cost plus distance cannot beat the best score is dropped (that
// sum never falls along a path), so the search ends at the best exit or the target, or proves that the known walls close
// every way (then the bug walks the king, as before). The search runs in the turns the king cannot move and goes on over several turns while it stands still; the
// king follows the path for REPLAN steps, to its end, or until a cell of it turns out walled.
public class KingPath {
    static final int MAXS = 60, REPLAN = 4;
    // a search starts or goes on only with this many bytecodes left, and pauses below STOP
    static final int START = 12000, STOP = 6500;

    static MapLocation pathTarget;
    static final int[] pathX = new int[MAXS + 1], pathY = new int[MAXS + 1];
    static int pathLen, pathIdx, pathSteps;
    // a target the known walls cut off (no exit, no target reachable) or whose search went past MAXS layers goes back to
    // the bug until the round given (in a pocket the bug's walk near the mine is the old play)
    static MapLocation failed;
    static int failedUntil;

    // false when this king's moves to the target are the bug's: a 5x5 king, or a target cut off or its search failed
    public static boolean handles(MapLocation target) {
        return !RatKing.is5x5 && !(target.equals(failed) && time < failedUntil);
    }

    // The next step of the stored path to the target, or null (none, used up, or no longer open). The king may stand on
    // a later cell of it or next to one (after a step round a robot): the path goes on from there.
    private static Direction stored(MapLocation target) {
        if (!target.equals(pathTarget) || pathSteps >= REPLAN) return null;
        for (int k = Math.min(pathLen - 1, pathIdx + 2); k >= pathIdx; k--) {
            if (myLocX == pathX[k] && myLocY == pathY[k]) {
                pathIdx = k + 1;
                pathSteps++;
                break;
            }
        }
        if (pathIdx + 1 < pathLen && Math.max(Math.abs(myLocX - pathX[pathIdx + 1]), Math.abs(myLocY - pathY[pathIdx + 1])) <= 1) pathIdx++;
        if (pathIdx >= pathLen) return null;
        MapLocation n = new MapLocation(pathX[pathIdx], pathY[pathIdx]);
        return myLoc.isAdjacentTo(n) && CellTracker.isPassable3x3(n) ? myLoc.directionTo(n) : null;
    }

    // A turn the king cannot move: plan ahead for the turn it can.
    public static void prepare(MapLocation target) {
        if (Math.max(Math.abs(myLocX - target.x), Math.abs(myLocY - target.y)) <= 1) return;
        if (stored(target) == null && handles(target)) search(target);
    }

    // The step toward the target; CENTER at it (Chebyshev <= 1); null while its search is still running (the king
    // waits) or when it is cut off or failed (then handles() turns false).
    public static Direction next(MapLocation target) {
        if (Math.max(Math.abs(myLocX - target.x), Math.abs(myLocY - target.y)) <= 1) return Direction.CENTER;
        Direction d = stored(target);
        if (d != null || !handles(target)) return d;
        search(target);
        return stored(target);
    }

    // The planned step d is blocked by a robot: a movable step next to the path's cell after d's, or null.
    public static Direction around(Direction d) {
        if (pathIdx + 1 >= pathLen) return null;
        int nx = pathX[pathIdx + 1], ny = pathY[pathIdx + 1];
        Direction[] alts = {d.rotateLeft(), d.rotateRight(), d.rotateLeft().rotateLeft(), d.rotateRight().rotateRight()};
        for (Direction alt : alts) {
            int x = myLocX + alt.dx, y = myLocY + alt.dy;
            if (Math.max(Math.abs(x - nx), Math.abs(y - ny)) <= 1 && rc.canMove(alt)) return alt;
        }
        return null;
    }

    // Starts the search, or goes on with the one for this target from this cell; files its result.
    private static void search(MapLocation target) {
        if (!(searching && target.equals(sTarget) && myLocX == px && myLocY == py)) {
            if (Clock.getBytecodesLeft() < START) return;
            begin(target);
        }
        if (Clock.getBytecodesLeft() < START - 2000) return;
        int r = run();
        if (r == -2) return;
        searching = false;
        pathTarget = null;
        if (r <= 0) {
            failed = target;
            failedUntil = time + (r == 0 ? 100 : 20);
            Pathfinder.reset(); // the bug starts afresh from where the king stands
        } else {
            pathTarget = target;
            pathIdx = 0;
            pathSteps = 0;
            pathLen = r;
        }
    }

    // search state, reused (layers made as first needed). Layer u holds the cells of cost u, in rows lo[u]..hi[u]; the
    // rows it wrote, two more each side, hold no stale bits. P: dirt cells found at the last layer, joining this one.
    static final long[] seenRows = new long[60], passableRows = new long[60], dirtRows = new long[60];
    static final long[][] L = new long[MAXS + 1][];
    static final long[] K = new long[60], X = new long[60], KD = new long[60], vis = new long[60];
    static long[] P = new long[60], Q = new long[60];
    static final int[] lo = new int[MAXS + 1], hi = new int[MAXS + 1];
    static final int[] backX = new int[MAXS + 1], backY = new int[MAXS + 1];
    static boolean searching;
    static MapLocation sTarget;
    static int px, py, tx, ty, kLo, kHi, s, best, bx, by, bs, pLo, pHi;
    static long onMap, interior;

    private static void begin(MapLocation target) {
        int H = mapHeight;
        searching = true;
        sTarget = target;
        px = myLocX;
        py = myLocY;
        tx = target.x;
        ty = target.y;
        onMap = mapWidth >= 64 ? -1L : (1L << mapWidth) - 1;
        interior = onMap & ~1L & ~(1L << (mapWidth - 1));
        CellTracker.copyRows(seenRows, passableRows, dirtRows);
        for (int y = H - 1; y >= 0; y--) {
            vis[y] = 0;
            P[y] = 0;
            Q[y] = 0;
        }
        kLo = py + 1; // rows kLo..kHi of K, X and KD are computed
        kHi = py;
        long[] f0 = L[0];
        if (f0 == null) f0 = L[0] = new long[60];
        for (int y = Math.max(0, py - 2), e = Math.min(H - 1, py + 2); y <= e; y++) f0[y] = 0;
        f0[py] = vis[py] = 1L << px;
        lo[0] = hi[0] = py;
        pLo = H;
        pHi = -1;
        best = Integer.MAX_VALUE;
        bs = -1;
        s = 1;
    }

    // Layers until done: the path's length (pathX/pathY filled, step 1 first); 0 when no exit and no target is
    // reachable; -1 past MAXS layers; -2 paused for bytecodes.
    private static int run() {
        int H = mapHeight, tx = KingPath.tx, ty = KingPath.ty;
        for (; s <= MAXS; s++) {
            if (Clock.getBytecodesLeft() < STOP) return -2;
            int a = Math.max(1, Math.min(lo[s - 1], pLo) - 1), b = Math.min(H - 2, Math.max(hi[s - 1], pHi) + 1);
            // cells that can still beat the best: Chebyshev distance to the target <= k
            int k = best == Integer.MAX_VALUE ? 99 : best - s - 1;
            if (k < 0) break;
            if (a < ty - k) a = ty - k;
            if (b > ty + k) b = ty + k;
            if (a > b) break;
            long[] f = L[s - 1], nf = L[s];
            if (nf == null) nf = L[s] = new long[60];
            while (kLo > a) kRow(--kLo);
            while (kHi < b) kRow(++kHi);
            long keep = k >= 63 ? -1L : band(tx - k, tx + k);
            for (int y = Math.max(0, a - 2), e = Math.min(H - 1, b + 2); y <= e; y++) {
                nf[y] = 0;
                Q[y] = 0;
            }
            long t = f[a - 1], dPrev = t | (t << 1) | (t >>> 1);
            t = f[a];
            long dCur = t | (t << 1) | (t >>> 1);
            int nlo = H, nhi = -1, qlo = H, qhi = -1;
            for (int y = a; y <= b; y++) {
                t = f[y + 1];
                long dNext = t | (t << 1) | (t >>> 1);
                long m = (dPrev | dCur | dNext) & K[y] & ~vis[y] & keep;
                dPrev = dCur;
                dCur = dNext;
                long slow = m & KD[y];
                if (slow != 0) {
                    vis[y] |= slow;
                    Q[y] = slow;
                    if (qlo == H) qlo = y;
                    qhi = y;
                }
                m = (m & ~slow) | (P[y] & keep);
                if (m == 0) continue;
                vis[y] |= m;
                nf[y] = m;
                if (nlo == H) nlo = y;
                nhi = y;
                long ex = m & X[y];
                if (ex != 0 || y == ty) {
                    int dy = y > ty ? y - ty : ty - y;
                    if (y == ty && ((m >>> tx) & 1) != 0 && s < best) {
                        best = s; bx = tx; by = y; bs = s;
                    }
                    if (ex != 0) {
                        long below = ex & ((1L << (tx + 1)) - 1), above = ex & -(1L << tx);
                        if (below != 0) {
                            int x = 63 - Long.numberOfLeadingZeros(below);
                            int sc = s + Math.max(tx - x, dy);
                            if (sc < best) { best = sc; bx = x; by = y; bs = s; }
                        }
                        if (above != 0) {
                            int x = Long.numberOfTrailingZeros(above);
                            int sc = s + Math.max(x - tx, dy);
                            if (sc < best) { best = sc; bx = x; by = y; bs = s; }
                        }
                    }
                }
            }
            long[] swap = P;
            P = Q;
            Q = swap;
            pLo = qlo;
            pHi = qhi;
            lo[s] = nlo;
            hi[s] = nhi;
            if (nhi < 0 && qhi < 0) break;
            if (nhi < 0) {
                // only dirt cells pending: an empty layer keeps the row bounds of the last one
                lo[s] = lo[s - 1];
                hi[s] = hi[s - 1];
                for (int y = Math.max(0, a - 2), e = Math.min(H - 1, b + 2); y <= e; y++) nf[y] = 0;
            }
        }
        if (bs < 0) return s > MAXS ? -1 : 0;
        // one cheapest path back from the best cell: a dirt cell came from two layers down, any other from one; each
        // step the neighbour nearest the target
        int cx = bx, cy = by, u = bs, n = 0;
        while (u > 0) {
            backX[n] = cx;
            backY[n] = cy;
            n++;
            u -= ((KD[cy] >>> cx) & 1) != 0 ? 2 : 1;
            if (u < 0) return -1;
            long[] f = L[u];
            int nx = -1, ny = -1, nd = Integer.MAX_VALUE;
            for (int y = cy - 1; y <= cy + 1; y++) {
                long m = (f[y] >>> (cx - 1)) & 7;
                if (m == 0) continue;
                for (int x = cx - 1; m != 0; x++, m >>>= 1) {
                    if ((m & 1) == 0) continue;
                    int d = (x - tx) * (x - tx) + (y - ty) * (y - ty);
                    if (d < nd) { nd = d; nx = x; ny = y; }
                }
            }
            if (nx < 0) return -1;
            cx = nx;
            cy = ny;
        }
        if (cx != px || cy != py) return -1;
        for (int i = 0; i < n; i++) {
            pathX[i] = backX[n - 1 - i];
            pathY[i] = backY[n - 1 - i];
        }
        return n;
    }

    // K: centres whose 3x3 holds no seen wall; X: those whose 3x3 touches an unseen cell; KD: those holding seen dirt
    private static void kRow(int y) {
        long u0 = ~seenRows[y - 1] & onMap, u1 = ~seenRows[y] & onMap, u2 = ~seenRows[y + 1] & onMap;
        long r = (u0 | passableRows[y - 1]) & (u1 | passableRows[y]) & (u2 | passableRows[y + 1]);
        long k = r & (r << 1) & (r >>> 1) & interior;
        long u = u0 | u1 | u2, d = dirtRows[y - 1] | dirtRows[y] | dirtRows[y + 1];
        K[y] = k;
        X[y] = k & (u | (u << 1) | (u >>> 1));
        KD[y] = k & (d | (d << 1) | (d >>> 1));
    }

    // bits lo..hi (clamped to 0..63)
    private static long band(int lo, int hi) {
        if (lo < 0) lo = 0;
        if (hi > 63) hi = 63;
        if (hi < lo) return 0;
        long upTo = hi == 63 ? -1L : (1L << (hi + 1)) - 1;
        return upTo & -(1L << lo);
    }
}
