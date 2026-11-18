package bcx.subst;

import java.nio.charset.Charset;

/** Patch of java.nio.charset.Charset: the default charset is UTF-8 (JEP 400), without reading properties. */
public abstract class Charset_p {
    public static Charset defaultCharset() { return java.nio.charset.StandardCharsets.UTF_8; }
}
