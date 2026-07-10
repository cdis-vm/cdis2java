package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyConnectionRefusedError extends PyConnectionError {
    public PyConnectionRefusedError() {
        super();
    }

    public PyConnectionRefusedError(String message) {
        super(message);
    }

    public PyConnectionRefusedError(PyStr message) {
        super(message);
    }
}
