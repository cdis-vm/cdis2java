package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceAddable {
    PyObject pyInplaceAdd(PyObject other);

    static PyObject inplaceAdd(PyObject left, PyObject right) {
        if (left instanceof PyInplaceAddable inplaceAddable) {
            var result = inplaceAddable.pyInplaceAdd(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyAddable.add(left, right);
    }
}
