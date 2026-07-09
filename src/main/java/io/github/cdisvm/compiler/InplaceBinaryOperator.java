package io.github.cdisvm.compiler;

import io.github.cdisvm.runtime.binary.inplace.PyInplaceAddable;

public enum InplaceBinaryOperator {
    Add("Add", PyInplaceAddable.class, "pyInplaceAdd", "inplaceAdd"),
    Sub("Sub", null, "pyInplaceSubtract", "inplaceSubtract"),
    Mult("Mult", null, "pyInplaceMultiply", "inplaceMultiply"),
    Div("Div", null, "pyInplaceDivide", "inplaceDivide"),
    FloorDiv("FloorDiv", null, "pyInplaceFloorDivide", "inplaceFloorDivide"),
    Mod("Mod", null, "pyInplaceModulo", "inplaceModulo"),
    Pow("Pow", null, "pyInplacePower", "inplacePower"),
    LShift("LShift", null, "pyInplaceLeftShift", "inplaceLeftShift"),
    RShift("RShift", null, "pyInplaceRightShift", "inplaceRightShift"),
    BitOr("BitOr", null, "pyInplaceBitOr", "inplaceBitOr"),
    BitXor("BitXor", null, "pyInplaceBitXor", "inplaceBitXor"),
    BitAnd("BitAnd", null, "pyInplaceBitAnd", "inplaceBitAnd"),
    MatMult("MatMult", null, "pyInplaceMatrixMultiply", "inplaceMatrixMultiply"),;

    private final String id;
    private final Class<?> inplaceInterface;
    private final String interfaceMethod;
    private final String staticMethod;

    InplaceBinaryOperator(String id, Class<?> inplaceInterface, String interfaceMethod, String staticMethod) {
        this.id = id;
        this.inplaceInterface = inplaceInterface;
        this.interfaceMethod = interfaceMethod;
        this.staticMethod = staticMethod;
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
