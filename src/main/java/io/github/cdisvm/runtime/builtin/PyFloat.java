package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyHasPos;
import io.github.cdisvm.runtime.PyNegatable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.binary.PyBitAndAble;
import io.github.cdisvm.runtime.binary.PyBitOrAble;
import io.github.cdisvm.runtime.binary.PyBitXorAble;
import io.github.cdisvm.runtime.binary.PyDividable;
import io.github.cdisvm.runtime.binary.PyFloorDividable;
import io.github.cdisvm.runtime.binary.PyLShiftable;
import io.github.cdisvm.runtime.binary.PyModuloAble;
import io.github.cdisvm.runtime.binary.PyMultipliable;
import io.github.cdisvm.runtime.binary.PyPowAble;
import io.github.cdisvm.runtime.binary.PyRShiftable;
import io.github.cdisvm.runtime.binary.PySubtractable;
import io.github.cdisvm.runtime.comparison.PyHasEquals;
import io.github.cdisvm.runtime.comparison.PyHasGreaterThan;
import io.github.cdisvm.runtime.comparison.PyHasGreaterThanOrEqual;
import io.github.cdisvm.runtime.comparison.PyHasLessThan;
import io.github.cdisvm.runtime.comparison.PyHasLessThanOrEqual;
import io.github.cdisvm.runtime.comparison.PyHasNotEquals;
import io.github.cdisvm.runtime.exception.PyZeroDivisionError;

@NullMarked
@PyBuiltin("float")
public record PyFloat(double value) implements PyObject,
        PyConstant, PyAddable, PySubtractable, PyMultipliable, PyDividable,
        PyModuloAble, PyFloorDividable, PyPowAble,
        PyHasPos, PyNegatable,
        PyHasEquals, PyHasNotEquals, PyHasLessThan, PyHasLessThanOrEqual, PyHasGreaterThan,
        PyHasGreaterThanOrEqual, Comparable<PyFloat> {
    public static PyType type;

    @PyConstructor
    public static PyFloat create() {
        // TODO
        return PyFloat.of(0L);
    }

    public static PyFloat of(double value) {
        return new PyFloat(value);
    }

    public PyInt asInt() {
        return PyInt.of((long) Math.floor(value));
    }

    public String hexString() {
        return Double.toHexString(value);
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.loadConstant(value);
        codeBuilder.invokestatic(CD.of(PyFloat.class), "of",
                MethodTypeDesc.of(CD.of(PyFloat.class), CD.DOUBLE));
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyFloat_%s".formatted(hexString()).replace('-', '$');
    }

    @Override
    public PyAttributes pyAttributes() {
        return null;
    }

    @Override
    public PyType pyType() {
        return type;
    }

    @Override
    public PyBool pyTruth() {
        return PyBool.of(value != 0.0);
    }

    // TODO: implement int handling
    @Override
    public PyObject pyAdd(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyFloat.of(value + otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyDivide(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            if (otherValue == 0) {
                throw new PyZeroDivisionError("division by zero");
            }
            return new PyFloat(value / otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyFloorDiv(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            if (otherValue == 0) {
                throw new PyZeroDivisionError("division by zero");
            }
            return new PyFloat(Math.floor(value / otherValue));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyModulo(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            if (otherValue == 0) {
                throw new PyZeroDivisionError("division by zero");
            }
            return new PyFloat(Math.floor(value % otherValue));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyMultiply(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyFloat.of(value * otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyPow(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyFloat.of(Math.pow(value, otherValue));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pySubtract(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyFloat.of(value - otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyFloat pyPositive() {
        return this;
    }

    @Override
    public PyFloat pyNegate() {
        return new PyFloat(-value);
    }

    @Override
    public PyObject pyEquals(PyObject other) {
        return PyBool.of(equals(other));
    }

    @Override
    public PyObject pyGreaterThan(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyBool.of(value > otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyGreaterThanOrEqual(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyBool.of(value >= otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyLessThan(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyBool.of(value < otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyLessThanOrEqual(PyObject other) {
        if (other instanceof PyFloat(var otherValue)) {
            return PyBool.of(value <= otherValue);
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyNotEquals(PyObject other) {
        return PyBool.of(!equals(other));
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PyFloat(var otherValue)) {
            return value == otherValue;
        } else if (o instanceof PyInt(var smallValue, var bigValue)) {
            if (bigValue == null) {
                return value == smallValue;
            } else {
                return value == bigValue.doubleValue();
            }
        }
        return false;
    }

    @Override
    public int compareTo(PyFloat other) {
        return Double.compare(value, other.value);
    }
}
