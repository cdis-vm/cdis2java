package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyDividable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceDividable {
    PyObject pyInplaceDivide(PyObject other);

    static PyObject inplaceDivide(PyObject left, PyObject right) {
        if (left instanceof PyInplaceDividable inplaceDividable) {
            var result = inplaceDividable.pyInplaceDivide(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyDividable.divide(left, right);
    }
}
