package bcx.subst;

/** Patch of java.util.stream.Tripwire: the org.openjdk.java.util.stream.tripwire property is unset. */
final class StreamTripwire_p {
    static final boolean ENABLED;
    static { ENABLED = false; }
    static void trip(Class<?> trippingClass, String msg) {}
}
