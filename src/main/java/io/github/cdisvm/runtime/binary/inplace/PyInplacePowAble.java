package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyPowAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplacePowAble {
    PyObject pyInplacePower(PyObject other);

    static PyObject inplacePower(PyObject left, PyObject right) {
        if (left instanceof PyInplacePowAble inplacePowAble) {
            var result = inplacePowAble.pyInplacePower(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyPowAble.power(left, right);
    }
}
