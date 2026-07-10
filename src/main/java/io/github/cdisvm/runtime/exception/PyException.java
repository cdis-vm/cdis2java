package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.builtin.PyTuple;
import io.github.cdisvm.runtime.PyObject;

public class PyException extends PyBaseException {
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
}
