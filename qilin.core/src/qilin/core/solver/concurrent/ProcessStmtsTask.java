package qilin.core.solver.concurrent;

import heros.solver.CountingThreadPoolExecutor;
import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.builder.CallGraphBuilder;
import qilin.core.builder.ExceptionHandler;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.NumberedString;
import soot.util.queue.QueueReader;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ProcessStmtsTask implements Runnable {
    private MethodOrMethodContext momc;
    private CallGraphBuilder cgb;
    private ExceptionHandler eh;
    private PTA pta;
    private PAG pag;
    CountingThreadPoolExecutor executor;

    public ProcessStmtsTask(MethodOrMethodContext momc, PTA pta, CountingThreadPoolExecutor executor) {
        this.momc = momc;
        this.cgb = pta.getCgb();
        this.eh = pta.getExceptionHandler();
        this.pta = pta;
        this.pag = pta.getPag();
        this.executor = executor;
    }

    @Override
    public void run() {
        SootMethod method = momc.method();
        if (method.isPhantom()) {
            return;
        }
        MethodPAG mpag = pag.getMethodPAG(method);
        addToPAG(mpag, momc.context());
        // !FIXME in a context-sensitive pointer analysis, clinits in a method maybe added multiple times.
        if (CoreConfig.v().getPtaConfig().clinitMode == CoreConfig.ClinitMode.ONFLY) {
            // add <clinit> find in the method to reachableMethods.
            Iterator<SootMethod> it = mpag.triggeredClinits();
            while (it.hasNext()) {
                SootMethod sm = it.next();
                cgb.injectCallEdge(sm.getDeclaringClass().getType(), pta.parameterize(sm, pta.emptyContext()), Kind.CLINIT);
            }
        }
        recordCallStmts(momc, mpag.getInvokeStmts());
        recordThrowStmts(momc, mpag.stmt2wrapperedTraps.keySet());
    }

    private void addToPAG(MethodPAG mpag, Context cxt) {
        Set<Context> contexts = pag.getMethod2ContextsMap().computeIfAbsent(mpag, k1 -> DataFactory.createSet());
        if (!contexts.add(cxt)) {
            return;
        }
        QueueReader<Node> xreader = mpag.getInternalReader();
        synchronized (xreader) {
            for (QueueReader<Node> reader = xreader.clone(); reader.hasNext(); ) {
                Node from = reader.next();
                Node to = reader.next();
                if (from instanceof AllocNode heap) {
                    from = pta.heapAbstractor().abstractHeap(heap);
                }
                if (from instanceof AllocNode && to instanceof GlobalVarNode) {
                    pag.addGlobalPAGEdge(from, to);
                } else {
                    from = pta.parameterize(from, cxt);
                    to = pta.parameterize(to, cxt);
                    if (from instanceof AllocNode) {
                        handleImplicitCallToFinalizerRegister((AllocNode) from);
                    }
                    pag.addEdge(from, to);
                }
            }
        }
    }

    private void recordCallStmts(MethodOrMethodContext m, Collection<Unit> units) {
        for (final Unit u : units) {
            final Stmt s = (Stmt) u;
            if (s.containsInvokeExpr()) {
                InvokeExpr ie = s.getInvokeExpr();
                if (ie instanceof InstanceInvokeExpr iie) {
                    Local receiver = (Local) iie.getBase();
                    VarNode recNode = cgb.getReceiverVarNode(receiver, m);
                    NumberedString subSig = iie.getMethodRef().getSubSignature();
                    VirtualCallSite virtualCallSite = new VirtualCallSite(recNode, s, m, iie, subSig, Edge.ieToKind(iie));
                    if (cgb.recordVirtualCallSite(recNode, virtualCallSite)) {
                        VirtualCallDispatchTask vcdt = new VirtualCallDispatchTask(virtualCallSite, cgb);
                        this.executor.execute(vcdt);
                    }
                } else {
                    SootMethod tgt = ie.getMethod();
                    if (tgt != null) { // static invoke or dynamic invoke
                        VarNode recNode = pag.getMethodPAG(m.method()).nodeFactory().caseThis();
                        recNode = (VarNode) pta.parameterize(recNode, m.context());
                        if (ie instanceof DynamicInvokeExpr) {
                            // !TODO dynamicInvoke is provided in JDK after Java 7.
                            // currently, PTA does not handle dynamicInvokeExpr.
                        } else {
                            cgb.addStaticEdge(m, s, tgt, Edge.ieToKind(ie));
                        }
                    } else if (!Options.v().ignore_resolution_errors()) {
                        throw new InternalError("Unresolved target " + ie.getMethod()
                                + ". Resolution error should have occured earlier.");
                    }
                }
            }
        }
    }

    private void recordThrowStmts(MethodOrMethodContext m, Collection<Stmt> stmts) {
        for (final Stmt stmt : stmts) {
            SootMethod sm = m.method();
            MethodPAG mpag = pag.getMethodPAG(sm);
            MethodNodeFactory nodeFactory = mpag.nodeFactory();
            Node src;
            if (stmt.containsInvokeExpr()) {
                src = nodeFactory.makeInvokeStmtThrowVarNode(stmt, sm);
            } else {
                assert stmt instanceof ThrowStmt;
                ThrowStmt ts = (ThrowStmt) stmt;
                src = nodeFactory.getNode(ts.getOp());
            }
            VarNode throwNode = (VarNode) pta.parameterize(src, m.context());
            ExceptionThrowSite throwSite = new ExceptionThrowSite(throwNode, stmt, m);
            if (eh.addThrowSite(throwNode, throwSite)) {
                ExceptionDispathTask edt = new ExceptionDispathTask(throwSite, eh);
                this.executor.execute(edt);
            }
        }
    }

    // handle implicit calls to java.lang.ref.Finalizer.register by the JVM.
    // please refer to library/finalization.logic in doop.
    private void handleImplicitCallToFinalizerRegister(AllocNode heap) {
        if (PTAUtils.supportFinalize(heap)) {
            SootMethod rm = PTAScene.v().getMethod("<java.lang.ref.Finalizer: void register(java.lang.Object)>");
            MethodPAG tgtmpag = pag.getMethodPAG(rm);
            MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
            Node parm = tgtnf.caseParm(0);
            Context calleeCtx = pta.emptyContext();
            AllocNode baseHeap = heap.base();
            parm = pta.parameterize(parm, calleeCtx);
            pag.addEdge(heap, parm);
            cgb.injectCallEdge(baseHeap, pta.parameterize(rm, calleeCtx), Kind.STATIC);
        }
    }
}
