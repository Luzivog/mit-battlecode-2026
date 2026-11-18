package bcx.subst;

import java.nio.charset.Charset;

/**
 * Patch of java.nio.charset.Charset: the default charset is UTF-8 (JEP 400), without reading properties, and
 * only the JDK's standard charsets exist (no service-loaded or extended providers).
 */
public abstract class Charset_p {
    public static Charset defaultCharset() { return java.nio.charset.StandardCharsets.UTF_8; }
    private static Charset lookupViaProviders(final String charsetName) { return null; }
    private static Charset lookupExtendedCharset(String charsetName) { return null; }
}
