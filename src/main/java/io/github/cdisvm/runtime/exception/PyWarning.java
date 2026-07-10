package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyWarning extends PyException {
    public PyWarning() {
        super();
    }

    public PyWarning(String message) {
        super(message);
    }

    public PyWarning(PyStr message) {
        super(message);
    }
}
