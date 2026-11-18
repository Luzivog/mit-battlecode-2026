package fuzz_lang;

import battlecode.common.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// Boxing, identity hashes and collections (instrumented java.util).
final class LC {
    static final class Plain {
        final int v;

        Plain(int v) {
            this.v = v;
        }
    }

    static final class Key implements Comparable<Key> {
        final int a;
        final String b;

        Key(int a, String b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && k.a == a && k.b.equals(b);
        }

        @Override
        public int hashCode() {
            return a * 31 + b.hashCode();
        }

        @Override
        public int compareTo(Key o) {
            return a != o.a ? Integer.compare(a, o.a) : b.compareTo(o.b);
        }
    }

    static int h(int a, int b) {
        return LA.h(a, b);
    }

    static int boxCache(int s) {
        int r = 30;
        int[] vs = {127, 128, -128, -129, 0, s & 255, 1000};
        for (int v : vs) {
            Integer a = v, b = v;
            Long la = (long) v, lb = (long) v;
            Short sa = (short) v, sb = (short) v;
            Byte ba = (byte) v, bb = (byte) v;
            Character ca = (char) (v & 0xffff), cb = (char) (v & 0xffff);
            r = h(r, (a == b ? 1 : 0) + (la == lb ? 2 : 0) + (sa == sb ? 4 : 0) + (ba == bb ? 8 : 0)
                    + (ca == cb ? 16 : 0) + (a.equals(b) ? 32 : 0));
        }
        Boolean t1 = true, t2 = Boolean.valueOf("true");
        r = h(r, t1 == t2 ? 1 : 0);
        Integer x = 1000, y = 1000;
        r = h(r, x.equals(y) ? 1 : 0);
        r = h(r, x == y ? 1 : 0);
        r = h(r, x <= y && x >= y ? 1 : 0);
        Integer z = Integer.valueOf(s & 127);
        r = h(r, z == Integer.valueOf(s & 127) ? 1 : 0);
        Double d1 = 0.0, d2 = -0.0;
        r = h(r, d1.equals(d2) ? 1 : 0);
        r = h(r, d1 == 0.0 ? 1 : 0);
        Integer nul = (s & 1) == 2 ? 1 : null;
        try {
            int u = nul;
            r = h(r, u);
        } catch (NullPointerException e) {
            K.caught();
            r = h(r, 3);
        }
        try {
            boolean flag = (s & 2) == 0;
            Integer pick = flag ? nul : Integer.valueOf(4);
            int w = flag ? pick : 0;
            r = h(r, w);
        } catch (NullPointerException e) {
            K.caught();
            r = h(r, 5);
        }
        List<Integer> li = new ArrayList<>();
        li.add(1);
        li.add(2);
        li.add(3);
        li.remove(1);
        li.remove(Integer.valueOf(1));
        r = h(r, li.size() * 10 + li.get(0));
        long sum = 0;
        Long boxedSum = 0L;
        for (int i = 0; i < 10; i++) {
            sum += i;
            boxedSum += i;
        }
        r = h(r, (int) (sum + boxedSum));
        return r;
    }

    static int identityHash(int s) {
        int r = 31;
        Object a = new Object(), b = new Plain(s), c = a;
        r = h(r, a.hashCode());
        r = h(r, b.hashCode());
        r = h(r, c.hashCode() == a.hashCode() ? 1 : 0);
        r = h(r, System.identityHashCode(b));
        r = h(r, Objects.hashCode(b));
        r = h(r, Objects.hash(1, "x", b, null));
        r = h(r, a.toString().hashCode());
        r = h(r, b.toString().length());
        r = h(r, Direction.WEST.hashCode());
        r = h(r, Direction.WEST.hashCode() == Direction.WEST.hashCode() ? 1 : 0);
        Character ch = 'q';
        r = h(r, ch.hashCode());
        r = h(r, Character.valueOf((char) 300).hashCode());
        r = h(r, "str".hashCode() + Integer.valueOf(s).hashCode() + Long.valueOf(s).hashCode()
                + Double.valueOf(s).hashCode() + Boolean.TRUE.hashCode());
        r = h(r, Arrays.hashCode(new int[]{1, 2, s}) + Arrays.hashCode(new Object[]{"a", b}));
        r = h(r, new LS.P(s, "z").hashCode());
        r = h(r, List.class.hashCode() == List.class.hashCode() ? 1 : 0);
        r = h(r, new ArrayList<>(Arrays.asList(1, 2)).hashCode());
        r = h(r, a.equals(c) ? 1 : 0);
        r = h(r, Objects.equals(a, new Object()) ? 1 : 0);
        return r;
    }

