package opp_gst.DataStructures;

public class BitMap {
    int width;
    int height;
    int[] map;

    public BitMap(int width, int height) {
        this.width = width;
        this.height = height;
        map = new int[Math.ceilDiv(width * height, 32)];
    }

    public boolean get(int x, int y) {
        int cell = y * width + x;
        return ((map[cell / 32] >> cell % 32) & 1) == 1;
    }

    public void toggle(int x, int y) {
        int cell = y * width + x;
        int index = cell / 32;
        int bit = cell % 32;
        map[index] ^= 1 << bit;
    }
}
