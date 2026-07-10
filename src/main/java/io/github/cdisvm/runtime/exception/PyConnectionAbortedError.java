package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyConnectionAbortedError extends PyConnectionError {
    public PyConnectionAbortedError() {
        super();
    }

    public PyConnectionAbortedError(String message) {
        super(message);
    }

    public PyConnectionAbortedError(PyStr message) {
        super(message);
    }
}
