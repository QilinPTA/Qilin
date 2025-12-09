package qilin.util.collect.multimap;

import java.util.*;

public class UnmodifiableMultiMap<K, V> implements MultiMap<K,V> {
    private final MultiMap<K,V> delegate;

    public UnmodifiableMultiMap(MultiMap<K,V> delegate){
        this.delegate = delegate;
    }

    @Override
    public Set<V> get(K key) {
        return Collections.unmodifiableSet(delegate.get(key));
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    @Override
    public Set<V> values() {
        return delegate.values();
    }

    @Override
    public boolean putAll(MultiMap<K, V> other) {
        throw new UnsupportedOperationException("This MultiMap is unmodifiable");
    }

    @Override
    public boolean contains(K key, V value) {
        return delegate.contains(key, value);
    }

    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }


    @Override
    public boolean put(K key, V value) {
        throw new UnsupportedOperationException("This MultiMap is unmodifiable");
    }

    @Override
    public boolean putAll(K key, Set<V> values) {
        throw new UnsupportedOperationException("This MultiMap is unmodifiable");
    }

    @Override
    public void remove(K key) {
        throw new UnsupportedOperationException("This MultiMap is unmodifiable");
    }
}
