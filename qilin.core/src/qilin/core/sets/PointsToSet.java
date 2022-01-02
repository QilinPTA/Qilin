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

import soot.Type;
import soot.jimple.ClassConstant;

import java.util.Set;

/**
 * A generic interface to some set of runtime objects computed by a pointer analysis.
 *
 * @author Ondrej Lhotak
 */
public interface PointsToSet {
    /**
     * Returns true if this set contains no run-time objects.
     */
    boolean isEmpty();

    /**
     * Returns true if this set shares some objects with other.
     */
    boolean hasNonEmptyIntersection(PointsToSet other);

    /**
     * Set of all possible run-time types of objects in the set.
     */
    Set<Type> possibleTypes();

    /**
     * If this points-to set consists entirely of string constants, returns a set of these constant strings. If this point-to
     * set may contain something other than constant strings, returns null.
     */
    Set<String> possibleStringConstants();

    /**
     * If this points-to set consists entirely of objects of type java.lang.Class of a known class, returns a set of
     * ClassConstant's that are these classes. If this point-to set may contain something else, returns null.
     */
    Set<ClassConstant> possibleClassConstants();

    /**
     * Size of objects in this set.
     *
     * @author Dongjie He
     */
    int size();

    /*
     * Empty this set.
     *
     * @author Dongjie He
     * */
    void clear();
}
