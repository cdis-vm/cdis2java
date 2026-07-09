package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightAddable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyAddable {
    PyObject pyAdd(PyObject other);

    static PyObject add(PyObject left, PyObject right) {
        // TODO: throw TypeError instead
        if (!(left instanceof PyAddable) && !(right instanceof PyRightAddable)) {
            throw new UnsupportedOperationException();
        }

        if (!(right instanceof PyRightAddable)) {
            var castedLeft = (PyAddable) left;
            var result = castedLeft.pyAdd(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else if (!(left instanceof PyAddable)) {
            var castedRight = (PyRightAddable) right;
            var result = castedRight.pyRightAdd(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new UnsupportedOperationException();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightAddable) right;
                var result = castedRight.pyRightAdd(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyAddable) left;
                    result = castedLeft.pyAdd(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyAddable) left;
                var result = castedLeft.pyAdd(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightAddable) right;
                    result = castedRight.pyRightAdd(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new UnsupportedOperationException();
                    }
                }
                return result;
            }
        }
    }
}
