package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyBlockingIOError extends PyOSError {
    public PyBlockingIOError() {
        super();
    }

    public PyBlockingIOError(String message) {
        super(message);
    }

    public PyBlockingIOError(PyStr message) {
        super(message);
    }
}
