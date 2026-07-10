package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyUnicodeEncodeError extends PyUnicodeError {
    public PyUnicodeEncodeError() {
        super();
    }

    public PyUnicodeEncodeError(String message) {
        super(message);
    }

    public PyUnicodeEncodeError(PyStr message) {
        super(message);
    }
}
