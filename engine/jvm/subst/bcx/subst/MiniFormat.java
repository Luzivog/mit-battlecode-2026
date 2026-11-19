package bcx.subst;

/**
 * java.util.Formatter's output for the conversions the engine and the corpus use: %s %S %d %x %X %o %c %b %f %n %%
 * with flags - 0 + space , and width/precision, in the default locale (English digits and separators). Anything
 * else throws, so an unsupported format is never silently different.
 */
final class MiniFormat {
    static String format(String fmt, Object[] args) {
        StringBuilder out = new StringBuilder();
        int ai = 0, last = -1;
        int i = 0, n = fmt.length();
        while (i < n) {
            char c = fmt.charAt(i);
            if (c != '%') { out.append(c); i++; continue; }
            int start = i++;
            // argument index or previous
            int argIndex = -1;
            boolean prev = false;
            int j = i;
            while (j < n && Character.isDigit(fmt.charAt(j))) j++;
            if (j < n && fmt.charAt(j) == '$' && j > i) { argIndex = Integer.parseInt(fmt.substring(i, j)) - 1; i = j + 1; }
            String flags = "";
            while (i < n && "-#+ 0,(<".indexOf(fmt.charAt(i)) >= 0) {
                if (fmt.charAt(i) == '<') prev = true;
                else flags += fmt.charAt(i);
                i++;
            }
            int width = -1, prec = -1;
            j = i;
            while (i < n && Character.isDigit(fmt.charAt(i))) i++;
            if (i > j) width = Integer.parseInt(fmt.substring(j, i));
            if (i < n && fmt.charAt(i) == '.') {
                j = ++i;
                while (i < n && Character.isDigit(fmt.charAt(i))) i++;
                if (i == j) throw new java.util.UnknownFormatConversionException(fmt.substring(start));
                prec = Integer.parseInt(fmt.substring(j, i));
            }
            if (i >= n) throw new java.util.UnknownFormatConversionException("%");
            char conv = fmt.charAt(i++);
            if (conv == 'n') { out.append('\n'); continue; }
            if (conv == '%') { pad(out, "%", flags, width); continue; }
            Object arg;
            if (prev) arg = args[last];
            else if (argIndex >= 0) { arg = args[argIndex]; last = argIndex; }
            else {
                if (args == null || ai >= args.length) throw new java.util.MissingFormatArgumentException(fmt.substring(start, i));
                last = ai;
                arg = args[ai++];
            }
            String s;
            switch (conv) {
                case 's': case 'S':
                    if (flags.indexOf('#') >= 0 || flags.indexOf('0') >= 0 || flags.indexOf('+') >= 0) unsupported(fmt, start, i);
                    s = String.valueOf(arg);
                    if (prec >= 0 && prec < s.length()) s = s.substring(0, prec);
                    if (conv == 'S') s = upper(s);
                    pad(out, s, flags, width);
                    break;
                case 'h': case 'H':
                    s = arg == null ? "null" : Integer.toHexString(arg.hashCode());
                    if (prec >= 0 && prec < s.length()) s = s.substring(0, prec);
                    if (conv == 'H') s = upper(s);
                    pad(out, s, flags, width);
                    break;
                case 'b': case 'B':
                    s = arg == null ? "false" : arg instanceof Boolean bb ? bb.toString() : "true";
                    if (prec >= 0 && prec < s.length()) s = s.substring(0, prec);
                    if (conv == 'B') s = upper(s);
                    pad(out, s, flags, width);
                    break;
                case 'c':
                    if (arg == null) s = "null";
                    else if (arg instanceof Character ch) s = String.valueOf(ch.charValue());
                    else if (arg instanceof Byte || arg instanceof Short || arg instanceof Integer) s = new String(Character.toChars(((Number) arg).intValue()));
                    else throw new java.util.IllegalFormatConversionException(conv, arg.getClass());
                    pad(out, s, flags, width);
                    break;
                case 'd':
                    if (arg == null) { pad(out, "null", flags, width); break; }
                    if (!(arg instanceof Integer || arg instanceof Long || arg instanceof Short || arg instanceof Byte))
                        mismatch(conv, arg, fmt, start, i);
                    integer(out, ((Number) arg).longValue(), flags, width);
                    break;
                case 'x': case 'X': case 'o':
                    if (arg == null) { pad(out, "null", flags, width); break; }
                    if (flags.indexOf('#') >= 0 || flags.indexOf(',') >= 0 || flags.indexOf('+') >= 0) unsupported(fmt, start, i);
                    long v = ((Number) arg).longValue();
                    if (arg instanceof Integer) s = conv == 'o' ? Integer.toOctalString((int) v) : Integer.toHexString((int) v);
                    else if (arg instanceof Long) s = conv == 'o' ? Long.toOctalString(v) : Long.toHexString(v);
                    else if (arg instanceof Short) s = conv == 'o' ? Integer.toOctalString((int) v & 0xffff) : Integer.toHexString((int) v & 0xffff);
                    else if (arg instanceof Byte) s = conv == 'o' ? Integer.toOctalString((int) v & 0xff) : Integer.toHexString((int) v & 0xff);
                    else { unsupported(fmt, start, i); s = null; }
                    if (conv == 'X') s = upper(s);
                    if (flags.indexOf('0') >= 0) while (s.length() < width) s = "0" + s;
                    pad(out, s, flags, width);
                    break;
                case 'f':
                    if (arg == null) { pad(out, "null", flags, width); break; }
                    if (!(arg instanceof Double || arg instanceof Float)) mismatch(conv, arg, fmt, start, i);
                    fixed(out, ((Number) arg).doubleValue(), flags, width, prec < 0 ? 6 : prec);
                    break;
                case 'a': case 'A': {
                    if (arg == null) { pad(out, conv == 'A' ? "NULL" : "null", flags, width); break; }
                    if (!(arg instanceof Double || arg instanceof Float)) mismatch(conv, arg, fmt, start, i);
                    if (prec >= 0) unsupported(fmt, start, i);
                    double d = ((Number) arg).doubleValue();
                    if (Double.isNaN(d)) { pad(out, conv == 'A' ? "NAN" : "NaN", flags, width); break; }
                    boolean neg = Double.compare(d, 0.0) == -1;
                    double av = Math.abs(d);
                    if (Double.isInfinite(av)) { signed(out, neg, conv == 'A' ? "INFINITY" : "Infinity", flags, width); break; }
                    String hs = Double.toHexString(av).substring(2);
                    int p = hs.indexOf('p');
                    String body = (conv == 'A' ? "0X" + upper(hs.substring(0, p)) + "P" : "0x" + hs.substring(0, p) + "p") + hs.substring(p + 1);
                    if (flags.indexOf('0') >= 0) unsupported(fmt, start, i);
                    signed(out, neg, body, flags, width);
                    break;
                }
                case 'g': case 'G':
                    if (arg == null) { pad(out, conv == 'G' ? "NULL" : "null", flags, width); break; }
                    if (!(arg instanceof Double || arg instanceof Float)) mismatch(conv, arg, fmt, start, i);
                    general(out, ((Number) arg).doubleValue(), flags, width, prec, conv == 'G');
                    break;
                case 'e': case 'E':
                    if (arg == null) { pad(out, conv == 'E' ? "NULL" : "null", flags, width); break; }
                    if (!(arg instanceof Double || arg instanceof Float)) mismatch(conv, arg, fmt, start, i);
                    scientific(out, ((Number) arg).doubleValue(), flags, width, prec < 0 ? 6 : prec, conv == 'E');
                    break;
                default:
                    unsupported(fmt, start, i);
            }
        }
        return out.toString();
    }

