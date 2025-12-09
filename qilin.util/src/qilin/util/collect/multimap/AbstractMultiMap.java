package qilin.util.collect.multimap;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractMultiMap<K,V> implements MultiMap<K,V> {
    protected transient Map<K, Set<V>> map;

    @Override
    public Set<V> get(K key) {
        var s = map.get(key);
        if(s == null){
            return Set.of();
        }
        return s;
    }

    @Override
    public boolean contains(K key, V value) {
        var s = map.get(key);
        if(s == null){
            return false;
        }
        return s.contains(value);
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }


    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Set<V> values() {
        return map.values().stream().flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());
    }
}
