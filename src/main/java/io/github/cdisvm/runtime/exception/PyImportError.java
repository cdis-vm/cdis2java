package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyImportError extends PyException {
    public PyImportError() {
        super();
    }

    public PyImportError(String message) {
        super(message);
    }

    public PyImportError(PyStr message) {
        super(message);
    }
}
