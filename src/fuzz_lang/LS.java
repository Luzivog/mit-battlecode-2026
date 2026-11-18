package fuzz_lang;

import battlecode.common.*;

// Strings, StringBuilder, formatting, parsing, regex.
final class LS {
    static String last = "";

    static final class Named {
        final int v;

        Named(int v) {
            this.v = v;
        }

        @Override
        public String toString() {
            return "N" + v;
        }
    }

    static int h(int a, String s) {
        last = s;
        return LA.h(a, s == null ? -1 : s.hashCode() ^ s.length());
    }

    static int concat(int s) {
        int r = 20;
        long l = s * 7919L;
        float f = s / 3f;
        double d = s / 7.0;
        char c = (char) ('a' + (s & 15));
        boolean b = (s & 1) == 0;
        String nul = null;
        Object o = new Named(s);
        Integer boxed = s;
        MapLocation ml = new MapLocation(s & 31, s >> 5 & 31);
        r = h(r, "i" + s + "l" + l + "f" + f + "d" + d + "c" + c + "b" + b + "n" + nul + "o" + o);
        r = h(r, s + c + "x" + (s + c) + 'q' + 1.5f + 2.5 + boxed + ml + Direction.NORTH + Team.B + UnitType.CAT);
        r = h(r, "" + Double.NaN + Float.NEGATIVE_INFINITY + (-0.0) + 1e-7 + 1e21 + 123456789.0f + Double.MIN_VALUE);
        r = h(r, "" + Long.MIN_VALUE + Integer.MIN_VALUE + (char) 0x4e2d + (byte) -1 + (short) 40000);
        String acc = "";
        for (int i = 0; i < 6; i++) acc += i + ",";
        r = h(r, acc);
        String t = "a";
        t += 1 + 2;
        t += 'c';
        t = 1 + 2 + t + 1 + 2;
        r = h(r, t);
        r = h(r, String.valueOf(c) + String.valueOf(s) + String.valueOf(d) + String.valueOf(b) + String.valueOf(ml)
                + String.valueOf(new char[]{'x', 'y'}) + String.valueOf((Object) null));
        r = h(r, "" + new P(s, "p" + s) + new Q(d, c));
        return r;
    }

    record P(int x, String s) {
    }

    record Q(double d, char c) {
    }

    static int doubleToString(int s) {
        int r = 21;
        double[] ds = {0.1, 0.2 + 0.1, 100.0, 1e7, 1.0e-3, 9.999999e-4, 1e21, 1e-5, 123456789012.0, Double.MAX_VALUE,
                Double.MIN_VALUE, Double.MIN_NORMAL, -0.0, Double.NaN, Double.NEGATIVE_INFINITY, 2.0 / 3,
                Double.longBitsToDouble(0x9E3779B97F4A7C15L * s), s * 1.1, 4.35, 5e-324, 1.7976931348623157e308};
        for (double d : ds) {
            r = h(r, Double.toString(d));
            r = h(r, Float.toString((float) d));
            r = h(r, String.valueOf(d));
        }
        r = h(r, Double.toHexString(s * 0.3));
        r = h(r, Float.toString(Float.intBitsToFloat(0x3f800000 + s)));
        return r;
    }

    static int parse(int s) {
        int r = 22;
        String[] in = {"123", "-0", "+7", "2147483647", "2147483648", "-2147483648", "abc", "", " 5", "0x10", "1_0",
                "١٢", Integer.toString(s)};
        for (String x : in) {
            try {
                r = LA.h(r, Integer.parseInt(x));
            } catch (NumberFormatException e) {
                K.caught();
                r = h(r, e.getMessage());
            }
        }
        String[] dn = {"1e3", "-0.0", "NaN", "Infinity", "0x1p3", "1.5f", "  2.5  ", "1e400", "1,5", ".5", "5."};
        for (String x : dn) {
            try {
                r = LA.h(r, Double.parseDouble(x));
            } catch (NumberFormatException e) {
                K.caught();
                r = h(r, e.getMessage());
            }
        }
        try {
            r = LA.h(r, Long.parseLong("99999999999999999999"));
        } catch (NumberFormatException e) {
            K.caught();
            r = h(r, e.getMessage());
        }
        try {
            r = LA.h(r, Integer.parseInt(null));
        } catch (NumberFormatException e) {
            K.caught();
            r = h(r, e.getMessage());
        }
        r = LA.h(r, Boolean.parseBoolean("TRUE") ? 1 : 0);
        r = LA.h(r, Integer.valueOf("-12") + Long.valueOf("34") + Double.valueOf("5.5").intValue());
        r = LA.h(r, Float.parseFloat("3.4028236e38"));
        return r;
    }

