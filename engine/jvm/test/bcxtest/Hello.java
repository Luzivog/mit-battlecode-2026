package bcxtest;

import java.util.*;

public class Hello {
    static void print(String s) { Out.print(s); }

    interface Shape { double area(); default String kind() { return "shape"; } }
    record Rect(int w, int h) implements Shape { public double area() { return w * h; } }
    static final class Circle implements Shape {
        final double r; Circle(double r) { this.r = r; }
        public double area() { return Math.PI * r * r; }
        public String kind() { return "circle"; }
    }
    enum Color { RED, GREEN, BLUE }
    static int fib(int n) { return n < 2 ? n : fib(n - 1) + fib(n - 2); }

    public static void main(String[] args) {
        print("hello " + 42 + " " + 1.5 + " " + 'c' + " " + true + " " + 123456789012L + " " + 0.1f);
        int[] a = new int[10];
        for (int i = 0; i < a.length; i++) a[i] = i * i;
        long s = 0;
        for (int v : a) s += v;
        print("sum " + s + " fib " + fib(20));
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Rect(3, 4));
        shapes.add(new Circle(2));
        for (Shape sh : shapes) print(sh.kind() + " " + sh.area());
        print(new Rect(1, 2).toString() + " " + new Rect(1, 2).equals(new Rect(1, 2)) + " " + new Rect(5, 6).hashCode());
        Map<String, Integer> m = new HashMap<>();
        for (Color c : Color.values()) m.put(c.name(), c.ordinal());
        print("map " + m + " " + Color.valueOf("GREEN"));
        StringBuilder sb = new StringBuilder("abc");
        sb.insert(1, 'X').deleteCharAt(0).append(7);
        print(sb.toString() + " " + sb.indexOf("c") + " " + Integer.toBinaryString(10) + " " + Long.reverse(1L));
        try { int[] z = new int[2]; z[5] = 1; } catch (ArrayIndexOutOfBoundsException e) { print("caught " + e.getMessage()); }
        try { Object o = "x"; Integer i = (Integer) o; } catch (ClassCastException e) { print("cce"); }
        try { int z = 0; print("" + (5 / z)); } catch (ArithmeticException e) { print("arith " + e.getMessage()); }
        Random r = new Random(42);
        print("rand " + r.nextInt(100) + " " + r.nextDouble() + " " + Math.pow(0.99, 37) + " " + Math.sqrt(2));
        Runnable run = () -> print("lambda " + shapes.size());
        run.run();
        Object o = shapes.get(0);
        String d = switch (o) { case Rect rr -> "rect " + rr.w(); case Circle c -> "circle"; default -> "other"; };
        print(d + " " + String.valueOf((Object) null) + " " + Double.toString(1e-7) + " " + (float) 1 / 3);
        int[][] g = new int[3][4];
        g[2][3] = 9;
        print("grid " + Arrays.toString(g[2]) + " " + Arrays.deepToString(new int[][]{{1}, {2, 3}}));
        List<Integer> li = new ArrayList<>(List.of(5, 3, 9, 1));
        Collections.sort(li);
        li.sort(Comparator.reverseOrder());
        print("sorted " + li);
    }
}
