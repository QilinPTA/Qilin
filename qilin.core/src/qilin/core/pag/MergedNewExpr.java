package qilin.core.pag;

import soot.RefType;
import soot.jimple.internal.JNewExpr;

import java.util.HashMap;
import java.util.Map;

public class MergedNewExpr extends JNewExpr {
    private static final Map<RefType, MergedNewExpr> map = new HashMap<>();

    private MergedNewExpr(RefType type) {
        super(type);
    }

    public static MergedNewExpr v(RefType type) {
        return map.computeIfAbsent(type, k -> new MergedNewExpr(type));
    }

}
