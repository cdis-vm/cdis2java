package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PySubtractable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceSubtractable {
    PyObject pyInplaceSubtract(PyObject other);

    static PyObject inplaceSubtract(PyObject left, PyObject right) {
        if (left instanceof PyInplaceSubtractable inplaceSubtractable) {
            var result = inplaceSubtractable.pyInplaceSubtract(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PySubtractable.subtract(left, right);
    }
}
