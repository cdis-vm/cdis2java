package io.github.cdisvm.compiler;

public enum FunctionType {
    FUNCTION("function"),
    LAMBDA("lambda"),
    GENERATOR("generator"),
    CLASS_BODY("class_body"),
    COROUTINE_GENERATOR("coroutine_generator"),
    ASYNC_FUNCTION("async_function"),
    ASYNC_GENERATOR("async_generator");

    private final String id;

    FunctionType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static FunctionType fromId(String id) {
        for (FunctionType type : FunctionType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Function type not found: " + id);
    }
}
