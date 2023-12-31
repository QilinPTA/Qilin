package qilin.core.solver.concurrent;

import qilin.core.builder.CallGraphBuilder;
import qilin.core.pag.VarNode;
import qilin.core.pag.VirtualCallSite;
import qilin.core.sets.HybridPointsToSet;

public class VirtualCallDispatchTask implements Runnable {
    private final VirtualCallSite site;
    private final CallGraphBuilder cgb;

    public VirtualCallDispatchTask(VirtualCallSite site, CallGraphBuilder cgb) {
        this.site = site;
        this.cgb = cgb;
    }

    @Override
    public void run() {
        final VarNode receiver = site.recNode();
        HybridPointsToSet pts = receiver.getP2Set().getOldSet();
        synchronized (pts) {
            cgb.virtualCallDispatch(pts, site);
        }
    }
}
