package io.github.cdisvm.compiler;

public enum BinaryOperator {
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
    MatMult("MatMult"),
    IAdd("IAdd"),
    ISub("ISub"),
    IMult("IMult"),
    IDiv("IDiv"),
    IFloorDiv("IFloorDiv"),
    IMod("IMod"),
    IPow("IPow"),
    ILShift("ILShift"),
    IRShift("IRShift"),
    IBitOr("IBitOr"),
    IBitXor("IBitXor"),
    IBitAnd("IBitAnd"),
    IMatMult("IMatMult"),
    Eq("Eq"),
    NotEq("NotEq"),
    Lt("Lt"),
    LtE("LtE"),
    Gt("Gt"),
    GtE("GtE");

    private final String id;

    BinaryOperator(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static BinaryOperator fromId(String id) {
        for (BinaryOperator op : BinaryOperator.values()) {
            if (op.getId().equals(id)) {
                return op;
            }
        }
        throw new IllegalArgumentException("No enum constant with id '" + id + "'");
    }
}
