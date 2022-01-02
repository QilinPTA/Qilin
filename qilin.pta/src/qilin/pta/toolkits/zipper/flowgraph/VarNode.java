package qilin.pta.toolkits.zipper.flowgraph;

public class VarNode extends Node {
    private final qilin.core.pag.VarNode var;

    public VarNode(final qilin.core.pag.VarNode var) {
        this.var = var;
    }

    public qilin.core.pag.VarNode getVar() {
        return this.var;
    }

    @Override
    public int hashCode() {
        return this.var.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof final VarNode anoNode)) {
            return false;
        }
        return this.var.equals(anoNode.var);
    }

    @Override
    public String toString() {
        return "VarNode: <" + this.var + ">";
    }
}
