package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyRuntimeError extends PyException {
    public PyRuntimeError() {
        super();
    }

    public PyRuntimeError(String message) {
        super(message);
    }

    public PyRuntimeError(PyStr message) {
        super(message);
    }
}
