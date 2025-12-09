package qilin.util.collect.twokeymultimap;


import qilin.util.collect.multimap.MultiMap;

import java.util.Set;

public interface TwoKeyMultiMap<K1, K2, V> {

    boolean contains(K1 key1, K2 key2, V value);
    boolean containsKey(K1 key1, K2 key2);
    boolean containsKey(K1 key1);
    Set<V> get(K1 key1, K2 key2);
    MultiMap<K2, V> get(K1 key1);
    boolean put(K1 key1, K2 key2, V value);


}
