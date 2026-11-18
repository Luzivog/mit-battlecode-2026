package fuzz_lang;

import java.util.*;
import java.util.function.*;

// Arrays, System.arraycopy, recursion, static initialisers with side effects, lambdas.
final class LX {
    static int h(int a, int b) {
        return LA.h(a, b);
    }

    static int arrays(int s) {
        int r = 60;
        int[] a = {1, 2, 3, s};
        int[] b = a.clone();
        b[0] = 99;
        r = h(r, a[0] + b[0] + (a == b ? 1 : 0));
        int[][] m = new int[3][4];
        int[][] mc = m.clone();
        mc[1][2] = 7;
        r = h(r, m[1][2]);
        int[][] z = new int[0][5];
        int[][][] z3 = new int[2][0][3];
        int[][] rag = new int[3][];
        rag[0] = new int[1];
        rag[2] = new int[]{5, 6, 7};
        r = h(r, z.length + z3.length * 10 + z3[1].length * 100 + (rag[1] == null ? 1000 : 0) + rag[2][2]);
        long[][] lm = new long[2][s % 5 + 1];
        lm[1][lm[1].length - 1] = 9;
        r = h(r, (int) lm[1][lm[1].length - 1] + lm[0].length);
        char[] cs = new char[3];
        boolean[] bs = new boolean[2];
        double[] ds = new double[2];
        String[] ss = new String[2];
        r = h(r, cs[0] + (bs[1] ? 1 : 0) + (int) ds[0] + (ss[1] == null ? 5 : 0));
        Object arrObj = a;
        r = h(r, arrObj instanceof int[] ? 1 : 0);
        r = h(r, arrObj instanceof Object[] ? 1 : 0);
        Object[] objs = new String[2];
        try {
            objs[0] = Integer.valueOf(1);
        } catch (ArrayStoreException e) {
            K.caught();
            r = h(r, 2);
        }
        try {
            int[] neg = new int[s % 2 == 0 ? -1 : -5];
            r = h(r, neg.length);
        } catch (NegativeArraySizeException e) {
            K.caught();
            r = h(r, 3);
        }
        try {
            r = h(r, a[s % 7 + 4]);
        } catch (ArrayIndexOutOfBoundsException e) {
            K.caught();
            r = h(r, 4);
        }
        try {
            int[] nul = s < 0 ? a : null;
            r = h(r, nul[0]);
        } catch (NullPointerException e) {
            K.caught();
            r = h(r, 5);
        }
        try {
            Object o = new Object[]{"x"};
            String[] sa = (String[]) o;
            r = h(r, sa.length);
        } catch (ClassCastException e) {
            K.caught();
            r = h(r, 6);
        }
        try {
            int[][] neg2 = new int[2][-1];
            r = h(r, neg2.length);
        } catch (NegativeArraySizeException e) {
            K.caught();
            r = h(r, 7);
        }
        Integer[] boxed = {3, null, 1};
        try {
            int t = boxed[1];
            r = h(r, t);
        } catch (NullPointerException e) {
            K.caught();
            r = h(r, 8);
        }
        List<Integer>[] gen = new List[2];
        gen[0] = new ArrayList<>();
        gen[0].add(s);
        r = h(r, gen[0].get(0));
        int[] big = new int[1000 + (s & 255)];
        for (int i = 0; i < big.length; i += 37) big[i] = i;
        r = h(r, big[big.length - 1] + big[370]);
        return r;
    }

