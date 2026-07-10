package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyEOFError extends PyException {
    public PyEOFError() {
        super();
    }

    public PyEOFError(String message) {
        super(message);
    }

    public PyEOFError(PyStr message) {
        super(message);
    }
}
