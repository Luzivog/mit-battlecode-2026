package bcx.subst;

import java.io.InputStream;
import java.io.PrintStream;

/** java.lang.System for translated code (renamed to java/lang/System). */
public final class JSystem {
    public static final InputStream in = null;
    public static final PrintStream out = (PrintStream) (Object) JPrintStream.make(1);
    public static final PrintStream err = (PrintStream) (Object) JPrintStream.make(2);

    private JSystem() {}

    public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);
    public static native int identityHashCode(Object x);
    public static native long currentTimeMillis();
    public static native long nanoTime();
    public static native void exit(int status);

    public static String lineSeparator() { return "\n"; }
    public static String getProperty(String key) { checkKey(key); return null; }
    public static String getProperty(String key, String def) { checkKey(key); return def; }
    public static String getenv(String name) { return null; }
    public static SecurityManager getSecurityManager() { return null; }
    public static boolean allowSecurityManager() { return false; }

    private static void checkKey(String key) {
        if (key == null) throw new NullPointerException("key can't be null");
        if (key.isEmpty()) throw new IllegalArgumentException("key can't be empty");
    }
}
