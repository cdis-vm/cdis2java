package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyConnectionError extends PyOSError {
    public PyConnectionError() {
        super();
    }

    public PyConnectionError(String message) {
        super(message);
    }

    public PyConnectionError(PyStr message) {
        super(message);
    }
}
