package io.github.cdisvm.runtime;

public interface PyCallable extends PyObject {
    PyCallBuilder pyCallBuilder();
    PyObject pyCall(PyCallBuilder callBuilder);
}
