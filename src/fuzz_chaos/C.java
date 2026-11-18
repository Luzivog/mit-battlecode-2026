package fuzz_chaos;

import battlecode.common.*;
import java.util.*;

// Chaos acts: bytecode-limit overruns at many points, single operations costing more than a turn, Clock.yield()
// in odd places, swallowed errors, debug_ methods. Each act returns a hash folded into the robot's state.
final class C {
    static RobotController rc;
    static int acc;
    static int dbgAcc;
    static int cmpCount;

    static int spin(int iters) {
        int x = acc;
        for (int i = 0; i < iters; i++) x = x * 31 + (i ^ (x >>> 7));
        return x;
    }

    static final class HeavyInit {
        static final int V;

        static {
            int x = 0;
            for (int i = 0; i < 30000; i++) x += i * (i ^ 5);
            V = x;
        }
    }

    static final class YieldInit {
        static final int R;

        static {
            int r0 = rc.getRoundNum();
            Clock.yield();
            Clock.yield();
            R = rc.getRoundNum() - r0;
        }
    }

    static final class BadInit {
        static int V = 3;

        static {
            if (V == 3) throw new IllegalArgumentException("bad clinit");
        }
    }

    static final class Boom extends RuntimeException {
        Boom(String m) {
            super(m);
        }
    }

    // ------------------------------------------------------------------ debug_ methods
    // With bc.engine.debug-methods=false (the default) every call to a void debug_ method is removed, so dbgAcc
    // stays 0 and nothing below happens; with it on they run without being counted.

    static void debug_heavy(int n) {
        for (int i = 0; i < n; i++) dbgAcc += i ^ (i >>> 3);
    }

    static void debug_side() throws GameActionException {
        dbgAcc++;
        rc.setIndicatorDot(rc.getLocation(), 9, 9, 9);
        if (rc.canTurn()) rc.turn(Direction.SOUTH);
    }

    static void debug_throw() {
        dbgAcc += 1000;
        throw new IllegalStateException("debug");
    }

    static void debug_yield() {
        dbgAcc += 7;
        Clock.yield();
        dbgAcc += 7;
    }

    static void debug_nested(int k) {
        debug_heavy(k);
        dbgAcc *= 3;
    }

    static int debug_counted(int v) {
        return v * 3 + 1;
    }

    // ------------------------------------------------------------------ acts

