package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyNotADirectoryError extends PyOSError {
    public PyNotADirectoryError() {
        super();
    }

    public PyNotADirectoryError(String message) {
        super(message);
    }

    public PyNotADirectoryError(PyStr message) {
        super(message);
    }
}
