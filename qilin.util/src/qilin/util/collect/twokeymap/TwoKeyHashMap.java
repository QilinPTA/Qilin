package qilin.util.collect.twokeymap;

import java.util.HashMap;
import java.util.Map;

public class TwoKeyHashMap<K1, K2, V> extends AbstractTwoKeyMap<K1, K2, V> {

    public TwoKeyHashMap() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean put(K1 key1, K2 key2, V value) {
        map.putIfAbsent(key1, new HashMap<>());
        Map<K2, V> innerMap = map.get(key1);
        boolean isNewKey = !innerMap.containsKey(key2);
        return innerMap.put(key2, value) != null || isNewKey;
    }

    @Override
    public void putAll(K1 key1, Map<K2, V> m) {
        map.putIfAbsent(key1, new HashMap<>());
        Map<K2, V> innerMap = this.map.get(key1);
        innerMap.putAll(m);
    }
}
