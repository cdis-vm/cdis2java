package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PySyntaxError extends PyException {
    public PySyntaxError() {
        super();
    }

    public PySyntaxError(String message) {
        super(message);
    }

    public PySyntaxError(PyStr message) {
        super(message);
    }
}
