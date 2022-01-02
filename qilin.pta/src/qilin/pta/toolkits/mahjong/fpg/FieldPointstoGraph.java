package qilin.pta.toolkits.mahjong.fpg;

import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;
import qilin.pta.toolkits.mahjong.pta.PTAProvider;

import java.util.*;

/**
 * @author Tian Tan
 * @author Yue Li
 * refacted by Dongjie He.
 */
public class FieldPointstoGraph {

    private final PTAProvider provider;
    private final Map<AllocNode, Map<SparkField, Set<AllocNode>>> pointsTo = new HashMap<>();
    private final Map<AllocNode, Map<SparkField, Set<AllocNode>>> pointedBy = new HashMap<>();

    public FieldPointstoGraph(PTAProvider provider) {
        this.provider = provider;
        provider.objIterator().forEachRemaining(this::insertObj);
        provider.fptIterator().forEachRemaining(triple -> {
            AllocNode baseObj = triple.getFirst();
            SparkField field = triple.getSecond();
            AllocNode obj = triple.getThird();
            insertFPT(baseObj, field, obj);
        });
    }

    public PTAProvider getPTAProvider() {
        return provider;
    }

    public Set<AllocNode> getAllObjs() {
        return pointsTo.keySet();
    }

    public Set<SparkField> outFieldsOf(AllocNode baseObj) {
        return pointsTo.getOrDefault(baseObj, Collections.emptyMap()).keySet();
    }

    public Set<SparkField> inFieldsOf(AllocNode obj) {
        return pointedBy.get(obj).keySet();
    }

    public Set<AllocNode> pointsTo(AllocNode baseObj, SparkField field) {
        return pointsTo.get(baseObj).get(field);
    }

    public Set<AllocNode> pointedBy(AllocNode obj, SparkField field) {
        return pointedBy.get(obj).get(field);
    }

    public boolean hasFieldPointer(AllocNode obj, SparkField field) {
        return pointsTo.get(obj).containsKey(field);
    }


    private void insertObj(AllocNode obj) {
        pointsTo.computeIfAbsent(obj, k -> new HashMap<>());
        pointedBy.computeIfAbsent(obj, k -> new HashMap<>());
    }

    /**
     * Insert field points-to relation.
     *
     * @param baseObj the base object
     * @param field   a field of `baseObj'
     * @param obj     the object pointed by `field'
     */
    private void insertFPT(AllocNode baseObj, SparkField field, AllocNode obj) {
        insertPointsTo(baseObj, field, obj);
        insertPointedBy(baseObj, field, obj);
    }

    private void insertPointsTo(AllocNode baseObj, SparkField field, AllocNode obj) {
        Map<SparkField, Set<AllocNode>> fpt = pointsTo.computeIfAbsent(baseObj, k -> new HashMap<>());
        fpt.computeIfAbsent(field, k -> new HashSet<>()).add(obj);
    }

    private void insertPointedBy(AllocNode baseObj, SparkField field, AllocNode obj) {
        Map<SparkField, Set<AllocNode>> fpb = pointedBy.computeIfAbsent(obj, k -> new HashMap<>());
        fpb.computeIfAbsent(field, k -> new HashSet<>()).add(baseObj);
    }

}
