package qilin.pta.toolkits.moon.Graph;

public enum FlowKind {
    NEW,
    LOCAL_ASSIGN,
    INSTANCE_LOAD,
    INSTANCE_STORE,
    STATIC_LOAD,
    STATIC_STORE,
    THIS_PASSING,

    PARAMETER_PASSING,
    RETURN,
    FIELD_STORE,
    FIELD_LOAD,
    CALL_STORE,
    CALL_LOAD,
    EXCEPTION_FLOW,
    IGNORE,

    THIS_PARAM_PASSING, // this is for the case that the method is called by another method in the same class

    THIS_CONNECT, // for inlinePtrStreamGraph

    // only for StoredVarTraverser.java
    STATIC_METHOD_RETURN,
    STATIC_PARAMETER_PASSING,
    THIS_METHOD_RETURN,
    THIS_FIELD_STORE_AND_LOAD
}
