package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightMultipliable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;
import io.github.cdisvm.runtime.exception.PyTypeError;

public interface PyMultipliable {
    PyObject pyMultiply(PyObject other);

    static PyObject multiply(PyObject left, PyObject right) {
        if (!(left instanceof PyMultipliable) && !(right instanceof PyRightMultipliable)) {
            throw new PyTypeError();
        }

        if (!(right instanceof PyRightMultipliable)) {
            var castedLeft = (PyMultipliable) left;
            var result = castedLeft.pyMultiply(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else if (!(left instanceof PyMultipliable)) {
            var castedRight = (PyRightMultipliable) right;
            var result = castedRight.pyRightMultiply(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightMultipliable) right;
                var result = castedRight.pyRightMultiply(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyMultipliable) left;
                    result = castedLeft.pyMultiply(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyMultipliable) left;
                var result = castedLeft.pyMultiply(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightMultipliable) right;
                    result = castedRight.pyRightMultiply(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                }
                return result;
            }
        }
    }
}
