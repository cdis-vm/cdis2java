package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyAttributeError extends PyException {
    public PyAttributeError() {
        super();
    }

    public PyAttributeError(String message) {
        super(message);
    }

    public PyAttributeError(PyStr message) {
        super(message);
    }
}
