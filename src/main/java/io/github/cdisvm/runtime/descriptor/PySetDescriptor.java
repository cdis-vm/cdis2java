package io.github.cdisvm.runtime.descriptor;

import io.github.cdisvm.runtime.PyObject;

public non-sealed interface PySetDescriptor extends PyDataDescriptor {
    void pySet(PyObject instance, PyObject value);
}
