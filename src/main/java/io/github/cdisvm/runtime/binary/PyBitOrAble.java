package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightBitOrAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;
import io.github.cdisvm.runtime.exception.PyTypeError;

public interface PyBitOrAble {
    PyObject pyBitOr(PyObject other);

    static PyObject bitOr(PyObject left, PyObject right) {
        if (!(left instanceof PyBitOrAble) && !(right instanceof PyRightBitOrAble)) {
            throw new PyTypeError();
        }

        if (!(right instanceof PyRightBitOrAble)) {
            var castedLeft = (PyBitOrAble) left;
            var result = castedLeft.pyBitOr(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else if (!(left instanceof PyBitOrAble)) {
            var castedRight = (PyRightBitOrAble) right;
            var result = castedRight.pyRightBitOr(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightBitOrAble) right;
                var result = castedRight.pyRightBitOr(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyBitOrAble) left;
                    result = castedLeft.pyBitOr(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyBitOrAble) left;
                var result = castedLeft.pyBitOr(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightBitOrAble) right;
                    result = castedRight.pyRightBitOr(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                }
                return result;
            }
        }
    }
}
