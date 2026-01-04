package io.github.cdisvm.compiler;

public enum UnaryOperator {
    INVERT("Invert"),
    POSITIVE("UAdd"),
    NEGATE("USub");

    private final String id;

    UnaryOperator(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static UnaryOperator fromId(String id) {
        for (UnaryOperator op : UnaryOperator.values()) {
            if (op.getId().equals(id)) {
                return op;
            }
        }
        throw new IllegalArgumentException("No enum constant with id '" + id + "'");
    }
}
