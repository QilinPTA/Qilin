package qilin.pta.toolkits.mahjong.pta;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.AllocNode;
import qilin.core.pag.Node;
import qilin.core.pag.SparkField;
import qilin.core.pag.VarNode;
import qilin.core.sets.P2SetVisitor;
import qilin.core.sets.PointsToSetInternal;
import qilin.util.Triple;
import soot.SootMethod;
import soot.Type;

import java.util.*;

public class PTAProviderImpl implements PTAProvider {

    private final PTA prePTA;

    private Map<AllocNode, Set<SootMethod>> obj2invokedMethods;
    private Map<Type, Set<AllocNode>> typeObjects;

    public PTAProviderImpl(PTA prePTA) {
        this.prePTA = prePTA;
    }

    @Override
    public Iterator<AllocNode> objIterator() {
        return new ObjIterator();
    }

    @Override
    public Iterator<Triple<AllocNode, SparkField, AllocNode>> fptIterator() {
        return new FPTIterator();
    }

    private class ObjIterator implements Iterator<AllocNode> {

        private final Iterator<AllocNode> objIter;

        private ObjIterator() {
            objIter = prePTA.getPag().getAllocNodes().iterator();
        }

        @Override
        public boolean hasNext() {
            return objIter.hasNext();
        }

        @Override
        public AllocNode next() {
            if (hasNext()) {
                return objIter.next();
            } else {
                throw new NoSuchElementException();
            }
        }

    }

    private class FPTIterator implements Iterator<Triple<AllocNode, SparkField, AllocNode>> {

        private final Iterator<Triple<AllocNode, SparkField, AllocNode>> fptIter;

        private FPTIterator() {
            List<Triple<AllocNode, SparkField, AllocNode>> list = new ArrayList<>();
            prePTA.getPag().getContextFields().forEach(contextField -> {
                AllocNode base = contextField.getBase();
                if (base.getMethod() == null) {
                    return;
                }
                SparkField field = contextField.getField();
                contextField.getP2Set().mapToCIPointsToSet().forall(new P2SetVisitor() {
                    @Override
                    public void visit(Node n) {
                        list.add(new Triple<>(base, field, (AllocNode) n));
                    }
                });
            });
            fptIter = list.iterator();
        }

        @Override
        public boolean hasNext() {
            return fptIter.hasNext();
        }

        @Override
        public Triple<AllocNode, SparkField, AllocNode> next() {
            if (fptIter.hasNext()) {
                Triple<AllocNode, SparkField, AllocNode> fpt = fptIter.next();
                return new Triple<>(fpt.getFirst(), fpt.getSecond(), fpt.getThird());
            } else {
                throw new NoSuchElementException();
            }
        }
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
    public Set<SootMethod> invokedMethodsOn(AllocNode heap) {
        if (obj2invokedMethods == null) {
            this.obj2invokedMethods = new HashMap<>();
            prePTA.getNakedReachableMethods().stream().filter(m -> !m.isStatic()).forEach(instMtd -> {
                MethodNodeFactory mthdNF = prePTA.getPag().getMethodPAG(instMtd).nodeFactory();
                VarNode thisVar = mthdNF.caseThis();
                pointsToSetOf(thisVar).forEach(obj -> {
                    obj2invokedMethods.computeIfAbsent(obj, k -> new HashSet<>()).add(instMtd);
                });
            });
        }
        return obj2invokedMethods.getOrDefault(heap, Collections.emptySet());
    }

    @Override
    public Set<AllocNode> objectsOfType(final Type type) {
        if (typeObjects == null) {
            typeObjects = new HashMap<>();
            objIterator().forEachRemaining(obj -> {
                this.typeObjects.computeIfAbsent(obj.getType(), k -> new HashSet<>()).add(obj);
            });
        }
        return this.typeObjects.getOrDefault(type, Collections.emptySet());
    }

}
