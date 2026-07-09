package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightSubtractable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PySubtractable {
    PyObject pySubtract(PyObject other);

    static PyObject subtract(PyObject left, PyObject right) {
        if (!(left instanceof PySubtractable) && !(right instanceof PyRightSubtractable)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightSubtractable)) {
            var castedLeft = (PySubtractable) left;
            var result = castedLeft.pySubtract(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PySubtractable)) {
            var castedRight = (PyRightSubtractable) right;
            var result = castedRight.pyRightSubtract(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightSubtractable) right;
                var result = castedRight.pyRightSubtract(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PySubtractable) left;
                    result = castedLeft.pySubtract(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PySubtractable) left;
                var result = castedLeft.pySubtract(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightSubtractable) right;
                    result = castedRight.pyRightSubtract(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
