package io.github.cdisvm.runtime.binary.inplace;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.binary.PyFloorDividable;
import io.github.cdisvm.runtime.builtin.PyNotImplemented;

public interface PyInplaceFloorDividable {
    PyObject pyInplaceFloorDivide(PyObject other);

    static PyObject inplaceFloorDivide(PyObject left, PyObject right) {
        if (left instanceof PyInplaceFloorDividable inplaceFloorDividable) {
            var result = inplaceFloorDividable.pyInplaceFloorDivide(right);
            if (result != PyNotImplemented.INSTANCE) {
                return result;
            }
        }
        return PyFloorDividable.floorDivide(left, right);
    }
}
