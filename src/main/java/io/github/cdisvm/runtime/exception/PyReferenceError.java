package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyReferenceError extends PyException {
    public PyReferenceError() {
        super();
    }

    public PyReferenceError(String message) {
        super(message);
    }

    public PyReferenceError(PyStr message) {
        super(message);
    }
}