    static int hashMapPlain(int s) {
        int r = 32;
        HashMap<Object, Integer> m = new HashMap<>();
        Plain[] ps = new Plain[20 + (s & 7)];
        for (int i = 0; i < ps.length; i++) {
            ps[i] = new Plain(i);
            m.put(ps[i], i * i);
        }
        m.put(Direction.values()[s & 7], -1);
        m.put(Character.valueOf('k'), -2);
        int k = 0;
        for (Map.Entry<Object, Integer> e : m.entrySet()) r = h(r, e.getValue() * 31 + k++);
        m.remove(ps[3]);
        r = h(r, m.containsKey(ps[3]) ? 1 : 0);
        r = h(r, m.get(ps[5]));
        r = h(r, m.getOrDefault(new Plain(5), 77));
        HashSet<Object> hs = new HashSet<>();
        for (int i = 0; i < 12; i++) hs.add(i % 3 == 0 ? new Plain(i) : (Object) Integer.valueOf(i * 37));
        for (Object o : hs) r = h(r, o instanceof Plain p ? p.v : (Integer) o);
        Iterator<Object> it = hs.iterator();
        it.next();
        it.remove();
        r = h(r, hs.size());
        return r;
    }

    static int hashMapString(int s) {
        int r = 33;
        HashMap<String, Integer> m = new HashMap<>();
        for (int i = 0; i < 40; i++) m.put("k" + (i * 7 + s) % 50, i);
        for (String key : m.keySet()) r = h(r, key.hashCode());
        m.merge("k1", 5, Integer::sum);
        m.computeIfAbsent("new", x -> x.length());
        m.computeIfPresent("k2", (x, v) -> v + 100);
        m.putIfAbsent("k3", 9);
        m.compute("k4", (x, v) -> v == null ? 1 : v * 2);
        m.replaceAll((x, v) -> v + 1);
        m.keySet().removeIf(x -> x.endsWith("7"));
        m.values().removeIf(v -> v > 30);
        for (Map.Entry<String, Integer> e : m.entrySet()) r = h(r, e.getKey().hashCode() ^ e.getValue());
        HashMap<Integer, String> hm = new HashMap<>(4, 0.5f);
        for (int i = 0; i < 30; i++) hm.put(i * 64 + (s & 63), "v" + i);
        for (int key : hm.keySet()) r = h(r, key);
        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<>(16, 0.75f, true);
        lhm.put("a", 1);
        lhm.put("b", 2);
        lhm.put("c", 3);
        lhm.get("a");
        r = h(r, lhm.toString().hashCode());
        TreeMap<Key, Integer> tm = new TreeMap<>();
        tm.put(new Key(2, "b"), 1);
        tm.put(new Key(1, "z"), 2);
        tm.put(new Key(2, "a"), 3);
        for (Key key : tm.keySet()) r = h(r, key.a * 100 + key.b.charAt(0));
        HashMap<Key, Integer> km = new HashMap<>();
        km.put(new Key(s, "x"), 1);
        r = h(r, km.get(new Key(s, "x")));
        return r;
    }

