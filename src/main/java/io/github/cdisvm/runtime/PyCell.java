package io.github.cdisvm.runtime;

public interface PyCell extends PyObject {
    PyObject get();
    PyObject set(PyObject value);
}
