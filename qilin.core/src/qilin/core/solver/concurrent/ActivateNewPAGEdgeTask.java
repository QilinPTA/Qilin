package qilin.core.solver.concurrent;

import heros.solver.CountingThreadPoolExecutor;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.pag.*;
import qilin.core.sets.HybridPointsToSet;

public class ActivateNewPAGEdgeTask extends AbstractHandlingConstraintsTask {
    Node from;
    Node to;

    public ActivateNewPAGEdgeTask(Node from, Node to, PTA pta, CountingThreadPoolExecutor executor) {
        super(executor, pta);
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        final Node addedSrc = from;
        final Node addedTgt = to;
        if (addedSrc instanceof VarNode && addedTgt instanceof VarNode
                || addedSrc instanceof ContextField || addedTgt instanceof ContextField
        ) { // x = y; x = o.f; o.f = y;
            final ValNode srcv = (ValNode) addedSrc;
            final ValNode tgtv = (ValNode) addedTgt;
            if (srcv == tgtv) {
                return;
            }
            HybridPointsToSet pts = srcv.getP2Set().getOldSet();
            synchronized (pts) {
                propagatePTS(tgtv, pts);
            }
        } else if (addedSrc instanceof final FieldRefNode srcfrn) { // b = a.f
            HybridPointsToSet pts = srcfrn.getBase().getP2Set().getOldSet();
            synchronized (pts) {
                handleLoadEdge(pts, srcfrn.getField(), (ValNode) addedTgt);
            }
        } else if (addedTgt instanceof final FieldRefNode tgtfrn) { // a.f = b;
            HybridPointsToSet pts = tgtfrn.getBase().getP2Set().getOldSet();
            synchronized (pts) {
                handleStoreEdge(pts, tgtfrn.getField(), (ValNode) addedSrc);
            }
        } else if (addedSrc instanceof AllocNode) { // alloc x = new T;
            propagatePTS((VarNode) addedTgt, (AllocNode) addedSrc);
        }
    }
}