    static int arraycopy(int s) {
        int r = 61;
        int[] a = new int[20];
        for (int i = 0; i < a.length; i++) a[i] = i * 3 + s;
        System.arraycopy(a, 0, a, 2, 15);
        r = h(r, Arrays.hashCode(a));
        System.arraycopy(a, 5, a, 1, 10);
        r = h(r, Arrays.hashCode(a));
        int[] b = new int[8];
        System.arraycopy(a, 3, b, 0, 8);
        System.arraycopy(a, 0, b, 0, 0);
        r = h(r, Arrays.hashCode(b));
        Object[] o = {"a", 1, 2.0};
        Object[] p = new Object[5];
        System.arraycopy(o, 0, p, 1, 3);
        r = h(r, p[1] == o[0] ? 1 : 0);
        try {
            System.arraycopy(a, 15, b, 0, 8);
        } catch (IndexOutOfBoundsException e) {
            K.caught();
            r = h(r, 1);
        }
        try {
            System.arraycopy(a, 0, b, -1, 1);
        } catch (IndexOutOfBoundsException e) {
            K.caught();
            r = h(r, 2);
        }
        try {
            System.arraycopy(null, 0, b, 0, 1);
        } catch (NullPointerException e) {
            K.caught();
            r = h(r, 3);
        }
        try {
            System.arraycopy(a, 0, new long[5], 0, 1);
        } catch (ArrayStoreException e) {
            K.caught();
            r = h(r, 4);
        }
        try {
            System.arraycopy(o, 0, new String[3], 0, 3);
        } catch (ArrayStoreException e) {
            K.caught();
            r = h(r, 5);
        }
        try {
            System.arraycopy(a, 0, b, 0, -2);
        } catch (IndexOutOfBoundsException e) {
            K.caught();
            r = h(r, 6);
        }
        char[] c = "abcdef".toCharArray();
        System.arraycopy(c, 1, c, 0, 5);
        r = h(r, new String(c).hashCode());
        return r;
    }

    static int fibMemo(int n, int[] memo) {
        if (n < 2) return n;
        if (memo[n] != 0) return memo[n];
        return memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
    }

    static int ack(int m, int n) {
        if (m == 0) return n + 1;
        if (n == 0) return ack(m - 1, 1);
        return ack(m - 1, ack(m, n - 1));
    }

    static boolean isEven(int n) {
        return n == 0 || isOdd(n - 1);
    }

    static boolean isOdd(int n) {
        return n != 0 && isEven(n - 1);
    }

    static int depth(int d, long a, double b, Object c) {
        if (d == 0) return (int) a + (int) b + (c == null ? 0 : 1);
        return depth(d - 1, a * 3 + d, b / 2 + d, d % 2 == 0 ? c : null) + 1;
    }

    static int parse(String e, int[] pos) {
        int v = term(e, pos);
        while (pos[0] < e.length() && (e.charAt(pos[0]) == '+' || e.charAt(pos[0]) == '-')) {
            char op = e.charAt(pos[0]++);
            int t = term(e, pos);
            v = op == '+' ? v + t : v - t;
        }
        return v;
    }

    static int term(String e, int[] pos) {
        if (e.charAt(pos[0]) == '(') {
            pos[0]++;
            int v = parse(e, pos);
            pos[0]++;
            return v;
        }
        int v = 0;
        while (pos[0] < e.length() && Character.isDigit(e.charAt(pos[0]))) v = v * 10 + e.charAt(pos[0]++) - '0';
        return v;
    }

    static int recursion(int s) {
        int r = 62;
        r = h(r, fibMemo(40 + s % 6, new int[50]));
        r = h(r, ack(2, s % 5));
        r = h(r, isEven(100 + s % 50) ? 1 : 0);
        r = h(r, depth(150 + s % 150, s, s, "o"));
        StringBuilder e = new StringBuilder();
        for (int i = 0; i < 30; i++) e.append('(');
        e.append(s % 100);
        for (int i = 0; i < 30; i++) e.append("+1)");
        r = h(r, parse(e.toString(), new int[1]));
        return r;
    }

    // static initialisers: each robot runs them in the order it first touches the classes
    static final StringBuilder LOG = new StringBuilder();

    static final class InitA {
        static final int V;

        static {
            LOG.append('A');
            InitB.counter += 10;
            V = InitB.counter;
        }
    }

    static final class InitB {
        static int counter;

        static {
            LOG.append('B');
            counter++;
        }
    }

    interface InitI {
        int K = InitC.make('I');

        static int ping() {
            return K;
        }
    }

