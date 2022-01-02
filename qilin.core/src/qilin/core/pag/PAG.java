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

package qilin.core.pag;

import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.PointsToAnalysis;
import qilin.core.natives.NativeMethodDriver;
import qilin.core.reflection.NopReflectionModel;
import qilin.core.reflection.ReflectionModel;
import qilin.core.reflection.TamiflexModel;
import qilin.parm.heapabst.HeapAbstractor;
import soot.*;
import soot.jimple.ClassConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.util.ArrayNumberer;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public class PAG {
    // ========================= context-sensitive nodes =================================
    protected final Map<VarNode, Map<Context, ContextVarNode>> contextVarNodeMap;
    protected final Map<AllocNode, Map<Context, ContextAllocNode>> contextAllocNodeMap;
    protected final Map<SootMethod, Map<Context, MethodOrMethodContext>> contextMethodMap;
    protected final Map<MethodPAG, Set<Context>> addedContexts;
    protected final Map<SparkField, Map<Context, ContextField>> contextFieldMap;

    // ========================= ir to Node ==============================================
    protected final Map<Object, AllocNode> valToAllocNode;
    protected final Map<Object, ValNode> valToValNode;
    protected final Map<SootMethod, MethodPAG> methodToPag;
    protected final Set<SootField> globals;
    protected final Set<Local> locals;

    // ==========================data=========================
    protected final ArrayNumberer<AllocNode> allocNodeNumberer;
    protected final ArrayNumberer<ValNode> valNodeNumberer;
    protected final ArrayNumberer<FieldRefNode> fieldRefNodeNumberer;

    // ==========================parms==============================
    private int maxFinishNumber = 0;
    // ==========================outer objects==============================
    protected final ChunkedQueue<Node> edgeQueue;
    protected final Map<ValNode, Set<ValNode>> simple;
    protected final Map<ValNode, Set<ValNode>> simpleInv;
    protected final Map<FieldRefNode, Set<VarNode>> load;
    protected final Map<VarNode, Set<FieldRefNode>> loadInv;
    protected final Map<AllocNode, Set<VarNode>> alloc;
    protected final Map<VarNode, Set<AllocNode>> allocInv;
    protected final Map<VarNode, Set<FieldRefNode>> store;
    protected final Map<FieldRefNode, Set<VarNode>> storeInv;

    protected final NativeMethodDriver nativeDriver;
    protected final ReflectionModel reflectionModel;

    protected final PTA pta;

    public PAG(PTA pta) {
        this.pta = pta;
        this.nativeDriver = new NativeMethodDriver();
        this.reflectionModel = createReflectionModel();

        this.contextVarNodeMap = new HashMap<>(16000);
        this.contextAllocNodeMap = new HashMap<>(6000);
        this.contextMethodMap = new HashMap<>(6000);
        this.addedContexts = new HashMap<>();
        this.contextFieldMap = new HashMap<>(6000);

        this.valToAllocNode = new HashMap<>(10000);
        this.valToValNode = new HashMap<>(100000);
        this.methodToPag = new HashMap<>();
        this.globals = new HashSet<>(100000);
        this.locals = new HashSet<>(100000);

        this.allocNodeNumberer = new ArrayNumberer<>();
        this.valNodeNumberer = new ArrayNumberer<>();
        this.fieldRefNodeNumberer = new ArrayNumberer<>();

        this.edgeQueue = new ChunkedQueue<>();
        this.simple = new HashMap<>();
        this.simpleInv = new HashMap<>();
        this.load = new HashMap<>();
        this.loadInv = new HashMap<>();
        this.alloc = new HashMap<>();
        this.allocInv = new HashMap<>();
        this.store = new HashMap<>();
        this.storeInv = new HashMap<>();
    }

    public void dumpPagStructureSize() {
        System.out.println("#globals: " + globals.size());
        System.out.println("#locals: " + locals.size());
        System.out.println("#allocNodeNumberer: " + allocNodeNumberer.size());
        System.out.println("#fieldRefNodeNumberer: " + fieldRefNodeNumberer.size());
    }

    public HeapAbstractor heapAbstractor() {
        return pta.heapAbstractor();
    }

    public int nextFinishNumber() {
        return ++maxFinishNumber;
    }

    protected ReflectionModel createReflectionModel() {
        ReflectionModel model;
        if (CoreConfig.v().getAppConfig().REFLECTION_LOG != null && CoreConfig.v().getAppConfig().REFLECTION_LOG.length() > 0) {
            model = new TamiflexModel();
        } else {
            model = new NopReflectionModel();
        }
        return model;
    }

    // ========================getters and setters=========================

    public ArrayNumberer<AllocNode> getAllocNodeNumberer() {
        return allocNodeNumberer;
    }

    public ArrayNumberer<FieldRefNode> getFieldRefNodeNumberer() {
        return fieldRefNodeNumberer;
    }

    public ArrayNumberer<ValNode> getValNodeNumberer() {
        return valNodeNumberer;
    }

    public Map<AllocNode, Set<VarNode>> getAlloc() {
        return alloc;
    }

    public Map<ValNode, Set<ValNode>> getSimple() {
        return simple;
    }

    public Map<ValNode, Set<ValNode>> getSimpleInv() {
        return simpleInv;
    }

    public Map<FieldRefNode, Set<VarNode>> getLoad() {
        return load;
    }

    public Map<FieldRefNode, Set<VarNode>> getStoreInv() {
        return storeInv;
    }

    public AllocNode getAllocNode(Object val) {
        return valToAllocNode.get(val);
    }

    public Map<MethodPAG, Set<Context>> getMethod2ContextsMap() {
        return addedContexts;
    }

    public PTA getPta() {
        return this.pta;
    }

    public MethodPAG getMethodPAG(SootMethod m) {
        return methodToPag.computeIfAbsent(m, k -> new MethodPAG(this, m));
    }

    public Collection<ContextField> getContextFields() {
        return contextFieldMap.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toSet());
    }

    public Map<VarNode, Map<Context, ContextVarNode>> getContextVarNodeMap() {
        return contextVarNodeMap;
    }

    public Map<AllocNode, Map<Context, ContextAllocNode>> getContextAllocNodeMap() {
        return contextAllocNodeMap;
    }

    public Map<SootMethod, Map<Context, MethodOrMethodContext>> getContextMethodMap() {
        return contextMethodMap;
    }

    public Map<SparkField, Map<Context, ContextField>> getContextFieldVarNodeMap() {
        return contextFieldMap;
    }

    public ContextField makeContextField(Context context, FieldValNode fieldValNode) {
        SparkField field = fieldValNode.getField();
        Map<Context, ContextField> ctx2field = contextFieldMap.computeIfAbsent(field, k -> new HashMap<>());
        return ctx2field.computeIfAbsent(context, k -> new ContextField(this, context, field));
    }

    public Collection<VarNode> getVarNodes(Local local) {
        Map<?, ContextVarNode> subMap = contextVarNodeMap.get(findLocalVarNode(local));
        if (subMap == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(subMap.values());
    }

    // ===============================read data==========================
    public QueueReader<Node> edgeReader() {
        return edgeQueue.reader();
    }

    public Collection<AllocNode> getAllocNodes() {
        return valToAllocNode.values();
    }

    public Set<SootField> getGlobalPointers() {
        return globals;
    }

    public Set<Local> getLocalPointers() {
        return locals;
    }

    // =======================add edge===============================
    protected <K, V> boolean addToMap(Map<K, Set<V>> m, K key, V value) {
        Set<V> valueList = m.computeIfAbsent(key, k -> new HashSet<>(4));
        return valueList.add(value);
    }

    public boolean addAllocEdge(AllocNode from, VarNode to) {
        if (addToMap(alloc, from, to)) {
            addToMap(allocInv, to, from);
            return true;
        }
        return false;
    }

    public boolean addSimpleEdge(ValNode from, ValNode to) {
        if (addToMap(simple, from, to)) {
            addToMap(simpleInv, to, from);
            return true;
        }
        return false;
    }

    public boolean addStoreEdge(VarNode from, FieldRefNode to) {
        if (addToMap(storeInv, to, from)) {
            addToMap(store, from, to);
            return true;
        }
        return false;
    }

    public boolean addLoadEdge(FieldRefNode from, VarNode to) {
        if (addToMap(load, from, to)) {
            addToMap(loadInv, to, from);
            return true;
        }
        return false;
    }

    public void addGlobalPAGEdge(Node from, Node to) {
        from = pta.parameterize(from, pta.emptyContext());
        to = pta.parameterize(to, pta.emptyContext());
        addEdge(from, to);
    }

    /**
     * Adds an edge to the graph, returning false if it was already there.
     */
    public final void addEdge(Node from, Node to) {
        if (addEdgeIntenal(from, to)) {
            edgeQueue.add(from);
            edgeQueue.add(to);
        }
    }

    private boolean addEdgeIntenal(Node from, Node to) {
        if (from instanceof ValNode) {
            if (to instanceof ValNode) {
                return addSimpleEdge((ValNode) from, (ValNode) to);
            } else {
                return addStoreEdge((VarNode) from, (FieldRefNode) to);
            }
        } else if (from instanceof FieldRefNode) {
            return addLoadEdge((FieldRefNode) from, (VarNode) to);
        } else {
            AllocNode heap = (AllocNode) from;
            return addAllocEdge(heap, (VarNode) to);
        }
    }

    // ======================lookups===========================
    protected <K, V> Set<V> lookup(Map<K, Set<V>> m, K key) {
        Set<V> valueList = m.get(key);
        if (valueList == null)
            return Collections.emptySet();
        return valueList;
    }

    public Set<VarNode> allocLookup(AllocNode key) {
        return lookup(alloc, key);
    }

    public Set<AllocNode> allocInvLookup(VarNode key) {
        return lookup(allocInv, key);
    }

    public Set<ValNode> simpleLookup(ValNode key) {
        return lookup(simple, key);
    }

    public Set<ValNode> simpleInvLookup(ValNode key) {
        return lookup(simpleInv, key);
    }

    public Set<FieldRefNode> loadInvLookup(VarNode key) {
        return lookup(loadInv, key);
    }

    public Set<VarNode> loadLookup(FieldRefNode key) {
        return lookup(load, key);
    }

    public Set<FieldRefNode> storeLookup(VarNode key) {
        return lookup(store, key);
    }

    public Set<VarNode> storeInvLookup(FieldRefNode key) {
        return lookup(storeInv, key);
    }

    // ===================find nodes==============================

    /**
     * Finds the GlobalVarNode for the variable value, or returns null.
     */
    public GlobalVarNode findGlobalVarNode(Object value) {
        return (GlobalVarNode) findValNode(value);
    }

    /**
     * Finds the LocalVarNode for the variable value, or returns null.
     */
    public LocalVarNode findLocalVarNode(Object value) {
        ValNode ret = findValNode(value);
        if (ret instanceof LocalVarNode) {
            return (LocalVarNode) ret;
        }
        return null;
    }

    /**
     * Finds the ValNode for the variable value, or returns null.
     */
    public ValNode findValNode(Object value) {
        return valToValNode.get(value);
    }

    public AllocNode findAllocNode(Object obj) {
        return valToAllocNode.get(obj);
    }

    /**
     * Finds the ContextVarNode for base variable value and context context, or
     * returns null.
     */
    public ContextVarNode findContextVarNode(Local baseValue, Context context) {
        Map<Context, ContextVarNode> contextMap = contextVarNodeMap.get(findLocalVarNode(baseValue));
        return contextMap == null ? null : contextMap.get(context);
    }

    // ==========================create nodes==================================
    public AllocNode makeAllocNode(Object newExpr, Type type, SootMethod m) {
        AllocNode ret = valToAllocNode.get(newExpr);
        if (ret == null) {
            valToAllocNode.put(newExpr, ret = new AllocNode(this, newExpr, type, m));
        } else if (!(ret.getType().equals(type))) {
            throw new RuntimeException(
                    "NewExpr " + newExpr + " of type " + type + " previously had type " + ret.getType());
        }
        return ret;
    }

    public AllocNode makeStringConstantNode(StringConstant sc) {
        if (!CoreConfig.v().getPtaConfig().stringConstants) {
            StringConstant mergedConstant = StringConstant.v(PointsToAnalysis.STRING_NODE);
            return valToAllocNode.computeIfAbsent(mergedConstant, k -> new StringConstantNode(this, mergedConstant));
        }
        return valToAllocNode.computeIfAbsent(sc, k -> new StringConstantNode(this, sc));
    }

    public AllocNode makeClassConstantNode(ClassConstant cc) {
        return valToAllocNode.computeIfAbsent(cc, k -> new ClassConstantNode(this, cc));
    }

    /**
     * Finds or creates the GlobalVarNode for the variable value, of type type.
     */
    public GlobalVarNode makeGlobalVarNode(Object value, Type type) {
        GlobalVarNode ret = (GlobalVarNode) valToValNode.get(value);
        if (ret == null) {
            valToValNode.put(value, ret = new GlobalVarNode(this, value, type));
            if (value instanceof SootField) {
                globals.add((SootField) value);
            }
        } else if (!(ret.getType().equals(type))) {
            throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + ret.getType());
        }
        return ret;
    }

    /**
     * Finds or creates the LocalVarNode for the variable value, of type type.
     */
    public LocalVarNode makeLocalVarNode(Object value, Type type, SootMethod method) {
        LocalVarNode ret = (LocalVarNode) valToValNode.get(value);
        if (ret == null) {
            valToValNode.put(value, ret = new LocalVarNode(this, value, type, method));
            if (value instanceof Local local) {
                if (local.getNumber() == 0) {
                    PTAScene.v().getLocalNumberer().add(local);
                }
                locals.add(local);
            }
        } else if (!(ret.getType().equals(type))) {
            throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + ret.getType());
        }
        return ret;
    }

    public LocalVarNode makeInvokeStmtThrowVarNode(Stmt invoke, SootMethod method) {
        return makeLocalVarNode(invoke, RefType.v("java.lang.Throwable"), method);
    }

    /**
     * Finds or creates the FieldVarNode for the Java field or array element.
     * Treat Java field and array element as normal local variable.
     */
    public FieldValNode makeFieldValNode(SparkField field) {
        return (FieldValNode) valToValNode.computeIfAbsent(field, k -> new FieldValNode(this, field));
    }

    /**
     * Finds or creates the FieldRefNode for base variable base and field field, of
     * type type.
     */
    public FieldRefNode makeFieldRefNode(VarNode base, SparkField field) {
        FieldRefNode ret = base.dot(field);
        if (ret == null) {
            ret = new FieldRefNode(this, base, field);
        }
        return ret;
    }

    /**
     * Finds or creates the ContextVarNode for base variable base and context.
     */
    public ContextVarNode makeContextVarNode(VarNode base, Context context) {
        Map<Context, ContextVarNode> contextMap = contextVarNodeMap.computeIfAbsent(base, k1 -> new HashMap<>());
        return contextMap.computeIfAbsent(context, k -> new ContextVarNode(this, base, context));
    }

    /**
     * Finds or creates the ContextAllocNode for base alloc site and context.
     */
    public ContextAllocNode makeContextAllocNode(AllocNode allocNode, Context context) {
        Map<Context, ContextAllocNode> contextMap = contextAllocNodeMap.computeIfAbsent(allocNode, k1 -> new HashMap<>());
        return contextMap.computeIfAbsent(context, k -> new ContextAllocNode(this, allocNode, context));
    }

    /**
     * Finds or creates the ContextMethod for method and context.
     */
    public MethodOrMethodContext makeContextMethod(Context context, SootMethod method) {
        Map<Context, MethodOrMethodContext> contextMap = contextMethodMap.computeIfAbsent(method, k1 -> new HashMap<>());
        return contextMap.computeIfAbsent(context, k -> new ContextMethod(method, context));
    }
}