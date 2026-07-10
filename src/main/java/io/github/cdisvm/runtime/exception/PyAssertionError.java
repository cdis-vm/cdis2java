package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyAssertionError extends PyException {
    public PyAssertionError() {
        super();
    }

    public PyAssertionError(String message) {
        super(message);
    }

    public PyAssertionError(PyStr message) {
        super(message);
    }
}
