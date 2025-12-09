package qilin.util.collect.multimap;

import java.util.Set;

public interface MultiMap<K,V> {
    boolean contains(K key, V value);

    boolean containsKey(K key);

    Set<V> get(K key);

    boolean put(K key, V value);

    boolean putAll(K key, Set<V> values);

    boolean putAll(MultiMap<K, V> other);

    Set<K> keySet();

    Set<V> values();

    void remove(K key);

}
