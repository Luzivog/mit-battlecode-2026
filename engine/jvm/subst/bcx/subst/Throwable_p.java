package bcx.subst;

import java.io.PrintStream;
import java.io.PrintWriter;

/** Patch of java.lang.Throwable: stack traces are not recorded, so printing one prints the causes only. */
public class Throwable_p {
    public void printStackTrace() { printStackTrace(System.err); }

    public void printStackTrace(PrintStream s) {
        s.println(this);
        Throwable c = getCause();
        for (int n = 0; c != null && n < 64; n++, c = c.getCause()) s.println("Caused by: " + c);
    }

    public void printStackTrace(PrintWriter s) {
        s.println(this);
        Throwable c = getCause();
        for (int n = 0; c != null && n < 64; n++, c = c.getCause()) s.println("Caused by: " + c);
    }

    public StackTraceElement[] getStackTrace() { return new StackTraceElement[0]; }

    @Orig
    public synchronized Throwable getCause() { return null; }
}
