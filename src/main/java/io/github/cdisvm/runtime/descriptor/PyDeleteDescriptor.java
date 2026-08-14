package io.github.cdisvm.runtime.descriptor;

import io.github.cdisvm.runtime.PyObject;

public non-sealed interface PyDeleteDescriptor extends PyDataDescriptor {
    void pyDelete(PyObject instance);
}