    static int lists(int s) {
        int r = 34;
        ArrayList<Integer> a = new ArrayList<>();
        for (int i = 0; i < 25; i++) a.add((i * 37 + s) % 50);
        a.add(0, -1);
        a.add(5, -5);
        a.set(2, 99);
        a.remove(3);
        a.remove(Integer.valueOf(99));
        r = h(r, a.indexOf(-5) + a.lastIndexOf(-1) + (a.contains(42) ? 100 : 0));
        List<Integer> sub = a.subList(2, 8);
        sub.set(0, 1234);
        r = h(r, a.get(2));
        a.sort(null);
        r = h(r, a.toString().hashCode());
        a.sort(Comparator.reverseOrder());
        a.removeIf(x -> x % 3 == 0);
        a.replaceAll(x -> x * 2);
        final int[] acc = {0};
        a.forEach(x -> acc[0] += x);
        r = h(r, acc[0]);
        Integer[] arr = a.toArray(new Integer[0]);
        Object[] oa = a.toArray();
        r = h(r, arr.length + oa.length);
        ListIterator<Integer> li = a.listIterator();
        while (li.hasNext()) {
            int v = li.next();
            if (v % 4 == 0) li.set(v + 1);
            else if (v % 5 == 0) li.add(-v);
        }
        r = h(r, a.hashCode());
        r = h(r, a.equals(new ArrayList<>(a)) ? 1 : 0);
        ArrayList<Integer> b = new ArrayList<>(a);
        b.retainAll(Arrays.asList(2, 4, 6, 8));
        b.addAll(Arrays.asList(7, 7, 7));
        b.addAll(1, a.subList(0, 3));
        r = h(r, b.size());
        a.clear();
        a.ensureCapacity(100);
        a.trimToSize();
        r = h(r, a.isEmpty() ? 1 : 0);
        List<int[]> la = new ArrayList<>();
        la.add(new int[]{s});
        r = h(r, la.get(0)[0]);
        return r;
    }

