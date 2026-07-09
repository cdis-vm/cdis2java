package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightBitXorAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyBitXorAble {
    PyObject pyBitXor(PyObject other);

    static PyObject bitXor(PyObject left, PyObject right) {
        if (!(left instanceof PyBitXorAble) && !(right instanceof PyRightBitXorAble)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightBitXorAble)) {
            var castedLeft = (PyBitXorAble) left;
            var result = castedLeft.pyBitXor(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PyBitXorAble)) {
            var castedRight = (PyRightBitXorAble) right;
            var result = castedRight.pyRightBitXor(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightBitXorAble) right;
                var result = castedRight.pyRightBitXor(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyBitXorAble) left;
                    result = castedLeft.pyBitXor(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyBitXorAble) left;
                var result = castedLeft.pyBitXor(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightBitXorAble) right;
                    result = castedRight.pyRightBitXor(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
