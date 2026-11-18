package bcx.subst;

import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/** Patch of java.security.AccessController: no security manager, so a privileged action just runs. */
@SuppressWarnings("removal")
public final class AccessController_p {
    public static <T> T doPrivileged(PrivilegedAction<T> action) { return action.run(); }
    public static <T> T doPrivileged(PrivilegedAction<T> action, AccessControlContext context) { return action.run(); }
    public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action) { return action.run(); }

    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PrivilegedActionException(e);
        }
    }

    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action, AccessControlContext context) throws PrivilegedActionException {
        return doPrivileged(action);
    }

    public static AccessControlContext getContext() { return null; }
}
