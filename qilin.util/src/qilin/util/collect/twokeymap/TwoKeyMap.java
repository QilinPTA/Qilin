package qilin.util.collect.twokeymap;

import java.util.Map;

public interface TwoKeyMap<K1, K2, V> {

    boolean containsKey(K1 key1, K2 key2);
    boolean containsKey(K1 key1);
    V get(K1 key1, K2 key2);
    Map<K2, V> get(K1 key1);
    boolean put(K1 key1, K2 key2, V value);
    void putAll(K1 key1, Map<K2, V> map);

}
