package qilin.core.solver.concurrent;

import heros.solver.CountingThreadPoolExecutor;
import qilin.core.PTA;
import qilin.core.pag.*;
import qilin.core.sets.DoublePointsToSet;
import qilin.core.sets.HybridPointsToSet;

import java.util.Collection;

public class ProcessPointerConstraintsTask extends AbstractHandlingConstraintsTask {
    private final ValNode pointer;

    public ProcessPointerConstraintsTask(ValNode pointer, PTA pta, CountingThreadPoolExecutor executor) {
        super(executor, pta);
        this.pointer = pointer;
    }

    @Override
    public void run() {
        ValNode curr = pointer;
        // Step 1: Resolving Direct Constraints
        assert curr != null;
        final DoublePointsToSet pts = curr.getP2Set();
        HybridPointsToSet newset;
        synchronized (pts.getNewSet()) {
            newset = pts.getNewSetCopy();
            pts.flushNew();
        }
        for (ValNode to : pag.simpleLookup(curr)) {
            propagatePTS(to, newset);
        }
        if (curr instanceof VarNode mSrc) {
            // Step 1 continues.
            Collection<ExceptionThrowSite> throwSites = eh.throwSitesLookUp(mSrc);
            for (ExceptionThrowSite site : throwSites) {
                eh.exceptionDispatch(newset, site);
            }
            // Step 2: Resolving Indirect Constraints.
            handleStoreAndLoadOnBase(mSrc, newset);
            // Step 3: Collecting New Constraints.
            Collection<VirtualCallSite> sites = cgb.callSitesLookUp(mSrc);
            for (VirtualCallSite site : sites) {
                cgb.virtualCallDispatch(newset, site);
            }
        }
    }

    private void handleStoreAndLoadOnBase(VarNode base, HybridPointsToSet newSet) {
        for (final FieldRefNode fr : base.getAllFieldRefs()) {
            for (final VarNode v : pag.storeInvLookup(fr)) {
                handleStoreEdge(newSet, fr.getField(), v);
            }
            for (final VarNode to : pag.loadLookup(fr)) {
                handleLoadEdge(newSet, fr.getField(), to);
            }
        }
    }

}
