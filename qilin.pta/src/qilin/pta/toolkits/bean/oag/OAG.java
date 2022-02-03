/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.pta.toolkits.bean.oag;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.sets.P2SetVisitor;
import qilin.core.sets.PointsToSetInternal;
import qilin.util.PTAUtils;
import soot.SootMethod;
import soot.util.queue.QueueReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Object Allocation Graph (OAG).
 */
public class OAG {
    private final PTA pta;
    private final Set<Node> nodes = new HashSet<>();
    private Set<Node> rootNodes;
    private Set<Node> tailNodes;
    /**
     * map a heap to its corresponding node in the OAG
     */
    private final Map<AllocNode, Node> heap2node = new HashMap<>();

    public OAG(PTA prePta) {
        this.pta = prePta;
    }

    public void build() {
        buildOAG();
        computeRootNodes();
        computeTailNodes();
    }

    public Set<Node> allNodes() {
        return nodes;
    }

    public Set<Node> rootNodes() {
        return rootNodes;
    }

    public Set<Node> tailNodes() {
        return tailNodes;
    }

    public Set<Node> getPredsOf(Node n) {
        return n.getPreds();
    }

    public Set<Node> getSuccsOf(Node n) {
        return n.getSuccs();
    }

    public int getInDegreeOf(Node n) {
        return n.getInDegree();
    }

    public int getOutDegreeOf(Node n) {
        return n.getOutDegree();
    }

    /**
     * @param source
     * @param dest
     * @return whether there is a path from source to target in the OAG
     */
    public boolean reaches(Node source, Node dest) {
        Set<Node> reachableNodes = source.getReachableNodes();
        if (reachableNodes == null) {
            reachableNodes = computeReachableNodes(source);
            source.setReachableNodes(reachableNodes);
        }
        return reachableNodes.contains(dest);
    }

    /**
     * @param source
     * @return the nodes in OAG which can be reached from source
     */
    public Set<Node> computeReachableNodes(Node source) {
        Set<Node> reachableNodes = new HashSet<>();
        Stack<Node> stack = new Stack<>();
        stack.push(source);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (reachableNodes.add(node)) {
                stack.addAll(node.getSuccs());
            }
        }
        return reachableNodes;
    }

    protected void buildOAG() {
        Map<LocalVarNode, Set<AllocNode>> pts = PTAUtils.calcStaticThisPTS(this.pta);
        for (SootMethod method : this.pta.getNakedReachableMethods()) {
            if (method.isPhantom()) {
                continue;
            }
            MethodPAG srcmpag = pta.getPag().getMethodPAG(method);
            MethodNodeFactory srcnf = srcmpag.nodeFactory();
            LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
            QueueReader<qilin.core.pag.Node> reader = srcmpag.getInternalReader().clone();
            while (reader.hasNext()) {
                qilin.core.pag.Node from = reader.next(), to = reader.next();
                if (from instanceof AllocNode) {
                    Node tgt = addNode((AllocNode) from);
                    if (PTAUtils.isFakeMainMethod(method)) {
                        // special treatment for fake main
                        Node src = addNode(pta.getRootNode());
                        addEdge(src, tgt);
                    } else if (method.isStatic()) {
                        pts.getOrDefault(thisRef, Collections.emptySet()).forEach(a -> {
                            Node src = addNode(a);
                            addEdge(src, tgt);
                        });
                    } else {
                        PointsToSetInternal thisPts = PTAUtils.fetchInsensitivePointsToResult(pta, thisRef);
                        thisPts.forall(new P2SetVisitor() {
                            @Override
                            public void visit(qilin.core.pag.Node n) {
                                AllocNode a = (AllocNode) n;
                                Node src = addNode(a);
                                addEdge(src, tgt);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Add a node which represents the given heap object to the OAG. If the
     * corresponding node already exists, then return the existing node.
     *
     * @param heap the one represented by the added node in the graph
     * @return the new added node
     */
    private Node addNode(AllocNode heap) {
        Node node = heap2node.get(heap);
        if (node == null) {
            node = new Node(heap);
            heap2node.put(heap, node);
            nodes.add(node);
        }
        return node;
    }

    /**
     * Add a directed object allocation edge to the OAG.
     */
    protected void addEdge(Node src, Node tgt) {
        src.addSucc(tgt);
    }

    private void computeRootNodes() {
        rootNodes = nodes.stream().filter(node -> node.getInDegree() == 0).collect(Collectors.toSet());
    }

    private void computeTailNodes() {
        tailNodes = nodes.stream().filter(node -> node.getOutDegree() == 0).collect(Collectors.toSet());
    }

    /*
     * utilities
     * */
    public void dumpToDot() {
        Vector<String> vec = constructOAGDotString();
        for (int idx = 0; idx < vec.size(); ++idx) {
            String fileName = "a" + idx + ".dot";
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                PrintStream ps = new PrintStream(fos);
                ps.println(vec.get(idx));
                ps.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int index = 0;
    private final Map<Object, Integer> node2Id = new HashMap<>();

    private int getNodeID(Object node) {
        if (node2Id.containsKey(node)) {
            return node2Id.get(node);
        } else {
            node2Id.put(node, index++);
            return node2Id.get(node);
        }
    }

    private Vector<String> constructOAGDotString() {
        Vector<String> vector = new Vector<>();
        rootNodes().stream().filter(x -> !tailNodes().contains(x)).forEach(root -> {
            Set<Node> reachs = computeReachableNodes(root);
            StringBuilder dumpString = new StringBuilder();
            dumpString.append("digraph G {\n");
            for (Node x : reachs) {
                x.getSuccs().forEach(y -> {
                    dumpString.append(addEdgeString(x, y));
                });
            }
            dumpString.append("}");
            vector.add(dumpString.toString());
        });
        node2Id.forEach((k, v) -> {
            System.out.println("\tID " + v + ":" + ((Node) k).getHeap().toString());
        });
        return vector;
    }

    private String addEdgeString(Node x, Node y) {
        return "\t" + getNodeID(x) + " -> " + getNodeID(y) + ";\n";
    }
}
