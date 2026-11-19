package challenger.util;

import battlecode.common.Clock;

import challenger.CellTracker;
import challenger.comms.GlobalArray;
import challenger.util.string.FastIndexedIntSet;

import static challenger.Game.*;
import static challenger.util.Util.*;

// A lower bound on the number of cheese mines on the map, from the mines the team has shared. Every mine's mirror is a
// mine, so the count is at least the known mines plus their unknown mirrors under the symmetry, and at least the
// smallest such count over the symmetries still possible. It only grows: mines are never forgotten.
public final class MineEstimate {
    private MineEstimate() {
    }

    private static int lastKey = -1, lastEst;

    // The estimate counted up to cap: cap means "at least cap". Recomputed only when the known mines, the symmetries
    // still possible or the cap change, and only with bytecode to spare (else n, a lower bound too).
    public static int atLeast(int cap) {
        FastIndexedIntSet mines = GlobalArray.globalCheeseMines;
        int n = mines.size();
        if (n >= cap) return cap;
        int key = n | (CellTracker.rsym | CellTracker.hsym << 1 | CellTracker.vsym << 2) << 8 | cap << 11;
        if (key == lastKey) return lastEst;
        if (2 * n < cap) return n;
        if (Clock.getBytecodesLeft() < 4000) return n;
        lastKey = key;
        int est = n;
        if (CellTracker.rsym + CellTracker.hsym + CellTracker.vsym > 0) {
            est = cap;
            if (CellTracker.rsym == 1) est = Math.min(est, withMirrors(mines, n, cap, true, true));
            if (CellTracker.hsym == 1) est = Math.min(est, withMirrors(mines, n, cap, true, false));
            if (CellTracker.vsym == 1) est = Math.min(est, withMirrors(mines, n, cap, false, true));
        }
        lastEst = est;
        return est;
    }

    // Known mines plus their unknown mirrors, counted up to cap. encode2x2 halves coordinates, so on an odd dimension
    // the mirror of an odd coordinate lands one cell high: the cell below counts as the same mine.
    private static int withMirrors(FastIndexedIntSet mines, int n, int cap, boolean fx, boolean fy) {
        boolean oddX = fx && (mapWidth & 1) == 1;
        boolean oddY = fy && (mapHeight & 1) == 1;
        int est = n;
        for (int i = 0; i < n && est < cap; i++) {
            int e = mines.at(i);
            int x = (e & 31) << 1, y = (e >>> 5) << 1;
            int s = encode2x2(fx ? mapWidth - 1 - x : x, fy ? mapHeight - 1 - y : y);
            if (mines.contains(s)) continue;
            if (oddX && mines.contains(s - 1)) continue;
            if (oddY && mines.contains(s - 32)) continue;
            if (oddX && oddY && mines.contains(s - 33)) continue;
            est++;
        }
        return est;
    }
}
