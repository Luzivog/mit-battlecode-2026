package bcx.subst;

/**
 * jdk.internal.access.JavaLangAccess for translated code (what System.initPhase1 would install). The translator
 * completes it ("complete" in natives.txt): every method not written here throws UnsupportedOperationException.
 */
public abstract class LangAccess implements jdk.internal.access.JavaLangAccess {
    public static jdk.internal.access.JavaLangAccess bcxNew() { throw new Error(); }

    /** String.join(prefix, suffix, delimiter, elements, size): the same characters. */
    public String join(String prefix, String suffix, String delimiter, String[] elements, int size) {
        StringBuilder b = new StringBuilder(prefix);
        for (int i = 0; i < size; i++) {
            if (i > 0) b.append(delimiter);
            b.append(elements[i]);
        }
        return b.append(suffix).toString();
    }

    /** StringUTF16.getChar on this (little-endian) machine. */
    public char getUTF16Char(byte[] bytes, int index) {
        index <<= 1;
        return (char) (((bytes[index + 1] & 0xff) << 8) | (bytes[index] & 0xff));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> klass) {
        return (E[]) ((JClass) (Object) klass).getEnumConstantsShared();
    }
}
