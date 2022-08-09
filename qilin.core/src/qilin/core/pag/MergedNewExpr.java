package qilin.core.pag;

import qilin.util.DataFactory;
import soot.RefLikeType;

import java.util.Map;

public class MergedNewExpr {
    private final RefLikeType type;
    private static final Map<RefLikeType, MergedNewExpr> map = DataFactory.createMap();

    private MergedNewExpr(RefLikeType type) {
        this.type = type;
    }

    public static MergedNewExpr v(RefLikeType type) {
        return map.computeIfAbsent(type, k -> new MergedNewExpr(type));
    }

}
