package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightPowAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;
import io.github.cdisvm.runtime.exception.PyTypeError;

public interface PyPowAble {
    PyObject pyPow(PyObject other);

    static PyObject power(PyObject left, PyObject right) {
        if (!(left instanceof PyPowAble) && !(right instanceof PyRightPowAble)) {
            throw new PyTypeError();
        }

        if (!(right instanceof PyRightPowAble)) {
            var castedLeft = (PyPowAble) left;
            var result = castedLeft.pyPow(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else if (!(left instanceof PyPowAble)) {
            var castedRight = (PyRightPowAble) right;
            var result = castedRight.pyRightPow(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightPowAble) right;
                var result = castedRight.pyRightPow(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyPowAble) left;
                    result = castedLeft.pyPow(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyPowAble) left;
                var result = castedLeft.pyPow(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightPowAble) right;
                    result = castedRight.pyRightPow(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                }
                return result;
            }
        }
    }
}
