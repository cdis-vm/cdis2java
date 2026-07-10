package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyFileNotFoundError extends PyOSError {
    public PyFileNotFoundError() {
        super();
    }

    public PyFileNotFoundError(String message) {
        super(message);
    }

    public PyFileNotFoundError(PyStr message) {
        super(message);
    }
}
