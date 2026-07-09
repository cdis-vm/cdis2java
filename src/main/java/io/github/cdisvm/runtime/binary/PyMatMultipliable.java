package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightMatMultipliable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyMatMultipliable {
    PyObject pyMatMult(PyObject other);

    static PyObject matrixMultiply(PyObject left, PyObject right) {
        if (!(left instanceof PyMatMultipliable) && !(right instanceof PyRightMatMultipliable)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightMatMultipliable)) {
            var castedLeft = (PyMatMultipliable) left;
            var result = castedLeft.pyMatMult(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PyMatMultipliable)) {
            var castedRight = (PyRightMatMultipliable) right;
            var result = castedRight.pyRightMatMult(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightMatMultipliable) right;
                var result = castedRight.pyRightMatMult(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyMatMultipliable) left;
                    result = castedLeft.pyMatMult(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyMatMultipliable) left;
                var result = castedLeft.pyMatMult(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightMatMultipliable) right;
                    result = castedRight.pyRightMatMult(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
