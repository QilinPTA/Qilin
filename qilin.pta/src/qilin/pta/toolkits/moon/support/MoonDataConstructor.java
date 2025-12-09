package qilin.pta.toolkits.moon.support;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.moon.Graph.FieldPointsToGraph;
import qilin.pta.toolkits.moon.Graph.FlowKind;
import qilin.pta.toolkits.moon.Graph.VFG;
import qilin.util.CallDetails;
import qilin.util.PTAUtils;
import qilin.util.collect.multimap.ConcurrentMultiMap;
import qilin.util.collect.multimap.MultiMap;
import soot.*;
import soot.jimple.*;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.NumberedString;
import soot.util.queue.QueueReader;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class MoonDataConstructor {

    private final PTA pta;
    private final PAG pag;
    private final VFG VFGForField;
    private final VFG VFGForObj;
    private final OAG oag;
    private final FieldRecorder fieldRecorder = new FieldRecorder();
    private final PtrSetCache ptrSetCache;
    private final MultiMap<AllocNode, SootMethod> objToInvokedMethods = new ConcurrentMultiMap<>();
    private final MultiMap<SootMethod, AllocNode> methodToRecvObjs = new ConcurrentMultiMap<>();
    private final Set<LocalVarNode> allocatedVars = ConcurrentHashMap.newKeySet();
    private final FieldFlowRecorder fieldFlowRecorder;
    private final KeyTypeCollector keyTypeCollector;
    private final ContainerCollector containerCollector;
    private final FieldPointsToGraph fieldPointsToGraph;
    public MoonDataConstructor(PTA pta) {
        this.pta = pta;
        this.pag = pta.getPag();
        this.ptrSetCache = new PtrSetCache(pta);
        this.VFGForField = new VFG();
        this.VFGForObj = new VFG();
        this.fieldFlowRecorder = new FieldFlowRecorder(pta, fieldRecorder, VFGForObj, objToInvokedMethods);
        this.oag = new OAG(pta);
        this.keyTypeCollector = new KeyTypeCollector(fieldRecorder);
        this.containerCollector = new ContainerCollector(pta, fieldRecorder, fieldFlowRecorder, keyTypeCollector);
        fieldPointsToGraph = new FieldPointsToGraph(pta, ptrSetCache);

    }

    public record MoonDataStructure(OAG oag, VFG vfgForField, VFG vfgForObj,
                                    FieldRecorder fieldRecorder, FieldFlowRecorder fieldFlowRecorder,
                                    MultiMap<SootMethod, AllocNode> mthToRecvObj, KeyTypeCollector keyTypeCollector,
                                    MultiMap<AllocNode, SootMethod> objToIvkMtds, Set<LocalVarNode> allocVars, Set<AllocNode> containers,
                                    PtrSetCache ptrSetCache, FieldPointsToGraph fieldPointsToGraph, PAG pag) {}
    public MoonDataStructure analyze(){
        List<Runnable> runnableItems = List.of(
                this.oag::build,
                this::buildOag,
                this::addExtraFlow,
                this::buildObjAndInvokeToCallee,
                this::initOpenTypes,
                this::initFieldReachability,
                this::initContainerFilter
        );

        for (Runnable runnableItem : runnableItems) {
            runnableItem.run();
        }
        return new MoonDataStructure(oag, VFGForField, VFGForObj, fieldRecorder, fieldFlowRecorder, methodToRecvObjs, keyTypeCollector,
                objToInvokedMethods, allocatedVars, containerCollector.containerToFields.keySet(), ptrSetCache, fieldPointsToGraph, pag);
    }


    private void initOpenTypes(){
        keyTypeCollector.run(pag.getAllocNodes());
    }
    private void initFieldReachability(){
        this.fieldFlowRecorder.build(this.keyTypeCollector);
    }
    private void initContainerFilter(){
        containerCollector.collect();
    }


    private void buildOag(){
        Map<ValNode, Set<ValNode>> simples = pta.getPag().getSimple();

        simples.keySet()
                .parallelStream()
                .forEach(source -> {
            Set<ValNode> targets = simples.get(source);
            if (localVarBase(source)) {
                targets.forEach(target -> {
                    if (localVarBase(target)) {
                        LocalVarNode toNode = fetchVar(target);
                        LocalVarNode fromNode = fetchVar(source);
                        LocalVarNode srcTmp = fetchLocalVar(source);
                        if (srcTmp.isInterProcSource() && srcTmp.isReturn()) { // source of THROW and RETURN
                            VFGForField.addSimpleFlowEdge(FlowKind.RETURN, fromNode, toNode);
                        } else {
                            if (fetchLocalVar(target).isInterProcTarget()) { // target of THIS_PASSING and PARAM_PASSING
                                if (!fetchLocalVar(target).isThis()) {
                                    VFGForField.addSimpleFlowEdge(FlowKind.PARAMETER_PASSING, fromNode, toNode);
                                }
                            } else {
                                VFGForField.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, fromNode, toNode);
                            }
                        }
                    } else if (target instanceof ContextField ctxField) {
                        LocalVarNode varNode = fetchVar(source);
                        VFGForField.addSimpleFlowEdge(FlowKind.INSTANCE_STORE, varNode, ctxField);

                    }
                });
            } else if (source instanceof ContextField ctxField) {
                targets.forEach(t -> {
                    assert localVarBase(t);
                    LocalVarNode varNode = fetchVar(t);
                    VFGForField.addSimpleFlowEdge(FlowKind.INSTANCE_LOAD, ctxField, varNode);
                });
            }
        });


        StreamSupport.stream(pta.getCallGraph().spliterator(), true) // true for parallel
                .forEach(callEdge -> {
            Stmt callsite = callEdge.srcStmt();
            SootMethod caller = callEdge.src();
            if (caller != null) {
                SootMethod callee = callEdge.tgt();
                if (!callee.isStatic()) {
                    MethodNodeFactory calleeNodeFactory = pta.getPag().getMethodPAG(callee).nodeFactory();
                    LocalVarNode thisVar = (LocalVarNode) calleeNodeFactory.caseThis();
                    InvokeExpr invokeExpr = callsite.getInvokeExpr();
                    Value base = null;
                    if (invokeExpr instanceof InstanceInvokeExpr instanceInvokeExpr) {
                        base = instanceInvokeExpr.getBase();
                    }
                    if (base != null) {
                        LocalVarNode fromNode = (LocalVarNode) pta.getPag().findValNode(base);
                        VFGForField.addSimpleFlowEdge(FlowKind.THIS_PASSING, fromNode, thisVar);
                    }
                }
            }
        });
    }


    private void addExtraFlow(){
        pta.getNakedReachableMethods()
                .parallelStream()
                .forEach(this::buildInternalWithInline);
    }



    protected void buildInternalWithInline(SootMethod method) {
        MethodPAG methodPAG = pag.getMethodPAG(method);
        MethodNodeFactory factory = methodPAG.nodeFactory();
        Set<FieldRefNode> stores = new HashSet<>();
        Set<FieldRefNode> loads = new HashSet<>();
        Set<Node> thisAlias = new HashSet<>();
        thisAlias.add(factory.caseThis());
        VFGForObj.recordThisVar(factory.caseThis());
        QueueReader<Node> reader = methodPAG.getInternalReader().clone();
        while (reader.hasNext()) {
            Node from = reader.next(), to = reader.next();
            if (from instanceof LocalVarNode) {
                if (to instanceof LocalVarNode) {
                    if(thisAlias.contains(from)){
                        thisAlias.add(to);
                    }
                    VFGForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, from, to);
                } else if (to instanceof FieldRefNode fr) {
                    stores.add(fr);
                    VFGForObj.addFieldFlowEdge(FlowKind.FIELD_STORE, from, fr.getBase(), fr.getField());
                    VFGForField.addFieldFlowEdge(FlowKind.FIELD_STORE, from, fr.getBase(), fr.getField());
                    fieldRecorder.putStore((LocalVarNode)fr.getBase(), fr.getField(), (LocalVarNode) from);
                }else if(to instanceof GlobalVarNode globalVarTo){
                    // local-global
                    Object variable = globalVarTo.getVariable();
                    if(variable instanceof SootField){
                        VFGForField.addSimpleFlowEdge(FlowKind.STATIC_STORE, from, globalVarTo);
                    }else if(!(variable instanceof ClassConstant) && !(variable instanceof StringConstant)){
                        throw new RuntimeException("Unknown GlobalVarNode");
                    }
                }else{
                    throw new RuntimeException("Unknown Node Type");
                }

            }else if (from instanceof AllocNode) {
                    if (to instanceof LocalVarNode localVarTo) {
                        VFGForObj.addSimpleFlowEdge(FlowKind.NEW, from, to);
                        allocatedVars.add(localVarTo);
                        VFGForField.addSimpleFlowEdge(FlowKind.NEW, from, to);

                    }else if(to instanceof GlobalVarNode globalVarTo){
                        VFGForField.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, from, globalVarTo);
                    }else {
                        throw new RuntimeException("Unknown to Node Type");
                    }
            }else if (from instanceof FieldRefNode fr) {
                    loads.add(fr);
                    VFGForObj.addFieldFlowEdge(FlowKind.FIELD_LOAD, fr.getBase(), to, fr.getField());
                    VFGForField.addFieldFlowEdge(FlowKind.FIELD_LOAD, fr.getBase(), to, fr.getField());
                fieldRecorder.putLoad((LocalVarNode)fr.getBase(), fr.getField(), (LocalVarNode) to);
            }else if(from instanceof GlobalVarNode globalVarFrom){
                // global-local
                    Object variable = globalVarFrom.getVariable();
                    if(variable instanceof SootField){
                        VFGForField.addSimpleFlowEdge(FlowKind.STATIC_LOAD, globalVarFrom, to);
                    }else if(!(variable instanceof ClassConstant) && !(variable instanceof StringConstant)){
                        throw new RuntimeException("Unknown GlobalVarNode");
                    }
            }
        }

        doHandleField(thisAlias, stores, true);
        doHandleField(thisAlias, loads, false);



        // handle call statements.
        for (final Unit u : methodPAG.getInvokeStmts()) {
            final Stmt s = (Stmt) u;
            InvokeExpr invokeExpr = s.getInvokeExpr();
            int numArgs = invokeExpr.getArgCount();
            Value[] args = new Value[numArgs];
            for (int i = 0; i < numArgs; i++) {
                Value arg = invokeExpr.getArg(i);
                if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant)
                    continue;
                args[i] = arg;
            }
            LocalVarNode retDest = null;
            if (s instanceof AssignStmt) {
                Value dest = ((AssignStmt) s).getLeftOp();
                if (dest.getType() instanceof RefLikeType) {
                    retDest = pag.findLocalVarNode(dest);
                }
            }
            if (invokeExpr instanceof InstanceInvokeExpr instanceInvokeExpr) {
                LocalVarNode receiver = pag.findLocalVarNode(instanceInvokeExpr.getBase());
                if (instanceInvokeExpr instanceof SpecialInvokeExpr specialInvokeExpr) {
                    inlineForMatchObjTraversal(s, method, specialInvokeExpr.getMethod(), false);
                    if (retDest != null) {
                        VFGForField.addSimpleFlowEdge(FlowKind.CALL_LOAD, receiver, retDest);
                    }
                    CallDetails.v().addInvokeExpr(receiver, instanceInvokeExpr);
                } else {
                    if (retDest != null) {
                        VFGForObj.addSimpleFlowEdge(FlowKind.CALL_LOAD, receiver, retDest);
                        VFGForField.addSimpleFlowEdge(FlowKind.CALL_LOAD, receiver, retDest);
                        CallDetails.v().addInvokeExpr(receiver, instanceInvokeExpr);
                    }

                    for (int i = 0; i < numArgs; i++) {
                        if (args[i] == null) {
                            continue;
                        }
                        ValNode argNode = pag.findValNode(args[i]);
                        if (argNode instanceof LocalVarNode) {
                            VFGForObj.addSimpleFlowEdge(FlowKind.CALL_STORE, argNode, receiver);
                        }
                    }
                }

                for (int i = 0; i < numArgs; i++) {
                    if (args[i] == null) {
                        continue;
                    }
                    ValNode argNode = pag.findValNode(args[i]);
                    if (argNode instanceof LocalVarNode) {
                        VFGForField.addSimpleFlowEdge(FlowKind.CALL_STORE, argNode, receiver);
                        CallDetails.v().addInvokeExpr(receiver, instanceInvokeExpr);
                    }
                }


            } else {
                if (invokeExpr instanceof StaticInvokeExpr sie) {
                    inlineForMatchObjTraversal(s, method, sie.getMethod(), true);
                }
            }
        }

        // handle parameters.
        for (int i = 0; i < method.getParameterCount(); ++i) {
            if (method.getParameterType(i) instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(method.getParameterType(i))) {
                LocalVarNode param = (LocalVarNode) factory.caseParm(i);
                VFGForObj.addSimpleFlowEdge(FlowKind.PARAMETER_PASSING, param, param);
            }
        }

        if(!method.isStatic()){
            VarNode thisVar = factory.caseThis();
            VFGForObj.addSimpleFlowEdge(FlowKind.PARAMETER_PASSING, thisVar, thisVar);
        }

        // handle returns
        if (method.getReturnType() instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(method.getReturnType())) {
            VFGForObj.addSimpleFlowEdge(FlowKind.RETURN, factory.caseRet(), factory.caseRet());
        }
    }




    private void doHandleField(Set<Node> thisAliases, Set<FieldRefNode> operations, boolean isStore){
        for (FieldRefNode operation : operations) {
            LocalVarNode baseVar = (LocalVarNode) operation.getBase();
            SparkField field = operation.getField();
            boolean isNonThisBase = !thisAliases.contains(baseVar);
            for (AllocNode heap : ptrSetCache.ptsOf(baseVar)) {
                fieldRecorder.recordObjToField(heap, field);
                if(isNonThisBase){
                    if(isStore){
                        fieldFlowRecorder.objToNonThisFieldStore.put(heap, field, baseVar);
                    }else{
                        fieldFlowRecorder.objToNonThisFieldLoad.put(heap, field, baseVar);
                        fieldFlowRecorder.hasNonThisFieldLoad.add(field);
                    }
                }
            }

        }
    }

    private void inlineForMatchObjTraversal(Stmt invokeStmt, SootMethod caller, SootMethod inlinedMethod, boolean isStaticCall) {
        VFGForObj.recordInlineMethod(inlinedMethod, caller);
        InvokeExpr ie = invokeStmt.getInvokeExpr();
        int numArgs = ie.getArgCount();
        Value[] args = new Value[numArgs];
        for (int i = 0; i < numArgs; i++) {
            Value arg = ie.getArg(i);
            if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant)
                continue;
            args[i] = arg;
        }
        LocalVarNode retDest = null;
        if (invokeStmt instanceof AssignStmt) {
            Value dest = ((AssignStmt) invokeStmt).getLeftOp();
            if (dest.getType() instanceof RefLikeType) {
                retDest = pag.findLocalVarNode(dest);
            }
        }
        LocalVarNode receiver = null;
        if (ie instanceof InstanceInvokeExpr iie) {
            receiver = pag.findLocalVarNode(iie.getBase());
        }
        MethodPAG mpag = pag.getMethodPAG(inlinedMethod);
        MethodNodeFactory nodeFactory = mpag.nodeFactory();
        if (numArgs != inlinedMethod.getParameterCount()) {
            return;
        }
        // handle parameters
        for (int i = 0; i < inlinedMethod.getParameterCount(); ++i) {
            if (args[i] != null && inlinedMethod.getParameterType(i) instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(inlinedMethod.getParameterType(i))) {
                LocalVarNode param = (LocalVarNode) nodeFactory.caseParm(i);
                ValNode argVal = pag.findValNode(args[i]);
                if (argVal instanceof LocalVarNode argNode) {
                    VFGForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, argNode, param);
                }
            }
        }
        // handle return node
        if (retDest != null && inlinedMethod.getReturnType() instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(inlinedMethod.getReturnType())) {
            VFGForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, nodeFactory.caseRet(), retDest);
        }
        // handle this node
        if (receiver != null) {
            VFGForObj.addSimpleFlowEdge(FlowKind.LOCAL_ASSIGN, receiver, nodeFactory.caseThis());
        }
    }





    private boolean localVarBase(ValNode valNode) {
        if (valNode instanceof ContextVarNode cvn) {
            return cvn.base() instanceof LocalVarNode;
        } else {
            return valNode instanceof LocalVarNode;
        }
    }

    private LocalVarNode fetchLocalVar(ValNode valNode) {
        if (valNode instanceof ContextVarNode cvn) {
            if (cvn.base() instanceof LocalVarNode) {
                return (LocalVarNode) cvn.base();
            }
        } else if (valNode instanceof LocalVarNode) {
            return (LocalVarNode) valNode;
        }
        throw new RuntimeException("Not a local var: " + valNode);
    }

    private LocalVarNode fetchVar(ValNode valNode) {
        if (valNode instanceof ContextVarNode cvn) {
            VarNode base = cvn.base();
            if (base instanceof LocalVarNode lvn) {
                return lvn;
            }
        } else if (valNode instanceof LocalVarNode lvn) {
            return lvn;
        }
        throw new RuntimeException("Not a local var: " + valNode);
    }


    private void buildObjAndInvokeToCallee(){
        CallGraph callgraph = pta.getCallGraph();
        // collect virtual callsites.
        Set<VirtualCallSite> vcallsites = new HashSet<>();
        for (Edge edge : callgraph) {
            SootMethod tgtM = edge.tgt();
            if (tgtM.isStatic() || tgtM.isPhantom()) {
                continue;
            }
            final Stmt s = edge.srcStmt();
            InvokeExpr ie = s.getInvokeExpr();
            if (ie instanceof InstanceInvokeExpr iie) {
                LocalVarNode receiver = pag.findLocalVarNode(iie.getBase());
                NumberedString subSig = iie.getMethodRef().getSubSignature();
                VirtualCallSite virtualCallSite = new VirtualCallSite(receiver, s, edge.src(), iie, subSig, Edge.ieToKind(iie));
                vcallsites.add(virtualCallSite);
            } else {
                throw new RuntimeException("ie could not be of " + ie.getClass());
            }
        }
        vcallsites.parallelStream().forEach(vcallsite -> {
            // foreach virtualcallsite, we build mapping from their receiver objects.
            InstanceInvokeExpr iie = vcallsite.iie();
            LocalVarNode receiver = pag.findLocalVarNode(iie.getBase());
            for (AllocNode heap : pta.reachingObjects(receiver).toCIPointsToSet().toCollection()) {
                QueueReader<SootMethod> reader = PTAUtils.dispatch(heap.getType(), vcallsite);
                while (reader.hasNext()) {
                    SootMethod tgtM = reader.next();
                    objToInvokedMethods.put(heap, tgtM);
                    methodToRecvObjs.put(tgtM, heap);
                }
            }
        });
    }

}
