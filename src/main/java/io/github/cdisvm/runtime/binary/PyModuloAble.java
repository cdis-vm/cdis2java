package io.github.cdisvm.runtime.binary;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.right.PyRightModuloAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;
import io.github.cdisvm.runtime.exception.PyTypeError;

public interface PyModuloAble {
    PyObject pyModulo(PyObject other);

    static PyObject modulo(PyObject left, PyObject right) {
        if (!(left instanceof PyModuloAble) && !(right instanceof PyRightModuloAble)) {
            throw new PyTypeError();
        }

        if (!(right instanceof PyRightModuloAble)) {
            var castedLeft = (PyModuloAble) left;
            var result = castedLeft.pyModulo(right);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else if (!(left instanceof PyModuloAble)) {
            var castedRight = (PyRightModuloAble) right;
            var result = castedRight.pyRightModulo(left);
            if (result == PyNotImplemented.INSTANCE) {
                throw new PyTypeError();
            }
            return result;
        } else {
            var leftClass = left.getClass();
            var rightClass = right.getClass();
            if (leftClass != rightClass && leftClass.isAssignableFrom(rightClass) && !rightClass.isAssignableFrom(leftClass)) {
                var castedRight = (PyRightModuloAble) right;
                var result = castedRight.pyRightModulo(left);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedLeft = (PyModuloAble) left;
                    result = castedLeft.pyModulo(right);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                    return result;
                }
                return result;
            } else {
                var castedLeft = (PyModuloAble) left;
                var result = castedLeft.pyModulo(right);
                if (result == PyNotImplemented.INSTANCE) {
                    var castedRight = (PyRightModuloAble) right;
                    result = castedRight.pyRightModulo(left);
                    if (result == PyNotImplemented.INSTANCE) {
                        throw new PyTypeError();
                    }
                }
                return result;
            }
        }
    }
}
