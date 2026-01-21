package qilin.pta.toolkits.zipper.flowgraph;

import qilin.util.graph.DirectedGraph;
import qilin.util.graph.StronglyConnectedComponents;

import java.util.*;


public class ZSCCMergedGraph<N> implements DirectedGraph<ZMergedNode<N>> {
    private Set<ZMergedNode<N>> nodes;
    private final Map<N, ZMergedNode<N>> nodeMap = new HashMap<>();

    public ZSCCMergedGraph(final DirectedGraph<N> graph) {
        this.init(graph);
    }

    public ZMergedNode<N> getLstMergedNode(N contentNode) {
        return nodeMap.get(contentNode);
    }

    @Override
    public Collection<ZMergedNode<N>> allNodes() {
        return this.nodes;
    }

    @Override
    public Collection<ZMergedNode<N>> predsOf(final ZMergedNode<N> node) {
        return node.getPreds();
    }

    @Override
    public Collection<ZMergedNode<N>> succsOf(final ZMergedNode<N> node) {
        return node.getSuccs();
    }

    private void init(final DirectedGraph<N> graph) {
        this.nodes = new HashSet<>();
        StronglyConnectedComponents<N> scc = new StronglyConnectedComponents<>(graph);
        scc.getComponents().forEach(component -> {
            final ZMergedNode<N> node2 = new ZMergedNode<>(component);
            component.forEach(n -> nodeMap.put(n, node2));
            this.nodes.add(node2);
        });
        this.nodes.forEach(node -> node.getContent().stream().map(graph::succsOf).flatMap(Collection::stream)
                .map(nodeMap::get).filter(succ -> succ != node).forEach(succ -> {
                    node.addSucc(succ);
                    succ.addPred(node);
                }));
    }
}
