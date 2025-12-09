package qilin.pta.toolkits.moon.traversal;


import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.moon.support.FieldFlowRecorder;
import qilin.pta.toolkits.moon.support.FieldRecorder;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.util.collect.multimap.ConcurrentMultiMap;
import qilin.util.collect.multimap.MultiMap;
import soot.*;
import soot.jimple.spark.pag.SparkField;



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VFGTraversal {
    private final int objCtxLen;
    private final MoonDataConstructor.MoonDataStructure moonData;
    public VFGTraversal(int objCtxLen,  MoonDataConstructor.MoonDataStructure moonData) {
        this.objCtxLen = objCtxLen;
        this.moonData = moonData;
    }


    public List<MultiMap<AllocNode, TraversalResult>> traverse(MultiMap<AllocNode, LocalVarNode> objToBaseVar){
        List<MultiMap<AllocNode, TraversalResult>> objToMatchRets = new ArrayList<>(objCtxLen);
        for (int i = 0; i < objCtxLen; i++) {
            objToMatchRets.add(new ConcurrentMultiMap<>());
        }

        System.out.println("Number of container objects after filtering: " + objToBaseVar.keySet().size());
        FieldRecorder fieldRecorder = moonData.fieldRecorder();
        FieldFlowRecorder fieldFlowRecorder = moonData.fieldFlowRecorder();
        OAG oag = moonData.oag();

        StoredVarTraverser storedVarTraverser = new StoredVarTraverser(moonData, objCtxLen);
        objToBaseVar.keySet().parallelStream().forEach(obj -> {
            objToBaseVar.get(obj).forEach(baseVar -> {
                if(notInAllocatorMethod(obj, baseVar) || baseVar.getMethod().isStatic()) return;
                var fieldToStoredFromVars = fieldRecorder.getFieldAndStoredFromVars(baseVar);
                for(SparkField storedField: fieldToStoredFromVars.keySet()){
                    for (LocalVarNode storedVar : fieldToStoredFromVars.get(storedField)) {
                        if(storedVar.getType() instanceof NullType || isIgnoredField(obj, storedField) ||
                                !fieldFlowRecorder.isConnceredField(obj, storedField))
                            continue;
                        // perform VFG traversal from storedVar
                        int toBeCheckedLen = determineToBeCheckedLen(obj, oag);
                        TraversalResult traversalResult = storedVarTraverser.findSourceOfVarStoredIn(obj, storedVar, objCtxLen, storedField);
                        objToMatchRets.get(toBeCheckedLen - 1).put(obj, traversalResult);
                    }
                }
            });
        });
        return objToMatchRets;
    }

    private int determineToBeCheckedLen(AllocNode obj, OAG oag){
        int toBeCheckedLen;
        if(this.objCtxLen == 1){
            toBeCheckedLen = 1;
        }else if (this.objCtxLen == 2){
            if(!(obj.getType() instanceof ArrayType) && oag.getPredsOf(obj).size() == 1)
                toBeCheckedLen = 2;
            else
                toBeCheckedLen = 1;
        }else{
            throw new RuntimeException("Unsupported objCtxLen: " + this.objCtxLen);
        }
        return toBeCheckedLen;
    }

    private boolean notInAllocatorMethod(AllocNode obj, LocalVarNode var){
        SootMethod inMethod = var.getMethod();
        var objToInvokedMethods = moonData.objToIvkMtds();
        var oag = moonData.oag();
        if(!objToInvokedMethods.containsKey(obj) || objToInvokedMethods.get(obj).contains(inMethod)) return false;
        Set<AllocNode> allocators = oag.getPredsOf(obj);
        return allocators.stream().noneMatch(o -> objToInvokedMethods.containsKey(o) && objToInvokedMethods.get(o).contains(inMethod));
    }

    private boolean isIgnoredField(AllocNode obj, SparkField field){
        boolean isRefType;

        if(obj.getType() instanceof ArrayType arrayType) {
            isRefType = arrayType.baseType instanceof RefType;
        }else{
            Type type = field.getType();
            if(type instanceof PrimType) isRefType = false;
            else if(type instanceof RefType) isRefType = true;
            else if(type instanceof ArrayType arrayType){
                isRefType = arrayType.baseType instanceof RefType;
            }else {
                throw new RuntimeException("Unexpected type: " + type);
            }
        }
        return !isRefType;
    }

}
