package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceAddable;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceBitAndAble;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceBitOrAble;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceBitXorAble;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceDividable;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceFloorDividable;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceLShiftable;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceMatMultipliable;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceModuloAble;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceMultipliable;
import io.github.cdisvm.runtime.binary.inplace.PyInplacePowAble;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceRShiftable;
import io.github.cdisvm.runtime.binary.inplace.PyInplaceSubtractable;

public enum InplaceBinaryOperator {
    Add("Add", PyInplaceAddable.class, "pyInplaceAdd", "inplaceAdd"),
    Sub("Sub", PyInplaceSubtractable.class, "pyInplaceSubtract", "inplaceSubtract"),
    Mult("Mult", PyInplaceMultipliable.class, "pyInplaceMultiply", "inplaceMultiply"),
    Div("Div", PyInplaceDividable.class, "pyInplaceDivide", "inplaceDivide"),
    FloorDiv("FloorDiv", PyInplaceFloorDividable.class, "pyInplaceFloorDivide", "inplaceFloorDivide"),
    Mod("Mod", PyInplaceModuloAble.class, "pyInplaceModulo", "inplaceModulo"),
    Pow("Pow", PyInplacePowAble.class, "pyInplacePower", "inplacePower"),
    LShift("LShift", PyInplaceLShiftable.class, "pyInplaceLeftShift", "inplaceLeftShift"),
    RShift("RShift", PyInplaceRShiftable.class, "pyInplaceRightShift", "inplaceRightShift"),
    BitOr("BitOr", PyInplaceBitOrAble.class, "pyInplaceBitOr", "inplaceBitOr"),
    BitXor("BitXor", PyInplaceBitXorAble.class, "pyInplaceBitXor", "inplaceBitXor"),
    BitAnd("BitAnd", PyInplaceBitAndAble.class, "pyInplaceBitAnd", "inplaceBitAnd"),
    MatMult("MatMult", PyInplaceMatMultipliable.class, "pyInplaceMatrixMultiply", "inplaceMatrixMultiply"),;

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

    public void implement(CodeBuilder codeBuilder) {
        codeBuilder.invokestatic(CD.of(inplaceInterface), staticMethod,
                MD.of(PyObject.class, PyObject.class, PyObject.class), true);
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
