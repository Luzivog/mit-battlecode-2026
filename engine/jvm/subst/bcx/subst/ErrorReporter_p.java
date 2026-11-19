package bcx.subst;

/** Patch of battlecode.server.ErrorReporter: the same reports on stderr, without Server. Not part of a game. */
public class ErrorReporter_p {
    public static void warn(String message) { System.err.println("[server:warning] " + message); }
    public static void report(Throwable e) { report(e, true); }
    public static void report(String message) { report(message, true); }
    public static void report(Throwable e, String message, boolean ourFault) {
        warn(e.getMessage());
        warn(message);
        printStackTrace(e);
    }
    public static void report(String message, boolean ourFault) { warn(message); }
    public static void report(String message, String thingsToTry) { warn(message); }
    public static void report(Throwable e, boolean ourFault) { printStackTrace(e); }
    private static void printStackTrace(Throwable e) {
        System.err.println("Stack trace: ");
        e.printStackTrace(System.err);
    }
}
