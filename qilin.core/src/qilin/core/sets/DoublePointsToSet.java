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

import qilin.core.pag.Node;
import qilin.core.pag.PAG;
import soot.Type;

/**
 * Implementation of points-to set that holds two sets: one for new elements that have not yet been propagated, and the other
 * for elements that have already been propagated.
 *
 * @author Ondrej Lhotak
 */
public class DoublePointsToSet extends PointsToSetInternal {
    public static DoublePointsToSet emptySet = new DoublePointsToSet(null, null);

    protected PointsToSetInternal newSet;
    protected PointsToSetInternal oldSet;
    private final PAG pag;

    public DoublePointsToSet(Type type, PAG pag) {
        super(type);
        this.pag = pag;
        newSet = new HybridPointsToSet(type, pag);
        oldSet = new HybridPointsToSet(type, pag);
    }

    public static P2SetFactory getFactory() {

        return new P2SetFactory() {
            public PointsToSetInternal newSet(Type type, PAG pag) {
                return new DoublePointsToSet(type, pag);
            }
        };
    }

    /**
     * Returns true if this set contains no run-time objects.
     */
    public boolean isEmpty() {
        return oldSet.isEmpty() && newSet.isEmpty();
    }

    /**
     * Returns true if this set shares some objects with other.
     */
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        return oldSet.hasNonEmptyIntersection(other) || newSet.hasNonEmptyIntersection(other);
    }

    /*
     * Empty this set.
     * */
    @Override
    public void clear() {
        oldSet.clear();
        newSet.clear();
    }

    /**
     * Adds contents of other into this set, returns true if this set changed.
     */
    public boolean addAll(PointsToSetInternal other, PointsToSetInternal exclude) {
        if (exclude != null) {
            throw new RuntimeException("NYI");
        }
        return newSet.addAll(other, oldSet);
    }

    /**
     * Calls v's visit method on all nodes in this set.
     */
    public boolean forall(P2SetVisitor v) {
        oldSet.forall(v);
        newSet.forall(v);
        return v.getReturnValue();
    }

    /**
     * Adds n to this set, returns true if n was not already in this set.
     */
    public boolean add(Node n) {
        if (oldSet.contains(n)) {
            return false;
        }
        return newSet.add(n);
    }

    /**
     * Returns set of nodes already present before last call to flushNew.
     */
    public PointsToSetInternal getOldSet() {
        return oldSet;
    }

    @Override
    public PointsToSetInternal mapToCIPointsToSet() {
        if (ciPointsToSet == null) {
            DoublePointsToSet ret = new DoublePointsToSet(type, pag);
            ret.newSet.addAll(newSet.mapToCIPointsToSet(), null);
            ret.oldSet.addAll(oldSet.mapToCIPointsToSet(), null);
            ciPointsToSet = ret;
        }
        return ciPointsToSet;
    }

    /**
     * Returns set of newly-added nodes since last call to flushNew.
     */
    public PointsToSetInternal getNewSet() {
        return newSet;
    }

    /**
     * Sets all newly-added nodes to old nodes.
     */
    public void flushNew() {
        oldSet.addAll(newSet, null);
        newSet = new HybridPointsToSet(type, pag);
    }

    /**
     * Returns true iff the set contains n.
     */
    public boolean contains(Node n) {
        return oldSet.contains(n) || newSet.contains(n);
    }
}
