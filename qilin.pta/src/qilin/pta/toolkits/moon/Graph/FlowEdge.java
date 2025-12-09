package qilin.pta.toolkits.moon.Graph;


import qilin.core.pag.Node;

import java.util.Objects;

public class FlowEdge extends AbsEdge<Node> {

    private final FlowKind flowKind;

    public FlowEdge(Node src, Node tgt, FlowKind flowKind){
        super(src, tgt);
        this.flowKind = flowKind;
    }

    public FlowKind flowKind(){
        return flowKind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlowEdge edge = (FlowEdge) o;
        return source().equals(edge.source()) && target().equals(edge.target()) && flowKind.equals(edge.flowKind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source(), target(), flowKind);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[" + flowKind + "]"
                + "{" + source() + " -> " + target() + '}';
    }
}
