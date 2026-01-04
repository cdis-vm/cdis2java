package io.github.cdisvm.compiler;

public enum MethodType {
    VIRTUAL("virtual"),
    STATIC("static"),
    CLASS("class");

    private final String id;

    MethodType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static MethodType fromId(String id) {
        for (MethodType type : MethodType.values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Method type not found: " + id);
    }
}
