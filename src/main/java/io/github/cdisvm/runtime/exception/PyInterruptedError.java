package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyInterruptedError extends PyOSError {
    public PyInterruptedError() {
        super();
    }

    public PyInterruptedError(String message) {
        super(message);
    }

    public PyInterruptedError(PyStr message) {
        super(message);
    }
}
