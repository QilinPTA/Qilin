package qilin.pta.toolkits.zipper.flowgraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Node {
    private Set<Edge> outEdges;

    public Node() {
        this.outEdges = Collections.emptySet();
    }

    public void addOutEdge(final Edge e) {
        if (this.outEdges.isEmpty()) {
            this.outEdges = new HashSet<>();
        }
        this.outEdges.add(e);
    }

    public Set<Edge> getOutEdges() {
        return this.outEdges;
    }
}
