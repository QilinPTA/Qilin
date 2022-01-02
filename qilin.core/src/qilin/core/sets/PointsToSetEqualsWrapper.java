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
 * A decorator that implements equals/hashCode for {@link PointsToSet} supporting the {@link EqualsSupportingPointsToSet}
 * interface.
 *
 * @author Eric Bodden
 */
public class PointsToSetEqualsWrapper implements PointsToSet {

    protected EqualsSupportingPointsToSet pts;

    public PointsToSetEqualsWrapper(EqualsSupportingPointsToSet pts) {
        this.pts = pts;
    }

    @Override
    public int hashCode() {
        return pts.pointsToSetHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj || this.pts == obj) {
            return true;
        }

        // unwrap other
        obj = unwrapIfNecessary(obj);
        return pts.pointsToSetEquals(obj);
    }

    public boolean hasNonEmptyIntersection(PointsToSet other) {
        // unwrap other
        other = (PointsToSet) unwrapIfNecessary(other);
        return pts.hasNonEmptyIntersection(other);
    }

    public boolean isEmpty() {
        return pts.isEmpty();
    }

    public Set<ClassConstant> possibleClassConstants() {
        return pts.possibleClassConstants();
    }

    public Set<String> possibleStringConstants() {
        return pts.possibleStringConstants();
    }

    public Set<Type> possibleTypes() {
        return pts.possibleTypes();
    }

    protected Object unwrapIfNecessary(Object obj) {
        if (obj instanceof PointsToSetEqualsWrapper wrapper) {
            obj = wrapper.pts;
        }
        return obj;
    }

    @Override
    public String toString() {
        return pts.toString();
    }

    @Override
    public int size() {
        return pts.size();
    }

    @Override
    public void clear() {
        pts.clear();
    }
}
