package io.github.cdisvm.runtime;

public interface PyCallable {
    PyCallBuilder getCallBuilder();
    PyObject call(PyCallBuilder callBuilder);
}
