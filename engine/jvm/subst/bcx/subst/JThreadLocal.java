package bcx.subst;

import java.util.function.Supplier;

/** java.lang.ThreadLocal for the single-threaded native engine (renamed): one value. */
public class JThreadLocal<T> {
    private T value;
    private boolean set;

    public JThreadLocal() {}

    protected T initialValue() { return null; }

    public static <S> JThreadLocal<S> withInitial(Supplier<? extends S> supplier) { return new Supplied<>(supplier); }

    public T get() {
        if (!set) { value = initialValue(); set = true; }
        return value;
    }

    public void set(T v) { value = v; set = true; }

    public void remove() { value = null; set = false; }

    static final class Supplied<T> extends JThreadLocal<T> {
        private final Supplier<? extends T> supplier;
        Supplied(Supplier<? extends T> supplier) { this.supplier = supplier; }
        protected T initialValue() { return supplier.get(); }
    }
}
