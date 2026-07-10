package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyBufferError extends PyException {
    public PyBufferError() {
        super();
    }

    public PyBufferError(String message) {
        super(message);
    }

    public PyBufferError(PyStr message) {
        super(message);
    }
}
