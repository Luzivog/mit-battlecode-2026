package bcx.subst;

/** Patch of jdk.internal.misc.VM: no saved system properties (the JVM's defaults apply). */
public final class VM_p {
    public static String getSavedProperty(String key) { return null; }
    /** Every translated class is loaded by the boot loader (getClassLoader() is null). */
    public static boolean isSystemDomainLoader(ClassLoader loader) { return true; }
}
