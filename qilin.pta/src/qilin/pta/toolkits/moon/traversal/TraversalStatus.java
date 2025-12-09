package qilin.pta.toolkits.moon.traversal;


import qilin.core.pag.LocalVarNode;
import qilin.core.pag.Node;
import qilin.pta.toolkits.moon.Graph.FlowKind;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class TraversalStatus {
    public Node pointer;
    public int allocatorLevel;
    public int callStackLevel;
    private Stack<SootMethod> staticCallStack;
    private Map<FlowKind, Integer> visitedFlowKinds;
    public boolean needCheckFieldRelationWhenMeetNew = true;
    public TraversalStatus(Node pointer, int allocatorLevel, int callStackLevel, Node retPtrOfStaticMethod, TraversalStatus predTrace, FlowKind flowKind) {
        this(pointer, allocatorLevel, callStackLevel, predTrace, flowKind);
        if(retPtrOfStaticMethod instanceof LocalVarNode localVarNode){
            if(staticCallStack == null){
                staticCallStack = new Stack<>();
            }
            staticCallStack.push(localVarNode.getMethod());
        }
    }


    public TraversalStatus(Node pointer, int allocatorLevel, int callStackLevel, TraversalStatus predTrace, FlowKind flowKind) {
        this(pointer, allocatorLevel, callStackLevel);
        if(this.visitedFlowKinds == null){
            visitedFlowKinds = new HashMap<>();
        }
        if(predTrace.visitedFlowKinds != null && !predTrace.visitedFlowKinds.isEmpty()){
            this.visitedFlowKinds.putAll(predTrace.visitedFlowKinds);
        }
        int i = this.visitedFlowKinds.computeIfAbsent(flowKind, __ -> 0);
        i += 1;
        this.visitedFlowKinds.put(flowKind, i);
        if(predTrace.staticCallStack != null && !predTrace.staticCallStack.isEmpty()){
            if(staticCallStack == null){
                staticCallStack = new Stack<>();
            }
            staticCallStack.addAll(predTrace.staticCallStack);
        }

        this.needCheckFieldRelationWhenMeetNew = predTrace.needCheckFieldRelationWhenMeetNew;
    }

    public TraversalStatus(Node pointer, int allocatorLevel, int callStackLevel) {
        this.pointer = pointer;
        this.allocatorLevel = allocatorLevel;
        this.callStackLevel = callStackLevel;
    }

    public SootMethod getStaticCallStackTop(){
        if(staticCallStack != null && !staticCallStack.isEmpty()){
            return staticCallStack.peek();
        }
        return null;
    }

    public int getVisitedFlowCounter(FlowKind flowKind){
        if(this.visitedFlowKinds == null) return 0;
        return this.visitedFlowKinds.getOrDefault(flowKind, 0);
    }


    @Override
    public int hashCode() {
        return Objects.hash(pointer, callStackLevel, allocatorLevel);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof TraversalStatus other){
            if(o == this) return true;
            return this.pointer.equals(other.pointer) &&
                    this.allocatorLevel == other.allocatorLevel &&
                    this.callStackLevel == other.callStackLevel;
        }else {
            return false;
        }
    }
}