    static int stringBuilder(int s) {
        int r = 23;
        StringBuilder b = new StringBuilder();
        b.append(s).append('c').append("str").append(1.5).append(2.5f).append(true).append(9L).append((Object) null)
                .append((String) null).append(new char[]{'x', 'y'}).append(new Named(s)).append(new MapLocation(1, 2));
        r = h(r, b.toString());
        b.insert(0, 'Z').insert(3, "ins").insert(b.length(), 42).insert(1, 1.25);
        r = h(r, b.toString());
        r = LA.h(r, b.indexOf("str") + b.lastIndexOf("s") + b.indexOf("nope") + b.indexOf("s", 5));
        b.deleteCharAt(0).delete(2, 5).delete(b.length() - 2, b.length() + 10);
        r = h(r, b.toString());
        b.setCharAt(1, '#');
        b.replace(0, 3, "REPL").replace(2, 2, "--");
        r = h(r, b.toString());
        b.reverse();
        r = h(r, b.toString());
        r = LA.h(r, b.charAt(2) + b.length());
        r = h(r, b.substring(3) + b.substring(1, 4) + b.subSequence(0, 2));
        b.repeat('*', 3).repeat("ab", 2).repeat(0x1F600, 2);
        r = h(r, b.toString());
        int n = b.length();
        b.setLength(n + 3);
        r = LA.h(r, b.charAt(n + 1));
        b.setLength(4);
        r = h(r, b.toString());
        StringBuilder c = new StringBuilder("init").append(new StringBuilder("cs"));
        r = h(r, c.toString() + new StringBuilder((CharSequence) "cq").length());
        r = LA.h(r, c.compareTo(new StringBuilder("inix")));
        try {
            b.charAt(100);
        } catch (IndexOutOfBoundsException e) {
            K.caught();
            r = LA.h(r, 31);
        }
        try {
            b.deleteCharAt(-1);
        } catch (StringIndexOutOfBoundsException e) {
            K.caught();
            r = LA.h(r, 32);
        }
        try {
            b.insert(99, "x");
        } catch (StringIndexOutOfBoundsException e) {
            K.caught();
            r = LA.h(r, 33);
        }
        try {
            b.setLength(-1);
        } catch (StringIndexOutOfBoundsException e) {
            K.caught();
            r = LA.h(r, 34);
        }
        StringBuffer sb = new StringBuffer("buf");
        sb.append(s).insert(0, '<').reverse().deleteCharAt(0);
        r = LA.h(r, sb.indexOf("f") + sb.length());
        r = h(r, sb.toString());
        return r;
    }

