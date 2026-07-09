package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightRShiftable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyRShiftable {
    PyObject pyRShift(PyObject other);

    static PyObject rightShift(PyObject left, PyObject right) {
        if (!(left instanceof PyRShiftable) && !(right instanceof PyRightRShiftable)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightRShiftable)) {
            var castedLeft = (PyRShiftable) left;
            var result = castedLeft.pyRShift(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PyRShiftable)) {
            var castedRight = (PyRightRShiftable) right;
            var result = castedRight.pyRightRShift(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightRShiftable) right;
                var result = castedRight.pyRightRShift(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyRShiftable) left;
                    result = castedLeft.pyRShift(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyRShiftable) left;
                var result = castedLeft.pyRShift(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightRShiftable) right;
                    result = castedRight.pyRightRShift(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
