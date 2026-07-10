package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyUnicodeTranslateError extends PyUnicodeError {
    public PyUnicodeTranslateError() {
        super();
    }

    public PyUnicodeTranslateError(String message) {
        super(message);
    }

    public PyUnicodeTranslateError(PyStr message) {
        super(message);
    }
}