    private static String upper(String s) {
        char[] cs = s.toCharArray();
        for (int k = 0; k < cs.length; k++) {
            if (cs[k] > 127) throw new UnsupportedOperationException("bcx MiniFormat: non-ASCII upper case");
            if (cs[k] >= 'a' && cs[k] <= 'z') cs[k] -= 32;
        }
        return new String(cs);
    }

    /** Formatter.addZeros: zeros (and a dot if needed) so the fraction has prec digits. */
    private static void addZeros(StringBuilder sb, int prec) {
        int len = sb.length(), i;
        for (i = 0; i < len; i++) if (sb.charAt(i) == '.') break;
        boolean needDot = i == len;
        int outPrec = len - i - (needDot ? 0 : 1);
        if (outPrec == prec) return;
        if (needDot) sb.append('.');
        for (int k = 0; k < prec - outPrec; k++) sb.append('0');
    }

    private static void general(StringBuilder out, double d, String flags, int width, int precision, boolean upper) {
        if (Double.isNaN(d)) { pad(out, upper ? "NAN" : "NaN", flags, width); return; }
        boolean neg = Double.compare(d, 0.0) == -1;
        double v = Math.abs(d);
        if (Double.isInfinite(v)) { signed(out, neg, upper ? "INFINITY" : "Infinity", flags.replace("0", ""), width); return; }
        int prec = precision == -1 ? 6 : precision == 0 ? 1 : precision;
        char[] exp;
        StringBuilder mant = new StringBuilder();
        int expRounded;
        if (v == 0.0) { exp = null; mant.append('0'); expRounded = 0; }
        else {
            jdk.internal.math.FormattedFPDecimal fd = jdk.internal.math.FormattedFPDecimal.valueOf(v, prec, jdk.internal.math.FormattedFPDecimal.GENERAL);
            exp = fd.getExponent();
            mant.append(fd.getMantissa());
            expRounded = fd.getExponentRounded();
        }
        if (exp != null) prec -= 1;
        else prec -= expRounded + 1;
        addZeros(mant, prec);
        if (flags.indexOf('#') >= 0 && prec == 0) mant.append('.');
        String m = mant.toString();
        if (flags.indexOf(',') >= 0) {
            int dot = m.indexOf('.');
            m = group(dot < 0 ? m : m.substring(0, dot)) + (dot < 0 ? "" : m.substring(dot));
        }
        String tail = exp == null ? "" : (upper ? "E" : "e") + new String(exp);
        int w = width < 0 ? -1 : width - tail.length();
        StringBuilder b = new StringBuilder();
        signed(b, neg, m, flags, w);
        String s = b.toString() + tail;
        if (flags.indexOf('-') >= 0 && width > s.length()) { StringBuilder p = new StringBuilder(s); while (p.length() < width) p.append(' '); s = p.toString(); }
        out.append(s);
    }

