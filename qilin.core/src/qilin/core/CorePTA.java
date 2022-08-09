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

import qilin.core.pag.*;
import qilin.core.sets.DoublePointsToSet;
import qilin.core.sets.PointsToSet;
import qilin.core.sets.PointsToSetInternal;
import qilin.core.solver.Propagator;
import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.heapabst.HeapAbstractor;
import qilin.parm.select.CtxSelector;
import soot.*;

/*
 * This represents a parameterized PTA which could be concreted to many pointer analyses.
 * */
public abstract class CorePTA extends PTA {
    /*
     * The following three parameterized functions must be initialized before doing the pointer analysis.
     * */
    protected CtxConstructor ctxCons;
    protected CtxSelector ctxSel;
    protected HeapAbstractor heapAbst;

    public CtxSelector ctxSelector() {
        return ctxSel;
    }

    public void setContextSelector(CtxSelector ctxSelector) {
        this.ctxSel = ctxSelector;
    }

    public HeapAbstractor heapAbstractor() {
        return heapAbst;
    }

    public CtxConstructor ctxConstructor() {
        return ctxCons;
    }


    public abstract Propagator getPropagator();

    @Override
    public Context createCalleeCtx(MethodOrMethodContext caller, AllocNode receiverNode, CallSite callSite, SootMethod target) {
        return ctxCons.constructCtx(caller, (ContextAllocNode) receiverNode, callSite, target);
    }

    public Context emptyContext() {
        return CtxConstructor.emptyContext;
    }

    @Override
    public Node parameterize(Node n, Context context) {
        if (context == null) {
            throw new RuntimeException("null context!!!");
        }
        if (n instanceof LocalVarNode) {
            return parameterize((LocalVarNode) n, context);
        }
        if (n instanceof FieldRefNode) {
            return parameterize((FieldRefNode) n, context);
        }
        if (n instanceof AllocNode) {
            return parameterize((AllocNode) n, context);
        }
        if (n instanceof FieldValNode) {
            return parameterize((FieldValNode) n, context);
        }
        if (n instanceof GlobalVarNode) {
            return pag.makeContextVarNode((GlobalVarNode) n, emptyContext());
        }
        throw new RuntimeException("cannot parameterize this node: " + n);
    }

    public ContextField parameterize(FieldValNode fvn, Context context) {
        Context ctx = ctxSel.select(fvn, context);
        return pag.makeContextField(ctx, fvn);
    }

    protected ContextVarNode parameterize(LocalVarNode vn, Context context) {
        Context ctx = ctxSel.select(vn, context);
        return pag.makeContextVarNode(vn, ctx);
    }

    protected FieldRefNode parameterize(FieldRefNode frn, Context context) {
        return pag.makeFieldRefNode((VarNode) parameterize(frn.getBase(), context), frn.getField());
    }

    protected ContextAllocNode parameterize(AllocNode node, Context context) {
        Context ctx = ctxSel.select(node, context);
        return pag.makeContextAllocNode(node, ctx);
    }

    /**
     * Finds or creates the ContextMethod for method and context.
     */
    @Override
    public MethodOrMethodContext parameterize(SootMethod method, Context context) {
        Context ctx = ctxSel.select(method, context);
        return pag.makeContextMethod(ctx, method);
    }

    public AllocNode getRootNode() {
        return rootNode;
    }


    /**
     * Returns the set of objects pointed to by variable l.
     */
    @Override
    public PointsToSet reachingObjects(Local l) {
        // find all context nodes, and collect their answers
        final PointsToSetInternal ret = setFactory.newSet(l.getType(), pag);
        pag.getVarNodes(l).forEach(vn -> {
            ret.addAll(vn.getP2Set(), null);
        });
        return ret;
    }

    /**
     * Returns the set of objects pointed by n:
     * case 1: n is an insensitive node, return objects pointed by n under every possible context.
     * case 2: n is a context-sensitive node, return objects pointed by n under the given context.
     */
    public PointsToSet reachingObjects(Node n) {
        if (n instanceof ContextVarNode cvn) {
            return cvn.getP2Set();
        } else {
            VarNode varNode = (VarNode) n;
            if (pag.getContextVarNodeMap().containsKey(varNode)) {
                final PointsToSetInternal ret = setFactory.newSet(n.getType(), pag);
                pag.getContextVarNodeMap().get(varNode).values().forEach(vn -> {
                    ret.addAll(vn.getP2Set(), null);
                });
                return ret;
            } else {
                return DoublePointsToSet.emptySet;
            }
        }
    }

    /**
     * Returns the set of objects pointed to by elements of the arrays in the
     * PointsToSet s.
     */
    @Override
    public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
        return reachingObjectsInternal(s, ArrayElement.v());
    }

    /**
     * Returns the set of objects pointed to by variable l in context c.
     */
    @Override
    public PointsToSet reachingObjects(Context c, Local l) {
        VarNode n = pag.findContextVarNode(l, c);
        if (n == null) {
            return DoublePointsToSet.emptySet;
        }
        return n.getP2Set();
    }

    /**
     * Returns the set of objects pointed to by instance field f of the objects
     * pointed to by l.
     */
    @Override
    public PointsToSet reachingObjects(Local l, SootField f) {
        return reachingObjects(reachingObjects(l), f);
    }

    /**
     * Returns the set of objects pointed to by instance field f of the objects in
     * the PointsToSet s.
     */
    @Override
    public PointsToSet reachingObjects(PointsToSet s, final SootField f) {
        if (f.isStatic()) {
            throw new RuntimeException("The parameter f must be an *instance* field.");
        }
        return reachingObjectsInternal(s, new Field(f));
    }

    /**
     * Returns the set of objects pointed to by instance field f of the objects
     * pointed to by l in context c.
     */
    @Override
    public PointsToSet reachingObjects(Context c, Local l, SootField f) {
        return reachingObjects(reachingObjects(c, l), f);
    }

    @Override
    public PointsToSet reachingObjects(SootField f) {
        if (!f.isStatic()) {
            final PointsToSetInternal ret = setFactory.newSet((f).getType(), pag);
            SparkField sparkField = new Field(f);
            pag.getContextFieldVarNodeMap().values().stream().filter(map -> map.containsKey(sparkField)).forEach(map -> {
                ContextField contextField = map.get(sparkField);
                ret.addAll(contextField.getP2Set(), null);
            });
            return ret;
        }

        VarNode n = pag.findGlobalVarNode(f);
        if (n == null) {
            return DoublePointsToSet.emptySet;
        }
        return n.getP2Set();
    }

    @Override
    public PointsToSet reachingObjectsInternal(PointsToSet s, final SparkField f) {
        PointsToSetInternal bases = (PointsToSetInternal) s;
        final PointsToSetInternal ret = setFactory.newSet((f instanceof SootField) ? ((SootField) f).getType() : null, pag);
        pag.getContextFieldVarNodeMap().values().stream().filter(map -> map.containsKey(f)).forEach(map -> {
            ContextField contextField = map.get(f);
            AllocNode base = contextField.getBase();
            if (bases.contains(base)) {
                ret.addAll(contextField.getP2Set(), null);
            }
        });
        return ret;
    }

    @Override
    public boolean mayAlias(Local l1, Local l2) {
        PointsToSet pts1 = ((PointsToSetInternal) reachingObjects(l1)).mapToCIPointsToSet();
        PointsToSet pts2 = ((PointsToSetInternal) reachingObjects(l2)).mapToCIPointsToSet();
        return pts1.hasNonEmptyIntersection(pts2);
    }
}
