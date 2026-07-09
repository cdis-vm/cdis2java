package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyBitXorAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceBitXorAble {
    PyObject pyInplaceBitXor(PyObject other);

    static PyObject inplaceBitXor(PyObject left, PyObject right) {
        if (left instanceof PyInplaceBitXorAble inplaceBitXorAble) {
            var result = inplaceBitXorAble.pyInplaceBitXor(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyBitXorAble.bitXor(left, right);
    }
}
