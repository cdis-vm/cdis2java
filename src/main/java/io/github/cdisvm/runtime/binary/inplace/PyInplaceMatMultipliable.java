package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyMatMultipliable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceMatMultipliable {
    PyObject pyInplaceMatrixMultiply(PyObject other);

    static PyObject inplaceMatrixMultiply(PyObject left, PyObject right) {
        if (left instanceof PyInplaceMatMultipliable inplaceMatMultipliable) {
            var result = inplaceMatMultipliable.pyInplaceMatrixMultiply(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyMatMultipliable.matrixMultiply(left, right);
    }
}
