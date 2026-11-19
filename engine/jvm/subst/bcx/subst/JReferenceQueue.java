package bcx.subst;

import java.lang.ref.Reference;

/** java.lang.ref.ReferenceQueue for the native engine (renamed): its collector never clears or enqueues. */
public class JReferenceQueue<T> {
    static final JReferenceQueue<Object> NULL = new JReferenceQueue<>();
    static final JReferenceQueue<Object> ENQUEUED = new JReferenceQueue<>();

    public JReferenceQueue() {}

    boolean enqueue(Reference<? extends T> r) { return false; }
    public Reference<? extends T> poll() { return null; }
    public Reference<? extends T> remove(long timeout) { throw new UnsupportedOperationException("bcx: ReferenceQueue.remove would block forever"); }
    public Reference<? extends T> remove() { throw new UnsupportedOperationException("bcx: ReferenceQueue.remove would block forever"); }
}
