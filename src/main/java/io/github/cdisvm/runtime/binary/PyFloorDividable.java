package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightFloorDividable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyFloorDividable {
    PyObject pyFloorDiv(PyObject other);

    static PyObject floorDivide(PyObject left, PyObject right) {
        if (!(left instanceof PyFloorDividable) && !(right instanceof PyRightFloorDividable)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightFloorDividable)) {
            var castedLeft = (PyFloorDividable) left;
            var result = castedLeft.pyFloorDiv(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PyFloorDividable)) {
            var castedRight = (PyRightFloorDividable) right;
            var result = castedRight.pyRightFloorDiv(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightFloorDividable) right;
                var result = castedRight.pyRightFloorDiv(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyFloorDividable) left;
                    result = castedLeft.pyFloorDiv(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyFloorDividable) left;
                var result = castedLeft.pyFloorDiv(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightFloorDividable) right;
                    result = castedRight.pyRightFloorDiv(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
