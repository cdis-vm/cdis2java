package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyIsADirectoryError extends PyOSError {
    public PyIsADirectoryError() {
        super();
    }

    public PyIsADirectoryError(String message) {
        super(message);
    }

    public PyIsADirectoryError(PyStr message) {
        super(message);
    }
}
