package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyValueError extends PyException {
    public PyValueError() {
        super();
    }

    public PyValueError(String message) {
        super(message);
    }

    public PyValueError(PyStr message) {
        super(message);
    }
}
