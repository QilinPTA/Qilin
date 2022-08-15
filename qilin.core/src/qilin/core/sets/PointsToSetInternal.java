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

package qilin.core.sets;

import qilin.core.pag.ClassConstantNode;
import qilin.core.pag.Node;
import qilin.core.pag.StringConstantNode;
import soot.Type;
import soot.jimple.ClassConstant;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for implementations of points-to sets.
 *
 * @author Ondrej Lhotak
 */
public abstract class PointsToSetInternal implements PointsToSet {

    protected Type type;
    protected PointsToSetInternal ciPointsToSet = null;

    public PointsToSetInternal(Type type) {
        this.type = type;
    }

    /**
     * Adds contents of other minus the contents of exclude into this set; returns true if this set changed.
     */
    public boolean addAll(PointsToSetInternal other, final PointsToSetInternal exclude) {
        if (other instanceof DoublePointsToSet dpts) {
            return addAll(dpts.getNewSet(), exclude) | addAll(dpts.getOldSet(), exclude);
        }
        return other.forall(new P2SetVisitor() {
            public void visit(Node n) {
                if (exclude == null || !exclude.contains(n)) {
                    returnValue = add(n) | returnValue;
                }
            }
        });
    }

    /**
     * Calls v's visit method on all nodes in this set.
     */
    public abstract boolean forall(P2SetVisitor v);

    /**
     * Adds n to this set, returns true if n was not already in this set.
     */
    public abstract boolean add(Node n);

    public abstract PointsToSetInternal mapToCIPointsToSet();

    /**
     * Sets all newly-added nodes to old nodes.
     */
    public void flushNew() {
    }

    /**
     * Returns true iff the set contains n.
     */
    public abstract boolean contains(Node n);

    public boolean hasNonEmptyIntersection(PointsToSet other) {
        final PointsToSetInternal o = (PointsToSetInternal) other;
        return forall(new P2SetVisitor() {
            public void visit(Node n) {
                if (o.contains(n)) {
                    returnValue = true;
                }
            }
        });
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int size() {
        final int[] ret = new int[1];
        forall(new P2SetVisitor() {
            public void visit(Node n) {
                ret[0]++;
            }
        });
        return ret[0];
    }

    public String toString() {
        final StringBuffer ret = new StringBuffer();
        this.forall(new P2SetVisitor() {
            public final void visit(Node n) {
                ret.append(n).append(",");
            }
        });
        return ret.toString();
    }

    public Set<String> possibleStringConstants() {
        final Set<String> ret = new HashSet<>();
        return this.forall(new P2SetVisitor() {
            public final void visit(Node n) {
                if (n instanceof StringConstantNode) {
                    ret.add(((StringConstantNode) n).getString());
                } else {
                    returnValue = true;
                }
            }
        }) ? null : ret;
    }

    public Set<ClassConstant> possibleClassConstants() {
        final Set<ClassConstant> ret = new HashSet<>();
        return this.forall(new P2SetVisitor() {
            public final void visit(Node n) {
                if (n instanceof ClassConstantNode) {
                    ret.add(((ClassConstantNode) n).getClassConstant());
                } else {
                    returnValue = true;
                }
            }
        }) ? null : ret;
    }

    public boolean pointsToSetEquals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PointsToSetInternal otherPts)) {
            return false;
        }

        // both sets are equal if they are supersets of each other
        return superSetOf(otherPts, this) && superSetOf(this, otherPts);
    }

    /**
     * Returns <code>true</code> if <code>onePts</code> is a (non-strict) superset of <code>otherPts</code>.
     */
    private boolean superSetOf(PointsToSetInternal onePts, final PointsToSetInternal otherPts) {
        return onePts.forall(new P2SetVisitorDefaultTrue() {

            public void visit(Node n) {
                returnValue = returnValue && otherPts.contains(n);
            }

        });
    }

    /**
     * A P2SetVisitor with a default return value of <code>true</code>.
     *
     * @author Eric Bodden
     */
    public static abstract class P2SetVisitorDefaultTrue extends P2SetVisitor {

        public P2SetVisitorDefaultTrue() {
            returnValue = true;
        }

    }

}
