package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightLShiftable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyLShiftable {
    PyObject pyLShift(PyObject other);

    static PyObject leftShift(PyObject left, PyObject right) {
        if (!(left instanceof PyLShiftable) && !(right instanceof PyRightLShiftable)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightLShiftable)) {
            var castedLeft = (PyLShiftable) left;
            var result = castedLeft.pyLShift(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PyLShiftable)) {
            var castedRight = (PyRightLShiftable) right;
            var result = castedRight.pyRightLShift(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightLShiftable) right;
                var result = castedRight.pyRightLShift(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyLShiftable) left;
                    result = castedLeft.pyLShift(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyLShiftable) left;
                var result = castedLeft.pyLShift(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightLShiftable) right;
                    result = castedRight.pyRightLShift(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
