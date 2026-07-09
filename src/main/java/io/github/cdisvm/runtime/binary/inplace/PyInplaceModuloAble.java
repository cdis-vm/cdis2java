package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyModuloAble;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceModuloAble {
    PyObject pyInplaceModulo(PyObject other);

    static PyObject inplaceModulo(PyObject left, PyObject right) {
        if (left instanceof PyInplaceModuloAble inplaceModuloAble) {
            var result = inplaceModuloAble.pyInplaceModulo(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyModuloAble.modulo(left, right);
    }
}
