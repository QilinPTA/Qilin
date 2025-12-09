package qilin.util.collect.twokeymultimap;



import qilin.util.collect.multimap.ConcurrentMultiMap;

import java.util.concurrent.ConcurrentHashMap;


// this is a implementation based on ConcurrentHashMap.
public class ConcurrentTwoKeyMultiMap<K1, K2, V> extends AbstractTwoKeyMultiMap<K1, K2, V>{

    public ConcurrentTwoKeyMultiMap() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public boolean put(K1 key1, K2 key2, V value) {
        return map.computeIfAbsent(key1, k -> new ConcurrentMultiMap<>()).put(key2, value);
    }


}
