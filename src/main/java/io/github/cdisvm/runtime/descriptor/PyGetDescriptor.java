package io.github.cdisvm.runtime.descriptor;

import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

public non-sealed interface PyGetDescriptor extends PyDescriptor {
    PyObject pyGet(PyObject instance, PyType type);
}
