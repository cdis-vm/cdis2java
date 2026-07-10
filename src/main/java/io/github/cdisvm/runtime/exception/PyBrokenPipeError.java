package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyBrokenPipeError extends PyConnectionError {
    public PyBrokenPipeError() {
        super();
    }

    public PyBrokenPipeError(String message) {
        super(message);
    }

    public PyBrokenPipeError(PyStr message) {
        super(message);
    }
}
