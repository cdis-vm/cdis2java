package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyConnectionResetError extends PyConnectionError {
    public PyConnectionResetError() {
        super();
    }

    public PyConnectionResetError(String message) {
        super(message);
    }

    public PyConnectionResetError(PyStr message) {
        super(message);
    }
}
