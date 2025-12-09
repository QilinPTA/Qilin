package qilin.util.collect.twokeymultimap;



import qilin.util.collect.multimap.MultiHashMap;
import qilin.util.collect.multimap.MultiMap;

import java.util.Map;
import java.util.Set;

public abstract class AbstractTwoKeyMultiMap<K1, K2, V> implements TwoKeyMultiMap<K1, K2, V> {
    protected transient Map<K1, MultiMap<K2, V>> map;
    @Override
    public boolean contains(K1 key1, K2 key2, V value) {
        // check if key1 exists
        if(!map.containsKey(key1)){
            return false;
        }
        MultiMap<K2, V> subMap = map.get(key1);
        // check if key2 exists
        if(!subMap.containsKey(key2)){
            return false;
        }
        Set<V> values = subMap.get(key2);
        return values.contains(value);
    }

    @Override
    public MultiMap<K2, V> get(K1 key1) {
        var m = map.get(key1);
        if(m != null) return m;
        return new MultiHashMap<>();
    }

    @Override
    public Set<V> get(K1 key1, K2 key2) {
        if(!map.containsKey(key1)){
            return Set.of();
        }
        MultiMap<K2, V> subMap = map.get(key1);
        return subMap.get(key2);
    }

    @Override
    public boolean containsKey(K1 key1) {
        return map.containsKey(key1);
    }

    @Override
    public boolean containsKey(K1 key1, K2 key2) {
        if(!map.containsKey(key1)){
            return false;
        }
        MultiMap<K2, V> subMap = map.get(key1);
        return subMap.containsKey(key2);
    }
}
