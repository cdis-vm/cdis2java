package io.github.cdisvm.compiler;

public enum InplaceBinaryOperator {
    Add("Add"),
    Sub("Sub"),
    Mult("Mult"),
    Div("Div"),
    FloorDiv("FloorDiv"),
    Mod("Mod"),
    Pow("Pow"),
    LShift("LShift"),
    RShift("RShift"),
    BitOr("BitOr"),
    BitXor("BitXor"),
    BitAnd("BitAnd"),
    MatMult("MatMult");

    private final String id;

    InplaceBinaryOperator(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static InplaceBinaryOperator fromId(String id) {
        for (InplaceBinaryOperator op : InplaceBinaryOperator.values()) {
            if (op.getId().equals(id)) {
                return op;
            }
        }
        throw new IllegalArgumentException("No enum constant with id '" + id + "'");
    }
}