    static int linkedDeque(int s) {
        int r = 35;
        LinkedList<Integer> l = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            if ((i + s) % 3 == 0) l.addFirst(i);
            else l.addLast(i);
        }
        l.push(-1);
        r = h(r, l.pop() + l.peek() + l.getFirst() + l.getLast() + l.removeFirst() + l.pollLast());
        l.add(3, 42);
        l.remove(Integer.valueOf(42));
        Iterator<Integer> d = l.descendingIterator();
        while (d.hasNext()) r = h(r, d.next());
        r = h(r, l.indexOf(5));
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < 20; i++) {
            if (i % 3 == 0) q.offerFirst(i);
            else q.offerLast(i);
            if (i % 5 == 4) q.pollFirst();
        }
        q.push(100);
        r = h(r, q.peekFirst() + q.peekLast() + q.pop() + q.removeLast() + q.size());
        for (int x : q) r = h(r, x);
        try {
            new ArrayDeque<Integer>().pop();
        } catch (NoSuchElementException e) {
            K.caught();
            r = h(r, 7);
        }
        try {
            new LinkedList<Integer>().getFirst();
        } catch (NoSuchElementException e) {
            K.caught();
            r = h(r, 8);
        }
        try {
            q.offer(null);
        } catch (NullPointerException e) {
            K.caught();
            r = h(r, 9);
        }
        return r;
    }

    static int priorityQueue(int s) {
        int r = 36;
        PriorityQueue<Integer> p = new PriorityQueue<>();
        for (int i = 0; i < 30; i++) p.add((i * 7919 + s) % 101);
        for (int x : p) r = h(r, x);
        for (int i = 0; i < 10; i++) r = h(r, p.poll());
        PriorityQueue<int[]> q = new PriorityQueue<>((x, y) -> x[0] != y[0] ? y[0] - x[0] : x[1] - y[1]);
        for (int i = 0; i < 15; i++) q.offer(new int[]{(i * 13 + s) % 5, i});
        q.remove(q.peek());
        while (!q.isEmpty()) r = h(r, q.poll()[1]);
        PriorityQueue<String> ps = new PriorityQueue<>(Comparator.comparing(String::length).thenComparing(Comparator.reverseOrder()));
        ps.addAll(Arrays.asList("ccc", "a", "bb", "b", "aaa"));
        while (!ps.isEmpty()) r = h(r, ps.poll().hashCode());
        return r;
    }

    static int treeMap(int s) {
        int r = 37;
        TreeMap<Integer, String> t = new TreeMap<>();
        for (int i = 0; i < 20; i++) t.put((i * 31 + s) % 47, "v" + i);
        r = h(r, t.firstKey() + t.lastKey());
        Integer fk = t.floorKey(20), ck = t.ceilingKey(20), hk = t.higherKey(46), lk = t.lowerKey(0);
        r = h(r, (fk == null ? -1 : fk) + (ck == null ? -1 : ck) * 3 + (hk == null ? -7 : hk) + (lk == null ? -9 : lk));
        r = h(r, t.headMap(15).size() + t.tailMap(30, false).size() * 100 + t.subMap(5, 25).size() * 10000);
        Map.Entry<Integer, String> e = t.pollFirstEntry();
        r = h(r, e.getKey());
        for (Integer k : t.descendingKeySet()) r = h(r, k);
        TreeSet<String> ts = new TreeSet<>(Comparator.reverseOrder());
        ts.addAll(Arrays.asList("m", "a", "z", "q"));
        r = h(r, ts.first().hashCode() + ts.headSet("m").size() + ts.pollLast().hashCode());
        try {
            t.put(null, "x");
        } catch (NullPointerException ex) {
            K.caught();
            r = h(r, 11);
        }
        try {
            TreeSet<Object> bad = new TreeSet<>();
            bad.add(new Plain(1));
            bad.add(new Plain(2));
        } catch (ClassCastException ex) {
            K.caught();
            r = h(r, 12);
        }
        return r;
    }

    static int sorting(int s) {
        int r = 38;
        List<String> l = new ArrayList<>(Arrays.asList("pear", "fig", "apple", "kiwi", "date", "banana", "" + s));
        Collections.sort(l);
        r = h(r, l.hashCode());
        Collections.sort(l, Comparator.comparing(String::length).thenComparing(x -> x.charAt(x.length() - 1)));
        r = h(r, l.hashCode());
        l.sort((a, b) -> b.compareTo(a));
        r = h(r, l.hashCode());
        Collections.reverse(l);
        Collections.swap(l, 0, 2);
        Collections.rotate(l, 3);
        Collections.shuffle(l, new Random(s));
        r = h(r, l.hashCode());
        r = h(r, Collections.max(l).hashCode() + Collections.min(l, Comparator.comparing(String::length)).hashCode());
        r = h(r, Collections.frequency(Arrays.asList(1, 2, 1, 1), 1) + Collections.nCopies(3, "x").size());
        int[] a = new int[60];
        for (int i = 0; i < a.length; i++) a[i] = (i * 7919 + s) % 97 - 40;
        Arrays.sort(a);
        r = h(r, Arrays.hashCode(a));
        Integer[] b = new Integer[50];
        for (int i = 0; i < b.length; i++) b[i] = (i * 31 + s) % 11;
        Arrays.sort(b, (x, y) -> y - x);
        r = h(r, Arrays.hashCode(b));
        long[] c = {5, -1, s, 3};
        Arrays.sort(c, 1, 4);
        r = h(r, Arrays.hashCode(c));
        double[] d = {Double.NaN, -0.0, 0.0, -1, 1e300};
        Arrays.sort(d);
        r = h(r, Arrays.hashCode(d));
        Key[] ks = {new Key(3, "a"), new Key(1, "b"), new Key(3, "0")};
        Arrays.sort(ks);
        r = h(r, ks[0].a + ks[1].b.hashCode());
        r = h(r, Arrays.binarySearch(a, a[s & 31]));
        return r;
    }

    static int arraysUtil(int s) {
        int r = 39;
        int[] a = new int[10];
        Arrays.fill(a, s);
        Arrays.fill(a, 2, 5, -1);
        int[] b = Arrays.copyOf(a, 15);
        int[] c = Arrays.copyOfRange(a, 3, 12);
        Object[] o = Arrays.copyOf(new Object[]{"x", 1}, 3);
        r = h(r, Arrays.toString(a).hashCode() + Arrays.toString(b).length() + Arrays.toString(c).hashCode());
        r = h(r, Arrays.deepToString(new int[][]{{1, 2}, {3}}).hashCode());
        r = h(r, Arrays.toString(new String[]{"q", null}).hashCode() + (o[2] == null ? 1 : 0));
        r = h(r, Arrays.equals(a, a.clone()) ? 1 : 0);
        r = h(r, Arrays.deepEquals(new int[][]{{1}}, new int[][]{{1}}) ? 1 : 0);
        List<Integer> l = Arrays.asList(3, 1, 2);
        Collections.sort(l);
        r = h(r, l.toString().hashCode());
        try {
            l.add(4);
        } catch (UnsupportedOperationException e) {
            K.caught();
            r = h(r, 1);
        }
        try {
            Arrays.copyOf(a, -1);
        } catch (NegativeArraySizeException e) {
            K.caught();
            r = h(r, 2);
        }
        char[] ch = "hello".toCharArray();
        Arrays.sort(ch);
        r = h(r, new String(ch).hashCode());
        boolean[] bo = new boolean[3];
        Arrays.fill(bo, true);
        r = h(r, Arrays.toString(bo).hashCode());
        return r;
    }

    static int streams(int s) {
        int r = 40;
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < 12; i++) l.add((i * 5 + s) % 17);
        r = h(r, (int) l.stream().filter(x -> x > 5).count());
        r = h(r, l.stream().map(x -> x * 2).toList().hashCode());
        r = h(r, l.stream().sorted().distinct().toList().hashCode());
        r = h(r, l.stream().reduce(0, (x, y) -> x * 31 + y));
        r = h(r, l.stream().anyMatch(x -> x == 3) ? 1 : 0);
        r = h(r, l.stream().allMatch(x -> x >= 0) ? 1 : 0);
        Optional<Integer> mx = l.stream().max(Comparator.naturalOrder());
        r = h(r, mx.orElse(-1));
        r = h(r, Stream.of("b", "a").sorted().toList().hashCode());
        r = h(r, l.stream().limit(3).skip(1).findFirst().orElse(0));
        r = h(r, Stream.iterate(1, x -> x * 3).limit(6).toList().hashCode());
        return r;
    }

    // Failures of the instrumented JDK (deterministic): ImmutableCollections needs System.nanoTime, which the
    // injected System lacks; EnumSet is sealed over the uninstrumented names; IntPipeline and Collectors.joining
    // hold method references the instrumenter refuses; String.chars() returns the uninstrumented IntStream.
    static int jdkQuirks(int s) {
        int r = 41;
        int k = s % 10;
        try {
            switch (k) {
                case 0: r = h(r, List.of(1, 2, s).size()); break;
                case 1: r = h(r, Set.of("a").size()); break;
                case 2: r = h(r, EnumSet.of(Direction.NORTH, Direction.EAST).size()); break;
                case 3: r = h(r, IntStream.range(0, 5).sum()); break;
                case 4: r = h(r, "abc".chars().sum()); break;
                case 5: r = h(r, Stream.of("a", "b").collect(Collectors.joining(",")).hashCode()); break;
                case 6: r = h(r, new ArrayList<Integer>().get(s & 3)); break;
                case 7: {
                    // the field's type is java.util.Comparator, the rewritten reference wants the instrumented one
                    String[] st = {"b", "B", "a", "A"};
                    Arrays.sort(st, String.CASE_INSENSITIVE_ORDER);
                    r = h(r, Arrays.hashCode(st));
                    break;
                }
                case 8: r = h(r, new java.util.concurrent.atomic.AtomicInteger(s).getAndAdd(5)); break;
                default: r = h(r, List.copyOf(new ArrayList<>(Arrays.asList(1, 2))).size());
            }
        } catch (NoSuchMethodError e) {
            K.caught();
            r = h(r, 1);
        } catch (NoClassDefFoundError e) {
            K.caught();
            r = h(r, 2);
        } catch (IncompatibleClassChangeError e) {
            K.caught();
            r = h(r, 3);
        } catch (IndexOutOfBoundsException e) {
            K.caught();
            r = h(r, 4);
        } catch (LinkageError e) {
            K.caught();
            r = h(r, 5);
        } catch (RuntimeException e) {
            K.caught();
            r = h(r, 6);
        }
        EnumMap<Direction, Integer> em = new EnumMap<>(Direction.class);
        em.put(Direction.SOUTH, s);
        em.put(Direction.NORTH, 1);
        for (Map.Entry<Direction, Integer> e : em.entrySet()) r = h(r, e.getKey().ordinal() * 1000 + e.getValue());
        return r;
    }

    static int iterErrors(int s) {
        int r = 42;
        List<Integer> l = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        try {
            for (Integer x : l) if (x == 2) l.remove(x);
        } catch (ConcurrentModificationException e) {
            K.caught();
            r = h(r, 1);
        }
        HashMap<Integer, Integer> m = new HashMap<>();
        m.put(1, 1);
        m.put(2, 2);
        try {
            for (Integer k : m.keySet()) m.put(k + 10, 0);
        } catch (ConcurrentModificationException e) {
            K.caught();
            r = h(r, 2);
        }
        Iterator<Integer> it = l.iterator();
        try {
            it.remove();
        } catch (IllegalStateException e) {
            K.caught();
            r = h(r, 3);
        }
        try {
            Iterator<Integer> e2 = new ArrayList<Integer>().iterator();
            e2.next();
        } catch (NoSuchElementException e) {
            K.caught();
            r = h(r, 4);
        }
        try {
            Collections.unmodifiableList(l).add(5);
        } catch (UnsupportedOperationException e) {
            K.caught();
            r = h(r, 5);
        }
        try {
            Optional.empty().get();
        } catch (NoSuchElementException e) {
            K.caught();
            r = h(r, e.getMessage().hashCode());
        }
        try {
            Objects.requireNonNull(null, "custom message " + s);
        } catch (NullPointerException e) {
            K.caught();
            r = h(r, e.getMessage().hashCode());
        }
        try {
            new ArrayList<Integer>(-1);
        } catch (IllegalArgumentException e) {
            K.caught();
            r = h(r, e.getMessage().hashCode());
        }
        return r;
    }

    static int misc(int s) {
        int r = 43;
        Optional<String> o = Optional.of("v" + s);
        r = h(r, o.map(String::length).orElse(0) + Optional.ofNullable((String) null).map(x -> 1).orElse(5));
        r = h(r, o.filter(x -> x.length() > 3).isPresent() ? 1 : 0);
        o.ifPresent(x -> K.note(x.length()));
        r = h(r, Objects.requireNonNullElse(null, "dflt").hashCode());
        r = h(r, Objects.equals(null, null) ? 1 : 0);
        Object a = "x", b = Integer.valueOf(1);
        r = h(r, a.getClass() == b.getClass() ? 1 : 0);
        r = h(r, a.getClass() == "y".getClass() ? 1 : 0);
        r = h(r, System.getProperty("java.version").hashCode() + System.lineSeparator().hashCode());
        r = h(r, System.getProperty("bc.testing.nothing", "none").hashCode());
        var v = new ArrayList<Map<String, List<Integer>>>();
        v.add(new HashMap<>());
        v.get(0).computeIfAbsent("k", x -> new ArrayList<>()).add(s);
        r = h(r, v.get(0).get("k").get(0));
        return r;
    }

    static int atomics(int s) {
        int r = 44;
        java.util.concurrent.atomic.AtomicInteger ai = new java.util.concurrent.atomic.AtomicInteger(s);
        r = h(r, ai.incrementAndGet() + ai.decrementAndGet() + ai.get());
        r = h(r, ai.compareAndSet(s, 0) ? 1 : 0);
        java.util.concurrent.atomic.AtomicLong al = new java.util.concurrent.atomic.AtomicLong(s);
        r = h(r, (int) (al.incrementAndGet() + al.getAndIncrement()));
        java.util.concurrent.ConcurrentHashMap<Integer, Integer> m = new java.util.concurrent.ConcurrentHashMap<>();
        for (int i = 0; i < 10; i++) m.put(i * 3, i);
        m.putIfAbsent(3, 99);
        m.putIfAbsent(100, s);
        r = h(r, m.size() + m.get(3) + m.get(100) + (m.containsKey(27) ? 1 : 0));
        return r;
    }

    static int bitset(int s) {
        int r = 45;
        BitSet b = new BitSet();
        for (int i = 0; i < 20; i++) b.set((i * 37 + s) % 200);
        r = h(r, b.cardinality() + b.nextSetBit(50) + b.nextClearBit(0) + b.length());
        BitSet c = (BitSet) b.clone();
        c.flip(0, 64);
        c.and(b);
        r = h(r, c.toString().hashCode() + b.toString().hashCode());
        return r;
    }
}
