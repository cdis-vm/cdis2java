package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.builtin.PyTuple;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("ExceptionGroup")
public class PyExceptionGroup extends PyBaseExceptionGroup {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyExceptionGroup(PyTuple<PyObject> args, PyBaseException exception, PyBaseExceptionGroup nested) {
        super(args, exception, nested);
    }

    @PyConstructor
    public static PyExceptionGroup create(PyTuple<PyObject> args, PyBaseException exception, PyBaseExceptionGroup nested) {
        return new PyExceptionGroup(args, exception, nested);
    }
}