    static class InitC implements InitI {
        static final int C;

        static {
            LOG.append('C');
            C = LOG.length();
        }

        static int make(char c) {
            LOG.append(c);
            return LOG.length() * 7;
        }
    }

    static class InitD extends InitC {
        static final int D;

        static {
            LOG.append('D');
            D = C + 100;
        }
    }

    static final class InitFail {
        static int V = 1;

        static {
            LOG.append('F');
            if (V == 1) throw new IllegalStateException("init fail");
        }
    }

    static int staticInit(int s) {
        int r = 63;
        switch ((s >> 3) % 6) {
            case 0: r = h(r, InitA.V); break;
            case 1: r = h(r, InitB.counter); break;
            case 2: r = h(r, InitI.ping()); break;
            case 3: r = h(r, InitD.D); break;
            case 4: r = h(r, InitC.C); break;
            default:
                try {
                    r = h(r, InitFail.V);
                } catch (ExceptionInInitializerError e) {
                    K.caught();
                    r = h(r, e.getCause() instanceof IllegalStateException ? 11 : 12);
                } catch (NoClassDefFoundError e) {
                    K.caught();
                    r = h(r, 13);
                }
        }
        r = LS.h(r, LOG.toString());
        return r;
    }

    interface TriFn {
        int apply(int a, int b, int c);
    }

    static int lambdas(int s) {
        int r = 64;
        int local = s % 17;
        Function<Integer, Integer> f = x -> x * 2 + local;
        Function<Integer, Integer> g = f.andThen(x -> x - 1).compose(x -> x + 3);
        r = h(r, g.apply(s % 100));
        BiFunction<Integer, Integer, Integer> add = Integer::sum;
        r = h(r, add.apply(3, local));
        Supplier<List<String>> sup = ArrayList::new;
        List<String> l = sup.get();
        l.add("x");
        Consumer<String> cons = l::add;
        cons.andThen(x -> l.add(x + x)).accept("y");
        r = LS.h(r, l.toString());
        Predicate<Integer> even = x -> x % 2 == 0, pos = x -> x > 0;
        r = h(r, (even.and(pos).test(s) ? 1 : 0) + (even.or(pos).negate().test(-s) ? 2 : 0) + (Predicate.not(even).test(s) ? 4 : 0));
        UnaryOperator<String> up = String::toUpperCase;
        r = LS.h(r, up.apply("abc" + local));
        IntBinaryOperator mul = (a, b) -> a * b;
        IntUnaryOperator inc = x -> x + 1;
        r = h(r, mul.applyAsInt(6, 7) + inc.andThen(inc).applyAsInt(local));
        ToIntFunction<String> len = String::length;
        r = h(r, len.applyAsInt("four"));
        TriFn tri = (a, b, c) -> a * b - c;
        r = h(r, tri.apply(local, 3, 2));
        Comparator<int[]> cmp = Comparator.<int[]>comparingInt(x -> x[0]).thenComparingInt(x -> -x[1]).reversed();
        List<int[]> pts = new ArrayList<>();
        for (int i = 0; i < 6; i++) pts.add(new int[]{(i * 7 + s) % 3, i});
        pts.sort(cmp);
        for (int[] p : pts) r = h(r, p[1]);
        Runnable[] rs = new Runnable[3];
        int[] counter = {0};
        for (int i = 0; i < 3; i++) {
            final int k = i;
            rs[i] = () -> counter[0] += k * local;
        }
        for (Runnable x : rs) x.run();
        r = h(r, counter[0]);
        Function<Integer, Function<Integer, Integer>> curried = a -> b -> a * 10 + b;
        r = h(r, curried.apply(local).apply(3));
        BinaryOperator<Integer> mx = BinaryOperator.maxBy(Comparator.naturalOrder());
        r = h(r, mx.apply(s, local));
        Optional<String> o = Optional.of("v").map(x -> x + local);
        r = LS.h(r, o.orElseGet(() -> "none"));
        return r;
    }
}
