package qilin.core.solver.concurrent;

import heros.solver.CountingThreadPoolExecutor;
import qilin.core.PTA;
import qilin.core.builder.CallGraphBuilder;
import qilin.core.builder.ExceptionHandler;
import qilin.core.pag.*;
import qilin.core.sets.DoublePointsToSet;
import qilin.core.sets.HybridPointsToSet;
import qilin.core.sets.P2SetVisitor;
import qilin.util.PTAUtils;
import soot.jimple.spark.pag.SparkField;

public abstract class AbstractHandlingConstraintsTask implements Runnable {
    protected CountingThreadPoolExecutor executor;
    protected ExceptionHandler eh;
    protected CallGraphBuilder cgb;
    protected PTA pta;
    protected PAG pag;

    AbstractHandlingConstraintsTask(CountingThreadPoolExecutor executor, PTA pta) {
        this.executor = executor;
        this.cgb = pta.getCgb();
        this.eh = pta.getExceptionHandler();
        this.pta = pta;
        this.pag = pta.getPag();
    }

    protected void handleStoreEdge(HybridPointsToSet baseHeaps, SparkField field, ValNode from) {
        baseHeaps.forall(new P2SetVisitor(pta) {
            public void visit(Node n) {
                if (disallowStoreOrLoadOn((AllocNode) n)) {
                    return;
                }
                final FieldValNode fvn = pag.makeFieldValNode(field);
                final ValNode oDotF = (ValNode) pta.parameterize(fvn, PTAUtils.plusplusOp((AllocNode) n));
                pag.addEdge(from, oDotF);
            }
        });
    }

    protected void handleLoadEdge(HybridPointsToSet baseHeaps, SparkField field, ValNode to) {
        baseHeaps.forall(new P2SetVisitor(pta) {
            public void visit(Node n) {
                if (disallowStoreOrLoadOn((AllocNode) n)) {
                    return;
                }
                final FieldValNode fvn = pag.makeFieldValNode(field);
                final ValNode oDotF = (ValNode) pta.parameterize(fvn, PTAUtils.plusplusOp((AllocNode) n));
                pag.addEdge(oDotF, to);
            }
        });
    }

    // we do not allow store to and load from constant heap/empty array.
    private boolean disallowStoreOrLoadOn(AllocNode heap) {
        AllocNode base = heap.base();
        // return base instanceof StringConstantNode || PTAUtils.isEmptyArray(base);
        return PTAUtils.isEmptyArray(base);
    }

    protected void propagatePTS(final ValNode pointer, HybridPointsToSet other) {
        final DoublePointsToSet addTo = pointer.getP2Set();
        synchronized (addTo.getNewSet()) {
            P2SetVisitor p2SetVisitor = new P2SetVisitor(pta) {
                @Override
                public void visit(Node n) {
                    if (PTAUtils.addWithTypeFiltering(addTo, pointer.getType(), n)) { // need
                        returnValue = true;
                    }
                }
            };
            other.forall(p2SetVisitor);
            if (p2SetVisitor.getReturnValue()) {
                ProcessPointerConstraintsTask ppct = new ProcessPointerConstraintsTask(pointer, pta, executor);
                this.executor.execute(ppct);
            }
        }
    }

    protected void propagatePTS(final ValNode pointer, AllocNode heap) {
        DoublePointsToSet pts = pointer.getP2Set();
        synchronized (pts.getNewSet()) {
            if (PTAUtils.addWithTypeFiltering(pts, pointer.getType(), heap)) {
                ProcessPointerConstraintsTask ppct = new ProcessPointerConstraintsTask(pointer, pta, executor);
                this.executor.execute(ppct);
            }
        }
    }
}
