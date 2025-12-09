package qilin.pta.toolkits.moon.support;


import qilin.core.pag.AllocNode;
import qilin.core.pag.ArrayElement;
import qilin.core.pag.LocalVarNode;
import qilin.util.collect.multimap.ConcurrentMultiMap;
import qilin.util.collect.twokeymultimap.ConcurrentTwoKeyMultiMap;
import qilin.util.collect.twokeymultimap.TwoKeyMultiMap;
import soot.ArrayType;
import soot.SootField;
import soot.Type;
import soot.jimple.spark.pag.SparkField;
import qilin.util.collect.multimap.MultiMap;

import java.util.Set;
import java.util.stream.Collectors;

public class FieldRecorder {


    private final TwoKeyMultiMap<LocalVarNode, SparkField, LocalVarNode> varToFieldToStoreFromVar = new ConcurrentTwoKeyMultiMap<>();
    private final TwoKeyMultiMap<LocalVarNode, SparkField, LocalVarNode> varToFieldToLoadedToVar = new ConcurrentTwoKeyMultiMap<>();

    protected final MultiMap<AllocNode, SparkField> objToFields = new ConcurrentMultiMap<>();
    protected final MultiMap<Type, SparkField> typeToFields = new ConcurrentMultiMap<>();

    protected final MultiMap<SparkField, AllocNode> fieldToBaseObjs = new ConcurrentMultiMap<>();

    public void putLoad(LocalVarNode var, SparkField field, LocalVarNode loadedToVar){
        if(field instanceof ArrayElement && !field.equals(ArrayElement.v())) throw new RuntimeException("ArrayElement should be the same object");
        varToFieldToLoadedToVar.put(var, field, loadedToVar);
    }

    public void putStore(LocalVarNode var, SparkField field, LocalVarNode storeFromVar){
        if(field instanceof ArrayElement && !field.equals(ArrayElement.v())) throw new RuntimeException("ArrayElement should be the same object");
        varToFieldToStoreFromVar.put(var, field, storeFromVar);
    }
    public boolean hasStore(LocalVarNode var, AllocNode obj){
        return hasUsage(varToFieldToStoreFromVar, var, obj);
    }

    public boolean hasLoad(LocalVarNode var, AllocNode obj){
        return hasUsage(varToFieldToLoadedToVar, var, obj);
    }

    private boolean hasUsage(TwoKeyMultiMap<LocalVarNode, SparkField, LocalVarNode> usageMap, LocalVarNode var, AllocNode obj){
        if(obj.getType() instanceof ArrayType){
            return usageMap.containsKey(var);
        }
        if(!usageMap.containsKey(var)) return false;
        Set<SootField> usageFields = usageMap.get(var).keySet().stream().filter(sf -> sf instanceof SootField).map(sf -> (SootField) sf).collect(Collectors.toSet());
        return !usageFields.isEmpty();
    }


    public Set<LocalVarNode> getStoredFromVars(LocalVarNode var, SparkField field){
        if(field instanceof ArrayElement && !field.equals(ArrayElement.v())) throw new RuntimeException("ArrayElement should be the same object");
        return varToFieldToStoreFromVar.get(var, field);
    }

    public Set<LocalVarNode> arrGetStoredFromVars(LocalVarNode var){
        return getStoredFromVars(var, ArrayElement.v());
    }

    public Set<LocalVarNode> getLoadedToVars(LocalVarNode var, SparkField field){
        if(field instanceof ArrayElement && !field.equals(ArrayElement.v())) throw new RuntimeException("ArrayElement should be the same object");
        return varToFieldToLoadedToVar.get(var, field);
    }

    public Set<SparkField> getLoadedFields(LocalVarNode var){
        return varToFieldToLoadedToVar.get(var).keySet();
    }

    public Set<SparkField> getStoredFields(LocalVarNode var){
        return varToFieldToStoreFromVar.get(var).keySet();
    }

    public MultiMap<SparkField, LocalVarNode> getFieldAndStoredFromVars(LocalVarNode var){
        return varToFieldToStoreFromVar.get(var);
    }



    public Set<SparkField> allFields(KeyTypeCollector openTypeCollector){
        return objToFields.values().parallelStream().filter(f -> openTypeCollector.isConcernedType(f.getType())).collect(Collectors.toSet());
    }

    public void recordObjToField(AllocNode obj, SparkField field){
        objToFields.put(obj, field);
        typeToFields.put(obj.getType(), field);
        fieldToBaseObjs.put(field, obj);
    }



}
