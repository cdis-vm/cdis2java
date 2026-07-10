package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightBitAndAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;
import io.github.cdisvm.runtime.exception.PyTypeError;

public interface PyBitAndAble {
    PyObject pyBitAnd(PyObject other);

    static PyObject bitAnd(PyObject left, PyObject right) {
        if (!(left instanceof PyBitAndAble) && !(right instanceof PyRightBitAndAble)) {
            throw new PyTypeError();
        }

        if (!(right instanceof PyRightBitAndAble)) {
            var castedLeft = (PyBitAndAble) left;
            var result = castedLeft.pyBitAnd(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else if (!(left instanceof PyBitAndAble)) {
            var castedRight = (PyRightBitAndAble) right;
            var result = castedRight.pyRightBitAnd(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightBitAndAble) right;
                var result = castedRight.pyRightBitAnd(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyBitAndAble) left;
                    result = castedLeft.pyBitAnd(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyBitAndAble) left;
                var result = castedLeft.pyBitAnd(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightBitAndAble) right;
                    result = castedRight.pyRightBitAnd(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                }
                return result;
            }
        }
    }
}
