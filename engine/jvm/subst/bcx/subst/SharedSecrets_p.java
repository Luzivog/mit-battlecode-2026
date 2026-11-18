package bcx.subst;

/** Patch of jdk.internal.access.SharedSecrets: the java.lang access exists without System's initialization. */
public final class SharedSecrets_p {
    private static jdk.internal.access.JavaLangAccess javaLangAccess;

    public static jdk.internal.access.JavaLangAccess getJavaLangAccess() {
        if (javaLangAccess == null) javaLangAccess = LangAccess.bcxNew();
        return javaLangAccess;
    }
}
