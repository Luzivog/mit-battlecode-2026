package bcx.subst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Patch of battlecode.instrumenter.inject.StreamMethods (what the instrumenter puts in place of Stream.toList()):
 * the same reflective toArray() call and the same wrapping of what it throws.
 */
public class StreamMethods_p {
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Object stream) {
        if (!hasToArray0(stream)) throw new RuntimeException(stream.getClass().getName() + ".toArray()");
        T[] result;
        try {
            result = (T[]) invokeToArray0(stream);
        } catch (Throwable t) {
            // Method.invoke wraps it in an InvocationTargetException, whose message is null
            throw new RuntimeException((String) null);
        }
        return (List<T>) Collections.unmodifiableList(new ArrayList<>(Arrays.asList(result)));
    }

    private static native boolean hasToArray0(Object o);
    private static native Object[] invokeToArray0(Object o);
}
