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

package qilin.core.pag;

import qilin.CoreConfig;
import qilin.core.builder.MethodNodeFactory;
import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.util.Chain;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

import java.util.*;

/**
 * Part of a pointer assignment graph for a single method.
 *
 * @author Ondrej Lhotak
 */
public class MethodPAG {
    private final ChunkedQueue<Node> internalEdges = new ChunkedQueue<>();
    private final QueueReader<Node> internalReader = internalEdges.reader();
    private final Set<SootMethod> clinits = new HashSet<>();
    private final Collection<Unit> invokeStmts = new HashSet<>();
    public Body body;
    /**
     * Since now the exception analysis is handled on-the-fly, we should record the
     * exception edges explicitly for Eagle and Turner.
     */
    private final Map<Node, Set<Node>> exceptionEdges = new HashMap<>();
    protected PAG pag;
    protected MethodNodeFactory nodeFactory;
    SootMethod method;
    /*
     * List[i-1] is wrappered in List[i].
     * We have to extend the following structure from Map<Node, List<Trap>> to
     * Map<Node, Map<Stmt, List<Trap>>> because there exists cases where the same
     * node are thrown more than once and lies in different catch blocks.
     * */
    public final Map<Stmt, List<Trap>> stmt2wrapperedTraps = new HashMap<>();
    public final Map<Node, Map<Stmt, List<Trap>>> node2wrapperedTraps = new HashMap<>();

    public MethodPAG(PAG pag, SootMethod m, Body body) {
        this.pag = pag;
        this.method = m;
        this.nodeFactory = new MethodNodeFactory(pag, this);
        this.body = body;
        build();
    }

    public PAG pag() {
        return pag;
    }

    public SootMethod getMethod() {
        return method;
    }

    public MethodNodeFactory nodeFactory() {
        return nodeFactory;
    }

    public Collection<Unit> getInvokeStmts() {
        return invokeStmts;
    }

    public boolean addCallStmt(Unit unit) {
        return this.invokeStmts.add(unit);
    }

    protected void build() {
        // this method is invalid but exists in pmd-deps.jar
        if (method.getSignature().equals("<org.apache.xerces.parsers.XML11Configuration: boolean getFeature0(java.lang.String)>")) {
            return;
        }
        buildException();
        buildNormal();
        addMiscEdges();
    }

    protected void buildNormal() {
        if (method.isStatic()) {
            PTAUtils.clinitsOf(method.getDeclaringClass()).forEach(this::addTriggeredClinit);
        }
        for (Unit unit : body.getUnits()) {
            try {
                nodeFactory.handleStmt((Stmt) unit);
            } catch (Exception e) {
                System.out.println("Warning:" + e);
            }
        }
    }

    protected void buildException() {
        // we use the same logic as doop (library/exceptions/precise.logic).
        if (!CoreConfig.v().getPtaConfig().preciseExceptions) {
            return;
        }
        Chain<Trap> traps = body.getTraps();
        PatchingChain<Unit> units = body.getUnits();
        Set<Unit> inTraps = new HashSet<>();
        /*
         * The traps is already visited in order. <a>, <b>; implies <a> is a previous Trap of <b>.
         * */
        traps.forEach(trap -> {
            units.iterator(trap.getBeginUnit(), trap.getEndUnit()).forEachRemaining(unit -> {
                if (unit == trap.getEndUnit()) {
                    return;
                }
                inTraps.add(unit);
                Stmt stmt = (Stmt) unit;
                Node src = null;
                if (stmt.containsInvokeExpr()) {
                    // note, method.getExceptions() does not return implicit exceptions.
                    src = pag.makeInvokeStmtThrowVarNode(stmt, method);
                } else if (stmt instanceof ThrowStmt ts) {
                    src = nodeFactory.getNode(ts.getOp());
                }
                if (src != null) {
                    addStmtTrap(src, stmt, trap);
                }
            });
        });

        for (Unit unit : body.getUnits()) {
            if (inTraps.contains(unit)) {
                continue;
            }
            Stmt stmt = (Stmt) unit;
            Node src = null;
            if (stmt.containsInvokeExpr()) {
                src = pag.makeInvokeStmtThrowVarNode(stmt, method);
            } else if (stmt instanceof ThrowStmt ts) {
                src = nodeFactory.getNode(ts.getOp());
            }
            if (src != null) {
                node2wrapperedTraps.computeIfAbsent(src, k -> new HashMap<>());
                stmt2wrapperedTraps.computeIfAbsent(stmt, k -> new ArrayList<>());
            }
        }
        boolean DEBUG = false;
        if (DEBUG) {
            if (method.toString().equals("<sun.security.util.SignatureFileVerifier: void <init>(java.util.ArrayList,sun.security.util.ManifestDigester,java.lang.String,byte[])>")) {
                node2wrapperedTraps.forEach((k, map) -> {
                    System.out.println(k);
                    for (Stmt stmt : map.keySet()) {
                        System.out.println("\t" + stmt + ":");
                        for (Trap trap : map.get(stmt)) {
                            System.out.println("\t\t" + trap);
                        }
                    }
                });
            }
        }
    }

    private void addStmtTrap(Node src, Stmt stmt, Trap trap) {
        Map<Stmt, List<Trap>> stmt2Traps = node2wrapperedTraps.computeIfAbsent(src, k -> new HashMap<>());
        List<Trap> trapList = stmt2Traps.computeIfAbsent(stmt, k -> new ArrayList<>());
        trapList.add(trap);
        stmt2wrapperedTraps.computeIfAbsent(stmt, k -> new ArrayList<>()).add(trap);
    }

    protected void addMiscEdges() {
        if (method.getSignature().equals("<java.lang.ref.Reference: void <init>(java.lang.Object,java.lang.ref.ReferenceQueue)>")) {
            // Implements the special status of java.lang.ref.Reference just as in Doop (library/reference.logic).
            StaticFieldRef sfr = Jimple.v().newStaticFieldRef(RefType.v("java.lang.ref.Reference").getSootClass().getFieldByName("pending").makeRef());
            addInternalEdge(nodeFactory.caseThis(), nodeFactory.getNode(sfr));
        }
    }

    public void addInternalEdge(Node src, Node dst) {
        if (src == null) {
            return;
        }
        internalEdges.add(src);
        internalEdges.add(dst);
    }

    public QueueReader<Node> getInternalReader() {
        return internalReader;
    }

    public void addTriggeredClinit(SootMethod clinit) {
        clinits.add(clinit);
    }

    public Iterator<SootMethod> triggeredClinits() {
        return clinits.iterator();
    }

    public void addExceptionEdge(Node from, Node to) {
        this.exceptionEdges.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    public Map<Node, Set<Node>> getExceptionEdges() {
        return this.exceptionEdges;
    }
}
