package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyTuple;
import io.github.cdisvm.runtime.PyObject;

public class PyExceptionGroup extends PyBaseExceptionGroup {
    public PyExceptionGroup(PyTuple<PyObject> args, PyBaseException exception, PyBaseExceptionGroup nested) {
        super(args, exception, nested);
    }
}
