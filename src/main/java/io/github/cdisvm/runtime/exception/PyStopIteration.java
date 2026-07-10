package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyStopIteration extends PyException {
    public PyStopIteration() {
        super();
    }

    public PyStopIteration(String message) {
        super(message);
    }

    public PyStopIteration(PyStr message) {
        super(message);
    }
}
