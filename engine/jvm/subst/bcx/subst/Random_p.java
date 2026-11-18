package bcx.subst;

import java.util.concurrent.atomic.AtomicLong;

/** Patch of java.util.Random's static initializer: no reflection (seedOffset serves deserialization only). */
public final class Random_p {
    private static final AtomicLong seedUniquifier;
    private static final long seedOffset;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private static final jdk.internal.misc.Unsafe unsafe;

    static {
        seedUniquifier = new AtomicLong(8682522807148012L);
        serialPersistentFields = null;
        unsafe = null;
        seedOffset = 0;
    }
}
