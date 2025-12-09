package qilin.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import qilin.core.pag.*;
import qilin.util.collect.twokeymap.TwoKeyHashMap;
import qilin.util.collect.twokeymap.TwoKeyMap;
import qilin.util.collect.twokeymultimap.TwoKeyMultiHashMap;
import qilin.util.collect.twokeymultimap.TwoKeyMultiMap;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;

import java.util.Collection;
import java.util.Set;


import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.util.HashMultiMap;
import soot.util.MultiMap;


import java.util.*;

// MethodCallDetail keeps track of method call relationships and contexts, mainly for MOON
public class CallDetails {
    public static final Object STATIC_OBJ_CTX = new Object();
    private final MultiMap<SootMethod, Pair<Object, SootMethod>> calleeToCtxAndCaller = new HashMultiMap<>();

    private final TwoKeyMap<SootMethod, SootMethod, Set<Object>> callerCalleeToRecvObj = new TwoKeyHashMap<>();
    private final MultiMap<SootMethod, SootMethod> callerToCallee = new HashMultiMap<>();
    private final MultiMap<SootMethod, SootMethod> calleeToCaller = new HashMultiMap<>();

    private final MultiMap<LocalVarNode, InstanceInvokeExpr> recvToInvokeExprs = new HashMultiMap<>();


    private final TwoKeyMultiMap<LocalVarNode, LocalVarNode, Value> argToParamToRecvValue = new TwoKeyMultiHashMap<>();

    private final TwoKeyMap<AllocNode, InvokeExpr, SootMethod> calleeCache = new TwoKeyHashMap<>();
    private static final CallDetails instance = new CallDetails();
    public static CallDetails v() {
        return instance;
    }



    private boolean enabled = false;
    private boolean initialized = false;
    public void enable() {
        enabled = true;
        initialized = true;
    }
    public void disable(){
        enabled = false;
    }
    private CallDetails() {
    }

    public void addCalleeToCtxAndCaller(SootMethod callee, Object ctx, SootMethod caller) {
        if(!enabled) return;
        callerToCallee.put(caller, callee);
        calleeToCaller.put(callee, caller);
        if(ctx instanceof AllocNode || ctx.equals(STATIC_OBJ_CTX)) {
            if(ctx instanceof ContextAllocNode){
                ctx = ((ContextAllocNode) ctx).base();
            }
            calleeToCtxAndCaller.put(callee, new Pair<>(ctx, caller));
            if(!callerCalleeToRecvObj.containsKey(caller, callee)){
                callerCalleeToRecvObj.put(caller, callee, new HashSet<>());
            }
            Set<Object> recvObjs = callerCalleeToRecvObj.get(caller, callee);
            recvObjs.add(ctx);
        }
        else
            throw new RuntimeException("Unknown context type");
    }

    private void checkInitialized(){
        if(!initialized) throw new RuntimeException("MethodCallDetail is not initialized");
    }
    public Collection<Pair<Object, SootMethod>> usageCtxAndCallerOf(SootMethod callee) {
        checkInitialized();
        return calleeToCtxAndCaller.get(callee);
    }


    public void addInvokeExpr(LocalVarNode receiver, InstanceInvokeExpr invokeExpr){
        recvToInvokeExprs.put(receiver, invokeExpr);
    }

    public void addArgToParamToRecvValue(Node arg, Node param, Stmt callStmt){
        if(!enabled) return;
        Value recvValue = getValue(callStmt);
        if(recvValue == null){
            return;
        }
        if(arg instanceof ContextVarNode contextVarNode){
            arg = contextVarNode.base();
        }
        if(param instanceof ContextVarNode contextVarNode){
            param = contextVarNode.base();
        }
        if(!argToParamToRecvValue.containsKey((LocalVarNode) arg, (LocalVarNode) param) || argToParamToRecvValue.get((LocalVarNode) arg, (LocalVarNode) param).size() < 2){
            argToParamToRecvValue.put((LocalVarNode) arg, (LocalVarNode) param, recvValue);
        }

    }

    private @Nullable Value getValue(Stmt callStmt) {
        InvokeExpr invokeExpr;
        if(callStmt instanceof JInvokeStmt invokeStmt){
            invokeExpr = invokeStmt.getInvokeExpr();
        }else if(callStmt instanceof JAssignStmt assignStmt){
            invokeExpr = assignStmt.getInvokeExpr();
        }else {
            throw new RuntimeException("Unsupported callStmt type.");
        }

        Value v = null;
        if(invokeExpr instanceof JVirtualInvokeExpr virtualInvokeExpr){
            v = virtualInvokeExpr.getBase();
        }else if(invokeExpr instanceof JSpecialInvokeExpr specialInvokeExpr){
            v = specialInvokeExpr.getBase();
        }else if(invokeExpr instanceof JInterfaceInvokeExpr interfaceInvokeExpr){
            v = interfaceInvokeExpr.getBase();
        }
        return v;
    }

    public Set<Value> getRecvValueOfArgAndParam(LocalVarNode arg, LocalVarNode param){
        return argToParamToRecvValue.get(arg, param);
    }

}
