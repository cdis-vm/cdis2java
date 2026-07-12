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
import io.github.cdisvm.runtime.PyHasPos;
import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyInvertible;
import io.github.cdisvm.runtime.PyNegatable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
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
@PyBuiltin("int")
public record PyInt(long smallValue, @Nullable BigInteger bigValue) implements PyObject,
        PyIndexable, PyConstant, PyAddable, PySubtractable, PyMultipliable, PyDividable,
        PyModuloAble, PyFloorDividable, PyLShiftable, PyRShiftable, PyPowAble, PyBitOrAble,
        PyBitAndAble, PyBitXorAble, PyHasPos, PyNegatable, PyInvertible,
        PyHasEquals, PyHasNotEquals, PyHasLessThan, PyHasLessThanOrEqual, PyHasGreaterThan,
        PyHasGreaterThanOrEqual {
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
            var rightBig = otherInt.bigIntegerValue();
            return new PyInt(0L, leftBig.add(rightBig));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyBitAnd(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                return PyInt.of(smallValue & otherInt.smallValue);
            }
            var leftBig = bigIntegerValue();
            var rightBig = otherInt.bigIntegerValue();
            return new PyInt(0L, leftBig.and(rightBig));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyBitOr(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                return PyInt.of(smallValue | otherInt.smallValue);
            }
            var leftBig = bigIntegerValue();
            var rightBig = otherInt.bigIntegerValue();
            return new PyInt(0L, leftBig.or(rightBig));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyBitXor(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                return PyInt.of(smallValue ^ otherInt.smallValue);
            }
            var leftBig = bigIntegerValue();
            var rightBig = otherInt.bigIntegerValue();
            return new PyInt(0L, leftBig.xor(rightBig));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyDivide(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                // return PyFloat.of((double) smallValue / otherInt.smallValue);
            } else {
                if (otherInt.bigIntegerValue().signum() == 0) {
                    throw new PyZeroDivisionError("division by zero");
                }
                var leftBig = bigIntegerValue();
                var rightBig = otherInt.bigIntegerValue();
                // TODO: Use float math
                return new PyInt(0L, leftBig.divide(rightBig));
            }
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyFloorDiv(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                return PyInt.of(smallValue / otherInt.smallValue);
            } else {
                if (otherInt.bigIntegerValue().signum() == 0) {
                    throw new PyZeroDivisionError("division by zero");
                }
                var leftBig = bigIntegerValue();
                var rightBig = otherInt.bigIntegerValue();
                return new PyInt(0L, leftBig.divide(rightBig));
            }
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyModulo(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                return PyInt.of(Math.floorMod(smallValue, otherInt.smallValue));
            } else {
                var leftBig = bigIntegerValue();
                var rightBig = otherInt.bigIntegerValue();
                var signum = rightBig.signum();
                if (signum == 0) {
                    throw new PyZeroDivisionError("division by zero");
                }
                // TODO: is the math collect for when RHS negative?
                return new PyInt(0L, signum == 1? leftBig.mod(rightBig) : leftBig.mod(rightBig.negate()).negate());
            }
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyLShift(PyObject other) {
        if (other instanceof PyInt otherInt) {
            var shift = otherInt.bigIntegerValue();
            if (shift.signum() < 0) {
                throw new ArithmeticException("negative shift count");
            }
            if (bigValue == null && otherInt.bigValue == null && shift.bitLength() < 63) {
                try {
                    long shiftAmount = shift.longValueExact();
                    var result = smallValue << shiftAmount;
                    if (shiftAmount > 0 && (smallValue << (shiftAmount - 1)) == (result >> 1)) {
                        return PyInt.of(result);
                    }
                } catch (ArithmeticException e) {
                    // Fall through to BigInteger
                }
            }
            var leftBig = bigIntegerValue();
            return new PyInt(0L, leftBig.shiftLeft(shift.intValueExact()));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyMultiply(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                try {
                    return PyInt.of(Math.multiplyExact(smallValue, otherInt.smallValue));
                } catch (ArithmeticException e) {
                    // Done outside the if
                }
            }
            var leftBig = bigIntegerValue();
            var rightBig = otherInt.bigIntegerValue();
            return new PyInt(0L, leftBig.multiply(rightBig));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyPow(PyObject other) {
        if (other instanceof PyInt otherInt) {
            var exp = otherInt.bigIntegerValue();
            if (exp.signum() < 0) {
                throw new ArithmeticException("negative exponent");
            }
            var leftBig = bigIntegerValue();
            return new PyInt(0L, leftBig.pow(exp.intValueExact()));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pyRShift(PyObject other) {
        if (other instanceof PyInt otherInt) {
            var shift = otherInt.bigIntegerValue();
            if (shift.signum() < 0) {
                throw new ArithmeticException("negative shift count");
            }
            if (bigValue == null && otherInt.bigValue == null && shift.bitLength() < 63) {
                try {
                    long shiftAmount = shift.longValueExact();
                    return PyInt.of(smallValue >> shiftAmount);
                } catch (ArithmeticException e) {
                    // Fall through to BigInteger
                }
            }
            var leftBig = bigIntegerValue();
            return new PyInt(0L, leftBig.shiftRight(shift.intValueExact()));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyObject pySubtract(PyObject other) {
        if (other instanceof PyInt otherInt) {
            if (bigValue == null && otherInt.bigValue == null) {
                try {
                    return PyInt.of(Math.subtractExact(smallValue, otherInt.smallValue));
                } catch (ArithmeticException e) {
                    // Done outside the if
                }
            }
            var leftBig = bigIntegerValue();
            var rightBig = otherInt.bigIntegerValue();
            return new PyInt(0L, leftBig.subtract(rightBig));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyInt pyPositive() {
        return this;
    }

    @Override
    public PyInt pyInvert() {
        return (PyInt) pyNegate().pySubtract(PyInt.of(1L));
    }

    @Override
    public PyInt pyNegate() {
        if (bigValue == null) {
            try {
                return PyInt.of(Math.multiplyExact(smallValue, -1L));
            } catch (ArithmeticException e) {
                return new PyInt(0L, BigInteger.valueOf(smallValue).negate());
            }
        }
        return new PyInt(0L, bigValue.negate());
    }

    @Override
    public PyObject pyEquals(PyObject other) {
        if (!(other instanceof PyInt otherInt)) {
            return PyNotImplemented.INSTANCE;
        }
        if (bigValue == null && otherInt.bigValue == null) {
            return PyBool.of(smallValue == otherInt.smallValue);
        }
        return PyBool.of(bigIntegerValue().equals(otherInt.bigIntegerValue()));
    }

    @Override
    public PyObject pyGreaterThan(PyObject other) {
        if (!(other instanceof PyInt otherInt)) {
            return PyNotImplemented.INSTANCE;
        }
        if (bigValue == null && otherInt.bigValue == null) {
            return PyBool.of(smallValue > otherInt.smallValue);
        }
        return PyBool.of(bigIntegerValue().compareTo(otherInt.bigIntegerValue()) > 0);
    }

    @Override
    public PyObject pyGreaterThanOrEqual(PyObject other) {
        if (!(other instanceof PyInt otherInt)) {
            return PyNotImplemented.INSTANCE;
        }
        if (bigValue == null && otherInt.bigValue == null) {
            return PyBool.of(smallValue >= otherInt.smallValue);
        }
        return PyBool.of(bigIntegerValue().compareTo(otherInt.bigIntegerValue()) >= 0);
    }

    @Override
    public PyObject pyLessThan(PyObject other) {
        if (!(other instanceof PyInt otherInt)) {
            return PyNotImplemented.INSTANCE;
        }
        if (bigValue == null && otherInt.bigValue == null) {
            return PyBool.of(smallValue < otherInt.smallValue);
        }
        return PyBool.of(bigIntegerValue().compareTo(otherInt.bigIntegerValue()) < 0);
    }

    @Override
    public PyObject pyLessThanOrEqual(PyObject other) {
        if (!(other instanceof PyInt otherInt)) {
            return PyNotImplemented.INSTANCE;
        }
        if (bigValue == null && otherInt.bigValue == null) {
            return PyBool.of(smallValue <= otherInt.smallValue);
        }
        return PyBool.of(bigIntegerValue().compareTo(otherInt.bigIntegerValue()) <= 0);
    }

    @Override
    public PyObject pyNotEquals(PyObject other) {
        if (!(other instanceof PyInt otherInt)) {
            return PyNotImplemented.INSTANCE;
        }
        if (bigValue == null && otherInt.bigValue == null) {
            return PyBool.of(smallValue != otherInt.smallValue);
        }
        return PyBool.of(!bigIntegerValue().equals(otherInt.bigIntegerValue()));
    }

    @Override
    public String toString() {
        if (bigValue == null) {
            return Long.toString(smallValue);
        }
        return bigValue.toString();
    }

    @Override
    public int hashCode() {
        if (bigValue == null) {
            return Long.hashCode(smallValue);
        }
        return bigValue.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PyInt other) {
            if (bigValue == null && other.bigValue == null) {
                return smallValue == other.smallValue;
            } else {
                return bigIntegerValue().equals(other.bigIntegerValue());
            }
        }
        return false;
    }
}
