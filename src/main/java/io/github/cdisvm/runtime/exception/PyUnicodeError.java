package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyUnicodeError extends PyValueError {
    public PyUnicodeError() {
        super();
    }

    public PyUnicodeError(String message) {
        super(message);
    }

    public PyUnicodeError(PyStr message) {
        super(message);
    }
}
