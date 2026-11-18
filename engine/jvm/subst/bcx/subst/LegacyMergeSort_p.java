package bcx.subst;

/** Patch of java.util.Arrays$LegacyMergeSort: the system property is unset. */
public final class LegacyMergeSort_p {
    private static final boolean userRequested;
    static { userRequested = false; }
}
