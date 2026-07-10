package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyLookupError extends PyException {
    public PyLookupError() {
        super();
    }

    public PyLookupError(String message) {
        super(message);
    }

    public PyLookupError(PyStr message) {
        super(message);
    }
}
