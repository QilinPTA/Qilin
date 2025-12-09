package qilin.util.collect.multimap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MultiHashMap<K,V> extends AbstractMultiMap<K,V>{
    public MultiHashMap() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean put(K key, V value) {
        if(!map.containsKey(key)){
            map.put(key, new HashSet<>());
        }
        return map.get(key).add(value);
    }

    @Override
    public boolean putAll(K key, Set<V> values) {
        if(!map.containsKey(key)){
            map.put(key, new HashSet<>());
        }
        return map.get(key).addAll(values);
    }

    @Override
    public boolean putAll(MultiMap<K, V> other) {
        boolean modified = false;
        for (K key : other.keySet()) {
            // defensive copy
            Set<V> values = new HashSet<>(other.get(key));
            if (this.putAll(key, values)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void remove(K key) {
        map.remove(key);
    }


}
