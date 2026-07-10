package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyTypeError extends PyException {
    public PyTypeError() {
        super();
    }

    public PyTypeError(String message) {
        super(message);
    }

    public PyTypeError(PyStr message) {
        super(message);
    }
}
