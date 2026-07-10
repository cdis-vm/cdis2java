package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PySystemError extends PyException {
    public PySystemError() {
        super();
    }

    public PySystemError(String message) {
        super(message);
    }

    public PySystemError(PyStr message) {
        super(message);
    }
}
