package qilin.pta.toolkits.moon.support;


import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ArrayElement;
import qilin.core.pag.PAG;
import qilin.util.collect.multimap.ConcurrentMultiMap;
import qilin.util.collect.multimap.MultiHashMap;
import qilin.util.collect.multimap.MultiMap;
import soot.ArrayType;
import soot.RefType;
import soot.Type;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.internal.AbstractNewArrayExpr;
import soot.jimple.spark.pag.SparkField;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerCollector {

    private final PAG pag;
    private final FieldRecorder fieldRecorder;
    private final FieldFlowRecorder fieldReachabilityRecorder;
    protected final MultiMap<AllocNode, SparkField> containerToFields = new ConcurrentMultiMap<>();
    private final KeyTypeCollector keyTypeCollector;
    public ContainerCollector(PTA pta, FieldRecorder fieldRecorder, FieldFlowRecorder fieldReachabilityRecorder, KeyTypeCollector keyTypeCollector){
        this.pag = pta.getPag();
        this.fieldRecorder = fieldRecorder;
        this.fieldReachabilityRecorder = fieldReachabilityRecorder;
        this.keyTypeCollector = keyTypeCollector;
    }
    public void collect(){
        Set<AllocNode> toBeDetermined = ConcurrentHashMap.newKeySet();
        pag.getAllocNodes().parallelStream().forEach(
        heap -> {
            if(qilinHack(heap)){
                containerToFields.put(heap, ArrayElement.v());
                return;
            }
            Type type = heap.getType();
            if(type instanceof ArrayType arrayType){
                AbstractNewArrayExpr arrayExpr = (AbstractNewArrayExpr) heap.getNewExpr();
                Value arrLen = arrayExpr.getSize();
                if(!(arrLen instanceof IntConstant intArrLen) || intArrLen.value > 0){
                    if(keyTypeCollector.isConcernedType(arrayType))
                        containerToFields.put(heap, ArrayElement.v());
                }
            }else if(type instanceof RefType refType){
                if(keyTypeCollector.isConcernedType(refType) && heap.getMethod() != null){
                    toBeDetermined.add(heap);
                }
            }
        });

        toBeDetermined.parallelStream()
                .forEach(heap -> {
            Set<SparkField> fields = fieldRecorder.objToFields.get(heap);
            for (SparkField field : fields) {
                if(!keyTypeCollector.isConcernedType(field.getType())) continue;
                if(fieldReachabilityRecorder.isConnceredField(heap, field)){
                    containerToFields.put(heap, field);
                }
            }
        });
    }

    private boolean qilinHack(AllocNode heap){
        // this is the same hack as DebloaterX[OOPSLA'23], due to the defect in Qilin framework
        if(heap.getMethod() == null) return false;
        String sig = heap.getMethod().getSignature();
        return sig.startsWith("<java.util.Arrays: java.lang.Object[] copyOf(java.lang.Object[],int,java.lang.Class)>")
                ||
                sig.startsWith("<java.util.AbstractCollection: java.lang.Object[] toArray(java.lang.Object[])>");
    }
}
