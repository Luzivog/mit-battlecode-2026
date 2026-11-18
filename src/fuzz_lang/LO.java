package fuzz_lang;

import java.util.*;
import java.util.function.*;

// Records, enums, pattern matching, interfaces, inner classes, generics, varargs, control flow, exceptions.
final class LO {
    static int h(int a, int b) {
        return LA.h(a, b);
    }

    // ------------------------------------------------------------------ records

    record Pt(int x, int y) implements Comparable<Pt> {
        Pt {
            if (x == Integer.MIN_VALUE) throw new IllegalArgumentException("bad x");
        }

        Pt(int v) {
            this(v, -v);
        }

        int norm() {
            return Math.abs(x) + Math.abs(y);
        }

        static Pt origin() {
            return new Pt(0, 0);
        }

        @Override
        public int compareTo(Pt o) {
            return x != o.x ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
        }
    }

    record Box(Pt a, Pt b, String tag, long n, boolean f) {
    }

    record Gen<T>(T v, int k) {
    }

    record Evil(int v) {
        @Override
        public int v() {
            if (v < 0) throw new IllegalStateException("accessor");
            return v;
        }
    }

    static int records(int s) {
        int r = 50;
        Pt p = new Pt(s & 15, s >> 4 & 15), q = new Pt(s & 15, s >> 4 & 15);
        r = h(r, p.equals(q) ? 1 : 0);
        r = h(r, p.hashCode());
        r = h(r, p.toString().hashCode());
        r = h(r, new Pt(7).norm() + Pt.origin().x());
        Box b = new Box(p, new Pt(-1, 2), "t" + s, s * 3L, (s & 1) == 0);
        r = h(r, b.hashCode());
        r = h(r, b.toString().hashCode());
        r = h(r, b.equals(new Box(q, new Pt(-1, 2), "t" + s, s * 3L, (s & 1) == 0)) ? 1 : 0);
        Gen<String> g = new Gen<>("g", s);
        r = h(r, g.toString().hashCode() + g.hashCode());
        try {
            new Pt(Integer.MIN_VALUE, 0);
        } catch (IllegalArgumentException e) {
            K.caught();
            r = h(r, e.getMessage().hashCode());
        }
        TreeSet<Pt> ts = new TreeSet<>();
        for (int i = 0; i < 6; i++) ts.add(new Pt((i * 5 + s) % 4, i % 3));
        for (Pt t : ts) r = h(r, t.x() * 10 + t.y());
        HashSet<Pt> hs = new HashSet<>(ts);
        r = h(r, hs.contains(new Pt(1, 1)) ? 1 : 0);
        Object o = b;
        if (o instanceof Box(Pt(var x1, var y1), Pt p2, String tag, long n, boolean f) && x1 >= 0) {
            r = h(r, x1 + y1 + p2.y() + tag.length() + (int) n + (f ? 1 : 0));
        }
        return r;
    }

    // ------------------------------------------------------------------ enums

    enum Op {
        ADD("+") {
            int apply(int a, int b) {
                return a + b;
            }
        },
        SUB("-") {
            int apply(int a, int b) {
                return a - b;
            }
        },
        MUL("*") {
            int apply(int a, int b) {
                return a * b;
            }
        },
        DIV("/") {
            int apply(int a, int b) {
                return a / b;
            }
        };

        final String sym;
        static final Map<String, Op> BY_SYM = new HashMap<>();

        static {
            for (Op o : values()) BY_SYM.put(o.sym, o);
        }

        Op(String sym) {
            this.sym = sym;
        }

        abstract int apply(int a, int b);
    }

    enum Color { RED, GREEN, BLUE }

