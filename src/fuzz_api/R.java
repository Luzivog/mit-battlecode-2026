package fuzz_api;

// Deterministic PRNG (seeded from robot id and round) and the running result hash.
final class R {
    static long s = 1;
    static int h = 0x12345;

    static void seed(int id, int round, int salt) {
        s = (id * 0x9E3779B97F4A7C15L) ^ (round * 0xC2B2AE3D27D4EB4FL) ^ (salt * 0x165667B19E3779F9L) ^ 0x5DEECE66DL;
        if (s == 0) s = 1;
        next(2);
    }

    static int nextInt() {
        s ^= s << 13;
        s ^= s >>> 7;
        s ^= s << 17;
        return (int) (s >>> 16);
    }

    static int next(int n) {
        int v = nextInt() & 0x7fffffff;
        return v % n;
    }

    static void mix(int v) {
        h = Integer.rotateLeft(h ^ v, 5) * 0x27d4eb2d + 0x165667b1;
    }

    static int mixOf(int a, int b) {
        int x = a * 0x9E3779B1 ^ b * 0x85EBCA77;
        x ^= x >>> 15;
        x *= 0x2C1B3C6D;
        x ^= x >>> 12;
        return x & 0x7fffffff;
    }
}
