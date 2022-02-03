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

package qilin.core.builder;

import qilin.CoreConfig;
import qilin.core.PTAScene;
import qilin.core.PointsToAnalysis;
import qilin.core.pag.*;
import qilin.util.PTAUtils;
import qilin.util.Pair;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JNewArrayExpr;

/**
 * @author Ondrej Lhotak
 */
public class MethodNodeFactory extends AbstractJimpleValueSwitch<Node> {

    protected PAG pag;
    protected MethodPAG mpag;
    protected SootMethod method;

    public MethodNodeFactory(PAG pag, MethodPAG mpag) {
        this.pag = pag;
        this.mpag = mpag;
        method = mpag.getMethod();
    }

    public Node getNode(Value v) {
        v.apply(this);
        return getNode();
    }

    /**
     * Adds the edges required for this statement to the graph.
     */
    final public void handleStmt(Stmt s) {
        if (s.containsInvokeExpr()) {
            mpag.invokeStmts.add(s);
            handleInvokeStmt(s);
        } else {
            handleIntraStmt(s);
        }
    }

    /**
     * Adds the edges required for this statement to the graph. Add throw stmt if
     * the invoke method throws an Exception.
     */
    protected void handleInvokeStmt(Stmt s) {
        InvokeExpr ie = s.getInvokeExpr();
        int numArgs = ie.getArgCount();
        for (int i = 0; i < numArgs; i++) {
            Value arg = ie.getArg(i);
            if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant) {
                continue;
            }
            arg.apply(this);
        }
        if (s instanceof AssignStmt) {
            Value l = ((AssignStmt) s).getLeftOp();
            if ((l.getType() instanceof RefLikeType)) {
                l.apply(this);
            }
        }
        if (ie instanceof InstanceInvokeExpr) {
            ((InstanceInvokeExpr) ie).getBase().apply(this);
        }
    }

    private void resolveClinit(StaticFieldRef staticFieldRef) {
        PTAUtils.clinitsOf(staticFieldRef.getField().getDeclaringClass()).forEach(mpag::addTriggeredClinit);
    }

    /**
     * Adds the edges required for this statement to the graph.
     */
    private void handleIntraStmt(Stmt s) {
        s.apply(new AbstractStmtSwitch<>() {
            public void caseAssignStmt(AssignStmt as) {
                Value l = as.getLeftOp();
                Value r = as.getRightOp();
                if (l instanceof StaticFieldRef) {
                    resolveClinit((StaticFieldRef) l);
                } else if (r instanceof StaticFieldRef) {
                    resolveClinit((StaticFieldRef) r);
                }

                if (!(l.getType() instanceof RefLikeType))
                    return;
                // check for improper casts, with mal-formed code we might get
                // l = (refliketype)int_type, if so just return
                if (r instanceof CastExpr && (!(((CastExpr) r).getOp().getType() instanceof RefLikeType))) {
                    return;
                }

                if (!(r.getType() instanceof RefLikeType))
                    throw new RuntimeException("Type mismatch in assignment (rhs not a RefLikeType) " + as
                            + " in method " + method.getSignature());
                Node dest = getNode(l);
                Node src = getNode(r);
                mpag.addInternalEdge(src, dest);
            }

            public void caseReturnStmt(ReturnStmt rs) {
                if (!(rs.getOp().getType() instanceof RefLikeType))
                    return;
                Node retNode = getNode(rs.getOp());
                mpag.addInternalEdge(retNode, caseRet());
            }

            public void caseIdentityStmt(IdentityStmt is) {
                if (!(is.getLeftOp().getType() instanceof RefLikeType)) {
                    return;
                }
                Node dest = getNode(is.getLeftOp());
                Node src = getNode(is.getRightOp());
                mpag.addInternalEdge(src, dest);
            }

            public void caseThrowStmt(ThrowStmt ts) {
                if (!CoreConfig.v().getPtaConfig().preciseExceptions) {
                    mpag.addInternalEdge(getNode(ts.getOp()), getNode(PTAScene.v().getFieldGlobalThrow()));
                }
            }
        });
    }

    final public Node getNode() {
        return getResult();
    }

    final public VarNode caseThis() {
        Type type = method.isStatic() ? RefType.v("java.lang.Object") : method.getDeclaringClass().getType();
        VarNode ret = pag.makeLocalVarNode(new Parm(method, PointsToAnalysis.THIS_NODE), type, method);
        ret.setInterProcTarget();
        return ret;
    }

    public VarNode caseParm(int index) {
        VarNode ret = pag.makeLocalVarNode(new Parm(method, index), method.getParameterType(index), method);
        ret.setInterProcTarget();
        return ret;
    }

    public VarNode caseRet() {
        VarNode ret = pag.makeLocalVarNode(new Parm(method, PointsToAnalysis.RETURN_NODE), method.getReturnType(),
                method);
        ret.setInterProcSource();
        return ret;
    }

    public VarNode caseMethodThrow() {
        VarNode ret = pag.makeLocalVarNode(new Parm(method, PointsToAnalysis.THROW_NODE), RefType.v("java.lang.Throwable"),
                method);
        ret.setInterProcSource();
        return ret;
    }

    final public FieldRefNode caseArray(VarNode base) {
        return pag.makeFieldRefNode(base, ArrayElement.v());
    }

    // OK, these ones are public, but they really shouldn't be; it's just
    // that Java requires them to be, because they override those other
    // public methods.
    @Override
    final public void caseArrayRef(ArrayRef ar) {
        caseLocal((Local) ar.getBase());
        setResult(caseArray((VarNode) getNode()));
    }

    final public void caseCastExpr(CastExpr ce) {
        Node opNode = getNode(ce.getOp());
        Node castNode = pag.makeLocalVarNode(ce, ce.getCastType(), method);
        mpag.addInternalEdge(opNode, castNode);
        setResult(castNode);
    }

    @Override
    final public void caseCaughtExceptionRef(CaughtExceptionRef cer) {
        if (CoreConfig.v().getPtaConfig().preciseExceptions) {
            // we model caughtException expression as an local assignment.
            setResult(pag.makeLocalVarNode(cer, cer.getType(), method));
        } else {
            setResult(getNode(PTAScene.v().getFieldGlobalThrow()));
        }
    }

    @Override
    final public void caseInstanceFieldRef(InstanceFieldRef ifr) {
        SootField sf = ifr.getField();
        if (sf == null) {
            sf = new SootField(ifr.getFieldRef().name(), ifr.getType(), Modifier.PUBLIC);
            sf.setNumber(Scene.v().getFieldNumberer().size());
            Scene.v().getFieldNumberer().add(sf);
            System.out.println("Warnning:" + ifr + " is resolved to be a null field in Scene.");
        }
        setResult(pag.makeFieldRefNode(pag.makeLocalVarNode(ifr.getBase(), ifr.getBase().getType(), method), new Field(sf)));
    }

    @Override
    final public void caseLocal(Local l) {
        setResult(pag.makeLocalVarNode(l, l.getType(), method));
    }

    @Override
    final public void caseNewArrayExpr(NewArrayExpr nae) {
        setResult(pag.makeAllocNode(nae, nae.getType(), method));
    }

    @Override
    final public void caseNewExpr(NewExpr ne) {
        SootClass cl = PTAScene.v().loadClassAndSupport(ne.getType().toString());
        PTAUtils.clinitsOf(cl).forEach(mpag::addTriggeredClinit);
        setResult(pag.heapAbstractor().abstractHeap(ne, ne.getType(), method));
    }

    @Override
    final public void caseNewMultiArrayExpr(NewMultiArrayExpr nmae) {
        ArrayType type = (ArrayType) nmae.getType();
        int pos = 0;
        AllocNode prevAn = pag.heapAbstractor().abstractHeap(new JNewArrayExpr(type, nmae.getSize(pos)), type, method);
        VarNode prevVn = pag.makeLocalVarNode(prevAn.getNewExpr(), prevAn.getType(), method);
        mpag.addInternalEdge(prevAn, prevVn); // new
        setResult(prevAn);
        while (true) {
            Type t = type.getElementType();
            if (!(t instanceof ArrayType)) {
                break;
            }
            type = (ArrayType) t;
            ++pos;
            Value sizeVal;
            if (pos < nmae.getSizeCount()) {
                sizeVal = nmae.getSize(pos);
            } else {
                sizeVal = IntConstant.v(1);
            }
            AllocNode an = pag.heapAbstractor().abstractHeap(new JNewArrayExpr(type, sizeVal), type, method);
            VarNode vn = pag.makeLocalVarNode(an.getNewExpr(), an.getType(), method);
            mpag.addInternalEdge(an, vn); // new
            mpag.addInternalEdge(vn, pag.makeFieldRefNode(prevVn, ArrayElement.v())); // store
            prevVn = vn;
        }
    }

    @Override
    final public void caseParameterRef(ParameterRef pr) {
        setResult(caseParm(pr.getIndex()));
    }

    @Override
    final public void caseStaticFieldRef(StaticFieldRef sfr) {
        setResult(pag.makeGlobalVarNode(sfr.getField(), sfr.getField().getType()));
    }

    @Override
    final public void caseThisRef(ThisRef tr) {
        setResult(caseThis());
    }

    @Override
    final public void caseNullConstant(NullConstant nr) {
        setResult(null);
    }

    @Override
    final public void caseStringConstant(StringConstant sc) {
        AllocNode stringConstantNode = pag.makeStringConstantNode(sc);
        VarNode stringConstantVar = pag.makeGlobalVarNode(sc, RefType.v("java.lang.String"));
        pag.addGlobalPAGEdge(stringConstantNode, stringConstantVar);
        VarNode vn = pag.makeLocalVarNode(new Pair<>(method, sc), RefType.v("java.lang.String"), method);
        mpag.addInternalEdge(stringConstantVar, vn);
        setResult(vn);
    }

    @Override
    final public void caseClassConstant(ClassConstant cc) {
        AllocNode classConstant = pag.makeClassConstantNode(cc);
        VarNode classConstantVar = pag.makeGlobalVarNode(cc, RefType.v("java.lang.Class"));
        pag.addGlobalPAGEdge(classConstant, classConstantVar);
        VarNode vn = pag.makeLocalVarNode(new Pair<>(method, cc), RefType.v("java.lang.Class"), method);
        mpag.addInternalEdge(classConstantVar, vn);
        setResult(vn);
    }

    @Override
    final public void defaultCase(Object v) {
        throw new RuntimeException("failed to handle " + v);
    }
}
