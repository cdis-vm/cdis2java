package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyKeyError extends PyLookupError {
    public PyKeyError() {
        super();
    }

    public PyKeyError(String message) {
        super(message);
    }

    public PyKeyError(PyStr message) {
        super(message);
    }
}
