package qilin.pta.toolkits.zipper.flowgraph;

import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.pta.toolkits.common.OAG;
import qilin.util.PTAUtils;
import qilin.util.collect.SetFactory;
import qilin.util.collect.multimap.ConcurrentMultiMap;
import qilin.util.collect.multimap.MultiHashMap;
import qilin.util.collect.multimap.MultiMap;
import qilin.util.graph.SCCMergedGraph;
import qilin.util.graph.TopologicalSorter;
import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.util.queue.QueueReader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A tailored Object Allocation Graph (OAG) for Zipper PTA.
 */
public class ZOAG extends OAG {

    private final MultiMap<AllocNode, AllocNode> obj2Allocatees = new ConcurrentMultiMap<>();

    private final MultiMap<Type, AllocNode> type2Allocatees = new ConcurrentMultiMap<>();
    private final MultiMap<Type, AllocNode> type2Objs = new ConcurrentMultiMap<>();
    public ZOAG(PTA prepta){
        super(prepta);
        for (AllocNode obj : this.pta.getPag().getAllocNodes()) {
            type2Objs.put(obj.getType(), obj);
        }
        build();
        computeAllocatees();
    }

    public MultiMap<Type, AllocNode> getType2Objs() {
        return type2Objs;
    }

    private void computeAllocatees(){
        // compute allocatees of objects
        ZSCCMergedGraph<AllocNode> mg = new ZSCCMergedGraph<>(this);
        TopologicalSorter<ZMergedNode<AllocNode>> sorter = new TopologicalSorter<>();
        SetFactory<AllocNode> canonicalizer = new SetFactory<>();
        for (ZMergedNode<AllocNode> node : sorter.sort(mg, true)) {
            Set<AllocNode> allocattes = canonicalizer.get(getAllocatees(node, mg));
            for (AllocNode obj : node.getContent()) {
                obj2Allocatees.putAll(obj, allocattes);
            }
        }



        // compute allocatees of types
        for (Type type : type2Objs.keySet()) {
            var objs = type2Objs.get(type);
            Set<AllocNode> allocatees = new HashSet<>();
            for (AllocNode obj : objs) {
                allocatees.addAll(getAllocateesOf(obj));
            }
            type2Allocatees.putAll(type, canonicalizer.get(allocatees));
        }
    }


    public Set<AllocNode> getAllocateesOf(Type type) {
        return type2Allocatees.get(type);
    }

    private Set<AllocNode> getAllocatees(ZMergedNode<AllocNode> node, ZSCCMergedGraph<AllocNode> mg) {
        Set<AllocNode> allocatees = new HashSet<>();
        for (ZMergedNode<AllocNode> n : mg.succsOf(node)) {
            // direct allocatees
            allocatees.addAll(n.getContent());
            // indirect allocatees
            AllocNode o = n.getContent().get(0);
            allocatees.addAll(getAllocateesOf(o));
        }

        AllocNode obj = node.getContent().get(0);
        if(node.getContent().size() > 1 || succsOf(obj).contains(obj)){ // self-loop
            // The merged node is a true SCC
            allocatees.addAll(node.getContent());
        }

        return allocatees;
    }

    private Set<AllocNode> getAllocateesOf(AllocNode obj) {
        return obj2Allocatees.get(obj);
    }



    @Override
    protected void buildOAG() {
        Map<LocalVarNode, Set<AllocNode>> pts = PTAUtils.calcStaticThisPTS(this.pta);
        for (SootMethod method : this.pta.getNakedReachableMethods()) {
            if (method.isPhantom()) {
                continue;
            }
            MethodPAG srcmpag = pta.getPag().getMethodPAG(method);
            MethodNodeFactory srcnf = srcmpag.nodeFactory();
            LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
            QueueReader<Node> reader = srcmpag.getInternalReader().clone();
            while (reader.hasNext()) {
                qilin.core.pag.Node from = reader.next(), to = reader.next();
                if (from instanceof AllocNode tgt) {
                    if (PTAUtils.isFakeMainMethod(method)) {
                        // special treatment for fake main
                        AllocNode src = pta.getRootNode();
                        addEdgeWithFilter(src, tgt);
                    } else if (method.isStatic()) {
                        pts.getOrDefault(thisRef, Collections.emptySet()).forEach(src -> {
                            addEdgeWithFilter(src, tgt);
                        });
                    } else {
                        PointsToSet thisPts = pta.reachingObjects(thisRef).toCIPointsToSet();
                        for (Iterator<AllocNode> it = thisPts.iterator(); it.hasNext(); ) {
                            AllocNode src = it.next();
                            addEdgeWithFilter(src, tgt);
                        }
                    }
                }
            }
        }
    }


    private final Type sbType = RefType.v("java.lang.StringBuilder");
    private final Type strType = RefType.v("java.lang.String");
    private final Type sbuffType = RefType.v("java.lang.StringBuffer");
    private final Type throwType = RefType.v("java.lang.Throwable");
    private void addEdgeWithFilter(AllocNode from, AllocNode to) {
        if (from.getType() instanceof ArrayType) return;
        if(from instanceof ConstantNode || to instanceof  ConstantNode) return;
        Type ftype = from.getType();
        Type ttype = to.getType();
        if (ftype.equals(strType) || ttype.equals(strType)) return;
        if (ftype.equals(sbType) || ttype.equals(sbType)) return;
        if (ftype.equals(sbuffType) || ttype.equals(sbuffType)) return;

        var hirerarchy = PTAScene.v().getOrMakeFastHierarchy();
        if( hirerarchy.canStoreType(ftype, throwType)|| hirerarchy.canStoreType(ttype, throwType)) return;

        addEdge(from, to);
    }
}
