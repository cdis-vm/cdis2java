package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyNameError extends PyException {
    public PyNameError() {
        super();
    }

    public PyNameError(String message) {
        super(message);
    }

    public PyNameError(PyStr message) {
        super(message);
    }
}
