/* Bean - Making k-Object-Sensitive Pointer Analysis More Precise with Still k-Limiting
 *
 * Copyright (C) 2016 Tian Tan, Yue Li, Jingling Xue
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package qilin.pta.toolkits.bean.oag;

import qilin.core.pag.AllocNode;

import java.util.HashSet;
import java.util.Set;

public class Node {

    /**
     * The heap that this node represents.
     */
    private final AllocNode heap;
    private final Set<Node> preds = new HashSet<>();
    private final Set<Node> succs = new HashSet<>();
    private int indegree = 0;
    private int outdegree = 0;
    private Set<Node> reachableNodes = null;

    public Node(AllocNode heap) {
        this.heap = heap;
    }

    public AllocNode getHeap() {
        return heap;
    }

    @Override
    public int hashCode() {
        return heap.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Node anoNode)) {
            return false;
        }
        return heap.equals(anoNode.heap);
    }

    @Override
    public String toString() {
        return heap.toString();
    }

    public Set<Node> getPreds() {
        return preds;
    }

    public Set<Node> getSuccs() {
        return succs;
    }

    int getInDegree() {
        return indegree;
    }

    int getOutDegree() {
        return outdegree;
    }

    public void addSucc(Node succNode) {
        if (succs.add(succNode)) {
            incOutDegree();
        }
        if (succNode.getPreds().add(this)) {
            succNode.incInDegree();
        }
    }

    Set<Node> getReachableNodes() {
        return reachableNodes;
    }

    void setReachableNodes(Set<Node> reachableNodes) {
        this.reachableNodes = reachableNodes;
    }

    private void incInDegree() {
        ++indegree;
    }

    private void incOutDegree() {
        ++outdegree;
    }

}
