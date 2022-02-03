package qilin.pta.toolkits.zipper.pta;

import com.google.common.collect.Streams;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.core.sets.P2SetVisitor;
import qilin.core.sets.PointsToSetInternal;
import qilin.pta.toolkits.zipper.Global;
import qilin.util.Pair;
import qilin.util.Triple;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.util.queue.QueueReader;

import java.util.*;

public class WrapperedPointsToAnalysis implements PointsToAnalysis {
    private final PTA prePTA;
    private Set<AllocNode> allObjs;
    private Set<SootMethod> reachableMethods;
    private Map<Type, Type> directSuperType;
    private Map<Type, Set<AllocNode>> typeObjects;
    private Map<Type, Set<SootMethod>> typeMethods;
    private List<Pair<VarNode, VarNode>> thisAssign;
    private Map<AllocNode, Set<AllocNode>> fieldPointsto;
    private Map<AllocNode, Set<SootMethod>> obj2invokedMethods;
    private Map<AllocNode, VarNode> objAssignedTo;
    private Map<SootMethod, Set<SootMethod>> caller2callees;
    private Map<SootMethod, Set<AllocNode>> allocatedHeaps;
    private Map<VarNode, SootMethod> var2declaringMethod;
    private Map<SootMethod, Set<VarNode>> declaringMethod2var;
    private Map<VarNode, Set<VarNode>> receiver2return;
    private int totalPTSSize;

    public WrapperedPointsToAnalysis(PTA prePTA) {
        this.prePTA = prePTA;
        this.init();
    }

    public Set<AllocNode> allObjects() {
        return this.allObjs;
    }

    public Set<AllocNode> pointsToSetOf(final VarNode var) {
        Set<AllocNode> ret = new HashSet<>();
        PointsToSetInternal pts = (PointsToSetInternal) prePTA.reachingObjects(var);
        pts.mapToCIPointsToSet().forall(new P2SetVisitor() {
            @Override
            public void visit(Node n) {
                ret.add((AllocNode) n);
            }
        });
        return ret;
    }

    @Override
    public int pointsToSetSizeOf(VarNode var) {
        return pointsToSetOf(var).size();
    }

    @Override
    public int totalPointsToSetSize() {
        return totalPTSSize;
    }

    @Override
    public Set<VarNode> variablesDeclaredIn(SootMethod method) {
        return declaringMethod2var.getOrDefault(method, Collections.emptySet());
    }

    public Set<AllocNode> objectsAllocatedIn(final SootMethod method) {
        return this.allocatedHeaps.getOrDefault(method, Collections.emptySet());
    }

    public Set<SootMethod> calleesOf(final SootMethod method) {
        return caller2callees.getOrDefault(method, Collections.emptySet());
    }

    @Override
    public Set<SootMethod> reachableMethods() {
        return reachableMethods;
    }

    public Set<SootMethod> methodsInvokedOn(final AllocNode obj) {
        return this.obj2invokedMethods.getOrDefault(obj, Collections.emptySet());
    }

    public Set<VarNode> returnToVariablesOf(final VarNode recv) {
        return receiver2return.getOrDefault(recv, Collections.emptySet());
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
        return null;
    }

    private VarNode fetchVar(ValNode valNode) {
        if (valNode instanceof ContextVarNode cvn) {
            return cvn.base();
        } else if (valNode instanceof VarNode) {
            return (VarNode) valNode;
        }
        return null;
    }

    public Iterator<Pair<VarNode, VarNode>> localAssignIterator() {
        return new Iterator<>() {
            private final Iterator<Pair<VarNode, VarNode>> assignIter = prePTA.getPag().getSimple().entrySet().stream()
                    .filter(e -> localVarBase(e.getKey()) && !fetchLocalVar(e.getKey()).isInterProcSource())
                    .flatMap(e -> e.getValue().stream()
                            .filter(tgt -> localVarBase(tgt) && !fetchLocalVar(tgt).isInterProcTarget())
                            .map(tgt -> new Pair<>(fetchVar(tgt), fetchVar(e.getKey())))
                    )
                    .iterator();

            @Override
            public boolean hasNext() {
                return this.assignIter.hasNext();
            }

            @Override
            public Pair<VarNode, VarNode> next() {
                if (this.assignIter.hasNext()) {
                    return this.assignIter.next();
                }
                throw new NoSuchElementException();
            }
        };
    }

