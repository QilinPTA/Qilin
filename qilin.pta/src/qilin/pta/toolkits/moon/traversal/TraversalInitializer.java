package qilin.pta.toolkits.moon.traversal;

import qilin.core.pag.AllocNode;
import qilin.core.pag.ConstantNode;
import qilin.core.pag.ContextAllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.pta.toolkits.moon.support.FieldRecorder;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.support.PtrSetCache;
import qilin.util.collect.multimap.ConcurrentMultiMap;
import qilin.util.collect.multimap.MultiMap;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TraversalInitializer {

    private final MoonDataConstructor.MoonDataStructure moonData;
    private final int objCtxLen;
    public TraversalInitializer(MoonDataConstructor.MoonDataStructure moonData, int objCtxLen){
        this.moonData = moonData;
        this.objCtxLen = objCtxLen;
    }
    public MultiMap<AllocNode, LocalVarNode> initializeObjToVarMap() {
        Set<LocalVarNode> containerVars = moonData.vfgForField().getContainerVars();
        Set<AllocNode> containerObjs = moonData.containers();
        MultiMap<AllocNode, LocalVarNode> containerObjToBaseVar = new ConcurrentMultiMap<>();
        PtrSetCache ptrSetCache = moonData.ptrSetCache();


        Set<AllocNode> filteredContainerObjs = containerVars.parallelStream().map(ptrSetCache::ptsOf).flatMap(Collection::stream)
                .filter(o -> {
                    if (!containerObjs.contains(o)) return false;
                    if (!hasMultiCtx(o)) return false;
                    if (o instanceof ContextAllocNode) throw new RuntimeException("ContextAllocNode detected!");
                    return !(o instanceof ConstantNode) && o.getMethod() != null;
                }).collect(Collectors.toSet());

        containerVars.parallelStream().forEach(var -> {
            Set<AllocNode> pts = ptrSetCache.ptsOf(var);
            for (AllocNode obj : pts) {
                if (filteredContainerObjs.contains(obj)) {
                    containerObjToBaseVar.put(obj, var);
                }
            }
        });

        return containerObjToBaseVar;
    }

    public boolean hasMultiCtx(AllocNode obj) {
        Set<AllocNode> crtObjs = new HashSet<>();
        crtObjs.add(obj);
        var oag = moonData.oag();
        for (int i = 0; i < this.objCtxLen; i++) {
            Set<AllocNode> preds = crtObjs.stream().map(oag::getPredsOf).flatMap(Set::stream).collect(Collectors.toSet());
            if(preds.size() > 1) return true;
            crtObjs = preds;
        }
        return false;
    }
}
