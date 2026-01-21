package qilin.pta.toolkits.zipper.flowgraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZMergedNode<N> {
    private Set<ZMergedNode<N>> preds;
    private Set<ZMergedNode<N>> succs;
    private final List<N> content;

    public ZMergedNode(final List<N> content) {
        this.content = List.copyOf(content);
    }

    public void addPred(final ZMergedNode<N> pred) {
        if (this.preds == null) {
            this.preds = new HashSet<>(4);
        }
        this.preds.add(pred);
    }

    public Set<ZMergedNode<N>> getPreds() {
        return (this.preds == null) ? Collections.emptySet() : this.preds;
    }

    public void addSucc(final ZMergedNode<N> succ) {
        if (this.succs == null) {
            this.succs = new HashSet<>(4);
        }
        this.succs.add(succ);
    }

    public Set<ZMergedNode<N>> getSuccs() {
        return (this.succs == null) ? Collections.emptySet() : this.succs;
    }

    public List<N> getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return this.content.toString();
    }
}
