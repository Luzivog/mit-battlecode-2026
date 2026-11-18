package opp_cerr.fast;

import battlecode.common.MapLocation;

public class FastRecentLocSet {
    public StringBuilder keys;
    public final int size;

    public FastRecentLocSet(int maxsize) {
        keys = new StringBuilder();
        size = maxsize;
        keys.append("^==".repeat(maxsize));
    }

    public MapLocation first() {
        if (keys.charAt(1) == '=') return null;
        return new MapLocation((int) keys.charAt(1), (int) keys.charAt(2));
    }

    public void add() {
        keys.append("^==");
        keys.delete(0, 3);
    }

    public void add(MapLocation loc) {
        String key = "^" + (char) (loc.x) + (char) (loc.y);
        keys.append(key);
        keys.delete(0, 3);
    }

    public void add(int x, int y) {
        String key = "^" + (char) x + (char) y;
        keys.append(key);
        keys.delete(0, 3);
    }

    public boolean contains(MapLocation loc) {
        return keys.indexOf("^" + (char) (loc.x) + (char) (loc.y)) >= 0;
    }

    public boolean contains(int x, int y) {
        return keys.indexOf("^" + (char) x + (char) y) >= 0;
    }

    public void clear() {
        keys = new StringBuilder();
        keys.append("^==".repeat(size));
    }

    public MapLocation[] getKeys() {
        MapLocation[] locs = new MapLocation[size];
        for (int i = 1; i < keys.length(); i += 3) {
            locs[i / 3] = new MapLocation((int) keys.charAt(i), (int) keys.charAt(i + 1));
        }
        return locs;
    }
}