    static int enums(int s) {
        int r = 51;
        for (Op o : Op.values()) {
            try {
                r = h(r, o.apply(s, (s % 3) - 1));
            } catch (ArithmeticException e) {
                K.caught();
                r = h(r, 9);
            }
            r = h(r, o.ordinal() * 100 + o.name().hashCode() + o.sym.hashCode() + o.toString().length());
        }
        r = h(r, Op.valueOf("MUL").apply(3, 4));
        r = h(r, Op.BY_SYM.get("-").apply(10, s));
        try {
            Op.valueOf("MOD");
        } catch (IllegalArgumentException e) {
            K.caught();
            r = h(r, e.getMessage().hashCode());
        }
        Color c = Color.values()[s % 3];
        switch (c) {
            case RED:
                r = h(r, 1);
            case GREEN:
                r = h(r, 2);
                break;
            default:
                r = h(r, 3);
        }
        int v = switch (c) {
            case RED -> 10;
            case GREEN -> {
                int t = s * 2;
                yield t + 1;
            }
            case BLUE -> 30;
        };
        r = h(r, v);
        r = h(r, c.compareTo(Color.GREEN));
        r = h(r, Color.valueOf("BLUE").ordinal());
        EnumMap<Color, Integer> em = new EnumMap<>(Color.class);
        em.put(Color.BLUE, 3);
        em.put(Color.RED, 1);
        em.merge(Color.RED, s, Integer::sum);
        for (Map.Entry<Color, Integer> e : em.entrySet()) r = h(r, e.getKey().ordinal() * 7 + e.getValue());
        HashMap<Color, Integer> hm = new HashMap<>();
        for (Color k : Color.values()) hm.put(k, k.ordinal());
        for (Color k : hm.keySet()) r = h(r, k.ordinal());
        Enum<?> en = c;
        r = h(r, en.getDeclaringClass() == Color.class ? 1 : 0);
        return r;
    }

    // ------------------------------------------------------------------ pattern matching

    sealed interface Shape permits Circle, Square, Rect, Poly {
    }

    record Circle(int r) implements Shape {
    }

    record Square(int a) implements Shape {
    }

    record Rect(int w, int h) implements Shape {
    }

    static final class Poly implements Shape {
        final int n;

        Poly(int n) {
            this.n = n;
        }
    }

    static int area(Shape sh) {
        return switch (sh) {
            case Circle c when c.r() > 10 -> 314 * c.r();
            case Circle c -> 3 * c.r() * c.r();
            case Square(int a) -> a * a;
            case Rect(int w, int hh) when w == hh -> -w;
            case Rect(int w, int hh) -> w * hh;
            case Poly p -> p.n * 11;
        };
    }

