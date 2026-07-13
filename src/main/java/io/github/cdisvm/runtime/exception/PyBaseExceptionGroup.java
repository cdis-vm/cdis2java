package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.builtin.PyTuple;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("BaseExceptionGroup")
public class PyBaseExceptionGroup extends PyBaseException {
    public static PyType type;

    public final PyBaseException exception;
    public final PyBaseExceptionGroup nested;

    public PyBaseExceptionGroup(PyTuple<PyObject> args, PyBaseException exception, PyBaseExceptionGroup nested) {
        super(args);
        this.exception = exception;
        this.nested = nested;
    }

    @PyConstructor
    public static PyBaseExceptionGroup create(PyTuple<PyObject> args, PyBaseException exception, PyBaseExceptionGroup nested) {
        return new PyBaseExceptionGroup(args, exception, nested);
    }
}
