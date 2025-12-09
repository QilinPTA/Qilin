package qilin.pta.toolkits.moon.ObjCollection;


import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.moon.Graph.LabeledGraph;
import qilin.pta.toolkits.moon.Moon;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.support.Util;
import qilin.util.collect.multimap.MultiHashMap;
import qilin.util.collect.multimap.MultiMap;
import qilin.util.collect.twokeymultimap.TwoKeyMultiHashMap;
import qilin.util.collect.twokeymultimap.TwoKeyMultiMap;
import qilin.pta.toolkits.moon.traversal.TraversalResult;
import soot.ArrayType;
import soot.SootMethod;
import soot.jimple.spark.pag.SparkField;


import java.util.*;

public class ObjCollector {
    private final int maxCtxLayer;
    private final MoonDataConstructor.MoonDataStructure moonData;
    private final PartialChecker partialChecker;
    public ObjCollector(int maxCtxLayer, MoonDataConstructor.MoonDataStructure moonData) {
        this.maxCtxLayer = maxCtxLayer;
        this.moonData = moonData;
        this.partialChecker = new PartialChecker(moonData);
    }

    public Set<AllocNode> analyze(List<MultiMap<AllocNode, TraversalResult>> traversalResults) {

        MultiMap<AllocNode, SparkField> basePRObjToFields = new MultiHashMap<>();

        Set<AllocNode> potentialPRObjs = new HashSet<>();
        TwoKeyMultiMap<AllocNode, SparkField, SootMethod> storedFToReachMtds = new TwoKeyMultiHashMap<>();

        collectbasePRObjs(traversalResults, basePRObjToFields, potentialPRObjs, storedFToReachMtds);
        var prObjDepGraph = buildPRObjDepGraph(traversalResults, basePRObjToFields, potentialPRObjs);
        var recurPRObjToFields = collectRecurPRObjs(traversalResults, basePRObjToFields, prObjDepGraph);

        Set<AllocNode> prObjs = new HashSet<>();
        prObjs.addAll(basePRObjToFields.keySet());
        prObjs.addAll(recurPRObjToFields.keySet());
        int oldSize, newSize;
        while(true){
            oldSize = prObjs.size();
            var redundantObjs = collectRedundantObjs(prObjs, prObjDepGraph);
            prObjs.removeAll(redundantObjs);
            redundantObjs.forEach(o -> {
                basePRObjToFields.remove(o);
                recurPRObjToFields.remove(o);
            });
            newSize = prObjs.size();
            if(oldSize == newSize){
                break;
            }
        }


        System.out.println("[ObjCollector] Collected " + prObjs.size() + " PR objects.");
        System.out.println("[ObjCollector] Base PR objects: " + basePRObjToFields.keySet().size());
        System.out.println("[ObjCollector] Recursive PR objects: " + recurPRObjToFields.keySet().size());
        return prObjs;

    }

    private Set<AllocNode> collectRedundantObjs(Set<AllocNode> prObjs, LabeledGraph<AllocNode, SparkField> containerGraph) {
        var fieldPointsToGraph = moonData.fieldPointsToGraph();
        Set<AllocNode> toRemoved = new HashSet<>();
        for (AllocNode matchedObj : prObjs) {
            if (matchedObj.getType() instanceof ArrayType) {
                // array obj.
                Set<AllocNode> pts = fieldPointsToGraph.getFieldPointsTo(matchedObj);
                if (pts.size() <= 1 && pts.stream().noneMatch(prObjs::contains)) {
                    toRemoved.add(matchedObj);
                }
            } else {
                // class obj.
                boolean isRemoved = fieldPointsToGraph.getAllFieldsOf(matchedObj).stream().allMatch(field -> {
                    Set<AllocNode> pts = fieldPointsToGraph.getFieldPointsTo(matchedObj, field);
                    return pts.size() <= 1 && pts.stream().noneMatch(prObjs::contains);
                });
                if (isRemoved) {
                    toRemoved.add(matchedObj);
                    containerGraph.getOutEdgesOf(matchedObj).forEach(edge -> {
                        AllocNode to = edge.target();
                        toRemoved.add(to);
                    });
                }
            }
        }
        return toRemoved;
    }

