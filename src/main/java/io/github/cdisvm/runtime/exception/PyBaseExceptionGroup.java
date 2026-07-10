package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyTuple;
import io.github.cdisvm.runtime.PyObject;

public class PyBaseExceptionGroup extends PyBaseException {
    public final PyBaseException exception;
    public final PyBaseExceptionGroup nested;

    public PyBaseExceptionGroup(PyTuple<PyObject> args, PyBaseException exception, PyBaseExceptionGroup nested) {
        super(args);
        this.exception = exception;
        this.nested = nested;
    }
}
