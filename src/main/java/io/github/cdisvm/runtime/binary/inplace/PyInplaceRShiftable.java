package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyRShiftable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceRShiftable {
    PyObject pyInplaceRightShift(PyObject other);

    static PyObject inplaceRightShift(PyObject left, PyObject right) {
        if (left instanceof PyInplaceRShiftable inplaceRShiftable) {
            var result = inplaceRShiftable.pyInplaceRightShift(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyRShiftable.rightShift(left, right);
    }
}
