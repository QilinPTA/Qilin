package qilin.util.collect.twokeymap;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractTwoKeyMap<K1, K2, V> implements TwoKeyMap<K1, K2, V>{

    protected transient Map<K1, Map<K2, V>> map;


    @Override
    public boolean containsKey(K1 key1, K2 key2) {
        if(!map.containsKey(key1)) return false;
        return map.get(key1).containsKey(key2);
    }

    @Override
    public boolean containsKey(K1 key1) {
        return map.containsKey(key1);
    }




    @Override
    public V get(K1 key1, K2 key2) {
        Map<K2, V> innerMap = map.get(key1);
        if (innerMap == null) {
            return null;
        }
        return innerMap.get(key2);
    }

    @Override
    public Map<K2, V> get(K1 key1) {
        var innerMap = map.get(key1);
        if(innerMap == null) return Map.of();
        return Collections.unmodifiableMap(innerMap);
    }

}
