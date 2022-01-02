package qilin.pta.toolkits.zipper.flowgraph;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;
import qilin.pta.toolkits.zipper.pta.PointsToAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectFlowGraph implements IObjectFlowGraph {

    private Set<Node> nodes;
    private Map<qilin.core.pag.VarNode, VarNode> var2node;
    private Table<AllocNode, SparkField, InstanceFieldNode> field2node;

    public ObjectFlowGraph(PointsToAnalysis pta) {
        init(pta);
    }

    /**
     * @param var
     * @return the VarNode representing var
     */
    public VarNode nodeOf(qilin.core.pag.VarNode var) {
//        if (!var2node.containsKey(var)) {
//            throw new RuntimeException(var + " does not exist in Flow Graph");
//        }
        return var2node.get(var);
    }

    public Set<Node> succsOf(Node node) {
        return node.getOutEdges()
                .stream()
                .map(Edge::getTarget)
                .collect(Collectors.toSet());
    }

    public Set<Edge> outEdgesOf(Node node) {
        return node.getOutEdges();
    }

    public Set<Node> allNodes() {
        return nodes;
    }

    private void init(PointsToAnalysis pta) {
        nodes = new HashSet<>();
        var2node = new HashMap<>();

        // Add local assignment edges.
        pta.localAssignIterator().forEachRemaining(pair -> {
            qilin.core.pag.VarNode to = pair.getFirst();
            qilin.core.pag.VarNode from = pair.getSecond();
            VarNode toNode = getVarNode(to);
            VarNode fromNode = getVarNode(from);
            fromNode.addOutEdge(new Edge(Kind.LOCAL_ASSIGN, fromNode, toNode));
        });

        // Add inter-procedural assignment edges.
        pta.interProceduralAssignIterator().forEachRemaining(pair -> {
            qilin.core.pag.VarNode to = pair.getFirst();
            qilin.core.pag.VarNode from = pair.getSecond();
            VarNode toNode = getVarNode(to);
            VarNode fromNode = getVarNode(from);
            fromNode.addOutEdge(new Edge(Kind.INTERPROCEDURAL_ASSIGN, fromNode, toNode));
        });

        // Add this-passing assignment edges
        pta.thisAssignIterator().forEachRemaining(pair -> {
            qilin.core.pag.VarNode thisVar = pair.getFirst();
            qilin.core.pag.VarNode baseVar = pair.getSecond();
            VarNode toNode = getVarNode(thisVar);
            VarNode fromNode = getVarNode(baseVar);
            fromNode.addOutEdge(new Edge(Kind.INTERPROCEDURAL_ASSIGN, fromNode, toNode));
        });

        field2node = HashBasedTable.create();
        // Add instance load edges;
        pta.instanceLoadIterator().forEachRemaining(triple -> {
            qilin.core.pag.VarNode var = triple.getFirst();
            AllocNode base = triple.getSecond();
            SparkField field = triple.getThird();
            VarNode varNode = getVarNode(var);
            InstanceFieldNode fieldNode = getInstanceFieldNode(base, field);
            fieldNode.addOutEdge(new Edge(Kind.INSTANCE_LOAD, fieldNode, varNode));
        });

        // Add instance store edges;
        pta.instanceStoreIterator().forEachRemaining(triple -> {
            AllocNode base = triple.getFirst();
            SparkField field = triple.getSecond();
            qilin.core.pag.VarNode var = triple.getThird();
            VarNode varNode = getVarNode(var);
            InstanceFieldNode fieldNode = getInstanceFieldNode(base, field);
            varNode.addOutEdge(new Edge(Kind.INSTANCE_STORE, varNode, fieldNode));
        });
    }

    /**
     * @param var
     * @return the VarNode representing var.
     * If the node does not exist, it will be created.
     */
    private VarNode getVarNode(qilin.core.pag.VarNode var) {
        if (!var2node.containsKey(var)) {
            VarNode node = new VarNode(var);
            var2node.put(var, node);
            nodes.add(node);
            return node;
        }
        return var2node.get(var);
    }

    /**
     * @param base
     * @param field
     * @return the instance field representing base.field.
     * If the node does not exist, it will be created.
     */
    private InstanceFieldNode getInstanceFieldNode(AllocNode base, SparkField field) {
        if (!field2node.contains(base, field)) {
            InstanceFieldNode node = new InstanceFieldNode(base, field);
            field2node.put(base, field, node);
            nodes.add(node);
            return node;
        }
        return field2node.get(base, field);
    }
}
