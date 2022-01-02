package qilin.pta.toolkits.zipper.analysis;

import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.zipper.pta.PointsToAnalysis;
import qilin.util.collect.SetFactory;
import qilin.util.graph.DirectedGraph;
import qilin.util.graph.MergedNode;
import qilin.util.graph.SCCMergedGraph;
import qilin.util.graph.TopologicalSorter;
import soot.SootMethod;
import soot.Type;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JNewMultiArrayExpr;

import java.util.*;

public class ObjectAllocationGraph implements DirectedGraph<AllocNode> {
    private final PointsToAnalysis pta;
    private final Map<Type, Set<AllocNode>> typeAllocatees;
    private final Map<AllocNode, Set<AllocNode>> successors;
    private final Map<AllocNode, Set<AllocNode>> predecessors;
    private final Map<AllocNode, Set<AllocNode>> allocateeMap;

    public ObjectAllocationGraph(final PointsToAnalysis pta) {
        this.typeAllocatees = new HashMap<>();
        this.predecessors = new HashMap<>();
        this.successors = new HashMap<>();
        this.allocateeMap = new HashMap<>();
        this.pta = pta;
        this.init();
    }

    public Set<AllocNode> allNodes() {
        return this.pta.allObjects();
    }

    public Set<AllocNode> predsOf(final AllocNode obj) {
        return predecessors.getOrDefault(obj, Collections.emptySet());
    }

    public Set<AllocNode> succsOf(final AllocNode obj) {
        return successors.getOrDefault(obj, Collections.emptySet());
    }

    public Set<AllocNode> allocateesOf(final AllocNode obj) {
        return this.allocateeMap.getOrDefault(obj, Collections.emptySet());
    }

    public Set<AllocNode> allocateesOf(final Type type) {
        return this.typeAllocatees.get(type);
    }

    private void init() {
        final Map<AllocNode, Set<SootMethod>> invokedMethods = this.computeInvokedMethods();
        invokedMethods.forEach((obj, methods) -> methods.stream().map(this.pta::objectsAllocatedIn).flatMap(Collection::stream).forEach(o -> {
            if (isArray(obj)) {
                return;
            }
            successors.computeIfAbsent(obj, k -> new HashSet<>()).add(o);
            predecessors.computeIfAbsent(o, k -> new HashSet<>()).add(obj);
        }));
        this.computeAllocatees();
        this.pta.allObjects().forEach(obj -> {
            final Type type = obj.getType();
            this.typeAllocatees.putIfAbsent(type, new HashSet<>());
            this.typeAllocatees.get(type).addAll(this.allocateesOf(obj));
        });
    }

    private Map<AllocNode, Set<SootMethod>> computeInvokedMethods() {
        final Map<AllocNode, Set<SootMethod>> invokedMethods = new HashMap<>();
        this.pta.allObjects().forEach(obj -> {
            final Set<SootMethod> methods = new HashSet<>();
            final Queue<SootMethod> queue = new LinkedList<>(this.pta.methodsInvokedOn(obj));
            while (!queue.isEmpty()) {
                SootMethod method = queue.poll();
                methods.add(method);
                this.pta.calleesOf(method).stream().filter(m -> m.isStatic() && !methods.contains(m)).forEach(queue::offer);
            }
            invokedMethods.put(obj, methods);
        });
        return invokedMethods;
    }

    private void computeAllocatees() {
        final SCCMergedGraph<AllocNode> mg = new SCCMergedGraph<>(this);
        final TopologicalSorter<MergedNode<AllocNode>> sorter = new TopologicalSorter<>();
        final SetFactory<AllocNode> setFactory = new SetFactory<>();

        sorter.sort(mg, true).forEach(node -> {
            final Set<AllocNode> allocatees = setFactory.get(this.getAllocatees(node, mg));
            node.getContent().forEach(obj -> {
                allocateeMap.put(obj, allocatees);
            });
        });
    }

    private Set<AllocNode> getAllocatees(final MergedNode<AllocNode> node, final SCCMergedGraph<AllocNode> mg) {
        final Set<AllocNode> allocatees = new HashSet<>();
        mg.succsOf(node).forEach(n -> {
            // direct allocatees
            allocatees.addAll(n.getContent());
            // indirect allocatees, here, it does not require to traverse all heaps in n.getContent()
            // because of lines 104-107.
            final AllocNode o = n.getContent().iterator().next();
            allocatees.addAll(this.allocateesOf(o));
        });
        final AllocNode obj = node.getContent().iterator().next();
        if (node.getContent().size() > 1 || this.succsOf(obj).contains(obj)) {
            // The merged node is a true SCC
            allocatees.addAll(node.getContent());
        }
        return allocatees;
    }

    private boolean isArray(AllocNode obj) {
        return obj.getNewExpr() instanceof JNewArrayExpr || obj.getNewExpr() instanceof JNewMultiArrayExpr;
    }
}
