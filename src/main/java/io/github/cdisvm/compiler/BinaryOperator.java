package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.binary.PyBitAndAble;
import io.github.cdisvm.runtime.binary.PyBitOrAble;
import io.github.cdisvm.runtime.binary.PyBitXorAble;
import io.github.cdisvm.runtime.binary.PyDividable;
import io.github.cdisvm.runtime.binary.PyFloorDividable;
import io.github.cdisvm.runtime.binary.PyLShiftable;
import io.github.cdisvm.runtime.binary.PyMatMultipliable;
import io.github.cdisvm.runtime.binary.PyModuloAble;
import io.github.cdisvm.runtime.binary.PyMultipliable;
import io.github.cdisvm.runtime.binary.PyPowAble;
import io.github.cdisvm.runtime.binary.PyRShiftable;
import io.github.cdisvm.runtime.binary.PySubtractable;
import io.github.cdisvm.runtime.binary.right.PyRightAddable;
import io.github.cdisvm.runtime.binary.right.PyRightBitAndAble;
import io.github.cdisvm.runtime.binary.right.PyRightBitOrAble;
import io.github.cdisvm.runtime.binary.right.PyRightBitXorAble;
import io.github.cdisvm.runtime.binary.right.PyRightDividable;
import io.github.cdisvm.runtime.binary.right.PyRightFloorDividable;
import io.github.cdisvm.runtime.binary.right.PyRightLShiftable;
import io.github.cdisvm.runtime.binary.right.PyRightMatMultipliable;
import io.github.cdisvm.runtime.binary.right.PyRightModuloAble;
import io.github.cdisvm.runtime.binary.right.PyRightMultipliable;
import io.github.cdisvm.runtime.binary.right.PyRightPowAble;
import io.github.cdisvm.runtime.binary.right.PyRightRShiftable;
import io.github.cdisvm.runtime.binary.right.PyRightSubtractable;

public enum BinaryOperator {
    Add("Add", PyAddable.class, "pyAdd", PyRightAddable.class, "pyRightAdd", "add"),
    Sub("Sub", PySubtractable.class, "pySubtract", PyRightSubtractable.class, "pyRightSubtract", "subtract"),
    Mult("Mult", PyMultipliable.class, "pyMultiply", PyRightMultipliable.class, "pyRightMultiply", "multiply"),
    Div("Div", PyDividable.class, "pyDivide", PyRightDividable.class, "pyRightDivide", "divide"),
    FloorDiv("FloorDiv", PyFloorDividable.class, "pyFloorDiv", PyRightFloorDividable.class, "pyRightFloorDiv", "floorDivide"),
    Mod("Mod", PyModuloAble.class, "pyModulo", PyRightModuloAble.class, "pyRightModulo", "modulo"),
    Pow("Pow", PyPowAble.class, "pyPow", PyRightPowAble.class, "pyRightPow", "power"),
    LShift("LShift", PyLShiftable.class, "pyLShift", PyRightLShiftable.class, "pyRightLShift", "leftShift"),
    RShift("RShift", PyRShiftable.class, "pyRShift", PyRightRShiftable.class, "pyRightRShift", "rightShift"),
    BitOr("BitOr", PyBitOrAble.class, "pyBitOr", PyRightBitOrAble.class, "pyRightBitOr", "bitOr"),
    BitXor("BitXor", PyBitXorAble.class, "pyBitXor", PyRightBitXorAble.class, "pyRightBitXor", "bitXor"),
    BitAnd("BitAnd", PyBitAndAble.class, "pyBitAnd", PyRightBitAndAble.class, "pyRightBitAnd", "bitAnd"),
    MatMult("MatMult",  PyMatMultipliable.class, "pyMatMult", PyRightMatMultipliable.class, "pyRightMatMult", "matrixMultiply"),
    Eq("Eq", null, "pyEq", null, "pyEq", "equal"),
    NotEq("NotEq", null, "pyNotEq", null, "pyNotEq", "notEqual"),
    Lt("Lt", null, "pyLt", null, "pyGt", "lessThan"),
    LtE("LtE",null, "pyLtE", null, "pyGtE", "lessThanOrEqual"),
    Gt("Gt", null, "pyGt", null, "pyLt", "greaterThan"),
    GtE("GtE", null, "pyGtE", null, "pyLtE", "greaterThanOrEqual");

    private final String id;
    private final Class<?> leftInterface;
    private final String leftMethod;
    private final Class<?> rightInterface;
    private final String rightMethod;
    private final String staticMethod;

    BinaryOperator(String id,
            Class<?> leftInterface,
            String leftMethod,
            Class<?> rightInterface,
            String rightMethod,
            String staticMethod) {
        this.id = id;
        this.leftInterface = leftInterface;
        this.leftMethod = leftMethod;
        this.rightInterface = rightInterface;
        this.rightMethod = rightMethod;
        this.staticMethod = staticMethod;
    }

    public String getId() {
        return id;
    }

    public void implement(CodeBuilder codeBuilder) {
        codeBuilder.invokestatic(CD.of(leftInterface), staticMethod, MD.of(PyObject.class, PyObject.class, PyObject.class));
    }

    public Class<?> getLeftInterface() {
        return leftInterface;
    }

    public String getLeftMethod() {
        return leftMethod;
    }

    public Class<?> getRightInterface() {
        return rightInterface;
    }

    public String getRightMethod() {
        return rightMethod;
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