    public Iterator<Pair<VarNode, VarNode>> interProceduralAssignIterator() {
        return new Iterator<>() {
            private final Iterator<Pair<VarNode, VarNode>> assignIter = Streams.concat(
                            prePTA.getPag().getSimple().entrySet().stream()
                                    .filter(e -> localVarBase(e.getKey()) && fetchLocalVar(e.getKey()).isInterProcSource())
                                    .flatMap(e -> e.getValue().stream()
                                            .map(tgt -> new Pair<>(fetchVar(tgt), fetchVar(e.getKey())))
                                    )
                            ,
                            prePTA.getPag().getSimpleInv().entrySet().stream()
                                    .filter(e -> localVarBase(e.getKey()) && fetchLocalVar(e.getKey()).isInterProcTarget() && !fetchLocalVar(e.getKey()).isThis())
                                    .flatMap(e -> e.getValue().stream()
                                            .map(src -> new Pair<>(fetchVar(e.getKey()), fetchVar(src)))
                                    )
                    )
                    .iterator();

            @Override
            public boolean hasNext() {
                return this.assignIter.hasNext();
            }

            @Override
            public Pair<VarNode, VarNode> next() {
                if (this.assignIter.hasNext()) {
                    return this.assignIter.next();
                }
                throw new NoSuchElementException();
            }
        };
    }

    public Iterator<Triple<VarNode, AllocNode, SparkField>> instanceLoadIterator() {
        return new Iterator<>() {

            private final Iterator<Triple<VarNode, AllocNode, SparkField>> loadIter = prePTA.getPag().getSimple().entrySet().stream()
                    .filter(e -> e.getKey() instanceof ContextField)
                    .flatMap(e -> e.getValue().stream()
                            .map(tgt -> new Triple<>(fetchVar(tgt), ((ContextField) e.getKey()).getBase(), ((ContextField) e.getKey()).getField()))
                    )
                    .iterator();

            @Override
            public boolean hasNext() {
                return this.loadIter.hasNext();
            }

            @Override
            public Triple<VarNode, AllocNode, SparkField> next() {
                if (this.loadIter.hasNext()) {
                    final Triple<VarNode, AllocNode, SparkField> load = this.loadIter.next();
                    final VarNode to = load.getFirst();
                    final AllocNode base = load.getSecond();
                    final SparkField field = load.getThird();
                    return new Triple<>(to, base, field);
                }
                throw new NoSuchElementException();
            }
        };
    }

    public Iterator<Triple<AllocNode, SparkField, VarNode>> instanceStoreIterator() {
        return new Iterator<>() {
            private final Iterator<Triple<AllocNode, SparkField, VarNode>> storeIter = prePTA.getPag().getSimpleInv().entrySet().stream()
                    .filter(e -> e.getKey() instanceof ContextField)
                    .flatMap(e -> e.getValue().stream().map(src -> new Triple<>(((ContextField) e.getKey()).getBase(), ((ContextField) e.getKey()).getField(), fetchVar(src)))
                    ).iterator();

            @Override
            public boolean hasNext() {
                return this.storeIter.hasNext();
            }

            @Override
            public Triple<AllocNode, SparkField, VarNode> next() {
                if (this.storeIter.hasNext()) {
                    final Triple<AllocNode, SparkField, VarNode> store = this.storeIter.next();
                    final AllocNode base = store.getFirst();
                    final SparkField field = store.getSecond();
                    final VarNode from = store.getThird();
                    return new Triple<>(base, field, from);
                }
                throw new NoSuchElementException();
            }
        };
    }

    public Iterator<Pair<VarNode, VarNode>> thisAssignIterator() {
        return this.thisAssign.iterator();
    }

    public Type declaringTypeOf(final SootMethod method) {
        return method.getDeclaringClass().getType();
    }

    public Type directSuperTypeOf(final Type type) {
        return this.directSuperType.get(type);
    }

    public Set<AllocNode> objectsOfType(final Type type) {
        return this.typeObjects.get(type);
    }

