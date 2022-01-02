package qilin.pta.toolkits.mahjong.pta;

import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;
import qilin.util.Triple;
import soot.SootMethod;
import soot.Type;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Tian Tan
 * @author Yue Li
 */
public interface PTAProvider {

    /**
     * @return an iterator for each object in the points-to set
     */
    Iterator<AllocNode> objIterator();

    /**
     * @return an iterator for every field points-to relation,
     * including instance field and array objects
     */
    Iterator<Triple<AllocNode, SparkField, AllocNode>> fptIterator();

    Set<SootMethod> invokedMethodsOn(AllocNode heap);

    Set<AllocNode> objectsOfType(final Type type);
}
