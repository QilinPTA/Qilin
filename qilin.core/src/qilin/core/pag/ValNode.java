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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qilin.core.sets.DoublePointsToSet;
import soot.Type;
import soot.util.Numberable;

/**
 * Represents a simple of pointer node in the pointer assignment graph.
 */
public class ValNode extends Node implements Comparable, Numberable {
    private static final Logger logger = LoggerFactory.getLogger(ValNode.class);
    protected int finishingNumber;

    protected ValNode(Type t) {
        super(t);
        PAG.getValNodeNumberer().add(this);
        this.finishingNumber = PAG.nextFinishNumber();
    }

    public int compareTo(Object o) {
        ValNode other = (ValNode) o;
        if (other.finishingNumber == finishingNumber && other != this) {
            logger.debug("" + "This is: " + this + " with id " + getNumber() + " and number " + finishingNumber);
            logger.debug("" + "Other is: " + other + " with id " + other.getNumber() + " and number " + other.finishingNumber);
            throw new RuntimeException("Comparison error");
        }
        return other.finishingNumber - finishingNumber;
    }

    /**
     * Returns the points-to set for this node.
     */
    public DoublePointsToSet getP2Set() {
        if (p2set != null) {
            return p2set;
        } else {
            return DoublePointsToSet.emptySet;
        }
    }

    /**
     * Delete current points-to set and make a new one
     */
    public void discardP2Set() {
        p2set = null;
    }

    /**
     * Returns the points-to set for this node, makes it if necessary.
     */
    public DoublePointsToSet makeP2Set() {
        if (p2set == null) {
            synchronized (this) {
                if (p2set == null) {
                    p2set = new DoublePointsToSet(type);
                }
            }
        }
        return p2set;
    }
}