    @Override
    public Set<VarNode> getParameters(SootMethod m) {
        MethodNodeFactory mthdNF = prePTA.getPag().getMethodPAG(m).nodeFactory();
        Set<VarNode> ret = new HashSet<>();
        for (int i = 0; i < m.getParameterCount(); ++i) {
            if (m.getParameterType(i) instanceof RefType) {
                VarNode param = mthdNF.caseParm(i);
                ret.add(param);
            }
        }
        return ret;
    }

    @Override
    public Set<VarNode> getRetVars(SootMethod m) {
        MethodNodeFactory mthdNF = prePTA.getPag().getMethodPAG(m).nodeFactory();
        if (m.getReturnType() instanceof RefType) {
            VarNode ret = mthdNF.caseRet();
            return Collections.singleton(ret);
        }
        return Collections.emptySet();
    }

    @Override
    public VarNode getThis(SootMethod m) {
        MethodNodeFactory mthdNF = prePTA.getPag().getMethodPAG(m).nodeFactory();
        return mthdNF.caseThis();
    }

    public Set<SootMethod> methodsInvokedOn(final Type type) {
        return this.typeMethods.get(type);
    }

    private void init() {
        this.receiver2return = new HashMap<>();

        for (SootMethod sig : prePTA.getNakedReachableMethods()) {
            prePTA.getPag().getMethodPAG(sig).invokeStmts.forEach(s -> {
                if (!(s instanceof AssignStmt)) {
                    return;
                }
                InvokeExpr invo = ((Stmt) s).getInvokeExpr();
                if (!(invo instanceof InstanceInvokeExpr)) {
                    return;
                }
                final VarNode recv = (VarNode) prePTA.getPag().findValNode(((InstanceInvokeExpr) invo).getBase());
                Value toNode = ((AssignStmt) s).getLeftOp();
                if (!(toNode.getType() instanceof RefType)) {
                    return;
                }
                final VarNode to = (VarNode) prePTA.getPag().findValNode(toNode);
                receiver2return.computeIfAbsent(recv, k -> new HashSet<>()).add(to);
            });
        }

        totalPTSSize = 0;
        this.buildPointsToSet();
        this.computeAllocatedObjects();
        this.buildCallees();
        this.buildMethodsInvokedOnObjects();
        this.buildVarDeclaringMethods();
        this.buildObjectAssignedVariables();
        this.buildFieldPointsToSet();
        this.buildDirectSuperType();
        this.typeObjects = new HashMap<>();
        this.typeMethods = new HashMap<>();

        this.allObjects().forEach(obj -> {
            final Type type = obj.getType();
            this.typeObjects.putIfAbsent(type, new HashSet<>());
            this.typeObjects.get(type).add(obj);
            this.typeMethods.putIfAbsent(type, new HashSet<>());
            this.typeMethods.get(type).addAll(this.methodsInvokedOn(obj));
        });
    }

    private void buildPointsToSet() {
        this.allObjs = new HashSet<>();
        for (ValNode var : prePTA.getPag().getValNodeNumberer()) {
            if (var instanceof VarNode varNode) {
                Collection<AllocNode> pts = pointsToSetOf(varNode);
                totalPTSSize += pts.size();
                this.allObjs.addAll(pts);
            }
        }
    }

    public SootMethod containingMethodOf(final AllocNode obj) {
        return obj.getMethod();
    }

    public SootMethod declaringMethodOf(final VarNode var) {
        return this.var2declaringMethod.get(var);
    }

    public VarNode assignedVarOf(final AllocNode obj) {
        return this.objAssignedTo.get(obj);
    }

    private void computeAllocatedObjects() {
        this.allocatedHeaps = new HashMap<>();
        for (AllocNode alloc : prePTA.getPag().getAllocNodeNumberer()) {
            if (alloc.getMethod() == null) {//TODO special objects?
                continue;
            }
            SootMethod method = alloc.getMethod();
            allocatedHeaps.computeIfAbsent(method, k -> new HashSet<>()).add(alloc);
        }
    }

