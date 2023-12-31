package qilin.core.solver.concurrent;

import qilin.core.builder.ExceptionHandler;
import qilin.core.pag.ExceptionThrowSite;
import qilin.core.pag.VarNode;
import qilin.core.sets.HybridPointsToSet;

public class ExceptionDispathTask implements Runnable {
    private final ExceptionThrowSite ets;
    private final ExceptionHandler eh;

    public ExceptionDispathTask(ExceptionThrowSite ets, ExceptionHandler eh) {
        this.ets = ets;
        this.eh = eh;
    }

    @Override
    public void run() {
        final VarNode throwNode = ets.getThrowNode();
        HybridPointsToSet pts = throwNode.getP2Set().getOldSet();
        synchronized (pts) {
            eh.exceptionDispatch(pts, ets);
        }
    }
}
