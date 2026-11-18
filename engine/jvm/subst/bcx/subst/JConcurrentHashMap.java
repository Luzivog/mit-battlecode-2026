package bcx.subst;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * java.util.concurrent.ConcurrentHashMap for the single-threaded native engine (renamed): a HashMap. Only system
 * code uses it (bots get the engine's battlecode.instrumenter.inject.ConcurrentHashMap); none of it iterates one
 * in a way that reaches a game.
 */
public class JConcurrentHashMap<K, V> extends HashMap<K, V> implements ConcurrentMap<K, V> {
    public JConcurrentHashMap() { super(); }
    public JConcurrentHashMap(int initialCapacity) { super(initialCapacity); }
    public JConcurrentHashMap(int initialCapacity, float loadFactor) { super(initialCapacity, loadFactor); }
    public JConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) { super(initialCapacity, loadFactor); }
    public JConcurrentHashMap(Map<? extends K, ? extends V> m) { super(m); }

    public V put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException();
        return super.put(key, value);
    }

    public V get(Object key) {
        if (key == null) throw new NullPointerException();
        return super.get(key);
    }

    public long mappingCount() { return size(); }
}