    private static void scientific(StringBuilder out, double d, String flags, int width, int prec, boolean upper) {
        if (Double.isNaN(d)) { pad(out, upper ? "NAN" : "NaN", flags, width); return; }
        boolean neg = Double.compare(d, 0.0) == -1;
        double v = Math.abs(d);
        if (Double.isInfinite(v)) { signed(out, neg, upper ? "INFINITY" : "Infinity", flags.replace("0", ""), width); return; }
        jdk.internal.math.FormattedFPDecimal fd = jdk.internal.math.FormattedFPDecimal.valueOf(v, prec, jdk.internal.math.FormattedFPDecimal.SCIENTIFIC);
        String m = new String(fd.getMantissa());
        int dot = m.indexOf('.');
        String ip = dot < 0 ? m : m.substring(0, dot);
        String fp = dot < 0 ? "" : m.substring(dot + 1);
        while (fp.length() < prec) fp += '0';
        char[] exp = v == 0.0 ? new char[]{'+', '0', '0'} : fd.getExponent();
        String tail = (upper ? "E" : "e") + new String(exp);
        String body = (prec == 0 ? ip : ip + "." + fp);
        // the exponent counts toward the width; zero padding goes between the sign and the mantissa
        int w = width < 0 ? -1 : width - tail.length();
        StringBuilder b = new StringBuilder();
        signed(b, neg, body, flags, w);
        String s = b.toString() + tail;
        if (flags.indexOf('-') >= 0 && width > s.length()) { StringBuilder p = new StringBuilder(s); while (p.length() < width) p.append(' '); s = p.toString(); }
        out.append(s);
    }