    private MultiMap<AllocNode, SparkField> collectRecurPRObjs(List<MultiMap<AllocNode, TraversalResult>> traversalResults,
                                                  MultiMap<AllocNode, SparkField> basePRObjToFields,
                                                  LabeledGraph<AllocNode, SparkField> prObjDepGraph) {
        if(Moon.enableRecursivePRObjs){
            MultiMap<AllocNode, SparkField> recurPRObjToFields = new MultiHashMap<>();

            var wrapperObjToFields = collectWrapperContainerPRObjs(prObjDepGraph, basePRObjToFields.keySet());
            recurPRObjToFields.putAll(wrapperObjToFields);
            if(maxCtxLayer == 2){
                var allocatorObjToFields = collectAllocatorContainersFor3obj(basePRObjToFields.keySet(), traversalResults);
                var wrapperOfAllocObjToFields = collectWrapperContainerPRObjs(prObjDepGraph, allocatorObjToFields.keySet());
                recurPRObjToFields.putAll(allocatorObjToFields);
                recurPRObjToFields.putAll(wrapperOfAllocObjToFields);
            }
            return recurPRObjToFields;
        }else{
            return new MultiHashMap<>();
        }
    }

    private MultiMap<AllocNode, SparkField> collectAllocatorContainersFor3obj(Set<AllocNode> basePRObjs, List<MultiMap<AllocNode, TraversalResult>> traversalResults) {
        MultiMap<AllocNode, SparkField> allocPRObjToFields = new MultiHashMap<>();
        for (AllocNode basePRObj : basePRObjs) {
            for (MultiMap<AllocNode, TraversalResult> traversalResult : traversalResults) {
                for (TraversalResult resultOfVarTrace : traversalResult.get(basePRObj)) {
                    Set<AllocNode> firstLayerCtxObjs = new HashSet<>(resultOfVarTrace.getMatchedCtxObjsOfParam(1));
                    Set<AllocNode> secondLayerCtxObjs = new HashSet<>(resultOfVarTrace.getMatchedCtxObjsOfParam(2));
                    if (firstLayerCtxObjs.isEmpty() || secondLayerCtxObjs.isEmpty()) {
                        continue;
                    }
                    for (AllocNode firstLayerCtxObj : firstLayerCtxObjs) {
                        Set<AllocNode> allocatorsOfFirstLayerCtxObjs = moonData.oag().getPredsOf(firstLayerCtxObj);
                        if ((firstLayerCtxObj.getMethod() != null && firstLayerCtxObj.getMethod().isStatic()) || Util.haveOverlap(allocatorsOfFirstLayerCtxObjs, secondLayerCtxObjs)) {
                            if(partialChecker.check(firstLayerCtxObj)){
                                allocPRObjToFields.put(firstLayerCtxObj, resultOfVarTrace.getField());
                            }
                        }
                    }
                }
            }
        }
        return allocPRObjToFields;
    }