    static int act(int a, int s) throws GameActionException {
        switch (a) {
            case 0:
                return spin(500 + s % 4000);
            case 1: {
                int target = 200 + s % 3000;
                int n = 0;
                while (Clock.getBytecodesLeft() > target) n++;
                return n * 7 + Clock.getBytecodeNum();
            }
            case 2: {
                ArrayList<Integer> l = new ArrayList<>();
                int n = 1500 + s % 2000;
                for (int i = 0; i < n; i++) l.add((i * 7919 + s) % 1009);
                l.sort((x, y) -> x - y);
                return l.get(n / 2) + l.get(n - 1) * 31;
            }
            case 3: {
                HashMap<Integer, Integer> m = new HashMap<>();
                int n = 1000 + s % 2500;
                for (int i = 0; i < n; i++) m.put(i * 37 + s, i);
                int r = 0;
                for (int k : m.keySet()) r = r * 31 + k;
                return r;
            }
            case 4: {
                int[] arr = new int[3000 + s % 6000];
                for (int i = 0; i < arr.length; i++) arr[i] = (i * 7919 + s) % 10007;
                Arrays.sort(arr);
                return arr[arr.length / 3];
            }
            case 5:
                return HeavyInit.V;
            case 6:
                return BigTable.SUM + BigTable.T[s % BigTable.T.length];
            case 7:
                return YieldInit.R;
            case 8:
                try {
                    throw new Boom("catch" + s);
                } catch (Boom e) {
                    T.caught[8]++;
                    return spin(1000 + s % 3000);
                }
            case 9: {
                int r = 0;
                try {
                    r = s;
                } finally {
                    r += spin(800 + s % 3000);
                }
                return r;
            }
            case 10: {
                ArrayList<Integer> l = new ArrayList<>();
                for (int i = 0; i < 60; i++) l.add(i);
                int[] box = {0};
                l.forEach(x -> box[0] += spin(30 + x + s % 50));
                return box[0];
            }
            case 11: {
                int[] big = new int[40000 + s % 100000];
                big[big.length - 1] = s;
                return big.length + big[big.length - 1];
            }
            case 12: {
                int[][] m = new int[100 + s % 200][300];
                m[m.length - 1][299] = 5;
                return m.length;
            }
            case 13: {
                int[] x = new int[30000], y = new int[30000];
                x[29999] = s;
                System.arraycopy(x, 0, y, 0, 30000);
                return y[29999];
            }
            case 14: {
                int r0 = rc.getRoundNum();
                try {
                    acc++;
                } finally {
                    Clock.yield();
                }
                return rc.getRoundNum() - r0;
            }
            case 15: {
                int r = 0;
                for (int i = 0; i < 3; i++) {
                    r = r * 31 + rc.getRoundNum() + spin(50);
                    Clock.yield();
                }
                return r;
            }
            case 16:
                try {
                    throw new Boom("y");
                } catch (Boom e) {
                    T.caught[16]++;
                    Clock.yield();
                    return rc.getRoundNum();
                }
            case 17: {
                cmpCount = 0;
                ArrayList<Integer> l = new ArrayList<>();
                for (int i = 0; i < 40; i++) l.add((i * 13 + s) % 41);
                l.sort((x, y) -> {
                    if (++cmpCount % 25 == 0) Clock.yield();
                    return Integer.compare(y, x);
                });
                return l.get(0) * 1000 + cmpCount;
            }
            case 18:
                try {
                    try {
                        throw new Boom("pending");
                    } finally {
                        Clock.yield();
                    }
                } catch (Boom e) {
                    T.caught[18]++;
                    return rc.getRoundNum();
                }
            case 19:
                return recurseYield(30 + s % 30);
            case 20: {
                int r = 0;
                try {
                    assert s < 0 : "assert message " + s;
                    r = 1;
                } catch (Throwable t) {
                    T.caught[20]++;
                    r = t instanceof AssertionError ? 2 : 3;
                    r += t.getMessage().length();
                }
                try {
                    assert false;
                } catch (AssertionError e) {
                    T.caught[20]++;
                    r += e.getMessage() == null ? 10 : 20;
                }
                return r;
            }
            case 21:
                try {
                    return BadInit.V;
                } catch (ExceptionInInitializerError e) {
                    T.caught[21]++;
                    return 1;
                } catch (NoClassDefFoundError e) {
                    T.caught[21]++;
                    return 2;
                }
            case 22: {
                debug_heavy(100000);
                debug_side();
                try {
                    debug_throw();
                } catch (IllegalStateException e) {
                    T.caught[22]++;
                    acc++;
                }
                debug_yield();
                debug_nested(s % 100);
                Runnable rr = () -> debug_heavy(50);
                rr.run();
                return debug_counted(s) + dbgAcc;
            }
            case 23:
                return spin(20000 + s % 30000);
            case 24:
                return deep(150 + s % 100, s);
            case 25: {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < 400 + s % 400; i++) b.append(i % 7 == 0 ? ',' : (char) ('a' + i % 26));
                String[] parts = b.toString().split(",+");
                return parts.length + b.toString().replaceAll("[aeiou]", "").length();
            }
            case 26: {
                int r = 0;
                for (int i = 0; i < 30 + s % 30; i++) {
                    try {
                        if (i % 2 == 0) throw new Boom("s" + i);
                        else throw new IllegalStateException();
                    } catch (Boom e) {
                        T.caught[26]++;
                        r += 1;
                    } catch (RuntimeException e) {
                        T.caught[26]++;
                        r += 2;
                    }
                }
                return r;
            }
            default:
                return 0;
        }
    }

    static int recurseYield(int d) {
        if (d == 0) {
            Clock.yield();
            return rc.getRoundNum();
        }
        return recurseYield(d - 1) + d;
    }

    static int deep(int d, int s) {
        if (d == 0) return s;
        int x = spin(40);
        return deep(d - 1, s ^ x) + 1;
    }
}
