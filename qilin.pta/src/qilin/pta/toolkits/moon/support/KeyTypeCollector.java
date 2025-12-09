package qilin.pta.toolkits.moon.support;

import qilin.core.PTAScene;
import qilin.core.pag.AllocNode;
import soot.*;
import soot.jimple.spark.pag.SparkField;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KeyTypeCollector {

    private static final Type OBJECT_TYPE = RefType.v("java.lang.Object");
    private final Set<Type> polyTypes = new HashSet<>();
    private final FieldRecorder fieldRecorder;
    public KeyTypeCollector(FieldRecorder fieldRecorder){
        this.fieldRecorder = fieldRecorder;
    }

    private boolean isPolyType(Type type){
        if(type.equals(OBJECT_TYPE)){
            return true;
        }
        if(type instanceof RefType refType){
            SootClass sootClass = refType.getSootClass();
            return sootClass.isAbstract() || sootClass.isInterface() || sootClass.getShortName().startsWith("Abstract");
        }
        return false;
    }

    public boolean isConcernedType(Type type){
        if(type instanceof ArrayType arrayType){
            type = arrayType.getElementType();
        }
        return isPolyType(type) || this.polyTypes.contains(type);
    }


    private Set<SparkField> fieldsOf(AllocNode node) {
        return fieldRecorder.objToFields.get(node);
    }

    private Set<SparkField> fieldsOf(Type type) {
        if (type instanceof RefType refType) {
            if(!fieldRecorder.typeToFields.containsKey(type)){
                for (AllocNode heap : fieldRecorder.objToFields.keySet()) {
                    if (PTAScene.v().getOrMakeFastHierarchy().canStoreType(heap.getType(), refType)) {
                        for (SparkField sparkField : fieldRecorder.objToFields.get(heap)) {
                            if (sparkField instanceof SootField sf) {
                                Type declType = sf.getDeclaringClass().getType();
                                if (PTAScene.v().getOrMakeFastHierarchy().canStoreType(type, declType)) {
                                    fieldRecorder.typeToFields.put(type, sparkField);
                                }
                            } else {
                                throw new RuntimeException(sparkField + ";" + sparkField.getClass());
                            }
                        }
                    }
                }
            }
            return fieldRecorder.typeToFields.get(type);
        } else {
            return Collections.emptySet();
        }
    }

    public void run(Collection<AllocNode> allObjs) {
        Set<Type> types = new HashSet<>();
        for (AllocNode heap : allObjs) {
            if(heap.getMethod() == null) continue;
            Type type = heap.getType();
            if (type instanceof ArrayType at) {
                Type et = at.getElementType();
                if (isPolyType(et)) {
                    polyTypes.add(et);
                } else {
                    types.add(et);
                }
            } else {
                for (SparkField field : fieldsOf(heap)) {
                    Type ft = field.getType();
                    if (ft instanceof ArrayType fat) {
                        ft = fat.getElementType();
                    }
                    if (isPolyType(ft)) {
                        polyTypes.add(ft);
                        polyTypes.add(type);
                    } else {
                        types.add(type);
                        types.add(ft);
                    }
                }
            }
        }
        boolean continueUpdating = true;
        while (continueUpdating) {
            continueUpdating = false;
            for (Type type : types) {
                for (SparkField field : fieldsOf(type)) {
                    Type ft = field.getType();
                    if (isConcernedType(ft)) {
                        if (polyTypes.add(type)) {
                            continueUpdating = true;
                        }
                    }
                }
            }
        }
    }
}
