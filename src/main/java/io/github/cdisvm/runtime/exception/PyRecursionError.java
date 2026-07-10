package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyRecursionError extends PyRuntimeError {
    public PyRecursionError() {
        super();
    }

    public PyRecursionError(String message) {
        super(message);
    }

    public PyRecursionError(PyStr message) {
        super(message);
    }
}
