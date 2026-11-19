package bcxtest;

/** String.format conversions the substitute formatter implements, against java.util.Formatter. */
public class Formats {
    public static void main(String[] a) {
        double[] ds = {0, -0.0, 1, -1, 0.5, 0.05, 0.005, 123.456, -9.995, 1e-5, 1e7, 1.23456789e10, 99.9999, 0.00012345,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Double.MAX_VALUE, 2.5, 3.5};
        String[] fs = {"%f", "%.2f", "%5.2f", "%-8.3f|", "%08.2f", "%+.1f", "% .0f", "%,.2f", "%e", "%.3e", "%12.4E", "%g",
                "%.3g", "%10.2g", "%G", "%.0e", "%#.0f", "%(.2f", "%a", "%A", "%20a", "%-20a|"};
        for (String f : fs) {
            StringBuilder b = new StringBuilder(f + ":");
            for (double d : ds) {
                try { b.append(' ').append(String.format(f, d)); } catch (Exception e) { b.append(" !").append(e.getClass().getSimpleName()); }
            }
            Out.print(b.toString());
        }
        Out.print(String.format("%d|%5d|%-5d|%05d|%,d|%+d|%x|%X|%o|%c|%b|%s|%S|%10s|%-10s|%.2s|%%|%n", 42, -42, 7, 42, 1234567, 5, 255, 255, 8, 'q', null, "str", "up", "r", "l", "trunc"));
        Out.print(String.format("%2$s %1$s %<s", "a", "b") + String.format("%h", "x").length());
    }
}
