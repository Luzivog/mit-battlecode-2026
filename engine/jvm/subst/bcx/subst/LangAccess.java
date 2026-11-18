package bcx.subst;

/**
 * jdk.internal.access.JavaLangAccess for translated code (what System.initPhase1 would install). The translator
 * completes it ("complete" in natives.txt): every method not written here throws UnsupportedOperationException.
 */
public abstract class LangAccess implements jdk.internal.access.JavaLangAccess {
    public static jdk.internal.access.JavaLangAccess bcxNew() { throw new Error(); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> klass) {
        return (E[]) ((JClass) (Object) klass).getEnumConstantsShared();
    }
}
