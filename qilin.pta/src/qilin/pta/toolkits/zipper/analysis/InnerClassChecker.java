package qilin.pta.toolkits.zipper.analysis;

import qilin.pta.toolkits.zipper.pta.PointsToAnalysis;
import soot.Type;

/**
 * Check whether a class is inner class of another class
 */
public class InnerClassChecker {
    private final PointsToAnalysis pta;

    public InnerClassChecker(final PointsToAnalysis pta) {
        this.pta = pta;
    }

    /**
     * @param pInner potential inner class
     * @param pOuter potential outer class
     * @return whether pInner is an inner class of pOuter
     */
    public boolean isInnerClass(final Type pInner, Type pOuter) {
        final String pInnerStr = pInner.toString();
        while (!pInnerStr.startsWith(pOuter.toString() + "$")) {
            pOuter = this.pta.directSuperTypeOf(pOuter);
            if (pOuter == null) {
                return false;
            }
        }
        return true;
    }
}
