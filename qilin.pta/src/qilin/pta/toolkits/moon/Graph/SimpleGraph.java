package qilin.pta.toolkits.moon.Graph;





import qilin.util.collect.multimap.MultiHashMap;
import qilin.util.collect.multimap.MultiMap;

import java.util.Collections;

import java.util.HashSet;
import java.util.Set;

public class SimpleGraph<N> {

    private final Set<N> nodes = new HashSet<>();

    private final MultiMap<N, N> preds = new MultiHashMap<>();

    private final MultiMap<N, N> succs = new MultiHashMap<>();

    public SimpleGraph() {
    }


    public void addNode(N node) {
        nodes.add(node);
    }

    public void addEdge(N source, N target) {
        nodes.add(source);
        nodes.add(target);
        preds.put(target, source);
        succs.put(source, target);
    }

    public boolean hasEdge(N source, N target) {
        return succs.containsKey(source) && succs.get(source).contains(target);
    }

    public Set<N> getPredsOf(N node) {
        return preds.get(node);
    }


    public Set<N> getSuccsOf(N node) {
        return succs.get(node);
    }

    public Set<N> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }
}
