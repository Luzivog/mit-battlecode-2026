package battlecode.world;

/** Map helpers for bcx. Plain Java only (translated to C++). */
public final class BcxMaps {
    private BcxMaps() {
    }

    /**
     * The map with its seed replaced, built with the jar's own LiveMap constructor: the same map the repo's
     * src/battlecode/world/GameMapIO override loads with that seed (it passes the seed to the same constructor in
     * place of the file's randomSeed; the arrays are copied and the initial bodies sorted again, which keeps them).
     */
    public static LiveMap withSeed(LiveMap m, int seed) {
        return new LiveMap(m.getWidth(), m.getHeight(), m.getOrigin(), seed, m.getRounds(), m.getMapName(),
                m.getSymmetry(), m.getWallArray(), m.getDirtArray(), m.getCheeseMineArray(), m.getCheeseArray(),
                m.getCatWaypointIDs(), m.getCatWaypoints(), m.getInitialBodies());
    }
}
