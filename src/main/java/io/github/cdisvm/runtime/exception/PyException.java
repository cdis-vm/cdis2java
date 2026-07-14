package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyVarArgs;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.builtin.PyTuple;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

@PyBuiltin("Exception")
public class PyException extends PyBaseException {
    public static PyType type;

    @Override
    public PyType pyType() {
        return type;
    }

    public PyException() {
        super();
    }

    public PyException(String message) {
        super(message);
    }

    public PyException(PyStr message) {
        super(message);
    }

    public PyException(PyTuple<PyObject> args) {
        super(args);
    }

    @PyConstructor
    public static PyException create(@PyVarArgs PyTuple<PyObject> args) {
        return new PyException(args);
    }
}
