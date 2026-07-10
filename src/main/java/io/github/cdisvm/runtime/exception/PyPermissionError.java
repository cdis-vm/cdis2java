package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyPermissionError extends PyOSError {
    public PyPermissionError() {
        super();
    }

    public PyPermissionError(String message) {
        super(message);
    }

    public PyPermissionError(PyStr message) {
        super(message);
    }
}
