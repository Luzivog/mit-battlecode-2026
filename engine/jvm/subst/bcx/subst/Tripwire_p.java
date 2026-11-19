package bcx.subst;

/** Patch of java.util.Tripwire: the org.openjdk.java.util.stream.tripwire property is unset. */
final class Tripwire_p {
    static final boolean ENABLED;
    static { ENABLED = false; }
    static void trip(Class<?> trippingClass, String msg) {}
}
