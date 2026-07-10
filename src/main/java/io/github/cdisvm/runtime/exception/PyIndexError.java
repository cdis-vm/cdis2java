package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyIndexError extends PyLookupError {
    public PyIndexError() {
        super();
    }

    public PyIndexError(String message) {
        super(message);
    }

    public PyIndexError(PyStr message) {
        super(message);
    }
}
