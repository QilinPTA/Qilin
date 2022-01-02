package qilin.pta.toolkits.zipper.pta;

import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;
import qilin.core.pag.VarNode;
import qilin.util.Pair;
import qilin.util.Triple;
import soot.SootMethod;
import soot.Type;

import java.util.Iterator;
import java.util.Set;

public interface PointsToAnalysis {
    // For points-to set.

    /**
     * @return all objects in the points-to analysis
     */
    Set<AllocNode> allObjects();

    /**
     * @param var
     * @return the objects pointed by variable var,
     * i.e., the points-to set of var
     */
    Set<AllocNode> pointsToSetOf(VarNode var);

    /**
     * @param var
     * @return the size of (i.e., number of objects in)
     * points-to set of var
     */
    int pointsToSetSizeOf(VarNode var);

    /**
     * @return the size of points-to set of the whole program
     */
    int totalPointsToSetSize();

    /**
     * @param method
     * @return the variables declared in method
     */
    Set<VarNode> variablesDeclaredIn(SootMethod method);

    // For pointer flow.
    Iterator<Pair<VarNode, VarNode>> localAssignIterator();

    // Inter-procedural assignment, including:
    // 1. parameter passing
    // 2. return value
    Iterator<Pair<VarNode, VarNode>> interProceduralAssignIterator();

    Iterator<Triple<VarNode, AllocNode, SparkField>> instanceLoadIterator();

    Iterator<Triple<AllocNode, SparkField, VarNode>> instanceStoreIterator();

    Iterator<Pair<VarNode, VarNode>> thisAssignIterator();

    // For object allocation relations.

    /**
     * @param method
     * @return the objects allocated in method
     */
    Set<AllocNode> objectsAllocatedIn(SootMethod method);

    /**
     * @param obj
     * @return the method containing the allocation site of obj.
     */
    SootMethod containingMethodOf(AllocNode obj);

    /**
     * @param var
     * @return the method where var is declared.
     */
    SootMethod declaringMethodOf(VarNode var);

    /**
     * @param obj
     * @return the variable which the obj is assigned to on creation.
     */
    VarNode assignedVarOf(AllocNode obj);


    // For method calls.

    /**
     * @param method
     * @return the callee methods of method
     */
    Set<SootMethod> calleesOf(SootMethod method);

    /**
     * @return all reachable methods in points-to analysis
     */
    Set<SootMethod> reachableMethods();

    /**
     * @param obj
     * @return the methods whose receiver object is obj
     */
    Set<SootMethod> methodsInvokedOn(AllocNode obj);

    /**
     * @param type
     * @return the methods whose receiver object is of type
     */
    Set<SootMethod> methodsInvokedOn(Type type);

    /**
     * @param recv
     * @return the variables which hold the return values from
     * the method call(s) on recv
     */
    Set<VarNode> returnToVariablesOf(VarNode recv);

    /**
     * @param method
     * @return the type that declares method
     */
    Type declaringTypeOf(SootMethod method);

    /**
     * @param type
     * @return the direct super type of type
     */
    Type directSuperTypeOf(Type type);

    /**
     * @param type
     * @return all objects of the given type
     */
    Set<AllocNode> objectsOfType(Type type);

    Set<VarNode> getParameters(SootMethod m);

    Set<VarNode> getRetVars(SootMethod m);

    VarNode getThis(SootMethod m);
}
