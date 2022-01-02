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
import soot.Type;
import soot.jimple.ClassConstant;

import java.util.Collections;
import java.util.Set;

/**
 * Implementation of an empty, immutable points-to set.
 *
 * @author Ondrej Lhotak
 */
public class EmptyPointsToSet extends PointsToSetInternal {
    private static EmptyPointsToSet instance = null;

    public EmptyPointsToSet() {
        super(null);
    }

    public static EmptyPointsToSet v() {
        if (instance == null) {
            instance = new EmptyPointsToSet();
        }
        return instance;
    }

    /**
     * Returns true if this set contains no run-time objects.
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Returns true if this set shares some objects with other.
     */
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        return false;
    }

    /**
     * Set of all possible run-time types of objects in the set.
     */
    public Set<Type> possibleTypes() {
        return Collections.emptySet();
    }

    /**
     * Adds contents of other into this set, returns true if this set changed.
     */
    public boolean addAll(PointsToSetInternal other, PointsToSetInternal exclude) {
        throw new RuntimeException("can't add into empty immutable set");
    }

    /**
     * Calls v's visit method on all nodes in this set.
     */
    public boolean forall(P2SetVisitor v) {
        return false;
    }

    /**
     * Adds n to this set, returns true if n was not already in this set.
     */
    public boolean add(Node n) {
        throw new RuntimeException("can't add into empty immutable set");
    }

    @Override
    public PointsToSetInternal mapToCIPointsToSet() {
        return instance;
    }

    /**
     * Returns true iff the set contains n.
     */
    public boolean contains(Node n) {
        return false;
    }

    public Set<String> possibleStringConstants() {
        return Collections.emptySet();
    }

    public Set<ClassConstant> possibleClassConstants() {
        return Collections.emptySet();
    }

    @Override
    public void clear() {

    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "{}";
    }

}
