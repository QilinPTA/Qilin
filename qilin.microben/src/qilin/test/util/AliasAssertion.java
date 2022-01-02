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

package qilin.test.util;

import qilin.core.PTA;
import qilin.core.sets.PointsToSet;
import qilin.core.sets.PointsToSetInternal;
import qilin.pta.PTAConfig;
import qilin.util.PTAUtils;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

import java.util.Objects;

public class AliasAssertion implements IAssertion {
    private final PTA pta;
    private final SootMethod sm;
    private final Stmt stmt;
    private final Value va;
    private final Value vb;
    private final boolean groundTruth;

    public AliasAssertion(PTA pta, SootMethod sm, Stmt stmt, Value va, Value vb, boolean groundTruth) {
        this.pta = pta;
        this.sm = sm;
        this.stmt = stmt;
        this.va = va;
        this.vb = vb;
        this.groundTruth = groundTruth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AliasAssertion that = (AliasAssertion) o;
        return groundTruth == that.groundTruth &&
                Objects.equals(sm, that.sm) &&
                Objects.equals(stmt, that.stmt) &&
                Objects.equals(va, that.va) &&
                Objects.equals(vb, that.vb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sm, stmt, va, vb, groundTruth);
    }

    @Override
    public boolean check() {
        return isMayAlias(pta, va, vb) == groundTruth;
    }

    private static boolean DEBUG = true;

    protected boolean isMayAlias(PTA pta, Value va, Value vb) {
        if (va instanceof NullConstant && vb instanceof NullConstant) {
            return true;
        }
        if (va instanceof NullConstant || vb instanceof NullConstant) {
            return false;
        }
        if (va instanceof StringConstant && vb instanceof StringConstant) {
            return va.equals(vb);
        } else if (va instanceof StringConstant) {
            String s = ((StringConstant) va).value;
            if (!PTAConfig.v().getPtaConfig().stringConstants) {
                s = "STRING_NODE";
            }
            PointsToSet pts = ((PointsToSetInternal) pta.reachingObjects((Local) vb)).mapToCIPointsToSet();
            return pts.possibleStringConstants().contains(s);
        } else if (vb instanceof StringConstant) {
            String s = ((StringConstant) vb).value;
            if (!PTAConfig.v().getPtaConfig().stringConstants) {
                s = "STRING_NODE";
            }
            PointsToSet pts = ((PointsToSetInternal) pta.reachingObjects((Local) va)).mapToCIPointsToSet();
            for (String s1 : pts.possibleStringConstants()) {
                System.out.println(s1);
            }
            return pts.possibleStringConstants().contains(s);
        } else if (va instanceof ClassConstant) {
            PointsToSet pts = ((PointsToSetInternal) pta.reachingObjects((Local) vb)).mapToCIPointsToSet();
            return pts.possibleClassConstants().contains(va);
        } else if (vb instanceof ClassConstant) {
            PointsToSet pts = ((PointsToSetInternal) pta.reachingObjects((Local) va)).mapToCIPointsToSet();
            return pts.possibleClassConstants().contains(vb);
        }
        PointsToSetInternal pts1 = ((PointsToSetInternal) pta.reachingObjects((Local) va)).mapToCIPointsToSet();
        if (DEBUG) {
//            PTAUtils.dumpPts(pta, true);
//            PTAUtils.dumpPAG(pta.getPag(), "pag.txt");
            System.out.println("va points to: " + PTAUtils.getNodeLabel(pta.getPag().findLocalVarNode(va)) + pta.getPag().findLocalVarNode(va));
            PTAUtils.printPts(pts1);
        }
        PointsToSetInternal pts2 = ((PointsToSetInternal) pta.reachingObjects((Local) vb)).mapToCIPointsToSet();
        if (DEBUG) {
            System.out.println("vb points to: " + PTAUtils.getNodeLabel(pta.getPag().findLocalVarNode(vb)) + pta.getPag().findLocalVarNode(vb));
            PTAUtils.printPts(pts2);
        }
        return pts1.hasNonEmptyIntersection(pts2);
    }
}
