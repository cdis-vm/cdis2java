package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyBitAndAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceBitAndAble {
    PyObject pyInplaceBitAnd(PyObject other);

    static PyObject inplaceBitAnd(PyObject left, PyObject right) {
        if (left instanceof PyInplaceBitAndAble inplaceBitAndAble) {
            var result = inplaceBitAndAble.pyInplaceBitAnd(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyBitAndAble.bitAnd(left, right);
    }
}
