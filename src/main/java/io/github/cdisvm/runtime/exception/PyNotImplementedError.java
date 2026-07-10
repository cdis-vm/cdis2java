package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyNotImplementedError extends PyRuntimeError {
    public PyNotImplementedError() {
        super();
    }

    public PyNotImplementedError(String message) {
        super(message);
    }

    public PyNotImplementedError(PyStr message) {
        super(message);
    }
}
