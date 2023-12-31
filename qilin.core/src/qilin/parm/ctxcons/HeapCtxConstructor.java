package qilin.parm.ctxcons;

import qilin.core.pag.AllocNode;
import soot.Context;
import soot.SootMethod;

public interface HeapCtxConstructor {
    Context constructCtx(SootMethod container, Context containerCtx, AllocNode heap);
}
