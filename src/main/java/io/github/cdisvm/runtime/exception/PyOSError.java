package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyOSError extends PyException {
    public PyOSError() {
        super();
    }

    public PyOSError(String message) {
        super(message);
    }

    public PyOSError(PyStr message) {
        super(message);
    }
}
