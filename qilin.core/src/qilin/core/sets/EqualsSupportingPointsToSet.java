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

/**
 * A points-to set supporting deep equals and hashCode operations.
 *
 * @author Eric Bodden
 * @see PointsToSetEqualsWrapper
 */
public interface EqualsSupportingPointsToSet extends PointsToSet {

    /**
     * Computes a hash code based on the contents of the points-to set. Note that hashCode() is not overwritten on purpose.
     * This is because Spark relies on comparison by object identity.
     */
    int pointsToSetHashCode();

    /**
     * Returns <code>true</code> if and only if other holds the same alloc nodes as this. Note that equals() is not overwritten
     * on purpose. This is because Spark relies on comparison by object identity.
     */
    boolean pointsToSetEquals(Object other);

}
