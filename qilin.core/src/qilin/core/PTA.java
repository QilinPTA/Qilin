/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core;

import qilin.core.builder.CallGraphBuilder;
import qilin.core.builder.ExceptionHandler;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.core.solver.Propagator;
import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.heapabst.HeapAbstractor;
import qilin.parm.select.CtxSelector;
import soot.Context;
import soot.MethodOrMethodContext;
import soot.RefType;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class PTA implements PointsToAnalysis {

    protected AllocNode rootNode;
    protected PAG pag;
    protected CallGraph callGraph;
    protected CallGraphBuilder cgb;
    protected ExceptionHandler eh;

    public PTA() {
        this.pag = createPAG();
        this.cgb = createCallGraphBuilder();
        this.eh = new ExceptionHandler(this);
        AllocNode rootBase = new AllocNode("ROOT", RefType.v("java.lang.Object"), null);
        this.rootNode = new ContextAllocNode(rootBase, CtxConstructor.emptyContext);
    }

    protected abstract PAG createPAG();

    protected abstract CallGraphBuilder createCallGraphBuilder();

    public void run() {
        pureRun();
    }

    public void pureRun() {
        getPropagator().propagate();
    }

    public PAG getPag() {
        return pag;
    }

    public CallGraphBuilder getCgb() {
        return cgb;
    }

    public ExceptionHandler getExceptionHandler() {
        return eh;
    }

    public CallGraph getCallGraph() {
        if (callGraph == null) {
            callGraph = cgb.getCICallGraph();
        }
        return callGraph;
    }

    public Collection<MethodOrMethodContext> getReachableMethods() {
        return cgb.getReachableMethods();
    }

    private Set<SootMethod> nakedReachables = null;

    public Collection<SootMethod> getNakedReachableMethods() {
        if (nakedReachables == null) {
            nakedReachables = new HashSet<>();
            cgb.getReachableMethods().forEach(momc -> nakedReachables.add(momc.method()));
        }
        return nakedReachables;
    }

    protected abstract Propagator getPropagator();

    public abstract Node parameterize(Node n, Context context);

    public abstract MethodOrMethodContext parameterize(SootMethod method, Context context);

    public abstract AllocNode getRootNode();

    public abstract Context emptyContext();

    public abstract Context createCalleeCtx(MethodOrMethodContext caller, AllocNode receiverNode, CallSite callSite, SootMethod target);

    public abstract PointsToSet reachingObjectsInternal(PointsToSet s, final SparkField f);

    public abstract HeapAbstractor heapAbstractor();

    public abstract CtxConstructor ctxConstructor();

    public abstract CtxSelector ctxSelector();
}
