package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyKeyboardInterrupt extends PyBaseException {
    public PyKeyboardInterrupt() {
        super();
    }

    public PyKeyboardInterrupt(String message) {
        super(message);
    }

    public PyKeyboardInterrupt(PyStr message) {
        super(message);
    }
}
