package awubot.comms;

import static awubot.Game.*;
import static awubot.util.Util.*;

/**
 * Baby-rat map manager that keeps a global (shared) tracker and a local tracker
 * that includes personal vision. The global tracker is currently empty because
 * rat kings are not yet publishing map chunks.
 */
public class MapManager {
    // Global knowledge (from shared comms) and local knowledge (global + vision).
    public static MapTracker globalTracker;
    public static MapTracker localTracker;

    // Cached unit bounds for printing and bounds checks.
    private static int maxUnitX;
    private static int maxUnitY;

    /**
     * Initializes trackers for baby rats.
     * The local tracker tracks unseen units; the global tracker does not.
     */
    public static void init() {
        maxUnitX = (mapWidth - MapTracker.UNIT_SIZE) / MapTracker.UNIT_SIZE;
        maxUnitY = (mapHeight - MapTracker.UNIT_SIZE) / MapTracker.UNIT_SIZE;

        if (isRatKing) {
            // Kings do not use MapManager yet; keep a valid but inert tracker.
            globalTracker = new MapTracker(false);
            localTracker = globalTracker;
            return;
        }

        globalTracker = new MapTracker(false);
        localTracker = new MapTracker(true);
    }

    /**
     * Per-turn update for baby rats. Incorporates vision into the local tracker
     * and prints the local seen-map at round 50.
     */
    public static void preTick() {
        // if (time % 50 == 0) {
        //     printLocalSeenUnits();
        //     printGlobalSeenUnits();
        // }
        if (isRatKing || localTracker == null) return;

        // Only local vision is available for now; global tracker remains empty.
        localTracker.updateVisibleUnits();
    }

    /**
     * Applies a global chunk update (when comms exist) and mirrors it into local.
     * @return true if new info was added to the global tracker.
     */
    public static boolean applyGlobalChunk(int encodedChunk) {
        if (globalTracker == null || localTracker == null) return false;
        GlobalArray.noteMapChunkSeen(encodedChunk);
        boolean changed = globalTracker.applyEncodedChunk(encodedChunk);
        if (changed) {
            localTracker.applyEncodedChunk(encodedChunk);
        }
        return changed;
    }

    /**
     * Finds a chunk that differs between local and global for sending to a king.
     * @return encoded chunk payload, or -1 if no differences exist.
     */
    public static int getLocalDiffChunk() {
        if (globalTracker == null || localTracker == null) return -1;
        return MapTracker.findDiffChunk(localTracker, globalTracker);
    }

    /**
     * Prints the local seen-unit grid (units, not cells) to stdout.
     * X = seen unit, . = unseen unit.
     */
    private static void printLocalSeenUnits() {
        if (localTracker == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Local seen units @ round ").append(time).append('\n');
        for (int y = maxUnitY; y >= 0; y--) {
            for (int x = 0; x <= maxUnitX; x++) {
                sb.append(localTracker.isUnitSeen(x, y) ? 'X' : '.');
            }
            sb.append('\n');
        }
        println(sb.toString());
    }

    /**
     * Prints the global seen-unit grid (units, not cells) to stdout.
     * X = seen unit, . = unseen unit.
     */
    private static void printGlobalSeenUnits() {
        if (globalTracker == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Global seen units @ round ").append(time).append('\n');
        for (int y = maxUnitY; y >= 0; y--) {
            for (int x = 0; x <= maxUnitX; x++) {
                sb.append(globalTracker.isUnitSeen(x, y) ? 'X' : '.');
            }
            sb.append('\n');
        }
        println(sb.toString());
    }
}
