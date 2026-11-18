package bcx.subst;

import java.nio.charset.Charset;

/** Patch of java.nio.charset.StandardCharsets: only the three charsets String decodes itself. */
final class StandardCharsets_p {
    public static final Charset US_ASCII;
    public static final Charset ISO_8859_1;
    public static final Charset UTF_8;
    public static final Charset UTF_16BE;
    public static final Charset UTF_16LE;
    public static final Charset UTF_16;

    static {
        US_ASCII = sun.nio.cs.US_ASCII.INSTANCE;
        ISO_8859_1 = sun.nio.cs.ISO_8859_1.INSTANCE;
        UTF_8 = sun.nio.cs.UTF_8.INSTANCE;
        UTF_16BE = null;
        UTF_16LE = null;
        UTF_16 = null;
    }
}
