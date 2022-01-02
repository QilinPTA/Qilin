package qilin.pta.toolkits.zipper.flowgraph;

import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;

import java.util.Objects;

public class InstanceFieldNode extends Node {
    private final AllocNode base;
    private final SparkField field;

    public InstanceFieldNode(final AllocNode base, final SparkField field) {
        this.base = base;
        this.field = field;
    }

    public AllocNode getBase() {
        return this.base;
    }

    public SparkField getField() {
        return this.field;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.base, this.field);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof final InstanceFieldNode anoNode)) {
            return false;
        }
        return this.base.equals(anoNode.base) && this.field.equals(anoNode.field);
    }

    @Override
    public String toString() {
        return "InstanceFieldNode: [" + this.base + "] " + this.field;
    }
}
