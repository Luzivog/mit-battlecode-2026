package bcxtest;

public class Strings {
    static void print(String s) { Out.print(s); }

    public static void main(String[] a) {
        try { print("[" + "ab\ncd".indent(2) + "]"); } catch (Throwable t) { print("indent threw " + t); }
        try { print("[" + "a\\tb".translateEscapes() + "]"); } catch (Throwable t) { print("translate threw " + t); }
        print(String.format("%-8s|%5.2f|%d|%x|%s", "ab", 3.14159, -42, 255, null));
        print("x".repeat(3) + " " + "  pad ".strip() + " " + "AbC".toLowerCase() + " " + "abc".toUpperCase());
        print(String.join(",", "a", "b") + " " + "a,b,,c".split(",").length + " " + "hello".replace('l', 'L'));
        print(Double.toString(0.1 + 0.2) + " " + Float.toString(1.1f) + " " + Double.parseDouble("1e-3") + " " + Integer.parseInt("-77"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i).append(',');
        sb.setCharAt(0, 'X');
        sb.insert(2, "ins").deleteCharAt(sb.length() - 1).reverse();
        print(sb + " " + sb.indexOf("ni") + " " + "über".length() + " " + "日本".charAt(1));
    }
}