    static String kind(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i when i > 100 -> "bigint";
            case Integer i -> "int" + i;
            case String str -> "str" + str.length();
            case int[] arr -> "arr" + arr.length;
            case Shape sh -> "shape" + area(sh);
            case Evil(int v) -> "evil" + v;
            default -> "other";
        };
    }

    static int patternSwitch(int s) {
        int r = 52;
        Shape[] shapes = {new Circle(s % 20), new Square(s % 7), new Rect(3, 3), new Rect(s % 5, 2), new Poly(s % 9)};
        for (Shape sh : shapes) r = h(r, area(sh));
        Object[] os = {null, 5, 500, "hey", new int[s % 4], new Circle(2), 2.5, new Evil(s % 3)};
        for (Object o : os) r = LS.h(r, kind(o));
        try {
            r = LS.h(r, kind(new Evil(-1)));
        } catch (MatchException e) {
            K.caught();
            r = h(r, e.getCause() instanceof IllegalStateException ? 77 : 78);
        }
        Object o = s % 2 == 0 ? (Object) "text" : Integer.valueOf(s);
        if (o instanceof String str && str.length() > 2) r = h(r, str.hashCode());
        if (!(o instanceof Integer n)) r = h(r, 1);
        else r = h(r, n);
        String key = switch (s % 4) {
            case 0 -> "Aa";
            case 1 -> "BB";
            case 2 -> "C";
            default -> "";
        };
        switch (key) {
            case "Aa" -> r = h(r, 100);
            case "BB" -> r = h(r, 200);
            case "C", "D" -> r = h(r, 300);
            default -> r = h(r, 400);
        }
        int old;
        switch (s % 5) {
            case 0:
            case 1:
                old = 1;
                break;
            case 3:
                old = 3;
                break;
            default:
                old = -1;
        }
        r = h(r, old);
        char ch = (char) ('a' + s % 4);
        r = h(r, switch (ch) { case 'a' -> 1; case 'b' -> 2; default -> 9; });
        long big = s % 3;
        r = h(r, (int) (big == 0 ? 1L : big == 1 ? 2L : 3L));
        return r;
    }

    // ------------------------------------------------------------------ interfaces

    interface Greeter {
        String name();

        default String greet() {
            return "hi " + name() + helper();
        }

        private String helper() {
            return "!";
        }

        static Greeter of(String n) {
            return () -> n;
        }
    }

    interface Left {
        default int which() {
            return 1;
        }
    }

    interface Right {
        default int which() {
            return 2;
        }
    }

    static final class Both implements Left, Right {
        @Override
        public int which() {
            return Left.super.which() * 10 + Right.super.which();
        }
    }

    abstract static class Base {
        final int seen;
        static int created;

        Base() {
            created++;
            seen = probe();
        }

        abstract int probe();

        int twice() {
            return probe() * 2;
        }
    }

    static final class Derived extends Base {
        int field = 42;

        @Override
        int probe() {
            return field;
        }
    }

    static int interfaces(int s) {
        int r = 53;
        Greeter g = Greeter.of("n" + s);
        r = LS.h(r, g.greet());
        Greeter anon = new Greeter() {
            @Override
            public String name() {
                return "anon";
            }

            @Override
            public String greet() {
                return Greeter.super.greet() + "?";
            }
        };
        r = LS.h(r, anon.greet());
        r = h(r, new Both().which());
        Derived d = new Derived();
        r = h(r, d.seen * 1000 + d.twice() + Base.created);
        Runnable run = () -> K.note(s);
        run.run();
        Comparable<Pt> cp = new Pt(1, 2);
        r = h(r, cp.compareTo(new Pt(1, 3)));
        Object o = cp;
        r = h(r, o instanceof Comparable ? 1 : 0);
        return r;
    }

    // ------------------------------------------------------------------ inner / local / anonymous classes

    private int outerField = 5;

    final class Inner {
        int get() {
            return outerField * 2;
        }
    }

    static final class Nested {
        static int count;
        final int id;

        Nested() {
            id = ++count;
        }
    }

    static int innerClasses(int s) {
        int r = 54;
        LO outer = new LO();
        outer.outerField = s;
        LO.Inner in = outer.new Inner();
        r = h(r, in.get());
        final int captured = s * 3;
        class Local {
            int calc(int v) {
                return v + captured;
            }
        }
        r = h(r, new Local().calc(7));
        Nested a = new Nested(), b = new Nested();
        r = h(r, b.id - a.id + Nested.count);
        Iterator<Integer> it = new Iterator<>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < 4;
            }

            @Override
            public Integer next() {
                return i++ * captured;
            }
        };
        while (it.hasNext()) r = h(r, it.next());
        Comparator<String> byLen = new Comparator<String>() {
            @Override
            public int compare(String x, String y) {
                return x.length() - y.length();
            }
        };
        List<String> l = new ArrayList<>(Arrays.asList("ccc", "a", "bb"));
        l.sort(byLen);
        r = LS.h(r, l.toString());
        return r;
    }

    // ------------------------------------------------------------------ generics

    static final class Holder<T extends Comparable<T>> {
        T best;

        void offer(T t) {
            if (best == null || t.compareTo(best) > 0) best = t;
        }
    }

    interface Producer<T> {
        T make();
    }

    static final class IntProducer implements Producer<Integer> {
        int n;

        @Override
        public Integer make() {
            return n++;
        }
    }

    static <T> T firstNonNull(T a, T b) {
        return a != null ? a : b;
    }

    static double sum(Collection<? extends Number> c) {
        double t = 0;
        for (Number n : c) t += n.doubleValue();
        return t;
    }

    static int generics(int s) {
        int r = 55;
        Holder<String> hs = new Holder<>();
        for (String x : new String[]{"pear", "zoo", "apple", "z" + s}) hs.offer(x);
        r = LS.h(r, hs.best);
        Holder<Pt> hp = new Holder<>();
        hp.offer(new Pt(1, 1));
        hp.offer(new Pt(s % 3, 9));
        r = h(r, hp.best.y());
        Producer<Integer> p = new IntProducer();
        @SuppressWarnings("rawtypes")
        Producer raw = p;
        r = h(r, p.make() + (Integer) raw.make());
        r = LS.h(r, firstNonNull(null, "fb"));
        r = LA.h(r, sum(Arrays.asList(1, 2.5, 3L, (short) 4)));
        List<? super Integer> sup = new ArrayList<Number>();
        sup.add(s);
        r = h(r, sup.size());
        try {
            @SuppressWarnings("unchecked")
            List<String> bad = (List<String>) (List<?>) new ArrayList<>(Arrays.asList(1, 2));
            String str = bad.get(0);
            r = LS.h(r, str);
        } catch (ClassCastException e) {
            K.caught();
            r = h(r, 3);
        }
        return r;
    }

    // ------------------------------------------------------------------ varargs

    static int va(int... xs) {
        return xs == null ? -1 : xs.length * 100 + (xs.length > 0 ? xs[0] : 0);
    }

    static int vo(Object... xs) {
        return xs == null ? -2 : xs.length;
    }

    static int ov(long x) {
        return 1;
    }

    static int ov(Integer x) {
        return 2;
    }

    static int ov(int... x) {
        return 3;
    }

    static int varargs(int s) {
        int r = 56;
        r = h(r, va());
        r = h(r, va(s));
        r = h(r, va(1, 2, 3));
        r = h(r, va((int[]) null));
        r = h(r, vo());
        r = h(r, vo((Object[]) null));
        r = h(r, vo((Object) null));
        r = h(r, vo(null, null));
        r = h(r, vo(new Object[]{1, 2}));
        r = h(r, vo((Object) new Object[]{1, 2}));
        r = h(r, ov(5) * 10 + ov(Integer.valueOf(5)) + ov() * 100);
        r = LS.h(r, String.format("%s-%s", "a", s));
        r = h(r, Arrays.asList(new int[]{1, 2}).size() + Arrays.asList(1, 2).size());
        return r;
    }

    // ------------------------------------------------------------------ control flow

    static int controlFlow(int s) {
        int r = 57;
        outer:
        for (int i = 0; i < 10; i++) {
            inner:
            for (int j = 0; j < 10; j++) {
                if (j == i) continue outer;
                if (i * j > s % 40) break outer;
                if ((i + j) % 4 == 0) continue inner;
                r = h(r, i * 10 + j);
            }
        }
        int k = 0;
        do {
            k += 3;
        } while (k < s % 50);
        r = h(r, k);
        int w = s;
        while (true) {
            if (w <= 1) break;
            w = (w & 1) == 0 ? w / 2 : 3 * w + 1;
            r = h(r, w);
            if (r == 0) break;
        }
        block:
        {
            if (s % 2 == 0) break block;
            r = h(r, 5);
        }
        boolean a = (s & 1) == 1, b = (s & 2) == 2;
        r = h(r, (a && b ? 1 : 0) + (a || b ? 2 : 0) + (a ^ b ? 4 : 0) + (!a ? 8 : 0) + (a & !b ? 16 : 0));
        int t = s > 100 ? 1 : s > 50 ? 2 : s > 10 ? 3 : 4;
        r = h(r, t);
        return r;
    }

    // ------------------------------------------------------------------ try / catch / finally

    static int finallyReturn(int s) {
        try {
            if (s % 2 == 0) throw new IllegalStateException("x");
            return 1;
        } catch (IllegalStateException e) {
            return 2;
        } finally {
            if (s % 3 == 0) return 3;
        }
    }

    static int finallyThrows(int s) {
        try {
            throw new IllegalArgumentException("first");
        } finally {
            if (s % 2 == 0) throw new UnsupportedOperationException("second");
        }
    }

    static final class Res implements AutoCloseable {
        final int id;
        static int closed;

        Res(int id) {
            this.id = id;
        }

        @Override
        public void close() {
            closed = closed * 10 + id;
            if (id == 3) throw new IllegalStateException("close " + id);
        }
    }

    static int tryFinally(int s) {
        int r = 58;
        int acc = 0;
        for (int i = 0; i < 8; i++) {
            try {
                if (i == 2) continue;
                if (i == 6 && s % 2 == 0) break;
                if (i % 3 == 0) throw new RuntimeException("loop" + i);
                acc += i;
            } catch (RuntimeException e) {
                K.caught();
                acc += 100;
            } finally {
                acc *= 3;
                acc %= 100003;
            }
        }
        r = h(r, acc);
        r = h(r, finallyReturn(s));
        try {
            finallyThrows(s);
        } catch (UnsupportedOperationException e) {
            K.caught();
            r = h(r, 1);
        } catch (IllegalArgumentException e) {
            K.caught();
            r = h(r, 2);
        }
        Res.closed = 0;
        try (Res a = new Res(1); Res b = new Res(3); Res c = new Res(2)) {
            if (s % 2 == 0) throw new IllegalArgumentException("body");
            r = h(r, a.id + b.id + c.id);
        } catch (IllegalArgumentException e) {
            K.caught();
            r = h(r, e.getSuppressed().length * 10 + 1);
        } catch (IllegalStateException e) {
            K.caught();
            r = h(r, e.getSuppressed().length * 10 + 2);
        }
        r = h(r, Res.closed);
        try {
            try {
                throw new IllegalStateException("inner");
            } finally {
                r = h(r, 7);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            K.caught();
            r = LS.h(r, e.getMessage());
        }
        return r;
    }

    // ------------------------------------------------------------------ custom exceptions

    static class FuzzChecked extends Exception {
        final int code;

        FuzzChecked(String m, int code) {
            super(m);
            this.code = code;
        }
    }

    static final class FuzzUnchecked extends RuntimeException {
        FuzzUnchecked(String m, Throwable cause) {
            super(m, cause);
        }
    }

    static final class FuzzError extends Error {
        FuzzError(String m) {
            super(m);
        }
    }

    static void deep(int d, int s) throws FuzzChecked {
        if (d == 0) throw new FuzzChecked("deep" + s, s % 7);
        deep(d - 1, s);
    }

    static int customExceptions(int s) {
        int r = 59;
        try {
            deep(10 + s % 20, s);
        } catch (FuzzChecked e) {
            K.caught();
            r = h(r, e.code);
            r = LS.h(r, e.getMessage());
            r = LS.h(r, e.toString());
        }
        try {
            try {
                deep(3, s);
            } catch (FuzzChecked e) {
                throw new FuzzUnchecked("wrap", e);
            }
        } catch (FuzzUnchecked e) {
            K.caught();
            r = h(r, e.getCause() instanceof FuzzChecked fc ? fc.code + 1 : 0);
            r = LS.h(r, e.toString() + e.getCause().getMessage());
        }
        try {
            throw new FuzzError("err" + s);
        } catch (Error e) {
            K.caught();
            r = LS.h(r, e.getMessage());
        }
        try {
            throw new IllegalStateException(new FuzzChecked("c", 1));
        } catch (RuntimeException e) {
            K.caught();
            r = LS.h(r, e.getMessage());
        }
        Throwable t = new Throwable("plain");
        t.addSuppressed(new RuntimeException("sup"));
        r = h(r, t.getSuppressed().length);
        try {
            throw t;
        } catch (Throwable x) {
            K.caught();
            r = LS.h(r, x.getLocalizedMessage());
        }
        Exception ex = new Exception();
        r = h(r, ex.getMessage() == null ? 1 : 0);
        r = h(r, new RuntimeException((String) null).getMessage() == null ? 1 : 0);
        return r;
    }
}
