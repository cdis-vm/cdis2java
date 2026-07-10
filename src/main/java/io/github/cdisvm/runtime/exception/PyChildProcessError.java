package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyChildProcessError extends PyOSError {
    public PyChildProcessError() {
        super();
    }

    public PyChildProcessError(String message) {
        super(message);
    }

    public PyChildProcessError(PyStr message) {
        super(message);
    }
}
