package bcx.subst;

/** Patch of jdk.internal.misc.VM: no saved system properties (the JVM's defaults apply). */
public final class VM_p {
    public static String getSavedProperty(String key) { return null; }
}