    static int stringMethods(int s) {
        int r = 24;
        String a = "Hello, World " + s, e = "", w = "  \t pad \n ";
        r = LA.h(r, a.length() + a.charAt(4) + a.indexOf('o') + a.indexOf("o", 5) + a.lastIndexOf('o')
                + a.lastIndexOf("l", 3));
        r = h(r, a.substring(3) + a.substring(2, 5) + a.toUpperCase() + a.toLowerCase());
        r = LA.h(r, (a.contains("World") ? 1 : 0) + (a.startsWith("He") ? 2 : 0) + (a.endsWith("x") ? 4 : 0)
                + (a.equals("x") ? 8 : 0) + (a.equalsIgnoreCase("HELLO, WORLD " + s) ? 16 : 0)
                + (e.isEmpty() ? 32 : 0) + (w.isBlank() ? 64 : 0) + (a.startsWith("lo", 3) ? 128 : 0));
        r = LA.h(r, a.compareTo("Hello") + a.compareToIgnoreCase("hello, world") + "b".compareTo("a"));
        r = h(r, w.trim() + "|" + w.strip() + "|" + w.stripLeading() + "|" + w.stripTrailing());
        r = h(r, a.replace('l', 'L') + a.replace("World", "There") + a.concat("!") + "ab".repeat(3) + "x".repeat(0));
        r = h(r, String.join("-", "a", "b", "c") + String.join("+", new java.util.ArrayList<>(java.util.Arrays.asList("x", "y"))));
        char[] cs = a.toCharArray();
        r = LA.h(r, cs.length + cs[0]);
        r = h(r, new String(cs, 2, 4) + String.copyValueOf(cs, 0, 3) + new String(new char[]{'q'}));
        byte[] bytes = a.getBytes();
        r = LA.h(r, bytes.length + bytes[1]);
        r = h(r, new String(bytes));
        r = LA.h(r, a.codePointAt(1) + "😀".codePointAt(0) + "😀".length()
                + "😀x".codePointCount(0, 3));
        r = LA.h(r, a.regionMatches(true, 0, "HELLO", 0, 5) ? 1 : 0);
        r = LA.h(r, a.hashCode() + "".hashCode() + "Aa".hashCode() - "BB".hashCode());
        r = h(r, a.indent(2) + a.translateEscapes() + "%d+%s".formatted(s, "f"));
        try {
            a.charAt(-1);
        } catch (StringIndexOutOfBoundsException ex) {
            K.caught();
            r = LA.h(r, 41);
        }
        try {
            a.substring(5, 2);
        } catch (StringIndexOutOfBoundsException ex) {
            K.caught();
            r = LA.h(r, 42);
        }
        try {
            String n = s < 0 ? "x" : null;
            r = LA.h(r, n.length());
        } catch (NullPointerException ex) {
            K.caught();
            r = LA.h(r, 43);
        }
        return r;
    }

    static int format(int s) {
        int r = 25;
        r = h(r, String.format("%d|%5d|%-5d|%05d|%x|%X|%o|%,d", s, s, -s, s, s, s, s, s * 1000));
        r = h(r, String.format("%s|%10s|%-10s|%.3s|%c|%b|%%|%n", "str", "r", "l", "truncate", 'c', null));
        r = h(r, String.format("%f|%.2f|%10.4f|%e|%.3e|%g|%a", s / 3.0, s / 7.0, -s / 9.0, s * 1e10, 1e-10, 0.0001,
                1.0));
        r = h(r, String.format("%s %s %s", new MapLocation(s & 7, 3), Direction.EAST, new Named(s)));
        r = h(r, String.format("%08.3f|%+d|% d|%(d", 3.14159, s, s, -s));
        try {
            try {
                String.format("%d", "notint");
            } catch (java.util.IllegalFormatException e) {
                // never matches: the catch type is rewritten to instrumented.java.util, the throw is java.util
                r = LA.h(r, 51);
            }
        } catch (RuntimeException e) {
            K.caught();
            r = LA.h(r, 52);
        }
        return r;
    }

    static int regex(int s) {
        int r = 26;
        String a = "a1b22c333d" + s;
        r = h(r, a.replaceAll("[0-9]+", "#"));
        r = h(r, a.replaceFirst("\\d", "_"));
        r = LA.h(r, a.matches("[a-z0-9]+") ? 1 : 0);
        String[] parts = "x,,y,z,,".split(",");
        r = LA.h(r, parts.length);
        String[] p2 = a.split("\\d+", 3);
        for (String p : p2) r = h(r, p);
        try {
            "x".matches("(");
        } catch (java.util.regex.PatternSyntaxException e) {
            K.caught();
            r = LA.h(r, 61);
        }
        return r;
    }
}