    private void buildCallees() {
        this.reachableMethods = new HashSet<>();
        this.thisAssign = new LinkedList<>();
        this.caller2callees = new HashMap<>();
        final Map<Unit, SootMethod> callIn = new HashMap<>();
        final Map<Unit, Object> callBase = new HashMap<>();

        for (final MethodOrMethodContext momc : prePTA.getReachableMethods()) {
            SootMethod sig = momc.method();
            prePTA.getPag().getMethodPAG(sig).invokeStmts.forEach(s -> {
                callIn.put(s, sig);
            });
        }

        prePTA.getCgb().getReceiverToSitesMap().forEach((recv, sites) -> {
            sites.forEach(site -> {
                callBase.put(site.getUnit(), recv.getVariable());
            });
        });

        prePTA.getCallGraph().forEach(e -> {
            Unit callsiteStr = e.srcUnit();
            SootMethod caller = callIn.get(callsiteStr);
            if (caller != null) {
                SootMethod callee = e.tgt();
                caller2callees.computeIfAbsent(caller, k -> new HashSet<>()).add(callee);
                this.reachableMethods.add(caller);
                this.reachableMethods.add(callee);
                if (!callee.isStatic()) {
                    MethodNodeFactory calleeNF = prePTA.getPag().getMethodPAG(callee).nodeFactory();
                    VarNode thisVar = calleeNF.caseThis();
                    Object baseVarStr = callBase.get(callsiteStr);
                    if (baseVarStr != null) {
                        VarNode baseVar = (VarNode) prePTA.getPag().findValNode(baseVarStr);
                        this.thisAssign.add(new Pair<>(thisVar, baseVar));
                    }
                }
            } else if (Global.isDebug()) {
                System.out.println("Null caller of: " + callsiteStr);
            }
        });

    }

    private void buildMethodsInvokedOnObjects() {
        this.obj2invokedMethods = new HashMap<>();
        reachableMethods.stream().filter(m -> !m.isStatic()).forEach(instMtd -> {
            MethodNodeFactory mthdNF = prePTA.getPag().getMethodPAG(instMtd).nodeFactory();
            VarNode thisVar = mthdNF.caseThis();
            this.pointsToSetOf(thisVar).forEach(obj -> {
                obj2invokedMethods.computeIfAbsent(obj, k -> new HashSet<>()).add(instMtd);
            });
        });
    }

    private void buildVarDeclaringMethods() {
        this.var2declaringMethod = new HashMap<>();
        this.declaringMethod2var = new HashMap<>();
        for (ValNode valnode : prePTA.getPag().getValNodeNumberer()) {
            if (!(valnode instanceof LocalVarNode lvn)) {
                continue;
            }
            SootMethod inMethod = lvn.getMethod();
            var2declaringMethod.put(lvn, inMethod);
            declaringMethod2var.computeIfAbsent(inMethod, k -> new HashSet<>()).add(lvn);
        }
    }

    private void buildObjectAssignedVariables() {
        this.objAssignedTo = new HashMap<>();
        for (final MethodOrMethodContext momc : prePTA.getReachableMethods()) {
            MethodPAG mpag = prePTA.getPag().getMethodPAG(momc.method());
            QueueReader<Node> reader = mpag.getInternalReader().clone();
            while (reader.hasNext()) {
                Node src = reader.next();
                Node tgt = reader.next();
                if (src instanceof AllocNode) {
                    objAssignedTo.put((AllocNode) src, (VarNode) tgt);
                }
            }
        }
    }

    private void buildFieldPointsToSet() {
        this.fieldPointsto = new HashMap<>();
        prePTA.getPag().getContextFields().forEach(contextField -> {
            AllocNode base = contextField.getBase();
            contextField.getP2Set().mapToCIPointsToSet().forall(new P2SetVisitor() {
                @Override
                public void visit(Node n) {
                    fieldPointsto.computeIfAbsent(base, k -> new HashSet<>()).add((AllocNode) n);
                }
            });
        });
    }

    private void buildDirectSuperType() {
        this.directSuperType = new HashMap<>();
        PTAScene.v().getClasses().forEach(c -> {
            if (!c.hasSuperclass()) {
                return;
            }
            Type type = c.getType();
            Type superType = c.getSuperclass().getType();
            this.directSuperType.put(type, superType);
        });
    }
}
