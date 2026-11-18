package bcx.subst;

import java.util.Locale;

/** Patch of java.lang.String: format without java.util.Formatter (MiniFormat). */
public class String_p {
    public static String format(String format, Object... args) { return MiniFormat.format(format, args); }
    public static String format(Locale l, String format, Object... args) { return MiniFormat.format(format, args); }
    public String formatted(Object... args) { return MiniFormat.format((String) (Object) this, args); }
}
