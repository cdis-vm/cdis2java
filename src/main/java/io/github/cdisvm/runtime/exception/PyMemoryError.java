package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyMemoryError extends PyException {
    public PyMemoryError() {
        super();
    }

    public PyMemoryError(String message) {
        super(message);
    }

    public PyMemoryError(PyStr message) {
        super(message);
    }
}
