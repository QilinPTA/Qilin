package qilin.pta.toolkits.moon.Graph;


import qilin.core.pag.Node;
import soot.jimple.spark.pag.SparkField;

public class FieldEdge extends FlowEdge{
    private final SparkField sparkField;
    public FieldEdge(Node src, Node tgt, FlowKind flowKind, SparkField sparkField) {
        super(src, tgt, flowKind);
        this.sparkField = sparkField;
    }

    public SparkField field() {
        return sparkField;
    }
}
