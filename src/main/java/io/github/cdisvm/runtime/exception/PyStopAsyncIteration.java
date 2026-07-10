package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyStopAsyncIteration extends PyException {
    public PyStopAsyncIteration() {
        super();
    }

    public PyStopAsyncIteration(String message) {
        super(message);
    }

    public PyStopAsyncIteration(PyStr message) {
        super(message);
    }
}
