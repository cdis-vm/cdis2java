package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyTimeoutError extends PyOSError {
    public PyTimeoutError() {
        super();
    }

    public PyTimeoutError(String message) {
        super(message);
    }

    public PyTimeoutError(PyStr message) {
        super(message);
    }
}
