package qilin.pta.toolkits.moon.ObjCollection;


import qilin.core.pag.*;
import qilin.pta.toolkits.moon.Graph.FieldEdge;
import qilin.pta.toolkits.moon.Graph.FlowEdge;
import qilin.pta.toolkits.moon.Graph.FlowKind;
import qilin.pta.toolkits.moon.Graph.VFG;
import qilin.pta.toolkits.moon.support.FieldFlowRecorder;
import qilin.pta.toolkits.moon.support.KeyTypeCollector;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.util.collect.multimap.MultiMap;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.jimple.spark.pag.SparkField;


import java.util.*;

public class PartialChecker {
    private static final int fieldTypeThreshold = 3;
    private final VFG VFGForObj;
    private final PAG pag;
    private final KeyTypeCollector keyTypeCollector;
    private final FieldFlowRecorder fieldFlowRecorder;
    private final MultiMap<SootMethod, AllocNode> methodToInvokeObjs;
    private final Set<AllocNode> objMetParamOfAllocMethod = new HashSet<>();
    public PartialChecker(MoonDataConstructor.MoonDataStructure graphBuilder){
        this.keyTypeCollector = graphBuilder.keyTypeCollector();
        this.fieldFlowRecorder = graphBuilder.fieldFlowRecorder();
        this.VFGForObj = graphBuilder.vfgForObj();
        this.methodToInvokeObjs = graphBuilder.mthToRecvObj();
        this.pag = graphBuilder.pag();
    }

    public boolean addMetParamObj(AllocNode obj){
        return objMetParamOfAllocMethod.add(obj);
    }

