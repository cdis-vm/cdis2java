package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.binary.right.PyRightAddable;

public enum BinaryOperator {
    Add("Add", PyAddable.class, "pyAdd", PyRightAddable.class, "pyRightAdd", "add"),
    Sub("Sub", null, "pySubtract", null, "pyRightSubtract", "subtract"),
    Mult("Mult", null, "pyMultiply", null, "pyRightMultiply", "multiply"),
    Div("Div", null, "pyDivide", null, "pyRightDivide", "divide"),
    FloorDiv("FloorDiv", null, "pyFloorDiv", null, "pyRightFloorDiv", "floorDivide"),
    Mod("Mod", null, "pyModulo", null, "pyRightModulo", "modulo"),
    Pow("Pow", null, "pyPow", null, "pyRightPow", "power"),
    LShift("LShift", null, "pyLShift", null, "pyRightLShift", "leftShift"),
    RShift("RShift", null, "pyRShift", null, "pyRightRShift", "rightShift"),
    BitOr("BitOr", null, "pyBitOr", null, "pyRightBitOr", "bitOr"),
    BitXor("BitXor", null, "pyBitXor", null, "pyRightBitXor", "bitXor"),
    BitAnd("BitAnd", null, "pyBitAnd", null, "pyRightBitAnd", "bitAnd"),
    MatMult("MatMult",  null, "pyMatMult", null, "pyRightMatMult", "matrixMultiply"),
    Eq("Eq", null, "pyEq", null, "pyEq", "equal"),
    NotEq("NotEq", null, "pyNotEq", null, "pyNotEq", "notEqual"),
    Lt("Lt", null, "pyLt", null, "pyGt", "lessThan"),
    LtE("LtE", null, "pyLtE", null, "pyGtE", "lessThanOrEqual"),
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
