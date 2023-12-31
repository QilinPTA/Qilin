package qilin.parm.ctxcons;

import qilin.core.pag.AllocNode;
import soot.Context;
import soot.SootMethod;

public class DefaultHeapCtxConstructor implements HeapCtxConstructor {
    @Override
    public Context constructCtx(SootMethod container, Context containerCtx, AllocNode heap) {
        return containerCtx;
    }
}