    public boolean check(AllocNode obj){
        SootMethod inMethod = obj.getMethod();
        if(inMethod == null) return false;

        if(canReturnOut(obj)){
            if(objMetParamOfAllocMethod.contains(obj) || inMethod.isStatic()){
                return true;
            }
        }

        VarNode thisVar = pag.getMethodPAG(inMethod).nodeFactory().caseThis();
        Set<SootMethod> traversalMethods = new HashSet<>();
        Set<Node> thisAlias = getAliasOf(thisVar, traversalMethods);
        Deque<CheckTrace> stack = new ArrayDeque<>();
        Set<CheckTrace> visited = new HashSet<>();
        stack.push(new CheckTrace(obj, CheckStatus.Allocation));

        Set<Type> fieldTypes = new HashSet<>();
        while(!stack.isEmpty()){
            CheckTrace crtTrace = stack.pop();
            if(visited.contains(crtTrace)){
                continue;
            }
            visited.add(crtTrace);
            Node crtNode = crtTrace.getNode();
            CheckStatus crtState = crtTrace.getState();


            for (FlowEdge outEdge : VFGForObj.getSuccsOf(crtNode)) {
                FlowKind flowKind = outEdge.flowKind();
                CheckStatus nextState = moveToNode(crtState, flowKind, true);
                if(nextState == CheckStatus.UnDef) continue;
                if(flowKind == FlowKind.FIELD_STORE && thisAlias.contains(outEdge.target())){
                    FieldEdge fieldEdge = (FieldEdge) outEdge;
                    fieldTypes.add(fieldEdge.field().getType());
                    if(isContextAwareField(inMethod, fieldEdge.field())){
                        return fieldTypes.size() <= fieldTypeThreshold;
                    }
                }else{
                    if(flowKind != FlowKind.FIELD_STORE || keyTypeCollector.isConcernedType(((FieldEdge)outEdge).field().getType())){
                        if(outEdge.target() instanceof LocalVarNode localVarNode) {
                        if (traversalMethods.contains(localVarNode.getMethod())) {
                            if(outEdge instanceof FieldEdge && flowKind == FlowKind.FIELD_STORE){
                                fieldTypes.add(((FieldEdge) outEdge).field().getType());
                            }
                            stack.push(new CheckTrace(localVarNode, nextState));
                        }
                        }else{
                            stack.push(new CheckTrace(outEdge.target(), nextState));
                        }
                    }
                }
            }
            for (FlowEdge inEdge : VFGForObj.getPredsOf(crtNode)) {
                FlowKind flowKind = inEdge.flowKind();
                CheckStatus nextState = moveToNode(crtState, flowKind, false);
                if(nextState == CheckStatus.UnDef) continue;
                if(flowKind == FlowKind.FIELD_LOAD && thisAlias.contains(inEdge.source())){
                    FieldEdge fieldEdge = (FieldEdge) inEdge;
                    fieldTypes.add(fieldEdge.field().getType());
                    if(isContextAwareField(inMethod, fieldEdge.field())) {
                        return fieldTypes.size() <= fieldTypeThreshold;
                    }
                }else{
                    if(flowKind != FlowKind.FIELD_LOAD || keyTypeCollector.isConcernedType(((FieldEdge)inEdge).field().getType())){
                        if(inEdge.source() instanceof LocalVarNode localVarNode) {
                        if (traversalMethods.contains(localVarNode.getMethod())) {
                            if(inEdge instanceof FieldEdge && flowKind == FlowKind.FIELD_LOAD){
                                fieldTypes.add(((FieldEdge) inEdge).field().getType());
                            }
                            stack.push(new CheckTrace(localVarNode, nextState));
                        }
                        }else{
                            stack.push(new CheckTrace(inEdge.source(), nextState));
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isContextAwareField(SootMethod method, SparkField field){
        if(!keyTypeCollector.isConcernedType(field.getType())) return false;
        Set<AllocNode> recvObjs = methodToInvokeObjs.get(method);
        for (AllocNode recvObj : recvObjs) {
            if(recvObj.getType() instanceof RefType){
                if(fieldFlowRecorder.isConnceredField(recvObj, field)) return true;
            }
        }
        return false;
    }

    private CheckStatus moveToNode(CheckStatus crtState, FlowKind flowKind, boolean forward){
        switch (crtState){
            case Allocation -> {
                if(forward && flowKind == FlowKind.NEW){
                    return CheckStatus.DirectVar;
                }
            }

            case DirectVar -> {
                if(forward && flowKind == FlowKind.LOCAL_ASSIGN){
                    return CheckStatus.DirectVar;
                }else if(forward && flowKind == FlowKind.FIELD_STORE){
                    return CheckStatus.StoredInVar;
                }
            }

            case StoredInVar -> {
                if(forward){
                    // Forward
                    if(flowKind == FlowKind.FIELD_STORE){
                        return CheckStatus.StoredInVar;
                    }else if(flowKind == FlowKind.LOCAL_ASSIGN){
                        return CheckStatus.StoredInVar;
                    }
                }
                else{
                    // backForward
                    if(flowKind == FlowKind.LOCAL_ASSIGN){
                        return CheckStatus.StoredInVar;
                    }else if(flowKind == FlowKind.FIELD_LOAD){
                        return CheckStatus.StoredInVar;
                    }
                }
            }
        }
        return CheckStatus.UnDef;
    }


    private boolean canReturnOut(AllocNode obj){
        Stack<Node> stack = new Stack<>();
        for (FlowEdge edge : VFGForObj.getSuccsOf(obj)) {
            stack.push(edge.target());
        }
        for (FlowEdge edge : VFGForObj.getPredsOf(obj)) {
            stack.push(edge.source());
        }
        Set<Node> visited = new HashSet<>();
        while(!stack.isEmpty()){
            Node crtNode = stack.pop();
            visited.add(crtNode);
            for (FlowEdge outEdge : VFGForObj.getSuccsOf(crtNode)) {
                if(outEdge.target() instanceof LocalVarNode localVarNode && localVarNode.isReturn() && localVarNode.getMethod().equals(obj.getMethod())){
                    return true;
                }
                if(outEdge.flowKind() == FlowKind.LOCAL_ASSIGN && !visited.contains(outEdge.target())){
                    stack.push(outEdge.target());
                }
            }

        }
        return false;

    }


    private Set<Node> getAliasOf(Node node, Set<SootMethod> traversalMethods){
        Stack<Node> stack = new Stack<>();
        for (FlowEdge edge : VFGForObj.getSuccsOf(node)) {
            stack.push(edge.target());
        }
        for (FlowEdge inEdge : VFGForObj.getPredsOf(node)) {
            stack.push(inEdge.source());
        }
        Set<Node> visited = new HashSet<>();
        while(!stack.isEmpty()){
            Node crtNode = stack.pop();
            visited.add(crtNode);
            if(crtNode instanceof LocalVarNode localVarNode){
                SootMethod method = localVarNode.getMethod();
                if(method != null){
                    traversalMethods.add(method);
                }

            }
            for (FlowEdge outEdge : VFGForObj.getSuccsOf(crtNode)) {
                if(outEdge.flowKind() == FlowKind.LOCAL_ASSIGN && !visited.contains(outEdge.target())){
                    stack.push(outEdge.target());
                }
            }
        }
        return visited;
    }
}