    private MultiMap<AllocNode, SparkField> collectWrapperContainerPRObjs(LabeledGraph<AllocNode, SparkField> containerGraph, Set<AllocNode> basePRObjs) {
        MultiMap<AllocNode, SparkField> wrapperObjToFields = new MultiHashMap<>();
        Set<AllocNode> nodes = new HashSet<>(containerGraph.getNodes());
        nodes.retainAll(basePRObjs);
        Queue<AllocNode> queue = new ArrayDeque<>(nodes);
        int size = queue.size();
        Queue<Integer> depthQueue = new ArrayDeque<>(Collections.nCopies(size, 0));
        Set<AllocNode> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            AllocNode current = queue.remove();
            int depth = depthQueue.element();
            if (!visited.contains(current)) {
                visited.add(current);
                if (depth < maxCtxLayer) {
                    // Push unvisited successors onto the queue
                    for (LabeledGraph<AllocNode, SparkField>.LabelEdge outEdge : containerGraph.getOutEdgesOf(current)) {
                        AllocNode neighbor = outEdge.target();
                        if (!visited.contains(neighbor) && !basePRObjs.contains(neighbor) && !wrapperObjToFields.contains(neighbor, outEdge.label())) {
                            queue.add(neighbor);
                            wrapperObjToFields.put(neighbor, outEdge.label());
                            depthQueue.add(depth + 1);
                        }
                    }
                }
            }
        }
        return wrapperObjToFields;
    }

    private LabeledGraph<AllocNode, SparkField> buildPRObjDepGraph(List<MultiMap<AllocNode, TraversalResult>> traversalResults, MultiMap<AllocNode, SparkField> basePRObjToFields, Set<AllocNode> potentialPRObjs) {
        var oag = moonData.oag();
        LabeledGraph<AllocNode, SparkField> objDepGraph = new LabeledGraph<>();
        for (AllocNode nonInnerContainerObj : potentialPRObjs) {

            for (MultiMap<AllocNode, TraversalResult> traversalResult : traversalResults) {
                for (TraversalResult resultOfVarTrace : traversalResult.get(nonInnerContainerObj)) {
                    for (int layer = 0; layer < resultOfVarTrace.getRecordSize(); layer++) {
                        if (layer > maxCtxLayer) break;
                        Set<AllocNode> newlyAllocObjs = resultOfVarTrace.getNewlyAllocObjs(layer);
                        for (AllocNode newlyAllocObj : newlyAllocObjs) {
                            if (newlyAllocObj.equals(nonInnerContainerObj)) continue;
                            Set<AllocNode> allocatorOfNewlyAlloc = oag.getPredsOf(newlyAllocObj);
                            Set<AllocNode> allocatorOfNonInner = oag.getPredsOf(nonInnerContainerObj);
                            if (Util.haveOverlap(allocatorOfNonInner, allocatorOfNewlyAlloc)
                                    &&
                                    !allocatorOfNewlyAlloc.contains(nonInnerContainerObj)
                            ) {
                                objDepGraph.addEdge(newlyAllocObj, nonInnerContainerObj, resultOfVarTrace.getField());
                            }
                        }
                    }
                }
            }
        }
        return LabeledGraph.unmodifiableGraph(objDepGraph);
    }

    private void collectbasePRObjs(List<MultiMap<AllocNode, TraversalResult>> traversalResults,
                                   MultiMap<AllocNode, SparkField> basePRObjToFields,
                                   Set<AllocNode> potentialPRObjs,
                                   TwoKeyMultiMap<AllocNode, SparkField, SootMethod> storedFieldToExistingMethods) {
        for (int idx = 0; idx < traversalResults.size(); idx++) {
            int checkLayer = idx + 1;
            for(AllocNode obj: traversalResults.get(idx).keySet()) {
                for (TraversalResult traversalResult : traversalResults.get(idx).get(obj)) {
                    if(!moonData.containers().contains(obj)){
                        continue;
                    }
                    if(traversalResult.isMetParamOfAllocatedMethod()){
                        partialChecker.addMetParamObj(obj);
                    }
                    var field = traversalResult.getField();
                    if (traversalResult.hasCtxObjsOnLayerOf(checkLayer)) {
                        basePRObjToFields.put(obj, field);
                        traversalResult.getVisitedMethods().forEach(m -> storedFieldToExistingMethods.put(obj, field, m));
                    } else if (Moon.enableRecursivePRObjs && traversalResult.hasNewlyAllocObjs()) {
                        potentialPRObjs.add(obj);
                        traversalResult.getVisitedMethods().forEach(m -> storedFieldToExistingMethods.put(obj, field, m));
                    }
                }
            }

        }


        var removedObj = basePRObjToFields.keySet().stream().filter(obj -> !partialChecker.check(obj)).toList();
        removedObj.forEach(basePRObjToFields::remove);
        potentialPRObjs.removeIf(obj -> !partialChecker.check(obj) || basePRObjToFields.containsKey(obj));
        
        
    }
}
