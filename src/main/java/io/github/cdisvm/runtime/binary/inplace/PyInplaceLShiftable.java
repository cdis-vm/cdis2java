package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyLShiftable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceLShiftable {
    PyObject pyInplaceLeftShift(PyObject other);

    static PyObject inplaceLeftShift(PyObject left, PyObject right) {
        if (left instanceof PyInplaceLShiftable inplaceLShiftable) {
            var result = inplaceLShiftable.pyInplaceLeftShift(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyLShiftable.leftShift(left, right);
    }
}
