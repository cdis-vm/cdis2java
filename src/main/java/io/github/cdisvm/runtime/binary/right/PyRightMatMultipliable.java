package io.github.cdisvm.runtime.binary.right;

import io.github.cdisvm.runtime.PyObject;

public interface PyRightMatMultipliable {
    PyObject pyRightMatMult(PyObject other);
}
