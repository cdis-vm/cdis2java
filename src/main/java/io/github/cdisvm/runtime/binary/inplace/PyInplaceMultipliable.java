package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyMultipliable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceMultipliable {
    PyObject pyInplaceMultiply(PyObject other);

    static PyObject inplaceMultiply(PyObject left, PyObject right) {
        if (left instanceof PyInplaceMultipliable inplaceMultipliable) {
            var result = inplaceMultipliable.pyInplaceMultiply(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyMultipliable.multiply(left, right);
    }
}
