package qilin.pta.tools;

import qilin.core.CorePTA;
import qilin.core.builder.CallGraphBuilder;
import qilin.core.pag.PAG;
import qilin.core.solver.Propagator;
import qilin.core.solver.Solver;

public class BasePTA extends CorePTA {
    @Override
    protected PAG createPAG() {
        return new PAG(this);
    }

    @Override
    protected CallGraphBuilder createCallGraphBuilder() {
        return new CallGraphBuilder(this);
    }

    @Override
    public Propagator getPropagator() {
        return new Solver(this);
    }
}
