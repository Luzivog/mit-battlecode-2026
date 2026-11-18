package bcx.subst;

import battlecode.instrumenter.inject.ObjectMethods;
import battlecode.instrumenter.inject.RobotMonitor;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * battlecode.instrumenter.inject.System for the native engine (renamed; one per robot like the original). The
 * same methods, without the original's Config and Properties: the properties it can answer are "who knows?".
 */
public final class InjectSystem {
    public static PrintStream out = (PrintStream) (Object) JPrintStream.make(-1);
    public static PrintStream err = out;
    public static InputStream in = null;
    private static java.util.HashMap<String, String> props;

    private InjectSystem() {}

    private static java.util.HashMap<String, String> props() {
        if (props == null) {
            props = new java.util.HashMap<>();
            for (String k : new String[]{"java.version", "java.vendor", "java.vendor.url", "java.home", "java.class.version",
                    "java.class.path", "os.name", "os.arch", "os.version", "file.separator", "path.separator",
                    "line.separator", "user.name", "user.home", "user.dir"}) props.put(k, "who knows?");
        }
        return props;
    }

    public static void setSystemOut(PrintStream newOut) {}

    public static void setIn(InputStream newIn) {
        throw new battlecode.instrumenter.InstrumentationException(battlecode.instrumenter.InstrumentationException.Type.ILLEGAL, "You can't change System.in, sorry.");
    }

    public static void setOut(PrintStream newOut) {
        throw new battlecode.instrumenter.InstrumentationException(battlecode.instrumenter.InstrumentationException.Type.ILLEGAL, "You can't change System.out, sorry.");
    }

    public static void setErr(PrintStream newErr) {
        throw new battlecode.instrumenter.InstrumentationException(battlecode.instrumenter.InstrumentationException.Type.ILLEGAL, "You can't change System.err, sorry.");
    }

    public static java.nio.channels.Channel inheritedChannel() { return null; }

    public static java.io.Console console() { return null; }

    public static String lineSeparator() { return "\n"; }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
        if (length > 0) RobotMonitor.incrementBytecodes(length);
    }

    public static int identityHashCode(Object x) { return ObjectMethods.identityHashCode(x); }
    public static String getProperty(String key) { return props().get(key); }
    public static String getProperty(String key, String def) { String v = props().get(key); return v == null ? def : v; }
    public static String setProperty(String key, String value) { return props().put(key, value); }
    public static String clearProperty(String key) { return props().remove(key); }
    public static void exit(int status) { throw new battlecode.instrumenter.RobotDeathException(); }
}
