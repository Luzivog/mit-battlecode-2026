package bcx.subst;

/** java.lang.Thread for the single-threaded native engine (renamed): the one thread there is. */
public class JThread implements Runnable {
    private static final JThread MAIN = new JThread();
    private final String name = "main";

    public JThread() {}
    public JThread(Runnable task) { throw new UnsupportedOperationException("bcx: no threads"); }

    public static JThread currentThread() { return MAIN; }
    public static void yield() {}
    public static void onSpinWait() {}
    public static boolean interrupted() { return false; }
    public static boolean holdsLock(Object obj) { return true; }
    public static void sleep(long millis) { throw new UnsupportedOperationException("bcx: Thread.sleep"); }
    public void run() {}
    public void start() { throw new UnsupportedOperationException("bcx: no threads"); }
    public boolean isInterrupted() { return false; }
    public void interrupt() {}
    public final String getName() { return name; }
    public long getId() { return 1; }
    public final long threadId() { return 1; }
    public final boolean isDaemon() { return false; }
    public final boolean isVirtual() { return false; }
    public ClassLoader getContextClassLoader() { return null; }
    public StackTraceElement[] getStackTrace() { return new StackTraceElement[0]; }
    public String toString() { return "Thread[#1,main,5,main]"; }
}
