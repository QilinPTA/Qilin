package qilin.pta.toolkits.moon.support;


import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.Node;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PtrSetCache {
    private final PTA pta;
    private final Map<Node, Set<AllocNode>> cache = new ConcurrentHashMap<>();
    public PtrSetCache(PTA pta) {
        this.pta = pta;
    }

    public Set<AllocNode> ptsOf(Node node) {
        Set<AllocNode> result = cache.get(node);
        if (result == null) {
            result = Collections.unmodifiableSet((Set<AllocNode>) pta.reachingObjects(node).toCIPointsToSet().toCollection());
            cache.put(node, result);
        }
        return result;
    }



}
