package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyUnboundLocalError extends PyNameError {
    public PyUnboundLocalError() {
        super();
    }

    public PyUnboundLocalError(String message) {
        super(message);
    }

    public PyUnboundLocalError(PyStr message) {
        super(message);
    }
}
