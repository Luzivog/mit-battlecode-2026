package bcx.subst;

/** Patch of jdk.internal.util.random.RandomSupport: the java.util.secureRandomSeed property is unset. */
public final class RandomSupport_p {
    private static boolean secureRandomSeedRequested() { return false; }
}
