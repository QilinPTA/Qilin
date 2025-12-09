package qilin.pta.toolkits.moon.traversal;

import qilin.core.pag.AllocNode;
import soot.SootMethod;
import soot.jimple.spark.pag.SparkField;

import java.util.*;

public class TraversalResult {
    private final List<Set<AllocNode>> matchedCtxObjsOfThisAsParam;
    private final List<Set<AllocNode>> matchedCtxObjsOfParam;
    private final List<Set<AllocNode>> newlyAllocObjs;
    private final int recordSize;
    private boolean metParamOfAllocatedMethod;
    private final Set<SootMethod> visitedMethods = new HashSet<>();
    private final SparkField field;
    public TraversalResult(int matchLayer, SparkField field) {
        this.field = field;
        matchLayer ++;
        recordSize = matchLayer;
        matchedCtxObjsOfThisAsParam = new ArrayList<>(matchLayer);
        matchedCtxObjsOfParam = new ArrayList<>(matchLayer);
        newlyAllocObjs = new ArrayList<>(matchLayer);
        for (int i = 0; i < matchLayer; i++) {
            matchedCtxObjsOfThisAsParam.add(null);
            matchedCtxObjsOfParam.add(null);
            newlyAllocObjs.add(null);
        }
    }

    public SparkField getField() {
        return field;
    }

    public Set<SootMethod> getVisitedMethods(){
        return visitedMethods;
    }
    public void addVisitedMethod(SootMethod method) {
        visitedMethods.add(method);
    }

    public void hasMetParamOfAllocatedMethod() {
        this.metParamOfAllocatedMethod = true;
    }
    public boolean isMetParamOfAllocatedMethod() {
        return metParamOfAllocatedMethod;
    }

    public int getRecordSize() {
        return recordSize;
    }



    public void addNewlyAllocObj(AllocNode newlyAllocObj, int index) {
        if(this.newlyAllocObjs.get(index) == null){
            Set<AllocNode> newlyAllocObjs = new HashSet<>();
            newlyAllocObjs.add(newlyAllocObj);
            this.newlyAllocObjs.set(index, newlyAllocObjs);
        }else{
            this.newlyAllocObjs.get(index).add(newlyAllocObj);
        }
    }

    public void addMatchedCtxObjsByThisAsParam(Set<AllocNode> matchedCtxObjsOfThis, int index) {
        if(matchedCtxObjsOfThis.isEmpty()) return;
        if(this.matchedCtxObjsOfThisAsParam.get(index) == null){
            this.matchedCtxObjsOfThisAsParam.set(index, new HashSet<>(matchedCtxObjsOfThis));
        }else{
            this.matchedCtxObjsOfThisAsParam.get(index).addAll(matchedCtxObjsOfThis);
        }
    }

    public void addMatchedCtxObjsOfParam(Set<AllocNode> matchedCtxObjsOfParam, int index) {
        if(matchedCtxObjsOfParam.isEmpty()) return;
        if(this.matchedCtxObjsOfParam.get(index) == null){
            this.matchedCtxObjsOfParam.set(index, new HashSet<>(matchedCtxObjsOfParam));
        }else{
            this.matchedCtxObjsOfParam.get(index).addAll(matchedCtxObjsOfParam);
        }
    }

    public Set<AllocNode> getMatchedCtxObjsOfThis(int index) {
        if(index >= recordSize) return Collections.emptySet();
        Set<AllocNode> matchedCtxObjsOfThis = this.matchedCtxObjsOfThisAsParam.get(index);
        return matchedCtxObjsOfThis == null ? Collections.emptySet() : Set.copyOf(matchedCtxObjsOfThis);
    }
    public Set<AllocNode> getMatchedCtxObjsOfParam(int index) {
        if(index >= recordSize) return Collections.emptySet();
        Set<AllocNode> matchedCtxObjsOfParam = this.matchedCtxObjsOfParam.get(index);
        return matchedCtxObjsOfParam == null ? Collections.emptySet() : Set.copyOf(matchedCtxObjsOfParam);
    }
    public Set<AllocNode> getNewlyAllocObjs(int index) {
        if(index >= recordSize) return Collections.emptySet();
        Set<AllocNode> newlyAllocObjs = this.newlyAllocObjs.get(index);
        return newlyAllocObjs == null ? Collections.emptySet() : Set.copyOf(newlyAllocObjs);
    }


    public boolean hasSpecificCtxObj(AllocNode ctxObj){
        return this.matchedCtxObjsOfParam.stream().anyMatch(s -> s != null && s.contains(ctxObj)) || this.matchedCtxObjsOfThisAsParam.stream().anyMatch(s -> s != null && s.contains(ctxObj));
    }

    public boolean hasSpecificNewlyAllocObj(AllocNode newlyAllocObj){
        return this.newlyAllocObjs.stream().anyMatch(s -> s != null && s.contains(newlyAllocObj));
    }

    public boolean hasAnyOfNewlyAllocObjs(Set<AllocNode> newlyAllocObjs){
        return this.newlyAllocObjs.stream().anyMatch(s -> s != null && !Collections.disjoint(s, newlyAllocObjs));
    }

    public boolean hasAnyOfCtxObjsOfThis(){
        return this.matchedCtxObjsOfThisAsParam.stream().anyMatch(s -> s != null && !s.isEmpty());
    }

    public Set<AllocNode> getAllNewlyAllocObjs(){
        Set<AllocNode> allNewlyAllocObjs = new HashSet<>();
        this.newlyAllocObjs.stream().filter(s -> s != null).forEach(allNewlyAllocObjs::addAll);
        return allNewlyAllocObjs;
    }

    public boolean hasCtxObjsOnLayerOf(int checkLayer){
        return !getMatchedCtxObjsOfParam(checkLayer).isEmpty() || !getMatchedCtxObjsOfThis(checkLayer).isEmpty();
    }

    public boolean hasNewlyAllocObjs(){
        return newlyAllocObjs.stream().anyMatch(allocNodes -> allocNodes != null && !allocNodes.isEmpty());
    }

}
