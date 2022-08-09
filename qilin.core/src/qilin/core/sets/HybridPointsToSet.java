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

import qilin.core.pag.AllocNode;
import qilin.core.pag.Node;
import qilin.core.pag.PAG;
import qilin.util.PTAUtils;
import soot.Type;
import soot.util.BitSetIterator;
import soot.util.BitVector;

import java.util.Arrays;

/**
 * Hybrid implementation of points-to set, which uses an explicit array for small sets, and a bit vector for large sets.
 *
 * @author Ondrej Lhotak
 */
public final class HybridPointsToSet extends PointsToSetInternal {
    private final Node[] nodes = new Node[16];
    private BitVector bits = null;
    private final PAG pag;

    private boolean empty = true;

    public HybridPointsToSet(Type type, PAG pag) {
        super(type);
        this.pag = pag;
    }

    /**
     * Returns true if this set contains no run-time objects.
     */
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void clear() {
        Arrays.fill(nodes, null);
        bits = null;
        empty = true;
    }

    private boolean superAddAll(PointsToSetInternal other, PointsToSetInternal exclude) {
        boolean ret = super.addAll(other, exclude);
        if (ret) {
            empty = false;
        }
        return ret;
    }

    private boolean nativeAddAll(HybridPointsToSet other, HybridPointsToSet exclude) {
        return other.forall(new P2SetVisitor() {
            @Override
            public void visit(Node n) {
                if (exclude == null || !exclude.contains(n)) {
                    this.returnValue |= add(n);
                }
            }
        });
    }

    /**
     * Adds contents of other into this set, returns true if this set changed.
     */
    public boolean addAll(final PointsToSetInternal other, final PointsToSetInternal exclude) {
        if (other != null && !(other instanceof HybridPointsToSet)) {
            return superAddAll(other, exclude);
        }
        if (exclude != null && !(exclude instanceof HybridPointsToSet)) {
            return superAddAll(other, exclude);
        }
        assert other != null;
        return nativeAddAll((HybridPointsToSet) other, (HybridPointsToSet) exclude);
    }

    /**
     * Calls v's visit method on all nodes in this set.
     */
    public boolean forall(P2SetVisitor v) {
        if (bits == null) {
            for (Node node : nodes) {
                if (node == null) {
                    return v.getReturnValue();
                }
                v.visit(node);
            }
        } else {
            for (BitSetIterator it = bits.iterator(); it.hasNext(); ) {
                v.visit(pag.getAllocNodeNumberer().get(it.next()));
            }
        }
        return v.getReturnValue();
    }

    /**
     * Adds n to this set, returns true if n was not already in this set.
     */
    public boolean add(Node n) {
        if (PTAUtils.castNeverFails(n.getType(), type)) {
            return fastAdd(n);
        }
        return false;
    }

    /**
     * Returns true iff the set contains n.
     */
    public boolean contains(Node n) {
        if (bits == null) {
            for (Node node : nodes) {
                if (node == n) {
                    return true;
                }
                if (node == null) {
                    break;
                }
            }
            return false;
        } else {
            return bits.get(n.getNumber());
        }
    }

    private boolean fastAdd(Node n) {
        if (bits == null) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] == null) {
                    empty = false;
                    nodes[i] = n;
                    return true;
                } else if (nodes[i] == n) {
                    return false;
                }
            }
            convertToBits();
        }
        boolean ret = bits.set(n.getNumber());
        if (ret) {
            empty = false;
        }
        return ret;
    }

    private void convertToBits() {
        if (bits != null) {
            return;
        }
        bits = new BitVector(pag.getAllocNodeNumberer().size());
        for (Node node : nodes) {
            if (node != null) {
                fastAdd(node);
            }
        }
    }

    @Override
    public PointsToSetInternal mapToCIPointsToSet() {
        if (ciPointsToSet == null) {
            PointsToSetInternal ret = new HybridPointsToSet(type, pag);
            this.forall(new P2SetVisitor() {
                @Override
                public void visit(Node n) {
                    AllocNode heap = (AllocNode) n;
                    ret.add(heap.base());
                }
            });
            ciPointsToSet = ret;
        }
        return ciPointsToSet;
    }
}
