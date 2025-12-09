package qilin.pta.toolkits.moon.traversal;



import qilin.core.pag.*;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.moon.Graph.*;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.support.PtrSetCache;
import qilin.pta.toolkits.moon.support.Util;
import qilin.util.CallDetails;
import qilin.util.Pair;
import qilin.util.collect.multimap.MultiMap;
import soot.ArrayType;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.spark.pag.SparkField;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StoredVarTraverser {

    private final VFG VFGForField;
    private final CallDetails methodCallDetail;
    private final MultiMap<AllocNode, SootMethod> objToInvokedMethodsOn;
    private final OAG objectAllocationGraphWithArr;
    private final int maxCtxLayer;
    private final Map<AllocNode, Map<AllocNode, Integer>> ctxObjsCache = new ConcurrentHashMap<>();
    private final FieldPointsToGraph fieldPointsToGraph;
    private final Set<LocalVarNode> allocatedVars;

    private final PtrSetCache ptrSetCache;
    private final PAG pag;
    public StoredVarTraverser(MoonDataConstructor.MoonDataStructure moonData, int maxCtxLayer) {
        this.maxCtxLayer = maxCtxLayer;
        this.methodCallDetail = CallDetails.v();
        this.VFGForField = moonData.vfgForField();
        this.objectAllocationGraphWithArr = moonData.oag();
        this.objToInvokedMethodsOn = moonData.objToIvkMtds();
        this.fieldPointsToGraph = moonData.fieldPointsToGraph();
        this.allocatedVars = moonData.allocVars();
        this.ptrSetCache = moonData.ptrSetCache();
        this.pag = moonData.pag();
    }
    public TraversalResult findSourceOfVarStoredIn(AllocNode obj, LocalVarNode varStored, int maxAllocLayerToTrace, SparkField __storedField) {
        SootMethod allocatedMethod = obj.getMethod();
        Deque<TraversalStatus> stack = new ArrayDeque<>(); // dfs
        Set<TraversalStatus> visited = new HashSet<>();
        TraversalResult traversalResult = new TraversalResult(maxAllocLayerToTrace, __storedField);
        traversalResult.addVisitedMethod(varStored.getMethod());
        int maxAllocatorLayer = getMaxAllocLayer(varStored, obj) - 1;
        if (maxAllocatorLayer == -1 - 1) return traversalResult; // -1 means the variable not in methods called by the obj or its ctx objs.
        stack.push(new TraversalStatus(varStored, maxAllocatorLayer, 0));

        if(obj.getType().toString().contains("javax.swing.event.EventListenerList")){
            // This is a hack for special case of EventListenerList.
            // Specifically, it's a has a lazy-init field, which will be initialized when certain method is called, and this behavior cannot be captured with context-insensitive analysis.
            traversalResult.addMatchedCtxObjsOfParam(objectAllocationGraphWithArr.getPredsOf(obj), 1);
        }

        while (!stack.isEmpty()) {
            TraversalStatus crtTrace = stack.pop();
            if (visited.contains(crtTrace)) continue;
            visited.add(crtTrace);
            Node crtPtr = crtTrace.pointer;
            int crtCallStackLevel = crtTrace.callStackLevel;
            if (crtPtr == null || crtCallStackLevel > 100) continue; // skip abnormal call stack.
            if (reachingPTS(crtPtr).isEmpty()) continue;
            int crtAllocatorLevel = crtTrace.allocatorLevel;
            if(crtPtr instanceof LocalVarNode localVarNode && localVarNode.getMethod().equals(allocatedMethod)){
                traversalResult.hasMetParamOfAllocatedMethod();
            }
            if (crtPtr instanceof LocalVarNode && allocatedVars.contains(crtPtr)) {
                Set<AllocNode> pts = reachingPTS(crtPtr);

                for (AllocNode newObj : pts) {
                    int allocatorLayer = -1;
                    if(newObj.equals(obj) || objectAllocationGraphWithArr.getSuccsOf(obj).contains(newObj)){
                        allocatorLayer = 0;
                    }else{
                        Map<AllocNode, Integer> ctxObjs = getCtxObjsOf(obj);
                        for (AllocNode allocator : ctxObjs.keySet()) {
                            if(objectAllocationGraphWithArr.getSuccsOf(allocator).contains(newObj)){
                                allocatorLayer = ctxObjs.get(allocator);
                                break;
                            }
                        }
                    }
                    if(allocatorLayer != -1){
                        traversalResult.addNewlyAllocObj(newObj, allocatorLayer);
                    }
                }
                continue;
            }

            Set<Node> predOfParamPassing = new HashSet<>();
            for (FlowEdge inEdge : VFGForField.getPredsOf(crtPtr)) {
                FlowKind flowKind = inEdge.flowKind();
                Node predNode = inEdge.source();
                switch (flowKind) {
                    case INSTANCE_LOAD, CALL_STORE, FIELD_STORE -> {
                    }
                    case STATIC_LOAD, STATIC_STORE -> {
                        TraversalStatus newPtrTrace = new TraversalStatus(predNode, crtAllocatorLevel, crtCallStackLevel, crtTrace, flowKind);
                        if (!visited.contains(newPtrTrace)) {
                            stack.push(newPtrTrace);
                            if(predNode instanceof LocalVarNode localVarNode){
                                traversalResult.addVisitedMethod(localVarNode.getMethod());
                            }
                        }
                    }
                    case RETURN -> {
                        if(predNode instanceof LocalVarNode localVarNode && localVarNode.getMethod().isStatic()){
                            TraversalStatus newPtrTrace = new TraversalStatus(predNode, crtAllocatorLevel, crtCallStackLevel, crtPtr, crtTrace, FlowKind.STATIC_METHOD_RETURN);
                            if (!visited.contains(newPtrTrace)) {
                                stack.push(newPtrTrace);
                                traversalResult.addVisitedMethod(localVarNode.getMethod());
                            }
                        }
                    }
                    case THIS_PASSING -> {
                        List<Set<AllocNode>> ctxObjs = collectCtxObj(crtPtr, obj);
                        for (int allocatorLayer = 0; allocatorLayer < ctxObjs.size(); allocatorLayer++) {
                            Set<AllocNode> ctxObjsOfLayer = ctxObjs.get(allocatorLayer);
                            if (ctxObjsOfLayer.isEmpty()) continue;
                            if (allocatorLayer == maxAllocLayerToTrace) {
                                // match finished.
                                if(connectedUnderLimitNumOfField(varStored, ctxObjsOfLayer, obj) != FieldRelation.NONE){
                                    traversalResult.addMatchedCtxObjsByThisAsParam(ctxObjsOfLayer, allocatorLayer);
                                }

                            } else {
                                if (allocatorLayer > 0 && allocatorLayer >= crtAllocatorLevel) {
                                    if(connectedUnderLimitNumOfField(varStored, ctxObjsOfLayer, obj)  != FieldRelation.NONE) {
                                        traversalResult.addMatchedCtxObjsByThisAsParam(ctxObjsOfLayer, allocatorLayer);
                                    }
                                }
                            }
                        }
                    }
                    
                    case CALL_LOAD -> {

                        if (checkIfThis(predNode)) {
                            Set<Node> returnVars = getReturnVarOfThisMethodCall(predNode, crtPtr);
                            for (Node varReturnFrom : returnVars) {
                                TraversalStatus newPtrTrace = new TraversalStatus(varReturnFrom, crtAllocatorLevel, crtCallStackLevel, crtTrace, FlowKind.THIS_METHOD_RETURN);
                                if (!visited.contains(newPtrTrace)) {
                                    stack.push(newPtrTrace);
                                }
                            }

                        } else {
                            if (containComplexFlowForSpecificFlow(crtTrace, FlowKind.CALL_LOAD)) continue;
                            TraversalStatus newPtrTrace = new TraversalStatus(predNode, crtAllocatorLevel, crtCallStackLevel, crtTrace, FlowKind.CALL_LOAD);
                            if (!visited.contains(newPtrTrace)) {
                                stack.push(newPtrTrace);
                            }
                        }

                    }
                    case FIELD_LOAD -> {
                        if (checkIfThis(predNode)) {
                            if(maxCtxLayer == 1 && __storedField.getType().toString().contains("java.awt.image.SampleModel")){ // handle another special case for SampleModel
                                if(crtTrace.getVisitedFlowCounter(FlowKind.THIS_FIELD_STORE_AND_LOAD) > 0){
                                    List<Set<AllocNode>> ctxObjs = collectCtxObj(crtPtr, obj);
                                    for (int allocatorLayer = 0; allocatorLayer < ctxObjs.size(); allocatorLayer++) {
                                        Set<AllocNode> ctxObjsOfLayer = ctxObjs.get(allocatorLayer);
                                        if (ctxObjsOfLayer.isEmpty()) continue;
                                        if (allocatorLayer == maxAllocLayerToTrace || (allocatorLayer > 0 && allocatorLayer >= crtAllocatorLevel)) {
                                            // match finished.
                                            if(connectedUnderLimitNumOfField(varStored, ctxObjsOfLayer, obj) != FieldRelation.NONE){
                                                traversalResult.addMatchedCtxObjsOfParam(ctxObjsOfLayer, allocatorLayer);
                                            }
                                        }
                                    }
                                }
                            }else{
                                SparkField __labeledField = ((FieldEdge)inEdge).field();
                                if(__labeledField instanceof SootField labeledField && __storedField instanceof SootField storedField){
                                    if(labeledField.getDeclaringClass().equals(storedField.getDeclaringClass()))
                                        continue;
                                }
                            }
                            if (containComplexFlowForSpecificFlow(crtTrace, FlowKind.THIS_FIELD_STORE_AND_LOAD))
                                continue;
                            Set<Node> fieldStoredVar = getFieldStoredVar(predNode, crtPtr);
                            for (Node pointer : fieldStoredVar) {
                                TraversalStatus newPtrTrace = new TraversalStatus(pointer, crtAllocatorLevel, crtCallStackLevel, crtTrace, FlowKind.THIS_FIELD_STORE_AND_LOAD);
                                newPtrTrace.needCheckFieldRelationWhenMeetNew = false;
                                if (!visited.contains(newPtrTrace)) {
                                    stack.push(newPtrTrace);
                                }
                            }

                        } else {
                            if (containComplexFlowForSpecificFlow(crtTrace, FlowKind.FIELD_LOAD)) continue;
                            TraversalStatus newPtrTrace = new TraversalStatus(predNode, crtAllocatorLevel, crtCallStackLevel, crtTrace, FlowKind.FIELD_LOAD);
                            if (!visited.contains(newPtrTrace)) {
                                stack.push(newPtrTrace);
                            }
                        }

                    }
                    case LOCAL_ASSIGN -> {
                        TraversalStatus newPtrTrace = new TraversalStatus(predNode, crtAllocatorLevel, crtCallStackLevel, crtTrace, FlowKind.LOCAL_ASSIGN);
                        if (!visited.contains(newPtrTrace)) {
                            stack.push(newPtrTrace);
                        }
                    }

                    case NEW ->  {
                    }
                    
                    case PARAMETER_PASSING ->  {
                        // leave for below.
                        predOfParamPassing.add(predNode);
                    }
                    default -> {
                        throw new RuntimeException("Unexpected flowKind: " + flowKind);
                    }
                }
            }
            
            if(!predOfParamPassing.isEmpty()) {
                if (getMethodOfPointer(crtPtr).isStatic()) {
                    // in static method.
                    Set<AllocNode> layerOneCtxObjs = objectAllocationGraphWithArr.getPredsOf(obj);
                    if (getMethodOfPointer(crtPtr).equals(obj.getMethod())) {
                        if (layerOneCtxObjs.size() > 1 && 1 >= crtAllocatorLevel) {
                            FieldRelation fieldRelation = connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
                            if(checkFieldRelation(fieldRelation, predOfParamPassing, varStored)){
                                traversalResult.addMatchedCtxObjsOfParam(layerOneCtxObjs, 1);
                            }
                        }
                    }else if(maxAllocLayerToTrace == 2 && crtAllocatorLevel >= 1){
                        FieldRelation fieldRelation = connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
                        if(checkFieldRelation(fieldRelation, predOfParamPassing, varStored)){
                            traversalResult.addMatchedCtxObjsOfParam(layerOneCtxObjs, 2);
                        }
                    }

                    SootMethod callerOfStatic = crtTrace.getStaticCallStackTop();
                    for (Node pred : predOfParamPassing) { // static
                        if (callerOfStatic != null && !callerOfStatic.equals(getMethodOfPointer(pred)))
                            continue; // skip suspicious caller of static method.
                        if (getMethodOfPointer(crtPtr).equals(getMethodOfPointer(pred)))
                            continue; // skip static recursive call.
                        TraversalStatus newPtrTrace = new TraversalStatus(pred, crtAllocatorLevel, crtCallStackLevel, null, crtTrace, FlowKind.STATIC_PARAMETER_PASSING);
                        traversalResult.addVisitedMethod(getMethodOfPointer(pred));
                        if (!visited.contains(newPtrTrace)) {
                            stack.push(newPtrTrace);
                        }
                    }
                } else {
                    // non-static
                    if (predOfParamPassing.stream().allMatch(pred -> checkIfThisCallOnArg(pred, crtPtr))) {
                        // all called by this variable.
                        for (Node pred : predOfParamPassing) {
                            if (falseParamPassingFlow(crtPtr, pred, obj)) continue;
                            List<Set<AllocNode>> ctxObjs = collectCtxObj(pred, obj);
                            if(ctxObjs.stream().anyMatch(s -> !s.isEmpty())){
                                TraversalStatus newPtrTrace = new TraversalStatus(pred, crtAllocatorLevel, crtCallStackLevel, crtTrace, FlowKind.THIS_PARAM_PASSING);
                                if (!visited.contains(newPtrTrace)) {
                                    stack.push(newPtrTrace);
                                    traversalResult.addVisitedMethod(getMethodOfPointer(pred));
                                }
                            }
                        }
                    }
                    else {

                        List<Set<AllocNode>> ctxObjs = collectCtxObj(crtPtr, obj);
                        for (int allocatorLayer = 0; allocatorLayer < ctxObjs.size(); allocatorLayer++) {
                            Set<AllocNode> ctxObjsOfLayer = ctxObjs.get(allocatorLayer);
                            if (ctxObjsOfLayer.isEmpty()) continue;
                            if (allocatorLayer >= maxAllocLayerToTrace) {
                                // match finished.
                                FieldRelation fieldRelation = connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
                                if(checkFieldRelation(fieldRelation, predOfParamPassing, varStored)){
                                    traversalResult.addMatchedCtxObjsOfParam(ctxObjsOfLayer, allocatorLayer);
                                    break;
                                }
                            }
                            else {
                                boolean toBreak = false;
                                if (allocatorLayer >= crtAllocatorLevel) {
                                    FieldRelation fieldRelation = connectedUnderLimitNumOfField(varStored, reachingPTS(crtPtr), obj);
                                    if(checkFieldRelation(fieldRelation, predOfParamPassing, varStored)){
                                        traversalResult.addMatchedCtxObjsOfParam(ctxObjsOfLayer, allocatorLayer);
                                        toBreak = true;
                                    }
                                }
                                // continue match more layer.
                                for (Node pred : predOfParamPassing) {
                                    if (falseParamPassingFlow(crtPtr, pred, ctxObjsOfLayer)) continue;
                                    TraversalStatus newPtrTrace = new TraversalStatus(pred, allocatorLayer, crtCallStackLevel + 1, crtTrace, FlowKind.PARAMETER_PASSING);
                                    if (!visited.contains(newPtrTrace)) {
                                        stack.push(newPtrTrace);
                                        traversalResult.addVisitedMethod(getMethodOfPointer(pred));
                                    }
                                }
                                if (toBreak) break;
                            }
                        }

                    }
                }
            }
        }

        return traversalResult;
    }


    private boolean checkIfThisCallOnArg(Node arg, Node param) {
        Set<Value> recvVar = methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) arg, (LocalVarNode) param);
        return recvVar.stream().allMatch(v -> {
            LocalVarNode recvVarNode = pag.findLocalVarNode(v);
            return checkIfThis(recvVarNode);
        });
    }



    private int getMaxAllocLayer(Node crtPtr, AllocNode obj) {
        Map<AllocNode, Integer> ctxs = getCtxObjsOf(obj);
        Set<AllocNode> ctxObjs = ctxs.keySet();
        Collection<Pair<Object, SootMethod>> usageCtxAndCallerPairs = methodCallDetail.usageCtxAndCallerOf(getMethodOfPointer(crtPtr));

        Set<AllocNode> matchCtxObjs = usageCtxAndCallerPairs.stream().map(p -> (AllocNode) (p.getFirst())).filter(ctxObjs::contains).collect(Collectors.toSet());
        return matchCtxObjs.stream().mapToInt(ctxs::get).min().orElse(-1);
    }

    private List<Set<AllocNode>> collectCtxObj(Node crtPtr, AllocNode obj) {
        Map<AllocNode, Integer> ctxs = getCtxObjsOf(obj);
        Set<AllocNode> ctxObjs = ctxs.keySet();
        SootMethod crtMethod = getMethodOfPointer(crtPtr);
        List<Set<AllocNode>> result = new ArrayList<>(this.maxCtxLayer + 1);
        for (int i = 0; i < this.maxCtxLayer + 1; i ++){
            result.add(Collections.emptySet());
        }
        int maxLayer = -1;
        for (AllocNode ctxObj : ctxObjs) {
            if(objToInvokedMethodsOn.containsKey(ctxObj) && objToInvokedMethodsOn.get(ctxObj).contains(crtMethod)){
                int layer = ctxs.get(ctxObj);
                maxLayer = Math.max(layer,maxLayer);
                if (result.get(layer).isEmpty()) {
                    result.set(layer, new HashSet<>());
                }
                result.get(layer).add(ctxObj);
            }
        }
        if(maxLayer == -1) return List.of();
        return result;
    }


    private Map<AllocNode, Integer> getCtxObjsOf(AllocNode obj) {
        return ctxObjsCache.computeIfAbsent(obj, k -> {
            Map<AllocNode, Integer> result = new HashMap<>();
            List<Set<AllocNode>> layerOfCtxObjs = new ArrayList<>();
            layerOfCtxObjs.add(new HashSet<>(Set.of(obj))); // index 0 for current obj.
            layerOfCtxObjs.add(new HashSet<>(objectAllocationGraphWithArr.getPredsOf(obj))); // index 1 for first allocators of current obj.

            for (int i = 2; i <= maxCtxLayer; i++) {
                layerOfCtxObjs.add(new HashSet<>(layerOfCtxObjs.get(i - 1).stream().map(objectAllocationGraphWithArr::getPredsOf).flatMap(Set::stream).collect(Collectors.toSet())));
            }
            Set<AllocNode> alreadyIn = new HashSet<>();
            for (int i = 0; i < layerOfCtxObjs.size(); i++) {
                Set<AllocNode> layerCtx = layerOfCtxObjs.get(i);
                for (AllocNode ctx : layerCtx) {
                    if (alreadyIn.contains(ctx)) continue;
                    alreadyIn.add(ctx);
                    result.put(ctx, i);
                }
            }
            return result;
        });
    }


    private boolean containComplexFlowForSpecificFlow(TraversalStatus crtTrace, FlowKind flowKind) {
        switch (flowKind) {
            case THIS_FIELD_STORE_AND_LOAD -> {
                int callLoadFreq = crtTrace.getVisitedFlowCounter(FlowKind.CALL_LOAD);
                int unwrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_LOAD);
                int wrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_STORE);
                return callLoadFreq + unwrappedFlowFreq + wrappedFlowFreq > 2;
            }
            case CALL_LOAD -> {
                int callLoadFreq = crtTrace.getVisitedFlowCounter(FlowKind.CALL_LOAD);
                int wrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_STORE);
                int unwrappedFlowFreq = crtTrace.getVisitedFlowCounter(FlowKind.FIELD_LOAD);
                return callLoadFreq + wrappedFlowFreq + unwrappedFlowFreq > 2;
            }
        }
        return false;
    }

    private Set<Node> getReturnVarOfThisMethodCall(Node predAkaThis, Node crtPtr) {
        Set<AllocNode> thisPts = reachingPTS(predAkaThis);
        Set<Node> preds = new HashSet<>();
        for (FlowEdge inEdge : VFGForField.getPredsOf(crtPtr)) {
            if(inEdge.flowKind().equals(FlowKind.RETURN)){
                SootMethod inMethod = getMethodOfPointer(inEdge.source());
                for (AllocNode thisObj : thisPts) {
                    if (objToInvokedMethodsOn.get(thisObj).contains(inMethod)) {
                        preds.add(inEdge.source());
                        break;
                    }
                }
            }
        }
        return preds;
    }

    private Set<Node> getFieldStoredVar(Node pred, Node crtPtr) {
        Set<Node> ptrStoredIn = new HashSet<>();
        Set<AllocNode> thisPts = reachingPTS(pred);
        AllocNode thisObj;


        for (FlowEdge edge : VFGForField.getPredsOf(crtPtr)) {
            if(edge.flowKind().equals(FlowKind.INSTANCE_LOAD)){
                ContextField instanceField = (ContextField) edge.source();

                if (thisPts.contains(instanceField.getBase()) && !VFGForField.getPredsOf(instanceField).isEmpty()) {
                    thisObj = instanceField.getBase();

                    for (FlowEdge inEdge : VFGForField.getPredsOf(instanceField)) {
                        SootMethod inMethod = getMethodOfPointer(inEdge.source());
                        if (objToInvokedMethodsOn.get(thisObj).contains(inMethod)) {
                            ptrStoredIn.add(inEdge.source());
                        }
                    }
                }
            }
        }
        return ptrStoredIn;
    }

    private boolean checkIfThis(Node node) {
        LocalVarNode thisVar = (LocalVarNode) pag.getMethodPAG(getMethodOfPointer(node)).nodeFactory().caseThis();
        boolean isThis = node.equals(thisVar);
        boolean isLocalAssignedFromThis = VFGForField.getPredsOf(node).stream().anyMatch(inEdge -> inEdge.source().equals(thisVar) && inEdge.flowKind().equals(FlowKind.LOCAL_ASSIGN));
        return isThis || isLocalAssignedFromThis;
    }


    private boolean falseParamPassingFlow(Node current, Node pred, Set<AllocNode> objs) {
        Set<Value> recvVars = methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) pred, (LocalVarNode) current);
        return recvVars.stream().noneMatch(v -> {
            LocalVarNode recvVarNode = pag.findLocalVarNode(v);
            return Util.haveOverlap(objs, reachingPTS(recvVarNode));
        });
    }
    private boolean falseParamPassingFlow(Node current, Node pred, AllocNode obj) {

        if (objToInvokedMethodsOn.containsKey(obj) && objToInvokedMethodsOn.get(obj).contains(getMethodOfPointer(current))) {
            Set<Value> recvVars = methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) pred, (LocalVarNode) current);
            return recvVars.stream().noneMatch(v -> {
                LocalVarNode recvVarNode = pag.findLocalVarNode(v);
                return reachingPTS(recvVarNode).contains(obj);
            });
        }else{
            SootMethod inMethod = getMethodOfPointer(current);
            if(inMethod.isStatic()){
                return false;
            }
            VarNode calleeThis = pag.getMethodPAG(inMethod).nodeFactory().caseThis();
            Set<Value> recvVars = methodCallDetail.getRecvValueOfArgAndParam((LocalVarNode) pred, (LocalVarNode) current);
            return recvVars.stream().noneMatch(v -> {
                LocalVarNode recvVar = pag.findLocalVarNode(v);
                return Util.haveOverlap(reachingPTS(calleeThis), reachingPTS(recvVar));
            });
        }
    }

    private boolean checkFieldRelation(FieldRelation fieldRelation, Set<Node> preds, LocalVarNode varStored){
        if(fieldRelation == FieldRelation.NONE) return false;
        if(fieldRelation == FieldRelation.SAME) return true;
        if(fieldRelation == FieldRelation.POINT_TO){

            Set<AllocNode> varStoredPTS = reachingPTS(varStored);
            List<Set<AllocNode>> predsFieldPTS = new ArrayList<>();
            for (Node pred : preds) {
                Set<AllocNode> predPts = reachingPTS(pred);
                Set<AllocNode> predFieldPTS = new HashSet<>(predPts.stream().map(fieldPointsToGraph::getFieldPointsTo).flatMap(Set::stream).collect(Collectors.toSet()));
                predFieldPTS.retainAll(varStoredPTS);
                predsFieldPTS.add(predFieldPTS);
            }
            for (int i = 0; i < predsFieldPTS.size(); i++) {
                for (int j = i + 1; j < predsFieldPTS.size(); j++) {
                    if (Collections.disjoint(predsFieldPTS.get(i), predsFieldPTS.get(j))) {
                        return true; // Found two sets with different elements
                    }
                }
            }
            return false;
        }
        if(fieldRelation == FieldRelation.POINT_FROM){
            if(preds.size() <= 1) return false;
            return true;
        }
        return false;
    }
    private FieldRelation connectedUnderLimitNumOfField(Node varStoredIn, Set<AllocNode> allocatorDepPTS, AllocNode obj){
        if(obj.getType() instanceof ArrayType) return FieldRelation.SAME;
        Set<AllocNode> inFlowPTS = reachingPTS(varStoredIn);
        if(Util.haveOverlap(inFlowPTS, allocatorDepPTS)) return FieldRelation.SAME;
        Set<AllocNode> allocFieldPTS = allocatorDepPTS.parallelStream().map(fieldPointsToGraph::getFieldPointsTo).flatMap(Set::stream).collect(Collectors.toSet());
        if(Util.haveOverlap(inFlowPTS, allocFieldPTS)) return FieldRelation.POINT_TO;
        Set<AllocNode> allcFieldFrom = allocatorDepPTS.parallelStream().map(fieldPointsToGraph::getFieldPointsFrom).flatMap(Set::stream).collect(Collectors.toSet());
        if(Util.haveOverlap(inFlowPTS, allcFieldFrom)) return FieldRelation.POINT_FROM;
        return FieldRelation.NONE;
    }

    private Set<AllocNode> reachingPTS(Node node){
        return ptrSetCache.ptsOf(node);
    }

    
    private SootMethod getMethodOfPointer(Node n){
        if(n instanceof LocalVarNode localVarNode){
            return localVarNode.getMethod();
        }
        throw new RuntimeException("Unexpected node type: " + n.getClass().getName());
    }

    private enum FieldRelation {
        NONE,
        POINT_TO,
        POINT_FROM,
        SAME
    }
}
