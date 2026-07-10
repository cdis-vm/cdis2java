package io.github.cdisvm.runtime.exception;

import io.github.cdisvm.runtime.builtin.PyStr;

public class PyOverflowError extends PyArithmeticError {
    public PyOverflowError() {
        super();
    }

    public PyOverflowError(String message) {
        super(message);
    }

    public PyOverflowError(PyStr message) {
        super(message);
    }
}
