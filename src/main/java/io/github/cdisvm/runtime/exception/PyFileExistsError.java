package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyFileExistsError extends PyOSError {
    public PyFileExistsError() {
        super();
    }

    public PyFileExistsError(String message) {
        super(message);
    }

    public PyFileExistsError(PyStr message) {
        super(message);
    }
}
