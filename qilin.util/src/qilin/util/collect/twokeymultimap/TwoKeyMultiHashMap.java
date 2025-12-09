package qilin.util.collect.twokeymultimap;



import qilin.util.collect.multimap.MultiHashMap;

import java.util.HashMap;

public class TwoKeyMultiHashMap<K1, K2, V> extends AbstractTwoKeyMultiMap<K1, K2, V>{

    public TwoKeyMultiHashMap(){
        this.map = new HashMap<>();
    }

    @Override
    public boolean put(K1 key1, K2 key2, V value) {
        map.putIfAbsent(key1, new MultiHashMap<>());
        return map.get(key1).put(key2, value);
    }

}
