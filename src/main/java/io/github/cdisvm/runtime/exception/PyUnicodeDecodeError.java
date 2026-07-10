package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyUnicodeDecodeError extends PyUnicodeError {
    public PyUnicodeDecodeError() {
        super();
    }

    public PyUnicodeDecodeError(String message) {
        super(message);
    }

    public PyUnicodeDecodeError(PyStr message) {
        super(message);
    }
}
