package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyBitOrAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceBitOrAble {
    PyObject pyInplaceBitOr(PyObject other);

    static PyObject inplaceBitOr(PyObject left, PyObject right) {
        if (left instanceof PyInplaceBitOrAble inplaceBitOrAble) {
            var result = inplaceBitOrAble.pyInplaceBitOr(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyBitOrAble.bitOr(left, right);
    }
}