    /** java.util.Formatter's error for an argument of the wrong type (BigInteger/BigDecimal are not supported here). */
    private static void mismatch(char conv, Object arg, String fmt, int s, int e) {
        if (arg instanceof java.math.BigInteger || arg instanceof java.math.BigDecimal) unsupported(fmt, s, e);
        throw new java.util.IllegalFormatConversionException(conv, arg.getClass());
    }

    private static void unsupported(String fmt, int s, int e) {
        throw new UnsupportedOperationException("bcx MiniFormat: unsupported format " + fmt.substring(s, e));
    }

    private static void pad(StringBuilder out, String s, String flags, int width) {
        if (width < 0 || s.length() >= width) { out.append(s); return; }
        if (flags.indexOf('-') >= 0) { out.append(s); for (int k = s.length(); k < width; k++) out.append(' '); }
        else { for (int k = s.length(); k < width; k++) out.append(' '); out.append(s); }
    }

    private static String group(String digits) {
        StringBuilder b = new StringBuilder();
        int len = digits.length();
        for (int k = 0; k < len; k++) {
            if (k > 0 && (len - k) % 3 == 0) b.append(',');
            b.append(digits.charAt(k));
        }
        return b.toString();
    }

    private static void signed(StringBuilder out, boolean neg, String body, String flags, int width) {
        String sign = neg ? "-" : flags.indexOf('+') >= 0 ? "+" : flags.indexOf(' ') >= 0 ? " " : "";
        boolean paren = neg && flags.indexOf('(') >= 0;
        if (paren) { sign = "("; body = body + ")"; }
        if (flags.indexOf('0') >= 0 && width > 0) {
            StringBuilder z = new StringBuilder(sign);
            for (int k = sign.length() + body.length(); k < width; k++) z.append('0');
            out.append(z).append(body);
        } else pad(out, sign + body, flags, width);
    }

    private static void integer(StringBuilder out, long v, String flags, int width) {
        boolean neg = v < 0;
        String digits = neg ? Long.toString(v).substring(1) : Long.toString(v);
        if (flags.indexOf(',') >= 0) digits = group(digits);
        signed(out, neg, digits, flags, width);
    }

    private static void fixed(StringBuilder out, double d, String flags, int width, int prec) {
        if (Double.isNaN(d)) { pad(out, "NaN", flags, width); return; }
        boolean neg = Double.compare(d, 0.0) == -1;
        double v = Math.abs(d);
        if (Double.isInfinite(v)) { signed(out, neg, "Infinity", flags.replace("0", ""), width); return; }
        // Formatter (JDK 21): FormattedFPDecimal's PLAIN digits, then zeros to the precision
        char[] mant = jdk.internal.math.FormattedFPDecimal.valueOf(v, prec, jdk.internal.math.FormattedFPDecimal.PLAIN).getMantissa();
        String m = new String(mant);
        int dot = m.indexOf('.');
        String ip = dot < 0 ? m : m.substring(0, dot);
        String fp = dot < 0 ? "" : m.substring(dot + 1);
        while (fp.length() < prec) fp += '0';
        if (flags.indexOf(',') >= 0) ip = group(ip);
        String body = prec == 0 ? (flags.indexOf('#') >= 0 ? ip + "." : ip) : ip + "." + fp;
        signed(out, neg, body, flags, width);
    }
}
