package qilin.pta.toolkits.moon.ObjCollection;

import qilin.core.pag.Node;

import java.util.Objects;

public class CheckTrace {

    private final Node node;
    private final CheckStatus state;

    public CheckTrace(Node node, CheckStatus state) {
        this.node = node;
        this.state = state;
    }

    public Node getNode() {
        return node;
    }

    public CheckStatus getState() {
        return state;
    }
    @Override
    public int hashCode() {
        return Objects.hash(node, state);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof CheckTrace other){
            if(o == this) return true;
            return this.node.equals(other.node) && this.state.equals(other.state);
        }
        return false;
    }

}
