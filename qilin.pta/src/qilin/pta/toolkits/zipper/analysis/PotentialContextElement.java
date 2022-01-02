package qilin.pta.toolkits.zipper.analysis;

import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.zipper.Global;
import qilin.pta.toolkits.zipper.pta.PointsToAnalysis;
import qilin.util.collect.SetFactory;
import qilin.util.graph.MergedNode;
import qilin.util.graph.SCCMergedGraph;
import qilin.util.graph.TopologicalSorter;
import soot.SootMethod;
import soot.Type;

import java.util.*;

/**
 * For each object o, this class compute the set of methods
 * which o could potentially be their context element.
 * <p>
 * Conversely, for each method m, this class compute the
 * set of objects which could potentially be its context element.
 */
public class PotentialContextElement {
    private final PointsToAnalysis pta;
    // This map maps each object to the methods invoked on it.
    // For instance methods, they are the methods whose receiver is the object.
    // For static methods, they are the methods reachable from instance methods.
    private Map<AllocNode, Set<SootMethod>> invokedMethods;
    private final Map<Type, Set<SootMethod>> typePCEMethods;
    private final Map<AllocNode, Set<SootMethod>> pceOfMap;

    PotentialContextElement(final PointsToAnalysis pta, final ObjectAllocationGraph oag) {
        this.typePCEMethods = new HashMap<>();
        this.pceOfMap = new HashMap<>();
        this.pta = pta;
        this.init(oag);
    }

    public Set<SootMethod> PCEMethodsOf(final AllocNode obj) {
        return this.pceOfMap.getOrDefault(obj, Collections.emptySet());
    }

    /**
     * @param type
     * @return PCE methods of the objects of given type.
     */
    public Set<SootMethod> PCEMethodsOf(final Type type) {
        if (!this.typePCEMethods.containsKey(type)) {
            final Set<SootMethod> methods = new HashSet<>();
            this.pta.objectsOfType(type).forEach(obj -> methods.addAll(this.PCEMethodsOf(obj)));
            this.typePCEMethods.put(type, methods);
        }
        return this.typePCEMethods.get(type);
    }

    /**
     * Compute PCE methods for each objects.
     */
    private void init(final ObjectAllocationGraph oag) {
        final SCCMergedGraph<AllocNode> mg = new SCCMergedGraph<>(oag);
        final TopologicalSorter<MergedNode<AllocNode>> topoSorter = new TopologicalSorter<>();
        final SetFactory<SootMethod> setFactory = new SetFactory<>();
        this.invokedMethods = new HashMap<>();
        topoSorter.sort(mg, true).forEach(node -> {
            final Set<SootMethod> methods = setFactory.get(this.getPCEMethods(node, mg));
            node.getContent().forEach(obj -> {
                pceOfMap.put(obj, methods);
            });
        });
        this.invokedMethods = null;
        if (Global.isDebug()) {
            this.computePCEObjects();
        }
    }

    private Set<SootMethod> getPCEMethods(final MergedNode<AllocNode> node, final SCCMergedGraph<AllocNode> mg) {
        final Set<SootMethod> methods = new HashSet<>();
        mg.succsOf(node).forEach(n -> {
            final AllocNode o2 = n.getContent().iterator().next();
            methods.addAll(this.PCEMethodsOf(o2));
        });
        node.getContent().forEach(o -> methods.addAll(this.invokedMethodsOf(o)));
        return methods;
    }

    private Set<SootMethod> invokedMethodsOf(final AllocNode obj) {
        if (!this.invokedMethods.containsKey(obj)) {
            final Set<SootMethod> methods = new HashSet<>();
            final Queue<SootMethod> queue = new LinkedList<>(this.pta.methodsInvokedOn(obj));
            while (!queue.isEmpty()) {
                final SootMethod method = queue.poll();
                methods.add(method);
                this.pta.calleesOf(method).stream().filter(m -> m.isStatic() && !methods.contains(m)).forEach(queue::offer);
            }
            this.invokedMethods.put(obj, methods);
        }
        return this.invokedMethods.get(obj);
    }

    private void computePCEObjects() {
        final Map<SootMethod, Set<AllocNode>> pceObjs = new HashMap<>();
        this.pta.allObjects().forEach(obj -> this.PCEMethodsOf(obj).forEach(method -> {
            if (!pceObjs.containsKey(method)) {
                pceObjs.put(method, new HashSet<>());
            }
            pceObjs.get(method).add(obj);
        }));
    }
}
