package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightDividable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyDividable {
    PyObject pyDivide(PyObject other);

    static PyObject divide(PyObject left, PyObject right) {
        if (!(left instanceof PyDividable) && !(right instanceof PyRightDividable)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightDividable)) {
            var castedLeft = (PyDividable) left;
            var result = castedLeft.pyDivide(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PyDividable)) {
            var castedRight = (PyRightDividable) right;
            var result = castedRight.pyRightDivide(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightDividable) right;
                var result = castedRight.pyRightDivide(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyDividable) left;
                    result = castedLeft.pyDivide(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyDividable) left;
                var result = castedLeft.pyDivide(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightDividable) right;
                    result = castedRight.pyRightDivide(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
