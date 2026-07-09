package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.math.BigInteger;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.binary.PyBitAndAble;
import io.github.cdisvm.runtime.binary.PyBitOrAble;
import io.github.cdisvm.runtime.binary.PyBitXorAble;
import io.github.cdisvm.runtime.binary.PyDividable;
import io.github.cdisvm.runtime.binary.PyFloorDividable;
import io.github.cdisvm.runtime.binary.PyLShiftable;
import io.github.cdisvm.runtime.binary.PyMultipliable;
import io.github.cdisvm.runtime.binary.PyPowAble;
import io.github.cdisvm.runtime.binary.PyRShiftable;
import io.github.cdisvm.runtime.binary.PySubtractable;

@NullMarked
public record PyInt(long smallValue, @Nullable BigInteger bigValue) implements PyObject,
        PyIndexable, PyConstant, PyAddable, PySubtractable, PyMultipliable, PyDividable,
        PyFloorDividable, PyLShiftable, PyRShiftable, PyPowAble, PyBitOrAble,
        PyBitAndAble, PyBitXorAble {
    private static final int CACHE_START = -10;
    private static final int CACHE_END = 256;
    private static final PyInt[] CACHE = generateCache();

    private static PyInt[] generateCache() {
        var cache = new PyInt[CACHE_END - CACHE_START + 1];
        for (var i = CACHE_START; i <= CACHE_END; i++) {
            cache[i - CACHE_START] = new PyInt(i, null);
        }
        return cache;
    }

    public static PyInt of(long value) {
        if (value < CACHE_START || value > CACHE_END) {
            return new PyInt(value, null);
        }
        return CACHE[(int) value - CACHE_START];
    }

    public int intValue() {
        if (bigValue == null) {
            return Math.toIntExact(smallValue);
        } else {
            return bigValue.intValueExact();
        }
    }

    public BigInteger bigIntegerValue() {
        if (bigValue == null) {
            return BigInteger.valueOf(smallValue);
        } else {
            return bigValue;
        }
    }

    public String hexString() {
        if (bigValue == null) {
            return Long.toString(smallValue, 16);
        } else {
            return bigValue.toString(16);
        }
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        if (bigValue == null) {
            codeBuilder.loadConstant(smallValue);
            codeBuilder.invokestatic(CD.of(PyInt.class), "of",
                    MethodTypeDesc.of(CD.of(PyInt.class), CD.LONG));
        } else {
            codeBuilder.new_(CD.of(PyInt.class));
            codeBuilder.dup();
            codeBuilder.loadConstant(0L);
            codeBuilder.new_(CD.of(BigInteger.class));
            codeBuilder.dup();
            codeBuilder.loadConstant(bigValue.toString(16));
            codeBuilder.invokespecial(CD.of(BigInteger.class), "<init>",
                    MD.of(void.class, String.class, int.class));
            codeBuilder.invokespecial(CD.of(PyInt.class), "<init>",
                    MD.of(void.class, long.class, BigInteger.class));
        }
    }

    @Override
    public String getJavaIdentifierName() {
        if (bigValue == null) {
            return "PyInt_%s".formatted(Long.toString(smallValue, 16)).replace('-', '$');
        } else {
            return "PyInt_%s".formatted(bigValue.toString(16).replace('-', '$'));
        }
    }

    @Override
    public PyInt pyIndex() {
        return this;
    }

    @Override
    public PyAttributes pyAttributes() {
        return null;
    }

    @Override
    public PyType pyType() {
        return null;
    }

    @Override
    public PyBool pyTruth() {
        if (bigValue == null) {
            return PyBool.of(smallValue != 0L);
        } else {
            return PyBool.of(bigValue.compareTo(BigInteger.ZERO) != 0);
        }
    }

    @Override
    public PyObject pyAdd(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                try {
                    return PyInt.of(Math.addExact(smallValue, otherInt.smallValue));
                } catch (ArithmeticException e) {
                    // Done outside the if
                }
            }
            var leftBig = bigIntegerValue();
            var rightBig = bigIntegerValue();
            return new PyInt(0L, leftBig.add(rightBig));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyBitAnd(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyBitOr(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyBitXor(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyDivide(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyFloorDiv(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyLShift(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyMultiply(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyPow(PyObject other) {
        return null;
    }

    @Override
    public PyObject pyRShift(PyObject other) {
        return null;
    }

    @Override
    public PyObject pySubtract(PyObject other) {
        return null;
    }
}
