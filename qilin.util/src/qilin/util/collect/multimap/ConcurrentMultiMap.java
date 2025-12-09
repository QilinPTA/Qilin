package qilin.util.collect.multimap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// NOTE: DO NOT USE the concurrent version of MultiMap from Soot, it has data race issues that may hurt your code logic!!!
public class ConcurrentMultiMap<K,V> extends AbstractMultiMap<K,V> {
    public ConcurrentMultiMap() {
        map = new ConcurrentHashMap<>();
    }

    @Override
    public boolean put(K key, V value) {
        return map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
    }

    @Override
    public boolean putAll(K key, Set<V> values) {
        return map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).addAll(values);
    }

    @Override
    public boolean putAll(MultiMap<K, V> other) {
        throw new UnsupportedOperationException("putAll is not supported in ConcurrentMultiMap");
    }

    @Override
    public void remove(K key) {
        map.remove(key);
    }
}
